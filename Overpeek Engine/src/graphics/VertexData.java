package graphics;

import utility.vec2;
import utility.vec4;

public class VertexData {
	
	float x, y;
	float u, v;
	float i;
	float r, g, b, a;

	public VertexData(float _x, float _y, float _u, float _v, float _i, float _r, float _g, float _b, float _a) {
		x = _x;
		y = _y;
		u = _u;
		v = _v;
		i = _i;
		r = _r;
		g = _g;
		b = _b;
		a = _a;
	}

	public VertexData(vec2 pos, vec2 size, float i, vec4 color) {
		this(pos.x, pos.y, size.x, size.y, i, color.x, color.y, color.z, color.w);
	}
	
	public float[] get() {
		float arr[] = new float[componentCount];
		arr[0] = x;
		arr[1] = y;
		arr[2] = u;
		arr[3] = v;
		arr[4] = i;
		arr[5] = r;
		arr[6] = g;
		arr[7] = b;
		arr[8] = a;
		return arr;
	}
	
	@Override
	public String toString() {
		return "VertexData[" + new vec2(x, y).toString() + "," + new vec2(u, v).toString() + "," + i + "," + new vec4(r, g, b, a).toString() + "]";
	}

	public static final int componentCount = 	9;
	public static final int sizeof = 			componentCount * Float.BYTES;

	public static final int attribPos = 		0 * Float.BYTES;
	public static final int attribUV = 			2 * Float.BYTES;
	public static final int attribTex = 		4 * Float.BYTES;
	public static final int attribCol = 		5 * Float.BYTES;
}
