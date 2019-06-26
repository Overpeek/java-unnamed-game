package graphics;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.GL42;

import utility.Debug;
import utility.Loader;
import utility.Logger;

public class GlyphTexture {

	public static class Glyph {

	    public final int width;
	    public final int height;
	    public final int textureId;

	    public Glyph(int width, int height, int id) {
	        this.width = width;
	        this.height = height;
	        textureId = id;
	    }

	}
    private Map<Character, Glyph> glyphs = new HashMap<>();
    private int maxHeight;
    private int resolution;
	private Texture texture;
	
	private GlyphTexture() {}
    
    public Texture getTexture() {
    	return texture;
    }
    
    public int getHeight() {
    	return maxHeight;
    }
    
    public Glyph getGlyphData(Character c) {
		return glyphs.get(c);
    }
    
    public int getResolution() {
    	return resolution;
    }
    
    public static GlyphTexture loadFont(String path, int resolution) {
    	Font font = null;
    	try {
    		InputStream is = Loader.loadRes(path);
    		font = Font.createFont(Font.TRUETYPE_FONT, is).deriveFont(Font.PLAIN, resolution);
		} catch (FontFormatException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
    	return loadFont(font);
    }
    
    public static GlyphTexture loadFont(Font font) {
    	//System.setProperty("awt.useSystemAAFontSettings","on");
    	//System.setProperty("Dswing.aatext","true");
    	GlyphTexture returned = new GlyphTexture();
    	returned.glyphs = new HashMap<>();
    	returned.texture = new Texture();
    	returned.texture.setType(GL30.GL_TEXTURE_2D_ARRAY);
    	returned.texture.setId(GL11.glGenTextures());;
    	returned.resolution = font.getSize();
    	
    	int imageWidth = 0;
    	int imageHeight = 0;

    	int index = 0;
    	for (int i = 32; i < 256; i++) {
    		index++;
    	    if (i == 127) continue;
    	    char c = (char) i;
    	    BufferedImage ch = createCharImage(font, c, true);
    	    imageWidth = Math.max(imageWidth, ch.getWidth());
    	    imageHeight = Math.max(imageHeight, ch.getHeight());
    	    
    	    /* Create glyph and draw char on image */
            Glyph gly = new Glyph(ch.getWidth(), ch.getHeight(), index);
            returned.glyphs.put(c, gly);
    	}
    	returned.maxHeight = imageHeight;

    	//4 (r, g, b, a) times pixels times 224 (glyph count)
    	ByteBuffer data = BufferUtils.createByteBuffer(4 * imageWidth * imageHeight * 225);
    	index = 0;
    	for (int i = 32; i < 256; i++) {
    		index++;
    	    if (i == 127) continue;
    	    char c = (char) i;
    	    BufferedImage ch = createCharImage(font, c, true);
    	    //Debug.debugImagePopup(ch);
    	    
    	    for (int x = 0; x < imageWidth; x++) {
    	    	for (int y = 0; y < imageHeight; y++) {
    	    		Color color = new Color(0.0f, 0.0f, 0.0f, 0.0f);
    	    		if (x < ch.getWidth() - 1 && y < ch.getHeight() - 1) {
        	    		color = new Color(ch.getRGB(x, y));
    	    		}

					data.put(4 * (x + y * imageWidth + index * imageWidth * imageHeight) + 0, (byte)color.getRed());
					data.put(4 * (x + y * imageWidth + index * imageWidth * imageHeight) + 1, (byte)color.getGreen());
					data.put(4 * (x + y * imageWidth + index * imageWidth * imageHeight) + 2, (byte)color.getBlue());
					data.put(4 * (x + y * imageWidth + index * imageWidth * imageHeight) + 3, (byte)color.getAlpha());
					//data.put(4 * (x + y * imageWidth + index * imageWidth * imageHeight) + 0, (byte)color.getRed());
					//data.put(4 * (x + y * imageWidth + index * imageWidth * imageHeight) + 1, (byte)color.getGreen());
					//data.put(4 * (x + y * imageWidth + index * imageWidth * imageHeight) + 2, (byte)color.getBlue());
					//data.put(4 * (x + y * imageWidth + index * imageWidth * imageHeight) + 3, (byte)color.getAlpha());
				}
			}
    	}
		
		data.flip();
    	
    	returned.texture.bind();

		GL11.glTexParameteri(GL30.GL_TEXTURE_2D_ARRAY, GL11.GL_TEXTURE_WRAP_S, GL12.GL_CLAMP_TO_EDGE);
		GL11.glTexParameteri(GL30.GL_TEXTURE_2D_ARRAY, GL11.GL_TEXTURE_WRAP_T, GL12.GL_CLAMP_TO_EDGE);
		GL11.glTexParameteri(GL30.GL_TEXTURE_2D_ARRAY, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
		GL11.glTexParameteri(GL30.GL_TEXTURE_2D_ARRAY, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);

		//GL12.glTexImage2D(GL30.GL_TEXTURE_2D_ARRAY, 1, GL11.GL_RGBA8, r, r, 225, 0, GL11.GL_RGBA8, GL11.GL_FLOAT, data);
		GL42.glTexStorage3D(GL30.GL_TEXTURE_2D_ARRAY, 1, GL11.GL_RGBA8, imageWidth, imageHeight, 225);
		GL12.glTexSubImage3D(GL30.GL_TEXTURE_2D_ARRAY, 0, 0, 0, 0, imageWidth, imageHeight, 225, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, data);
		
		data.clear();
		
		return returned;
    }
    
    static private BufferedImage createCharImage(java.awt.Font font, char c, boolean antiAlias) {
    	/*
        BufferedImage image = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = image.createGraphics();
        if (antiAlias) {
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
            		RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        }
        g.setFont(font);
        FontMetrics metrics = g.getFontMetrics();
        int ascent = metrics.getAscent();
        int charWidth = metrics.charWidth(c);
        int charHeight = metrics.getHeight();
        g.dispose();

        if (charWidth == 0) {
            return null;
        }

        image.flush();
        image = new BufferedImage(charWidth, charHeight, BufferedImage.TYPE_INT_ARGB);
        g = image.createGraphics();
        if (antiAlias) {
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
            		RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        }
        g.setFont(font);
        g.setPaint(java.awt.Color.WHITE);
        g.drawString(String.valueOf(c), 0, ascent);
        g.dispose();
        return image;
        */

		FontRenderContext frc = new FontRenderContext(null, true, true);
	    TextLayout textabel = new TextLayout(String.valueOf(c), font, frc);
	    Rectangle bounds = textabel.getPixelBounds(null, 0, 0);
	    //Logger.debug("-y: " + font.getSize());
	    BufferedImage bi = new BufferedImage(bounds.width + 2, bounds.height + 2, BufferedImage.TYPE_4BYTE_ABGR);
	    Graphics2D g2d = (Graphics2D) bi.getGraphics();
	    Color color = new Color(0.0f, 0.0f, 0.0f, 0.5f);
	    g2d.setBackground(color);
	    color = new Color(1.0f, 1.0f, 1.0f, 1.0f);
	    g2d.setColor(color);
	    textabel.draw(g2d, 0, -bounds.y);
	    g2d.dispose();
	    return bi;
    }
	
}
