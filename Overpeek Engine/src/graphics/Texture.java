package graphics;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

import javax.imageio.ImageIO;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.GL40;
import org.lwjgl.opengl.GL42;

import utility.Loader;
import utility.Logger;

public class Texture {
	
    private int texture;
    private int type;
    private int width;
    private int height;
    private int depth;
    
    
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
    
    public int getWidth() {
    	return width;
    }
    
    public int getHeight() {
    	return height;
    }

    public void bind() {
		GL11.glBindTexture(type, texture);
    }
    
    public static void unbind() {
		GL11.glBindTexture(GL30.GL_TEXTURE_2D, 0);
    }
    
    public static int GL_2D = GL13.GL_TEXTURE_2D;
    public static int GL_2D_ARRAY = GL30.GL_TEXTURE_2D_ARRAY;
    public static int GL_3D = GL13.GL_TEXTURE_3D;
    public static int GL_3D_ARRAY = GL13.GL_TEXTURE_2D;
    
    /**
     * Types: (GL_2D, GL_2D_ARRAY, GL_3D, GL_CUBE)
     * */
    public static Texture empty2d(int width, int height, int type) {
    	Texture returned = new Texture();
    	returned.type = type;
    	returned.texture = GL11.glGenTextures();
    	returned.width = width;
    	returned.height = height;
    	returned.depth = 1;
		GL11.glBindTexture(type, returned.texture);

		GL11.glTexParameteri(type, GL11.GL_TEXTURE_WRAP_S, GL13.GL_CLAMP_TO_EDGE);
		GL11.glTexParameteri(type, GL11.GL_TEXTURE_WRAP_T, GL13.GL_CLAMP_TO_EDGE);
		GL11.glTexParameteri(type, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
		GL11.glTexParameteri(type, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);

		GL11.glTexImage2D(type, 0, GL11.GL_RGBA, width, height, 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, (ByteBuffer)null);
		return returned;
    }
    
    /**
     * Types: (GL_2D, GL_2D_ARRAY, GL_3D, GL_CUBE)
     * */
    public static Texture empty3d(int width, int height, int depth, int type) {
    	Texture returned = new Texture();
    	returned.type = type;
    	returned.texture = GL11.glGenTextures();
    	returned.width = width;
    	returned.height = height;
    	returned.depth = depth;
		GL11.glBindTexture(type, returned.texture);

		GL11.glTexParameteri(type, GL11.GL_TEXTURE_WRAP_S, GL12.GL_CLAMP_TO_EDGE);
		GL11.glTexParameteri(type, GL11.GL_TEXTURE_WRAP_T, GL12.GL_CLAMP_TO_EDGE);
		GL11.glTexParameteri(type, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
		GL11.glTexParameteri(type, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);

		GL42.glTexStorage3D(type, 1, GL11.GL_RGBA8, width, height, depth);
		//GL12.glTexSubImage3D(type, 0, 0, 0, 0, width, height, depth, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, (ByteBuffer)null);
		//GL30.glTexImage3D(type, 1, GL11.GL_RGB, width, height, depth, 0, GL11.GL_RGB, GL11.GL_UNSIGNED_BYTE, (ByteBuffer)null);
		return returned;
    }
    
    /**
     * Data must be in RGBA as unsigned bytes
     * Size: (width * height * 4(rgba))
     * */
    public void setData2D(ByteBuffer data) {
		GL11.glTexSubImage2D(type, 0, 0, 0, width, height, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, data);
    }
    
    /**
     * Data must be in RGBA as unsigned bytes
     * Size: (width * height * depth * 4(rgba))
     * */
    public void setData3D(ByteBuffer data) {
		GL12.glTexSubImage3D(type, 0, 0, 0, 0, width, height, depth, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, data);
    }
    
    public void delete() {
    	GL11.glDeleteTextures(texture);
    }
    
    public static Texture loadBufferedImage(BufferedImage img) {
    	Texture returned = new Texture();
    	returned.type = GL30.GL_TEXTURE_2D;
    
    	//Buffer for image
		ByteBuffer data = BufferUtils.createByteBuffer(img.getWidth() * img.getHeight()  * 4);
		for (int x = 0; x < img.getWidth(); x++) {
			for (int y = 0; y < img.getHeight(); y++) {
				
				Color c = new Color(img.getRGB(x, y), true);
				
				data.put(4 * (x + y * img.getWidth()) + 0, (byte) c.getRed());
				data.put(4 * (x + y * img.getWidth()) + 1, (byte) c.getGreen());
				data.put(4 * (x + y * img.getWidth()) + 2, (byte) c.getBlue());
				data.put(4 * (x + y * img.getWidth()) + 3, (byte) c.getAlpha());
			}
		}
		data.flip();
    	
		//Opengl
    	returned.texture = GL11.glGenTextures();
    	returned.width = img.getWidth();
    	returned.height = img.getHeight();
    	returned.depth = 1;
    	GL11.glBindTexture(GL11.GL_TEXTURE_2D, returned.texture);
    	GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL11.GL_REPEAT);
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL11.GL_REPEAT);
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
		GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA, img.getWidth(), img.getHeight(), 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, data);
		//GL30.glGenerateMipmap(GL11.GL_TEXTURE_2D);

