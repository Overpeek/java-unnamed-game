package graphics;

import utility.vec2;
import utility.vec3;
import utility.vec4;

public class VertexData {
	
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

	public VertexData(vec3 pos, vec2 size, float i, vec4 color) {
		this(pos.x, pos.y, pos.z, size.x, size.y, i, color.x, color.y, color.z, color.w);
	}
	
	public float[] get() {
		float arr[] = new float[componentCount];
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

	public static final int componentCount = 	10;

	public static final int sizeof = componentCount * 4;

	public static final int attribPos = 		0 * 4;

	public static final int attribUV = 			3 * 4;

	public static final int attribTex = 		5 * 4;

	public static final int attribCol = 		6 * 4;
}
