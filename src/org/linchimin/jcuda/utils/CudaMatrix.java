package org.linchimin.jcuda.utils;


import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Arrays;

import org.ejml_float.simple.SimpleFloatMatrix;
import org.linchimin.jcuda.pointer.ConstantDeviceData;
import org.linchimin.jcuda.pointer.HostFloatsPointer;
import org.linchimin.utils.ArgumentChecker;
import jcuda.Sizeof;
import jcuda.driver.CUdeviceptr;
import jcuda.driver.JCudaDriver;

/**
 * 
 * 
 * @author Lin Chi-Min (v381654729@gmail.com)
 *
 */
public class CudaMatrix {
	
	static {
		JCudaManager.initialize();
	}
	
	public int numRows;
	public int numCols;
	
	private int deviceCapacity;
	public CUdeviceptr devicePointer;
	
	/**
	 * the corresponding host data pointer;
	 * store the float array in column major
	 */
	private HostFloatsPointer hostPointer;
	
	
	/** 
	 * create a pure device side matrix
	 */
	public CudaMatrix(int numRows, int numCols) {
		this(numRows, numCols, numRows * numCols);
	}
	
	
	private CudaMatrix(int numRows, int numCols, int deviceCapacity) {
		this(numRows, numCols, JCudaMemoryUtils.createNewDevicePointer(deviceCapacity * Sizeof.FLOAT), null, deviceCapacity);
	}
	
	/**
	 * @param hostData : column major data
	 */
	public CudaMatrix(int numRows, int numCols, float... hostData) {
		this(numRows, numCols);
		int numElements = getNumElements();
		if (hostData.length > numElements){
			throw new IllegalArgumentException("hostData.length exceeds matrix dimensions");
		} else if (numElements > hostData.length){
			// add zero paddings
			hostData = Arrays.copyOf(hostData, numElements);
		}
		setHostData(hostData);
	    copyDataFromHostToDevice();
	}
	
	
	private CudaMatrix(int numRows, int numCols, CUdeviceptr devicePointer, HostFloatsPointer hostPointer, int deviceCapacity){
		this.numRows = numRows;
		this.numCols = numCols;
		int numElements = getNumElements();
		ArgumentChecker.checkLarger(numElements, 0);
	    this.devicePointer = devicePointer;
	    this.hostPointer = hostPointer;
	    this.deviceCapacity = deviceCapacity;
	    if (deviceCapacity < numElements){
	    	throw new Error("The device capacity is too small.");
	    }
	}
	
	/**
	 * set hostData with length euqal to matrix num elements
	 * @param hostData
	 * @param hostPinned
	 */
	private void setHostData(float[] hostData){
		hostPointer = new HostFloatsPointer(hostData);
	}
	
	
	
	/**
	 * copy data from hostPointer.pointer to device
	 */
	private void copyDataFromHostToDevice() {
		copyDataFromHostToDevice(getNumElements());
	}
	

	/**
	 * copy data from hostPointer.pointer to device
	 */
	private void copyDataFromHostToDevice(int numElementsToCopy){
		if (hostPointer == null){
			throw new Error("CudaMatrix.copyDataFromHostToDevice() the hostPointer is currently null; use 'putHostData' to put host data.");
		}
		JCudaMemoryUtils.copyDataFromHostToDevice(hostPointer.pointer, numElementsToCopy * Sizeof.FLOAT, devicePointer);
	}
	

	
	private void resetHostPointer(){
		int numElements = getNumElements();
		if (hostPointer == null) {
			hostPointer = new HostFloatsPointer(numElements);
		}
	}
	
	public int getNumElements(){
		return numRows * numCols;
	}
	
	public int getDeviceCapacity(){
		return deviceCapacity;
	}
	
	public CudaMatrix setDimensions(int numRows, int numCols){
		if (numRows * numCols > deviceCapacity){
			throw new IllegalArgumentException("The new dimensions (" + numRows + ", " + numCols + ") are larger than device capacity; "
					+ "call ensureDeviceCapacity and than set these dimensions");
		}
		this.numRows = numRows;
		this.numCols = numCols;
		return this;
	}
	

	
	@Override
	public String toString() {
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		CudaMatrixIO.print(new PrintStream(stream), this);
		return stream.toString();
	}
	
	
	/**
	 * @param matrix
	 * @param transpose : the constructed CudaMatrix is the transpose of 'matrix' or not 
	 */
	public CudaMatrix(SimpleFloatMatrix matrix, boolean transpose) {
		this(transpose ? matrix.numCols() : matrix.numRows(), transpose ? matrix.numRows() : matrix.numCols());
		float[] rowMajorData = matrix.getData();
		if (transpose) {
			setHostData(rowMajorData);
		} else {
			float[] columnMajorData = toColumnMajorFloats(rowMajorData, matrix.numRows(), matrix.numCols());
			setHostData(columnMajorData);
		}
		copyDataFromHostToDevice(matrix.getNumElements());
	}
	
	/**
	 * copy this to result
	 */
	private void copyTo(float[] result){
		copyTo(result, result.length);
	}
	
