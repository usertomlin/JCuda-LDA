package org.linchimin.utils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.regex.Pattern;



/**
 * @author Lin Chi-Min
 *
 */
public class StringUtils {
			
	static Pattern SPACES_PATTERN = Pattern.compile("\\s+");
	
	/**
	 * a simple method for printing a vector
	 * @param vector
	 */
	public static void print(float[] vector){
		System.out.println(toString(vector, vector.length));
	}
	
	private static String toString(float[] arr, int length) {
		ArrayList<String> list = new ArrayList<>(length);
		for (int i = 0; i < length; i++) {
			list.add(format(arr[i], 4));
		}
		return list.toString();
	}
	
	private static String format(double decimalValue, int numDecimalDigits) {
	    if (numDecimalDigits < 0) throw new IllegalArgumentException();
	    BigDecimal bd = new BigDecimal(decimalValue);
	    bd = bd.setScale(numDecimalDigits, RoundingMode.HALF_UP);
	    return bd.toString();
	}
	

	/**
	 * Uppercases the first character of a string.
	 *
	 * @param s a string to capitalize
	 * @return a capitalized version of the string
	 */
	public static String capitalize(String s) {
		if (Character.isLowerCase(s.charAt(0))) {
			return Character.toUpperCase(s.charAt(0)) + s.substring(1);
		} else {
			return s;
		}
	}
	
	
}
