package graphics;

public class Renderer {

	public QuadRenderer quads;
	public PointRenderer points;
	public TriangleRenderer triangles;
	public LineRenderer lines;
	public SphereRenderer spheres;
	
	public Renderer() {
		quads = new QuadRenderer();
		points = new PointRenderer();
		triangles = new TriangleRenderer();
		lines = new LineRenderer();
		spheres = new SphereRenderer();
	}
	
	public void clear() {
		quads.clear();
		points.clear();
		triangles.clear();
		lines.clear();
		spheres.clear();
	}
	
	public void draw(Texture texture) {
		quads.draw(texture);
		points.draw(texture);
		triangles.draw(texture);
		lines.draw(texture);
		spheres.draw(texture);
	}

	public void draw(int texture, int textureType) {
		quads.draw(texture, textureType);
		points.draw(texture, textureType);
		triangles.draw(texture, textureType);
		lines.draw(texture, textureType);
		spheres.draw(texture, textureType);
	}
	
}
