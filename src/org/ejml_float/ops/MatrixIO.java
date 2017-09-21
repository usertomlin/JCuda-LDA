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

package org.ejml_float.ops;

import java.io.*;

import org.ejml_float.data.*;



/**
 * Provides simple to use routines for reading and writing matrices to and from files.
 *
 * @author Peter Abeles
 */
public class MatrixIO {

    /**
     * Saves a matrix to disk using Java binary serialization.
     *
     * @param A The matrix being saved.
     * @param fileName Name of the file its being saved at.
     * @throws java.io.IOException
     */
    public static void saveBin(RealMatrix32F A, String fileName)
        throws IOException
    {
        FileOutputStream fileStream = new FileOutputStream(fileName);
        ObjectOutputStream stream = new ObjectOutputStream(fileStream);

        try {
            stream.writeObject(A);
            stream.flush();
        } finally {
            // clean up
            try {
                stream.close();
            } finally {
                fileStream.close();
            }
        }

    }

    /**
     * Loads a DeneMatrix64F which has been saved to file using Java binary
     * serialization.
     *
     * @param fileName The file being loaded.
     * @return  DenseMatrix64F
     * @throws IOException
     */
    @SuppressWarnings({ "unchecked", "resource" })
	public static <T extends RealMatrix32F> T loadBin(String fileName)
        throws IOException
    {
        FileInputStream fileStream = new FileInputStream(fileName);
        ObjectInputStream stream = new ObjectInputStream(fileStream);

        T ret;
        try {
            ret = (T)stream.readObject();
            if( stream.available() !=  0 ) {
                throw new RuntimeException("File not completely read?");
            }
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }

        stream.close();
        return (T)ret;
    }

    /**
     * Saves a matrix to disk using in a Column Space Value (CSV) format. For a 
     * description of the format see {@link MatrixIO#loadFromCSV(String)}.
     *
     * @param A The matrix being saved.
     * @param fileName Name of the file its being saved at.
     * @throws java.io.IOException
     */
    public static void saveCSV(D1Matrix32F A , String fileName)
        throws IOException
    {
        PrintStream fileStream = new PrintStream(fileName);

        fileStream.println(A.getNumRows() + " " + A.getNumCols() + " real");
        for( int i = 0; i < A.getNumRows(); i++ ) {
            for( int j = 0; j < A.getNumCols(); j++ ) {
                fileStream.print(A.get(i,j)+" ");
            }
            fileStream.println();
        }
        fileStream.close();
    }

    /**
	 * @param mat
	 * @param fileName
	 * @param numColumnsPerLine
	 */
	public static void saveCSV(DenseMatrix32F A, String fileName, int numColumnsPerLine) 
			throws IOException 
	{
		PrintStream fileStream = new PrintStream(fileName);

		fileStream.println(A.getNumRows() + " " + A.getNumCols() + " real");
		for (int i = 0; i < A.getNumRows(); i++) {
			for (int j = 0; j < A.getNumCols(); j++) {
				if (j % numColumnsPerLine == 0) {
					fileStream.println();
				}
				fileStream.print(A.get(i, j) + " ");
			}
			fileStream.println();
		}
		fileStream.close();
	}
    
    

    public static void print( PrintStream out , RealMatrix32F mat ) {
		print(out, mat, 6, 3);
    }

	public static void printCorner(PrintStream out, RealMatrix32F mat, int numTopRows, int numTopCols){
		printCorner(out, mat, numTopRows, numTopCols, 6, 3);
	}
    
    public static void print(PrintStream out, RealMatrix32F mat , int numChar , int precision ) {
        String format = "%"+numChar+"."+precision+"f ";
		print(out, mat, format);
    }

	public static void printCorner(PrintStream out, RealMatrix32F mat,  int numTopRows, int numTopCols, int numChar, int precision) {
		String format = "%" + numChar + "." + precision + "f ";
		printCorner(out, mat, numTopRows, numTopCols, format);
	}
	
    public static void print(PrintStream out , RealMatrix32F mat , String format ) {

        String type = ReshapeMatrix.class.isAssignableFrom(mat.getClass()) ? "dense real" : "dense fixed";
        out.println("Type = "+type+" , numRows = "+mat.getNumRows()+" , numCols = "+mat.getNumCols());

        format += " ";

        for( int y = 0; y < mat.getNumRows(); y++ ) {
            for( int x = 0; x < mat.getNumCols(); x++ ) {
                out.printf(format,mat.get(y,x));
            }
            out.println();
        }
    }
    
    public static void printCorner(PrintStream out, RealMatrix32F mat, int numTopRows, int numTopCols, String format) {
    	final int numCols = mat.getNumCols();
    	String type = ReshapeMatrix.class.isAssignableFrom(mat.getClass()) ? "dense real" : "dense fixed";
		out.println("Type = " + type + " , numRows = " + mat.getNumRows() + " , numCols = " + numCols);

		format += " ";
		for (int y = 0; y < numTopRows; y++) {
			for (int x = 0; x < numTopCols; x++) {
				out.printf(format, mat.get(y, x)); 
			}
			out.println();
		}
	}


	public static void printWithComma(PrintStream writer, DenseMatrix32F mat) {
		printWithComma(writer,mat,6,3);
	}

	private static void printWithComma(PrintStream writer, DenseMatrix32F mat, int numChar , int precision) {
		String format = "%" + numChar + "." + precision + "f ";
		printWithComma(writer, mat,format);
	}

	private static void printWithComma(PrintStream writer, DenseMatrix32F mat, String format) {
		String type = ReshapeMatrix.class.isAssignableFrom(mat.getClass()) ? "dense real" : "dense fixed";

        writer.println("Type = "+type+" , numRows = "+mat.getNumRows()+" , numCols = "+mat.getNumCols());

        format += " ";

        for( int y = 0; y < mat.getNumRows(); y++ ) {
            for( int x = 0; x < mat.getNumCols(); x++ ) {
                writer.printf(format,mat.get(y,x));
                writer.print(", ");
            }
            writer.println();
        }
	}

    
}
