package creatures;

import graphics.Renderer;
import graphics.VertexData;
import logic.Database;
import logic.Database.Creature_Data;
import logic.Main;
import logic.Settings;
import utility.Colors;
import utility.vec2;
import utility.vec3;
import world.Map;

public abstract class Creature {
	private static final int HEADING_UP = 0;
	private static final int HEADING_DOWN = 2;
	private static final int HEADING_LEFT = 3;
	private static final int HEADING_RIGHT = 1;
	private static final float PLAYER_WIDTH = 0.8f;
	private static final float PLAYER_HEIGHT = 0.8f;

	private float counterToRemoveSwingAnimation = 0;
	private int id;
	private int swingDir;

	private float health;
	private float stamina;

	private float maxHealth;
	private float maxStamina;

	private float healthGainRate;
	private float staminaGainRate;

	private float staminaRegenCooldown;
	private float healthRegenCooldown;

	private boolean item;

	private vec2 pos;
	private vec2 old_pos;
	private vec2 vel;
	private vec2 acc;

	private char heading;

	
	/*
	 * GENERIC CONSTRUCTOR
	 */
	public Creature(float _x, float _y, int _id, boolean _item) {
		id = _id;
		setPos(new vec2(_x + 0.5f, _y + 0.5f));
		setVel(new vec2(0.0f, 0.0f));
		setAcc(new vec2(0.0f, 0.0f));
		item = _item;
		swingDir = 0;

		// HS
		maxHealth = getData().health;
		maxStamina = getData().stamina;
		healthGainRate = getData().healthgain;
		staminaGainRate = getData().staminagain;

		staminaRegenCooldown = 0;
		healthRegenCooldown = 0;

		resetHealth();
		resetStamina();
	}

	
	/*
	 * ABSTRACT METHODS
	 */
	public abstract void die();
	
	public abstract void draw(Renderer renderer, float renderOffsetX, float renderOffsetY, float corrector, float renderScale);
	
	public abstract void update(int index, float divider);
	
	public abstract void collide(float divider);
	
	public abstract void ai(float divider);
	
	
	
	/*
	 * COMMON METHODS FOR ABSTRACT CALLS
	 */
	protected void commonDie() {
		//Drops
		for (int i = 0; i < getData().drops.size(); i++) {
			Main.game.getMap().addCreature(getPos().x, getPos().y, getData().drops.get(i).item, true);
		}

		//Remove this
		Main.game.getMap().removeCreature(this);
	}
	
