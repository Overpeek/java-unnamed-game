package utility;

import java.io.IOException;
import java.nio.file.*;

public class DataIO {

	public static byte[] read(String path) {
		Path fileLocation = Paths.get(path);
		byte[] data;
		try {
			data = Files.readAllBytes(fileLocation);
			return data;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return null;
	}
	
	public static void write(String path, byte[] data) {
		Path fileLocation = Paths.get(path);
		try {
			Files.write(fileLocation, data);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
}
