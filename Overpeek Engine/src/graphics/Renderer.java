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

public class Renderer {
	
	private static int MAX_QUADS = 10000;
	private static int MAX_VERT = MAX_QUADS * 4;
	private static int MAX_INDEX = MAX_QUADS * 6;

	public static class VertexData {
		float x, y, z;
		float u, v;
		float i;
		float r, g, b, a;

		public VertexData(float _x, float _y, float _z, float _u, float _v, float _i, float _r, float _g, float _b, float _a) {
			x = _x;
			y = _y;
			z = _z;
			u = _u;
			v = _v;
			i = _i;
			r = _r;
			g = _g;
			b = _b;
			a = _a;
		}

		public VertexData(Vector3f pos, Vector2f size, float i, Vector4f color) {
			this(pos.x, pos.y, pos.z, size.x, size.y, i, color.x, color.y, color.z, color.w);
		}
		
		public float[] get() {
			float arr[] = new float[componentCount()];
			arr[0] = x;
			arr[1] = y;
			arr[2] = z;
			arr[3] = u;
			arr[4] = v;
			arr[5] = i;
			arr[6] = r;
			arr[7] = g;
			arr[8] = b;
			arr[9] = a;
			return arr;
		}

		static int sizeof() {
			return 10 * 4;
		}

		static int componentCount() {
			return 10;
		}

		static int attribPos() {
			return 0 * 4;
		}

		static int attribUV() {
			return 3 * 4;
		}

		static int attribTex() {
			return 5 * 4;
		}

		static int attribCol() {
			return 6 * 4;
		}
		
		
	}

	private VertexArray vertexArray;
	private Buffer arrayBufferObject;
	private Buffer indexBufferObject;

	private FloatBuffer arrayBuffer;
	
	private boolean buffer_mapped;

	public Renderer() {
		FloatBuffer arrayBuffer = BufferUtils.createFloatBuffer(MAX_VERT * VertexData.componentCount());
		IntBuffer indexBuffer = BufferUtils.createIntBuffer(MAX_INDEX);
		buffer_mapped = false;
		
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
	
	public void begin() {
		arrayBuffer = arrayBufferObject.mapBuffer().asFloatBuffer();
		arrayBuffer.clear();
		buffer_mapped = true;
	}
	
	public void end() {
		arrayBufferObject.unmapBuffer();
		buffer_mapped = false;
	}

	public void submitVertex(VertexData data) {
		if (!buffer_mapped) begin();
		
		arrayBuffer.put(data.get());
	}
	
	public void submitBakedText(Vector3f _pos, Vector2f _size, TextLabelTexture label, Vector4f _color) {
		submitQuad(_pos, new Vector2f(_size.x * label.getFrameBuffer().aspect(), _size.y), 0, _color);
		draw(label.getFrameBuffer().getTexture().getId(), label.getFrameBuffer().getTexture().getType());
	}

	public void submitQuad(Vector3f _pos, Vector2f _size, int _id, Vector4f _color) {
		submitVertex(new VertexData(_pos.x, 			_pos.y, 			_pos.z, 0.0f, 0.0f, _id, _color.x, _color.y, _color.z, _color.w));
		submitVertex(new VertexData(_pos.x, 			_pos.y + _size.y, 	_pos.z, 0.0f, 1.0f, _id, _color.x, _color.y, _color.z, _color.w));
		submitVertex(new VertexData(_pos.x + _size.x, 	_pos.y + _size.y, 	_pos.z, 1.0f, 1.0f, _id, _color.x, _color.y, _color.z, _color.w));
		submitVertex(new VertexData(_pos.x + _size.x, 	_pos.y, 			_pos.z, 1.0f, 0.0f, _id, _color.x, _color.y, _color.z, _color.w));
	}

	public void draw(int texture, int textureType) {
		int vertex_count = arrayBuffer.position() / VertexData.componentCount();
		
		//Stop submitting
		if (buffer_mapped) end();
		
		//Return if no vertices to render
		if (vertex_count == 0)
			return;

		// Binding
		GL13.glActiveTexture(GL13.GL_TEXTURE0);
		GL11.glBindTexture(textureType, texture);
		vertexArray.bind();
		indexBufferObject.bind();
		
		//Actual drawing
		GL11.nglDrawElements(GL11.GL_TRIANGLES, MAX_INDEX, GL11.GL_UNSIGNED_INT, 0);
	}
}