	protected void commonDraw(Renderer renderer, float renderOffsetX, float renderOffsetY, float corrector, float renderScale ) {
		if (!item) {
			int heading_texture = 0;
			switch (heading)
			{
			case HEADING_UP:
				heading_texture = getData().texture;
				break;
			case HEADING_DOWN:
				heading_texture = getData().texture + 1;
				break;
			case HEADING_LEFT:
				heading_texture = getData().texture + 2;
				break;
			case HEADING_RIGHT:
				heading_texture = getData().texture + 3;
				break;
			default:
				break;
			}
			vec3 pos = new vec3((getPos().x + getVel().x * corrector / Settings.UPDATES_PER_SECOND + renderOffsetX - 0.5f) * Settings.TILE_SIZE, (getPos().y + getVel().y * corrector / Settings.UPDATES_PER_SECOND + renderOffsetY - 0.5f) * Settings.TILE_SIZE, 0.0f);
			vec2 size = new vec2(Settings.TILE_SIZE, Settings.TILE_SIZE);
			pos.mult(Main.game.renderScale());
			size.mult(Main.game.renderScale());

			renderer.points.submitVertex(new VertexData(pos, size, heading_texture, Colors.WHITE));

			float swingX = 0.0f, swingY = 0.0f;
			int swingTexture = 13;
			switch (swingDir)
			{
			case 1:
				swingTexture = 13;
				swingX = (getPos().x + getVel().x * corrector / Settings.UPDATES_PER_SECOND + renderOffsetX - 0.5f) * Settings.TILE_SIZE; 
				swingY = (getPos().y + getVel().y * corrector / Settings.UPDATES_PER_SECOND + renderOffsetY - 1.0f) * Settings.TILE_SIZE;
				break;
			case 2:
				swingTexture = 12;
				swingX = (getPos().x + getVel().x * corrector / Settings.UPDATES_PER_SECOND + renderOffsetX - 0.0f) * Settings.TILE_SIZE; 
				swingY = (getPos().y + getVel().y * corrector / Settings.UPDATES_PER_SECOND + renderOffsetY - 0.5f) * Settings.TILE_SIZE;
				break;
			case 3:
				swingTexture = 15;
				swingX = (getPos().x + getVel().x * corrector / Settings.UPDATES_PER_SECOND + renderOffsetX - 0.5f) * Settings.TILE_SIZE; 
				swingY = (getPos().y + getVel().y * corrector / Settings.UPDATES_PER_SECOND + renderOffsetY - 0.0f) * Settings.TILE_SIZE;
				break;
			case 4:
				swingTexture = 14;
				swingX = (getPos().x + getVel().x * corrector / Settings.UPDATES_PER_SECOND + renderOffsetX - 1.0f) * Settings.TILE_SIZE; 
				swingY = (getPos().y + getVel().y * corrector / Settings.UPDATES_PER_SECOND + renderOffsetY - 0.5f) * Settings.TILE_SIZE;
				break;
			default:
				break;
			}
			if (swingDir != 0) {
				pos = new vec3(swingX, swingY, 0.0f);
				size = new vec2(Settings.TILE_SIZE);
				pos.mult(Main.game.renderScale());
				size.mult(Main.game.renderScale());

				renderer.points.submitVertex(new VertexData(pos, size, swingTexture, Colors.WHITE));
			}
		}
		else {
			vec3 pos = new vec3((getPos().x + getVel().x * corrector / Settings.UPDATES_PER_SECOND + renderOffsetX - 0.5f) * Settings.TILE_SIZE, (getPos().x + getVel().y * corrector / Settings.UPDATES_PER_SECOND + renderOffsetY - 0.5f) * Settings.TILE_SIZE, 0.0f);
			vec2 size = new vec2(Settings.TILE_SIZE);
			pos.mult(Main.game.renderScale());
			size.mult(Main.game.renderScale());

			int texture = Database.getItem(id).texture;
			renderer.points.submitVertex(new VertexData(pos, size, texture, Colors.WHITE));
		}
	}
	
	protected void commonMeleeAi(float divider) {
		
	}
	
	protected void commonUpdate(float divider) {
		ai(divider);
		
		//Vectorplate
		if (Main.game.getMap().getTile((int) getPos().x, (int) getPos().y).object == 7) {
			setAcc(new vec2(getAcc().x, getAcc().y + 1.0f));
		}
	
		//Health and stamina regeneration
		if (staminaRegenCooldown > 2.0f) stamina += staminaGainRate;
		else staminaRegenCooldown += 1.0 / divider;
	
		if (healthRegenCooldown > 2.0f) health += healthGainRate;
		else healthRegenCooldown += 1.0 / divider;
	
	
		clampHPAndSTA();
		if (health <= 0) {
			die();
			return;
		}
	
		if (swingDir != 0) {
			float addition = 1.0f / divider;
			counterToRemoveSwingAnimation += addition;
		}
		if (counterToRemoveSwingAnimation > 0.10) {
			counterToRemoveSwingAnimation = 0;
			swingDir = 0;
		}
	
		//Positions
		setVel(getVel().add(getAcc()));
		setOld_pos(getPos());
		setPos(getPos().add(getVel().mult(1.0f / divider)));
		setVel(getVel().mult(1.0f - 1.0f / (divider / 10.0f)));
		setAcc(new vec2(0.0f));
	}
		
	protected void commonCollide(float divider) {
		if (!item && Database.getCreature(id).ghost) return;
	
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
	 * PRIVATE HELPER FUNCTIONS
	 */
	public boolean canSeePlayer(float _x, float _y) {
		if (Main.game.advancedDebugMode) return false;
		//LineABXY from this enemy to x,y

		//for all tiles
		//	if this tile has inpassable object
		//	for all object sides
		//	if LineABXY and this object side intersects
		//		return false
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
	
	public float getHealth() {
		return health;
	}
	
	public float getStamina() {
		return stamina;
	}
	
	public int getId() {
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
