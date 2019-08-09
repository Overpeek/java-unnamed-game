package graphics;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.Graphics2D;
import java.awt.Rectangle;
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

import utility.Loader;

public class GlyphTexture {

	public static class Glyph {

	    public final int advance;
	    public final int y;
	    public final int height;
	    public final int textureId;

	    public Glyph(int advance, int y, int height, int id) {
	        this.advance = advance;
	        this.y = y;
	        this.height = height;
	        this.textureId = id;
	    }

	}
    private Map<Character, Glyph> glyphs = new HashMap<>();
    private int maxHeight;
    private int maxWidth;
    private int resolution;
	private Texture texture;
	private Rectangle maxTextBounds;
	
	private GlyphTexture() {}
    
    public Texture getTexture() {
    	return texture;
    }

    /**
     * Max height difference between all characters
     * */
    public int getHeight() {
    	return maxHeight;
    }
    
    /**
     * Width of the widest character
     * */
    public int getWidth() {
    	return maxWidth;
    }
    
    public Glyph getGlyphData(Character c) {
		return glyphs.get(c);
    }

    /**
     * Font size
     * */
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
    	GlyphTexture returned = new GlyphTexture();
    	returned.glyphs = new HashMap<>();
    	returned.texture = new Texture();
    	returned.texture.setType(GL30.GL_TEXTURE_2D_ARRAY);
    	returned.texture.setId(GL11.glGenTextures());;
    	returned.resolution = font.getSize();

    	//Get max possible bounds
    	FontRenderContext frc = new FontRenderContext(null, true, true);
	    StringBuilder allCharacters = new StringBuilder();
	    int maxWidth = 0;
    	for (char i = 32; i < 256; i++) {
    	    if (i == 127) continue;
    	    TextLayout textabel = new TextLayout(String.valueOf(i), font, frc);
    		allCharacters.append(i);
    		Rectangle bound = textabel.getPixelBounds(frc, 0, 0);
    		maxWidth = Math.max(maxWidth, bound.width);
    	}
    	
    	TextLayout textabel = new TextLayout(allCharacters.toString(), font, frc);
	    returned.maxTextBounds = textabel.getPixelBounds(null, 0, 0);
    	returned.maxHeight = returned.maxTextBounds.height;
    	returned.maxWidth = maxWidth;

    	
    	
    	
    	//4 (r, g, b, a) times pixels times 224 (glyph count)
    	ByteBuffer data = BufferUtils.createByteBuffer(4 * returned.maxWidth * returned.maxHeight * 225);
    	int index = 0;
    	for (int i = 32; i < 256; i++) {
    	    if (i == 127) continue;
    	    index++;
    	    char c = (char) i;
    	    BufferedImage ch = returned.createCharImage(font, c, index, true);
    	    //if (c == 'M') Debug.debugImagePopup(Debug.resize(ch, 600, 600));
    	    
    	    for (int y = 0; y < returned.maxHeight; y++) {
        	    for (int x = 0; x < returned.maxWidth; x++) {
        	    	Color color = new Color(0.0f, 0.0f, 0.0f, 0.0f);
        	    	if (x < ch.getWidth() - 1 && y < ch.getHeight() - 1) {
        	    		color = new Color(ch.getRGB(x, ch.getHeight() - 1 - y));
        	    		//Logger.debug("Color: " + new vec4(color));
        	    		//System.out.printf("0x%02X", (byte)color.getAlpha());
        	    	}
        	    	
        	    	//data.putInt(x + y * returned.maxWidth + index * returned.maxWidth * returned.maxHeight, color.getRGB());
        	    	data.put(4 * (x + y * returned.maxWidth + index * returned.maxWidth * returned.maxHeight) + 0, (byte)255);
        	    	data.put(4 * (x + y * returned.maxWidth + index * returned.maxWidth * returned.maxHeight) + 1, (byte)255);
        	    	data.put(4 * (x + y * returned.maxWidth + index * returned.maxWidth * returned.maxHeight) + 2, (byte)255);
        	    	data.put(4 * (x + y * returned.maxWidth + index * returned.maxWidth * returned.maxHeight) + 3, (byte)color.getRed());
        	    	//data.put(4 * (x + y * returned.maxWidth + index * returned.maxWidth * returned.maxHeight) + 0, (byte)color.getRed());
        	    	//data.put(4 * (x + y * returned.maxWidth + index * returned.maxWidth * returned.maxHeight) + 1, (byte)color.getGreen());
        	    	//data.put(4 * (x + y * returned.maxWidth + index * returned.maxWidth * returned.maxHeight) + 2, (byte)color.getBlue());
        	    	//data.put(4 * (x + y * returned.maxWidth + index * returned.maxWidth * returned.maxHeight) + 3, (byte)color.getAlpha());
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
		GL42.glTexStorage3D(GL30.GL_TEXTURE_2D_ARRAY, 1, GL11.GL_RGBA8, returned.maxWidth, returned.maxHeight, 225);
		GL12.glTexSubImage3D(GL30.GL_TEXTURE_2D_ARRAY, 0, 0, 0, 0, returned.maxWidth, returned.maxHeight, 225, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, data);
		
		data.clear();
		
		return returned;
    }
    
    /**
     *adds new character to set
     * */
    private BufferedImage createCharImage(java.awt.Font font, char c, int texture_index, boolean antiAlias) {
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
    	
    	

		FontRenderContext frc = new FontRenderContext(null, antiAlias, true);
	    TextLayout textabel = new TextLayout(String.valueOf(c), font, frc);
	    Rectangle bounds = textabel.getPixelBounds(frc, 0, 0);
	    //Logger.debug("c: " + c + ", c.b: " + bounds + ", greatest c.b: " + maxTextBounds);
	    	// c: j, c.b: Rectangle[x=-3,y=-46,width=13,height=60], greatest c.b: Rectangle[x=22,y=-58,width=61,height=61]
	    BufferedImage bi = new BufferedImage(maxWidth, maxHeight, BufferedImage.TYPE_INT_ARGB);
	    Graphics2D g2d = (Graphics2D) bi.getGraphics();
	    Color color = new Color(0.0f, 0.0f, 0.0f, 1.0f);
	    g2d.setBackground(color);
	    g2d.setColor(color);
	    g2d.fillRect(0, 0, maxWidth, maxHeight);
	    color = new Color(1.0f, 1.0f, 1.0f, 1.0f);
	    g2d.setColor(color);
	    textabel.draw(g2d, 0, -maxTextBounds.y);
	    g2d.dispose();
	    
	    glyphs.put(c, new Glyph((int) textabel.getAdvance(), -bounds.y, bounds.height, texture_index));
	    
	    return bi;
    }
	
}
