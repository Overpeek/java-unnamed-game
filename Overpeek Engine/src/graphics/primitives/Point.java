package graphics.primitives;

import utility.vec2;
import utility.vec4;

public class Point extends Primitive {

	public Point(vec2 position, int texture, vec4 color) {
		vertexData = new VertexData[1];
		
		vertexData[0] = new VertexData(position.x, position.y, 0.0f, 0.0f, texture, color.x, color.y, color.z, color.w);
	}

	public Point(VertexData vertices[]) {
		vertexData = new VertexData[1];

		vertexData = vertices;
	}

}
