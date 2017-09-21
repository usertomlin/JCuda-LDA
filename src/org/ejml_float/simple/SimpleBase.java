/*
 * Copyright (c) 2009-2014, Peter Abeles. All Rights Reserved.
 *
 * This file is part of Efficient Java Matrix Library (EJML).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.ejml_float.simple;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.Serializable;
import java.util.Arrays;

import org.ejml_float.data.DenseMatrix32F;
import org.ejml_float.data.RealMatrix32F;
import org.ejml_float.ops.MatrixDimensionException;
import org.ejml_float.ops.MatrixIO;




/**
 * Parent of {@link SimpleFloatMatrix} implements all the standard matrix operations and uses
 * generics to allow the returned matrix type to be changed.  This class should be extended
 * instead of SimpleMatrix.
 *
 * @author Peter Abeles
 */
public abstract class SimpleBase <T extends SimpleBase<T>> implements Serializable {

    private static final long serialVersionUID = 6745351051460019059L;
    
	/**
     * Internal matrix which this is a wrapper around.
     */
    protected DenseMatrix32F mat;

    public SimpleBase( int numRows , int numCols ) {
        mat = new DenseMatrix32F(numRows, numCols);
    }

    protected SimpleBase() {
    }

    /**
     * Used internally for creating new instances of SimpleMatrix.  If SimpleMatrix is extended
     * by another class this function should be overridden so that the returned matrices are
     * of the correct type.
     *
     * @param numRows number of rows in the new matrix.
     * @param numCols number of columns in the new matrix.
     * @return A new matrix.
     */
    protected abstract T createMatrix( int numRows , int numCols );

    

	
	/**
     * <p>
     * Returns a reference to the matrix that it uses internally.  This is useful
     * when an operation is needed that is not provided by this class.
     * </p>
     *
     * @return Reference to the internal DenseMatrix32F.
     */
    protected DenseMatrix32F getMatrix() {
        return mat;
    }
    
    public float[] getData(){
    	return mat.data;
    }
    
    /**
     * for each column[i] of this, divide the elements of column[i] by row[i]
     * @param row
     */
    public void divideRowEquals(SimpleFloatMatrix row) {
    	final int numCols = numCols();
		if (row.getNumElements() != numCols){
			throw new IllegalArgumentException("The size of row, or row.getNumElements() = " + row.getNumElements() + 
					", does not match numCols() of this, " + numCols());
		}
		
		float[] data = mat.data;
		final int numRows = numRows();
		
    	for (int i = 0; i < numCols; ++i) {
    		float valueToDivide = row.get(i);
    		int index = i;
    		for (int j = 0; j < numRows; ++j) {
    			data[index] /= valueToDivide;
    			index += numCols;
			}
		}
	}
    
    
    /**
     * <p>
     * Returns the inverse of this matrix.<br>
     * <br>
     * b = a<sup>-1<sup><br>
     * </p>
     *
     * <p>
     * If the matrix could not be inverted then SingularMatrixException is thrown.  Even
     * if no exception is thrown the matrix could still be singular or nearly singular.
     * </p>
     *
     * @see CommonOps#invert(DenseMatrix32F, DenseMatrix32F)
     *
     * @throws org.ejml_float.factory.SingularMatrixException
     *
     * @return The inverse of this matrix.
     */
//    private T invert() {
//        T ret = createMatrix(mat.numRows,mat.numCols);
//        if( !CommonOps.invert(mat,ret.getMatrix()) ) {
//            throw new SingularMatrixException();
//        }
//        if( MatrixFeatures.hasUncountable(ret.getMatrix()))
//            throw new SingularMatrixException("Solution has uncountable numbers");
//        return ret;
//    }

    
    
