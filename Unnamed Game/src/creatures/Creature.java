package creatures;

import java.util.ArrayList;

import graphics.Renderer;
import logic.Database;
import logic.Database.Creature_Data;
import logic.Main;
import logic.Particle;
import logic.ParticleManager;
import logic.Settings;
import utility.Colors;
import utility.vec2;
import utility.vec3;
import world.Map;

public abstract class Creature {
	
	private static final float PLAYER_WIDTH = 0.8f;
	private static final float PLAYER_HEIGHT = 0.8f;

	protected String id;

	private float health;
	private float stamina;

	private float maxHealth;
	private float maxStamina;

	private float healthGainRate;
	private float staminaGainRate;

	private float staminaRegenCooldown;
	private float healthRegenCooldown;

	private vec2 pos;
	private vec2 old_pos;
	private vec2 vel;
	private vec2 acc;

	private char heading;
	private boolean item;

	
	/*
	 * ABSTRACT METHODS
	 */
	/**args == item id if item*/
	public abstract Creature construct(float x, float y, String args);

	public abstract void die();
	
	public abstract void draw(Renderer renderer, float preupdate_scale);
	
	public abstract void update(float ups);
	
	public abstract void collide(float ups);
	
	public abstract void ai(float ups);
	
	/*
	 * COMMON METHODS FOR ABSTRACT CALLS
	 */
	protected Creature commonConstruct(float x, float y, String id) {
		this.id = id;
		setPos(new vec2(x + 0.5f, y + 0.5f));
		setVel(new vec2(0.0f, 0.0f));
		setAcc(new vec2(0.0f, 0.0f));

		maxHealth = getData().health;
		maxStamina = getData().stamina;
		healthGainRate = getData().healthgain;
		staminaGainRate = getData().staminagain;

		staminaRegenCooldown = 0;
		healthRegenCooldown = 0;

		resetHealth();
		resetStamina();
		
		//Check if item object
		item = false;
		if (this instanceof Item) item = true;
		
		return this;
	}
	
	protected void commonDie() {
		//Drops
		for (int i = 0; i < getData().drops.size(); i++) {
			//TODO:
			//Main.game.getMap().addCreature(getPos().x, getPos().y, getData().drops.get(i).item, true);
		}

		//Remove this
		Main.game.getMap().removeCreature(this);
	}
	
	protected void commonDraw(Renderer renderer, float preupdate_scale) {
		int heading_texture = getData().texture + heading;
		vec3 pos = new vec3(
				(getPos().x + getVel().x * preupdate_scale / Settings.UPDATES_PER_SECOND - 0.5f) * Settings.TILE_SIZE, 
				(getPos().y + getVel().y * preupdate_scale / Settings.UPDATES_PER_SECOND - 0.5f) * Settings.TILE_SIZE, 
				0.0f);
		vec2 size = new vec2(Settings.TILE_SIZE);
		pos.mult(Main.game.renderScale());
		size.mult(Main.game.renderScale());

		renderer.quads.submit(pos, size, heading_texture, Colors.WHITE);
	}
	
	protected void commonMeleeAi(float ups) {}
	
	protected void commonUpdate(float ups) {
		float oneOverups = 1.0f / ups;
		
		setHeading(getAcc().x, getAcc().y);
		
		ai(ups);
		
		//Vectorplate
		if (Main.game.getMap().getTile((int) getPos().x, (int) getPos().y).object == 7) {
			//setAcc(new vec2(getAcc().x, getAcc().y + 1.0f));
		}
	
		//Health and stamina regeneration
		if (staminaRegenCooldown > 2.0f) stamina += staminaGainRate;
		else staminaRegenCooldown += oneOverups;
	
		if (healthRegenCooldown > 2.0f) health += healthGainRate;
		else healthRegenCooldown += oneOverups;
	
	
		clampHPAndSTA();
		if (health <= 0) {
			die();
			return;
		}
	
		//Positions
		getVel().add( getAcc() );
		//setOld_pos( getPos() );
		getPos().add( getVel().mult(oneOverups) );
		//getVel().mult( 1.0f - (oneOverups / 10.0f) );
		setAcc( new vec2(0.0f) );
		
		collide(ups);
	}
		
