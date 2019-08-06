package graphics;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL40;

import utility.Colors;
import utility.vec2;
import utility.vec3;
import utility.vec4;

public class QuadRenderer implements GenericRenderer {
	
	private int MAX_QUADS;
	private int MAX_VERT;
	private int MAX_INDEX;

	private VertexArray vertexArray;
	private Buffer arrayBufferObject;
	private Buffer indexBufferObject;

	private FloatBuffer arrayBuffer;
	private int vertex_count;
	
	private boolean buffer_mapped;

	
	
	public int getPrimitiveCount() {
		return vertex_count / 4;
	}
	
	public static QuadRenderer defaultQuads() {
		return new QuadRenderer(1000000);
	}
	
	public static QuadRenderer maxQuads(int max_quads) {
		return new QuadRenderer(max_quads);
	}
	
	private QuadRenderer(int max_quads) {
		MAX_QUADS = max_quads;
		MAX_VERT = MAX_QUADS * 4;
		MAX_INDEX = MAX_QUADS * 6;
		
		arrayBuffer = BufferUtils.createFloatBuffer(MAX_VERT * VertexData.componentCount);
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
		arrayBufferObject = new Buffer(arrayBuffer, GL20.GL_ARRAY_BUFFER, VertexData.componentCount, GL15.GL_DYNAMIC_DRAW);
		indexBufferObject = new Buffer(indexBuffer, GL40.GL_ELEMENT_ARRAY_BUFFER, VertexData.componentCount, GL15.GL_STATIC_DRAW);
		
		GL20.glEnableVertexAttribArray(0);
		GL20.glVertexAttribPointer(0, 3, GL11.GL_FLOAT, false, VertexData.sizeof, VertexData.attribPos);
		GL20.glEnableVertexAttribArray(1);
		GL20.glVertexAttribPointer(1, 2, GL11.GL_FLOAT, false, VertexData.sizeof, VertexData.attribUV);
		GL20.glEnableVertexAttribArray(2);
		GL20.glVertexAttribPointer(2, 1, GL11.GL_FLOAT, false, VertexData.sizeof, VertexData.attribTex);
		GL20.glEnableVertexAttribArray(3);
		GL20.glVertexAttribPointer(3, 4, GL11.GL_FLOAT, false, VertexData.sizeof, VertexData.attribCol);
		
		submit(new vec3(0.0f), new vec2(0.1f), 0, Colors.WHITE);
		draw();
		clear();
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
	
	public void overrideSubmitVertex(VertexData data, int position) {
		if (!buffer_mapped) begin();
		
		for (int i = 0; i < 10; i++) {
			arrayBuffer.put(position * VertexData.componentCount + i, data.get()[i]);
		}
	}
	
	public void overrideQuadCount(int count) {
		vertex_count = count * 6;
	}

	@Override
	public void submit(vec3 _pos, vec2 _size, int _id, vec4 _color) {
		submitVertex(new VertexData(_pos.x, 			_pos.y, 			_pos.z, 0.0f, 0.0f, _id, _color.x, _color.y, _color.z, _color.w));
		submitVertex(new VertexData(_pos.x, 			_pos.y + _size.y, 	_pos.z, 0.0f, 1.0f, _id, _color.x, _color.y, _color.z, _color.w));
		submitVertex(new VertexData(_pos.x + _size.x, 	_pos.y + _size.y, 	_pos.z, 1.0f, 1.0f, _id, _color.x, _color.y, _color.z, _color.w));
		submitVertex(new VertexData(_pos.x + _size.x, 	_pos.y, 			_pos.z, 1.0f, 0.0f, _id, _color.x, _color.y, _color.z, _color.w));
	}

	public void overrideSubmit(vec3 _pos, vec2 _size, int _id, vec4 _color, int position) {
		overrideSubmitVertex(new VertexData(_pos.x, 			_pos.y, 			_pos.z, 0.0f, 0.0f, _id, _color.x, _color.y, _color.z, _color.w), position * 4 + 0);
		overrideSubmitVertex(new VertexData(_pos.x, 			_pos.y + _size.y, 	_pos.z, 0.0f, 1.0f, _id, _color.x, _color.y, _color.z, _color.w), position * 4 + 1);
		overrideSubmitVertex(new VertexData(_pos.x + _size.x, 	_pos.y + _size.y, 	_pos.z, 1.0f, 1.0f, _id, _color.x, _color.y, _color.z, _color.w), position * 4 + 2);
		overrideSubmitVertex(new VertexData(_pos.x + _size.x, 	_pos.y, 			_pos.z, 1.0f, 0.0f, _id, _color.x, _color.y, _color.z, _color.w), position * 4 + 3);
	}

	@Override
	public void clear() {
		vertex_count = 0;
		arrayBuffer.clear();
	}

	public void drawFromCount(int texture, int textureType, int start, int count) {

		//Stop submitting
		if (buffer_mapped) end();
		
		//Return if no vertices to render
		if (vertex_count == 0)
			return;
		
		//Overflow
		if ((start + count) * 6.0f > vertex_count / 4.0f * 6.0f)
			return;

		// Binding
		GL13.glActiveTexture(GL13.GL_TEXTURE0);
		if (textureType != 0) GL11.glBindTexture(textureType, texture);
		vertexArray.bind();
		indexBufferObject.bind();
		//Actual drawing
		GL11.nglDrawElements(GL11.GL_TRIANGLES, count * 6, GL11.GL_UNSIGNED_INT, start * 6);
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

	@Override
	public void draw() {
		draw(0, 0);
	}
	
}
