package utility;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.CharBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

import org.json.JSONObject;

public class DataIO {
	
	public static ByteBuffer readResourceFile(String path) {
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
	
	public static byte[] readByte(String path) {
		Path fileLocation = Paths.get(path);
		byte[] data;
		try {
			data = Files.readAllBytes(fileLocation);
			return data;
		} catch (IOException e) {
			Logger.warn("Couldn't read file \"" + path + "\"");
		}
		
		return null;
	}
	
	public static ByteBuffer readByteBuffer(String path) {
		byte[] bytes = readByte(path);
		if (bytes != null) ByteBuffer.wrap(bytes);
		return null;
	}
	
	public static void writeByte(String path, byte[] data) {
		Path fileLocation = Paths.get(path);
		try {
			Files.write(fileLocation, data);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
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
	
	public static int[] readInt(String path) {
		byte bytes[] = readByte(path);
		if (bytes == null) return null;
		IntBuffer intBuf = ByteBuffer.wrap(bytes).order(ByteOrder.BIG_ENDIAN).asIntBuffer();
		int[] array = new int[intBuf.remaining()];
		intBuf.get(array);
		
		return array;
	}
	
	public static float[] readFloat(String path) {
		byte bytes[] = readByte(path);
		if (bytes == null) return null;
		FloatBuffer intBuf = ByteBuffer.wrap(bytes).order(ByteOrder.BIG_ENDIAN).asFloatBuffer();
		float[] array = new float[intBuf.remaining()];
		intBuf.get(array);
		
		return array;
	}
	
	public static char[] readChar(String path) {
		byte bytes[] = readByte(path);
		if (bytes == null) return null;
		CharBuffer charBuf = ByteBuffer.wrap(bytes).order(ByteOrder.BIG_ENDIAN).asCharBuffer();
		char[] array = new char[charBuf.remaining()];
		charBuf.get(array);
		
		return array;
	}
	
	public static JSONObject loadJSONObject(String path) {
		String source = StandardCharsets.UTF_8.decode(DataIO.readResourceFile(path)).toString();
		return new JSONObject(source);
	}
	
}
