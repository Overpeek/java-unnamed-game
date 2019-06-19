package graphics;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;

import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;

import utility.Colors;

public class TextLabelTexture {
	
	private final static int MAX_CHARACTERS = 512;
	
	private Framebuffer framebuffer;
	private String current_text;
	
	private static Shader shader;
	private static Shader single_shader;
	private static VertexArray textVAO;
	private static Buffer textBuffer;
	private static FloatBuffer bufferMap;
	private static int buffer_current;
	private static int vertex_count;
	private static Vector4f bgcolor = new Vector4f(0.0f, 0.0f, 0.0f, 0.5f);
	private static Window window;
	private static GlyphTexture glyphs;
	
	private static Renderer label_renderer;
	
	
	public String getText() {
		return current_text;
	}
	
	public static void setBGColor(Vector4f col) {
		bgcolor = col;
	}
	
	public Framebuffer getFrameBuffer() {
		return framebuffer;
	}
	
	public static void initialize(Window _window, GlyphTexture _glyphs, Shader single_texture_shader) {
		shader = new Shader();
		shader.setUniform1i("textured", 1);
		
		bufferMap = ByteBuffer.allocateDirect(MAX_CHARACTERS * 6 * VertexData.componentCount() * 4).asFloatBuffer();
		textVAO = new VertexArray();
		textBuffer = new Buffer(bufferMap, GL15.GL_ARRAY_BUFFER, VertexData.componentCount(), GL15.GL_DYNAMIC_DRAW);
		buffer_current = 0;
		vertex_count = 0;
		glyphs = _glyphs;
		window = _window;
		single_shader = single_texture_shader;

		GL20.glEnableVertexAttribArray(0);
		GL20.glVertexAttribPointer(0, 3, GL11.GL_FLOAT, false, VertexData.sizeof(), VertexData.attribPos());
		GL20.glEnableVertexAttribArray(1);
		GL20.glVertexAttribPointer(1, 2, GL11.GL_FLOAT, false, VertexData.sizeof(), VertexData.attribUV());
		GL20.glEnableVertexAttribArray(2);
		GL20.glVertexAttribPointer(2, 1, GL11.GL_FLOAT, false, VertexData.sizeof(), VertexData.attribTex());
		GL20.glEnableVertexAttribArray(3);
		GL20.glVertexAttribPointer(3, 4, GL11.GL_FLOAT, false, VertexData.sizeof(), VertexData.attribCol());
		
		
		//Label rendering
		label_renderer = new Renderer();
	}
	
	public void delete() {
		if (framebuffer != null) framebuffer.delete();
	}
	
	public void rebake(String text) {
		rebake(text, glyphs);
	}
	
	public void draw(Vector3f pos, Vector2f size) {
		draw(pos, size, Colors.WHITE);
	}
	
	public void draw(Vector3f pos, Vector2f size, Vector4f color) {
		size.x *= getFrameBuffer().aspect();
		
		single_shader.enable();
		label_renderer.quads.submit(pos, size, 0, color);
		label_renderer.quads.draw(getFrameBuffer().getTexture());
	}
	
	public void drawCentered(Vector3f pos, Vector2f size, Vector4f color) {
		size.x *= getFrameBuffer().aspect();
		
		single_shader.enable();
		label_renderer.quads.submit(new Vector3f(pos.x - size.x / 2.0f, pos.y - size.y / 2.0f, pos.z), size, 0, color);
		label_renderer.quads.draw(getFrameBuffer().getTexture());
	}
	
	public void rebake(String text, GlyphTexture glyphs) {
		if (text.equals(current_text)) return; //Dont want to update if text is going to be the same
		
		current_text = text;
		delete();
		framebuffer = generateFramebuffer(text, glyphs);
		bakeToFramebuffer(text, glyphs, framebuffer);
	}

	public static TextLabelTexture bakeTextToTexture(String text) {
		return bakeTextToTexture(text, glyphs);
	}

	public static TextLabelTexture bakeTextToTexture(String text, GlyphTexture glyphs) {
		TextLabelTexture obj = new TextLabelTexture();
		if (text.length() == 0) return obj;
		
		obj.current_text = text;
		obj.framebuffer = generateFramebuffer(text, glyphs);
		bakeToFramebuffer(text, glyphs, obj.framebuffer);
		return obj;
	}
	
