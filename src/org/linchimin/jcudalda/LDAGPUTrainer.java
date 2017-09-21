package org.linchimin.jcudalda;

import java.util.ArrayList;
import java.util.List;

import org.ejml_float.simple.SimpleFloatMatrix;
import org.linchimin.common.Dictionary;
import org.linchimin.jcuda.utils.CudaIntsMatrix;
import org.linchimin.jcuda.utils.CudaMatrix;
import org.linchimin.jcuda.utils.JCudaManager;
import org.linchimin.utils.ArgumentChecker;
import org.linchimin.utils.FileUtils;

import gnu.trove.TIntArrayList;


/**
 * <pre>
 * 
 * 1. This implementation is based on 
 * http://machinelearning.wustl.edu/mlpapers/paper_files/icml2015_tristan15.pdf
 * "Efficient Training of LDA on a GPU by Mean-for-Mode Estimation"
 * 
 * 2. 
 * Method 'trainWithIntsCorpus' supports training an LDA with processed ints documents; 
 * see TrainAndUseExample.java and ProcessIntsDocumentsExample.java
 *   
 * Method 'trainWithTextsCorpus' supports training an LDA model with raw texts files of format like "resources/example-docs.txt 
 * 
 * It's recommended to use 'trainWithIntsCorpus' to train, which is by far faster than 'trainWithTextsCorpus'.
 * 
 * </pre>
 * 
 * @author Lin Chi-Min (v381654729@gmail.com)
 *
 */
public class LDAGPUTrainer {
	
	private static final int DEFAULT_NUM_DOCUMENTS_IN_ONE_BATCH = 20000;
	
	private static final int MAX_MINIBATCH_NUM_WORDS = 8000000;
	
	private static final float DEFAULT_ALPHA = 0.1f;
	private static final float DEFAULT_BETA = 0.01f;
	
	public static final String UNKNOWN_TOKEN = "UUUNKKK";
	
	
	private Dictionary dictionary;
	
	/**
	 * K : number of topics 
	 */
	private final int K;

	/**
	 * K : number of vocabulary words 
	 */
	private final int V;
	
	/**
	 * M : number of documents used for training
	 */
	private final int M;

	
	/**
	 * the alpha parameter
	 */
	private float alpha = DEFAULT_ALPHA;
	
	/**
	 * the beta parameter
	 */
	private float beta  = DEFAULT_BETA;
	
	
	/**
	 * constructor that supports both 'trainWithIntsCorpus' and 'trainWithTextsCorpus'
	 * 
	 * 
	 * @param vocabularyFilePath : 
	 * load a dictionary used for method 'trainWithTextsCorpus', 
	 * only consider words in 'vocabularyFilePath' 
	 * @param K : numer of topics
	 * @param M : number of top documents used for training
	 */
	public LDAGPUTrainer(String vocabularyFilePath, int K, int M) {
		
		this.dictionary = Dictionary.loadFromWordList(vocabularyFilePath);
		if (dictionary.containsWord(UNKNOWN_TOKEN) == false){
			dictionary.add(UNKNOWN_TOKEN);
		}
		this.V = dictionary.size();
		this.K = K;
		this.M = M;
	}
	
	
	/**
	 * Warning : supports only 'trainWithIntsCorpus'. 
	 * To call 'trainWithTextsCorpus' without NullPointerException,
	 * use constructor LDAGPUTrainer(String vocabularyFilePath, int K, int M)
	 * 
	 * @param V : the vocabualry size; word indices < 0 or >= V are ignored. 
	 * @param K : numer of topics
	 * @param M : number of top documents used for training
	 */
	public LDAGPUTrainer(int V, int K, int M) {
		
		this.dictionary = null;
		this.V = V;
		this.K = K;
		this.M = M;
	}

	public void setAlpha(float alpha) {
		this.alpha = alpha;
	}
	
	public void setBeta(float beta) {
		this.beta = beta;
	}
	
	public float getAlpha() {
		return alpha;
	}
	
	public float getBeta() {
		return beta;
	}
	

