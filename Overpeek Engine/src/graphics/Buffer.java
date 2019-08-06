package graphics;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;

public class Buffer {

	int id;
	int type;
	int componentCount;
	
	public static boolean oneIsMapped;
	public boolean thisIsMapped;

	/*
	- "componentCount" is amount of components per vertex
	- "usage" is either GL_STATIC_DRAW or GL_DYNAMIC_DRAW
	*/
	public Buffer(float _data[], int type, int _componentCount, int _usage) {
		componentCount = _componentCount;
		this.type = type; 
		
		id = GL20.glGenBuffers();
		bind();
		GL15.glBufferData(type, _data, _usage);
	}

	/*
	- "componentCount" is amount of components per vertex
	- "usage" is either GL_STATIC_DRAW or GL_DYNAMIC_DRAW
	*/
	public Buffer(FloatBuffer _data, int type, int _componentCount, int _usage) {
		componentCount = _componentCount;
		this.type = type; 
		
		id = GL20.glGenBuffers();
		bind();
		GL15.glBufferData(type, _data, _usage);
	}

	/*
	- "componentCount" is amount of components per vertex
	- "usage" is either GL_STATIC_DRAW or GL_DYNAMIC_DRAW
	*/
	public Buffer(ByteBuffer _data, int type, int _componentCount, int _usage) {
		componentCount = _componentCount;
		this.type = type; 
		
		id = GL20.glGenBuffers();
		bind();
		GL15.glBufferData(type, _data, _usage);
	}

	/*
	- "componentCount" is amount of components per vertex
	- "usage" is either GL_STATIC_DRAW or GL_DYNAMIC_DRAW
	*/
	public Buffer(IntBuffer _data, int type, int _componentCount, int _usage) {
		componentCount = _componentCount;
		this.type = type; 
		
		id = GL20.glGenBuffers();
		bind();
		GL15.glBufferData(type, _data, _usage);
	}

	public void bind() {
		GL15.glBindBuffer(type, id);
	}

	public void unbind() {
		GL15.glBindBuffer(type, 0);
	}

	public void setBufferData(float _data[], int _componentCount) {
		componentCount = _componentCount;

		bind();
		GL15.glBufferSubData(type, 0, _data);
	}

	public void setBufferData(FloatBuffer _data, int _componentCount) {
		componentCount = _componentCount;

		bind();
		GL15.glBufferSubData(type, 0, _data);
	}

	public ByteBuffer mapBuffer() {
		thisIsMapped = true;
		oneIsMapped = true;
		bind();
		return GL15.glMapBuffer(type, GL15.GL_WRITE_ONLY);
	}

	public void mapBuffer(ByteBuffer buffer) {
		bind();
		GL15.glMapBuffer(type, GL15.GL_WRITE_ONLY, buffer);
	}

	public void unmapBuffer() {
		thisIsMapped = false;
		oneIsMapped = false;
		bind();
		GL15.glUnmapBuffer(type);
	}

	public int getComponentCount() { 
		return componentCount; 
	}

	public void setBufferData(ByteBuffer buffermap, int _componentCount) {
		componentCount = _componentCount;

		bind();
		GL15.glBufferSubData(type, 0, buffermap);
	}
	
}
