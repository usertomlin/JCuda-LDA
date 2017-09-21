package org.linchimin.jcudalda;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.ejml_float.simple.SimpleFloatMatrix;
import org.linchimin.common.Dictionary;
import org.linchimin.jcuda.utils.CudaIntsMatrix;
import org.linchimin.jcuda.utils.CudaMatrix;
import org.linchimin.jcuda.utils.JCudaManager;

import gnu.trove.TIntArrayList;



/**
 * data structure for a trained LDA model
 * 
 * <pre>
 * 
 * 1. Use LDAGPUTrainer to train an LDAModel; see TrainAndUseExamples.java for examples
 * 2. Call method 'inferTopics' for efficient parallel inference of topic vectors 
 * for a collection of texts 
 * 
 * </pre>
 * @author Lin Chi-Min (v381654729@gmail.com)
 *
 */
public class LDAModel {
	
	private static final int DEFAULT_NUM_ITERATIONS_FOR_INFERENCE = 30;
	private static final float DEFAULT_ALPHA_FOR_INFERENCE = 0.05f;
	
	/**
	 * a K * V matrix; probabilities of words given topics 
	 */
	private SimpleFloatMatrix phisHost;

	/**
	 * a K * V matrix; probabilities of words given topics
	 * phis matrix on device
	 */
	private CudaMatrix phis;
	
	/**
	 * number of topics
	 */
	private final int K;

	/** non-null if using the second constructor
	 */
	private Dictionary dictionary;

	/** non-null if using the second constructor
	 */
	private CorpusProcessor corpusProcessor;


	
	private LDAModel(String phisSerPath) {
		this.phisHost = SimpleFloatMatrix.loadFromBinary(phisSerPath);
		this.phis = new CudaMatrix(phisHost, false);
		this.K = phisHost.numRows();
	}

	/**
	 * @param phisSerPath : stores the phis matrix, the probabilities of words given topics 
	 * @param vocabularyFilePath : the vocabulary words of this model; 
	 */
	public LDAModel(String phisSerPath, String vocabularyFilePath) {
		this(phisSerPath);
		this.dictionary = Dictionary.loadFromWordList(vocabularyFilePath);
		this.corpusProcessor = new CorpusProcessor(dictionary);
	}
	
	
	
	/**
	 * 1. print some topic words for each topic
	 * 2. for debugging whether the training process was run normally as expected 
	 * 
	 * @param phisSerPath
	 */
	public void checkTopicsWords(int numTopWordsOfATopic) {
		
		long startTime = System.currentTimeMillis();
		
		System.out.println("LDAGPUTrainer.collectDominantWordsOfTopics() "
				+ "Time taken to run this part: "
				+ (System.currentTimeMillis() - startTime) + " milliseconds.");
		
		SimpleFloatMatrix phisCopy = phisHost.copy();
		SimpleFloatMatrix a1xVMatrix = phisCopy.sumRows();
		phisCopy.divideRowEquals(a1xVMatrix);
		
		for (int i = 0; i < K; ++i) {
			System.out.println("Topic = " + i  + ": ");
			SimpleFloatMatrix row = phisCopy.getRow(i);
			
			// Suppose the words are sorted by frequency, penalized less frequent words and very frequent words
			for (int j = 0; j < row.getNumElements(); j++) {
				if (j < 70) {
					row.set(j, 0);	
				}
//				row.set(j, (float) (row.get(j) / Math.log(j + Math.E)));
				row.set(j, (float) (row.get(j) / Math.pow(j + 1, 0.2)));
			}
			
			int[] indices = row.argSort(true);
			for (int j = 0; j < numTopWordsOfATopic; ++j) {
				int wordIndex = indices[j];
				System.out.print(dictionary.lookupValue(wordIndex) + ", ");
				if ((j + 1) % 5 == 0) {
					System.out.println();
				}
			}
			System.out.println("\n----------------------------------------------------------");
		}		
	}

