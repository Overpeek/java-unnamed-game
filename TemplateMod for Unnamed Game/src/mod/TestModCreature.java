package mod;

import creatures.Creature;
import graphics.Renderer;

public class TestModCreature extends Creature {

	public TestModCreature(float _x, float _y) {
		super(_x, _y, "");
		// TODO Auto-generated constructor stub
	}

	@Override
	public void die() {
		commonDie();
	}

	@Override
	public void draw(Renderer renderer, float preupdate_scale) {
		commonDraw(renderer, preupdate_scale);
	}

	@Override
	public void update(int index, float ups) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void collide(float ups) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void ai(float ups) {
		// TODO Auto-generated method stub
		
	}
	
}