    /**
     * <p>
     * The condition p = 2 number of a matrix is used to measure the sensitivity of the linear
     * system <b>Ax=b</b>.  A value near one indicates that it is a well conditioned matrix.
     * </p>
     *
     * @see NormOps#conditionP2(DenseMatrix32F)
     *
     * @return The condition number.
     */
//    private double conditionP2() {
//		return 0;
//        return NormOps.conditionP2(mat);
//    }

    /**
     * Assigns an element a value based on its index in the internal array..
     *
     * @param index The matrix element that is being assigned a value.
     * @param value The element's new value.
     */
    public void set( int index , float value ) {
        mat.set(index,value);
    }

    /**
     * Returns the value of the matrix at the specified index of the 1D row major array.
     *
     * @see org.ejml_float.data.DenseMatrix32F#get(int)
     *
     * @param index The element's index whose value is to be returned
     * @return The value of the specified element.
     */
    public float get(int index) {
        return mat.data[ index ];
    }

    
    /**
     * @param rowIndex
     * @param result
     */
    private void copyRowAtTo(int rowIndex, T result) {
    	int numCols = numCols();
    	if (result.numRows() != 1 || result.numCols() != numCols){
    		throw new MatrixDimensionException("The result matrix does not have the desired dimensions");
    	}
    	int offset = rowIndex * numCols;
		System.arraycopy(mat.getData(), offset, result.getData(), 0, numCols);
	}

    
	public T getRow(int rowIndex) {
		T ret = createMatrix(1, numCols());
		copyRowAtTo(rowIndex, ret);
		return ret;
	}
	
	
	
	
	
	
	
    /**
     * Creates a new iterator for traversing through a submatrix inside this matrix.  It can be traversed
     * by row or by column.  Range of elements is inclusive, e.g. minRow = 0 and maxRow = 1 will include rows
     * 0 and 1.  The iteration starts at (minRow,minCol) and ends at (maxRow,maxCol)
     *
     * @param rowMajor true means it will traverse through the submatrix by row first, false by columns.
     * @param minRow first row it will start at.
     * @param minCol first column it will start at.
     * @param maxRow last row it will stop at.
     * @param maxCol last column it will stop at.
     * @return A new MatrixIterator
     */
//    private MatrixIterator64F iterator(boolean rowMajor, int minRow, int minCol, int maxRow, int maxCol)
//    {
//        return new MatrixIterator64F(mat,rowMajor, minRow, minCol, maxRow, maxCol);
//    }

    /**
     * Creates and returns a matrix which is idential to this one.
     *
     * @return A new identical matrix.
     */
    public T copy() {
        T ret = createMatrix(mat.numRows,mat.numCols);
        ret.getMatrix().set(this.getMatrix());
        return ret;
    }

    /**
     * Returns the number of rows in this matrix.
     *
     * @return number of rows.
     */
    public int numRows() {
        return mat.numRows;
    }

    /**
     * Returns the number of columns in this matrix.
     *
     * @return number of columns.
     */
    public int numCols() {
        return mat.numCols;
    }

    /**
     * Returns the number of elements in this matrix, which is equal to
     * the number of rows times the number of columns.
     *
     * @return The number of elements in the matrix.
     */
    public int getNumElements() {
        return mat.getNumElements();
    }

    public T sumRows(){
    	T ret = createMatrix(1, numCols());
    	sumRows(getMatrix(), ret.getMatrix());
    	return ret;
    }
    
    private static DenseMatrix32F sumRows( DenseMatrix32F input , DenseMatrix32F output ) {
        if( output == null ) {
            output = new DenseMatrix32F(1,input.numCols);
        } else if( output.getNumElements() < input.numCols )
            throw new MatrixDimensionException("Output does not have enough elements to store the results");

        for( int cols = 0; cols < input.numCols; cols++ ) {
            float total = 0;

            int index = cols;
            int end = index + input.numCols*input.numRows;
            for( ; index < end; index += input.numCols ) {
                total += input.data[index];
            }

            output.set(cols, total);
        }
        return output;
    }

    
    /**
     * <p>
     * Converts the array into a string format for display purposes.
     * The conversion is done using {@link MatrixIO#print(java.io.PrintStream, org.ejml_float.data.RealMatrix64F)}.
     * </p>
     *
     * @return String representation of the matrix.
     */
    @Override
    public String toString() {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        MatrixIO.print(new PrintStream(stream),mat);
        return stream.toString();
    }


