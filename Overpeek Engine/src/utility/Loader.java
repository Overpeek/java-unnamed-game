package utility;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

public class Loader {
	
	private static Loader loader = new Loader();

	public static InputStream loadClassRes(String name) throws NullPointerException {
		return loader.getClass().getResourceAsStream(name);
	}

	public static InputStream loadRes(String path) throws FileNotFoundException {
		return new FileInputStream(path);
	}
	
}
