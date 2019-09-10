package utility;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.CharBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;

import org.json.JSONException;
import org.json.JSONObject;

public class DataIO {
	
	public static String readTextFile(String path) throws IOException {
		//Load and compile
		StringBuilder text = new StringBuilder();
		InputStream is = Loader.loadRes(path);
		BufferedReader reader = new BufferedReader(new InputStreamReader(is));
		String line;
		
		while((line = reader.readLine()) != null) {
			text.append(line).append("\n");
		}
		
		reader.close();
		
		return text.toString();
	}
	
	public static String readTextFile(File file) throws IOException {
		//Load and compile
		StringBuilder text = new StringBuilder();
		FileInputStream fis = new FileInputStream(file);
		BufferedReader reader = new BufferedReader(new InputStreamReader(fis));
		String line;
		
		while((line = reader.readLine()) != null) {
			text.append(line).append("\n");
		}
		
		reader.close();
		
		return text.toString();
	}
	
	public static ByteBuffer readResourceFile(String path) throws FileNotFoundException {
		InputStream is = Loader.loadRes(path);
		ArrayList<Integer> allBytes = new ArrayList<Integer>();
		try {
			while (true) {
				int result = is.read();
				if (result == -1) break;
				allBytes.add(result);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		ByteBuffer databuffer = ByteBuffer.allocateDirect(allBytes.size());
		for (int i = 0; i < allBytes.size(); i++) {
			databuffer.put((byte)allBytes.get(i).intValue());
		}
		databuffer.flip();
		return databuffer;
	}
	
	public static byte[] readByte(String path) throws IOException {
		Path fileLocation = Paths.get(path);
		return Files.readAllBytes(fileLocation);
	}
	
	public static ByteBuffer readByteBuffer(String path) throws IOException {
		byte[] bytes = readByte(path);
		if (bytes == null) return null;
		
		return ByteBuffer.wrap(bytes);
	}
	
	public static void writeByte(String path, byte[] data) {
		Path fileLocation = Paths.get(path);
		fileLocation.toFile().getParentFile().mkdirs();
		try {
			Files.write(fileLocation, data, StandardOpenOption.CREATE);
		} catch (IOException e) {
			e.printStackTrace();
			Logger.error("This is a bug");
		}
	}

	public static void writeByteBuffer(String path, ByteBuffer data) {
		writeByte(path, data.array());
	}
	
	public static void writeInt(String path, int[] data) {
		ByteBuffer byteBuf = ByteBuffer.allocateDirect(data.length * 4);
		byteBuf.asIntBuffer().put(data);
		writeByte(path, byteBuf.array());
	}
	
	public static void writeChar(String path, char[] data) {
		ByteBuffer byteBuf = ByteBuffer.allocateDirect(data.length * 4);
		byteBuf.asCharBuffer().put(data);
		writeByte(path, byteBuf.array());
	}
	
	public static int[] readInt(String path) throws IOException {
		byte bytes[] = readByte(path);
		if (bytes == null) return null;
		
		IntBuffer intBuf = ByteBuffer.wrap(bytes).order(ByteOrder.BIG_ENDIAN).asIntBuffer();
		int[] array = new int[intBuf.remaining()];
		intBuf.get(array);
		
		return array;
	}
	
	public static float[] readFloat(String path) throws IOException {
		byte bytes[] = readByte(path);
		if (bytes == null) return null;
		
		FloatBuffer intBuf = ByteBuffer.wrap(bytes).order(ByteOrder.BIG_ENDIAN).asFloatBuffer();
		float[] array = new float[intBuf.remaining()];
		intBuf.get(array);
		
		return array;
	}
	
	public static char[] readChar(String path) throws IOException {
		byte bytes[] = readByte(path);
		if (bytes == null) return null;
		
		CharBuffer charBuf = ByteBuffer.wrap(bytes).order(ByteOrder.BIG_ENDIAN).asCharBuffer();
		char[] array = new char[charBuf.remaining()];
		charBuf.get(array);
		
		return array;
	}
	
	public static JSONObject loadJSONObject(String path) throws JSONException, FileNotFoundException {
		String source = StandardCharsets.UTF_8.decode(DataIO.readResourceFile(path)).toString();
		return new JSONObject(source);
	}
	
//	public static java.nio.Buffer readFileToBuffer(String path) {
//		byte[] bytes = readByte(path);
//		if (bytes == null) return null;
//		
//		return ByteBuffer.wrap(bytes);
//	}
//
//	public static void writeBufferToFile(String path, java.nio.Buffer data) {
//		writeByte(path, ((ByteBuffer(data))data).array());
//	}
	
}
