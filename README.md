# JCuda-LDA

JCuda-LDA is a Java library for LDA topic modeling with JCuda. It implements an uncollapsed Gibbs sampler that enables high speed training with a large data size on GPU. JCuda-LDA is implemented with Java & JCuda as a pure Java library for maintainability. 

See TrainAndUseExamples.java in package 'jcudalda.example' for an example of training a 30-topic LDA model with 200k Wikipedia documents (138 million words) for 50 iterations, which takes within 5.5 minutes (and around 6.5 seconds per iteration) on a Windows NB with NVIDIA GTX 950M and i7-4750HQ.


## Environment 

1. Supports NVIDIA graphic cards, Cuda 8.0.44, and Java 7 or above, on 64 bit Windows 7, 8, and 10. 

Feel free to leave issues if there are any Cuda configuration problems.

2. Use Maven to build project, or add the jar files in the 'libs' folder to build path. 


## Usage


Train an LDA model: 

```java

		/**number of topics
		 */
		int K = 30;					
		
		/**number of training documents
		 */
		int M = 200000;				
		
		/**number of iterations for training
		 */
		int numIterations = 50;		
		
		/**
		 * number of documents per mini-batch during;
		 * the larger the faster training speed.
		 */
		int numDocumentsInOneMiniBatch = 50000;
		
		/**
		 * a folder containing a list of serializable files, or an 'ints corpus',  
		 * each of which is a serialized ArrayList<int[]> object where each int[] contains word indices of a document.
		 * Before invoking 'trainWithIntsCorpus' to train, create an ints corpus in 'intsCorpusDirectory' first. 
		 */
		String intsCorpusDirectory = "D:/Corpora/wiki/ints-enwiki-45k"; 
		
		/**A list of vocabulary words. Used for the mapping of words and word indices for the created ints corpus.
		 */
		String vocabularyFilePath = "resources/enVocabulary-45k.txt";
		
		
		/**
		 * a file path the stores the output trained LDA model, the phis matrix, 
		 * The "resources/phis-wiki.ser" is trained on the first 200k documents of 'enwiki-20160601-pages-articles.xml.bz2' wikipedia documents.
		 */
		String resultPhisSerPath = "resources/phis-wiki.ser";
		
		boolean trainWithIntsCorpus = true;
		
		
		LDAGPUTrainer trainer = new LDAGPUTrainer(vocabularyFilePath, K, M);
		
		/**
		 * 1. See 'ProcessIntsDocumentsExample.java' for how to process an ints corpus and saved to 'intsCorpusDirectory'
		 * 
		 * 2. For example, suppose
		 * ArrayList<int[]> docs = ObjectSerializer.deserialize("D:/Corpora/wiki/ints-enwiki-45k/1.ser");
		 * If docs.size() is 10000, it contains 10000 documents.
		 * And if docs.get(0) is {1, 3, 100, 30, 27, -1, 53, 82, ...}, the document contains these word indices.
		 * 
		 * 
		 * 3. recommended to use 'trainWithIntsCorpus' to train, which is by far faster than 'trainWithTextsCorpus'
		 */ 
		if (trainWithIntsCorpus) {
			trainer.trainWithIntsCorpus(intsCorpusDirectory, resultPhisSerPath, numIterations, numDocumentsInOneMiniBatch);
		}
		
		/**
		 * 1. supports training with a folder of many text files containing documents with format like 'example-docs.txt'
		 * 2. not recommended to use this method to train, which is by far slower than 'trainWithIntsCorpus'
		 */
		else {
			String textsCorpusDirectory = "D:/Corpora/wiki/enwiki";
			trainer.trainWithTextsCorpus(textsCorpusDirectory, resultPhisSerPath, numIterations, numDocumentsInOneMiniBatch);
		}
		
		
``` 


Use a trained LDA model for efficient parallel distributed representation for multiple texts:


```java

		/**
		 * A list of vocabulary words. Used for the mapping of words and word indices 
		 */
		String vocabularyFilePath = "resources/enVocabulary-45k.txt";
		
		/**
		 * 1. a file the stores a trained LDA model, the phis matrix, the probabilities of words given topics 
		 * 2. the current "resources/phis-wiki.ser" is trained on the first 200k documents of 'enwiki-20160601-pages-articles.xml.bz2' wikipedia documents.
		 */
		String phisSerPath = "resources/phis-wiki.ser";
		
		/**
		 * load a trained LDA model
		 */
		LDAModel model = new LDAModel(phisSerPath, vocabularyFilePath);
		
		/**
		 * the sample text files
		 */
		String enText1 = "Computer software, or simply software, is a part of a computer system that consists of data or computer instructions, in contrast to the physical hardware from which the system is built. In computer science and software engineering, computer software is all information processed by computer systems, programs and data. Computer software includes computer programs, libraries and related non-executable data, such as online documentation or digital media. Computer hardware and software require each other and neither can be realistically used on its own.";
		String enText2 = "ACE (angiotensin converting enzyme) inhibitors-Angiotensin converting enzyme inhibitors are used to treat high blood pressure. They cause the blood vessels to relax and become larger and, as a result, blood pressure is lowered. When blood pressure is reduced, the heart has an easier time pumping blood. This is especially beneficial when the heart is failing. ACE inhibitors also cause the process of hypertensive- and diabetes-related kidney diseases to slow down and prevent early deaths associated with high blood pressure. ACE inhibitors cannot be taken during pregnancy since they may cause birth defects. Generic ACE inhibitors are available.";
		String enText3 = "A guitarist (or a guitar player) is a person who plays the guitar. Guitarists may play a variety of guitar family instruments such as classical guitars, acoustic guitars, electric guitars, and bass guitars. Some guitarists accompany themselves on the guitar by singing or playing the harmonica.";
		
		/**
		 * parallel inference of topic vectors for the input texts
		 */
		float[][] topics = model.inferTopics(enText1, enText2, enText3);

		StringUtils.print(topics[0]);
		StringUtils.print(topics[1]);
		StringUtils.print(topics[2]);


```



See TrainAndUseExamples.java and ProcessIntsDocumentsExample.java in package 'jcudalda.example' for more details. 