	private void copyTo(float[] result, int numElementsToCopy){
		ArgumentChecker.checkNonDecreasingOrder(0, numElementsToCopy, result.length);
		ArgumentChecker.checkEqualOrSmaller(numElementsToCopy, getNumElements());
		JCudaMemoryUtils.copyDataFromDeviceToHost(devicePointer, result, numElementsToCopy);
	}
	
	
	/**
	 * get data from device;
	 * to get data from host, access 'hostPointer' directly  
	 */
	protected float[] getColumnMajorData(){
		int numElements = getNumElements();
		return getColumnMajorData(numElements);
	}
	
	
	
	/**
	 * @param numTopElements
	 * @return column major floats for the first 'numElements' elements 
	 */
	private float[] getColumnMajorData(int numTopElements){
		
		if (numTopElements == getNumElements()){
			resetHostPointer();
			JCudaMemoryUtils.copyDataFromDeviceToHost(devicePointer, hostPointer.pointer, numTopElements);
			return hostPointer.getData();	
		} else {
			float[] data = new float[numTopElements];
			copyTo(data);
			return data;
		}
		
	}
	
	public SimpleFloatMatrix toSimpleFloatMatrix(){
		int numElements = getNumElements();
		resetHostPointer();
		CudaMatrix tempMatrix = ConstantDeviceData.getTempMatrix(numElements);
		tempMatrix.resetHostPointer();
		
		HostFloatsPointer tempHost = tempMatrix.hostPointer;
		JCudaMemoryUtils.copyDataFromDeviceToHost(devicePointer, tempHost.pointer, numElements);
		float[] colMajorData = tempHost.getData();
		float[] rowMajorData = hostPointer.getData();
		toRowMajorFloats(colMajorData, numRows, numCols, rowMajorData);
		return new SimpleFloatMatrix(numRows, numCols, rowMajorData);

	}
	
	public void free() {
		
		JCudaDriver.cuMemFree(devicePointer);
		devicePointer = null;
		if (hostPointer != null){
			hostPointer.free();
			hostPointer = null;
		}
	}
	
	
	protected static float[] toColumnMajor(float[][] rowMajorData) {
		int numRows = rowMajorData.length; 
		int numCols = rowMajorData[0].length;
		final int numElements = numRows * numCols;
		float[] data = new float[numElements];
		int i = 0;
		for (int r = 0; r < numRows; r++) {
			float[] mr = rowMajorData[r];
			for (int c = 0; c < numCols; c++) {
				data[i] = mr[c];
				i += numRows;
				if (i >= numElements){
					i -= numElements - 1;
				}
			}
		}
		return data;
	}
	
	
	protected static float[][] fromColumnMajor(float[] data, int numRows, int numCols) {		
		float[][] mat = new float[numRows][numCols];
		for (int i = 0; i < numRows; i++) {
			int index = i;
			float[] mr = mat[i];
			for (int j = 0; j < numCols; j++) {
				mr[j] = data[index];
				index += numRows;
			}
		}
		return mat;
	}
	
	protected static float[] toRowMajor(float[][] mat) {
		int numRows = mat.length;
		int numCols = mat[0].length;
		float[] data = new float[numRows * numCols];
		for (int r = 0; r < numRows; r++) {
			System.arraycopy(mat[r], 0, data, r * numCols, numCols);
		}
		return data;
	}
	
	protected static float[][] fromRowMajor(float[] data, int numRows, int numCols) {		
		float[][] mat = new float[numRows][];
		for (int i = 0; i < numRows; i++) {
			mat[i] = Arrays.copyOfRange(data, i * numCols, (i + 1) * numCols);
		}
		return mat;
	}
	

	/**
	 * convert columnMajorFloats to rowMajorFloats
	 * @param rowMajorFloats
	 * @param columnMajorFloats
	 */
	private static void toRowMajorFloats(float[] columnMajorFloats, int numRows, int numCols, float[] rowMajorFloats){
		final int length = numRows * numCols;
		int rowMajorIndex = 0;
		for (int i = 0; i < length; i++) {
			rowMajorFloats[rowMajorIndex] = columnMajorFloats[i];
			rowMajorIndex += numCols;
			if (rowMajorIndex >= length){
				rowMajorIndex = rowMajorIndex - length + 1;
			}
		}
	}

	
	private static float[] toColumnMajorFloats(float[] rowMajorFloats, int numRows, int numCols){
		float[] columnMajorFloats = new float[rowMajorFloats.length];
		toColumnMajorFloats(rowMajorFloats, numRows, numCols, columnMajorFloats);
		return columnMajorFloats;
	}
	
	
	/**
	 * rowMajorData and columnMajorData should be two different arrays
	 */
	private static void toColumnMajorFloats(float[] rowMajorFloats, int numRows, int numCols, float[] columnMajorFloats){
		final int length = numRows * numCols;
		int resultIndex = 0;
		for (int i = 0; i < length; i++) {
			columnMajorFloats[resultIndex] = rowMajorFloats[i];
			resultIndex += numRows;
			if (resultIndex >= length){
				resultIndex = resultIndex - length + 1;
			}
		}
	}
	
	
	
}
