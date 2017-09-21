
#include <curand_kernel.h>


/**
 * Some kernel functions for LDA
 * 
 * @author Lin Chi-Min (v381654729@gmail.com)
 */


/**
 * wpt[k][v] : words per topic
 * wt[k] : total words per topic
 * tpd[m][k] : topics per document
 * td[m] : total topics per document
 * 
 * wpt and phis : a K * V matrix
 * tpd and thetas : a M * K matrix
 * 
 * numElements = (num documents in one batch) 
 * 
 * p:  K * numDocumentsInOneBatch
 */
extern "C"
__global__ void drawLatentVariables(
		const int* __restrict__ docsWordCounts, const int* __restrict__ docsWordOffsets, const int* __restrict__ docsWordIndices,
		int* wpt, int* wt, int* tpd, int* td, const float* __restrict__ phis, const float* __restrict__ thetas, 
		float* p,
		int docOffset, int K, int M, int V, int numDocumentsInOneBatch) {
	
	/**
	 * with size numDocumentsInOneBatch * K
	 */
	
	int m = blockDim.x * blockIdx.x + threadIdx.x;
    if (m < numDocumentsInOneBatch) {
    	
    	int Nm = docsWordCounts[m];
    	int docIndex = docOffset + m;
    	int docWordOffset = docsWordOffsets[m];
    	
    	curandState s;
    	// reset a random number generator
    	curand_init(docIndex, 0, 0, &s);
    	
    	int pOffset = m * K;
    	
    	for (int i = 0; i < Nm; i++) {
    		
    		float sum = 0;
    		int c_word = docsWordIndices[docWordOffset + i];
    		if (c_word < 0 || c_word >= V){
    			continue;
    		}
    		int j;
    		for (j = 0; j < K; j++) {
    			sum += phis[j + (c_word * K)] * thetas[docIndex + (j * M)];
    			p[j + pOffset] = sum;
			}
    		float stop = curand_uniform(&s) * sum;
    		for (j = 0; j < K; j++) {
    			if (stop < p[j + pOffset]) {
    				break;
    			}
			}
    		if (j == K){
    			j--;
    		}
    		
    		atomicAdd(&wpt[j + (c_word * K)], 1);
    		atomicAdd(&wt[j], 1);
    		tpd[docIndex + (j * M)]++;
		}
    	td[docIndex] += Nm;
    }
	
}


extern "C"
__global__ void drawLatentVariablesForTestingQuick(
		const int* __restrict__ docsWordCounts, const int* __restrict__ docsWordOffsets, const int* __restrict__ docsWordIndices,
		int* tpd, int* td, const float* __restrict__ phis, const float* __restrict__ thetas, 
		int docOffset, int K, int M, int numDocumentsInOneBatch) {
	
	extern __shared__ float p[];
	
	/**
	 * with size numDocumentsInOneBatch * K
	 */
	
	int m = blockDim.x * blockIdx.x + threadIdx.x;
    if (m < numDocumentsInOneBatch) {
    	
    	int Nm = docsWordCounts[m];
    	int docIndex = docOffset + m;
    	int docWordOffset = docsWordOffsets[m];
    	
    	curandState s;
    	// reset a random number generator
    	curand_init(docIndex, 0, 0, &s);
    	
    	int pOffset = m * K;
    	
    	for (int i = 0; i < Nm; i++) {
    		
    		float sum = 0;
    		int c_word = docsWordIndices[docWordOffset + i];
    		if (c_word < 0){
    			continue;
    		}
    		int j;
    		for (j = 0; j < K; j++) {
    			sum += phis[j + (c_word * K)] * thetas[docIndex + (j * M)];
    			p[j + pOffset] = sum;
			}
    		float stop = curand_uniform(&s) * sum;
    		for (j = 0; j < K; j++) {
    			if (stop < p[j + pOffset]) {
    				break;
    			}
			}
    		if (j == K){
    			j--;
    		}
    		
    		tpd[docIndex + (j * M)]++;
		}
    	td[docIndex] += Nm;
    }
	
}



/**
 * Use this to infer topics for testing;
 * phis are fixed and not updated 
 * 
 */
extern "C"
__global__ void drawLatentVariablesForTesting(
		const int* __restrict__ docsWordCounts, const int* __restrict__ docsWordOffsets, const int* __restrict__ docsWordIndices,
		int* tpd, int* td, const float* __restrict__ phis, const float* __restrict__ thetas, 
		float* p,
		int docOffset, int K, int M, int numDocumentsInOneBatch) {
	
	/**
	 * with size numDocumentsInOneBatch * K
	 */
	
	int m = blockDim.x * blockIdx.x + threadIdx.x;
    if (m < numDocumentsInOneBatch) {
    	
    	int Nm = docsWordCounts[m];
    	int docIndex = docOffset + m;
    	int docWordOffset = docsWordOffsets[m];
    	
    	curandState s;
    	// reset a random number generator
    	curand_init(docIndex, 0, 0, &s);
    	
    	int pOffset = m * K;
    	
    	for (int i = 0; i < Nm; i++) {
    		
    		float sum = 0;
    		int c_word = docsWordIndices[docWordOffset + i];
    		if (c_word < 0){
    			continue;
    		}
    		int j;
    		for (j = 0; j < K; j++) {
    			sum += phis[j + (c_word * K)] * thetas[docIndex + (j * M)];
    			p[j + pOffset] = sum;
			}
    		float stop = curand_uniform(&s) * sum;
    		for (j = 0; j < K; j++) {
    			if (stop < p[j + pOffset]) {
    				break;
    			}
			}
    		if (j == K){
    			j--;
    		}
    		
    		tpd[docIndex + (j * M)]++;
		}
    	td[docIndex] += Nm;
    }
	
}



/**
 * K = 30
 * V = 120000
 * 
 * numElements = K * V
 * 
 * wpt and phis : a K * V matrix
 * 
 * wpt[k][v] : words per topic
 * wt[k] : total words per topic
 * 
 */
extern "C"
__global__ void computePhis(const int* __restrict__ wpt, const int* __restrict__ wt, float* phis, float beta, float betaV, int K, int numElements)
{
	int i = blockDim.x * blockIdx.x + threadIdx.x;
	if (i < numElements) {	
		int k = i % K;
		phis[i] = (wpt[i] + beta) / (wt[k] + betaV);
	}
}


extern "C"
__global__ void computePhisExact(const int* __restrict__ wpt, const int* __restrict__ wt, float* phis, int K, int numElements)
{
	int i = blockDim.x * blockIdx.x + threadIdx.x;
    if (i < numElements) {	
    	int k = i % K;
    	phis[i] = (wpt[i] + 0.0) / wt[k];
    }
}



/**
 * M = 90000
 * K = 30
 * 
 * numElements = M * K
 * 
 * thetas : a M * K matrix
 * 
 * tpd[m][k] : topics per document
 * td[m] : total topics per document
 */
extern "C"
__global__ void computeThetas(const int* __restrict__  tpd, const int* __restrict__ td, float* thetas, 
		float alpha, float alphaK, int M, int numElements)
{
	int i = blockDim.x * blockIdx.x + threadIdx.x;
    if (i < numElements) {
    	int m = i % M;
    	thetas[i] = (tpd[i] + alpha) / (td[m] + alphaK);
    }
}





