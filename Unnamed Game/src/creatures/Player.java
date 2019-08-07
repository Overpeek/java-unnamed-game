package creatures;

import org.json.JSONObject;

import graphics.Renderer;
import logic.Database;
import logic.Inventory;
import logic.Main;
import logic.Settings;
import utility.DataIO;
import utility.Keys;
import utility.Logger;
import utility.vec2;

public class Player extends Creature {
	
	private Inventory inventory;
	private vec2 spawn_location;
	private boolean god;
	private boolean clipmode;
	private vec2 death_location;
	private vec2 ghost_object_location;

	

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
		Main.game.getGui().addChatLine("Player died at " + getPos().x + ", " + getPos().y);

		death_location = getPos();

		resetHealth();
		resetStamina();
		setPos(spawn_location);
	}

	@Override
	public void draw(Renderer renderer, float preupdate_scale) {
		commonDraw(renderer, preupdate_scale);

		//Main.game.getMap().();
		

//		double mx, my;
//		Game::getWindow()->getMousePos(mx, my);
//		mx /= TILE_SIZE;
//		my /= TILE_SIZE;
//
//
//		if (Database::items[inventory->selectedId].placedAs != 0) {
//			int x_dst = abs((int)getX() - floor(getX() + mx));
//			int y_dst = abs((int)getY() - floor(getY() + my));
//			if (x_dst == 0) y_dst++;
//			if (y_dst == 0) x_dst++;
//			if (x_dst + y_dst < m_hitdist) {
//				
//			}
//		}
	}
	
	public void buttonPress(int button, int action) {
		
		if (button == Keys.MOUSE_BUTTON_LEFT && action == Keys.PRESS) {

			vec2 cursor = Main.game.getWindow().getCursorFast();
			cursor.x /= Settings.TILE_SIZE;
			cursor.y /= Settings.TILE_SIZE;
			vec2 tile_on_map = new vec2(
					(float)((int)getPos().x - Math.floor(getPos().x + cursor.x)),
					(float)((int)getPos().y - Math.floor(getPos().y + cursor.y))
					).mult(-1.0f);
			tile_on_map.x += Math.floor(getPos().x);
			tile_on_map.y += Math.floor(getPos().y);
			ghost_object_location = tile_on_map;
			
			Main.game.getMap().hit((int)tile_on_map.x, (int)tile_on_map.y, (int)getData().meleeDamage);
			hit();
			
		}
		
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
		

		vec2 cursor = Main.game.getWindow().getCursorFast();
		setHeading(cursor.x, cursor.y);
		
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
