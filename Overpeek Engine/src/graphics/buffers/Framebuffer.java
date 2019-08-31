package graphics.buffers;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.GL32;

import graphics.Renderer;
import graphics.Texture;
import graphics.primitives.Primitive.Primitives;
import graphics.primitives.Quad;
import utility.Colors;
import utility.Logger;
import utility.vec2;
import utility.vec4;

public class Framebuffer extends Buffer {

	private static Renderer renderer = new Renderer(Primitives.Quad, 1);
	
	private int framebuffer_id;
	private Texture texture;
	private int width;
	private int height;
	private int rbo;
	private vec4 clearColor;
	
	public Framebuffer(int width, int height) {
		this();

		this.framebuffer_id = GL30.glGenFramebuffers();
		bind();
		this.width = width;
		this.height = height;
		

		this.texture = Texture.empty2d(width, height, GL30.GL_TEXTURE_2D);
		GL32.glFramebufferTexture(GL30.GL_FRAMEBUFFER, GL30.GL_COLOR_ATTACHMENT0, this.texture.getId(), 0);
		//GL32.glFramebufferTexture3D(GL30.GL_FRAMEBUFFER, GL30.GL_COLOR_ATTACHMENT0, returned.texture.getType(), returned.texture.getId(), 0, 0);
		//GL30.glFramebufferTexture2D(GL30.GL_FRAMEBUFFER, GL30.GL_COLOR_ATTACHMENT0, GL11.GL_TEXTURE_2D, returned.texture.getId(), 0);
		
		
		this.rbo = GL30.glGenRenderbuffers();
		GL30.glBindRenderbuffer(GL30.GL_RENDERBUFFER, this.rbo); 
        GL11.glDrawBuffer(GL30.GL_COLOR_ATTACHMENT0);
        
		GL30.glRenderbufferStorage(GL30.GL_RENDERBUFFER, GL30.GL_DEPTH24_STENCIL8, width, height);  
		GL30.glBindRenderbuffer(GL30.GL_RENDERBUFFER, 0);
		GL30.glFramebufferRenderbuffer(GL30.GL_FRAMEBUFFER, GL30.GL_DEPTH_STENCIL_ATTACHMENT, GL30.GL_RENDERBUFFER, this.rbo);

		int fboStatus = GL30.glCheckFramebufferStatus(GL30.GL_FRAMEBUFFER);
		if (fboStatus != GL30.GL_FRAMEBUFFER_COMPLETE)
			Logger.error("Framebuffer not complete: " + fboStatus);
		
		clear();
		this.unbind();
	}
	
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
	
	public void clear() {
		GL11.glClearColor(clearColor.x, clearColor.y, clearColor.z, clearColor.w);
		GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
	}
	
	public void clear(vec4 c) {
		GL11.glClearColor(c.x, c.y, c.z, c.w);
		GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
		resetClearColor();
	}
	
	public void clear(float r, float g, float b, float a) {
		GL11.glClearColor(r, g, b, a);
		GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
		resetClearColor();
	}
	
	public void clearColor(vec4 c) {
		GL11.glClearColor(c.x, c.y, c.z, c.w);
		clearColor = c;
	}
	
	public void clearColor(float r, float g, float b, float a) {
		GL11.glClearColor(r, g, b, a);
		clearColor = new vec4(r, g, b, a);
	}
	
	private void resetClearColor() {
		GL11.glClearColor(clearColor.x, clearColor.y, clearColor.z, clearColor.w);
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
	
	public static void unbindAll() {
		GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, 0);
	}
	
	public void unbind() {
		unbindAll();
	}
	
	public void drawFullScreen(vec2 pos, vec2 size, vec4 color) {
		renderer.submit(new Quad(pos, size, 0, color));
		texture.bind();
		renderer.draw();
		renderer.clear();
	}
	
	/*
	 * initDrawing() must have ran
	 * First one has to have image already
	 * Last used is always the first one
	 * **/
	public static void multipass(vec2 pos, vec2 size, Framebuffer first, Framebuffer second, int count) {
		for (int i = 0; i < count; i++) {
			second.bind();
			second.clear();
			first.drawFullScreen(pos, size, Colors.WHITE);
			second.unbind();

			first.bind();
			first.clear();
			second.drawFullScreen(pos, size, Colors.WHITE);
			first.unbind();
		}
	}
	
}
