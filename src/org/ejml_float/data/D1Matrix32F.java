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

package org.ejml_float.data;


import java.util.Arrays;


/**
 * A generic abstract class for matrices whose data is stored in a single 1D array of floats.  The
 * format of the elements in this array is not specified.  For example row major, column major,
 * and block row major are all common formats.
 *
 * @author Peter Abeles
 */
public abstract class D1Matrix32F implements ReshapeMatrix, RealMatrix32F {
    
	
	
	private static final long serialVersionUID = -3403393967113026754L;

	/**
     * Where the raw data for the matrix is stored.  The format is type dependent.
     */
    public float[] data;

    /**
     * Number of rows in the matrix.
     */
    public int numRows;
    /**
     * Number of columns in the matrix.
     */
    public int numCols;

    private static final float[] ZERO_FLOATS = new float[1024];

    
    /**
     * set values of a larger matrix to zero
     */
	public void reset() {
		int numElements = getNumElements();
		if (numElements < ZERO_FLOATS.length){
			System.arraycopy(ZERO_FLOATS, 0, data, 0, numElements);
			return;
		}
		
		int zerosArrayLength = ZERO_FLOATS.length;
		final int times = numElements / zerosArrayLength; 
		int position = 0;
		for (int i = 0; i < times; i++) {
			System.arraycopy(ZERO_FLOATS, 0, data, position, zerosArrayLength);
			position += zerosArrayLength;
		}
		System.arraycopy(ZERO_FLOATS, 0, data, position, numElements - position);
	}

    
   
    
    @Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.hashCode(data);
		result = prime * result + numCols;
		result = prime * result + numRows;
		return result;
	}


	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		D1Matrix32F other = (D1Matrix32F) obj;
		if (!Arrays.equals(data, other.data))
			return false;
		if (numCols != other.numCols)
			return false;
		if (numRows != other.numRows)
			return false;
		return true;
	}




	/**Sets all the elements in the matrix equal to zero.
     * @see CommonOps#fill(org.ejml_float.data.D1Matrix64F , double)
     */
    public void zero() {
        reset();
    }

  
    /**
     * Used to get a reference to the internal data.
     *
     * @return Reference to the matrix's data.
     */
    public float[] getData() {
        return data;
    }

	/**
	 * Changes the internal array reference.
	 */
	public void setData( float[] data ) {
		this.data = data;
	}

    /**
     * Returns the internal array index for the specified row and column.
     *
     * @param row Row index.
     * @param col Column index.
     * @return Internal array index.
     */
    public abstract int getIndex( int row, int col );

    /**
     * Sets the value of this matrix to be the same as the value of the provided matrix.  Both
     * matrices must have the same shape:<br>
     * <br>
     * a<sub>ij</sub> = b<sub>ij</sub><br>
     * <br>
     *
     * @param b The matrix that this matrix is to be set equal to.
     */
    public void set( D1Matrix32F b )
    {
        this.reshape(b.numRows,b.numCols);

        int dataLength = b.getNumElements();

        System.arraycopy(b.data, 0, this.data, 0, dataLength);
    }

    /**
     * Returns the value of the matrix at the specified internal array index. The element at which row and column
     * returned by this function depends upon the matrix's internal structure, e.g. row-major, column-major, or block.
     *
     * @param index Internal array index.
     * @return Value at the specified index.
     */
    public float get( int index ) {
        return data[index];
    }

    /**
     * Sets the element's value at the specified index.  The element at which row and column
     * modified by this function depends upon the matrix's internal structure, e.g. row-major, column-major, or block.
     *
     * @param index Index of element that is to be set.
     * @param val The new value of the index.
     */
    public float set( int index , float val ) {
        // See benchmarkFunctionReturn.  Pointless return does not degrade performance.  Tested on JDK 1.6.0_21
        return data[index] = val;
    }

    /**
     * <p>
     * Adds the specified value to the internal data array at the specified index.<br>
     * <br>
     * Equivalent to: this.data[index] += val;
     * </p>
     *
     * <p>
     * Intended for use in highly optimized code.  The  row/column coordinate of the modified element is
     * dependent upon the matrix's internal structure.
     * </p>
     *
     * @param index The index which is being modified.
     * @param val The value that is being added.
     */
    public float plus( int index , float val ) {
        // See benchmarkFunctionReturn.  Pointless return does not degrade performance.  Tested on JDK 1.6.0_21
        return data[index] += val;
    }

    
    
    /**
     * <p>
     * Subtracts the specified value to the internal data array at the specified index.<br>
     * <br>
     * Equivalent to: this.data[index] -= val;
     * </p>
     *
     * <p>
     * Intended for use in highly optimized code.  The  row/column coordinate of the modified element is
     * dependent upon the matrix's internal structure.
     * </p>
     *
     * @param index The index which is being modified.
     * @param val The value that is being subtracted.
     */
    public float minus( int index , float val ) {
        // See benchmarkFunctionReturn.  Pointless return does not degrade performance.  Tested on JDK 1.6.0_21
        return data[index] -= val;
    }

    /**
     * <p>
     * Multiplies the specified value to the internal data array at the specified index.<br>
     * <br>
     * Equivalent to: this.data[index] *= val;
     * </p>
     *
     * <p>
     * Intended for use in highly optimized code.  The  row/column coordinate of the modified element is
     * dependent upon the matrix's internal structure.
     * </p>
     *
     * @param index The index which is being modified.
     * @param val The value that is being multiplied.
     */
    public float times( int index , float val ) {
        // See benchmarkFunctionReturn.  Pointless return does not degrade performance.  Tested on JDK 1.6.0_21
        return data[index] *= val;
    }

    /**
     * <p>
     * Divides the specified value to the internal data array at the specified index.<br>
     * <br>
     * Equivalent to: this.data[index] /= val;
     * </p>
     *
     * <p>
     * Intended for use in highly optimized code.  The  row/column coordinate of the modified element is
     * dependent upon the matrix's internal structure.
     * </p>
     *
     * @param index The index which is being modified.
     * @param val The value that is being divided.
     */
    public float div( int index , float val ) {
        // See benchmarkFunctionReturn.  Pointless return does not degrade performance.  Tested on JDK 1.6.0_21
        return data[index] /= val;
    }

    /**
     * <p>
     * Changes the number of rows and columns in the matrix, allowing its size to grow or shrink.
     * If the saveValues flag is set to true, then the previous values will be maintained, but
     * reassigned to new elements in a row-major ordering.  If saveValues is false values will only
     * be maintained when the requested size is less than or equal to the internal array size.
     * The primary use for this function is to encourage data reuse and avoid unnecessarily declaring
     * and initialization of new memory.
     * </p>
     *
     * <p>
     * Examples:<br>
     * [ 1 2 ; 3 4 ] -> reshape( 2 , 3 , true ) = [ 1 2 3 ; 4 0 0 ]<br>
     * [ 1 2 ; 3 4 ] -> reshape( 1 , 2 , true ) = [ 1 2 ]<br>
     * [ 1 2 ; 3 4 ] -> reshape( 1 , 2 , false ) = [ 1 2 ]<br>
     * [ 1 2 ; 3 4 ] -> reshape( 2 , 3 , false ) = [ 0 0 0 ; 0 0 0 ]
     * </p>
     *
     * @param numRows The new number of rows in the matrix.
     * @param numCols The new number of columns in the matrix.
     * @param saveValues If true then the value of each element will be save using a row-major reordering.  Typically this should be false.
     */
    public abstract void reshape(int numRows, int numCols, boolean saveValues);

    /**
     * Equivalent to invoking reshape(numRows,numCols,false);
     *
     * @param numRows The new number of rows in the matrix.
     * @param numCols The new number of columns in the matrix.
     */
    @Override
    public void reshape( int numRows , int numCols ) {
        reshape(numRows,numCols,false);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getNumRows() {
        return numRows;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getNumCols() {
        return numCols;
    }

    /**
     * Sets the number of rows.
     *
     * @param numRows Number of rows
     */
    public void setNumRows(int numRows) {
        this.numRows = numRows;
    }

    /**
     * Sets the number of columns.
     *
     * @param numCols Number of columns
     */
    public void setNumCols(int numCols) {
        this.numCols = numCols;
    }
}