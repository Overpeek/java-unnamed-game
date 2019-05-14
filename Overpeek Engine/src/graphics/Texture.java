package graphics;

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

public class Texture {
	
    private int texture;
    private int type;
    
    
    public Texture() {}
    
    public void setId(int id) {
    	texture = id;
    }
    
    public void setType(int _type) {
    	type = _type;
    }
    
    public int getId() {
    	return texture;
    }
    
    public int getType() {
    	return type;
    }
    
    public void bind() {
		GL11.glBindTexture(type, texture);
    }
    
    public static void unbind() {
		GL11.glBindTexture(GL30.GL_TEXTURE_2D, 0);
    }
    
    public static Texture empty(int width, int height) {
    	Texture returned = new Texture();
    	returned.type = GL30.GL_TEXTURE_2D;
    	returned.texture = GL11.glGenTextures();
		GL11.glBindTexture(GL30.GL_TEXTURE_2D, returned.texture);

		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL12.GL_CLAMP_TO_EDGE);
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL12.GL_CLAMP_TO_EDGE);
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);

		GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGB, width, height, 0, GL11.GL_RGB, GL11.GL_FLOAT, (ByteBuffer)null);
		return returned;
    }
    
    
    
    public static Texture loadTextureSingle(String path) {
    	Texture returned = new Texture();
    	returned.type = GL30.GL_TEXTURE_2D;
    
    	//Load image
		try {
			BufferedImage img = ImageIO.read(new File(path));
	    	ByteBuffer data = ByteBuffer.allocateDirect(img.getWidth() * img.getHeight() * 4);
			for (int x = 0; x < img.getWidth(); x++) {
				for (int y = 0; y < img.getHeight(); y++) {
					
					Color c = new Color(img.getRGB(x, y), true);
					
					data.put(4 * (x + y * img.getWidth()) + 0, (byte)c.getRed());
					data.put(4 * (x + y * img.getWidth()) + 1, (byte)c.getGreen());
					data.put(4 * (x + y * img.getWidth()) + 2, (byte)c.getBlue());
					data.put(4 * (x + y * img.getWidth()) + 3, (byte)c.getAlpha());
				}
			}
			img.flush();
			data.flip();
	    	
			//Opengl
	    	returned.texture = GL11.glGenTextures();
	    	GL11.glBindTexture(GL11.GL_TEXTURE_2D, returned.texture);
	    	GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL11.GL_REPEAT);
			GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL11.GL_REPEAT);
			GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
			GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
			GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA, img.getWidth(), img.getHeight(), 0, GL11.GL_RGBA, GL11.GL_FLOAT, data);
			GL30.glGenerateMipmap(GL11.GL_TEXTURE_2D);

			data.clear();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
    	
    	return returned;
    }

    public static Texture loadTextureAtlas(int r, int rows, int cols, String path) {
    	Texture returned = new Texture();
    	returned.type = GL30.GL_TEXTURE_2D_ARRAY;
    	
    	try {
			BufferedImage img = ImageIO.read(new File(path));
			
			int dataSize = 4 * r * r * 256;
			float data[] = new float[dataSize];
			for (int i = 0; i < 256; i++) {
				for (int y = 0; y < r; y++) {
					for (int x = 0; x < r; x++) {
						int xInInput = (i % rows) * r + x;
						int yInInput = (int)(Math.floor(i / cols)) * r + y;

						Color c = new Color(img.getRGB(xInInput, yInInput), true);
						
						data[4 * (x + y * r + i * r * r) + 0] = c.getRed() / 255.0f;
						data[4 * (x + y * r + i * r * r) + 1] = c.getGreen() / 255.0f;
						data[4 * (x + y * r + i * r * r) + 2] = c.getBlue() / 255.0f;
						data[4 * (x + y * r + i * r * r) + 3] = c.getAlpha() / 255.0f;
					}
				}
			}

			returned.texture = GL11.glGenTextures();
			GL11.glBindTexture(GL30.GL_TEXTURE_2D_ARRAY, returned.texture);

			GL11.glTexParameteri(GL30.GL_TEXTURE_2D_ARRAY, GL11.GL_TEXTURE_WRAP_S, GL12.GL_CLAMP_TO_EDGE);
			GL11.glTexParameteri(GL30.GL_TEXTURE_2D_ARRAY, GL11.GL_TEXTURE_WRAP_T, GL12.GL_CLAMP_TO_EDGE);
			GL11.glTexParameteri(GL30.GL_TEXTURE_2D_ARRAY, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
			GL11.glTexParameteri(GL30.GL_TEXTURE_2D_ARRAY, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);

			GL42.glTexStorage3D(GL30.GL_TEXTURE_2D_ARRAY, 1, GL11.GL_RGBA8, r, r, 256);
			GL12.glTexSubImage3D(GL30.GL_TEXTURE_2D_ARRAY, 0, 0, 0, 0, r, r, 256, GL11.GL_RGBA, GL11.GL_FLOAT, data);
			
			data = new float[0];
			
			img.getClass();
			
		} catch (IOException e) {
			e.printStackTrace();
		}
    	
    	return returned;
    }
    
}
