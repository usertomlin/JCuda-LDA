package org.linchimin.jcuda.pointer;

import java.util.Arrays;

import jcuda.Pointer;


/**
 * a wrapper for the two fields pointer and data;
 * 
 * 
 * @author Lin Chi-Min (v381654729@gmail.com)
 *
 */
public class HostIntsPointer {
	
	public Pointer pointer;
	private int[] data;
	
	public HostIntsPointer(int dataLength) {
		this(new int[dataLength]); 
	}
	
	public HostIntsPointer(int[] data){
		putData(data);
	}
	
	public HostIntsPointer copy(){
		int[] dataCopy = data.clone(); 
		HostIntsPointer copy = new HostIntsPointer(dataCopy);
		return copy;
	}
	
	
	public void putData(int[] data){
		this.data = data;
		this.pointer = Pointer.to(data);
	}

	
	/**
	 * copy ints to 'data' from buffer
	 * @return
	 */
	public int[] getData(){
		return data;
	}
	
	
	/**
	 * warning: make sure hostPinned == false
	 * before calling this
	 */
	public int getFirstValue(){
		return data[0];
	}

	public int getValueAt(int index) {
		return data[index];
	}
	
	public void ensureCapacity(int capacity){
		if (capacity <= data.length) {
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
