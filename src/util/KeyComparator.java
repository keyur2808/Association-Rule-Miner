package util;

import java.util.Comparator;

public class KeyComparator implements Comparator<String> {

	public int compare(String key1, String key2) {
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
}
