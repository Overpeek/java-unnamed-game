package creatures;

import org.joml.Vector2f;
import org.joml.Vector3f;

import graphics.Renderer;
import graphics.Renderer.VertexData;
import logic.Database;
import logic.Database.Creature_Data;
import utility.Colors;
import logic.Game;
import logic.Settings;

public class Creature {
	private static final int HEADING_UP = 0;
	private static final int HEADING_DOWN = 2;
	private static final int HEADING_LEFT = 3;
	private static final int HEADING_RIGHT = 1;

	private float counterToRemoveSwingAnimation = 0;
	private int id;
	private int swingDir;
	private boolean bumping;

	private float health;
	private float stamina;

	private float maxHealth;
	private float maxStamina;

	private float healthGainRate;
	private float staminaGainRate;

	private float staminaRegenCooldown;
	private float healthRegenCooldown;

	private boolean item;

	private float x, y;
	private float old_x, old_y;
	private float vel_x, vel_y;
	private float acc_x, acc_y;
	
	public float getX() {
		return x;
	}

	public float getY() {
		return y;
	}
	
	public float getOld_x() {
		return old_x;
	}

	public void setOld_x(float old_x) {
		this.old_x = old_x;
	}

	public float getOld_y() {
		return old_y;
	}

	public void setOld_y(float old_y) {
		this.old_y = old_y;
	}

	public float getVel_x() {
		return vel_x;
	}

	public void setVel_x(float vel_x) {
		this.vel_x = vel_x;
	}

	public float getVel_y() {
		return vel_y;
	}

	public void setVel_y(float vel_y) {
		this.vel_y = vel_y;
	}

	public float getAcc_x() {
		return acc_x;
	}

	public void setAcc_x(float acc_x) {
		this.acc_x = acc_x;
	}

	public float getAcc_y() {
		return acc_y;
	}

	public void setAcc_y(float acc_y) {
		this.acc_y = acc_y;
	}

	public void setX(float x) {
		this.x = x;
	}

	public void setY(float y) {
		this.y = y;
	}

	private char heading;

