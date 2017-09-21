package org.linchimin.jcuda.pointer;

import java.util.List;

import org.linchimin.jcuda.utils.CudaIntsMatrix;
import org.linchimin.jcuda.utils.CudaMatrix;

import jcuda.NativePointerObject;
import jcuda.Pointer;
import jcuda.Sizeof;
import jcuda.driver.CUdeviceptr;
import jcuda.driver.JCudaDriver;

/**
 * @author Lin Chi-Min (v381654729@gmail.com)
 *
 */
public class PointerUtils {

	/**
	 * @param parameters
	 *            Pointer kernelParameters =
	 *            Pointer.to(Pointer.to(A.devicePointer),
	 *            Pointer.to(B.devicePointer), Pointer.to(C.devicePointer),
	 *            ptr(n));
	 */
	public static Pointer kernelParams(Object... parameters) {
		NativePointerObject[] pointers = new NativePointerObject[parameters.length];
		for (int i = 0; i < parameters.length; i++) {
			String className = parameters[i].getClass().getSimpleName();
			switch (className) {
			case "CUdeviceptr":
			case "Pointer":
				Pointer p0 = (Pointer) parameters[i];
				pointers[i] = Pointer.to(p0);
				break;
			case "byte[]":
				byte[] p1 = (byte[]) parameters[i];
				pointers[i] = Pointer.to(p1);
				break;
			case "int[]":
				int[] p2 = (int[]) parameters[i];
				pointers[i] = Pointer.to(p2);
				break;
			case "HeapFloatBuffer":
			case "DirectFloatBufferU":
				java.nio.Buffer p3 = (java.nio.Buffer) parameters[i];
				pointers[i] = Pointer.to(p3);
				break;
			case "double[]":
				double[] p4 = (double[]) parameters[i];
				pointers[i] = Pointer.to(p4);
				break;
			case "float[]":
				float[] p5 = (float[]) parameters[i];
				pointers[i] = Pointer.to(p5);
				break;
			case "long[]":
				long[] p6 = (long[]) parameters[i];
				pointers[i] = Pointer.to(p6);
				break;
			case "char[]":
				char[] p7 = (char[]) parameters[i];
				pointers[i] = Pointer.to(p7);
				break;
			case "short[]":
				short[] p8 = (short[]) parameters[i];
				pointers[i] = Pointer.to(p8);
				break;
			case "Float":
				float p9 = (float) parameters[i];
				pointers[i] = to(p9);
				break;
			case "Integer":
				int p10 = (int) parameters[i];
				pointers[i] = to(p10);
				break;
			case "Long":
				long p11 = (long) parameters[i];
				pointers[i] = to(p11);
				break;
			}
		}
		return Pointer.to(pointers);
	}

	public static Pointer to(float value) {
		return Pointer.to(new float[] {value});
	}
	
	public static Pointer to(double value) {
		return Pointer.to(new float[] {(float) value});
	}
	
	public static Pointer to(long value) {
		return Pointer.to(new long[] {value});
	}

	public static Pointer to(int value) {
		return Pointer.to(new int[] {value});
	}
	
	public static Pointer to(float[] values) {
		return Pointer.to(values);
	}
	
	public static Pointer to(int[] values) {
		return Pointer.to(values);
	}
	
	public static Pointer to(CudaMatrix matrix) {
		return Pointer.to(matrix.devicePointer);
	}
	
	public static Pointer to(CUdeviceptr devicePointer) {
		return Pointer.to(devicePointer);
	}
	
	public static Pointer to(CudaIntsMatrix matrix) {
		return Pointer.to(matrix.devicePointer);
	}

	
	/**
	 * for one-time use
	 * @return pointerToArray
	 */
	public static CUdeviceptr to(List<CudaMatrix> matrixList) {
		CUdeviceptr[] Apointers = new CUdeviceptr[matrixList.size()];
		for (int i = 0; i < matrixList.size(); i++) {
			Apointers[i] = matrixList.get(i).devicePointer;
		}
		CUdeviceptr pointerToArray = new CUdeviceptr();
		JCudaDriver.cuMemAlloc(pointerToArray, matrixList.size() * Sizeof.POINTER);
		JCudaDriver.cuMemcpyHtoD(pointerToArray, Pointer.to(Apointers), matrixList.size() * Sizeof.POINTER);
		return pointerToArray;
	}
	
	
	/**
	 * for one-time use
	 * @return pointerToArray
	 */
	public static CUdeviceptr to(CudaMatrix[] matrixList) {
		CUdeviceptr pointerToArray = new CUdeviceptr();
		int length = matrixList.length;
		JCudaDriver.cuMemAlloc(pointerToArray, length * Sizeof.POINTER);
		
		updatePointerToArray(pointerToArray, matrixList);
		
		return pointerToArray;
	}
	
	/**
	 * use this to avoid repeated cuMemAlloc for the pointerToArray 
	 * @param pointerToArray
	 */
	public static void updatePointerToArray(CUdeviceptr pointerToArray, CudaMatrix[] matrixList){
		final int length = matrixList.length;
		CUdeviceptr[] Apointers = new CUdeviceptr[length];
		for (int i = 0; i < length; i++) {
			Apointers[i] = matrixList[i].devicePointer;
		}
		JCudaDriver.cuMemcpyHtoD(pointerToArray, Pointer.to(Apointers), length * Sizeof.POINTER);		
	}
	
	/**
	 * @param matrixArray
	 * @return an array of CUdeviceptrs of each element matrix
	 */
	public static CUdeviceptr[] getDevicePointers(CudaMatrix[] matrixArray) {
		if (matrixArray == null){
			return null;
		}
		final int batchCount = matrixArray.length;
		CUdeviceptr[] devicePointers = new CUdeviceptr[batchCount];
		for (int i = 0; i < batchCount; i++) {
			devicePointers[i] = matrixArray[i].devicePointer;
		}
		return devicePointers;
	}
	
	
	
	/**
	 * @param matricesPointers
	 * @return a device pointer to array 
	 */
	public static CUdeviceptr to(Pointer[] matricesPointers) {
		int batchCount = matricesPointers.length;
		CUdeviceptr pointerToArray = new CUdeviceptr();
		JCudaDriver.cuMemAlloc(pointerToArray, batchCount * Sizeof.POINTER);
		JCudaDriver.cuMemcpyHtoD(pointerToArray, Pointer.to(matricesPointers), batchCount * Sizeof.POINTER);
		return pointerToArray;
	}

	
	
}
