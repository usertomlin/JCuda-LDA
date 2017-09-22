package org.linchimin.common;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.regex.Pattern;

import gnu.trove.TIntArrayList;
import gnu.trove.TObjectIntHashMap;





/**
 * a class for storing a vocabulary of words
 * 
 * @author Lin Chi-Min (v381654729@gmail.com)
 */
public class Dictionary implements Serializable {

	private static final long serialVersionUID = 1L;
	protected static final float DEFAULT_LOAD_FACTOR = 0.5f;
	
	private static final int NO_ENTRY_VALUE = 0;

	/**
	 * used to look up index by feature; the index starts from 0
	 */
	private final TObjectIntHashMap<String> valueToIndexMap;

	/**
	 * used to look up word by index; the index starts from 0
	 */
	private final ArrayList<String> values;

	/**
	 * frequencies of corresponding elements;
	 * the frequencies are not larger than 2^31 - 1
	 */
	private final TIntArrayList frequencies;

	public Dictionary() {
		this(128);
	}

	
	public Dictionary(int capacity) {
		this.valueToIndexMap = new TObjectIntHashMap<String>(capacity, DEFAULT_LOAD_FACTOR);
		
		this.values = new ArrayList<String>(capacity);
		this.frequencies = new TIntArrayList(capacity);
	}

	public Dictionary(TObjectIntHashMap<String> valueToIndexMap, ArrayList<String> values) {
		this(valueToIndexMap, values, new TIntArrayList(new int[values.size()]));
	}
	
	public Dictionary(TObjectIntHashMap<String> valueToIndexMap, ArrayList<String> values, TIntArrayList frequencies) {
		if (values.size() != valueToIndexMap.size() || values.size() != frequencies.size()) {
			throw new IllegalArgumentException("IllegalArgumentException: the sizes are not all compatible; "
					+ "values.size() = " + values.size() + ", valueToIndexMap.size() = " + valueToIndexMap.size() + ", frequencies.size() = " + frequencies.size());
		}
		this.valueToIndexMap = valueToIndexMap;
		this.values = values;
		this.frequencies = frequencies;
		checkSizes();
	}

	
	public Dictionary(List<String> values, TIntArrayList frequencies) {
		this(values, frequencies, true);
	}
	
	public Dictionary(List<String> values, TIntArrayList frequencies, boolean checkDuplicates) {
		this(Math.max(128, values.size() + 3));
		if (checkDuplicates){
			if (new HashSet<String>(values).size() < values.size()){
				throw new IllegalArgumentException("the collection contains duplicate values; preprocess it before constructing a Dictionary.");
			}
		}
		for (int i = 0; i < values.size(); i++) {
			String value = values.get(i);
			int frequency = frequencies.getQuick(i);
			this.valueToIndexMap.put(value, i);
			this.values.add(value);
			this.frequencies.add(frequency);
		}
		checkSizes();
	}
	
	
	/**
	 * @param elements
	 */
	public Dictionary(String... elements) {
		this(Arrays.asList(elements), new TIntArrayList(new int[elements.length]), true);
	}

	public Dictionary(String[] elements, boolean checkDuplicates) {
		this(Arrays.asList(elements), new TIntArrayList(new int[elements.length]), checkDuplicates);
	}
	
	
	public Dictionary(List<String> values){
		this(values, new TIntArrayList(new int[values.size()]), true);
	}

	public Dictionary(List<String> values, int[] frequencies) {
		this(values);
		this.frequencies.add(frequencies);
	}
	
	
	private void checkSizes() {
		final int size = valueToIndexMap.size();
		if (values.size() != size || frequencies.size() != size) {
			throw new IllegalArgumentException(
					"IllegalArgumentException: the sizes are not congruent: \n" + "valueToIndexMap.size() = " + size
							+ ", values.size() = " + values.size() + ", frequencies.size() = " + frequencies.size());
		}
	}

	public boolean containsWord(String word) {
		return lookupIndex(word) >= 0;
	}

	/**
	 * return noEntryValue = getNoEntryValue() if no present
	 * @param word
	 * @return
	 */
	public int lookupIndex(String word) {
		return lookupIndex(word, false);
	}
	
	private int lookupIndex(String word, boolean addIfNotPresent) {
		if (word == null) {
			throw new NullPointerException("NullPointerException: the argument cannot be null: word");
		}
		
		int wordIndex = valueToIndexMap.get(word);
		if (addIfNotPresent){
			if (wordIndex == NO_ENTRY_VALUE && valueToIndexMap.containsKey(word) == false){
				wordIndex = values.size();
				valueToIndexMap.put(word, wordIndex);
				values.add(word);
				frequencies.add(1);
			} else {
				int currentFrequency = frequencies.get(wordIndex);
				frequencies.set(wordIndex, currentFrequency + 1);
			}
		}
		return wordIndex;
	}

	public int add(String word) {
		return lookupIndex(word, true);
	}
	
	public int size() {
		return values.size();
	}

	public String lookupValue(int index) {
		return values.get(index);
	}

	
	/**index, word, and frequency; for logging
	 * @return
	 */
	private String getDetail(int index){
		return index + "-" + values.get(index) + "-" + frequencies.get(index);
	}

	@Override
	public String toString() {
		final int printSize = Math.min(30, size());
		StringBuilder builder = new StringBuilder();
		for (int i = 0; i < printSize; i++) {
			builder.append(getDetail(i));
			builder.append(", ");
		}
		if (printSize < size()) {
			builder.append("... ");
		}
		return "Dictionary [size = " + size() + ", indices and features = " + builder + "]";
	}

	
	public static Dictionary loadFromWordList(String filePath){
		return loadFromWordList(filePath, false);
	}
	
	/**
	 * @param filePath : assumed a list of distinct words;
	 * 
	 */
	public static Dictionary loadFromWordList(String filePath, boolean lowercase) {
		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(filePath), StandardCharsets.UTF_8));
			final Pattern spaceOrTabPattern = Pattern.compile("[\t ]");
			ArrayList<String> elements = new ArrayList<String>(100);
			String line = null;
			while ((line = reader.readLine()) != null) {
				String element = spaceOrTabPattern.split(line)[0];
				if (lowercase){
					element = element.toLowerCase();
				}
				elements.add(element);
			}
			reader.close();
			return new Dictionary(elements);
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	/**
	 * a method for debugging word indices
	 * @param wordIndices
	 */
	public void printWordIndicesAsText(int[] wordIndices) {
		
		StringBuilder builder = new StringBuilder(wordIndices.length * 8);

		for (int wordIndex : wordIndices) {
			if (wordIndex < 0){
				builder.append('\n');
			} else {
				String word = values.get(wordIndex);
				if (word.isEmpty()){
					continue;
				}
				if (builder.length() == 0 || builder.charAt(builder.length()-1) == '\n'){
					word = capitalize(word);
				} 
				builder.append(word);	
				builder.append(' ');
			}
		}
		System.out.println(builder);
	}
	
	private String capitalize(String s) {
		if (Character.isLowerCase(s.charAt(0))) {
			return Character.toUpperCase(s.charAt(0)) + s.substring(1);
		} else {
			return s;
		}
	}

	
}
