package org.linchimin.jcuda.pointer;

import jcuda.Pointer;

/**
 * 
 * @author Lin Chi-Min (v381654729@gmail.com)
 */
public class ConstantHostData {

	public static final Pointer TEMP = Pointer.to(new float[]{0});
	public static final Pointer ONE = Pointer.to(new float[]{1});
	public static final Pointer ZERO = Pointer.to(new float[]{0});
	
}
