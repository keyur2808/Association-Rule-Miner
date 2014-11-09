import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Properties;

import util.PropertyReader;

public class ReadIn {

	private static double MIN_SUPPORT;
	private static String ITEMSEP;
	private static String TRAIN_DATA_FILE;
	private ArrayList<String> dataset = new ArrayList<String>();
	private static int itemCount = 0;

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		ThreadMXBean mbean = ManagementFactory.getThreadMXBean();
		long time1 = mbean.getCurrentThreadCpuTime();
		Properties prop = PropertyReader.getConfig();
		ITEMSEP = (String) prop.get("ITEMSEP");
		TRAIN_DATA_FILE = (String) prop.get("TRAIN_DATA_FILE");
		MIN_SUPPORT = Double.parseDouble((String) prop.get("MIN_SUPPORT"));
		ReadIn readIn = new ReadIn();
		HashMap<String, Double> singletons = readIn.readInput("trainData/" + TRAIN_DATA_FILE);

		File outFile = new File("singletons_" + MIN_SUPPORT + ".txt");
		FileWriter fw = null;
		BufferedWriter bw = null;
		try {
			fw = new FileWriter(outFile.getAbsoluteFile());
			bw = new BufferedWriter(fw);
			for (Entry<String, Double> entry : singletons.entrySet()) {
				String key = entry.getKey();
				Double value = entry.getValue();
				bw.write(key);
				bw.write("\t");
				bw.write(value.toString());
				bw.newLine();
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				bw.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		outFile = new File("dataset.txt");
		fw = null;
		bw = null;
		try {
			fw = new FileWriter(outFile.getAbsoluteFile());
			bw = new BufferedWriter(fw);
			for (String entry : readIn.dataset) {
				bw.write(entry);
				bw.newLine();
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				bw.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		long time2 = mbean.getCurrentThreadCpuTime();
		double timeInSecs = (double) (time2 - time1) * Math.pow(10, -9);
		System.out.println("Execution time in secs: " + timeInSecs);
	}

	private HashMap<String, Double> readInput(String inFileString) {
		HashMap<String, Integer> candidates = new HashMap<String, Integer>();
		HashMap<String, Double> singletons = new HashMap<String, Double>();

		File inFile = new File(inFileString);
		FileReader fr = null;
		BufferedReader br = null;
		itemCount = 0;
		try {
			fr = new FileReader(inFile.getAbsoluteFile());
			br = new BufferedReader(fr);
			String line;
			while ((line = br.readLine()) != null) {
				StringBuffer dataBuffer = new StringBuffer();
				String[] contents = line.split("\t");
				itemCount++;
				for (int i = 1; i < contents.length; i++) {
					String key = "";
					if (i == contents.length - 1) {
						key = contents[i];
					} else {
						key = i + "_" + contents[i].toUpperCase();
					}
					dataBuffer.append(key);
					dataBuffer.append(ITEMSEP);
					int value = 0;
					if (candidates.containsKey(key)) {
						value = candidates.get(key);
					}
					value++;
					candidates.put(key, value);
				}
				dataset.add(dataBuffer.deleteCharAt(dataBuffer.length() - 1).toString());
			}

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				br.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		System.out.println("itemCount: " + itemCount);
		for (Entry<String, Integer> entry : candidates.entrySet()) {
			String key = entry.getKey();
			Integer value = entry.getValue();
			double support = (double) value.intValue() / (double) itemCount;
			if (support >= MIN_SUPPORT) {
				singletons.put(key, support);
			} else {
				System.out.println("Removed key: " + key + " Support: " + support);
			}
		}

		return singletons;
	}
}
