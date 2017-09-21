package org.linchimin.jcudalda;

import org.linchimin.jcuda.functions.CudaLDAFunctions;
import org.linchimin.jcuda.pointer.ConstantDeviceData;
import org.linchimin.jcuda.pointer.PointerUtils;
import org.linchimin.jcuda.utils.CudaIntsMatrix;
import org.linchimin.jcuda.utils.CudaMatrix;
import org.linchimin.jcuda.utils.JCudaManager;

import jcuda.Pointer;

/**
 * <pre>
 * a class containing some internal utility methods;
 * 
 * See paper 
 * http://machinelearning.wustl.edu/mlpapers/paper_files/icml2015_tristan15.pdf
 * "Efficient Training of LDA on a GPU by Mean-for-Mode Estimation"
 * for notation details.
 * </pre>
 * 
 * @author Lin Chi-Min (v381654729@gmail.com)
 *
 */
class LDAUtils {
	
	
	/**
	 * <pre>
	 * Example: 
	 * 
	 * K = 30, number of topics
	 * V = 120000, number of vocabulary words
	 * 
	 * wpt[k][v] : words per topic
	 * wt[k] : total words per topic
	 * 
	 * </pre>
	 * 
	 * @param wpt : a K * V matrix; words per topic, or counts of words given topics
	 * @param wt : a K dimension vector, total counts of words given topics
	 * @param phis : a K * V matrix; probabilities of words given topics
	 * @param beta : the beta parameter 
	 * @param betaV : beta * V
	 * @param K : the K parameter
	 * @param numElements_KxV : K * V
	 * 
	 */
	public static void computePhis(
			CudaIntsMatrix wpt,
			CudaIntsMatrix wt,
			CudaMatrix phis,
			float beta,
			float betaV,
			int K,
			int numElements_KxV) {
		Pointer kernelParameters = Pointer.to(
				PointerUtils.to(wpt),
				PointerUtils.to(wt),
				PointerUtils.to(phis),
				PointerUtils.to(beta),
				PointerUtils.to(betaV),
				PointerUtils.to(K),
				PointerUtils.to(numElements_KxV)
		);

		JCudaManager.launchKernelByDefault(CudaLDAFunctions.computePhis, 
				kernelParameters, numElements_KxV);
	}

	
	
