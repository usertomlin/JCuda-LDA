package org.linchimin.jcuda.utils;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Arrays;

import org.linchimin.jcuda.pointer.HostIntsPointer;

import jcuda.Pointer;
import jcuda.Sizeof;
import jcuda.driver.CUdeviceptr;
import jcuda.driver.JCudaDriver;


/**
 * data structure for a device matrix of integers 
 * 
 * @author Lin Chi-Min (v381654729@gmail.com)
 */
public class CudaIntsMatrix  {

	
	public int numRows;
	public int numCols;
	
	private int deviceCapacity;
	public CUdeviceptr devicePointer;
	
	
	/**
	 * the corresponding host data pointer;
	 * the int array is stored in column major
	 */
	private HostIntsPointer hostPointer;
	
	
	private CudaIntsMatrix(int numRows, int numCols, int deviceCapacity) {
		this(numRows, numCols, JCudaMemoryUtils.createNewDevicePointer(deviceCapacity * Sizeof.INT), null, deviceCapacity);
	}
	
	
	public CudaIntsMatrix(int numRows, int numCols, int... hostData) {
		this (numRows, numCols, numRows * numCols);
		int n = getNumElements();
		if (hostData.length > n){
			throw new IllegalArgumentException("hostData.length exceeds matrix dimensions");
		} else if (n > hostData.length){
			hostData = Arrays.copyOf(hostData, n);
		}
		hostPointer = new HostIntsPointer(hostData);
	    copyDataFromHostToDevice();
	}
	
	private CudaIntsMatrix(int numRows, int numCols, CUdeviceptr devicePointer, HostIntsPointer hostPointer, int deviceCapacity){
		if (numRows <= 0 || numCols <= 0){
			throw new IllegalArgumentException("IllegalArgumentException : the dimensions should be positive, "
					+ "but numRows = " + numRows + ", numCols = " + numCols);
		}
		this.numRows = numRows;
		this.numCols = numCols;
		int numElements = getNumElements();
	    this.devicePointer = devicePointer;
	    this.hostPointer = hostPointer;
	    this.deviceCapacity = deviceCapacity;
	    if (deviceCapacity < numElements){
	    	throw new Error("The device capacity is too small.");
	    }
	}
	
	
	/**
	 * @param hostData : column major data;
	 * to copy transposed data, call MatrixMath.transpose to get a transpose first
	 */
	public void copyFrom(int... hostData){
		copyFrom(hostData, hostData.length);
	}
	
	
	private void copyFrom(int[] hostData, int numElementsToCopy){
		JCudaMemoryUtils.copyDataFromHostToDevice(Pointer.to(hostData), numElementsToCopy * Sizeof.INT, devicePointer);
	}
	
	private void copyDataFromHostToDevice() {
		if (hostPointer == null){
			throw new Error("CudaIntsMatrix.copyDataFromHostToDevice() the hostPointer is currently null; use 'putHostData' to put host data.");
		}
		JCudaMemoryUtils.copyDataFromHostToDevice(hostPointer.pointer, getBytesSize(), devicePointer);
	}

	private void resetHostPointer(){
		int numElements = getNumElements();
		if (hostPointer == null) {
			hostPointer = new HostIntsPointer(numElements);
		}
	}
	
	public int getNumElements(){
		return numRows * numCols;
	}
	
	
	/**the maximum bytes size for CudaIntsMatrix is around 2G
	 */
	private int getBytesSize(){
		return numRows * numCols * Sizeof.INT;
	}

	public CudaIntsMatrix setDimensions(int numRows, int numCols) {
		if (numRows * numCols > deviceCapacity){
			throw new IllegalArgumentException("The new dimensions (" + numRows + ", " + numCols + ") are larger than device capacity " + deviceCapacity +"; "
					+ "call ensureDeviceCapacity and than set these dimensions");
		}
		this.numRows = numRows;
		this.numCols = numCols;
		return this;
	}

	public int getDeviceCapacity(){
		return deviceCapacity;
	}
	

	@Override
	public String toString() {
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		CudaMatrixIO.print(new PrintStream(stream), this);
		return stream.toString();
	}
	
	/**
	 * copy this to result
	 */
	private void copyTo(int[] result){
		JCudaMemoryUtils.copyDataFromDeviceToHost(devicePointer, result);
	}
	
	/**
	 * set a single value to the matrix
	 * @param alpha
	 */
	public void setElements(int value) {
		JCudaMemoryUtils.setInts(devicePointer, value, 0, getNumElements());
	}
	
	
	
	//////////////////////////////////////////////////////////
	
	protected int[] getColumnMajorData(){
		int numElements = getNumElements();
		return getColumnMajorData(numElements);
	}
	
	private int[] getColumnMajorData(int numTopElements){
		if (numTopElements == getNumElements()){
			resetHostPointer();
			JCudaMemoryUtils.copyDataFromDeviceToHost(devicePointer, hostPointer.pointer, numTopElements);
			return hostPointer.getData();	
		} else {
			int[] data = new int[numTopElements];
			copyTo(data);
			return data;
		}
	}
	
	public void free() {
		
		JCudaDriver.cuMemFree(devicePointer);
		if (hostPointer != null){
			hostPointer.free();
			hostPointer = null;
		}
	}

	
	protected static int[] toColumnMajor(int[][] mat) {
		int numRows = mat.length; 
		int numCols = mat[0].length;
		final int numElements = numRows * numCols;
		int[] data = new int[numElements];
		int i = 0;
		for (int r = 0; r < numRows; r++) {
			int[] mr = mat[r];
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
	
	protected static int[][] fromColumnMajor(int[] data, int numRows, int numCols) {		
		int[][] mat = new int[numRows][numCols];
		for (int i = 0; i < numRows; i++) {
			int index = i;
			int[] mr = mat[i];
			for (int j = 0; j < numCols; j++) {
				mr[j] = data[index];
				index += numRows;
			}
		}
		return mat;
	}
	
	protected static int[][] fromRowMajor(int[] data, int numRows, int numCols) {		
		int[][] mat = new int[numRows][];
		for (int i = 0; i < numRows; i++) {
			mat[i] = Arrays.copyOfRange(data, i * numCols, (i + 1) * numCols);
		}
		return mat;
	}

}
