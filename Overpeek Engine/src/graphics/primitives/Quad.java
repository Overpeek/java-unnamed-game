package graphics.primitives;

import utility.vec2;
import utility.vec4;

public class Quad extends Primitive {

	public Quad(vec2 position, vec2 size, int texture, vec4 color) {
		vertexData = new VertexData[4];
		
		vertexData[0] = new VertexData(position.x, 				position.y,				0.0f, 0.0f, texture, color.x, color.y, color.z, color.w);
		vertexData[1] = new VertexData(position.x, 				position.y + size.y, 	0.0f, 1.0f, texture, color.x, color.y, color.z, color.w);
		vertexData[2] = new VertexData(position.x + size.x, 	position.y + size.y, 	1.0f, 1.0f, texture, color.x, color.y, color.z, color.w);
		vertexData[3] = new VertexData(position.x + size.x, 	position.y,	 			1.0f, 0.0f, texture, color.x, color.y, color.z, color.w);
	}

	public Quad(vec2 position, vec2 size, float angle, int texture, vec4 color) {
		vertexData = new VertexData[4];
		vec2 origin = new vec2(position.x, position.y);
		
		vec2 point0 = new vec2(position.x - size.x / 2.0f, position.y - size.y / 2.0f);
		point0 = vec2.rotate(origin, point0, angle);
		vec2 point1 = new vec2(position.x - size.x / 2.0f, position.y + size.y / 2.0f);
		point1 = vec2.rotate(origin, point1, angle);
		vec2 point2 = new vec2(position.x + size.x / 2.0f, position.y + size.y / 2.0f);
		point2 = vec2.rotate(origin, point2, angle);
		vec2 point3 = new vec2(position.x + size.x / 2.0f, position.y - size.y / 2.0f);
		point3 = vec2.rotate(origin, point3, angle);
		
		vertexData[0] = new VertexData(point0.x, point0.y,	0.0f, 0.0f, texture, color.x, color.y, color.z, color.w);
		vertexData[1] = new VertexData(point1.x, point1.y, 	0.0f, 1.0f, texture, color.x, color.y, color.z, color.w);
		vertexData[2] = new VertexData(point2.x, point2.y, 	1.0f, 1.0f, texture, color.x, color.y, color.z, color.w);
		vertexData[3] = new VertexData(point3.x, point3.y, 	1.0f, 0.0f, texture, color.x, color.y, color.z, color.w);
	}

	public Quad(VertexData vertices[]) {
		vertexData = new VertexData[4];

		vertexData = vertices;
	}

}
