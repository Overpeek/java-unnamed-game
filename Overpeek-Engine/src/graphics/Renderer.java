package graphics;

import java.nio.ByteBuffer;

import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;

public class Renderer {

	public static class VertexData {
		float x, y, z;
		float u, v;
		float i;
		float r, g, b, a;

		VertexData(float _x, float _y, float _z, float _u, float _v, float _i, float _r, float _g, float _b, float _a) {
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
	private Buffer buffer;

	private ByteBuffer buffermap;
	private boolean m_buffer_mapped;
	private int buffer_current;
	private int vertex_count;
	private float buffer_data[] = new float[1000];

	public Renderer() {
		m_buffer_mapped = false;
		buffer_current = 0;
		vertex_count = 0;

		vertexArray = new VertexArray();
		buffer = new Buffer(buffer_data, VertexData.componentCount(), GL15.GL_DYNAMIC_DRAW);

		GL20.glEnableVertexAttribArray(0);
		GL20.glVertexAttribPointer(0, 3, GL11.GL_FLOAT, false, VertexData.sizeof(), VertexData.attribPos());
		GL20.glEnableVertexAttribArray(1);
		GL20.glVertexAttribPointer(1, 2, GL11.GL_FLOAT, false, VertexData.sizeof(), VertexData.attribUV());
		GL20.glEnableVertexAttribArray(2);
		GL20.glVertexAttribPointer(2, 1, GL11.GL_FLOAT, false, VertexData.sizeof(), VertexData.attribTex());
		GL20.glEnableVertexAttribArray(3);
		GL20.glVertexAttribPointer(3, 4, GL11.GL_FLOAT, false, VertexData.sizeof(), VertexData.attribCol());

	}

	public void beginRendering() {
		buffermap = buffer.mapBuffer();
		m_buffer_mapped = true;
	}

	public void stopRendering() {
		if (m_buffer_mapped)
			buffer.unmapBuffer();
		m_buffer_mapped = false;
	}

	public void submitVertex(VertexData data) {
		if (!m_buffer_mapped)
			beginRendering();

		buffermap.putFloat((buffer_current + 0) * 4, data.x);
		buffermap.putFloat((buffer_current + 1) * 4, data.y);
		buffermap.putFloat((buffer_current + 2) * 4, data.z);
		buffermap.putFloat((buffer_current + 3) * 4, data.u);
		buffermap.putFloat((buffer_current + 4) * 4, data.v);
		buffermap.putFloat((buffer_current + 5) * 4, data.i);
		buffermap.putFloat((buffer_current + 6) * 4, data.r);
		buffermap.putFloat((buffer_current + 7) * 4, data.g);
		buffermap.putFloat((buffer_current + 8) * 4, data.b);
		buffermap.putFloat((buffer_current + 9) * 4, data.a);

		buffer_current += VertexData.componentCount();

		vertex_count++;
	}

	public void submitQuad(Vector3f _pos, Vector2f _size, int _id, Vector4f _color) {
		submitVertex(new VertexData(_pos.x, _pos.y, _pos.z, 0.0f, 0.0f, _id, _color.x, _color.y, _color.z, _color.w));
		submitVertex(new VertexData(_pos.x, _pos.y + _size.y, _pos.z, 0.0f, 1.0f, _id, _color.x, _color.y, _color.z, _color.w));
		submitVertex(new VertexData(_pos.x + _size.x, _pos.y + _size.y, _pos.z, 1.0f, 1.0f, _id, _color.x, _color.y, _color.z, _color.w));

		submitVertex(new VertexData(_pos.x, _pos.y, _pos.z, 0.0f, 0.0f, _id, _color.x, _color.y, _color.z, _color.w));
		submitVertex(new VertexData(_pos.x + _size.x, _pos.y + _size.y, _pos.z, 1.0f, 1.0f, _id, _color.x, _color.y, _color.z, _color.w));
		submitVertex(new VertexData(_pos.x + _size.x, _pos.y, _pos.z, 1.0f, 0.0f, _id, _color.x, _color.y, _color.z, _color.w));
	}

	public void draw(int texture, int textureType) {
		if (vertex_count == 0)
			return;

		stopRendering();

		GL11.glEnable(GL11.GL_DEPTH_TEST);

		// Binding
		GL13.glActiveTexture(GL13.GL_TEXTURE0);
		GL11.glBindTexture(textureType, texture);
		vertexArray.bind();

		GL11.glDrawArrays(GL11.GL_TRIANGLES, 0, vertex_count);
		buffer_current = 0;
		vertex_count = 0;

		GL11.glDisable(GL11.GL_DEPTH_TEST);
	}
}
