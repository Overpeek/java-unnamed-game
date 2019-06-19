package creatures;

import graphics.Renderer;

public class Zombie extends Creature {

	public Zombie(float _x, float _y) {
		super(_x, _y, 1, false);
	}

	@Override
	public void die() {
		commonDie();
	}

	@Override
	public void draw(Renderer renderer, float renderOffsetX, float renderOffsetY, float corrector, float renderScale) {
		commonDraw(renderer, renderOffsetX, renderOffsetY, corrector, renderScale);
	}

	@Override
	public void update(int index, float divider) {
		commonUpdate(divider);
	}

	@Override
	public void collide(float divider) {
		commonCollide(divider);
	}

	@Override
	public void ai(float divider) {
		commonMeleeAi(divider);
	}

}
