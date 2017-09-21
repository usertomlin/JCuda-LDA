package org.linchimin.jcudalda;

import java.util.ArrayList;
import java.util.Iterator;

import org.linchimin.utils.FileUtils;
import org.linchimin.utils.ObjectSerializer;

/**
 * 
 * @author Lin Chi-Min (v381654729@gmail.com)
 *
 */
class CorpusIntsIterator implements Iterator<ArrayList<int[]>>{
	
	private final int numDocumentsInOneBatch;
	
	private ArrayList<String> textsFilePaths;
	
	private boolean isTerminated;
	private ArrayList<int[]> currentDocsIndices;
	
	private int currentFileIndex; 	// index at textsFilePaths
	private int currentDocIndex;	// index at currentDocsIndices
	
	
	public CorpusIntsIterator(String intsCorpusDirectory, int numDocumentsInOneBatch) {
		this.numDocumentsInOneBatch = numDocumentsInOneBatch;
		this.textsFilePaths = FileUtils.getFileListRecursively(intsCorpusDirectory);
		this.isTerminated = (textsFilePaths.size() == 0);
		this.currentFileIndex = 0;
		if (isTerminated){
			this.currentDocsIndices = null;	
		} else{
			this.currentDocsIndices = ObjectSerializer.deserialize(textsFilePaths.get(0)); 
		}
		
	}
	

	@Override
	public boolean hasNext() {
		return isTerminated == false;
	}


	@Override
	public ArrayList<int[]> next() {
		
		ArrayList<int[]> miniBatchDocs = new ArrayList<int[]>(numDocumentsInOneBatch);
		
		while (miniBatchDocs.size() < numDocumentsInOneBatch) {
			
			int[] docIndices = currentDocsIndices.get(currentDocIndex++);
			miniBatchDocs.add(docIndices);
			
			if (currentDocIndex == currentDocsIndices.size()) {
				if (currentFileIndex < textsFilePaths.size() - 1) {
					currentFileIndex ++ ;
					currentDocIndex = 0;
					currentDocsIndices = ObjectSerializer.deserialize(textsFilePaths.get(currentFileIndex)); 
				} else {
					isTerminated = true;
					break;
				}
			}
		}
		
		return miniBatchDocs;
	}
	
	@Override
	public void remove() {
		
	}




	
}
