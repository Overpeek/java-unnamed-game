package graphics;

import static org.lwjgl.opengl.GL11.GL_COLOR_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.GL_DEPTH_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.glClear;
import static org.lwjgl.opengl.GL11.glClearColor;

import java.nio.FloatBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.GL32;

import utility.Colors;
import utility.Logger;
import utility.vec4;

public class Framebuffer {

	private int framebuffer_id;
	private Texture texture;
	private int width;
	private int height;
	private int rbo;
	private vec4 clearColor;
	
	private Framebuffer() {
		clearColor = new vec4(0.0f, 0.0f, 0.0f, 0.0f);
	}
	
	public int getWidth() {
		return width;
	}
	
	public int getHeight() {
		return height;
	}
	
	public float aspect() {
		return (float)width / (float)height;
	}
	
	public static Framebuffer createFramebuffer(int width, int height) {
		Framebuffer returned = new Framebuffer();

		returned.framebuffer_id = GL30.glGenFramebuffers();
		returned.bind();
		returned.width = width;
		returned.height = height;
		

		returned.texture = Texture.empty2d(width, height, GL30.GL_TEXTURE_2D);
		GL32.glFramebufferTexture(GL30.GL_FRAMEBUFFER, GL30.GL_COLOR_ATTACHMENT0, returned.texture.getId(), 0);
		//GL32.glFramebufferTexture3D(GL30.GL_FRAMEBUFFER, GL30.GL_COLOR_ATTACHMENT0, returned.texture.getType(), returned.texture.getId(), 0, 0);
		//GL30.glFramebufferTexture2D(GL30.GL_FRAMEBUFFER, GL30.GL_COLOR_ATTACHMENT0, GL11.GL_TEXTURE_2D, returned.texture.getId(), 0);
		
		
		returned.rbo = GL30.glGenRenderbuffers();
		GL30.glBindRenderbuffer(GL30.GL_RENDERBUFFER, returned.rbo); 
        GL11.glDrawBuffer(GL30.GL_COLOR_ATTACHMENT0);
        
		GL30.glRenderbufferStorage(GL30.GL_RENDERBUFFER, GL30.GL_DEPTH24_STENCIL8, width, height);  
		GL30.glBindRenderbuffer(GL30.GL_RENDERBUFFER, 0);
		GL30.glFramebufferRenderbuffer(GL30.GL_FRAMEBUFFER, GL30.GL_DEPTH_STENCIL_ATTACHMENT, GL30.GL_RENDERBUFFER, returned.rbo);

		int fboStatus = GL30.glCheckFramebufferStatus(GL30.GL_FRAMEBUFFER);
		if (fboStatus != GL30.GL_FRAMEBUFFER_COMPLETE)
			Logger.out("Framebuffer not complete: " + fboStatus, Logger.type.ERROR);
		
		returned.unbind();
		return returned;
	}
	
	public void clear() {
		glClearColor(clearColor.x, clearColor.y, clearColor.z, clearColor.w);
		glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
	}
	
	public void clear(vec4 c) {
		glClearColor(c.x, c.y, c.z, c.w);
		glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
		resetClearColor();
	}
	
	public void clear(float r, float g, float b, float a) {
		glClearColor(r, g, b, a);
		glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
		resetClearColor();
	}
	
	public void clearColor(vec4 c) {
		glClearColor(c.x, c.y, c.z, c.w);
		clearColor = c;
	}
	
	public void clearColor(float r, float g, float b, float a) {
		glClearColor(r, g, b, a);
		clearColor = new vec4(r, g, b, a);
	}
	
	private void resetClearColor() {
		glClearColor(clearColor.x, clearColor.y, clearColor.z, clearColor.w);
	}
	
	public void delete() {
		GL30.glDeleteFramebuffers(framebuffer_id);
		GL30.glDeleteRenderbuffers(rbo);
		texture.delete();
	}
	
	public void bind() {
		GL30.glViewport(0, 0, getWidth(), getHeight());
		GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, framebuffer_id);
		Texture.unbind(); 
	}
	
	public Texture getTexture() {
		return texture;
	}
	
	public void unbind() {
		GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, 0);
	}
	

	private static VertexArray draw_vao;
	private static Buffer draw_vbo;
	private static FloatBuffer draw_buffer;
	public static void initDrawing() {
		draw_vao = new VertexArray();
		draw_buffer = BufferUtils.createFloatBuffer(VertexData.componentCount * 6);
		draw_vbo = new Buffer(draw_buffer, GL20.GL_ARRAY_BUFFER, VertexData.componentCount, GL15.GL_DYNAMIC_DRAW);
		GL20.glEnableVertexAttribArray(0);
		GL20.glVertexAttribPointer(0, 3, GL11.GL_FLOAT, false, VertexData.sizeof, VertexData.attribPos);
		GL20.glEnableVertexAttribArray(1);
		GL20.glVertexAttribPointer(1, 2, GL11.GL_FLOAT, false, VertexData.sizeof, VertexData.attribUV);
		GL20.glEnableVertexAttribArray(2);
		GL20.glVertexAttribPointer(2, 1, GL11.GL_FLOAT, false, VertexData.sizeof, VertexData.attribTex);
		GL20.glEnableVertexAttribArray(3);
		GL20.glVertexAttribPointer(3, 4, GL11.GL_FLOAT, false, VertexData.sizeof, VertexData.attribCol);
	}
	
	public void drawFullScreen(vec4 color) {
		draw_buffer.put(new VertexData(-1.0f, -1.0f, 0.0f, 							0.0f, 0.0f, 0.0f, color.x, color.y, color.z, color.w).get());
		draw_buffer.put(new VertexData(-1.0f,  1.0f, 0.0f, 							0.0f, 1.0f, 0.0f, color.x, color.y, color.z, color.w).get());
		draw_buffer.put(new VertexData( 1.0f,  1.0f, 0.0f, 							1.0f, 1.0f, 0.0f, color.x, color.y, color.z, color.w).get());
		
		draw_buffer.put(new VertexData(-1.0f, -1.0f, 0.0f,							0.0f, 0.0f, 0.0f, color.x, color.y, color.z, color.w).get());
		draw_buffer.put(new VertexData( 1.0f,  1.0f, 0.0f,							1.0f, 1.0f, 0.0f, color.x, color.y, color.z, color.w).get());
		draw_buffer.put(new VertexData( 1.0f, -1.0f, 0.0f,							1.0f, 0.0f, 0.0f, color.x, color.y, color.z, color.w).get());
		draw_buffer.flip();
		draw_vbo.setBufferData(draw_buffer, VertexData.componentCount);

		GL13.glActiveTexture(GL13.GL_TEXTURE0);
		getTexture().bind();
		draw_vao.bind();
		GL11.glDrawArrays(GL11.GL_TRIANGLES, 0, 6);
		draw_buffer.clear();
	}
	
	/*
	 * initDrawing() must have ran
	 * First one has to have image already
	 * Last used is always the first one
	 * **/
	public static void multipass(Framebuffer first, Framebuffer second, int count) {
		for (int i = 0; i < count; i++) {
			second.bind();
			second.clear();
			first.drawFullScreen(Colors.WHITE);
			second.unbind();

			first.bind();
			first.clear();
			second.drawFullScreen(Colors.WHITE);
			first.unbind();
		}
	}
	
}
