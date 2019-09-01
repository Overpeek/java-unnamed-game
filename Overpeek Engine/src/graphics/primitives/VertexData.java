package graphics.primitives;

import graphics.buffers.VertexBufferObject;
import utility.vec2;
import utility.vec3;
import utility.vec4;

public class VertexData {
	
	public static final int componentCount = 10;
	public static final int sizeof = componentCount * Float.BYTES;
	
	public vec3 position;
	public vec2 uv;
	public int texture;
	public vec4 color;
	
	public VertexData(vec3 position, vec2 uv, int texture, vec4 color) {
		this.position = position;
		this.uv = uv;
		this.texture = texture;
		this.color = color;
	}
	
	public VertexData(vec2 position, vec2 uv, int texture, vec4 color) {
		this.position = position.tovec3(0.0f);
		this.uv = uv;
		this.texture = texture;
		this.color = color;
	}
	
	public VertexData(float x, float y, float z, float u, float v, int texture, float r, float g, float b, float a) {
		this.position = new vec3(x, y, z);
		this.uv = new vec2(u, v);
		this.texture = texture;
		this.color = new vec4(r, g, b, a);
	}
	
	public VertexData(float x, float y, float u, float v, int texture, float r, float g, float b, float a) {
		this.position = new vec3(x, y, 0.0f);
		this.uv = new vec2(u, v);
		this.texture = texture;
		this.color = new vec4(r, g, b, a);
	}

	public float[] get() {
		float arr[] = new float[componentCount];
		arr[0] = position.x;
		arr[1] = position.y;
		arr[2] = position.z;
		arr[3] = uv.x;
		arr[4] = uv.y;
		arr[5] = texture;
		arr[6] = color.x;
		arr[7] = color.y;
		arr[8] = color.z;
		arr[9] = color.w;
		return arr;
	}
	
	@Override
	public String toString() {
		return "VertexData[" + new vec3(position.x, position.y, position.z).toString() + "," + new vec2(uv.x, uv.y).toString() + "," + texture + "," + new vec4(color.x, color.y, color.z, color.w).toString() + "]";
	}

	public static void configVBO(VertexBufferObject vbo) {
		vbo.attrib(0, 3, sizeof, 0 * Float.BYTES);
		vbo.attrib(1, 2, sizeof, 3 * Float.BYTES);
		vbo.attrib(2, 1, sizeof, 5 * Float.BYTES);
		vbo.attrib(3, 4, sizeof, 6 * Float.BYTES);
	}

}
