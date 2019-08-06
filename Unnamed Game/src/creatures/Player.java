package creatures;

import org.json.JSONObject;

import graphics.Renderer;
import logic.Database;
import logic.Inventory;
import logic.Main;
import logic.Settings;
import utility.DataIO;
import utility.Keys;
import utility.vec2;

public class Player extends Creature {
	
	private Inventory inventory;
	private vec2 spawn_location;
	private boolean god;
	private boolean clipmode;
//	private vec2 death_location;

	

	@Override
	public Creature construct(float x, float y, String id) {
		inventory = new Inventory();
		clipmode = true;
		god = false;
		return commonConstruct(Settings.MAP_SIZE_TILES / 2.0f, Settings.MAP_SIZE_TILES / 2.0f, "player");
	}

	@Override
	public void die() {
		if (god) return;

		inventory.dropAll();
		//Game.getGui().addChatLine("Player died at " + getPos().x + ", " + getPos().y);

//		death_location = getPos();

		resetHealth();
		resetStamina();
		setPos(spawn_location);
	}

	@Override
	public void draw(Renderer renderer, float preupdate_scale) {
		commonDraw(renderer, preupdate_scale);
	}

	@Override
	public void update(float ups) {
		float playerSpeed = Database.getCreature("player").walkSpeed;

		if (Main.game.getWindow().key(Keys.KEY_LEFT_SHIFT)) playerSpeed *= 2.0f;
		if (Main.game.getWindow().key(Keys.KEY_LEFT_CONTROL)) playerSpeed /= 2.0f;
		if (Main.game.getWindow().key(Keys.KEY_TAB)) playerSpeed *= 50.0f;
		if (Main.game.getWindow().key(Keys.KEY_S)) { setAcc(new vec2( getAcc().x,  playerSpeed)); }
		if (Main.game.getWindow().key(Keys.KEY_D)) { setAcc(new vec2( playerSpeed,  getAcc().y)); }
		if (Main.game.getWindow().key(Keys.KEY_W)) { setAcc(new vec2( getAcc().x, -playerSpeed)); }
		if (Main.game.getWindow().key(Keys.KEY_A)) { setAcc(new vec2(-playerSpeed,  getAcc().y)); }
		
		commonUpdate(ups);
	}

	@Override
	public void collide(float ups) {
		if (clipmode) commonCollide(ups);
	}

	@Override
	public void ai(float ups) {
		//NO AI
	}

	public static void addToDatabase() {
		Class<?> clazz = Player.class;
		JSONObject data = DataIO.loadJSONObject("player.json");
		Database.loadCreature(data, clazz);
	}
	

	
	public boolean load() {
		inventory.load();

		float playerData[] = DataIO.readFloat(Settings.SAVE_PATH + "player.data");
		if (playerData == null) {
			setSpawnPoint((int) getPos().x, (int) getPos().y);
			return false;
		}
		setPos(new vec2(playerData[0], playerData[1]));
		setSpawnPoint((int) playerData[2], (int) playerData[3]);
		return true;
	}


	public void setSpawnPoint(int x, int y) {
		spawn_location = new vec2(x, y);
		//if (Game.getGui()) Game.getGui().addChatLine("Spawnpoint set");
	}
	
	public vec2 getSpawnPoint() {
		return spawn_location;
	}
	
	public boolean getGodmode() {
		return god;
	}
	
	public void setGodmode(boolean mode) {
		god = mode;
	}
	
	public boolean getClipmode() {
		return clipmode;
	}
	
	public void setClipmode(boolean mode) {
		clipmode = mode;
	}

}
