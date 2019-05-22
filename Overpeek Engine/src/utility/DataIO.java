package utility;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.CharBuffer;
import java.nio.IntBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class DataIO {
	
	public static byte[] readByte(String path) {
		Path fileLocation = Paths.get(path);
		byte[] data;
		try {
			data = Files.readAllBytes(fileLocation);
			return data;
		} catch (IOException e) {
			e.printStackTrace();
		}
		
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
		IntBuffer intBuf = ByteBuffer.wrap(readByte(path)).order(ByteOrder.BIG_ENDIAN).asIntBuffer();
		int[] array = new int[intBuf.remaining()];
		intBuf.get(array);
		
		return array;
	}
	
	public static char[] readChar(String path) {
		CharBuffer charBuf = ByteBuffer.wrap(readByte(path)).order(ByteOrder.BIG_ENDIAN).asCharBuffer();
		char[] array = new char[charBuf.remaining()];
		charBuf.get(array);
		
		return array;
	}
	
}
