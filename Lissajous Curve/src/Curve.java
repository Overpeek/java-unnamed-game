import graphics.*;
import utility.*;

public class Curve {

	vec2[] tail;
	int tail_len = 100;
	float t;
	vec2 hz;
	vec2 pos;
	float size;
	vec2 current_pos;
	SimplexNoise_octave octavenoise;
	
	public Curve(vec2 pos, float size, vec2 hz) {
		this.hz = hz;
		this.size = size;
		this.pos = pos;
		
		tail = new vec2[tail_len];
		t = 0.0f;
		current_pos = new vec2(0.0f);
		//int seed = (int) (4738 * hz.x - 89350 * hz.y);
		int seed = (int) System.currentTimeMillis();
		octavenoise = new SimplexNoise_octave(seed);
	}
	
	public void update() {
		t = Main.;
		//current_pos = new vec2(
		//		(float)(Math.cos(t * hz.x) * size), 
		//		(float)(Math.sin(t * hz.y) * size)
		//).add(pos);
		current_pos = new vec2(
				(float)(octavenoise.noiseOctave((float) (t * hz.x + Math.PI), (float) (100.0f + Math.PI), 1, 1.0f, 1.0f)) * size, 
				(float)(octavenoise.noiseOctave((float) (100.0f + Math.PI), (float) (t * hz.y + Math.PI), 1, 1.0f, 1.0f)) * size
		).add(pos);
		//Logger.out(current_pos);
		
		
		for (int i = tail_len - 1; i > 0; i--) tail[i] = tail[i - 1];
		tail[0] = current_pos;
	}
	
	public void draw(Renderer renderer) {
		//renderer.spheres.submit(new vec3(0.0f), new vec2(size), 64, 0, Colors.TRANSPARENT, Colors.WHITE);
		//renderer.spheres.submit(new vec3(current_pos, 0.0f), new vec2(0.01f), 64, 0, Colors.WHITE);
		
		//Tail draw
		for (int i = 0; i < tail_len - 1; i++) {
			if (tail[i] != null && tail[i + 1] != null) {
				renderer.lines.submitVertex(new VertexData(new vec3(tail[i], 0.0f), new vec2(0.0f), 0, new vec4(1.0f, 1.0f, 1.0f, Maths.map(i, 0.0f, tail_len, 1.0f, 0.0f))));
				renderer.lines.submitVertex(new VertexData(new vec3(tail[i + 1], 0.0f), new vec2(0.0f), 0, new vec4(1.0f, 1.0f, 1.0f, Maths.map(i + 1, 0.0f, tail_len, 1.0f, 0.0f))));
			}
		}
	}
	
}
