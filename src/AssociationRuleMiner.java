import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Map.Entry;

import util.KeyComparator;
import util.PropertyReader;

public class AssociationRuleMiner {

	private static double MIN_SUPPORT;
	private static double MIN_CONFIDENCE;
	private static String ITEMSEP;

	public static void main(String[] args) {
		ThreadMXBean mbean = ManagementFactory.getThreadMXBean();
		long time1 = mbean.getCurrentThreadCpuTime();
		Properties prop = PropertyReader.getConfig();
		ITEMSEP = (String) prop.get("ITEMSEP");
		MIN_SUPPORT = Double.parseDouble((String) prop.get("MIN_SUPPORT"));
		MIN_CONFIDENCE = Double.parseDouble((String) prop.get("MIN_CONFIDENCE"));
		String frequentItemSetsFile = "frequentItemSets_" + MIN_SUPPORT + ".txt";
		AssociationRuleMiner associationRuleMiner = new AssociationRuleMiner();
		ArrayList<HashMap<String, Double>> freqItemSets = associationRuleMiner.retrieveFrequentItemSets(frequentItemSetsFile);
		List<HashMap<String, Double>> associationRules = associationRuleMiner.generateAssociationRules(freqItemSets);

		File outFile = new File("associationRules_" + MIN_SUPPORT + "_" + MIN_CONFIDENCE + ".txt");
		FileWriter fw = null;
		BufferedWriter bw = null;
		try {
			fw = new FileWriter(outFile.getAbsoluteFile());
			bw = new BufferedWriter(fw);
			for (HashMap<String, Double> assocRuleMap : associationRules) {
				if (assocRuleMap != null) {
					for (Entry<String, Double> entry : assocRuleMap.entrySet()) {
						String key = entry.getKey();
						Double value = entry.getValue();
						bw.write(key);
						bw.write("\t");
						bw.write(value.toString());
						bw.newLine();
					}
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

	private ArrayList<HashMap<String, Double>> retrieveFrequentItemSets(String frequentItemSetsFile) {
		ArrayList<HashMap<String, Double>> freqItemSets = new ArrayList<HashMap<String, Double>>();

		File inFile = new File(frequentItemSetsFile);
		FileReader fr = null;
		BufferedReader br = null;
		try {
			fr = new FileReader(inFile.getAbsoluteFile());
			br = new BufferedReader(fr);
			String line;
			int itemSetMax = 0;
			HashMap<String, Double> freqItems = null;
			while ((line = br.readLine()) != null) {
				String[] contents = line.split("\t");
				String key = contents[0];
				String value = contents[1];
				Double support = Double.parseDouble(value);
				if (key.split(ITEMSEP).length > itemSetMax) {
					if (freqItems != null) {
						freqItemSets.add(freqItems);
					}
					freqItems = new HashMap<String, Double>();
					itemSetMax++;
				}
				freqItems.put(key, support);
			}
			freqItemSets.add(freqItems);
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

		return freqItemSets;
	}

	@SuppressWarnings("unchecked")
	private List<HashMap<String, Double>> generateAssociationRules(ArrayList<HashMap<String, Double>> freqItemSets) {
		List<HashMap<String, Double>> associationRules;
		HashMap<String, Double>[] associationRulesArray = (HashMap<String, Double>[]) new HashMap<?, ?>[freqItemSets.size() - 1];
		HashMap<String, Double> associationRuleMap = null;
		int processedCount = 0;
		for (int i = freqItemSets.size() - 1; i >= 1; i--) {

			HashMap<String, Double> freq = freqItemSets.get(i);
			for (Entry<String, Double> entry : freq.entrySet()) {
				processedCount++;
				ArrayDeque<String> unprocessed = new ArrayDeque<String>();
				HashSet<String> infreqRHS = new HashSet<String>();
				String key = entry.getKey();
				Double supportXY = entry.getValue();
				System.out.println("Key: " + key + " Support: " + supportXY);

				unprocessed.add(key + "->" + "");
				while (unprocessed.size() > 0) {
					String toProcess = unprocessed.pop();
					String[] ruleArray = toProcess.split("->");
					String initLHS = ruleArray[0];
					String initRHS = null;
					// get confidence for rule
					if (ruleArray.length == 2) {
						int lhsLength = initLHS.split(ITEMSEP).length;
						HashMap<String, Double> freqLHS = freqItemSets.get(lhsLength - 1);
						double supportX = freqLHS.get(initLHS);
						double confidence = supportXY / supportX;
						if (confidence > MIN_CONFIDENCE) {
							associationRuleMap = associationRulesArray[lhsLength - 1];
							if (associationRuleMap == null) {
								associationRuleMap = new HashMap<String, Double>();
							}
							associationRuleMap.put(toProcess, confidence);
							associationRulesArray[lhsLength - 1] = associationRuleMap;
						} else {
							infreqRHS.add(initRHS);
						}
						initRHS = ruleArray[1];
					}

					if (infreqRHS.contains(initRHS))
						continue;
					// Add rule in tree to queue
					String[] initLHSitems = initLHS.split(ITEMSEP);
					String[] initRHSitems = null;
					if (initRHS != null)
						initRHSitems = initRHS.split(ITEMSEP);
					for (int j = 0; initLHSitems.length > 1 && j < initLHSitems.length; j++) {
						StringBuffer sblhs = new StringBuffer();
						StringBuffer sbrhs = new StringBuffer();
						String[] newLHSitems = new String[initLHSitems.length - 1];
						String[] newRHSitems = null;
						if (initRHSitems != null) {
							newRHSitems = new String[initRHSitems.length + 1];
							for (int k = 0; k < initRHSitems.length; k++) {
								newRHSitems[k] = initRHSitems[k];
							}
						} else {
							newRHSitems = new String[1];
						}
						for (int l = 0, lhscount = 0; l < initLHSitems.length; l++) {
							if (l != j) {
								//								System.out.println("here: " + initLHSitems[l]);
								newLHSitems[lhscount] = initLHSitems[l];
								lhscount++;
							} else {
								newRHSitems[newRHSitems.length - 1] = initLHSitems[l];
							}
						}
						// LHS sorting not required
						//						Arrays.sort(newLHSitems);
						//						Arrays.sort(newRHSitems);
						Arrays.sort(newRHSitems, new KeyComparator());
						for (int m = 0; m < newLHSitems.length; m++) {
							sblhs.append(newLHSitems[m]);
							sblhs.append(ITEMSEP);
						}
						sblhs.deleteCharAt(sblhs.length() - 1);

						for (int n = 0; n < newRHSitems.length; n++) {
							sbrhs.append(newRHSitems[n]);
							sbrhs.append(ITEMSEP);
						}
						sbrhs.deleteCharAt(sbrhs.length() - 1);

						String lhs = sblhs.toString();
						String rhs = sbrhs.toString();
						System.out.println("\t LHS: " + lhs + " \t RHS: " + rhs);
						String newKey = lhs + "->" + rhs;
						if (!unprocessed.contains(newKey)) {
							unprocessed.add(newKey);
						}
					}
				}
			}
		}
		System.out.println("Processed " + processedCount + " frequent itemsets");
		associationRules = Arrays.asList(associationRulesArray);
		return associationRules;
	}
}
