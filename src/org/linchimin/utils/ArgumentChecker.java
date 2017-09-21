package org.linchimin.utils;




/**
 * 
 * @author Lin Chi-Min (v381654729@gmail.com)
 */
public class ArgumentChecker {
	
	private static final String NOT_LARGER_EXCEPTION = "IllegalStateException: the first value should be larger than the second value; ";
	private static final String NOT_EQUAL_OR_SMALLER_EXCEPTION = "IllegalStateException: the first value should be smaller than the second value; ";
	
	public static void checkLarger(int v1, int v2){
		if (v1 > v2 == false){
			throw new IllegalStateException(NOT_LARGER_EXCEPTION + "; v1 = " + v1 + ", v2 = " + v2);
		}
	}
	public static void checkEqualOrSmaller(int v1, int v2){
		if (v1 <= v2 == false){
			throw new IllegalStateException(NOT_EQUAL_OR_SMALLER_EXCEPTION + "; v1 = " + v1 + ", v2 = " + v2);
		}
	}
	
	/**
	 * check if all indices are in bounds
	 */
	public static void checkAllInBounds(int[] indices, final int startIndex, final int endIndex){
		for (int i = 0; i < indices.length; i++) {
			int index = indices[i];
			if (index < startIndex || index >= endIndex) {
				throw new IndexOutOfBoundsException("IndexOutOfBoundsException: index = "
						+ index + ", startIndex = " + startIndex + ", endIndex = " + endIndex);
			}
		}
	}
	
	/**
	 * check if the values are of non-decrasing order
	 * @param values
	 */
	public static void checkNonDecreasingOrder(double... values){
		final int length = values.length;
		if (length <= 1)
			return;
		double previousValue = values[0]; 
		for (int i = 1; i < values.length; i++) {
			double value = values[i];
			if (value < previousValue){
				throw new IllegalArgumentException("IllegalArgumentException: the values should be of non-decreasing order; "
						+ "values[" +(i-1) + "] = " + previousValue + ", values[" + i + "] = " + value);
			}
			previousValue = value;
		}
	}

	
}
