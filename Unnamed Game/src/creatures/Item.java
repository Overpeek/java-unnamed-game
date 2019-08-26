package creatures;


import graphics.Renderer;
import graphics.primitives.VertexData;
import logic.Database;
import logic.Main;
import logic.CompiledSettings;
import utility.Colors;
import utility.vec2;
import utility.vec3;

public class Item extends Creature {

	@Override
	public Creature construct(float x, float y, String id) {
		return commonConstruct(x, y, id);
	}

	@Override
	public void die() {
		commonDie();
	}

	@Override
	public void draw(Renderer renderer, float preupdate_scale) {
		vec3 pos = new vec3(
				(getPos().x + getVel().x * preupdate_scale / CompiledSettings.UPDATES_PER_SECOND - 0.5f) * CompiledSettings.TILE_SIZE, 
				(getPos().x + getVel().y * preupdate_scale / CompiledSettings.UPDATES_PER_SECOND - 0.5f) * CompiledSettings.TILE_SIZE, 
				0.0f);
		vec2 size = new vec2(CompiledSettings.TILE_SIZE);
		pos.mult(Main.game.renderScale());
		size.mult(Main.game.renderScale());

		int texture = Database.getItem(id).texture;
		renderer.points.submitVertex(new VertexData(pos, size, texture, Colors.WHITE));
	}

	@Override
	public void update(float ups) {
		commonUpdate(ups);
	}

	@Override
	public void collide(float ups) {
		commonCollide(ups);
	}

	@Override
	public void ai(float ups) {
		//NO AI
	}

}
