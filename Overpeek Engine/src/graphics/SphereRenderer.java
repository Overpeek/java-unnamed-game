package graphics;

import graphics.primitives.Primitive.Primitives;
import utility.vec2;
import utility.vec4;

public class SphereRenderer {

	private Renderer outline_renderer;
	private Renderer fill_renderer;
	

	
	public SphereRenderer() {
		outline_renderer = new Renderer(Primitives.Line);
		fill_renderer = new Renderer(Primitives.Triangle);
	}

	public void submit(vec2 pos, vec2 size, int resolution, int id, vec4 fill_color) {
		submit(pos, size, resolution, id, fill_color, new vec4(0.0f, 0.0f, 0.0f, 0.0f));
	}

	public void submit(vec2 pos, vec2 size, int resolution, int id, vec4 fill_color, vec4 line_color) {
		VertexData fillVertexData = new VertexData(pos, new vec2(0.0f, 0.0f), id, fill_color);
		VertexData lineVertexData = new VertexData(pos, new vec2(0.0f, 0.0f), id, line_color);

		for (int i = 0; i < resolution; i++)
		{
			//Center
			fill_renderer.submit(fillVertexData);

			//First
			float xoff = (float) (Math.cos(i / (float)resolution * Math.PI * 2.0f) * size.x);
			float yoff = (float) (Math.sin(i / (float)resolution * Math.PI * 2.0f) * size.y);

			fillVertexData.x += xoff; lineVertexData.x += xoff;
			fillVertexData.y += yoff; lineVertexData.y += yoff;
			fill_renderer.submit(fillVertexData);
			outline_renderer.submit(lineVertexData);
			fillVertexData.x -= xoff; lineVertexData.x -= xoff;
			fillVertexData.y -= yoff; lineVertexData.y -= yoff;

			//Second
			xoff = (float) (Math.cos((i + 1) / (float)resolution * Math.PI * 2.0f) * size.x);
			yoff = (float) (Math.sin((i + 1) / (float)resolution * Math.PI * 2.0f) * size.y);

			fillVertexData.x += xoff; lineVertexData.x += xoff;
			fillVertexData.y += yoff; lineVertexData.y += yoff;
			fill_renderer.submit(fillVertexData);
			outline_renderer.submit(lineVertexData);
			fillVertexData.x -= xoff; lineVertexData.x -= xoff;
			fillVertexData.y -= yoff; lineVertexData.y -= yoff;
		}
	}

	public void clear() {
		outline_renderer.clear();
		fill_renderer.clear();
	}

	public void draw() {
		outline_renderer.draw();
		fill_renderer.draw();
	}

}
