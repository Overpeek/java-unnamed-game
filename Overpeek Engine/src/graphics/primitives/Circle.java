package graphics.primitives;

import utility.Maths;
import utility.vec2;
import utility.vec4;

public class Circle extends Primitive {

	public Circle(vec2 position, vec2 size, int texture, vec4 color) {
		vertexData = new VertexData[Primitives.Circle.vertex_count];
		
		vertexData[0] = new VertexData(position, new vec2(0.5f, 0.5f), texture, color);
		for (int i = 0; i < circle_res; i++)
		{
			float xoff = Maths.cos((float) (i / (float)circle_res * Maths.PI * 2.0f)) * size.x;
			float yoff = Maths.sin((float) (i / (float)circle_res * Maths.PI * 2.0f)) * size.y;

			vertexData[i + 1] = new VertexData(position.addLocal(xoff, yoff), new vec2(0.5f, 0.5f), texture, color);
		}
	}

	/*
	 * must have Primitives.Circle.vertex_count count of vertices
	 * **/
	public Circle(VertexData vertices[]) {
		vertexData = new VertexData[Primitives.Circle.vertex_count];
		
		vertexData = vertices;
	}

}
