import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.util.Arrays;

public class Test {
	public static enum Expr1 {
		RULE, BODY, HEAD
	};

	public static enum Expr2 {
		ANY, NONE
	};

	public static void main(String args[]) {
		ThreadMXBean mbean = ManagementFactory.getThreadMXBean();
		long time1 = mbean.getCurrentThreadCpuTime();
		/*
				File inFile = new File("association-rule-test-data.txt");
				FileReader fr = null;
				BufferedReader br = null;
				try {
					fr = new FileReader(inFile.getAbsoluteFile());
					br = new BufferedReader(fr);
					String line;
					int count = 0;
					while ((line = br.readLine()) != null) {
						String[] contents = line.split("\t");
						if (!contents[13].toUpperCase().equals("DOWN"))
							continue;
						if (!contents[72].toUpperCase().equals("UP"))
							continue;
						if (!contents[97].toUpperCase().equals("DOWN"))
							continue;
						count++;
					}
					System.out.println(count);
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}*/
		//		String line = "ipioisad AND sdsd AND dsd OR Sada";
		String line = "HEAD HAS 1 OF 1_UP,2_DOWN AND RULE >= 3";
		//		String line = "HEAD HAS 1 OF 1_UP,2_DOWN OR RULE >= 3";
		//		String line = "HEAD HAS 1 OF 1_UP,2_DOWN";
		//		String line = "RULE >= 3";
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
				String[] itemArray = expr3String.split(",");

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
		}
		long time2 = mbean.getCurrentThreadCpuTime();
		System.out.println(time1);
		System.out.println(time2);
		System.out.println(time2 - time1);
	}
}