	/**
	 * <pre>
	 * 
	 * M = 200000, number of documents for training
	 * K = 30, number of topics
	 * 
	 * tpd[m][k] : topics per document
	 * td[m] : total topics per document
	 * 
	 * </pre>
	 * 
	 * @param tpd : a M * K matrix; topics per documents, or counts of topics given documents
	 * @param td : a M dimension vector, total counts of topics given documents
	 * @param thetas : a M * K matrix; probabilities of topics given documents
	 * @param alpha : a M * K matrix; probabilities of topics given documents
	 * @param alphaK : alpha * K 
	 * @param M : the M parameter
	 * @param numElements_MxK : M * K 
	 */
	protected static void computeThetas(
			CudaIntsMatrix tpd,
			CudaIntsMatrix td,
			CudaMatrix thetas,
			float alpha,
			float alphaK,
			int M,
			int numElements_MxK) {

		Pointer kernelParameters = Pointer.to(
				PointerUtils.to(tpd),
				PointerUtils.to(td),
				PointerUtils.to(thetas),
				PointerUtils.to(alpha),
				PointerUtils.to(alphaK),
				PointerUtils.to(M),
				PointerUtils.to(numElements_MxK)
				);

		JCudaManager.launchKernelByDefault(CudaLDAFunctions.computeThetas, 
				kernelParameters, numElements_MxK);
	}
	
	

	
	/**
	 * <pre>
	 * draw latent variables for a mini-batch of documents of size 'numDocumentsInOneBatch'
	 * 
	 * wpt[k][v] : words per topic
	 * wt[k] : total words per topic
	 * tpd[m][k] : topics per document
	 * td[m] : total topics per document
	 * 
	 * wpt and phis : a K * V matrix
	 * tpd and thetas : a M * K matrix
	 * </pre>
	 * 	
	 * @param docsWordCounts : a device vector that stores word counts of documents  
	 * @param docsWordOffsets : a device vector that stores word offsets of documents 
	 * @param docsWordIndices : a device vector that stores word indices for the current mini-batch of documents
	 * @param wpt : a K * V matrix; words per topic, or counts of words given topics
	 * @param wt : a K dimension vector, total counts of words given topics
	 * @param tpd : a M * K matrix; topics per documents, or counts of topics given documents
	 * @param td : a M dimension vector, total counts of topics given documents
	 * @param phis : a K * V matrix; probabilities of words given topics
	 * @param thetas : a M * K matrix; probabilities of topics given documents 
	 * @param docOffset : document index offset for the input mini-batch 
	 * @param K : number of topics 
	 * @param M : number of documents used for training
	 * @param V : number of vocabulary words
	 * @param numDocumentsInOneBatch : numer fo documents in the current mini-batch
	 */
	protected static void drawLatentVariables(
			CudaIntsMatrix docsWordCounts,
			CudaIntsMatrix docsWordOffsets,
			CudaIntsMatrix docsWordIndices,
			CudaIntsMatrix wpt,
			CudaIntsMatrix wt,
			CudaIntsMatrix tpd,
			CudaIntsMatrix td,
			CudaMatrix phis,
			CudaMatrix thetas,
			int docOffset,
			int K, int M, int V,
			int numDocumentsInOneBatch) {

			CudaMatrix pStatic = ConstantDeviceData.getTempMatrix(K * numDocumentsInOneBatch);
		
			int numElements = numDocumentsInOneBatch;

			Pointer kernelParameters = Pointer.to(
				PointerUtils.to(docsWordCounts),
				PointerUtils.to(docsWordOffsets),
				PointerUtils.to(docsWordIndices),
				PointerUtils.to(wpt),
				PointerUtils.to(wt),
				PointerUtils.to(tpd),
				PointerUtils.to(td),
				PointerUtils.to(phis),
				PointerUtils.to(thetas),
				
				PointerUtils.to(pStatic),
				
				PointerUtils.to(docOffset),
				PointerUtils.to(K),
				PointerUtils.to(M),
				PointerUtils.to(V),
				PointerUtils.to(numDocumentsInOneBatch)
			);

			JCudaManager.launchKernelByDefault(CudaLDAFunctions.drawLatentVariables, 
				kernelParameters, numElements);
		}

	
	/**
	 * <pre>
	 * Similar to 'drawLatentVariables' except that the phis matrix is kept fixed, 
	 * and wpt and wp are not needed to be updated
	 * </pre>    
	 * @see drawLatentVariables
	 */
	protected static void drawLatentVariablesForTesting(
			CudaIntsMatrix docsWordCounts,
			CudaIntsMatrix docsWordOffsets,
			CudaIntsMatrix docsWordIndices,
			CudaIntsMatrix tpd,
			CudaIntsMatrix td,
			CudaMatrix phis,
			CudaMatrix thetas,
			int docOffset,
			int K,
			int M,
			int numDocumentsInOneBatch) {
		
		CudaMatrix pStatic = ConstantDeviceData.getTempMatrix(K * numDocumentsInOneBatch);
		
		
		int numElements = numDocumentsInOneBatch;
		Pointer kernelParameters = Pointer.to(
				PointerUtils.to(docsWordCounts),
				PointerUtils.to(docsWordOffsets),
				PointerUtils.to(docsWordIndices),
				PointerUtils.to(tpd),
				PointerUtils.to(td),
				PointerUtils.to(phis),
				PointerUtils.to(thetas),
				PointerUtils.to(pStatic),
				PointerUtils.to(docOffset),
				PointerUtils.to(K),
				PointerUtils.to(M),
				PointerUtils.to(numDocumentsInOneBatch)
				);
		
		JCudaManager.launchKernelByDefault(CudaLDAFunctions.drawLatentVariablesForTesting, 
				kernelParameters, numElements);
	}
	
	/**
	 * <pre>
	 * Similar to 'drawLatentVariablesForTesting' except that shared memory is used to store p
	 * </pre>    
	 * @see drawLatentVariablesForTesting
	 */
	protected static void drawLatentVariablesForTestingQuick(
			CudaIntsMatrix docsWordCounts,
			CudaIntsMatrix docsWordOffsets,
			CudaIntsMatrix docsWordIndices,
			CudaIntsMatrix tpd,
			CudaIntsMatrix td,
			CudaMatrix phis,
			CudaMatrix thetas,
			int docOffset,
			int K,
			int M,
			int numDocumentsInOneBatch) {
			
			int numElements = numDocumentsInOneBatch;
			Pointer kernelParameters = Pointer.to(
					PointerUtils.to(docsWordCounts),
					PointerUtils.to(docsWordOffsets),
					PointerUtils.to(docsWordIndices),
					PointerUtils.to(tpd),
					PointerUtils.to(td),
					PointerUtils.to(phis),
					PointerUtils.to(thetas),
					PointerUtils.to(docOffset),
					PointerUtils.to(K),
					PointerUtils.to(M),
					PointerUtils.to(numDocumentsInOneBatch)
					);
		
			
			JCudaManager.launchKernelByDefault(CudaLDAFunctions.drawLatentVariablesForTestingQuick, 
				kernelParameters, numElements, K * M);
		}

	
	
	/**
	 * @param values : like [1, 3, 1, 121, ..., 5]
	 * @param startingWithZero : 
	 * if true , the results are like [0, 1, 4, 5, 126, ...]; if false, the results are like [1, 4, 5, 126, ...]
	 */
	protected static int[] cumulativeSums(int[] values, boolean startingWithZero){
		final int length = values.length;
		int[] result = startingWithZero ? new int[length + 1] : new int[length];
		if (startingWithZero){
			for (int i = 0; i < length; i++) {
				result[i+1] = result[i] + values[i];
			}
		} else {
			result[0] = values[0];
			for (int i = 1; i < length; i++) {
				result[i] = result[i - 1] + values[i];
			}
		}
		return result;
	}
	

	
}
