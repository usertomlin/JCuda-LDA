package org.linchimin.jcuda.utils;


import org.linchimin.utils.ArgumentChecker;

import jcuda.Pointer;
import jcuda.Sizeof;
import jcuda.driver.CUdeviceptr;
import jcuda.driver.JCudaDriver;

/**
 * @author Lin Chi-Min (v381654729@gmail.com)
 *
 */
public class JCudaMemoryUtils {

	static {
		JCudaManager.initialize();
	}
	
	//////////////////////////////////////////////////////////

	/**
	 * Compared to 
	 * 'device_data = new CUdeviceptr(); 
	 * JCuda.cudaMalloc(device_data, getBytesSize());'
	 * 1. faster
	 * 2. 'jcuda.CudaException: CUDA_ERROR_OUT_OF_MEMORY' will be thrown if this happens
	 */
	protected static CUdeviceptr createNewDevicePointer(int bytesSize) {
		ArgumentChecker.checkLarger(bytesSize, 0);
		
		CUdeviceptr devicePointer = new CUdeviceptr();
		JCudaDriver.cuMemAlloc(devicePointer, bytesSize);
		return devicePointer;
	}
	
	
	protected static void copyDataFromDeviceToHost(CUdeviceptr devicePointer, Pointer hostPointer, int numElementsToCopy){
		JCudaDriver.cuMemcpyDtoH(hostPointer, devicePointer, numElementsToCopy * Sizeof.FLOAT);
		JCudaManager.synchronize();
	}
	
	protected static void copyDataFromDeviceToHost(CUdeviceptr devicePointer, int[] hostArray) {
		copyDataFromDeviceToHost(devicePointer, Pointer.to(hostArray), hostArray.length);
	}

	//////////////////////////////////////////////////////////
	
	protected static void copyDataFromDeviceToHost(CUdeviceptr devicePointer, float[] hostData, int numElementsToCopy) {
		ArgumentChecker.checkNonDecreasingOrder(0, numElementsToCopy, hostData.length);
		if (numElementsToCopy == 0){
			return;
		}
		JCudaDriver.cuMemcpyDtoH(Pointer.to(hostData), devicePointer, numElementsToCopy * Sizeof.FLOAT);
		JCudaManager.synchronize();
	}

	protected static void copyDataFromDeviceToHost(CUdeviceptr devicePointer, byte[] hostData, int numElementsToCopy) {
		ArgumentChecker.checkNonDecreasingOrder(0, numElementsToCopy, hostData.length);
		if (numElementsToCopy == 0){
			return;
		}
		JCudaDriver.cuMemcpyDtoH(Pointer.to(hostData), devicePointer, numElementsToCopy * Sizeof.BYTE);
		JCudaManager.synchronize();
	}

	
	/**
	 * with byte offset: does not affect speed
	 * @param hostPointer
	 *            : pointer to float array
	 */
	protected static void copyDataFromHostToDevice(Pointer hostPointer, int numBytesToCopy,
			CUdeviceptr devicePointer) {
		JCudaDriver.cuMemcpyHtoD(devicePointer, hostPointer, numBytesToCopy);
		JCudaManager.synchronize();
	}
	
	/**
	 * set 'value' at 'devicePointer' from offset to end 
	 */
	protected static void setInts(CUdeviceptr devicePointer, int value, int offset, int count) {
		if (offset == 0){
			JCudaDriver.cuMemsetD32(devicePointer, value, count);	
		} else {
			JCudaDriver.cuMemsetD32(devicePointer.withByteOffset(offset * Sizeof.INT), value, count);	
		}
		JCudaManager.synchronize();
	}
	

}
