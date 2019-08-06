package graphics;

import org.lwjgl.opengl.GL20;

import java.util.ArrayList;

import org.lwjgl.opengl.*;

public class VertexArray {
	
	private int id;
	private ArrayList<Buffer> buffers = new ArrayList<Buffer>();

	public VertexArray() {
		id = GL30.glGenVertexArrays();
		bind();
	}

	public void addBuffer(Buffer buffer) {
		bind();
		buffer.bind();
		buffers.add(buffer);
	}

	public void addBuffer(Buffer buffer, int index) {
		bind();
		buffer.bind();

		GL20.glEnableVertexAttribArray(index);
		GL20.glVertexAttribPointer(index, buffer.getComponentCount(), GL11.GL_FLOAT, false, 0, 0);
		buffers.add(buffer);
	}
	
	public Buffer getBuffer(int index) {
		return buffers.get(index);
	}

	public void bind() {
		GL30.glBindVertexArray(id);
	}

	public void unbind() {
		GL30.glBindVertexArray(0);
	}
	
}
