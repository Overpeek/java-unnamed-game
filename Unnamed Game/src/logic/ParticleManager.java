package logic;

import java.util.ArrayList;

import graphics.Renderer;
import graphics.primitives.Quad;
import utility.Colors;
import utility.vec2;

public class ParticleManager {

	private static ArrayList<Particle> particles; 
	
	public static void init() {
		particles = new ArrayList<Particle>();
	}
	
	public static void add(Particle particle) {
		particles.add(particle);
	}
	
	public static void update(float ups) {
		for(int i = 0; i < particles.size(); i++) {
			particles.get(i).update(ups);
			
			if (particles.get(i).decayed()) {
				particles.remove(i);
			}
		}
	}
	
	public static void draw(Renderer renderer) {
		for(Particle p : particles) {
			float size = Main.game.renderScale() * CompiledSettings.TILE_SIZE;
			renderer.submit(new Quad(new vec2(p.pos.x - size/2.0f, p.pos.y - size/2.0f), new vec2(size), p.texture, Colors.WHITE));
		}
	}
	
}
