package graphics;

import java.nio.FloatBuffer;

import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;

import graphics.Renderer.VertexData;

public class TextLabelTexture {
	
	private final static int MAX_CHARACTERS = 512;
	
	private Framebuffer framebuffer;
	
	private static Shader shader;
	private static VertexArray textVAO;
	private static Buffer textBuffer;
	private static FloatBuffer bufferMap;
	private static int buffer_current;
	private static int vertex_count;
	
	public Framebuffer getFrameBuffer() {
		return framebuffer;
	}
	
	public static void initialize() {
		shader = new Shader();
		shader.setUniform1i("textured", 1);
		
		bufferMap = FloatBuffer.allocate(MAX_CHARACTERS * 6 * VertexData.componentCount());
		textVAO = new VertexArray();
		textBuffer = new Buffer(bufferMap, VertexData.componentCount(), GL15.GL_DYNAMIC_DRAW);
		buffer_current = 0;
		vertex_count = 0;

		GL20.glEnableVertexAttribArray(0);
		GL20.glVertexAttribPointer(0, 3, GL11.GL_FLOAT, false, VertexData.sizeof(), VertexData.attribPos());
		GL20.glEnableVertexAttribArray(1);
		GL20.glVertexAttribPointer(1, 2, GL11.GL_FLOAT, false, VertexData.sizeof(), VertexData.attribUV());
		GL20.glEnableVertexAttribArray(2);
		GL20.glVertexAttribPointer(2, 1, GL11.GL_FLOAT, false, VertexData.sizeof(), VertexData.attribTex());
		GL20.glEnableVertexAttribArray(3);
		GL20.glVertexAttribPointer(3, 4, GL11.GL_FLOAT, false, VertexData.sizeof(), VertexData.attribCol());
	}

	public static TextLabelTexture bakeTextToTexture(String text, GlyphTexture glyphs) {
		TextLabelTexture obj = new TextLabelTexture();
		int textLength = text.length();
		
		int framebufferWidth = 0;
		int framebufferHeight = glyphs.getHeight();
		
		for (int i = 0; i < textLength; i++) {
			char c = text.charAt(i);
			framebufferWidth += glyphs.getGlyphData(c).width;
		}

		buffer_current = 0;
		vertex_count = 0;
		bufferMap = textBuffer.mapBuffer().asFloatBuffer();
		
		//Submit all characters
		float x = 0;
		for (int i = 0; i < textLength; i++) {
			char c = text.charAt(i);
			float w = (float)glyphs.getGlyphData(c).width / (float)glyphs.getResolution();
			//float h = (float)glyphs.getGlyphData(c).height / (float)glyphs.getResolution();
			submitQuad(new Vector3f(x, 0.0f, 0.0f), new Vector2f(1.0f, 1.0f), glyphs.getGlyphData(c).textureId, new Vector4f(1.0f, 1.0f, 1.0f, 1.0f));
			x += w;
		}
		
		
		textBuffer.unmapBuffer();

		//Rendering
		//obj.framebuffer = Framebuffer.createFramebuffer(680, 720);
		obj.framebuffer = Framebuffer.createFramebuffer(framebufferWidth, framebufferHeight);
		GL30.glViewport(0, 0, framebufferWidth, framebufferHeight);
		obj.framebuffer.bind();
		obj.framebuffer.clear();
		
		shader.enable();
		Matrix4f mat = new Matrix4f().ortho(0.0f, x, 1.0f, 0.0f, 0.0f, 10.0f);
		shader.setUniformMat4("pr_matrix", mat);
		GL11.glEnable(GL11.GL_DEPTH_TEST);

		// Binding
		GL13.glActiveTexture(GL13.GL_TEXTURE0);
		GL11.glBindTexture(glyphs.getTexture().getType(), glyphs.getTexture().getId());
		textVAO.bind();

		GL11.glDrawArrays(GL11.GL_TRIANGLES, 0, vertex_count);
		buffer_current = 0;
		vertex_count = 0;

		GL11.glDisable(GL11.GL_DEPTH_TEST);
		
		Framebuffer.unbind();
		
		return obj;
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
