package graphics;

import java.nio.FloatBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;

import utility.vec2;
import utility.vec3;
import utility.vec4;

public class TriangleRenderer implements GenericRenderer {

	private static int MAX_TRIANGLES = 60000;
	private static int MAX_VERTICES = MAX_TRIANGLES * 3;

	private VertexArray vertexArray;
	private Buffer arrayBufferObject;

	private FloatBuffer arrayBuffer;
	private int vertex_count;
	
	private boolean buffer_mapped;


	
	public int getPrimitiveCount() {
		return vertex_count / 3;
	}
	
	public TriangleRenderer() {
		arrayBuffer = BufferUtils.createFloatBuffer(MAX_VERTICES * VertexData.componentCount);
		buffer_mapped = false;
		vertex_count = 0;
		
		vertexArray = new VertexArray();
		arrayBufferObject = new Buffer(arrayBuffer, GL20.GL_ARRAY_BUFFER, VertexData.componentCount, GL15.GL_DYNAMIC_DRAW);

		GL20.glEnableVertexAttribArray(0);
		GL20.glVertexAttribPointer(0, 3, GL11.GL_FLOAT, false, VertexData.sizeof, VertexData.attribPos);
		GL20.glEnableVertexAttribArray(1);
		GL20.glVertexAttribPointer(1, 2, GL11.GL_FLOAT, false, VertexData.sizeof, VertexData.attribUV);
		GL20.glEnableVertexAttribArray(2);
		GL20.glVertexAttribPointer(2, 1, GL11.GL_FLOAT, false, VertexData.sizeof, VertexData.attribTex);
		GL20.glEnableVertexAttribArray(3);
		GL20.glVertexAttribPointer(3, 4, GL11.GL_FLOAT, false, VertexData.sizeof, VertexData.attribCol);

	}

	@Override
	public void begin() {
		arrayBuffer = arrayBufferObject.mapBuffer().asFloatBuffer();
		buffer_mapped = true;
	}

	@Override
	public void end() {
		arrayBufferObject.unmapBuffer();
		buffer_mapped = false;
	}

	@Override
	public void submitVertex(VertexData data) {
		if (!buffer_mapped) begin();
		
		arrayBuffer.put(data.get());
		vertex_count++;
	}

	@Override
	public void submit(vec3 _pos, vec2 _size, int _id, vec4 _color) {
		submitVertex(new VertexData(_pos.x + _size.x / 2.0f,	_pos.y,				_pos.z, 0.0f, 0.0f, _id, _color.x, _color.y, _color.z, _color.w));
		submitVertex(new VertexData(_pos.x + _size.x,			_pos.y + _size.y, 	_pos.z, 0.0f, 0.0f, _id, _color.x, _color.y, _color.z, _color.w));
		submitVertex(new VertexData(_pos.x, 					_pos.y + _size.y, 	_pos.z, 0.0f, 0.0f, _id, _color.x, _color.y, _color.z, _color.w));
	}

	@Override
	public void clear() {
		vertex_count = 0;
		arrayBuffer.clear();
	}

	@Override
	public void draw(Texture texture) {
		draw(texture.getId(), texture.getType());
	}

	@Override
	public void draw(int texture, int textureType) {
		//Stop submitting
		if (buffer_mapped) end();
		
		//Return if no vertices to render
		if (vertex_count == 0)
			return;

		// Binding
		GL13.glActiveTexture(GL13.GL_TEXTURE0);
		if (textureType != 0) GL11.glBindTexture(textureType, texture);
		vertexArray.bind();
		
		//Actual drawing
		GL11.glDrawArrays(GL11.GL_TRIANGLES, 0, vertex_count);
	}

	@Override
	public void draw() {
		draw(0, 0);
	}

}