    /**
	 * @return element indices of the sorted array
	 * the data are not modified;
	 * 
	 */
	public int[] argSort(boolean descending ){
		return argSortInternal(getData(), descending);
	}
	
	
	/**
	 * return corresponding indices of the sorted matrix
	 */
	private static int[] argSortInternal(float[] array, boolean descending) {
		int length = array.length;
		long[] vis = new long[length];
		for (int i = 0; i < length; i++) {
			long intBits = Float.floatToIntBits(array[i]);
			if (intBits < 0){
				intBits = -2147483648 - intBits;
			}
			vis[i] = (intBits << 32) + i; 
		}
		
		Arrays.sort(vis);
		if (descending){
			reverse(vis, 0, length);
		}
		int[] result = new int[length];
		for (int i = 0; i < length; i++) {
			result[i] = (int) (vis[i] & 0xffffffff);
		}
		return result;
	}
	
    private static void reverse(final long[] array, final int startIndexInclusive, final int endIndexExclusive) {
        if (array == null) {
            return;
        }
        int i = startIndexInclusive < 0 ? 0 : startIndexInclusive;
        int j = Math.min(array.length, endIndexExclusive) - 1;
        long tmp;
        while (j > i) {
            tmp = array[j];
            array[j] = array[i];
            array[i] = tmp;
            j--;
            i++;
        }
    }

	
	

    
    


    
    
    
    
    /**
     * apply this function on all individual elements of this matrix
     * @param function
     */
//    private void apply(Function<Float, Float> function){
//    	float[] data = getData();
//    	for (int i = 0; i < data.length; i++) {
//			data[i] = function.apply(data[i]);
//		}
//    }
    
    /**
     * <p>
     * Saves this matrix to a file as a serialized binary object.
     * </p>
     *
     * @see MatrixIO#saveBin( org.ejml_float.data.RealMatrix64F, String)
     *
     * @param fileName
     * @throws java.io.IOException
     */
    public void saveToFileBinary( String fileName ) {
        try {
			MatrixIO.saveBin(mat, fileName);
		} catch (IOException e) {
			e.printStackTrace();
		}
    }


    /**
     * <p>
     * Loads a new matrix from a CSV file.  For the file format see {@link MatrixIO}.
     * </p>
     *
     * @see MatrixIO#loadCSV(String)
     *
     * @param fileName File which is to be loaded.
     * @return The matrix.
     * @throws IOException
     */
//    private static SimpleFloatMatrix loadFromCSV( String fileName ) {
//		try {
//			RealMatrix32F mat = MatrixIO.loadFromCSV(fileName);
//			SimpleFloatMatrix floatMatrix = new SimpleFloatMatrix(mat);
//			return floatMatrix;
//		} catch (Exception e) {
//			e.printStackTrace();
//			return null;
//		}
//    }
    /**
     * <p>
     * Loads a new matrix from a serialized binary file.
     * </p>
     *
     * @see MatrixIO#loadBin(String)
     *
     * @param fileName File which is to be loaded.
     * @return The matrix.
     * @throws IOException
     */
    public static SimpleFloatMatrix loadFromBinary( String fileName){
    	try {
            RealMatrix32F mat = MatrixIO.loadBin(fileName);
            // see if its a DenseMatrix32F
            if( mat instanceof DenseMatrix32F ) {
                return SimpleFloatMatrix.wrap((DenseMatrix32F)mat);
            } else {
                // if not convert it into one and wrap it
                return SimpleFloatMatrix.wrap( new DenseMatrix32F(mat));
            }			
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}

    }
    
    

}
