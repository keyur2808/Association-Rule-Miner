package util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;

public class PropertyReader {

	public static Properties getConfig() {
		Properties prop = new Properties();
		File inFile = new File("conf/config.properties");
		FileReader fr = null;
		try {
			fr = new FileReader(inFile.getAbsoluteFile());
			prop.load(fr);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return prop;
	}

}
