package org.linchimin.jcuda.utils;

import static jcuda.driver.JCudaDriver.*;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import org.linchimin.jcuda.kernels.CudaKernelLoader;

import jcuda.Pointer;
import jcuda.driver.CUcontext;
import jcuda.driver.CUctx_flags;
import jcuda.driver.CUdevice;
import jcuda.driver.CUfunction;
import jcuda.driver.CUmodule;
import jcuda.driver.CUstream;
import jcuda.driver.JCudaDriver;
import jcuda.runtime.JCuda;


/**
 * set HKEY_LOCAL_MACHINE - SYSTEM - CurrentControlSet - Control - GraphicsDrivers for TdrDelay to a larger value
 * 
 * 
 * @author Lin Chi-Min (v381654729@gmail.com)
 */
public class JCudaManager {
	

	private static boolean LAUNCH_SYNCHRONOUSLY = true;
	
	private static final int MAX_POSSIBLE_BLOCKS = 1024;
	private static final int MAX_BLOCKS = 128;
	
	
	private static CUdevice device;
	private static CUcontext context;
	private static CUstream cuStream;

	
	
	/**
	 * modules; temporarily stored here for shutdown
	 */
	private static ArrayList<CUmodule> cuMmodules;

	
	
	
	static void initialize() {
		if (device != null) {
			return;
		}
		
		/**initialize all used functions in corresponding classes 
		 */
		
		long time = System.currentTimeMillis();
		
		JCuda.setExceptionsEnabled(true);
		JCudaDriver.setExceptionsEnabled(true);
	
		JCudaDriver.cuInit(0);
      
        device = new CUdevice();
        cuDeviceGet(device, 0);
        
        context = new CUcontext();
        cuCtxCreate(context, CUctx_flags.CU_CTX_SCHED_AUTO, device);
        
        cuStream = new CUstream();
        JCudaDriver.cuStreamCreate(cuStream, 0);
        
        
        cuMmodules = CudaKernelLoader.loadCudaModules();
    	
//        cublasHandle hostHandle = createCublasHostHandle();
//        cublasHandle deviceHandle = createCublasDeviceHandle();

		
		long millis = System.currentTimeMillis() - time;
		System.out.println(
				" Time taken to initialize JCuda: " + (millis > 100000 ? (millis / 1000) + " seconds." : millis + " milliseconds."));
		System.out.println("----------------------------------------------------------");
	}
	
	public static void close() {
		for (CUmodule cuMmodule : cuMmodules) {
			cuModuleUnload(cuMmodule);	
		}
		if (context != null) {
			cuCtxDestroy(context);
		}
	}
	
	
	/**
	 * @return like 50
	 */
	@SuppressWarnings("deprecation")
	public static String getSunVersionString(){
		initialize();
		int[] major = new int[1];
		int[] minor = new int[1];
		cuDeviceComputeCapability(major, minor, device);
		String versionString = major[0] + "" +minor[0];
		return versionString;
	}
	
