package creatures;

import graphics.Renderer;

public class Item extends Creature {

	public Item(float _x, float _y, int _id) {
		super(_x, _y, _id, true);
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
		//NO AI
	}

}
