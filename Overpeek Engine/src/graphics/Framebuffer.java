package graphics;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.GL32;

import utility.Logger;
import utility.Logger.type;

public class Framebuffer {

	private int framebuffer_id;
	private Texture texture;
	
	private Framebuffer() {
		
	}
	
	public static Framebuffer createFramebuffer(int width, int height) {
		Framebuffer returned = new Framebuffer();
		returned.framebuffer_id = GL30.glGenFramebuffers();
		returned.bind();
		
		
		returned.texture = Texture.empty(width, height); 
		GL32.glFramebufferTexture(GL30.GL_FRAMEBUFFER, GL30.GL_COLOR_ATTACHMENT0, returned.texture.getId(), 0);
		//GL30.glFramebufferTexture2D(GL30.GL_FRAMEBUFFER, GL30.GL_COLOR_ATTACHMENT0, GL11.GL_TEXTURE_2D, returned.texture.getId(), 0);
		
		
		//int rbo = GL30.glGenRenderbuffers();
		//GL30.glBindRenderbuffer(GL30.GL_RENDERBUFFER, rbo); 
		//GL30.glRenderbufferStorage(GL30.GL_RENDERBUFFER, GL30.GL_DEPTH24_STENCIL8, width, height);  
		//GL30.glBindRenderbuffer(GL30.GL_RENDERBUFFER, 0);
		//GL30.glFramebufferRenderbuffer(GL30.GL_FRAMEBUFFER, GL30.GL_DEPTH_STENCIL_ATTACHMENT, GL30.GL_RENDERBUFFER, rbo);

		

		if (GL30.glCheckFramebufferStatus(GL30.GL_FRAMEBUFFER) != GL30.GL_FRAMEBUFFER_COMPLETE) {
			Logger.out("Framebuffer was not complete!", type.ERROR);
		}
		
		Framebuffer.unbind();
		return returned;
	}
	
	public void clear() {
		GL11.glClear(GL11.GL_COLOR_BUFFER_BIT);
	}
	
	public void delete() {
		GL30.glDeleteFramebuffers(framebuffer_id);
	}
	
	public void bind() {
		GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, framebuffer_id);
	}
	
	public Texture getTexture() {
		return texture;
	}
	
	public static void unbind() {
		GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, 0);
	}
	
}
