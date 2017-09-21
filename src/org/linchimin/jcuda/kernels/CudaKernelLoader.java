package org.linchimin.jcuda.kernels;

import static jcuda.driver.JCudaDriver.cuModuleGetFunction;
import static jcuda.driver.JCudaDriver.cuModuleLoad;
import static jcuda.driver.JCudaDriver.cuModuleLoadData;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.ArrayList;

import org.linchimin.jcuda.functions.AbstractCudaModule;
import org.linchimin.jcuda.utils.JCudaManager;
import org.linchimin.utils.FileUtils;

import jcuda.driver.CUfunction;
import jcuda.driver.CUmodule;

/**
 * class for compiling kernels 
 * by loading .ptx files in src
 * 
 * https://www.cyberciti.biz/tips/linux-running-commands-on-a-remote-host.html
 * 
 * @author Tom3_Lin
 * 
 */
public class CudaKernelLoader {

	
	/**
	 * for simplicity, all cu and ptx files are put here
	 */
	private static final String CUDA_KERNELS_DIRECTORY = FileUtils.getJavaFileAbsoluteDirectory(CudaKernelLoader.class);
	
//	private static String CUDA_KERNELS_DIRECTORY;
//	static{
//		try {
//			CUDA_KERNELS_DIRECTORY = CudaKernelLoader.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath();
//			System.out.println("CudaKernelLoader CUDA_KERNELS_DIRECTORY = " + CUDA_KERNELS_DIRECTORY);
////			System.out.println("System.exit(0) at CudaKernelLoader.enclosing_method()");
////			System.exit(0);
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//	}
	
