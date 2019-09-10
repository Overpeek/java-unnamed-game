package graphics;

import java.util.ArrayList;

import org.lwjgl.opengl.GL13;

import graphics.GlyphTexture.Glyph;
import graphics.buffers.Framebuffer;
import graphics.primitives.Quad;
import graphics.primitives.Primitive.Primitives;
import utility.Colors;
import utility.mat4;
import utility.vec2;
import utility.vec4;

public class TextLabelTexture {

	private static class TextDrawData {
		private static class CharColorPair {
			public char character;
			public vec4 color;
			
			public CharColorPair(char character, vec4 color) {
				this.character = character;
				this.color = color;
			}
		}
		
		public ArrayList<CharColorPair> drawData;
		
		public TextDrawData() {
			drawData = new ArrayList<CharColorPair>();
		}
	}
	
	private static class QueueData {
		public TextLabelTexture label;
		
		public QueueData(TextLabelTexture label) {
			this.label = label;
		}
	}
	
	public static enum alignment {
		TOP_LEFT(0.0f, 0.0f), 		TOP_CENTER(0.5f, 0.0f), 	TOP_RIGHT(1.0f, 0.0f), 
		CENTER_LEFT(0.0f, 0.5f), 	CENTER_CENTER(0.5f, 0.5f), 	CENTER_RIGHT(1.0f, 0.5f), 
		BOTTOM_LEFT(0.0f, 1.0f), 	BOTTOM_CENTER(0.5f, 1.0f), 	BOTTOM_RIGHT(1.0f, 1.0f);
		
		vec2 a;
		private alignment(float x, float y) {
			a = new vec2(x, y);
		}
		
		public vec2 getAlignment(vec2 framebuffer_size) {
			return framebuffer_size.mulLocal(a);
		}
	}
	
	
	
	private TextLabelTexture() {
		label_draw = new Renderer(Primitives.Quad, 1);
	}
	
	private Renderer label_draw;
	
	private static ArrayList<QueueData> drawQueue;
	
	private float textAspect;
	private float framebufferAspect;
	private String current_text;
	private Framebuffer buffer;

	private static Window window;
	private static Shader label_draw_shader;
	private static Shader label_bake_shader;
	private static Renderer label_bake;
	private static GlyphTexture glyphs;
	
	
	public String getText() {
		return current_text;
	}
	
	public Framebuffer getFramebuffer() {
		return buffer;
	}
	
	public static Shader getDefaultShader() {
		return label_draw_shader;
	}
	
	public static void initialize(Window _window, GlyphTexture _glyphs) {
		glyphs = _glyphs;
		label_draw_shader = Shader.singleTextureShader();
		label_bake_shader = Shader.multiTextureShader();
		label_bake = new Renderer();
		drawQueue = new ArrayList<QueueData>();
		window = _window;
	}
	
	public void delete() {
		if (buffer != null) buffer.delete();
	}
	
	public static void drawQueue(boolean useDefaultShader) {
		if (useDefaultShader) label_draw_shader.bind();
		GL13.glActiveTexture(GL13.GL_TEXTURE0);
		for (QueueData qd : drawQueue) {
			qd.label.getFramebuffer().getTexture().bind();
			qd.label.label_draw.draw();
		}
		drawQueue = new ArrayList<QueueData>();
	}
	
//	public void submitCentered(vec2 _pos, vec2 size) {
//		vec2 pos = _pos.clone();
//		pos.x -= size.x * textAspect / 2.0f;
//		pos.y -= size.y / 2.0f;
//		submit(pos, size);
//	}
//	
//	public void submit(vec2 pos, vec2 size) {
//		label_draw.clear();
//		label_draw.submit(new Quad(pos, new vec2(size.x * framebufferAspect, size.y), 0, Colors.WHITE));
//		drawQueue.add(new QueueData(this));
//	}
	
	public void submit(vec2 pos, vec2 size, alignment align) {
		label_draw.clear();
		vec2 framebuffer_size = new vec2(size.x * framebufferAspect, size.y);
		vec2 text_size = new vec2(size.x * textAspect, size.y);
		vec2 framebuffer_position = pos.subLocal(align.getAlignment(text_size));
		label_draw.submit(new Quad(framebuffer_position, framebuffer_size, 0, Colors.WHITE));
		drawQueue.add(new QueueData(this));
	}
	
	public TextLabelTexture rebake(String text, GlyphTexture glyphs) {
		if (text.equals(current_text)) return this; //No rebaking if new text is going to be the same
		
		//No need to generate new framebuffer
		stringToTextureOpenGL(this, text, glyphs);
		return this;
	}
	
	public TextLabelTexture rebake(String text) {
		rebake(text, glyphs);
		return this;
	}

	public static TextLabelTexture bakeToTexture(String text) {
		return bakeToTexture(text, glyphs);
	}
	
	private static TextDrawData textToDrawData(String text) {
		TextDrawData drawData = new TextDrawData();
		boolean nextIsColor = false;
		vec4 curColor = new vec4(1.0f, 1.0f, 1.0f, 1.0f);
		StringBuilder stringBuilder = new StringBuilder();
		for (char c : text.toCharArray()) {
			
			if (nextIsColor) {
				try {
					int colorCode = Integer.parseInt(String.valueOf(c));
					curColor = Colors.colorCode(colorCode);
					nextIsColor = false;
					continue;
				} catch (Exception e) {
					//Wasn't colorCode
					
					stringBuilder.append('$');
					drawData.drawData.add(new TextDrawData.CharColorPair('$', curColor));
				}
			}
			
			nextIsColor = false;
			if (c == '$') {
				nextIsColor = true;
				continue;
			}
			
			stringBuilder.append(c);
			drawData.drawData.add(new TextDrawData.CharColorPair(c, curColor));
		}
		return drawData;
	}
	
