package creatures;

import org.joml.Vector2f;

import graphics.Renderer;
import logic.Game;
import logic.Inventory;
import logic.Settings;
import utility.DataIO;

public class Player extends Creature {
	
	private Inventory inventory;
	private Vector2f spawn_location;
	private boolean god;
	private Vector2f death_location;

	
	public Player(float playerX, float playerY, Inventory _inventory) {
		super(playerX, playerY, 0, false);
		inventory = _inventory;
	}

	@Override
	public void die() {
		if (god) return;

		inventory.dropAll();
		//Game.getGui().addChatLine("Player died at " + getPos().x + ", " + getPos().y);

		death_location = getPos();

		resetHealth();
		resetStamina();
		setPos(spawn_location);
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
	

	
	public boolean load() {
		inventory.load();

		float playerData[] = DataIO.readFloat(Settings.SAVE_PATH + "player.data");
		if (playerData == null) {
			setSpawnPoint((int) getPos().x, (int) getPos().y);
			return false;
		}
		setPos(new Vector2f(playerData[0], playerData[1]));
		setSpawnPoint((int) playerData[2], (int) playerData[3]);
		return true;
	}


	public void setSpawnPoint(int x, int y) {
		spawn_location = new Vector2f(x, y);
		//if (Game.getGui()) Game.getGui().addChatLine("Spawnpoint set");
	}
	
	public Vector2f getSpawnPoint() {
		return spawn_location;
	}

}