	public static void viewPortReset(Window window) {
		GL11.glViewport(0, 0, window.getWidth(), window.getHeight());
	}
	
	private static Framebuffer generateFramebuffer(String text, GlyphTexture glyphs) {
		if (text.length() == 0) return null;
		
		int textLength = text.length();
		int framebufferWidth = 0;
		int framebufferHeight = glyphs.getHeight();
		
		for (int i = 0; i < textLength; i++) {
			char c = text.charAt(i);
			framebufferWidth += glyphs.getGlyphData(c).width;
		}
		
		return Framebuffer.createFramebuffer(framebufferWidth, framebufferHeight);
	}
	
	private static void bakeToFramebuffer(String text, GlyphTexture glyphs, Framebuffer obj) {
		if (text.length() == 0) return;
		
		buffer_current = 0;
		vertex_count = 0;
		bufferMap = textBuffer.mapBuffer().asFloatBuffer();
		
		//Submit all characters
		float x = 0;
		for (int i = 0; i < text.length(); i++) {
			char c = text.charAt(i);
			float w = (float)glyphs.getGlyphData(c).width / (float)glyphs.getResolution();
			//float h = (float)glyphs.getGlyphData(c).height / (float)glyphs.getResolution();
			submitQuad(new Vector3f(x, 1.0f, 0.0f), new Vector2f(1.0f, -1.0f), glyphs.getGlyphData(c).textureId, new Vector4f(1.0f, 1.0f, 1.0f, 1.0f));
			x += w;
		}
		
		
		textBuffer.unmapBuffer();

		//Rendering
		obj.bind();
		GL11.glClearColor(bgcolor.x, bgcolor.y, bgcolor.z, bgcolor.w);
		obj.clear();
		
		shader.enable();
		Matrix4f mat = new Matrix4f().ortho(0.0f, x, 1.0f, 0.0f, 0.0f, 10.0f);
		shader.setUniformMat4("pr_matrix", mat);

		// Binding
		GL13.glActiveTexture(GL13.GL_TEXTURE0);
		GL11.glBindTexture(glyphs.getTexture().getType(), glyphs.getTexture().getId());
		textVAO.bind();

		GL11.glDrawArrays(GL11.GL_TRIANGLES, 0, vertex_count);
		buffer_current = 0;
		vertex_count = 0;
		
		Framebuffer.unbind();
		
		viewPortReset(window);
	}

	private static void submitQuad(Vector3f _pos, Vector2f _size, int _id, Vector4f _color) {
		submitVertex(new VertexData(_pos.x, 			_pos.y, 			_pos.z, 0.0f, 0.0f, _id, _color.x, _color.y, _color.z, _color.w));
		submitVertex(new VertexData(_pos.x, 			_pos.y + _size.y, 	_pos.z, 0.0f, 1.0f, _id, _color.x, _color.y, _color.z, _color.w));
		submitVertex(new VertexData(_pos.x + _size.x, 	_pos.y + _size.y, 	_pos.z, 1.0f, 1.0f, _id, _color.x, _color.y, _color.z, _color.w));

		submitVertex(new VertexData(_pos.x, _pos.y, _pos.z, 0.0f, 0.0f, _id, _color.x, _color.y, _color.z, _color.w));
		submitVertex(new VertexData(_pos.x + _size.x, _pos.y + _size.y, _pos.z, 1.0f, 1.0f, _id, _color.x, _color.y, _color.z, _color.w));
		submitVertex(new VertexData(_pos.x + _size.x, _pos.y, _pos.z, 1.0f, 0.0f, _id, _color.x, _color.y, _color.z, _color.w));
	}

	private static void submitVertex(VertexData data) {
		bufferMap.put(buffer_current + 0, data.x);
		bufferMap.put(buffer_current + 1, data.y);
		bufferMap.put(buffer_current + 2, data.z);
		bufferMap.put(buffer_current + 3, data.u);
		bufferMap.put(buffer_current + 4, data.v);
		bufferMap.put(buffer_current + 5, data.i);
		bufferMap.put(buffer_current + 6, data.r);
		bufferMap.put(buffer_current + 7, data.g);
		bufferMap.put(buffer_current + 8, data.b);
		bufferMap.put(buffer_current + 9, data.a);

		buffer_current += VertexData.componentCount();

		vertex_count++;
	}
	
}
