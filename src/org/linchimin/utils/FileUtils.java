package org.linchimin.utils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import org.linchimin.utils.ObjectSerializer.CompressionAlgoithm;


/**
 * @author Lin Chi-Min
 *
 */
public class FileUtils {
	
	static ObjectOutputStream newObjectOutputStream(String filePath, CompressionAlgoithm algoithm) {
		return newObjectOutputStream(filePath, algoithm, false);
	}
	
	
	private static ObjectOutputStream newObjectOutputStream(String filePath, CompressionAlgoithm algoithm, boolean append) {
		try {
			FileOutputStream fos = new FileOutputStream(filePath, append);
			BufferedOutputStream bos = new BufferedOutputStream(fos);
			ObjectOutputStream oos = null;
			switch (algoithm) {
			case NONE:  	oos = new ObjectOutputStream(bos);  break;
			case GZ:		oos = new ObjectOutputStream(new GZIPOutputStream(bos)); 	break;
			default: 		break;
			}
			return oos;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	static ObjectInputStream newObjectInputStream(String filePath, CompressionAlgoithm algoithm) {
		try {
			FileInputStream fis = new FileInputStream(filePath);
			BufferedInputStream bis = new BufferedInputStream(fis);
			ObjectInputStream ois = null;
			switch (algoithm) {
			case NONE:  	ois = new ObjectInputStream(bis);  break;
			case GZ:		ois = new ObjectInputStream(new GZIPInputStream(bis)); 	break;
			default: 		break;
			}
			return ois;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	
	/**
	 * the file with name file name and the class should be in the same package
	 * @param fileName
	 * @param clazz
	 */
	private static InputStream newInputStream(Class<?> clazz, String fileName){
		try {
			return clazz.getResourceAsStream(fileName);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	
	public static ArrayList<String> getFileListRecursively(String dir){
		return getFileListRecursively(new File(dir));
	}
	
	protected static ArrayList<String> getFileListRecursively(File dir) {
		if (dir.isDirectory() == false)
			return null;
		ArrayList<String> list = new ArrayList<String>();
		File[] listFiles = dir.listFiles();
		for (int i = 0; i < listFiles.length; i++) {
			File file = listFiles[i];
			String thePath = file.getAbsolutePath();
			if (file.isDirectory()) {
				ArrayList<String> subFileList = getFileListRecursively(file);
				list.addAll(subFileList);
			} else {
				list.add(thePath);
			}
		}
		return list;
	}

	/**
	 * @param like FileUtils.class
	 * @return the absolute path of directory where the java file locates 
	 */
	public static <T> String getJavaFileAbsoluteDirectory(Class<T> clazz){
		
		URL location = clazz.getResource('/'+clazz.getName().replace('.', '/') + ".class");
		String result = location.getPath().substring(1);
		result = result.replaceFirst("/bin/", "/src/");
		int end = result.lastIndexOf('/');
		result = result.substring(0, end + 1);
		return result;
	}
	
	
	public static InputStream readInputStream(Class<?> clazz, String fileName){
		return newInputStream(clazz, fileName);
	}
	
	public static ArrayList<String> readLines(BufferedReader reader){
		try {
			ArrayList<String> result = new ArrayList<>(100);
			for (;;) {
				String line = reader.readLine();
				if (line == null)
					break;
				result.add(line);
			}
			reader.close();
			return result;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
		
}