	/**
	 * dynamic parallelism
	 * https://viralfsharp.com/2014/08/17/compiling-cuda-projects-with-dynamic-parallelism-vs-201213/
	 */
	public static String compilePtxFile(String fileName) {
		
		String ptxFileName = fileName + ".ptx";
		try {
			File ptxFile = new File(ptxFileName);
			if (ptxFile.exists()) {
				return ptxFileName;
			}

			long start = System.nanoTime();
			
			File cuFile = new File(fileName);
			String cuFilePath = cuFile.getPath();
			String modelString = "-m" + System.getProperty("sun.arch.data.model");
			String v = getSunVersionString();
			
			String compiler_bindir = "\"C:/Program Files (x86)/Microsoft Visual Studio 12.0/VC/bin\" ";
			String command = "nvcc --compiler-bindir " + compiler_bindir 
					+ "-use_fast_math -arch=sm_" + v + " " + modelString + " -ptx \"" + cuFilePath + "\" -o \""
					+ ptxFileName + "\"";
			
			System.out.println("Executing\n" + command);
			
			Process process = Runtime.getRuntime().exec(command);

			String errorMessage =
					new String(toByteArray(process.getErrorStream()));
			String outputMessage =
					new String(toByteArray(process.getInputStream()));
			int exitValue = 0;
			try
			{
				exitValue = process.waitFor();
			}
			catch (InterruptedException e)
			{
				Thread.currentThread().interrupt();
				throw new IOException(
						"Interrupted while waiting for nvcc output", e);
			}

			if (exitValue != 0)
			{
				System.out.println("nvcc process exitValue "+exitValue);
				System.out.println("errorMessage:\n"+errorMessage);
				System.out.println("outputMessage:\n"+outputMessage);
				throw new IOException(
						"Could not create .ptx file: "+errorMessage);
			}
			System.out.println("Finished creating PTX file");

			long end = System.nanoTime();
			System.out.println("Compile time: "+(end - start) / 1e6 + "ms");
		} catch (IOException e) {
			e.printStackTrace();
		}
		return ptxFileName;
    }
	
	
	private static byte[] toByteArray(InputStream inputStream) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte buffer[] = new byte[8192];
        while (true)
        {
            int read = inputStream.read(buffer);
            if (read == -1)
            {
                break;
            }
            baos.write(buffer, 0, read);
        }
        return baos.toByteArray();
    }

	
	
	
	/**
	 * Block for a context's tasks to complete. Blocks until the device has
	 * completed all preceding requested tasks. cuCtxSynchronize() returns an
	 * error if one of the preceding tasks failed. If the context was created
	 * with the CU_CTX_SCHED_BLOCKING_SYNC flag, the CPU thread will block until
	 * the GPU context has finished its work.
	 */
	public static void synchronize(){
		cuCtxSynchronize();
	}
	
	
	/**
	 * blockDimX : like numRows 
	 * gridDimX : like numCols
	 * 
	 * blockIdx.x : like column index 
	 * threadIdx.x : like row index
	 */
	public static void launchKernelByDefault(CUfunction cuFunction, Pointer kernelParameters, int numElements, boolean synchronous){
		int blockDimX_numRows = Math.min(numElements, MAX_BLOCKS);
		int gridDimX_numCols = (numElements + blockDimX_numRows - 1) / blockDimX_numRows;
		cuLaunchKernel(cuFunction,
				gridDimX_numCols, 1, 1,		// Grid dimension
				blockDimX_numRows, 1, 1,	// Block dimension
				0, null,               		// Shared memory size and stream
				kernelParameters, null 		// Kernel- and extra parameters
				);
		if (synchronous) {
			cuCtxSynchronize();
		}
	}
	
	
	/**
	 * a default way of launching a CUfunction
	 * @param cuFunction
	 * @param kernelParameters
	 * @param numElements : count of elements to be processed with this function
	 */
	public static void launchKernelByDefault(CUfunction cuFunction, Pointer kernelParameters, int numElements){
		int blockDimX_numRows = Math.min(numElements, MAX_BLOCKS);
		int gridDimX_numCols = (numElements + blockDimX_numRows - 1) / blockDimX_numRows;
		
		cuLaunchKernel(cuFunction,
				gridDimX_numCols, 1, 1,      // Grid dimension
				blockDimX_numRows, 1, 1,      // Block dimension
				0, null,               // Shared memory size and stream
				kernelParameters, null // Kernel- and extra parameters
				);
		if (LAUNCH_SYNCHRONOUSLY){
			cuCtxSynchronize();
		}
	}

	public static void launchKernelByDefault(CUfunction cuFunction, Pointer kernelParameters, int numElements, int sharedMemoryNumElements) {
		int blockDimX_numRows = Math.min(numElements, MAX_BLOCKS);
		int gridDimX_numCols = (numElements + blockDimX_numRows - 1) / blockDimX_numRows;
		cuLaunchKernel(cuFunction,
				gridDimX_numCols, 1, 1,      // Grid dimension
				blockDimX_numRows, 1, 1,      // Block dimension
				sharedMemoryNumElements * 4, null,               // Shared memory size and stream
				kernelParameters, null // Kernel- and extra parameters
		);
		if (LAUNCH_SYNCHRONOUSLY){
			cuCtxSynchronize();
		}
	}
	
	
	public static void launchKernelByDefault(CUfunction cuFunction, Pointer kernelParameters, int numElements, int sharedMemoryNumElements, boolean synchronous) {
		int blockDimX_numRows = Math.min(numElements, MAX_BLOCKS);
		int gridDimX_numCols = (numElements + blockDimX_numRows - 1) / blockDimX_numRows;
		cuLaunchKernel(cuFunction,
				gridDimX_numCols, 1, 1,      // Grid dimension
				blockDimX_numRows, 1, 1,      // Block dimension
				sharedMemoryNumElements * 4, null,               // Shared memory size and stream
				kernelParameters, null // Kernel- and extra parameters
		);
		if (synchronous) {
			cuCtxSynchronize();
		}
	}

	
	public static void launchKernel(CUfunction cuFunction, Pointer kernelParameters, 
			int blockDimX_numRows, int gridDimX_numCols){
		launchKernel(cuFunction, kernelParameters, blockDimX_numRows, gridDimX_numCols, 0);	
	}
	
	public static void launchKernel(CUfunction cuFunction, Pointer kernelParameters, 
			int blockDimX_numRows, int gridDimX_numCols, int sharedMemoryNumElements){
		if (blockDimX_numRows > MAX_POSSIBLE_BLOCKS){
			throw new IllegalArgumentException(
					"IllegalArgumentException: the argument blockDimX_numRows " + blockDimX_numRows + "should not be larger than " + MAX_POSSIBLE_BLOCKS + ".");
		}
		cuLaunchKernel(cuFunction,
				gridDimX_numCols, 1, 1,      // Grid dimension
				blockDimX_numRows, 1, 1,      // Block dimension
				sharedMemoryNumElements * 4, null,               // Shared memory size and stream
				kernelParameters, null // Kernel- and extra parameters
				);
		if (LAUNCH_SYNCHRONOUSLY){
			cuCtxSynchronize();
		}
	}

