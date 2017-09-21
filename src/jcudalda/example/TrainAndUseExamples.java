
package jcudalda.example;

import org.linchimin.jcudalda.LDAGPUTrainer;
import org.linchimin.jcudalda.LDAModel;
import org.linchimin.utils.StringUtils;

/**
 * <pre>
 * Examples for 
 * 1. how to train an LDA model with this library
 * 2. how to check whether a trained model is OK by viewing whether the major topic words of same topics are topically related
 * 3. how to use a trained LDA model for distributed representation of texts with library
 * </pre>
 * 
 * @author Lin Chi-Min (v381654729@gmail.com)
 *
 */
class TrainAndUseExamples {

	/**
	 * Train a 30-topic LDA model with 200k Wikipedia documents (138M words) for 50 iterations 
	 * within 5.5 mins on a Windows NB with NVIDIA GTX 950M and i7-4750HQ
	 */
	public static void trainLDAExample() {
		
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
		
		/**
		 * A list of vocabulary words. Used for the mapping of words and word indices for the created ints corpus.
		 */
		String vocabularyFilePath = "resources/enVocabulary-45k.txt";
		
		
		/**
		 * 1. A file path the stores the output trained LDA model, the phis matrix, the probabilities of words given topics 
		 * 2. The current "resources/phis-wiki.ser" is trained on the first 200k documents of 'enwiki-20160601-pages-articles.xml.bz2' wikipedia documents.
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
		
	}
	

	
	/**
	 * <pre>
	 * Check whether the trained model is OK or the training process was run normally
	 * by viewing whether the major topic words of same topics are topically related.
	 * 
	 * For example, topic words like these below seem reasonable and OK.  
	 * If not, tune the number of document 'M', the number of topics 'K', 
	 * vocabulary words and their size in vocabularyFilePath,
	 * and number of iterations 'numIterations' for training, 
	 * and retrain an LDA. 
	 * 
	 * Topic = 22: 
	 * 	hezbollah, bin, jun, kabul, condos, 
	 *	delhi, wen, aloe, macau, khan, 
	 *	kathmandu, china, bangladesh, afghans, karachi, 
	 *	musharraf, lien, bhutto, gandhi, metroid, 
	 *	sheikh, imap, cpa, chen, lebanon, 
	 *	multifamily, zardari, limewire, jihad, chengdu, 
	 *	uscis, aipac, chinese, mailboxes, jiang, 
	 *	goodling, morsi, kuala, pakistani, ase, 
	 *	pakistanis, lumpur, sharif, qaeda, islamist, 
	 *	sentry, abdullah, sommelier, syria, cease-fire, 
	 *	wu, ration, afghan, laden, zhou,
	 * 
	 * Topic = 23: 
	 *	symptoms, patients, disease, vitamin, viagra, 
	 *	cholesterol, skin, muscles, infection, tissue, 
	 *	muscle, infections, dose, clinical, medications, 
	 *	treatment, diagnosis, cation, autism, disorders, 
	 *	acupuncture, antibiotics, therapy, syndrome, inflammation, 
	 *	eggs, viruses, virus, tissues, hormone, 
	 *	thyroid, immune, liver, dysfunction, hormones, 
	 *	nutrients, treatments, respiratory, patient, animals, 
	 *	constipation, genetic, tumors, brain, antibiotic, 
	 *	estrogen, redness, diazepam, urine, diet, 
	 *	fda, edmunds.com, erectile, glands, disorder, 
	 *	dietary, bladder, birds, neurological, penicillin, 
	 *	</pre>
	 */
	public static void checkTrainedLDAModel() {

		String vocabularyFilePath = "resources/enVocabulary-45k.txt";
		String resultPhisSerPath = "resources/phis-wiki.ser";
		
		/**load a trained LDA model 
		 */
		LDAModel model = new LDAModel(resultPhisSerPath, vocabularyFilePath);
		
		/**
		 * print some major topic words and check whether the major words of topics are topically related 
		 * to verify if the trained LDA model is OK
		 */
		model.checkTopicsWords(100);
	}
	
	
	/**
	 * <pre>
	 * An example for using a trained LDA model for efficient parallel distributed representation for multiple texts.
	 * 
	 * After reviewing topic words of topics 0 to 29, it can be guessed that 
	 * enText1, exText2, and enText3 are highly related to topic 4, topic 23, and topic 9 respectively,
	 * and the inferred topic vectors exactly match this expectation.
	 * </pre>
	 */
	public static void useLDAForDistributedRepresentationExample() {
		

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
		
		
	}
	

	
	public static void main(String[] args) {
		
		/**
		 * Train a 30-topic LDA model with 200k Wikipedia documents (138M words) for 50 iterations 
		 * within 5.5 mins on a Windows NB with NVIDIA GTX 950M and i7-4750HQ
		 */
		trainLDAExample();
		
		/**
		 *  Check whether the trained model is OK or the training process was run normally
		 * by viewing whether the major topic words of same topics are topically related.
		 */
		checkTrainedLDAModel();
		
		/**
		 * An example for using a trained LDA model for efficient parallel distributed representation for multiple texts.
		 */
		useLDAForDistributedRepresentationExample();
		
	}

	

	
}
