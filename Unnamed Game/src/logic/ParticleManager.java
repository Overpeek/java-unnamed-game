package logic;

import java.util.ArrayList;

import graphics.Renderer;
import utility.Colors;
import utility.vec2;
import utility.vec3;

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
			float size = Main.game.renderScale() * Settings.TILE_SIZE;
			renderer.quads.submit(new vec3(p.pos.x - size/2.0f, p.pos.y - size/2.0f, 0.5f), new vec2(size), p.texture, Colors.WHITE);
		}
	}
	
}
