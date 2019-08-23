package graphics.buffers;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;

public abstract class Buffer {
	
	protected int id;
	protected int target;
	protected int usage;
	protected int componentCount;
	protected boolean mapped;
	
	public boolean isMapped() {
		return mapped;
	}
	
	public int getComponentCount() {
		return componentCount;
	}
	
	
	
	
	private void pregen(int target, int componentCount, int usage) {
		this.id = GL20.glGenBuffers();
		this.target = target;
		this.usage = usage;
		this.componentCount = componentCount;
		bind();
	}
	
	protected void gen(int target, ByteBuffer data, int componentCount, int usage) {
		pregen(target, componentCount, usage);
		GL15.glBufferData(target, data, usage);
	}
	
	protected void gen(int target, IntBuffer data, int componentCount, int usage) {
		pregen(target, componentCount, usage);
		GL15.glBufferData(target, data, usage);
	}
	
	protected void gen(int target, FloatBuffer data, int componentCount, int usage) {
		pregen(target, componentCount, usage);
		GL15.glBufferData(target, data, usage);
	}

	public void setBufferData(ByteBuffer data) {
		bind();
		GL15.glBufferSubData(target, 0, data);
	}

	public void setBufferData(IntBuffer data) {
		bind();
		GL15.glBufferSubData(target, 0, data);
	}

	public void setBufferData(FloatBuffer data) {
		bind();
		GL15.glBufferSubData(target, 0, data);
	}

	public ByteBuffer mapBuffer() {
		bind();
		mapped = true;
		return GL15.glMapBuffer(target, GL15.GL_WRITE_ONLY);
	}

	public void unmapBuffer() {
		bind();
		mapped = false;
		GL15.glUnmapBuffer(target);
	}
	
	public abstract void bind();
	public abstract void unbind();
	public abstract void delete();

}
