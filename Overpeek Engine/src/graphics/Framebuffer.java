package graphics;

import static org.lwjgl.opengl.GL11.GL_COLOR_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.GL_DEPTH_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.glClear;
import static org.lwjgl.opengl.GL11.glClearColor;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.GL32;

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
		
		Framebuffer.unbind();
		return returned;
	}
	
	public void clear() {
		glClearColor(clearColor.x, clearColor.y, clearColor.z, clearColor.w);
		GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
	}
	
	public void clear(vec4 c) {
		glClearColor(c.x, c.y, c.z, c.w);
		glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
		clearColor(c);
	}
	
	public void clear(float r, float g, float b, float a) {
		glClearColor(r, g, b, a);
		glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
		clearColor(r, g, b, a);
	}
	
	public void clearColor(vec4 c) {
		clearColor = c;
	}
	
	public void clearColor(float r, float g, float b, float a) {
		clearColor = new vec4(r, g, b, a);
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
	
	public static void unbind() {
		GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, 0);
	}
	
}
