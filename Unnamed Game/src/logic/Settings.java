package logic;

public final class Settings {

	public static final int VERSION_MAJOR = 0;
	public static final int VERSION_MINOR = 4;
	public static final int VERSION_BUG_FIX = 0;
	public static final String VERSION_STR = VERSION_MAJOR + "." + VERSION_MINOR + "." + VERSION_BUG_FIX;

	public static final int UPDATES_PER_SECOND = 60;

	public static final float DEBUG_ZOOM = 1.0f;
	public static final boolean DEBUG_MODE = false;
	public static final boolean SHOW_DEBUG_MESSAGES = false;
	public static final boolean DEBUG_DISABLE_SAVING = false;

	public static final int WINDOW_HEIGHT = 720;
	public static final int WINDOW_WIDTH = 1280;
	public static final float ASPECT = (float) WINDOW_WIDTH / (float) WINDOW_HEIGHT;
	public static final boolean ASPECT_FIXED = false;
	public static final String GAME_NAME = "Unnamed Game";
	public static final String WINDOW_DEFAULT_TITLE = GAME_NAME + " Pre-Alpha v" + VERSION_STR;

	public static final int RENDER_DIST = 16;
	public static final int MAP_WORK_DST = 32;

	public static final float TILE_SIZE = 0.1f;

	public static final float PLAYER_WIDTH = 0.8f;
	public static final float PLAYER_HEIGHT = 0.8f;

	public static final int MAP_SIZE = 250;
	public static final int MAP_MAX_CREATURES = 2048;

	public static final String USER_HOME = System.getProperty("user.home");
	public static final String SAVE_PATH = USER_HOME + "/AppData/Roaming/" + GAME_NAME;
	public static final String WORLD_NAME = "test";
	
	public static final int INVENTORY_WIDTH = 5;
	public static final int INVENTORY_HEIGHT = 5;
	public static final float INVENTORY_SCALE = 0.2f;
	

	public static final float MAP_FREQ = 0.01f;
	public static final float MAP_BIOME_FREQ = 0.003f;
	public static final float MAP_PLANT1_FREQ = 0.1f;
	public static final float MAP_PLANT2_FREQ = 0.1f;
	public static final int OCTAVES = 8;
	public static final float PERSISTANCE = 0.5f;
	public static final float LACUNARITY = 1.72f;
	public static final boolean DEBUG_REGEN_WORLD = false;
	
}
