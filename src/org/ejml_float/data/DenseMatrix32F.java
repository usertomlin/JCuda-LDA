/*
 * Copyright (c) 2009-2015, Peter Abeles. All Rights Reserved.
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

package org.ejml_float.data;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Arrays;

import org.ejml_float.ops.MatrixDimensionException;
import org.ejml_float.ops.MatrixIO;


/**
 * <p>
 * DenseMatrix64F is a dense matrix with elements that are 32-bit floats.  A matrix
 * is the fundamental data structure in linear algebra.  Unlike a sparse matrix, there is no
 * compression in a dense matrix and every element is stored in memory.  This allows for fast
 * reads and writes to the matrix.
 * </p>
 *
 * <p>
 * The matrix is stored internally in a row-major 1D array format:<br>
 * <br>
 * data[ y*numCols + x ] = data[y][x]<br>
 * <br>
 * For example:<br>
 * data =
 * </p>
 * <pre>
 * a[0]  a[1]   a[2]   a[3]
 * a[4]  a[5]   a[6]   a[7]
 * a[8]  a[9]   a[10]  a[11]
 * a[12] a[13]  a[14]  a[15]
 * </pre>
 *
 * @author Peter Abeles
 */
public class DenseMatrix32F extends D1Matrix32F {

    private static final long serialVersionUID = 1L;

	/**
     * <p>
     * Creates a new matrix which has the same value as the matrix encoded in the
     * provided array.  The input matrix's format can either be row-major or
     * column-major.
     * </p>
     *
     * <p>
     * Note that 'data' is a variable argument type, so either 1D arrays or a set of numbers can be
     * passed in:<br>
     * DenseMatrix a = new DenseMatrix(2,2,true,new float[]{1,2,3,4});<br>
     * DenseMatrix b = new DenseMatrix(2,2,true,1,2,3,4);<br>
     * <br>
     * Both are equivalent.
     * </p>
     *
     * @param numRows The number of rows.
     * @param numCols The number of columns.
     * @param rowMajor If the array is encoded in a row-major or a column-major format.
     * @param data The formatted 1D array. Not modified.
     */
    public DenseMatrix32F(int numRows, int numCols, boolean rowMajor, float... data) {
        final int length = numRows * numCols;
        this.data = new float[ length ];

        this.numRows = numRows;
        this.numCols = numCols;

        set(numRows,numCols, rowMajor, data);
    }
    

    /**
     * <p>
     * Creates a matrix with the values and shape defined by the 2D array 'data'.
     * It is assumed that 'data' has a row-major formatting:<br>
     *  <br>
     * data[ row ][ column ]
     * </p>
     * @param data 2D array representation of the matrix. Not modified.
     */
    public DenseMatrix32F(float data[][]) {
        this.numRows = data.length;
        this.numCols = data[0].length;

        this.data = new float[ numRows*numCols ];

        int pos = 0;
        for( int i = 0; i < numRows; i++ ) {
            float []row = data[i];

            if( row.length != numCols ) {
                throw new IllegalArgumentException("All rows must have the same length");
            }

            System.arraycopy(row,0,this.data,pos,numCols);

            pos += numCols;
        }
    }

    public DenseMatrix32F(float data[]) {
        this.numRows = 1;
        this.numCols = data.length;
        this.data = Arrays.copyOf(data, data.length);
    }
    
    /**
     * Creates a new Matrix with the specified shape whose elements initially
     * have the value of zero.
     *
     * @param numRows The number of rows in the matrix.
     * @param numCols The number of columns in the matrix.
     */
    public DenseMatrix32F(int numRows, int numCols) {
        data = new float[ numRows * numCols ];
        this.numRows = numRows;
        this.numCols = numCols;
    }
    
    public DenseMatrix32F(int numRows, int numCols, float[] data) {
        this.data = data;
        if (data.length != numRows * numCols){
        	throw new MatrixDimensionException("the data length does not match numRows and numCols");
        }
        this.numRows = numRows;
        this.numCols = numCols;
    }
    /**
     * Creates a new matrix which is equivalent to the provided matrix.  Note that
     * the length of the data will be determined by the shape of the matrix.
     *
     * @param orig The matrix which is to be copied.  This is not modified or saved.
     */
    public DenseMatrix32F(DenseMatrix32F orig) {
        this(orig.numRows,orig.numCols);
        System.arraycopy(orig.data, 0, this.data, 0, orig.getNumElements());
    }

    /**
     * This declares an array that can store a matrix up to the specified length.  This is use full
     * when a matrix's size will be growing and it is desirable to avoid reallocating memory.
     *
     * @param length The size of the matrice's data array.
     */
    public DenseMatrix32F(int length) {
        data = new float[ length ];
    }
    
//    @Override
//    public boolean equals(Object obj) {
//    	if (obj instanceof DenseMatrix32F)
//    	return super.equals(obj);
//    }
//    public boolean equals(){
    	
//    }

