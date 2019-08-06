package logic;

import utility.vec2;

public class Particle {
	
	public vec2 pos;
	public String particle;
	public int texture;
	
	public float decay_timer;
	
	public Particle(vec2 pos, String particle, int texture_add) {
		this.decay_timer = 0.0f;
		this.pos = pos;
		this.texture = Database.getParticle(particle).texture + texture_add;
		this.particle = particle;
		
		ParticleManager.add(this);
	}
	
	public void update(float ups) {
		decay_timer += 1.0f / ups;
	}
	
	public boolean decayed() {
		return decay_timer > Database.getParticle(particle).life_time;
	}

}
