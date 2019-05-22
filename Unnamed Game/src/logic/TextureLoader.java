package logic;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;

import javax.imageio.ImageIO;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.GL42;

import utility.Logger;

public class TextureLoader {

	public static class MultitextureReturnData {
		public int first_index;
		public int last_index;

		public MultitextureReturnData(int first, int last) {
			first_index = first;
			last_index = last;
		}
	}

	static private ByteBuffer textureBuffer;
	static private int r;
	static private int texture;
	static private int type;
	static private int current_layer;

	public static int getTextureType() {
		return type;
	}

	public static int getTextureId() {
		return texture;
	}

	/*
	 * 'wh' is width and height of all textures NOTE: All textures must be same size
	 */
	static public void init(int wh) {
		r = wh;
		textureBuffer = ByteBuffer.allocateDirect(0);
		Logger.out("Max supported texture count: " + GL30.GL_MAX_ARRAY_TEXTURE_LAYERS);
	}

	static public int load(String path) {

		//Texture loading
		BufferedImage img = null;
		try {
			img = ImageIO.read(new File(path));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Logger.out("Couldn't load file: " + path, Logger.type.ERROR);
		}
		
		//Texture data
		int layer = current_layer++;
		
		//Data processing
		byte[] data = new byte[r * r * 4];
		for (int x = 0; x < r; x++) {
			for (int y = 0; y < r; y++) {

				Color c = new Color(img.getRGB(x, y), true);

				data[4 * (x + y * r) + 0] = (byte) c.getRed();
				data[4 * (x + y * r) + 1] = (byte) c.getGreen();
				data[4 * (x + y * r) + 2] = (byte) c.getBlue();
				data[4 * (x + y * r) + 3] = (byte) c.getAlpha();
			}
		}
		
		textureBuffer.flip();
		textureBuffer = ByteBuffer.allocateDirect(textureBuffer.limit() + data.length).put(textureBuffer).put(data);

		return layer;
	}

	static public MultitextureReturnData loadMultitexture(String path) {

		//Texture loading
		BufferedImage img = null;
		try {
			img = ImageIO.read(new File(path));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Logger.out("Couldn't load file: " + path, Logger.type.ERROR);
		}
		
		//Texture data
		int rows = img.getHeight() / r;
		int columns = img.getWidth() / r;
		int layer = current_layer;
		current_layer += rows * columns;
		MultitextureReturnData returnData = new MultitextureReturnData(layer, layer + rows * columns - 1);
		
		//Data processing
		for (int i = 0; i < rows; i++) {
			for (int j = 0; j < columns; j++) {
				byte[] data = new byte[r * r * 4];
				for (int x = 0; x < r; x++) {
					for (int y = 0; y < r; y++) {

						Color c = new Color(img.getRGB(x + j * r, y + i * r), true);

						data[4 * (x + y * r) + 0] = (byte) c.getRed();
						data[4 * (x + y * r) + 1] = (byte) c.getGreen();
						data[4 * (x + y * r) + 2] = (byte) c.getBlue();
						data[4 * (x + y * r) + 3] = (byte) c.getAlpha();
					}
				}
				
				textureBuffer.flip();
				textureBuffer = ByteBuffer.allocateDirect(textureBuffer.limit() + data.length).put(textureBuffer).put(data);
			}

		}

		return returnData;
	}

	static public void finish() {
		int layers = current_layer;
		
		//ByteBuffer data = ByteBuffer.allocateDirect(layers * r * r * 4);
        //
		//for (int l = 0; l < layers; l++) {
		//	data.put(textureBuffers.get(l));
		//}
		//
		// Free temporary data
		//textureBuffers = new ArrayList<byte[]>();

		type = GL30.GL_TEXTURE_2D_ARRAY;
		texture = GL11.glGenTextures();
		GL11.glBindTexture(type, texture);

		GL11.glTexParameteri(type, GL11.GL_TEXTURE_WRAP_S, GL12.GL_CLAMP_TO_EDGE);
		GL11.glTexParameteri(type, GL11.GL_TEXTURE_WRAP_T, GL12.GL_CLAMP_TO_EDGE);
		GL11.glTexParameteri(type, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
		GL11.glTexParameteri(type, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);

		//Logger.out("r: " + r + " layers: " + layers);
		GL42.glTexStorage3D(type, 1, GL11.GL_RGBA8, r, r, layers);
		textureBuffer.flip();
		GL12.glTexSubImage3D(type, 0, 0, 0, 0, r, r, layers, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, textureBuffer);
	}

}