		data.clear();
		
    	
    	return returned;
    }
    
    public static Texture loadTextureSingle(String path) {
		try {
			InputStream is = Loader.loadRes(path);
			BufferedImage img = ImageIO.read(is);
			
			return Texture.loadBufferedImage(img);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
    }
    
    /**
     * Cube texture data is static and cannot be changed
     * */
    public static Texture loadCubeMap(int r, String paths[]) {
    	Texture returned = new Texture();
    	returned.type = GL13.GL_TEXTURE_CUBE_MAP;
    	
    	try {

	    	returned.texture = GL11.glGenTextures();
	    	GL11.glBindTexture(returned.type, returned.texture);
	    	
    		for (int i = 0; i < paths.length; i++) {
    			InputStream is = Loader.loadRes(paths[i]);
    			BufferedImage img = ImageIO.read(is);
    			
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
    			data.flip();
                GL11.glTexImage2D(GL13.GL_TEXTURE_CUBE_MAP_POSITIVE_X + i, 
                        0, GL11.GL_RGBA, img.getWidth(), img.getHeight(), 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, data);
    			data.clear();
			}

        	returned.width = 0;
        	returned.height = 0;
        	returned.depth = 0;
    		GL11.glTexParameteri(GL13.GL_TEXTURE_CUBE_MAP, GL11.GL_TEXTURE_MIN_FILTER, GL13.GL_LINEAR);
    		GL11.glTexParameteri(GL13.GL_TEXTURE_CUBE_MAP, GL11.GL_TEXTURE_MAG_FILTER, GL13.GL_LINEAR);
    		GL11.glTexParameteri(GL13.GL_TEXTURE_CUBE_MAP, GL11.GL_TEXTURE_WRAP_S, GL13.GL_CLAMP_TO_EDGE);
    		GL11.glTexParameteri(GL13.GL_TEXTURE_CUBE_MAP, GL11.GL_TEXTURE_WRAP_T, GL13.GL_CLAMP_TO_EDGE);
    		GL11.glTexParameteri(GL13.GL_TEXTURE_CUBE_MAP, GL13.GL_TEXTURE_WRAP_R, GL13.GL_CLAMP_TO_EDGE);
			
		} catch (IOException e) {
			e.printStackTrace();
		}
    	
    	return returned;
    }

    public static Texture loadTextureAtlas(int r, int rows, int cols, int count, String path) {
    	Texture returned = new Texture();
    	returned.type = GL30.GL_TEXTURE_2D_ARRAY;
    	
    	try {
			InputStream is = Loader.loadRes(path);
			BufferedImage img = ImageIO.read(is);
			
			int dataSize = 4 * r * r * 256;
			ByteBuffer data = ByteBuffer.allocateDirect(dataSize);
			for (int i = 0; i < count; i++) {
				for (int y = 0; y < r; y++) {
					for (int x = 0; x < r; x++) {
						int xInInput = (i % rows) * r + x;
						int yInInput = (int)(Math.floor(i / cols)) * r + y;

						Color c = new Color(img.getRGB(xInInput, yInInput), true);
						
						data.put(4 * (x + y * r + i * r * r) + 0, (byte)c.getRed());
						data.put(4 * (x + y * r + i * r * r) + 1, (byte)c.getGreen());
						data.put(4 * (x + y * r + i * r * r) + 2, (byte)c.getBlue());
						data.put(4 * (x + y * r + i * r * r) + 3, (byte)c.getAlpha());
					}
				}
			}
			data.flip();

			returned.texture = GL11.glGenTextures();
        	returned.width = r;
        	returned.height = r;
        	returned.depth = 256;
			GL11.glBindTexture(GL30.GL_TEXTURE_2D_ARRAY, returned.texture);

			GL11.glTexParameteri(GL30.GL_TEXTURE_2D_ARRAY, GL11.GL_TEXTURE_WRAP_S, GL12.GL_CLAMP_TO_EDGE);
			GL11.glTexParameteri(GL30.GL_TEXTURE_2D_ARRAY, GL11.GL_TEXTURE_WRAP_T, GL12.GL_CLAMP_TO_EDGE);
			GL11.glTexParameteri(GL30.GL_TEXTURE_2D_ARRAY, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
			GL11.glTexParameteri(GL30.GL_TEXTURE_2D_ARRAY, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);

			GL42.glTexStorage3D(GL30.GL_TEXTURE_2D_ARRAY, 1, GL11.GL_RGBA8, r, r, 256);
			GL12.glTexSubImage3D(GL30.GL_TEXTURE_2D_ARRAY, 0, 0, 0, 0, r, r, 256, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, data);
			
			data.clear();
			
		} catch (IOException e) {
			e.printStackTrace();
		}
    	
    	return returned;
    }
    
}