	private static void resetViewPort() {
		window.viewport();
	}
	
	private Framebuffer generateFramebuffer(String text, GlyphTexture glyphs, int minWidth, int minHeight) {
		TextDrawData drawData = textToDrawData(text);
		
		int framebuffer_width = 0;
		int framebuffer_height = glyphs.getHeight();
		for (TextDrawData.CharColorPair cp : drawData.drawData) {
			framebuffer_width += glyphs.getGlyphData(cp.character).advance;
		}
		textAspect = (float)framebuffer_width / (float)framebuffer_height;
		
		if (framebuffer_height <= 0) framebuffer_height = 1; // no div by 0
		if (framebuffer_width <= minWidth) framebuffer_width = minWidth;
		if (framebuffer_height <= minHeight) framebuffer_height = minHeight;
		framebufferAspect = (float)framebuffer_width / (float)framebuffer_height;
		
		//Framebuffer stuff
		buffer = new Framebuffer(framebuffer_width, framebuffer_height);
		return buffer;
	}
	
	/**
	 * Framebuffer has to be created
	 * */
	private static void stringToTextureOpenGL(TextLabelTexture label, String text, GlyphTexture glyphs) {
		//Debug.startTimer();
		label.current_text = text;
		TextDrawData drawData = textToDrawData(text);
		
		label.buffer.bind();
		mat4 bakeMatrix = new mat4().ortho(0.0f, label.getFramebuffer().getWidth(), 0.0f, label.getFramebuffer().getHeight());
		label_bake_shader.setUniformMat4("pr_matrix", bakeMatrix);
		
		//Clear framebuffer (background)
		//label.buffer.clear(1.0f, 0.0f, 0.5f, 1.0f); //pink
		label.buffer.clear(0.0f, 0.0f, 0.0f, 0.0f); //transparent
		
		//Draw submits
		float xDelta = 0.0f;
		label_bake.clear();
		for (TextDrawData.CharColorPair cp : drawData.drawData) {
			Glyph glyph = glyphs.getGlyphData(cp.character);
			//float yDelta = glyph.y;
			label_bake.submit(new Quad(new vec2(xDelta, glyphs.getHeight()), new vec2(glyphs.getWidth(), -glyphs.getHeight()), glyph.textureId, cp.color));
			xDelta += glyph.advance;
		}
		
		//Drawing
		label_bake_shader.bind();
		Texture tex = glyphs.getTexture();
		tex.bind();
		label_bake_shader.setUniform1i("usetex", 1);
		label_bake.draw();
		label.buffer.unbind();
		
		resetViewPort();
		//Debug.printElapsedMS();
	}
	
	/*
	private static void stringToTextureJava(TextLabelTexture label, String text, Font font) {
		label.current_text = text;
		TextDrawData drawData = textToDrawData(text);
		
		//Setup
		FontRenderContext frc = new FontRenderContext(null, true, true);
	    TextLayout textabel = new TextLayout(drawData.text, font, frc);
	    Rectangle bounds = textabel.getPixelBounds(null, 0, 0);
	    BufferedImage bi = new BufferedImage(bounds.width + drawData.text.length() * 2, bounds.height + 2, BufferedImage.TYPE_4BYTE_ABGR);
	    Graphics2D g2d = (Graphics2D) bi.getGraphics();
	    Color bg = new Color(1.0f, 1.0f, 1.0f, 0.5f);
	    g2d.setBackground(bg);
	    label.textAspect = (float)bounds.width / (float)bounds.height;
		
	    //Drawing
		float xDelta = 0.0f;
		for (TextDrawData.CharColorPair cp : drawData.drawData) {
		    TextLayout characterLabel = new TextLayout(String.valueOf(cp.character), font, frc);
		    g2d.setColor(cp.color.toJavaColor());
		    characterLabel.draw(g2d, xDelta + 1, -bounds.y + 1);
		    xDelta += characterLabel.getAdvance();
		}
	    
	    g2d.dispose();
	    //label.texture = Texture.loadBufferedImage(bi);
	}
	*/
	
	/**
	 * Default min-width is 50 characters
	 * */
	public static TextLabelTexture bakeToTexture(String text, GlyphTexture glyphs) {
		TextLabelTexture label = new TextLabelTexture();
		label.generateFramebuffer(text, glyphs, glyphs.getWidth() * 50, 1);
		stringToTextureOpenGL(label, text, glyphs);
		return label;
	}

	/**
	 * Default min-width is 50 characters
	 * */
	public static TextLabelTexture bakeToTexture(String text, int minWidth) {
		TextLabelTexture label = new TextLabelTexture();
		label.generateFramebuffer(text, glyphs, minWidth, 1);
		stringToTextureOpenGL(label, text, glyphs);
		return label;
	}

	/**
	 * Default min-width is 50 characters
	 * */
	public static TextLabelTexture bakeToTexture(String text, GlyphTexture glyphs, int minWidth) {
		TextLabelTexture label = new TextLabelTexture();
		label.generateFramebuffer(text, glyphs, minWidth, 1);
		stringToTextureOpenGL(label, text, glyphs);
		return label;
	}
	
}