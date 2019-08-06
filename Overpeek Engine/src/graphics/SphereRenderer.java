package graphics;

import utility.vec2;
import utility.vec3;
import utility.vec4;

public class SphereRenderer {

	private LineRenderer outline_renderer;
	private TriangleRenderer fill_renderer;
	

	
	public int getPrimitiveCount() {
		return outline_renderer.getPrimitiveCount() + fill_renderer.getPrimitiveCount();
	}
	
	public SphereRenderer() {
		outline_renderer = new LineRenderer();
		fill_renderer = new TriangleRenderer();
	}

	public void submit(vec3 pos, vec2 size, int resolution, int id, vec4 fill_color) {
		submit(pos, size, resolution, id, fill_color, new vec4(0.0f, 0.0f, 0.0f, 0.0f));
	}

	public void submit(vec3 pos, vec2 size, int resolution, int id, vec4 fill_color, vec4 line_color) {
		VertexData fillVertexData = new VertexData(pos, new vec2(0.0f, 0.0f), id, fill_color);
		VertexData lineVertexData = new VertexData(pos, new vec2(0.0f, 0.0f), id, line_color);

		for (int i = 0; i < resolution; i++)
		{
			//Center
			fill_renderer.submitVertex(fillVertexData);

			//First
			float xoff = (float) (Math.cos(i / (float)resolution * Math.PI * 2.0f) * size.x);
			float yoff = (float) (Math.sin(i / (float)resolution * Math.PI * 2.0f) * size.y);

			fillVertexData.x += xoff; lineVertexData.x += xoff;
			fillVertexData.y += yoff; lineVertexData.y += yoff;
			fill_renderer.submitVertex(fillVertexData);
			outline_renderer.submitVertex(lineVertexData);
			fillVertexData.x -= xoff; lineVertexData.x -= xoff;
			fillVertexData.y -= yoff; lineVertexData.y -= yoff;

			//Second
			xoff = (float) (Math.cos((i + 1) / (float)resolution * Math.PI * 2.0f) * size.x);
			yoff = (float) (Math.sin((i + 1) / (float)resolution * Math.PI * 2.0f) * size.y);

			fillVertexData.x += xoff; lineVertexData.x += xoff;
			fillVertexData.y += yoff; lineVertexData.y += yoff;
			fill_renderer.submitVertex(fillVertexData);
			outline_renderer.submitVertex(lineVertexData);
			fillVertexData.x -= xoff; lineVertexData.x -= xoff;
			fillVertexData.y -= yoff; lineVertexData.y -= yoff;
		}
	}

	public void clear() {
		outline_renderer.clear();
		fill_renderer.clear();
	}

	public void draw(Texture texture) {
		draw(texture.getId(), texture.getType());
	}

	public void draw(int texture, int textureType) {
		outline_renderer.draw(texture, textureType);
		fill_renderer.draw(texture, textureType);
	}

	public void draw() {
		draw(0, 0);
	}

}
