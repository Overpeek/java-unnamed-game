package graphics.buffers;

import java.nio.FloatBuffer;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;

import graphics.Renderer.Type;

public class VertexBufferObject extends Buffer {

	public VertexBufferObject(FloatBuffer data, int componentCount, Type renderType) {
		gen(GL15.GL_ARRAY_BUFFER, data, componentCount, renderType.GLINT());
	}
	
	public void attrib(int index, int components, int strideBytes, int startBytes) {
		GL20.glEnableVertexAttribArray(index);
		GL20.glVertexAttribPointer(index, components, GL11.GL_FLOAT, false, strideBytes, startBytes);
	}

	@Override
	public void bind() {
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, id);
	}

	@Override
	public void unbind() {
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
	}

	@Override
	public void delete() {
		GL15.glDeleteBuffers(id);
	}
	
}
