package graphics;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL40;

public class QuadRenderer implements GenericRenderer {
	
	private static int MAX_QUADS = 60000;
	private static int MAX_VERT = MAX_QUADS * 4;
	private static int MAX_INDEX = MAX_QUADS * 6;

	private VertexArray vertexArray;
	private Buffer arrayBufferObject;
	private Buffer indexBufferObject;

	private FloatBuffer arrayBuffer;
	private int vertex_count;
	
	private boolean buffer_mapped;

	
	public QuadRenderer() {
		FloatBuffer arrayBuffer = BufferUtils.createFloatBuffer(MAX_VERT * VertexData.componentCount());
		IntBuffer indexBuffer = BufferUtils.createIntBuffer(MAX_INDEX);
		buffer_mapped = false;
		vertex_count = 0;
		
		//TODO
		int index_counter = 0;
		for (int i = 0; i < MAX_QUADS; i++) {
			indexBuffer.put(index_counter + 0);
			indexBuffer.put(index_counter + 1);
			indexBuffer.put(index_counter + 2);
			indexBuffer.put(index_counter + 0);
			indexBuffer.put(index_counter + 2);
			indexBuffer.put(index_counter + 3);
			
			index_counter += 4;
		}
		indexBuffer.flip();
		
		vertexArray = new VertexArray();
		arrayBufferObject = new Buffer(arrayBuffer, GL20.GL_ARRAY_BUFFER, VertexData.componentCount(), GL15.GL_DYNAMIC_DRAW);
		indexBufferObject = new Buffer(indexBuffer, GL40.GL_ELEMENT_ARRAY_BUFFER, VertexData.componentCount(), GL15.GL_STATIC_DRAW);
		
		GL20.glEnableVertexAttribArray(0);
		GL20.glVertexAttribPointer(0, 3, GL11.GL_FLOAT, false, VertexData.sizeof(), VertexData.attribPos());
		GL20.glEnableVertexAttribArray(1);
		GL20.glVertexAttribPointer(1, 2, GL11.GL_FLOAT, false, VertexData.sizeof(), VertexData.attribUV());
		GL20.glEnableVertexAttribArray(2);
		GL20.glVertexAttribPointer(2, 1, GL11.GL_FLOAT, false, VertexData.sizeof(), VertexData.attribTex());
		GL20.glEnableVertexAttribArray(3);
		GL20.glVertexAttribPointer(3, 4, GL11.GL_FLOAT, false, VertexData.sizeof(), VertexData.attribCol());

	}

	@Override
	public void begin() {
		arrayBuffer = arrayBufferObject.mapBuffer().asFloatBuffer();
		arrayBuffer.clear();
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
	public void submit(Vector3f _pos, Vector2f _size, int _id, Vector4f _color) {
		submitVertex(new VertexData(_pos.x, 			_pos.y, 			_pos.z, 0.0f, 0.0f, _id, _color.x, _color.y, _color.z, _color.w));
		submitVertex(new VertexData(_pos.x, 			_pos.y + _size.y, 	_pos.z, 0.0f, 1.0f, _id, _color.x, _color.y, _color.z, _color.w));
		submitVertex(new VertexData(_pos.x + _size.x, 	_pos.y + _size.y, 	_pos.z, 1.0f, 1.0f, _id, _color.x, _color.y, _color.z, _color.w));
		submitVertex(new VertexData(_pos.x + _size.x, 	_pos.y, 			_pos.z, 1.0f, 0.0f, _id, _color.x, _color.y, _color.z, _color.w));
	}

	@Override
	public void clear() {
		vertex_count = 0;
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
		indexBufferObject.bind();
		
		//Actual drawing
		GL11.nglDrawElements(GL11.GL_TRIANGLES, vertex_count / 4 * 6, GL11.GL_UNSIGNED_INT, 0);
	}

	@Override
	public void draw(Texture texture) {
		draw(texture.getId(), texture.getType());
	}
	
}
