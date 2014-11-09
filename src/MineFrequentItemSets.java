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
import java.util.HashSet;
import java.util.Properties;
import java.util.Map.Entry;
import java.util.Set;

import util.PropertyReader;

public class MineFrequentItemSets {

	private static double MIN_SUPPORT;
	private static String ITEMSEP;
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
		MIN_SUPPORT = Double.parseDouble((String) prop.get("MIN_SUPPORT"));
		String singletonFile = "singletons_" + MIN_SUPPORT + ".txt";
		String dataSetFile = "dataset.txt";
		MineFrequentItemSets mineFrequentItemSets = new MineFrequentItemSets();
		ArrayList<HashMap<String, Double>> freqItemSets = new ArrayList<HashMap<String, Double>>();
		HashMap<String, Double> singletons = mineFrequentItemSets.retrieveSingletons(singletonFile);
		mineFrequentItemSets.retrieveDataSet(dataSetFile);
		freqItemSets.add(singletons);

		for (int i = 0; i < itemCount && i < freqItemSets.size(); i++) {
			System.out.println(freqItemSets.size() + " # Itemsets: " + freqItemSets.get(i).size());
			HashMap<String, Double> freqItemSet = mineFrequentItemSets.generateFreqItemset(freqItemSets.get(i), i + 1);
			if (freqItemSet.size() > 0)
				freqItemSets.add(freqItemSet);
			else
				break;
		}
		int i = 0;
		for (HashMap<String, Double> freq : freqItemSets) {
			i++;
			System.out.println("Level " + i + ": #: " + freq.size());
		}

		File outFile = new File("frequentItemSets_" + MIN_SUPPORT + ".txt");
		FileWriter fw = null;
		BufferedWriter bw = null;
		try {
			fw = new FileWriter(outFile.getAbsoluteFile());
			bw = new BufferedWriter(fw);
			for (HashMap<String, Double> freq : freqItemSets) {
				for (Entry<String, Double> entry : freq.entrySet()) {
					String key = entry.getKey();
					Double value = entry.getValue();
					bw.write(key);
					bw.write("\t");
					bw.write(value.toString());
					bw.newLine();
				}
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

	private HashMap<String, Double> retrieveSingletons(String inFileString) {
		HashMap<String, Double> singletons = new HashMap<String, Double>();

		File inFile = new File(inFileString);
		FileReader fr = null;
		BufferedReader br = null;
		try {
			fr = new FileReader(inFile.getAbsoluteFile());
			br = new BufferedReader(fr);
			String line;
			while ((line = br.readLine()) != null) {
				String[] contents = line.split("\t");
				singletons.put(contents[0], Double.parseDouble(contents[1]));
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

		return singletons;
	}

	@SuppressWarnings("unchecked")
	private HashMap<String, Double> generateFreqItemset(HashMap<String, Double> freqItemSet, int currentLevel) {
		HashMap<String, Double> newFreqItemSet = new HashMap<String, Double>();
		HashSet<String> candidateSet = new HashSet<String>();
		// Generate candidate set at currentLevel + 1
		Set<Entry<String, Double>> entrySet = freqItemSet.entrySet();
		Object[] entryArray = entrySet.toArray();

		for (int i = 0; i < entrySet.size(); i++) {
			for (int j = i + 1; j < entrySet.size(); j++) {
				String p = ((Entry<String, Double>) entryArray[i]).getKey();
				String q = ((Entry<String, Double>) entryArray[j]).getKey();
				if (p.indexOf(ITEMSEP) != -1 && q.indexOf(ITEMSEP) != -1) {
					String pinit = p.substring(0, p.lastIndexOf(ITEMSEP));
					String qinit = q.substring(0, q.lastIndexOf(ITEMSEP));
					if (pinit.equals(qinit)) {
						String pfinal = p.substring(p.lastIndexOf(ITEMSEP) + 1);
						String qfinal = q.substring(q.lastIndexOf(ITEMSEP) + 1);
						String key;
						if (compare(pfinal, qfinal) < 0) {
							key = p + ITEMSEP + qfinal;
						} else {
							key = q + ITEMSEP + pfinal;
						}
						candidateSet.add(key);
					}
				} else {
					String key;
					if (compare(p, q) < 0) {
						key = p + ITEMSEP + q;
					} else {
						key = q + ITEMSEP + p;
					}
					candidateSet.add(key);
				}
			}
		}

		HashSet<String> newCandidateSet = pruneItemSets(candidateSet, freqItemSet);

		for (String key : newCandidateSet) {
			outerLoop: for (String transaction : dataset) {
				boolean contains = false;
				for (String item : key.split(ITEMSEP)) {
					if (transaction.contains(ITEMSEP + item + ITEMSEP) || transaction.startsWith(item) || transaction.endsWith(item)) {
						contains = true;
					} else
						continue outerLoop;
				}
				if (contains) {
					double value = 0;
					if (newFreqItemSet.containsKey(key)) {
						value = newFreqItemSet.get(key);
					}
					value++;
					newFreqItemSet.put(key, value);
				}
			}
			if (newFreqItemSet.containsKey(key)) {
				double support = newFreqItemSet.get(key) / (double) itemCount;
				if (support >= MIN_SUPPORT) {
					newFreqItemSet.put(key, support);
					System.out.println("Retained key: " + key + " Support: " + support);
				} else {
					newFreqItemSet.remove(key);
				}
			}
		}

		return newFreqItemSet;
	}

	private HashSet<String> pruneItemSets(HashSet<String> candidateSet, HashMap<String, Double> freqItemSet) {
		HashSet<String> newCandidateSet = new HashSet<String>();
		for (String key : candidateSet) {
			String[] arr = key.split(ITEMSEP);
			boolean contains = false;
			for (int i = 0; i < arr.length; i++) {
				StringBuffer sb = new StringBuffer();
				for (int j = 0; j < arr.length; j++) {
					if (j != i) {
						sb.append(arr[j]);
						sb.append(ITEMSEP);
					}
				}
				sb.deleteCharAt(sb.length() - 1);
				if (freqItemSet.containsKey(sb.toString())) {
					contains = true;
				} else {
					contains = false;
					break;
				}
			}
			if (contains) {
				newCandidateSet.add(key);
			}
		}
		return candidateSet;

	}

	private int compare(String key1, String key2) {
		String[] arr1 = key1.split("_");
		String[] arr2 = key2.split("_");
		try {
			int int1 = Integer.parseInt(arr1[0]);
			int int2 = Integer.parseInt(arr2[0]);

			if (int1 < int2)
				return -1;
			else if (int1 > int2)
				return 1;
			else {
				return arr1[1].compareTo(arr2[1]);
			}
		} catch (NumberFormatException e) {
			return key1.compareTo(key2);
		}
	}

	private void retrieveDataSet(String inFileString) {

		File inFile = new File(inFileString);
		FileReader fr = null;
		BufferedReader br = null;
		itemCount = 0;
		try {
			fr = new FileReader(inFile.getAbsoluteFile());
			br = new BufferedReader(fr);
			String line;
			while ((line = br.readLine()) != null) {
				dataset.add(line);
				itemCount++;
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
	}
}
