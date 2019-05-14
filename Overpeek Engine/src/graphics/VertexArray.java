package graphics;

import org.lwjgl.opengl.GL20;

import java.util.ArrayList;

import org.lwjgl.opengl.*;

public class VertexArray {
	
	int id;
	ArrayList<Buffer> buffers = new ArrayList<Buffer>();

	public VertexArray() {
		id = GL30.glGenVertexArrays();
		bind();
	}

	void addBuffer(Buffer buffer) {
		bind();
		buffer.bind();
		buffers.add(buffer);
	}

	void addBuffer(Buffer buffer, int index) {
		bind();
		buffer.bind();

		GL20.glEnableVertexAttribArray(index);
		GL20.glVertexAttribPointer(index, buffer.getComponentCount(), GL11.GL_FLOAT, false, 0, 0);
		buffers.add(buffer);
	}
	
	Buffer getBuffer(int index) {
		return buffers.get(index);
	}

	void bind() {
		GL30.glBindVertexArray(id);
	}

	void unbind() {
		GL30.glBindVertexArray(0);
	}
	
}
