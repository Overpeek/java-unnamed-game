package graphics.buffers;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import org.lwjgl.opengl.GL15;

import graphics.Renderer.Type;

public class IndexBufferObject extends Buffer {

	public IndexBufferObject(ByteBuffer data, Type rendeType) {
		gen(GL15.GL_ELEMENT_ARRAY_BUFFER, data, 1, rendeType.GLINT());
	}

	public IndexBufferObject(IntBuffer data, Type rendeType) {
		gen(GL15.GL_ELEMENT_ARRAY_BUFFER, data, 1, rendeType.GLINT());
	}
	
	
	
	@Override
	public void bind() {
		GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, id);
	}

	@Override
	public void unbind() {
		GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, 0);
	}

	@Override
	public void delete() {
		GL15.glDeleteBuffers(id);
	}
	
}
