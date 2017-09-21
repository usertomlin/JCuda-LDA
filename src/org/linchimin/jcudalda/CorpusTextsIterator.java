package org.linchimin.jcudalda;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.linchimin.common.Dictionary;
import org.linchimin.utils.FileUtils;

import smile.nlp.tokenizer.PennTreebankTokenizer;

/**
 * @author Lin Chi-Min (v381654729@gmail.com)
 */
class CorpusTextsIterator implements Iterator<ArrayList<int[]>>{

	private final int numDocumentsInOneBatch;
	
	private ArrayList<String> textsFilePaths;
	private int currentFileIndex;
	private boolean isTerminated;
	private BufferedReader currentReader;

	private Dictionary dictionary ;
	private int unknownTokenIndex;

	
	
	public CorpusTextsIterator(String textsCorpusDirectory, int numDocumentsInOneBatch, Dictionary dictionary) {
		this.numDocumentsInOneBatch = numDocumentsInOneBatch;
		this.textsFilePaths = FileUtils.getFileListRecursively(textsCorpusDirectory);
		this.isTerminated = (textsFilePaths.size() == 0);
		this.currentFileIndex = 0;
		this.currentReader = isTerminated ? null : toBufferedReader(textsFilePaths.get(0)); 
		this.dictionary = dictionary;
		
		String UNKNOWN_TOKEN = LDAGPUTrainer.UNKNOWN_TOKEN;
		if (dictionary.containsWord(UNKNOWN_TOKEN) == false){
			dictionary.add(UNKNOWN_TOKEN);
		}
		this.unknownTokenIndex = dictionary.lookupIndex(UNKNOWN_TOKEN);
	}
	
	private static BufferedReader toBufferedReader(String filePath){
		try {
			return new BufferedReader(new InputStreamReader(new FileInputStream(filePath), StandardCharsets.UTF_8));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public boolean hasNext() {
		return isTerminated == false;
	}


	@Override
	public ArrayList<int[]> next() {
		
		ArrayList<int[]> miniBatchDocs = new ArrayList<int[]>(numDocumentsInOneBatch);
		StringBuilder builder = new StringBuilder(300);
		PennTreebankTokenizer tokenizer = PennTreebankTokenizer.getInstance();
		
		while (miniBatchDocs.size() < numDocumentsInOneBatch){
			String docText = nextDocText(currentReader, builder);
			if (docText != null){
				String[] words = tokenizer.split(docText);
				int[] wordIndices = lookupWordIndices(Arrays.asList(words));
				miniBatchDocs.add(wordIndices);
			} else {
				closeReader(currentReader);
				if (currentFileIndex < textsFilePaths.size() - 1){
					currentFileIndex ++;
					currentReader = toBufferedReader(textsFilePaths.get(currentFileIndex));
					System.out.println("TextsCorpusIterator.next() Processed file " + textsFilePaths.get(currentFileIndex));
				} else {
					isTerminated = true;
					break;
				}
			}
		}
		
		return miniBatchDocs;
	}
	
	
	private void closeReader(BufferedReader reader) {
		try {
			reader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	
	private int[] lookupWordIndices(List<String> wordStrings) {
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

	@Override
	public void remove() {
		
	}



	
}