	protected void commonCollide(float ups) {
		if (Database.getCreature(id).ghost) return;
	
		for (int _x = -1; _x < 2; _x++)
		{
			for (int _y = -1; _y < 2; _y++)
			{
				int tilex = (int)Math.round(getPos().x) + _x;
				int tiley = (int)Math.round(getPos().y) + _y;
				Map.MapTile tile = Main.game.getMap().getTile(tilex, tiley);
				if (tile == null) continue;
				if (Database.getObject(tile.object).wall) {
					boolean top = false, bottom = false;
					boolean left = false, right = false;
					float x_to_move = 0;
					float y_to_move = 0;
	
					//LEFT COLLIDER
					if (AABB(
						new vec2(getPos().x - PLAYER_WIDTH / 2.0f, getPos().y - PLAYER_HEIGHT / 2.0f + 0.3f),
						new vec2(PLAYER_WIDTH / 2.0f, PLAYER_HEIGHT - 0.6f),
						new vec2(tilex, tiley),
						new vec2(1.0f, 1.0f)
					)) {
						left = true;
						x_to_move = ((float)tilex + 1.0f + (PLAYER_WIDTH / 2.0f)) - getPos().x;
					}
					
					//RIGHT COLLIDER
					if (AABB(
						new vec2(getPos().x, getPos().y - PLAYER_HEIGHT / 2.0f + 0.3f),
						new vec2(PLAYER_WIDTH / 2.0f, PLAYER_HEIGHT - 0.6f),
						new vec2(tilex, tiley),
						new vec2(1.0f, 1.0f)
					)) {
						right = true;
						x_to_move = ((float)tilex - (PLAYER_WIDTH / 2.0f)) - getPos().x;
					}
					//TOP COLLIDER
					if (AABB(
						new vec2(getPos().x - PLAYER_WIDTH / 2.0f + 0.3f, getPos().y - PLAYER_HEIGHT / 2.0f),
						new vec2(PLAYER_WIDTH - 0.6f, PLAYER_HEIGHT / 2.0f),
						new vec2(tilex, tiley),
						new vec2(1.0f, 1.0f)
					)) {
						top = true;
						y_to_move = ((float)tiley + 1.0f + (PLAYER_HEIGHT / 2.0f)) - getPos().y;
					}
					//BOTTOM COLLIDER
					if (AABB(
						new vec2(getPos().x - PLAYER_WIDTH / 2.0f + 0.3f, getPos().y),
						new vec2(PLAYER_WIDTH - 0.6f, PLAYER_HEIGHT / 2.0f),
						new vec2(tilex, tiley),
						new vec2(1.0f, 1.0f)
					)) {
						bottom = true;
						y_to_move = ((float)tiley - PLAYER_HEIGHT / 2.0f) - getPos().y;
					}
	
					if (top != bottom) { getPos().add(0.0f, y_to_move); getVel().mult(1.0f, 0.0f); }
					if (left != right) { getPos().add(x_to_move, 0.0f);getVel().mult(0.0f, 1.0f); }
				}
			}
		}
	}

	
	/*
	 * HELPER FUNCTIONS
	 */
	public void hit() {
		if (item) return;
		float hitx = getPos().x, hity = getPos().y;
		switch (heading)
		{
		case 0:
			hity -= 1;
			break;
		case 1:
			hitx -= 1;
			break;
		case 2:
			hity += 1;
			break;
		default:
			hitx += 1;
			break;
		}

		// Swing particle
		float swingX = 0.0f, swingY = 0.0f;
		switch (heading)
		{
		case 0: // Swing to up
			swingX =  0.0f;
			swingY = -0.5f;
			break;
		case 1: // Swing to down
			swingX =  0.0f;
			swingY =  0.5f;
			break;
		case 2: // Swing to left
			swingX = -0.5f;
			swingY =  0.0f;
			break;
		default: // Swing to right
			swingX =  0.5f;
			swingY =  0.0f;
			break;
		}
		swingX *= Main.game.renderScale();
		swingY *= Main.game.renderScale();
		vec3 swing_pos = new vec3(getPos().x + swingX, getPos().y + swingY, 0.0f).mult(Settings.TILE_SIZE);
		ParticleManager.add(new Particle(swing_pos.vec2(), "swing", heading));
		

		//Creature hitting
		ArrayList<Creature> in_radius_creatures = Main.game.getMap().findAllCreatures(hitx, hity, 1.0f);
		for (int i = 0; i < in_radius_creatures.size(); i++)
		{
			if (in_radius_creatures.get(i).equals(this)) continue;

			vec2 directionVector = new vec2(
					in_radius_creatures.get(i).getPos().x - getPos().x, 
					in_radius_creatures.get(i).getPos().y - getPos().y);
			directionVector.normalizeNew();
		
			in_radius_creatures.get(i).getAcc().x += directionVector.x / 10.0f * (getData().knockback);
			in_radius_creatures.get(i).getAcc().y += directionVector.y / 10.0f * (getData().knockback);
			in_radius_creatures.get(i).setHealth(in_radius_creatures.get(i).getHealth() - getData().meleeDamage);
		}

		//Tile hitting
		//Map.MapTile tmp = Main.game.getMap().getTile(Math.round(hitx), Math.round(hity));
		//if (tmp != null) {
		//	if (Database.getObject(tmp.object).destroyable) {
		//		Main.game.getMap().hit(Math.round(hitx), Math.round(hity), (int) getData().meleeDamage);
		//	}
		//}

		//Swing noise
		Database.getSound("hit");
	}
	
	public void setHeading(float x, float y) {
		//Heads towards where its going
		if (Math.abs(x) > Math.abs(y)) {
			if (x < 0) {
				heading = 2;
			}
			else {
				heading = 3;
			}
		}
		else {
			if (y < 0) {
				heading = 0;
			}
			else {
				heading = 1;
			}
		}
	}
	
