package creatures;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;

import graphics.Renderer;
import logic.Database;
import logic.Inventory;
import logic.Main;
import settings.CompiledSettings;
import utility.Keys;
import utility.Logger;
import utility.SaveManager;
import utility.vec2;

public class Player extends Creature {
	
	private Inventory inventory;
	private vec2 spawn_location;
	private boolean god;
	private boolean clipmode;
	@SuppressWarnings("unused")
	private vec2 death_location = null;
	@SuppressWarnings("unused")
	private vec2 ghost_object_location = null;

	

	@Override
	public Creature construct(float x, float y, String id) {
		inventory = new Inventory();
		clipmode = true;
		god = false;
		setSpawnPoint(CompiledSettings.MAP_SIZE_TILES / 2, CompiledSettings.MAP_SIZE_TILES / 2);
		return commonConstruct(CompiledSettings.MAP_SIZE_TILES / 2.0f, CompiledSettings.MAP_SIZE_TILES / 2.0f, "player");
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
		
		if (!Main.game.getGui().chatOpened()) { // no interacting when typing

			if (button == Keys.MOUSE_BUTTON_LEFT && action == Keys.PRESS) {
				
				hit();

				vec2 cursor = Main.game.getWindow().getCursorFast();
				cursor.x /= CompiledSettings.TILE_SIZE;
				cursor.y /= CompiledSettings.TILE_SIZE;
				vec2 tile_on_map = new vec2(
						(float)((int)getPos().x - Math.floor(getPos().x + cursor.x)),
						(float)((int)getPos().y - Math.floor(getPos().y + cursor.y))
						).mul(-1.0f);
				tile_on_map.x += Math.floor(getPos().x);
				tile_on_map.y += Math.floor(getPos().y);
				ghost_object_location = tile_on_map;
				
				Main.game.getMap().hit((int)tile_on_map.x, (int)tile_on_map.y, (int)getData().meleeDamage);
			}
			
		}
		
	}

	@Override
	public void update(float ups) {

		if (!Main.game.getGui().chatOpened()) { // no interacting when typing
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
		}
		
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
	
	public void save(String... path) {
		inventory.save(path);

		ByteBuffer playerData = ByteBuffer.allocate(4 * Float.BYTES);
		playerData.putFloat(getPos().x);
		playerData.putFloat(getPos().y);
		playerData.putFloat(getSpawnPoint().y);
		playerData.putFloat(getSpawnPoint().y);
		SaveManager.saveData(playerData, path);
	}
	
	public void load(String... path) {
		inventory.load(path);

		FloatBuffer playerData = null;
		try {
			playerData = SaveManager.loadData(path).asFloatBuffer();
		} catch(IOException e) {
			Logger.error(e.getMessage());
		}
		
		setPos(new vec2(playerData.get(), playerData.get()));
		setSpawnPoint((int) playerData.get(), (int) playerData.get());
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
