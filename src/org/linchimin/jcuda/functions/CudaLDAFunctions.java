package org.linchimin.jcuda.functions;

import jcuda.driver.CUfunction;

/**
 * @author Lin Chi-Min (v381654729@gmail.com)
 */
public class CudaLDAFunctions extends AbstractCudaModule {

	
	public static CUfunction drawLatentVariables;
	public static CUfunction drawLatentVariablesForTesting;
	public static CUfunction drawLatentVariablesForTestingQuick;

	
	public static CUfunction computePhis;
	public static CUfunction computeThetas;
	

	
}
