package org.linchimin.jcuda.functions;

/**
 * Each extended class contains some CUfunctions.
 * 
 * @author Lin Chi-Min (v381654729@gmail.com)
 *
 */
public abstract class AbstractCudaModule {

	public static Class<?>[] allModules = {
			CudaLDAFunctions.class, 
			
	};
	
	
}