	public boolean canSee(float _x, float _y) {
		if (Main.game.advancedDebugMode) return false;
		//LineABXY from this enemy to x,y

		for (int x = -Settings.MAP_WORK_DST; x < Settings.MAP_WORK_DST; x++)
		{
			for (int y = -Settings.MAP_WORK_DST; y < Settings.MAP_WORK_DST; y++)
			{
				Map.MapTile tile = Main.game.getMap().getTile((int) (x + Math.floor(getPos().x)), (int) (y + Math.floor(getPos().y)));

				if (Database.getObject(tile.object).wall) {
					if (lineLine(
						new vec2(getPos().x, getPos().y), 
						new vec2(_x, _y),
						new vec2((int) (x + Math.floor(getPos().x)), (float) (y + Math.floor(getPos().y))), 
						new vec2((int) (x + Math.floor(getPos().x)), (float) (y + Math.floor(getPos().y) + 1.0f))
					)) {
						//oe::Logger::out("0");
						return false;
					}
					if (lineLine(
						new vec2(getPos().x, getPos().y),
						new vec2(_x, _y),
						new vec2((int) (x + Math.floor(getPos().x)), (float) (y + Math.floor(getPos().y))),
						new vec2((int) (x + Math.floor(getPos().x) + 1.0f), (float) (y + Math.floor(getPos().y)))
					)) {
						//oe::Logger::out("1");
						return false;
					}
					if (lineLine(
						new vec2(getPos().x, getPos().y),
						new vec2(_x, _y),
						new vec2((int) (x + Math.floor(getPos().x) + 1.0f), (float) (y + Math.floor(getPos().y) + 1.0f)),
						new vec2((int) (x + Math.floor(getPos().x)), (float) (y + Math.floor(getPos().y) + 1.0f))
					)) {
						//oe::Logger::out("2");
						return false;
					}
					if (lineLine(
						new vec2(getPos().x, getPos().y),
						new vec2(_x, _y),
						new vec2((int) (x + Math.floor(getPos().x) + 1.0f), (float) (y + Math.floor(getPos().y) + 1.0f)),
						new vec2((int) (x + Math.floor(getPos().x) + 1.0f), (float) (y + Math.floor(getPos().y)))
					)) {
						//oe::Logger::out("3");
						return false;
					}
				}
			}
		}

		return true;
	}
	
	protected boolean AABB(vec2 aPos, vec2 aSize, vec2 bPos, vec2 bSize) {
		return	bPos.x < aPos.x + aSize.x && aPos.x < bPos.x + bSize.x
			&&  bPos.y < aPos.y + aSize.y && aPos.y < bPos.y + bSize.y;
	}
	
	protected boolean lineLine(vec2 a, vec2 b, vec2 c, vec2 d)
	{
		float denominator = ((b.x - a.x) * (d.y - c.y)) - ((b.y - a.y) * (d.x - c.x));
		float numerator1 = ((a.y - c.y) * (d.x - c.x)) - ((a.x - c.x) * (d.y - c.y));
		float numerator2 = ((a.y - c.y) * (b.x - a.x)) - ((a.x - c.x) * (b.y - a.y));

		// Detect coincident lines (has a problem, read below)
		if (denominator == 0) return numerator1 == 0 && numerator2 == 0;

		float r = numerator1 / denominator;
		float s = numerator2 / denominator;

		return (r >= 0 && r <= 1) && (s >= 0 && s <= 1);
	}

	protected void clampHPAndSTA() {
		if (health > maxHealth) health = maxHealth;
		if (health < 0) health = 0;
		if (stamina > maxStamina) stamina = maxStamina;
		if (stamina < 0) stamina = 0;
	}

	protected void resetHealth() {
		health = maxHealth;
	}

	protected void resetStamina() {
		stamina = maxStamina;
	}

	/*
	 * GETTERS AND SETTERS
	 */
	public boolean isItem() {
		return item;
	}
	
	public float getMaxHealth() {
		return maxHealth;
	}
	
	public float getMaxStamina() {
		return maxStamina;
	}
	
	public void setHealth(float value) {
		health = value;
	}
	
	public float getHealth() {
		return health;
	}
	
	public float getStamina() {
		return stamina;
	}
	
	public String getId() {
		return id;
	}
	
	public Creature_Data getData() {
		return Database.getCreature(id);
	}

	public vec2 getPos() {
		return pos;
	}

	public void setPos(vec2 pos) {
		this.pos = pos;
	}

	public vec2 getOld_pos() {
		return old_pos;
	}

	public void setOld_pos(vec2 old_pos) {
		this.old_pos = old_pos;
	}

	public vec2 getVel() {
		return vel;
	}

	public void setVel(vec2 vel) {
		this.vel = vel;
	}

	public vec2 getAcc() {
		return acc;
	}

	public void setAcc(vec2 acc) {
		this.acc = acc;
	}
	
}
