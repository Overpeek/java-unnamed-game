package graphics.primitives;

import utility.vec2;
import utility.vec4;

public class Line extends Primitive {

	public Line(vec2 position0, vec2 position1, int texture, vec4 color) {
		vertexData = new VertexData[2];
		
		vertexData[0] = new VertexData(position0.x, position0.y, 0.0f, 0.0f, texture, color.x, color.y, color.z, color.w);
		vertexData[1] = new VertexData(position1.x, position1.y, 1.0f, 1.0f, texture, color.x, color.y, color.z, color.w);
	}

	public Line(VertexData vertices[]) {
		vertexData = new VertexData[2];

		vertexData = vertices;
	}

}
