package utility;

import java.io.InputStream;

public class Loader {
	
	private static Loader loader = new Loader();

	public static InputStream loadRes(String name) {
		return loader.getClass().getResourceAsStream(name);
	}
	
}