	/**
	 * method for training an LDA model with a directory of many raw text files of format similar to "resources/example-docs.txt"
	 */
	public void trainWithTextsCorpus(String textsCorpusDirectory, String resultPhisSerPath, int numIterations, int numDocumentsInOneMiniBatch) {
		
		CudaIntsMatrix wpt = new CudaIntsMatrix(K, V);
		CudaMatrix phis = new CudaMatrix(K, V);
		CudaIntsMatrix tpd = new CudaIntsMatrix(M, K);
		CudaMatrix thetas = new CudaMatrix(M, K);
		
		CudaIntsMatrix wt = new CudaIntsMatrix(1, K);
		CudaIntsMatrix td = new CudaIntsMatrix(1, M);
		
		// both with length 1000
		CudaIntsMatrix docsWordCounts = new CudaIntsMatrix(1, numDocumentsInOneMiniBatch);
		CudaIntsMatrix docsWordOffsets = new CudaIntsMatrix(1, numDocumentsInOneMiniBatch + 1);
		
		// with length around 2000000
		CudaIntsMatrix docsWordIndices = new CudaIntsMatrix(1, MAX_MINIBATCH_NUM_WORDS);
		
		long startTime = System.currentTimeMillis();

		LDAUtils.computePhis(wpt, wt, phis, beta, beta * V, K, K * V);
		LDAUtils.computeThetas(tpd, td, thetas, alpha, alpha * K, M, M * K);
		
		for (int iteration = 1; iteration <= numIterations; ++iteration) {

			wpt.setElements(0);
			tpd.setElements(0);
			wt.setElements(0);
			td.setElements(0);
			JCudaManager.synchronize();
			
			int docOffset = 0;
			
			
//			for (String textFilePath : textsFilePaths) {
			
			CorpusTextsIterator iterator = new CorpusTextsIterator(textsCorpusDirectory, numDocumentsInOneMiniBatch, dictionary);
			
			
			while (iterator.hasNext()) {
			
				ArrayList<int[]> miniBatchDocs = iterator.next();
				if (miniBatchDocs.size() == 0){
					continue;
				}
				
				int[] documentWordCounts = new int[miniBatchDocs.size()];
				TIntArrayList documentsWordIndicesList = new TIntArrayList(5000000);
				for (int i = 0; i < miniBatchDocs.size(); ++i) {
					int[] documentWordIndices = miniBatchDocs.get(i);
//						documentWordIndices = ArrayUtils.removeAll(documentWordIndices, -1);
					documentsWordIndicesList.add(documentWordIndices);
					documentWordCounts[i] = documentWordIndices.length;
				}
				
				
				/**cumulative sums of 'documentWordCounts'
				 */
				int[] documentWordOffsets = LDAUtils.cumulativeSums(documentWordCounts, true);
				if (documentsWordIndicesList.size() > docsWordIndices.getNumElements()){
					docsWordIndices.free();
					docsWordIndices = new CudaIntsMatrix(1, documentsWordIndicesList.size() * 2);
				}
				
				docsWordCounts.copyFrom(documentWordCounts);
				docsWordOffsets.copyFrom(documentWordOffsets);
				docsWordIndices.copyFrom(documentsWordIndicesList.toNativeArray());
				
				System.out.println("iteration = " + iteration + ", docOffset = " + docOffset + ", num documents words = " + documentWordOffsets[documentWordOffsets.length - 1] );
				
				LDAUtils.drawLatentVariables(docsWordCounts, docsWordOffsets, docsWordIndices, 
						wpt, wt, tpd, td, phis, thetas, docOffset, K, M, V, miniBatchDocs.size());
				docOffset += miniBatchDocs.size();
				
				if (docOffset >= M){
					break;
				}
				
				if (miniBatchDocs.size() != numDocumentsInOneMiniBatch){
					break;
				}
				
			}
			
			/**do for each iteration
			 */
			LDAUtils.computePhis(wpt, wt, phis, beta, beta * V, K, K * V);
			LDAUtils.computeThetas(tpd, td, thetas, alpha, alpha * K, M, M * K);
			
		} 	// end of for (int iteration = 1; iteration <= numIterations; ++iteration) {

		docsWordCounts.free();
		docsWordOffsets.free();
		docsWordIndices.free();
		
//		phis.scaleEquals(1000);
		SimpleFloatMatrix matrix = phis.toSimpleFloatMatrix();
		matrix.saveToFileBinary(resultPhisSerPath);
		
		System.out.println("LDAGPUTrainer.trainWithTextsCorpus() Time taken to run this part: "
		+ (System.currentTimeMillis() - startTime) / 1000f  + " seconds.");
		
		System.exit(0);
	}
	
	
	
	/**
	 * Similar to trainWithIntsCorpus(String intsCorpusDirectory, String resultPhisSerPath, int numIterations, int numDocumentsInOneMiniBatch)
	 */
	public void trainWithIntsCorpus(String intsCorpusDirectory, String resultPhisSerPath, int numIterations){
		trainWithIntsCorpus(intsCorpusDirectory, resultPhisSerPath, numIterations, DEFAULT_NUM_DOCUMENTS_IN_ONE_BATCH);
	}
	
	
	