	//////////////////////////////////////////////////////////
	

	
	/**
	 * A method for distributed representation for an English text. 
	 * To infer topics for multiple texts, it's much faster to call inferTopics(enTexts) below
	 * @param enText : an English text
	 * @return a topic vector of the input English texts
	 */
	public float[] inferTopics(String enText) {
		return inferTopics(Collections.singletonList(enText))[0];
	}

	
	/**
	 * A method for efficient parallel distributed representation for multiple English texts.
	 * Word tokenization is based on smile.nlp.tokenizer.PennTreebankTokenizer
	 * @param enTexts : a set of English texts 
	 * @return topic vectors for the input English texts
	 */
	public float[][] inferTopics(String... enTexts){
		return inferTopics(Arrays.asList(enTexts));
	}
	
	
	/**
	 * A method for efficient parallel distributed representation for multiple English texts.
	 * Word tokenization is based on smile.nlp.tokenizer.PennTreebankTokenizer
	 * @param enTexts : a set of English texts 
	 * @return topic vectors for the input English texts 
	 */
	public float[][] inferTopics(Collection<String> enTexts) {
		
		int M = enTexts.size();	//num documents in this mini-batch
		if (M == 0){
			return new float[0][];
		}
		
		TIntArrayList documentsWordIndicesList = new TIntArrayList(1000);
		int[] documentWordCounts = new int[M];
		
		int count = 0;
		for (String enText : enTexts) {
			int[] documentWordIndices = corpusProcessor.lookupTextWordsIndices(enText);
			documentsWordIndicesList.add(documentWordIndices);
			documentWordCounts[count++] = documentWordIndices.length;
		}
		int[] documentWordOffsets = LDAUtils.cumulativeSums(documentWordCounts, true);
		int[] documentWordIndices = documentsWordIndicesList.toNativeArray();
		
		float[][] topicVectors = inferTopics(documentWordCounts, documentWordOffsets, documentWordIndices, M);
		return topicVectors;
	}

	
	
	
	/**
	 * A method for efficient parallel distributed representation for multiple English texts.
	 * @param enTokenizedTexts : each 'ArrayList&lt;String&gt;' is the tokenized words of a text 
	 * @return topic vectors for the input texts
	 */
	public float[][] inferTopics(List<ArrayList<String>> enTokenizedTexts) {
		int M = enTokenizedTexts.size();	//num documents in this mini-batch
		if (M == 0){
			return new float[0][];
		}
		
		TIntArrayList documentsWordIndicesList = new TIntArrayList(1000);
		int[] documentWordCounts = new int[M];
		
		int count = 0;
		for (ArrayList<String> textWords : enTokenizedTexts) {
			int[] documentWordIndices = corpusProcessor.lookupWordIndices(textWords);
			documentsWordIndicesList.add(documentWordIndices);
			documentWordCounts[count++] = documentWordIndices.length;
		}
		int[] documentWordOffsets = LDAUtils.cumulativeSums(documentWordCounts, true);
		int[] documentWordIndices = documentsWordIndicesList.toNativeArray();
		
		float[][] topicVectors = inferTopics(documentWordCounts, documentWordOffsets, documentWordIndices, M);
		return topicVectors;
	}
	
	/**
	 * A method for efficient parallel distributed representation for multiple English texts.
	 * @param enTokenizedTexts : each 'ArrayList&lt;String&gt;' is the tokenized words of a text 
	 * @return topic vectors for the input texts
	 */
	public float[][] inferTopics(int[][] textsWordIndices) {
		int M = textsWordIndices.length;	//num documents in this mini-batch
		if (M == 0){
			return new float[0][];
		}
		
		TIntArrayList documentsWordIndicesList = new TIntArrayList(1000);
		int[] documentWordCounts = new int[M];
		
		int count = 0;
		for (int[] textWordIndices : textsWordIndices) {
			documentsWordIndicesList.add(textWordIndices);
			documentWordCounts[count++] = textWordIndices.length;
		}
		int[] documentWordOffsets = LDAUtils.cumulativeSums(documentWordCounts, true);
		int[] documentWordIndices = documentsWordIndicesList.toNativeArray();
		
		float[][] topicVectors = inferTopics(documentWordCounts, documentWordOffsets, documentWordIndices, M);
		return topicVectors;
	}
	
	/**
	 * @param documentWordCounts
	 * @param documentWordOffsets
	 * @param documentWordIndices
	 * @return topic vectors
	 */
	private float[][] inferTopics(int[] documentWordCounts, int[] documentWordOffsets, int[] documentWordIndices, int M) {
		CudaIntsMatrix tpd = new CudaIntsMatrix(M, K);
		CudaMatrix thetas = new CudaMatrix(M, K);
		CudaIntsMatrix td = new CudaIntsMatrix(1, M);
		
		// both with length 1000
		CudaIntsMatrix docsWordCounts = new CudaIntsMatrix(1, M, documentWordCounts);
		CudaIntsMatrix docsWordOffsets = new CudaIntsMatrix(1, M + 1, documentWordOffsets);
		
		// with length around 2000000
		CudaIntsMatrix docsWordIndices = new CudaIntsMatrix(1, documentWordIndices.length, documentWordIndices);

		
		LDAUtils.computeThetas(tpd, td, thetas, DEFAULT_ALPHA_FOR_INFERENCE , DEFAULT_ALPHA_FOR_INFERENCE  * K, M, M * K);
		
		for (int i = 0; i < DEFAULT_NUM_ITERATIONS_FOR_INFERENCE; i++) {
			tpd.setElements(0);
			td.setElements(0);
			JCudaManager.synchronize();
			
			if (M * K < 12000){
				LDAUtils.drawLatentVariablesForTestingQuick(docsWordCounts, docsWordOffsets, docsWordIndices, 
						tpd, td, phis, thetas, 0, K, M, M);
			} else {
				LDAUtils.drawLatentVariablesForTesting(docsWordCounts, docsWordOffsets, docsWordIndices, 
						tpd, td, phis, thetas, 0, K, M, M);
			}
			LDAUtils.computeThetas(tpd, td, thetas, DEFAULT_ALPHA_FOR_INFERENCE , DEFAULT_ALPHA_FOR_INFERENCE  * K, M, M * K);
		}

		
		SimpleFloatMatrix inferredTopics = thetas.toSimpleFloatMatrix();
		float[][] result = new float[M][];
		for (int i = 0; i < M; i++) {
			SimpleFloatMatrix row = inferredTopics.getRow(i);
			result[i] = row.getData();
		}

		tpd.free();
		thetas.free();
		td.free();
		docsWordCounts.free();
		docsWordOffsets.free();
		docsWordIndices.free();
		
		return result;
	}

	

	
	
}
