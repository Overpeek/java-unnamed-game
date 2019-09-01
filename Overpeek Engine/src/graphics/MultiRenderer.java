package graphics;

import graphics.primitives.Primitive.Primitives;

public class MultiRenderer {
	
	public Renderer quads;
	public Renderer triangles;
	public Renderer lines;
	public Renderer points;
	public Renderer circles;
	
	public MultiRenderer() {
		quads = new Renderer(Primitives.Quad);
		triangles = new Renderer(Primitives.Triangle);
		lines = new Renderer(Primitives.Line);
		points = new Renderer(Primitives.Point);
		circles = new Renderer(Primitives.Circle);
	}

}