	/**
	 * method for training an LDA model with a directory of serializable files of ArrayList<int[]> documents
	 * 
	 * @param intsCorpusDirectory :
	 * <pre> 
	 * a directory containing a list of serializable files,  
	 * each of which is a serialized ArrayList<int[]> documents where each int[] contains word indices of a document
	 * 
	 * For example, suppose
	 * ArrayList<int[]> docs = ObjectSerializer.deserialize("D:\\Corpora\\wiki\\ints-enwiki-45k\\1.ser" )
	 * If docs.size() is 10000, it contains 10000 documents.
	 * And if docs.get(0) is {1, 3, 100, 30, 20, ...}, the document contains these word indices 
	 * </pre>
	 * @param resultPhisSerPath : the produced 'phis' matrix when training is done
	 * @param numIterations : number of iterations
	 * @param numDocumentsInOneMiniBatch : the larger the faster and more memory it takes
	 */
	public void trainWithIntsCorpus(String intsCorpusDirectory, String resultPhisSerPath, int numIterations, int numDocumentsInOneMiniBatch) {

		long startTime = System.currentTimeMillis();

		List<String> serFilePaths = FileUtils.getFileListRecursively(intsCorpusDirectory);
		
		System.out.println("LDAGPUTrainer.trainWithIntsCorpus() serFilePaths.size() = " + serFilePaths.size());
		
		CudaIntsMatrix wpt = new CudaIntsMatrix(K, V);
		CudaMatrix phis = new CudaMatrix(K, V);
		CudaIntsMatrix tpd = new CudaIntsMatrix(M, K);
		CudaMatrix thetas = new CudaMatrix(M, K);
		
		CudaIntsMatrix wt = new CudaIntsMatrix(1, K);
		CudaIntsMatrix td = new CudaIntsMatrix(1, M);
		
		// both with length 1000
		CudaIntsMatrix docsWordCounts = new CudaIntsMatrix(1, numDocumentsInOneMiniBatch);
		CudaIntsMatrix docsWordOffsets = new CudaIntsMatrix(1, numDocumentsInOneMiniBatch + 1);
		
		// with length around 2000000
		CudaIntsMatrix docsWordIndices = new CudaIntsMatrix(1, MAX_MINIBATCH_NUM_WORDS);
		

		LDAUtils.computePhis(wpt, wt, phis, beta, beta * V, K, K * V);
		LDAUtils.computeThetas(tpd, td, thetas, alpha, alpha * K, M, M * K);
		
		
		
		for (int iteration = 1; iteration <= numIterations; ++iteration) {

			wpt.setElements(0);
			tpd.setElements(0);
			wt.setElements(0);
			td.setElements(0);
			JCudaManager.synchronize();
			
			int docOffset = 0;
			
			CorpusIntsIterator iterator = new CorpusIntsIterator(intsCorpusDirectory, numDocumentsInOneMiniBatch);
			
			while (iterator.hasNext()){
				ArrayList<int[]> miniBatchDocsIndices = iterator.next();
				if (miniBatchDocsIndices.isEmpty()){
					continue;
				}
				
				int[] documentWordCounts = new int[miniBatchDocsIndices.size()];
				TIntArrayList documentsWordIndicesList = new TIntArrayList(5000000);
				for (int i = 0; i < miniBatchDocsIndices.size(); ++i) {
					int[] documentWordIndices = miniBatchDocsIndices.get(i);
					documentsWordIndicesList.add(documentWordIndices);
					documentWordCounts[i] = documentWordIndices.length;
				}
				
				int[] documentsWordIndicesArray = documentsWordIndicesList.toNativeArray();
				ArgumentChecker.checkAllInBounds(documentsWordIndicesArray, 0, V);
				
				
				/**cumulative sums of 'documentWordCounts'
				 */
				int[] documentWordOffsets = LDAUtils.cumulativeSums(documentWordCounts, true);
				if (documentsWordIndicesList.size() > docsWordIndices.getNumElements()){
					docsWordIndices.free();
					docsWordIndices = new CudaIntsMatrix(1, documentsWordIndicesList.size() * 2);
				}
				
				docsWordCounts.copyFrom(documentWordCounts);
				docsWordOffsets.copyFrom(documentWordOffsets);
				docsWordIndices.copyFrom(documentsWordIndicesList.toNativeArray());

				System.out.println("iteration = " + iteration + ", docOffset = " + docOffset + ", num documents words = " + documentWordOffsets[documentWordOffsets.length - 1] );
				
				LDAUtils.drawLatentVariables(docsWordCounts, docsWordOffsets, docsWordIndices, 
						wpt, wt, tpd, td, phis, thetas, docOffset, K, M, V, miniBatchDocsIndices.size());
				docOffset += miniBatchDocsIndices.size();
				if (docOffset >= M) {
					break;
				}
			}
			
			
			/**do for each iteration
			 */
			LDAUtils.computePhis(wpt, wt, phis, beta, beta * V, K, K * V);
			LDAUtils.computeThetas(tpd, td, thetas, alpha, alpha * K, M, M * K);
			
			
		} 	// end of for (int iteration = 1; iteration <= numIterations; ++iteration) {

		docsWordCounts.free();
		docsWordOffsets.free();
		docsWordIndices.free();
		
		
		SimpleFloatMatrix matrix = phis.toSimpleFloatMatrix();
		matrix.saveToFileBinary(resultPhisSerPath);
		
		System.out.println("LDAGPUTrainer.trainWithIntsCorpus() Time taken to run this part: "
		+ (System.currentTimeMillis() - startTime) / 1000f  + " seconds.");
		
		System.exit(0);
	}

	
	
}