    /**
     * Default constructor in which nothing is configured.  THIS IS ONLY PUBLICLY ACCESSIBLE SO THAT THIS
     * CLASS CAN BE A JAVA BEAN. DON'T USE IT UNLESS YOU REALLY KNOW WHAT YOU'RE DOING!
     */
    public DenseMatrix32F(){}

    /**
     * Creates a new DenseMatrix64F which contains the same information as the provided Matrix64F.
     *
     * @param mat Matrix whose values will be copied.  Not modified.
     */
    public DenseMatrix32F(RealMatrix32F mat) {
        this(mat.getNumRows(),mat.getNumCols());
        for( int i = 0; i < numRows; i++ ) {
            for( int j = 0; j < numCols; j++ ) {
                set(i,j, mat.get(i,j));
            }
        }
    }

    /**
     * Creates a new DenseMatrix64F around the provided data.  The data must encode
     * a row-major matrix.  Any modification to the returned matrix will modify the
     * provided data.
     *
     * @param numRows Number of rows in the matrix.
     * @param numCols Number of columns in the matrix.
     * @param data Data that is being wrapped. Referenced Saved.
     * @return A matrix which references the provided data internally.
     */
	public static DenseMatrix32F wrap(int numRows, int numCols, float[] data ) {
        DenseMatrix32F s = new DenseMatrix32F();
        s.data = data;
        s.numRows = numRows;
        s.numCols = numCols;
        return s;
    }

    
    public void checkDimensions(int expectedNumRows, int expectedNumCols) {
    	if (numRows != expectedNumRows || numCols != expectedNumCols) {
    		throw new Error("Error in DenseMatrix32F.checkDimensions() : "
    				+ "the dimensions does not match; numRows = " + expectedNumRows + ", numCols = " + expectedNumCols);
    	}
    }
    
    /**
     * @inheritDoc
     */
    @Override
    public void reshape(int numRows, int numCols, boolean saveValues) {
        if( data.length < numRows * numCols ) {
			float[] d = new float[numRows * numCols];
			if (saveValues) {
				System.arraycopy(data, 0, d, 0, getNumElements());
			}
            this.data = d;
        }

        this.numRows = numRows;
        this.numCols = numCols;
    }

    /**
     * <p>
     * Assigns the element in the Matrix to the specified value.  Performs a bounds check to make sure
     * the requested element is part of the matrix. <br>
     * <br>
     * a<sub>ij</sub> = value<br>
     * </p>
     *
     * @param row The row of the element.
     * @param col The column of the element.
     * @param value The element's new value.
     */
    @Override
    public void set( int row , int col , float value ) {
//        if( col < 0 || col >= numCols || row < 0 || row >= numRows ) {
//            throw new IllegalArgumentException("Specified element is out of bounds: ("+row+" , "+col+")");
//        }
        data[ row * numCols + col ] = value;
    }

    @Override
    public void unsafe_set( int row , int col , float value ) {
        data[ row * numCols + col ] = value;
    }

    /**
     * <p>
     * Adds 'value' to the specified element in the matrix.<br>
     * <br>
     * a<sub>ij</sub> = a<sub>ij</sub> + value<br>
     * </p>
     *
     * @param row The row of the element.
     * @param col The column of the element.
     * @param value The value that is added to the element
     */
    // todo move to commonops
    public void add( int row , int col , float value ) {
        if( col < 0 || col >= numCols || row < 0 || row >= numRows ) {
            throw new IllegalArgumentException("Specified element is out of bounds");
        }

        data[ row * numCols + col ] += value;
    }

    /**
     * Returns the value of the specified matrix element.  Performs a bounds check to make sure
     * the requested element is part of the matrix.
     *
     * @param row The row of the element.
     * @param col The column of the element.
     * @return The value of the element.
     */
    @Override
    public float get( int row , int col ) {
//        if( col < 0 || col >= numCols || row < 0 || row >= numRows ) {
//            throw new IllegalArgumentException("Specified element is out of bounds: "+row+" "+col);
//        }

        return data[ row * numCols + col ];
    }

    @Override
    public float unsafe_get( int row , int col ) {
        return data[ row * numCols + col ];
    }

    @Override
    public int getIndex( int row , int col ) {
        return row * numCols + col;
    }

    
    
    
    
    
    /**
     * Determins if the specified element is inside the bounds of the Matrix.
     *
     * @param row The element's row.
     * @param col The elements' column.
     * @return True if it is inside the matrices bound, false otherwise.
     */
    public boolean isInBounds( int row  , int col ) {
        return( col >= 0 && col < numCols && row >= 0 && row < numRows );
    }

    /**
     * Returns the number of elements in this matrix, which is equal to
     * the number of rows times the number of columns.
     *
     * @return The number of elements in the matrix.
     */
    @Override
    public int getNumElements() {
        return numRows*numCols;
    }

