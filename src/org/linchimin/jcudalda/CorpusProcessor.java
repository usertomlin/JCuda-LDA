package org.linchimin.jcudalda;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.linchimin.common.Dictionary;

import gnu.trove.TIntArrayList;
import smile.nlp.tokenizer.PennTreebankTokenizer;


/**
 * a helper class for document tokenization, text iteration,  
 * and the processing of text files like 'resources/example-docs.txt' to ArrayList<int[]> serialized files of documents word indices
 * 
 * @author Lin Chi-Min (v381654729@gmail.com)
 *
 */
public class CorpusProcessor {

	private static final String UNKNOWN_TOKEN = LDAGPUTrainer.UNKNOWN_TOKEN;
	
	private Dictionary dictionary;
	private int unknownTokenIndex;
	
	
	public CorpusProcessor(String vocabularyFilePath) {
		this(Dictionary.loadFromWordList(vocabularyFilePath));
	}

	public CorpusProcessor(Dictionary dictionary){
		this.dictionary = dictionary;
		if (dictionary.containsWord(UNKNOWN_TOKEN) == false){
			dictionary.add(UNKNOWN_TOKEN);
		}
		this.unknownTokenIndex = dictionary.lookupIndex(UNKNOWN_TOKEN);
	}
	
	
	private BufferedReader toBufferedReader(String filePath){
		try {
			return new BufferedReader(new InputStreamReader(new FileInputStream(filePath), StandardCharsets.UTF_8));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	
	
	public ArrayList<int[]> tokenizeDocuments(
			String docsFilePath) {
		
		StringBuilder builder = new StringBuilder(2000);
		BufferedReader reader = toBufferedReader(docsFilePath);
		
		PennTreebankTokenizer tokenizer = PennTreebankTokenizer.getInstance();
		
		ArrayList<int[]> intsDocuments = new ArrayList<int[]>(100);
		for (;;) {
			String docText = nextDocText(reader, builder);
			if (docText == null) {
				break;
			}
			TIntArrayList list = new TIntArrayList(500);
			String[] words = tokenizer.split(docText);
			int[] wordIndices = lookupWordIndices(Arrays.asList(words));
			list.add(wordIndices);
			intsDocuments.add(list.toNativeArray());
		}
		try {
			reader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return intsDocuments;
	}
	
	
	/**
	 * iterate a text file with format like "resources/example-docs.txt"
	 * @param reader
	 * @param builder
	 * @return next doc text
	 */
	private String nextDocText(BufferedReader reader, StringBuilder builder) {
		builder.setLength(0);
		try {
			String result = null;
			String line = null;
			while ((line = reader.readLine()) != null) {
				line = line.trim();
				if (line.startsWith("<doc") && line.endsWith(">")){
					while ((line = reader.readLine()) != null) {
						boolean isDocEnd = line.startsWith("</doc>");
						if (isDocEnd == false){
							builder.append(line);
							builder.append('\n');
						} else{
							break;
						}
					}
					result = builder.toString();
					return result;
				}
			}
			return result;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	
	public int[] lookupWordIndices(List<String> wordStrings) {
		final int length = wordStrings.size();
		int[] result = new int[length];
		if (length == 0){
			return result;
		}
		for (int i = 0; i < length; ++i) {
			String word = wordStrings.get(i);
			
			int wordIndex = dictionary.lookupIndex(word.toLowerCase());
			if (wordIndex < 0){
				wordIndex = unknownTokenIndex;
			}
			result[i] = wordIndex;
		}
		return result;
	}
	
	public int[] lookupWordIndices(String[] wordStrings) {
		final int length = wordStrings.length;
		int[] result = new int[length];
		if (length == 0){
			return result;
		}
		for (int i = 0; i < length; ++i) {
			String word = wordStrings[i];
			int wordIndex = dictionary.lookupIndex(word.toLowerCase());
			if (wordIndex < 0){
				wordIndex = unknownTokenIndex;
			}
			result[i] = wordIndex;
		}
		return result;
	}
	
	/**
	 * a method for debugging word indices
	 * @param wordIndices
	 */
	public void printWordIndicesAsText(int[] wordIndices) {
		dictionary.printWordIndicesAsText(wordIndices);
	}
	
	
	/**
	 * @param enText : a normal English text 
	 * @return word indices of the enText according to the tokenizer
	 */
	protected int[] lookupTextWordsIndices(String enText) {
		PennTreebankTokenizer tokenizer = PennTreebankTokenizer.getInstance();
		String[] words = tokenizer.split(enText);
		return lookupWordIndices(words);
	}
	
	
	
	
	
}
