package graphics.buffers;

import java.util.ArrayList;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;

public class VertexArrayObject extends Buffer {
	
	private int id;
	private ArrayList<VertexBufferObject> buffers = new ArrayList<VertexBufferObject>();

	public VertexArrayObject() {
		id = GL30.glGenVertexArrays();
		bind();
	}

	public void addBuffer(VertexBufferObject buffer) {
		bind();
		buffer.bind();
		buffers.add(buffer);
	}

	public void addBuffer(VertexBufferObject buffer, int index) {
		bind();
		buffer.bind();

		GL20.glEnableVertexAttribArray(index);
		GL20.glVertexAttribPointer(index, buffer.getComponentCount(), GL11.GL_FLOAT, false, 0, 0);
		buffers.add(buffer);
	}
	
	public VertexBufferObject getBuffer(int index) {
		return buffers.get(index);
	}

	
	@Override
	public void bind() {
		GL30.glBindVertexArray(id);
	}

	@Override
	public void unbind() {
		GL30.glBindVertexArray(0);
	}

	@Override
	public void delete() {
		GL15.glDeleteBuffers(id);
	}
	
}