    /**
     * Sets this matrix equal to the matrix encoded in the array.
     *
     * @param numRows The number of rows.
     * @param numCols The number of columns.
     * @param rowMajor If the array is encoded in a row-major or a column-major format.
     * @param data The formatted 1D array. Not modified.
     */
    public void set(int numRows, int numCols, boolean rowMajor, float ...data) {
        reshape(numRows,numCols);
        int length = numRows*numCols;

        if( length > this.data.length )
            throw new IllegalArgumentException("The length of this matrix's data array is too small.");

        if( rowMajor ) {
            System.arraycopy(data,0,this.data,0,length);
        } else {
            int index = 0;
            for( int i = 0; i < numRows; i++ ) {
                for( int j = 0; j < numCols; j++ ) {
                    this.data[index++] = data[j*numRows+i];
                }
            }
        }
    }


  

 
    
    /**
     * Creates and returns a matrix which is idential to this one.
     *
     * @return A new identical matrix.
     */
    @SuppressWarnings({"unchecked"})
    public DenseMatrix32F copy() {
        return new DenseMatrix32F(this);
    }

    @Override
    public void set(Matrix original) {
        RealMatrix32F m = (RealMatrix32F)original;

        reshape(original.getNumRows(),original.getNumCols());

        if( original instanceof DenseMatrix32F) {
            // do a faster copy if its of type DenseMatrix64F
            System.arraycopy(((DenseMatrix32F)m).data,0,data,0,numRows*numCols);
        } else {
            int index = 0;
            for (int i = 0; i < numRows; i++) {
                for (int j = 0; j < numCols; j++) {
                    data[index++] = m.get(i, j);
                }
            }
        }
    }

    /**
     * Prints the value of this matrix to the screen.  For more options see
     * {@link org.ejml_float.UtilEjml}
     *
     */
    @Override
    public void print() {
        MatrixIO.print(System.out,this);
    }

    /**
     * <p>
     * Prints the value of this matrix to the screen using the same format as {@link java.io.PrintStream#printf).
     * </p>
     *
     * @param format The format which each element is printed uses.
     */
    public void print( String format ) {
        MatrixIO.print(System.out,this,format);
    }

    /**
     * <p>
     * Converts the array into a string format for display purposes.
     * The conversion is done using {@link org.ejml_float.ops.MatrixIO#print(java.io.PrintStream, org.ejml_float.data.RealMatrix64F)}.
     * </p>
     *
     * @return String representation of the matrix.
     */
    @Override
    public String toString() {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        MatrixIO.print(new PrintStream(stream),this);

        return stream.toString();
    }
    
    /**
	 * copy valus from sourceMatrix from the beginning of the matrix
	 * @param sourceMatrix
	 */
	public void copyValuesFrom(DenseMatrix32F sourceMatrix) {
		final int numSourceElements = sourceMatrix.getNumElements();
    	if (numSourceElements != getNumElements()){
			throw new IllegalArgumentException("IllegalArgumentException: the matrices data lengths do not match.");
    	}
    	System.arraycopy(sourceMatrix.data, 0, data, 0, numSourceElements);
	}

	public void copyRowsFrom(DenseMatrix32F sourceRowMatrix) {
		copyRowsFrom(sourceRowMatrix, numRows);
	}
	
	/**
	 * the numCols of this matrix should be equal to sourceRowMatrix.getNumElements()
	 * @param sourceRowMatrix 
	 * @param numRows
	 */
	public void copyRowsFrom(DenseMatrix32F sourceRowMatrix, int numRows) {

		final int numCols = this.numCols;
		if (numCols != sourceRowMatrix.getNumElements()){
			throw new MatrixDimensionException("the matrices dimensions do not match.");
    	}
    	int offset = 0;
    	float[] source = sourceRowMatrix.data;
    	float[] target = data;
    	for (int i = 0; i < numRows; i++) {
    		System.arraycopy(source, 0, target, offset, numCols);
    		offset += numCols;
		}
	}
	
	
	
	public void copyRowFrom(DenseMatrix32F sourceRowMatrix, int destinationRowIndex) {

		if (numCols != sourceRowMatrix.getNumElements()){
			throw new MatrixDimensionException("the matrices dimensions do not match.");
    	}
    	System.arraycopy(sourceRowMatrix.data, 0, data, destinationRowIndex * numCols, numCols);
	}
	
		
	public void copyColumnsFrom(DenseMatrix32F sourceColumnMatrix) {
		copyColumnsFrom(sourceColumnMatrix, numCols);
	}
	
	/**
	 * the numCols of this matrix should be equal to sourceColumnMatrix.getNumElements()
	 * @param sourceColumnMatrix 
	 * @param numColumns
	 */
	public void copyColumnsFrom(DenseMatrix32F sourceColumnMatrix, int numColumns) {

		final int numRows = this.numRows, numCols = this.numCols;
		if (numRows != sourceColumnMatrix.getNumElements()){
			throw new MatrixDimensionException("the matrices dimensions do not match.");
    	}
    	
    	float[] source = sourceColumnMatrix.data;
    	float[] target = data;
    	
    	int offset = 0;    	
    	for (int i = 0; i < numRows; i++) {
    		float val = source[i];
    		Arrays.fill(target, offset, offset + numColumns, val);
    		offset += numCols;
		}
	}
	
}
