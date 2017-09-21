package org.linchimin.jcuda.utils;

import java.io.PrintStream;

import jcuda.driver.CUdeviceptr;


/**
 * @author Lin Chi-Min
 *
 */
public class CudaMatrixIO {

	private static final int MAX_ELEMENTS_TO_PRINT = 1200000;
	
	public static void print(PrintStream out, CudaMatrix mat){
		print(out, mat, 6, 3);
	}
	
	public static void printCorner(PrintStream out, CudaMatrix mat, int numTopRows, int numTopCols){
		printCorner(out, mat, numTopRows, numTopCols, 6, 3);
	}
	
//	public static void printWithComma(PrintStream out, CudaMatrix mat){
//		String format = "%" + 6 + "." + 2 + "f ";
//		out.println("Type = dense real , numRows = " + mat.numRows + " , numCols = " + mat.numCols);
//		format += " ";
//		int n = mat.getNumElements();
//		if (n > MAX_ELEMENTS_TO_PRINT)
//			return;
//		
//		int numRows = mat.numRows, numCols = mat.numCols;
//		float[] columnMajorData = mat.getColumnMajorFloats();
//		for (int rowIndex = 0; rowIndex < numRows; rowIndex++) {
//			for (int columnIndex = 0; columnIndex < numCols; columnIndex++) {
//				out.printf(format, columnMajorData[columnIndex * numRows + rowIndex]);
//				out.print(", ");
//			}
//			out.println();
//		}
//	}
//	
//	public static void printIntsWithComma(PrintStream out, CudaIntsMatrix mat){
//		String format = "%" + 5 + "d";
//		out.println("Type = dense real , numRows = " + mat.numRows + " , numCols = " + mat.numCols);
//		format += " ";
//		int n = mat.getNumElements();
//		if (n > MAX_ELEMENTS_TO_PRINT)
//			return;
//		
//		int numRows = mat.numRows, numCols = mat.numCols;
//		int[] columnMajorData = mat.getColumnMajorInts();
//		for (int rowIndex = 0; rowIndex < numRows; rowIndex++) {
//			for (int columnIndex = 0; columnIndex < numCols; columnIndex++) {
//				out.printf(format, columnMajorData[columnIndex * numRows + rowIndex]);
//				out.print(", ");
//			}
//			out.println();
//		}
//	}
	
	
	public static void print(PrintStream out, CudaIntsMatrix mat) {
		printInts(out, mat, 3);
	}	
	
	
	public static void print(PrintStream out, CudaMatrix mat, int numChar, int precision) {
		String format = "%" + numChar + "." + precision + "f ";
		print(out, mat, format);
	}
	
	
	public static void printCorner(PrintStream out, CudaMatrix mat,  int numTopRows, int numTopCols, int numChar, int precision) {
		String format = "%" + numChar + "." + precision + "f ";
		printCorner(out, mat, numTopRows, numTopCols, format);
	}
	
	
	public static void printInts(PrintStream out, CudaIntsMatrix mat, int numChar) {
		String format = "%" + numChar + "d";
		printInts(out, mat, format);
	}
	
	
	public static void print(PrintStream out, CudaMatrix mat, String format) {
		out.println("Type = dense , numRows = " + mat.numRows + " , numCols = " + mat.numCols);
		format += " ";
		int n = mat.getNumElements();
		if (n > MAX_ELEMENTS_TO_PRINT)
			return;
		
		int numRows = mat.numRows, numCols = mat.numCols;
		float[] columnMajorData = mat.getColumnMajorData();
		for (int rowIndex = 0; rowIndex < numRows; rowIndex++) {
			for (int columnIndex = 0; columnIndex < numCols; columnIndex++) {
				out.printf(format, columnMajorData[columnIndex * numRows + rowIndex]); 
			}
			out.println();
		}
	}
	
	public static void printCorner(PrintStream out, CudaMatrix mat, int numTopRows, int numTopCols, String format) {
		out.println("Type = dense , numRows = " + mat.numRows + " , numCols = " + mat.numCols);
		format += " ";
		
		float[] columnMajorData = mat.getColumnMajorData();
		for (int rowIndex = 0; rowIndex < numTopRows; rowIndex++) {
			for (int columnIndex = 0; columnIndex < numTopCols; columnIndex++) {
				out.printf(format, columnMajorData[columnIndex * mat.numRows + rowIndex]); 
			}
			out.println();
		}
	}
	
	
	
	
	
	public static void printInts(PrintStream out, CudaIntsMatrix mat, String format) {
		out.println("Type = dense real , numRows = " + mat.numRows + " , numCols = " + mat.numCols);
		format += " ";
//		int n = mat.getNumElements();
//		if (n > MAX_ELEMENTS_TO_PRINT)
//			return;
		
		int numRows = mat.numRows, numCols = mat.numCols;
		int[] columnMajorData = mat.getColumnMajorData();
		for (int rowIndex = 0; rowIndex < numRows; rowIndex++) {
			for (int columnIndex = 0; columnIndex < numCols; columnIndex++) {
				out.printf(format, columnMajorData[columnIndex * numRows + rowIndex]); 
			}
			out.println();
			out.flush();
		}
	}

	/**
	 * print 'devicePointer' as a vectors
	 */
	public static void printBytes(PrintStream out, CUdeviceptr devicePointer, int numElements) {
		if (numElements == 0){
			out.print("[]");
			return;
		}
		byte[] hostArray = new byte[numElements];
		JCudaMemoryUtils.copyDataFromDeviceToHost(devicePointer, hostArray, numElements);
		StringBuilder builder = new StringBuilder(numElements * 5);
		builder.append('[');
		for (int i = 0; i < numElements; ++i) {
			builder.append(hostArray[i]);
			builder.append(", ");
		}
		builder.setCharAt(builder.length() - 2, ']');
		out.print(builder);
	} 

	
}
