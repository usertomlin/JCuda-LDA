package org.linchimin.jcuda.pointer;

import java.util.Arrays;

import jcuda.Pointer;
import jcuda.Sizeof;

/**
 * 
 * class to wrap Pointer of host data
 *   
 * 
 * @author Lin Chi-Min (v381654729@gmail.com)
 *
 */
public class HostFloatsPointer {


	private float[] data;
	
	public Pointer pointer;

	
	public HostFloatsPointer(int dataLength) {
		this(new float[dataLength]); 
	}
	
	public HostFloatsPointer(float[] data) {
		putData(data);
	}
	
	public void putData(float[] data){
		this.data = data;
		this.pointer = Pointer.to(data);
	}
	
	public HostFloatsPointer copy(){
		float[] dataCopy = data.clone(); 
		HostFloatsPointer copy = new HostFloatsPointer(dataCopy);
		return copy;
	}
	
	public Pointer withElementOffsetPtr(int elementOffset){
		return pointer.withByteOffset(elementOffset * Sizeof.FLOAT);
	}
	

	/**
	 * copy floats to 'data' from buffer
	 * @return
	 */
	public float[] getData(){
		return data;
	}
	
	
	/**
	 * warning: make sure hostPinned == false
	 * before calling this
	 */
	public float getFirstValue() {
		return data[0];
	}
	
	public float getValueAt(int index) {
		return data[index];
	}
	
	
	/**
	 * ensure that length of data >= capacity
	 */
	public void ensureCapacity(int capacity){
		if (data.length >= capacity) {
			return;
		}
		data = Arrays.copyOf(data, capacity);
		pointer = Pointer.to(data);
	}
	
	
	public int size() {
		return data.length;
	}
	
	public void free(){
		data = null;
	}

	
}
