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

import org.ejml_float.data.DenseMatrix32F;



/**
 * <p>
 * {@link SimpleFloatMatrix} is a wrapper around {@link org.ejml_float.data.DenseMatrix64F} that provides an
 * easy to use object oriented interface for performing matrix operations.  It is designed to be
 * more accessible to novice programmers and provide a way to rapidly code up solutions by simplifying
 * memory management and providing easy to use functions.
 * </p>
 *
 * <p>
 * Most functions in SimpleMatrix do not modify the original matrix.  Instead they
 * create a new SimpleMatrix instance which is modified and returned.  This greatly simplifies memory
 * management and writing of code in general. It also allows operations to be chained, as is shown
 * below:<br>
 * <br>
 * SimpleMatrix K = P.mult(H.transpose().mult(S.invert()));
 * </p>
 *
 * <p>
 * Working with both {@link org.ejml_float.data.DenseMatrix64F} and SimpleMatrix in the same code base is easy.
 * To access the internal DenseMatrix64F in a SimpleMatrix simply call {@link SimpleFloatMatrix#getMatrix()}.
 * To turn a DenseMatrix64F into a SimpleMatrix use {@link SimpleFloatMatrix#wrap(org.ejml_float.data.DenseMatrix64F)}.  Not
 * all operations in EJML are provided for SimpleMatrix, but can be accessed by extracting the internal
 * DenseMatrix64F.
 * </p>
 *
 * <p>
 * EXTENDING: SimpleMatrix contains a list of narrowly focused functions for linear algebra.  To harness
 * the functionality for another application and to the number of functions it supports it is recommended
 * that one extends {@link SimpleBase} instead.  This way the returned matrix type's of SimpleMatrix functions
 * will be of the appropriate types.  See StatisticsMatrix inside of the examples directory.
 * </p>
 *
 * <p>
 * PERFORMANCE: The disadvantage of using this class is that it is more resource intensive, since
 * it creates a new matrix each time an operation is performed.  This makes the JavaVM work harder and
 * Java automatically initializes the matrix to be all zeros.  Typically operations on small matrices
 * or operations that have a runtime linear with the number of elements are the most affected.  More
 * computationally intensive operations have only a slight unnoticeable performance loss.  MOST PEOPLE
 * SHOULD NOT WORRY ABOUT THE SLIGHT LOSS IN PERFORMANCE.
 * </p>
 *
 * <p>
 * It is hard to judge how significant the performance hit will be in general.  Often the performance
 * hit is insignificant since other parts of the application are more processor intensive or the bottle
 * neck is a more computationally complex operation.  The best approach is benchmark and then optimize the code.
 * </p>
 *
 * <p>
 * If SimpleMatrix is extended then the protected function {link #createMatrix} should be extended and return
 * the child class.  The results of SimpleMatrix operations will then be of the correct matrix type. 
 * </p>
 *
 * <p>
 * The object oriented approach used in SimpleMatrix was originally inspired by Jama.
 * http://math.nist.gov/javanumerics/jama/
 * </p>
 *
 * @author Peter Abeles
 */
public class SimpleFloatMatrix extends SimpleBase<SimpleFloatMatrix> {

	
	private static final long serialVersionUID = 1L;
	

    /**
     * Creates a new matrix that is initially set to zero with the specified dimensions.
     *
     * @see org.ejml_float.data.DenseMatrix64F#DenseMatrix64F(int, int) 
     *
     * @param numRows The number of rows in the matrix.
     * @param numCols The number of columns in the matrix.
     */
    private SimpleFloatMatrix(int numRows, int numCols) {
        mat = new DenseMatrix32F(numRows, numCols);
    }

    public SimpleFloatMatrix(int numRows, int numCols, float[] data){
    	mat = new DenseMatrix32F(numRows, numCols, data);
    }

    /**
     * Constructor for internal library use only.  Nothing is configured and is intended for serialization.
     */
    private SimpleFloatMatrix(){}

    

    
    /**
     * @inheritdoc
     */
    @Override
    protected SimpleFloatMatrix createMatrix( int numRows , int numCols ) {
        return new SimpleFloatMatrix(numRows,numCols);
    }

    
    
    /**
     * Creates a new SimpleMatrix with the specified DenseMatrix64F used as its internal matrix.  This means
     * that the reference is saved and calls made to the returned SimpleMatrix will modify the passed in DenseMatrix64F.
     *
     * @param internalMat The internal DenseMatrix64F of the returned SimpleMatrix. Will be modified.
     */
    public static SimpleFloatMatrix wrap( DenseMatrix32F internalMat ) {
        SimpleFloatMatrix ret = new SimpleFloatMatrix();
        ret.mat = internalMat;
        return ret;
    }
    

    
//	private static final float[] SHORT_TO_FLOAT_MAP = setShortToFloatMap();
//	private static float[] setShortToFloatMap() {
//		float[] map = new float[65536];
//		for (int s = -32768; s <= 32767; ++s) {
//			map[s+32768] = (float) StrictMath.exp(s * 0.001d);
//		}
//		return map;
//	}
	
	
	/**
	 * 1. only for positive floats
	 * 2. use log and exp for conversion
	 * 3. keep exp(-32.768) to exp(32.767)
	 */
	protected static short floatToShort(float f){
		double lgf = Math.log(f);
		if (lgf > 32.767){
			return 32767;
		} else if (lgf < -32.768){
			return -32768;
		} else {
			return (short) (lgf * 1000);
		}
	}
	/** 
	 * Used in conjunction with 'floatToShort'
	 */
	protected static float shortToFloat(short s){
//		return SHORT_TO_FLOAT_MAP[s + 32768];
		double lgf = s * 0.001d;
		return (float) StrictMath.exp(lgf);
	}
	
	
	/**
	 * for debugging
	 */
	protected static double relativeError(double v1, double v2){
		double denom = Math.abs(v1) + Math.abs(v2);
		if (denom == 0){
			return 0;
		}
		double nom = Math.abs(v1 - v2);
		return nom / denom;
	}

	
    
    
}
