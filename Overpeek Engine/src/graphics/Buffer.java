package graphics;

import java.nio.ByteBuffer;

import org.lwjgl.opengl.*;

public class Buffer {

	int id;
	int componentCount;

	/*
	- "componentCount" is amount of components per vertex
	- "usage" is either GL_STATIC_DRAW or GL_DYNAMIC_DRAW
	*/
	Buffer(float _data[], int _componentCount, int _usage) {
		componentCount = _componentCount;
		
		id = GL20.glGenBuffers();
		bind();
		GL15.glBufferData(GL20.GL_ARRAY_BUFFER, _data, _usage);
	}

	void bind() {
		GL15.glBindBuffer(GL20.GL_ARRAY_BUFFER, id);
	}

	void unbind() {
		GL15.glBindBuffer(GL20.GL_ARRAY_BUFFER, 0);
	}

	void setBufferData(float _data[], int _componentCount) {
		componentCount = _componentCount;

		bind();
		GL15.glBufferSubData(GL20.GL_ARRAY_BUFFER, 0, _data);
	}

	ByteBuffer mapBuffer() {
		bind();
		return GL15.glMapBuffer(GL20.GL_ARRAY_BUFFER, GL15.GL_READ_WRITE);
	}

	void unmapBuffer() {
		bind();
		GL15.glUnmapBuffer(GL20.GL_ARRAY_BUFFER);
	}

	int getComponentCount() { 
		return componentCount; 
	}
	
}