//	protected static cublasHandle createCublasHostHandle(){
//		cublasHandle cublasHandle = new cublasHandle();
//		JCublas2.cublasCreate(cublasHandle);
//		cudaStream_t cudaStream_t = new cudaStream_t();
//        cudaStreamCreate(cudaStream_t);
//        JCublas2.cublasSetStream(cublasHandle, cudaStream_t);
//	
//		JCublas2.cublasSetPointerMode(cublasHandle, cublasPointerMode.CUBLAS_POINTER_MODE_HOST);
//		JCublas2.cublasSetAtomicsMode(cublasHandle, cublasAtomicsMode.CUBLAS_ATOMICS_ALLOWED);
//		
//		return cublasHandle;
//	}
//	
//	protected static cublasHandle createCublasDeviceHandle(){
//		cublasHandle cublasHandle = new cublasHandle();
//		JCublas2.cublasCreate(cublasHandle);
//		cudaStream_t cudaStream_t = new cudaStream_t();
//        cudaStreamCreate(cudaStream_t);
//        JCublas2.cublasSetStream(cublasHandle, cudaStream_t);
//		JCublas2.cublasSetPointerMode(cublasHandle, cublasPointerMode.CUBLAS_POINTER_MODE_DEVICE);
//		JCublas2.cublasSetAtomicsMode(cublasHandle, cublasAtomicsMode.CUBLAS_ATOMICS_ALLOWED);
//		
//		return cublasHandle;
//	}
	
	
}
