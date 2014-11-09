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

public class TemplatedAssociationRuleMiner {
	public static enum Expr1 {
		RULE, BODY, HEAD
	};

	public static enum Expr2 {
		ANY, NONE
	};

	private static double MIN_SUPPORT;
	private static double MIN_CONFIDENCE;
	private static String ITEMSEP;
	private static String TEMPLATE_FILE;

	private ArrayList<Conditions> conditionList = new ArrayList<Conditions>();
	private List<String> conjunctionList;

	public static void main(String[] args) {
		ThreadMXBean mbean = ManagementFactory.getThreadMXBean();
		long time1 = mbean.getCurrentThreadCpuTime();
		Properties prop = PropertyReader.getConfig();
		ITEMSEP = (String) prop.get("ITEMSEP");
		MIN_SUPPORT = Double.parseDouble((String) prop.get("MIN_SUPPORT"));
		MIN_CONFIDENCE = Double.parseDouble((String) prop.get("MIN_CONFIDENCE"));
		TEMPLATE_FILE = (String) prop.get("TEMPLATE_FILE");
		String frequentItemSetsFile = "frequentItemSets_" + MIN_SUPPORT + ".txt";
		int fileNo = Integer.parseInt(TEMPLATE_FILE.split("_")[1].split("\\.")[0]);
		TemplatedAssociationRuleMiner templatedAssociationRuleMiner = new TemplatedAssociationRuleMiner();
		templatedAssociationRuleMiner.readTemplate("templates/" + TEMPLATE_FILE);

		ArrayList<HashMap<String, Double>> freqItemSets = templatedAssociationRuleMiner.retrieveFrequentItemSets(frequentItemSetsFile);
		List<HashMap<String, Double>> associationRules = templatedAssociationRuleMiner.generateAssociationRules(freqItemSets);

		File outFile = new File("associationRules_" + MIN_SUPPORT + "_" + MIN_CONFIDENCE + "template_" + fileNo + ".txt");
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

	private void readTemplate(String templateFile) {
		File inFile = new File(templateFile);
		FileReader fr = null;
		BufferedReader br = null;

		try {
			fr = new FileReader(inFile.getAbsoluteFile());
			br = new BufferedReader(fr);
			String line;
			while ((line = br.readLine()) != null) {
				String[] templateArray = line.split("AND|OR");
				String[] conjunctionArray = null;
				if (templateArray.length > 1) {
					conjunctionArray = new String[templateArray.length - 1];
					for (int i = 0; i < templateArray.length - 1; i++) {
						int beginIndex = line.indexOf(templateArray[i]) + templateArray[i].length();
						int endIndex = line.indexOf(templateArray[i + 1]);
						String conjunction = line.substring(beginIndex, endIndex);
						conjunctionArray[i] = conjunction;
						templateArray[i] = templateArray[i].trim();
					}
					templateArray[templateArray.length - 1] = templateArray[templateArray.length - 1].trim();
				}
				conjunctionList = Arrays.asList(conjunctionArray);
				for (String template : templateArray) {
					Conditions conditions = new Conditions();
					// Template 1
					if (template.contains("HAS")) {
						conditions.setTemplate1(true);
						int hasIndex = template.indexOf("HAS");
						int ofIndex = template.indexOf("OF");
						String expr1String = template.substring(0, hasIndex - 1);
						String expr2String = template.substring(hasIndex + 4, ofIndex - 1);
						String expr3String = template.substring(ofIndex + 3);
						String[] itemArray = expr3String.split(ITEMSEP);

						Expr1 expr1 = Expr1.valueOf(expr1String);
						Expr2 expr2 = null;
						try {
							expr2 = Expr2.valueOf(expr2String);
						} catch (IllegalArgumentException e) {

						}
						switch (expr1) {
						case HEAD: {
							if (expr2 != null) {
								switch (expr2) {
								case ANY: {
									conditions.setItems(Arrays.asList(itemArray));
									break;
								}
								case NONE: {
									conditions.setNotItems(Arrays.asList(itemArray));
									break;
								}
								}
							} else {
								Integer headSizeOfSomeItems = Integer.parseInt(expr2String);
								conditions.setSizeOfSomeItems(headSizeOfSomeItems);
								conditions.setSomeItems(Arrays.asList(itemArray));
							}
							conditions.setPart("HEAD");
							break;
						}
						case BODY: {
							if (expr2 != null) {
								switch (expr2) {
								case ANY: {
									conditions.setItems(Arrays.asList(itemArray));
									break;
								}
								case NONE: {
									conditions.setNotItems(Arrays.asList(itemArray));
									break;
								}
								}
							} else {
								Integer bodySizeOfSomeItems = Integer.parseInt(expr2String);
								conditions.setSizeOfSomeItems(bodySizeOfSomeItems);
								conditions.setSomeItems(Arrays.asList(itemArray));
							}
							conditions.setPart("BODY");
							break;
						}
						case RULE: {
							if (expr2 != null) {
								switch (expr2) {
								case ANY: {
									conditions.setItems(Arrays.asList(itemArray));
									break;
								}
								case NONE: {
									conditions.setNotItems(Arrays.asList(itemArray));
									break;
								}
								}
							} else {
								Integer ruleSizeOfSomeItems = Integer.parseInt(expr2String);
								conditions.setSizeOfSomeItems(ruleSizeOfSomeItems);
								conditions.setSomeItems(Arrays.asList(itemArray));
							}
							conditions.setPart("RULE");
							break;
						}
						}
					}
					// Template 2
					else {
						conditions.setTemplate1(false);
						int geIndex = template.indexOf(">=");
						String expr1String = template.substring(0, geIndex - 1);
						String expr2String = template.substring(geIndex + 3);
						Expr1 expr1 = Expr1.valueOf(expr1String);

						switch (expr1) {
						case HEAD: {
							Integer headSize = Integer.parseInt(expr2String);
							conditions.setSize(headSize);
							conditions.setPart("HEAD");
							break;
						}
						case BODY: {
							Integer bodySize = Integer.parseInt(expr2String);
							conditions.setSize(bodySize);
							conditions.setPart("BODY");
							break;
						}
						case RULE: {
							Integer ruleSize = Integer.parseInt(expr2String);
							conditions.setSize(ruleSize);
							conditions.setPart("RULE");
							break;
						}
						}
					}
					conditionList.add(conditions);
				}
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
				//				System.out.println("Key: " + key + " Support: " + supportXY);

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
							if (matchTemplates(toProcess)) {
								associationRuleMap = associationRulesArray[lhsLength - 1];
								if (associationRuleMap == null) {
									associationRuleMap = new HashMap<String, Double>();
								}
								associationRuleMap.put(toProcess, confidence);
								associationRulesArray[lhsLength - 1] = associationRuleMap;
							}
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
						// Arrays.sort(newLHSitems);
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
						//						System.out.println("\t LHS: " + lhs + " \t RHS: " + rhs);
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

	private boolean matchTemplates(String key) {
		String[] keyArray = key.split("->");
		String body = keyArray[0];
		String head = keyArray[1];
		List<String> bodyItems = Arrays.asList(body.split(","));
		List<String> headItems = Arrays.asList(head.split(","));
		List<String> ruleItems = new ArrayList<String>();
		ruleItems.addAll(bodyItems);
		ruleItems.addAll(headItems);
		int bodyLength = bodyItems.size();
		int headLength = headItems.size();
		int ruleLength = bodyLength + headLength;
		ArrayList<Boolean> resultList = new ArrayList<Boolean>();
		for (Conditions c : conditionList) {
			String partToCheck = c.getPart();
			boolean template1 = c.isTemplate1();
			if (partToCheck.equals("HEAD")) {
				// handle template 1 for head
				if (template1) {
					if (c.getItems() != null) {
						boolean result = false;
						for (String condItem : c.getItems()) {
							if (headItems.contains(condItem)) {
								result = true;
								break;
							}
						}
						resultList.add(result);
					} else if (c.getNotItems() != null) {
						boolean result = true;
						for (String condItem : c.getNotItems()) {
							if (headItems.contains(condItem)) {
								result = false;
								break;
							}
						}
						resultList.add(result);
					} else if (c.getSomeItems() != null) {
						int numberToCheck = c.getSizeOfSomeItems();
						int numberMatched = 0;
						for (String condItem : c.getSomeItems()) {
							if (headItems.contains(condItem)) {
								numberMatched++;
							}
						}
						if (numberMatched == numberToCheck) {
							resultList.add(true);
						} else {
							resultList.add(false);
						}
					}
				}
				// handle template 2 for head
				else {
					if (headLength >= c.getSize())
						resultList.add(true);
					else
						resultList.add(false);
				}
			} else if (partToCheck.equals("BODY")) {
				// handle template 1 for body
				if (template1) {
					if (c.getItems() != null) {
						boolean result = false;
						for (String condItem : c.getItems()) {
							if (bodyItems.contains(condItem)) {
								result = true;
								break;
							}
						}
						resultList.add(result);
					} else if (c.getNotItems() != null) {
						boolean result = true;
						for (String condItem : c.getNotItems()) {
							if (bodyItems.contains(condItem)) {
								result = false;
								break;
							}
						}
						resultList.add(result);
					} else if (c.getSomeItems() != null) {
						int numberToCheck = c.getSizeOfSomeItems();
						int numberMatched = 0;
						for (String condItem : c.getSomeItems()) {
							if (bodyItems.contains(condItem)) {
								numberMatched++;
							}
						}
						if (numberMatched == numberToCheck) {
							resultList.add(true);
						} else {
							resultList.add(false);
						}
					}
				}
				// handle template 2 for body
				else {
					if (bodyLength >= c.getSize())
						resultList.add(true);
					else
						resultList.add(false);
				}
			} else if (partToCheck.equals("RULE")) {
				// handle template 1 for rule
				if (template1) {
					if (c.getItems() != null) {
						boolean result = false;
						for (String condItem : c.getItems()) {
							if (ruleItems.contains(condItem)) {
								result = true;
								break;
							}
						}
						resultList.add(result);
					} else if (c.getNotItems() != null) {
						boolean result = true;
						for (String condItem : c.getNotItems()) {
							if (ruleItems.contains(condItem)) {
								result = false;
								break;
							}
						}
						resultList.add(result);
					} else if (c.getSomeItems() != null) {
						int numberToCheck = c.getSizeOfSomeItems();
						int numberMatched = 0;
						for (String condItem : c.getSomeItems()) {
							if (ruleItems.contains(condItem)) {
								numberMatched++;
							}
						}
						if (numberMatched == numberToCheck) {
							resultList.add(true);
						} else {
							resultList.add(false);
						}
					}
				}
				// handle template 2 for rule
				else {
					if (ruleLength >= c.getSize())
						resultList.add(true);
					else
						resultList.add(false);
				}
			} else {
				System.out.println("Code error");
			}
		}
		boolean prevResult = resultList.get(0);
		if (resultList.size() > 1) {
			for (int index = 1; index < resultList.size(); index++) {
				String conjunction = conjunctionList.get(index - 1);
				boolean currentResult = resultList.get(index);
				if (conjunction.equals("AND")) {
					prevResult = prevResult && currentResult;
				} else if (conjunction.equals("OR")) {
					prevResult = prevResult || currentResult;
				}
			}
		}
		return prevResult;
	}
}
