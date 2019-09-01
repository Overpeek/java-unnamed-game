package utility;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.InflaterOutputStream;

import org.lwjgl.system.Platform;

public class SaveManager {
	
	public static final Platform PLATFORM = Platform.get();
	public static Path SAVELOCATION_WINDOWS;
	public static Path SAVELOCATION_LINUX;
	public static Path SAVELOCATION_MACOSX;
	
	static public Path init(String game_name) {
		SAVELOCATION_WINDOWS = Paths.get(System.getProperty("user.home"), "Documents", "My Games", "Overpeek Engine", game_name);
		SAVELOCATION_LINUX = Paths.get(System.getProperty("user.home"), "Overpeek Engine", game_name); // TODO: FIX
		SAVELOCATION_MACOSX = Paths.get(System.getProperty("user.home"), "Overpeek Engine", game_name); // TODO: FIX
		
		return SAVELOCATION_WINDOWS;
	}
	
	static public String getSaveLocation(String... path) {
		
		switch (PLATFORM) {
		case WINDOWS:
			return Paths.get(SAVELOCATION_WINDOWS.toString(), path).toString();
		case LINUX:
			return Paths.get(SAVELOCATION_WINDOWS.toString(), path).toString();
		case MACOSX:
			return Paths.get(SAVELOCATION_WINDOWS.toString(), path).toString();

		default:
			return null;
		}
		
	}
	
	static public void saveData(ByteBuffer data, String... path) {
		String locationString = getSaveLocation(path);
		DataIO.writeByteBuffer(locationString, data);
	}
	
	static public ByteBuffer loadData(String... path) throws IOException {
		String locationString = getSaveLocation(path);
		return DataIO.readByteBuffer(locationString);
	}
	
	public static byte[] compress(byte[] input) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        OutputStream out = new DeflaterOutputStream(baos);
        out.write(input);
        out.close();
        
        return baos.toByteArray();
    }

    public static byte[] decompress(byte[] input) throws IOException {
    	ByteArrayOutputStream baos = new ByteArrayOutputStream();
        OutputStream out = new InflaterOutputStream(baos);
        out.write(input);
        out.close();
        
        return baos.toByteArray();
    }
	
}