	public Creature(float _x, float _y, int _id, boolean _item) {
		id = _id;
		x = _x + 0.5f;
		y = _y + 0.5f;
		item = _item;
		vel_x = 0;
		vel_y = 0;
		acc_x = 0;
		acc_y = 0;
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
	
	public void die() {
		//Drops
		for (int i = 0; i < getData().drops.size(); i++) {
			Game.getMap().addCreature(getX(), getY(), getData().drops.get(i).id, true);
		}

		//Remove this
		Game.getMap().removeCreature(this);
	}

	public void submitToRenderer(Renderer renderer, float renderOffsetX, float renderOffsetY, float corrector, float renderScale) {
		if (!item) {
			int heading_texture = 0;
			switch (heading)
			{
			case HEADING_UP:
				heading_texture = getData().texture_heading_up;
				break;
			case HEADING_DOWN:
				heading_texture = getData().texture_heading_down;
				break;
			case HEADING_LEFT:
				heading_texture = getData().texture_heading_left;
				break;
			case HEADING_RIGHT:
				heading_texture = getData().texture_heading_right;
				break;
			default:
				break;
			}
			Vector3f pos = new Vector3f((getX() + getVel_x() * corrector / Settings.UPDATES_PER_SECOND + renderOffsetX - 0.5f) * Settings.TILE_SIZE, (getY() + getVel_y() * corrector / Settings.UPDATES_PER_SECOND + renderOffsetY - 0.5f) * Settings.TILE_SIZE, 0.0f);
			Vector2f size = new Vector2f(Settings.TILE_SIZE, Settings.TILE_SIZE);
			pos.mul(Game.renderScale());
			size.mul(Game.renderScale());

			renderer.submitVertex(new VertexData(new Vector3f(pos.x, pos.y, pos.z), new Vector2f(0.0f, 0.0f), heading_texture, Colors.WHITE));
			renderer.submitVertex(new VertexData(new Vector3f(pos.x, pos.y + size.y, pos.z), new Vector2f(0.0f, 1.0f), heading_texture, Colors.WHITE));
			renderer.submitVertex(new VertexData(new Vector3f(pos.x + size.x, pos.y + size.y, pos.z), new Vector2f(1.0f, 1.0f), heading_texture, Colors.WHITE));
			renderer.submitVertex(new VertexData(new Vector3f(pos.x + size.x, pos.y, pos.z), new Vector2f(1.0f, 0.0f), heading_texture, Colors.WHITE));

			float swingX = 0.0f, swingY = 0.0f;
			int swingTexture = 13;
			switch (swingDir)
			{
			case 1:
				swingTexture = 13;
				swingX = (getX() + getVel_x() * corrector / Settings.UPDATES_PER_SECOND + renderOffsetX - 0.5f) * Settings.TILE_SIZE; 
				swingY = (getY() + getVel_y() * corrector / Settings.UPDATES_PER_SECOND + renderOffsetY - 1.0f) * Settings.TILE_SIZE;
				break;
			case 2:
				swingTexture = 12;
				swingX = (getX() + getVel_x() * corrector / Settings.UPDATES_PER_SECOND + renderOffsetX - 0.0f) * Settings.TILE_SIZE; 
				swingY = (getY() + getVel_y() * corrector / Settings.UPDATES_PER_SECOND + renderOffsetY - 0.5f) * Settings.TILE_SIZE;
				break;
			case 3:
				swingTexture = 15;
				swingX = (getX() + getVel_x() * corrector / Settings.UPDATES_PER_SECOND + renderOffsetX - 0.5f) * Settings.TILE_SIZE; 
				swingY = (getY() + getVel_y() * corrector / Settings.UPDATES_PER_SECOND + renderOffsetY - 0.0f) * Settings.TILE_SIZE;
				break;
			case 4:
				swingTexture = 14;
				swingX = (getX() + getVel_x() * corrector / Settings.UPDATES_PER_SECOND + renderOffsetX - 1.0f) * Settings.TILE_SIZE; 
				swingY = (getY() + getVel_y() * corrector / Settings.UPDATES_PER_SECOND + renderOffsetY - 0.5f) * Settings.TILE_SIZE;
				break;
			default:
				break;
			}
			if (swingDir != 0) {
				pos = new Vector3f(swingX, swingY, 0.0f);
				size = new Vector2f(Settings.TILE_SIZE);
				pos.mul(Game.renderScale());
				size.mul(Game.renderScale());

				renderer.submitVertex(new VertexData(new Vector3f(pos.x, pos.y, pos.z), new Vector2f(0.0f, 0.0f), swingTexture, Colors.WHITE));
				renderer.submitVertex(new VertexData(new Vector3f(pos.x, pos.y + size.y, pos.z), new Vector2f(0.0f, 1.0f), swingTexture, Colors.WHITE));
				renderer.submitVertex(new VertexData(new Vector3f(pos.x + size.x, pos.y + size.y, pos.z), new Vector2f(1.0f, 1.0f), swingTexture, Colors.WHITE));
				renderer.submitVertex(new VertexData(new Vector3f(pos.x + size.x, pos.y, pos.z), new Vector2f(1.0f, 0.0f), swingTexture, Colors.WHITE));
			}
		}
		else {
			Vector3f pos = new Vector3f((getX() + getVel_x() * corrector / Settings.UPDATES_PER_SECOND + renderOffsetX - 0.5f) * Settings.TILE_SIZE, (getY() + getVel_y() * corrector / Settings.UPDATES_PER_SECOND + renderOffsetY - 0.5f) * Settings.TILE_SIZE, 0.0f);
			Vector2f size = new Vector2f(Settings.TILE_SIZE);
			pos.mul(Game.renderScale());
			size.mul(Game.renderScale());

			int texture = Database.items.get(id).texture;
			renderer.submitVertex(new VertexData(new Vector3f(pos.x, pos.y, pos.z), new Vector2f(0.0f, 0.0f), texture, Colors.WHITE));
			renderer.submitVertex(new VertexData(new Vector3f(pos.x, pos.y + size.y, pos.z),new Vector2f(0.0f, 1.0f), texture, Colors.WHITE));
			renderer.submitVertex(new VertexData(new Vector3f(pos.x + size.x, pos.y + size.y, pos.z), new Vector2f(1.0f, 1.0f), texture, Colors.WHITE));
			renderer.submitVertex(new VertexData(new Vector3f(pos.x + size.x, pos.y, pos.z), new Vector2f(1.0f, 0.0f), texture, Colors.WHITE));
		}

	}
	
	public Creature_Data getData() {
		return Database.creatures.get(id);
	}

	private void resetHealth() {
		health = maxHealth;
	}

	private void resetStamina() {
		stamina = maxStamina;
	}

}
