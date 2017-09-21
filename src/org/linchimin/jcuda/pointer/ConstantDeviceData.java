package org.linchimin.jcuda.pointer;

import org.linchimin.jcuda.utils.CudaIntsMatrix;
import org.linchimin.jcuda.utils.CudaMatrix;

import jcuda.driver.JCudaDriver;

/**
 * 
 * a class of some matrices: largeOnesMatrix and largeTempMatrix
 * 1. largeOnesMatrix : a large matrix with values 1f
 * 2. largeTempMatrix : a large matrix for temporary use
 * 
 * 
 * @author Lin Chi-Min (v381654729@gmail.com)
 *
 */
public class ConstantDeviceData {
	
	
	private static final int LARGE_CAPACITY = 15000;

	
	
	/**for readability; with value always 1
	 */
	public static final CudaMatrix ONE_FLOAT = new CudaMatrix(1, 1, new float[]{1f});
	public static final CudaMatrix ZERO_FLOAT = new CudaMatrix(1, 1, new float[]{0});
	public static final CudaIntsMatrix ONE_INT = new CudaIntsMatrix(1, 1, new int[]{1});
	public static final CudaIntsMatrix ZERO_INT = new CudaIntsMatrix(1, 1, new int[]{0});
	
	
	public static final CudaMatrix TEMP_VALUE_1 = new CudaMatrix(1, 1, new float[]{0});
	public static final CudaMatrix TEMP_VALUE_2 = new CudaMatrix(1, 2, new float[]{0, 0});
	
	public static final CudaIntsMatrix TEMP_INT_VALUE = new CudaIntsMatrix(1, 1, new int[]{0});
	
	/**
	 * for use only in the device side 
	 */
	private static CudaMatrix ONES_MATRIX;
	
	private static CudaMatrix ZEROS_MATRIX;
	
	
	/**
	 * temporary matrix; it's used to reduce 
	 * repeated allocating and freeing device memory 
	 */
	private static CudaMatrix TEMP_MATRIX;
	
	
	private static CudaIntsMatrix TEMP_INTS_MATRIX;
	
	
	
	/**
	 * return a matrix in which all values are 1 
	 */
	public static CudaMatrix getOnes(int minCapacity){
		return ONES_MATRIX = ensureLargeOnesMatrixCapacity(ONES_MATRIX, minCapacity);
	}
	
	/**
	 *  return a numRows * numCols matrix in which all values are 1
	 */
	public static CudaMatrix getOnes(int numRows, int numCols){
		CudaMatrix ones = getOnes(numRows * numCols);
		ones.setDimensions(numRows, numCols);
		return ones;
	}
	
	
	public static CudaMatrix getZeros(int minCapacity){
		return ZEROS_MATRIX = ensureLargeZerosMatrixCapacity(ZEROS_MATRIX, minCapacity);
	}
	
	
	public static CudaMatrix getTempMatrix(int minCapacity){
		return TEMP_MATRIX = ensureLargeTempMatrixCapacity(TEMP_MATRIX, minCapacity);
	}
	
	
	public static CudaMatrix getTempMatrix(int numRows, int numCols){
		CudaMatrix tempMatrix = getTempMatrix(numRows * numCols);
		tempMatrix.setDimensions(numRows, numCols);
		return tempMatrix;
	}
	
	
	
	
	public static CudaIntsMatrix getTempIntsMatrix(int minCapacity){
		return TEMP_INTS_MATRIX = ensureLargeTempIntsMatrixCapacity(TEMP_INTS_MATRIX, minCapacity);
	}
	
	
	
	
	private static CudaMatrix ensureLargeTempMatrixCapacity(CudaMatrix tempMatrix, int minCapacity) {
		minCapacity = Math.max(minCapacity, LARGE_CAPACITY);
		if (tempMatrix == null) {
			tempMatrix = new CudaMatrix(1, minCapacity, new float[minCapacity]);
			return tempMatrix;
		}
		
		if (tempMatrix.getDeviceCapacity() >= minCapacity) {
			if (tempMatrix.getNumElements() < minCapacity){
				tempMatrix.setDimensions(1, minCapacity);
			}
			return tempMatrix;
		}
		
		tempMatrix.free();
		tempMatrix = new CudaMatrix(1, minCapacity, new float[minCapacity]);
		return tempMatrix;
	}
	
	
	private static CudaIntsMatrix ensureLargeTempIntsMatrixCapacity(CudaIntsMatrix tempMatrix, int minCapacity){
		minCapacity = Math.max(minCapacity, LARGE_CAPACITY);
		if (tempMatrix == null) {
			tempMatrix = new CudaIntsMatrix(1, minCapacity, new int[minCapacity]);
			return tempMatrix;
		}
		
		if (tempMatrix.getDeviceCapacity() >= minCapacity){
//			tempMatrix.setDimensions(1, minCapacity);
			if (tempMatrix.getNumElements() < minCapacity){
				tempMatrix.setDimensions(1, minCapacity);
			}
			return tempMatrix;
		}
		
		tempMatrix.free();
		tempMatrix = new CudaIntsMatrix(1, minCapacity, new int[minCapacity]);
		return tempMatrix;
	}
	
	
	private static CudaMatrix ensureLargeOnesMatrixCapacity(CudaMatrix onesMatrix, int minCapacity) {
		minCapacity = Math.max(minCapacity, LARGE_CAPACITY);
		if (onesMatrix == null){
			onesMatrix = new CudaMatrix(minCapacity, 1);
			JCudaDriver.cuMemsetD32(onesMatrix.devicePointer, Float.floatToIntBits(1f), minCapacity);
			return onesMatrix;
		}
		
		if (onesMatrix.getDeviceCapacity() >= minCapacity){
			onesMatrix.setDimensions(1, minCapacity);
			return onesMatrix;
		}
		
		onesMatrix.free();
		onesMatrix = new CudaMatrix(minCapacity, 1);
		JCudaDriver.cuMemsetD32(onesMatrix.devicePointer, Float.floatToIntBits(1f), minCapacity);
		return onesMatrix;
	}
	
	private static CudaMatrix ensureLargeZerosMatrixCapacity(CudaMatrix onesMatrix, int minCapacity) {
		minCapacity = Math.max(minCapacity, LARGE_CAPACITY);
		if (onesMatrix == null){
			onesMatrix = new CudaMatrix(minCapacity, 1);
			JCudaDriver.cuMemsetD32(onesMatrix.devicePointer, 0, minCapacity);
			return onesMatrix;
		}
		
		if (onesMatrix.getDeviceCapacity() >= minCapacity){
			onesMatrix.setDimensions(1, minCapacity);
			return onesMatrix;
		}
		
		onesMatrix.free();
		onesMatrix = new CudaMatrix(minCapacity, 1);
		JCudaDriver.cuMemsetD32(onesMatrix.devicePointer, 0, minCapacity);
		return onesMatrix;
	}
	
	
	
	
}
