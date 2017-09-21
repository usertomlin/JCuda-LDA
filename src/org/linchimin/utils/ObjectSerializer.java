package org.linchimin.utils;

import java.io.File;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;


/**
 * 
 * @author Lin Chi-Min
 *
 */
public class ObjectSerializer {

	public enum CompressionAlgoithm {
		GZ,
		NONE
	}
	
	private static boolean createDirectoryByPath(String path) {
		int slashIndex = path.lastIndexOf('/');
		if (slashIndex == -1)
			return false;
		String directory = path.substring(0, slashIndex);
		return createDirectory(directory, true);
	}

	private static boolean createDirectory(String directory, boolean recursively) {	
		File dir = new File(directory);
		if (dir.exists() == false) {
			boolean success = recursively ? dir.mkdirs() : dir.mkdir();
			if (success) {
				System.err.println("FileUtils.createDirectory(): created directory " + directory);
			}
			return success;
		}
		return true;
	}
	
	public static <T extends Serializable> boolean serialize(T object, String filePath) {
		return serialize(object, filePath, CompressionAlgoithm.NONE);
	}
	
	
	private static <T extends Serializable> boolean serialize(T object, String filePath, CompressionAlgoithm algoithm) {
		if (algoithm == null)
			algoithm = CompressionAlgoithm.NONE;
		createDirectoryByPath(filePath);
		
		try {
			ObjectOutputStream oos = FileUtils.newObjectOutputStream(filePath, algoithm);
			oos.writeObject(object);
			oos.close();
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	public static <T extends Serializable> T deserialize(String filePath) {
		return deserialize(filePath, CompressionAlgoithm.NONE);
	}
	
	
	
	@SuppressWarnings("unchecked")
	private static <T extends Serializable> T deserialize(String filePath, CompressionAlgoithm algoithm) {
		if (algoithm == null)
			algoithm = CompressionAlgoithm.NONE;
		try {
			ObjectInputStream ois = FileUtils.newObjectInputStream(filePath, algoithm);
			T object = (T) ois.readObject();
			ois.close();
			return object;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		} 
	}
	
	public static void main(String[] args) { 
//		Document doc1 = Document.getDummyDocument();
//		System.out.println(doc1);
//		CompressionAlgoithm algoithm = CompressionAlgoithm.XZ;
//		serialize(doc1, "D:/a.zip", algoithm);
//		Document doc2 = deserialize("D:/a.zip", algoithm);
//		System.out.println(doc2);
//		System.out.println("doc1.equals(doc2) = " + doc1.equals(doc2));
		
		long startTime = System.currentTimeMillis();

		deserialize("D:\\Corpora\\paragraph2vec\\paragraphVectors-float.ser");

		System.out.println("Time taken to run this part: " + (System.currentTimeMillis() - startTime));
	}


}
