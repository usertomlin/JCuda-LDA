package jcudalda.example;

import java.util.ArrayList;
import java.util.Arrays;

import org.linchimin.jcudalda.CorpusProcessor;
import org.linchimin.utils.FileUtils;
import org.linchimin.utils.ObjectSerializer;


/**
 * 
 * Example for how to process an 'ints corpus', a collection of many ArrayList<int[]> serializable files
 * The 'phis-wiki.ser' is trained on the first 200k documents of 'enwiki-20160601-pages-articles.xml.bz2' wikipedia documents.
 * 
 * @author Lin Chi-Min (v381654729@gmail.com)
 */
class ProcessIntsDocumentsExample {

	/**
	 * <pre>
	 * 1. This snippet is an example for creating many ArrayList<int[]> serialized files 
	 * from a folder (sourceTextsDir) of texts files with format like 'resources/example-docs.txt', 
	 * * (where each document is preceded by a line of "<doc>" with optional attributes and succeeded by a line of "</doc>")
	 * and then save the ArrayList<int[]> serializable files to a folder
	 * Each 'int[]' represents word indices of a document. 
	 * 
	 * 
	 * sourceTextsDir (each text file is like "resources/example-docs.txt")
	 * --wiki_00.txt
	 * --wiki_01.txt
	 * ...
	 * 
	 * intsCorpusDir
	 * --1.ser
	 * --2.ser
	 * --3.ser
	 * ...
	 * 
	 * The processing of ArrayList<int[]> files takes some time for a very large corpus but not very long. 
	 * The serialized documents are reusable not only for training LDAs with this library but also for other NLP tasks. 
	 * Paragraphs and sentences of a text can be separated by -2 and -1 respectively, 
	 * and this 'ints corpus' can greatly speed up IO for all different tasks.
	 * 
	 * The directory of the processed serialized files 'intsCorpusDir' is a parameter of the 'trainWithIntsCorpus' method in LDAGPUTrainer.java.
	 * See TrainAndUseExample.java for how to train an LDA model with ints documents, and
	 * see the two 'if (debug)' parts below to check whether the created ints corpus files are OK.  
	 * 
	 * 2. When the sourceTextsDir contains text files (like 00.txt, 01.txt, ...) of the format,  
	 * this snippet below can be directly used to create serialized files (like 1.ser, 2.ser, 3.ser, ...) of ints documents.
	 * Text tokenization of 'CorpusProcessor.java' is based on smile.nlp.tokenizer.PennTreebankTokenizer. 
	 * Alternatively, ints documents can also be created with a different vocabulary and any external tokenizer arbitrarily.
	 * 
	 * 
	 * 3. To acquire a large collection of raw English texts, one way is to download a wikipedia dump file like 
	 * "enwiki-20170820-pages-articles-multistream.xml.bz2" from https://dumps.wikimedia.org/enwiki/ 
	 * and process it to raw texts with a tool like WikiExtractor.py on https://github.com/attardi/wikiextractor , 
	 * or obtain raw texts from other sources like Common Crawl. 
	 * 
	 * 
	 * 
	 * </pre>
	 */
	public static void processIntsDocuments() {
		
		/**
		 * a list of vocabulary words, like "resources/enVocabulary-45k.txt"
		 * words in the vocabulary are mapped to corresponding word indices,
		 * and out-of-vocabulary words are mapped to an unknown word's word index
		 */
		String vocabularyFilePath = "resources/enVocabulary-45k.txt";
		
		/**
		 * a folder of texts files, with format like 'resources/example-docs.txt'
		 */
		String sourceTextsDir = "D:/Corpora/wiki/enwiki";
		
		/**
		 * the folder for generated .ser files of ArrayList<int[]> ints documents;
		 * Each 'int[]' represents word indices of a document
		 */
		String intsCorpusDir = "D:/Corpora/wiki/ints-enwiki-45k/";
		

		
		/**
		 * number of documents, or size of each generated serialized file of ArrayList<int[]>;
		 * a modestly large value like 10000 facilitates IO 
		 */
		int numDocumentsInOneBatch = 10000;
		
		/**
		 * enable debugging or not 
		 */
		final boolean debug = false;
		
		
		
		//////////////////////////////////////////////////////////
		
		/**
		 * a class that processes a file of texts like 'resources/example-docs.txt' to an ArrayList<int[]>
		 */
		CorpusProcessor corpusProcessor = new CorpusProcessor(vocabularyFilePath);
		
		ArrayList<String> filePaths = FileUtils.getFileListRecursively(sourceTextsDir);
		
		ArrayList<int[]> miniBatchDocuments = new ArrayList<int[]>(numDocumentsInOneBatch);
		
		
		int count = 0;
		for (int i = 0; i < filePaths.size(); i++) {
			String docsFilePath = filePaths.get(i);
			ArrayList<int[]> fileDocuments = corpusProcessor.tokenizeDocuments(docsFilePath);
			
			if (debug) {
				if (fileDocuments.size() > 0) {
					corpusProcessor.printWordIndicesAsText(fileDocuments.get(0));
				}
			}
			
			System.out.println("CorpusProcessor.processIntsDocuments() docsFilePath = " + docsFilePath);
			int currentSize = miniBatchDocuments.size();
			int remaining = numDocumentsInOneBatch - currentSize;
			if (remaining >= fileDocuments.size()){
				miniBatchDocuments.addAll(fileDocuments);
			} else {
				count++;
				miniBatchDocuments.addAll(fileDocuments.subList(0, remaining));
				ObjectSerializer.serialize(miniBatchDocuments, intsCorpusDir + count + ".ser");
				
				if (debug) {
					ArrayList<int[]> miniBatchDocuments2 = ObjectSerializer.deserialize(intsCorpusDir + count + ".ser");
					for (int j = 0; j < miniBatchDocuments.size(); j++) {
						int[] arr1 = miniBatchDocuments.get(j);
						int[] arr2 = miniBatchDocuments2.get(j);
						if (Arrays.equals(arr1, arr2) == false){
							throw new Error("Error in ProcessIntsDocumentsExample.processIntsDocuments()");
						}
					}
				}
				
				miniBatchDocuments.clear();
				miniBatchDocuments.addAll(fileDocuments.subList(remaining, fileDocuments.size()));
			}
		}
		
		System.out.println("System.exit(0) at LDAGPUTrainer.enclosing_method()");
		System.exit(0);
	}	
	
	


	public static void main(String[] args) throws Exception {
		processIntsDocuments();
	}
	
}