	/**
	 * difference from toByteArray: 
	 * an extra 'baos.write(0);' 
	 * @param inputStream
	 * @return
	 */
	private static byte[] toZeroTerminatedByteArray(InputStream inputStream) {
		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			byte buffer[] = new byte[8192];
			while (true) {
				int read = inputStream.read(buffer);
				if (read == -1) {
					break;
				}
				baos.write(buffer, 0, read);
			}
			baos.write(0);
			return baos.toByteArray();
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
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
	 * http://stackoverflow.com/questions/34259441/jcuda-cumoduleload-cannot-load-file-using-the-path-of-getclass-getresource
	 * @param ptxFileName
	 * @return
	 */
	private static CUmodule loadFromPTXFile(String ptxFileName){
		
//		CUlinkState linkState = new CUlinkState();
//        JITOptions jitOptions = new JITOptions();
//        cuLinkCreate(jitOptions, linkState);
//        
//        String ptxFilePath = FileUtils.toAbsolutePath(ptxFileName);
//        cuLinkAddFile(linkState, CUjitInputType.CU_JIT_INPUT_OBJECT, ptxFilePath, jitOptions);
//        
//        long sz[] = new long[1];
//        Pointer image = new Pointer();
//        cuLinkComplete(linkState, image, sz);
//        cuLinkDestroy(linkState);
        
        //////////////////////////////////////////////////////////
		
		InputStream inputStream = FileUtils.readInputStream(CudaKernelLoader.class, ptxFileName);
		if (inputStream == null) {
			return null;
		}
		byte[] ptxData  = toZeroTerminatedByteArray(inputStream);
		return loadFromPTXData(ptxData);
	}
	
	
	
	private static CUmodule loadFromPTXData(byte[] ptxData){
		if (ptxData == null){
			return null;
		}
		CUmodule module = new CUmodule();
		cuModuleLoadData(module, ptxData);
//		JCudaDriver.cuModuleLoadDataex
//		JCudaDriver.cuModuleLoadDataEx(module, Pointer.to(ptxData), 0, new int[0], Pointer.to(new int[0]));
		return module;
	}
	

	/**
	 * 
	 * @param cuFileName
	 * @return a ptxFilePath
	 */
	private static String compilePtxFileAndSave(String cuFileName) {
		String cuFilePath = CUDA_KERNELS_DIRECTORY + cuFileName;
		String ptxFilePath = cuFilePath + ".ptx";
		try {
			long start = System.currentTimeMillis();
			
			String modelString = "-m" + System.getProperty("sun.arch.data.model");
			String v = JCudaManager.getSunVersionString();
//			v = "30";
			v = "20";
			
			String compiler_bindir = " \"C:/Program Files (x86)/Microsoft Visual Studio 12.0/VC/bin\" ";
//			String command = "nvcc --compiler-bindir " + compiler_bindir 
//					+ "-use_fast_math -arch=sm_" + v + " " + modelString + " -rdc=true -ptx \"" + cuFilePath + "\"  -o \""
//					+ ptxFilePath + "\" -lcudadevrt -lcublas_device";
			
//			String command = "nvcc --compiler-bindir " + compiler_bindir 
//					+ "-use_fast_math -arch=compute_" + v + " " + modelString + " -rdc=true -ptx \"" + cuFilePath + "\"  -o \""
//					+ ptxFilePath + "\" -lcudadevrt";
			
			String command = "nvcc --compiler-bindir " + compiler_bindir 
					+ "-use_fast_math -arch=compute_" + v + " " + modelString + " -ptx \"" + cuFilePath + "\"  -o \""
					+ ptxFilePath + "\" ";
			
//			String command = "nvcc --compiler-bindir " + compiler_bindir 
//					+ "-use_fast_math -gencode arch=compute_32,code=sm_32 " + modelString + " -ptx \"" + cuFilePath + "\"  -o \""
//					+ ptxFilePath + "\" ";
			
			System.out.println("Executing\n" + command);
			
			Process process = Runtime.getRuntime().exec(command);
			String errorMessage = new String(toByteArray(process.getErrorStream()));
			String outputMessage = new String(toByteArray(process.getInputStream()));
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
//			process.getInputStream().close();
			long end = System.currentTimeMillis();
			System.out.println("Compile time: "+(end - start) + "ms");
			return ptxFilePath;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	

	
	public static void setFunctionsToModule(Class<?> moduleFunctionsClass, CUmodule module) {
		try {
			if ( !(moduleFunctionsClass.newInstance() instanceof AbstractCudaModule) ){
				throw new Error("Error in CudaKernelLoader.setFunctionsToModule() : moduleFunctionsClass " + 
						moduleFunctionsClass.getSimpleName() + " should be an AbstractCudaModule.");
			}	
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		
		Field[] fields = moduleFunctionsClass.getDeclaredFields();
		for (Field field : fields) {
			CUfunction function = new CUfunction();
			try {
				cuModuleGetFunction(function, module, field.getName());
				field.setAccessible(true);
				field.set(null, function);
			} catch (Exception e) {
				e.printStackTrace();
				System.err.println("field.getName() = " + field.getName() + ", module = " + moduleFunctionsClass);
				continue;
			}
		}
	}


	/**
	 * avoid -Djava.library.path problems
	 */
	public static ArrayList<CUmodule> loadCudaModules() {
		ArrayList<CUmodule> cudaModules = new ArrayList<CUmodule>();
		
		Class<?>[] allModules = AbstractCudaModule.allModules;
		
		for (Class<?> moduleClass : allModules) {
			cudaModules.add(loadModule(moduleClass, moduleClass.getSimpleName() + ".cu"));	
		}
		
		return cudaModules;
	}

	private static CUmodule loadModule(Class<?> moduleFunctionsClass, String cuFileName) {
		String ptxFileName = cuFileName + ".ptx";
		try {
			CUmodule result = loadFromPTXFile(ptxFileName);
			if (result == null) {
				String ptxFilePath = compilePtxFileAndSave(cuFileName);
				result = new CUmodule();
		        cuModuleLoad(result, ptxFilePath);
			}
			setFunctionsToModule(moduleFunctionsClass, result);
			return result;	
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("CudaKernelLoader.loadModule() problematic cu file: " + cuFileName);
			return null;
		}
		
	}
	
	
	
}
