package graphics;

import java.util.ArrayList;

import org.lwjgl.opengl.GL40;

import graphics.GlyphTexture.Glyph;

import org.lwjgl.opengl.GL11;

import utility.Colors;
import utility.Debug;
import utility.Logger;
import utility.mat4;
import utility.vec2;
import utility.vec3;
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
		public String text;
		
		public TextDrawData() {
			drawData = new ArrayList<CharColorPair>();
		}
	}
	
	private static class QueueData {
		public vec3 pos;
		public vec2 size;
		public vec4 color;
		public float aspect;
		public Texture tex;
		
		public QueueData(vec3 pos, vec2 size, vec4 color, float aspect, Texture tex) {
			this.pos = pos;
			this.size = size;
			this.color = color;
			this.tex = tex;
			this.aspect = aspect;
		}
	}
	
	private static ArrayList<QueueData> drawQueue;
	
	private String current_text;
	private Framebuffer buffer;
	private float textAspect;

	private static Window window;
	private static Shader label_draw_shader;
	private static Shader label_bake_shader;
	private static Renderer label_draw;
	private static Renderer label_bake;
	private static GlyphTexture glyphs;
	
	
	public String getText() {
		return current_text;
	}
	
	public Framebuffer getFramebuffer() {
		return buffer;
	}
	
	public static void initialize(Window _window, GlyphTexture _glyphs, Shader single_texture_shader) {
		glyphs = _glyphs;
		label_draw_shader = single_texture_shader;
		label_bake_shader = Shader.multiTextureShader();
		label_draw = new Renderer();
		label_bake = new Renderer();
		drawQueue = new ArrayList<QueueData>();
		window = _window;
	}
	
	public void delete() {
		if (buffer != null) buffer.delete();
	}
	
	public void queueDraw(vec3 pos, vec2 size) {
		queueDraw(pos, size, Colors.WHITE);
	}
	
	public static void drawQueue() {
		//label_draw_shader.enable();
		for (QueueData qd : drawQueue) {
			qd.size.x *= qd.aspect;
			qd.pos.z += 0.1f;
			
			label_draw.clear();
			label_draw.quads.submit(qd.pos, qd.size, 0, qd.color);
			label_draw.quads.draw(qd.tex);
		}
		drawQueue = new ArrayList<QueueData>();
	}
	
	public void queueDraw(vec3 pos, vec2 size, vec4 color) {
		drawQueue.add(new QueueData(new vec3(pos), new vec2(size), new vec4(color), textAspect, buffer.getTexture()));
	}
	
	public void queueDrawCentered(vec3 pos, vec2 size, vec4 color) {
		pos.x -= size.x * textAspect / 2.0f;
		pos.y -= size.y / 2.0f;
		queueDraw(new vec3(pos.x, pos.y, pos.z), size, color);
	}
	
	public TextLabelTexture rebake(String text, GlyphTexture glyphs) {
		if (text.equals(current_text)) return this; //No rebaking if new text is going to be the same
		
		delete();
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
		drawData.text = stringBuilder.toString();
		return drawData;
	}
	
	private static void resetViewPort() {
		window.viewport();
	}
	
	/**
	 * Framebuffer has to be created
	 * */
	private static void stringToTextureOpenGL(TextLabelTexture label, String text, GlyphTexture glyphs) {
		//Debug.startTimer();
		label.current_text = text;
		TextDrawData drawData = textToDrawData(text);
		
		//Get final framebuffer metrics
		int framebuffer_width = 0;
		int framebuffer_height = glyphs.getTopToBottomHeight();
		for (TextDrawData.CharColorPair cp : drawData.drawData) {
			framebuffer_width += glyphs.getGlyphData(cp.character).advance;
		}
		label.textAspect = (float)framebuffer_width / (float)framebuffer_height;
		//Logger.debug(framebuffer_width + " * " + framebuffer_height);
		
		//Framebuffer stuff
		label.buffer = Framebuffer.createFramebuffer(framebuffer_width, framebuffer_height);
		label.buffer.bind();
		mat4 bakeMatrix = new mat4().ortho(0.0f, framebuffer_width, framebuffer_height, 0.0f);
		label_bake_shader.setUniformMat4("pr_matrix", bakeMatrix);
		
		//Clear framebuffer (background)
		label.buffer.clear(0.0f, 0.0f, 0.0f, 0.2f);
		
		//Draw submits
		float xDelta = 0.0f;
		float baseline = -glyphs.getHeight();
		label_bake.clear();
		//label_bake.quads.submit(new vec3(-10.0f), new vec2(framebuffer_width * 2, framebuffer_height * 2), 0, Colors.ORANGE);
		for (TextDrawData.CharColorPair cp : drawData.drawData) {
			Glyph glyph = glyphs.getGlyphData(cp.character);
			float yDelta = glyph.y;
			//label_bake.quads.submitVertex(new VertexData(new vec3(xDelta, yDelta, 0.0f), 						 					new vec2(0.0f, 0.0f), glyph.textureId, cp.color));
			//label_bake.quads.submitVertex(new VertexData(new vec3(xDelta, yDelta + glyphs.getHeight(), 0.0f), 				 		new vec2(0.0f, 1.0f), glyph.textureId, cp.color));
			//label_bake.quads.submitVertex(new VertexData(new vec3(xDelta + glyphs.getWidth(), yDelta + glyphs.getHeight(), 0.0f), 	new vec2(1.0f, 1.0f), glyph.textureId, cp.color));
			//label_bake.quads.submitVertex(new VertexData(new vec3(xDelta + glyphs.getWidth(), yDelta, 0.0f), 		 				new vec2(1.0f, 0.0f), glyph.textureId, cp.color));
			label_bake.quads.submit(new vec3(xDelta, yDelta + baseline, 0.0f), new vec2(glyphs.getWidth(), glyphs.getHeight()), glyph.textureId, cp.color);
			xDelta += glyph.advance;
		}
		
		//Drawing
		label_bake_shader.enable();
		label_bake_shader.setUniform1i("usetex", 1);
		Texture tex = glyphs.getTexture();
		label_bake.quads.draw(tex);
		Framebuffer.unbind();
		
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
	
	public static TextLabelTexture bakeToTexture(String text, GlyphTexture glyphs) {
		TextLabelTexture label = new TextLabelTexture();
		stringToTextureOpenGL(label, text, glyphs);
		return label;
	}
	
}
