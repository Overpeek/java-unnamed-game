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
	
}
