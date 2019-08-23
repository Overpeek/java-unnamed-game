package utility;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.CharBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.InflaterOutputStream;

import org.json.JSONException;
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
	
	public static byte[] readByte(String path) throws IOException {
		Path fileLocation = Paths.get(path);
		return Files.readAllBytes(fileLocation);
	}
	
	public static ByteBuffer readByteBuffer(String path) throws IOException {
		byte[] bytes = readByte(path);
		if (bytes == null) return null;
		
		return ByteBuffer.wrap(bytes);
	}
	
	public static void writeByte(String path, byte[] data) throws IOException {
		Path fileLocation = Paths.get(path);
		fileLocation.toFile().getParentFile().mkdirs();
		Files.write(fileLocation, data, StandardOpenOption.CREATE);
	}

	public static void writeByteBuffer(String path, ByteBuffer data) throws IOException {
		writeByte(path, data.array());
	}
	
	public static void writeInt(String path, int[] data) throws IOException {
		ByteBuffer byteBuf = ByteBuffer.allocateDirect(data.length * 4);
		byteBuf.asIntBuffer().put(data);
		writeByte(path, byteBuf.array());
	}
	
	public static void writeChar(String path, char[] data) throws IOException {
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
	
	public static JSONObject loadJSONObject(String path) throws JSONException {
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
