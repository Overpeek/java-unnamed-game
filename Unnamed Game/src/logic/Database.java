package logic;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.filechooser.FileNameExtensionFilter;

import org.joml.Vector4f;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import graphics.Renderer;
import logic.Database.Biome_Data.BetweenBiomes;
import logic.Database.Biome_Data.BiomeTileHeight_Data;
import logic.Database.Biome_Data.BiomeTileHeight_Data.BiomeTileHeightObjects_Data;
import logic.Database.Creature_Data.Drop;
import logic.TextureLoader.MultitextureReturnData;
import utility.DataIO;
import utility.Logger;

//Good source https://www.redblobgames.com/maps/terrain-from-noise/

public class Database {

	public static final float MAP_FREQ =		0.05f;
	public static final float MAP_BIOME_FREQ =	MAP_FREQ / 4.0f;
	public static final float MAP_PLANT1_FREQ =	0.1f;
	public static final float MAP_PLANT2_FREQ =	0.1f;
           
	public static final float MAP_OCTA =		3;
	public static final float MAP_BIOME_OCTA =	4;
	public static final float MAP_PLANT1_OCTA =	2;
	public static final float MAP_PLANT2_OCTA =	2;
	
	private static HashMap<String, Item_Data> items;
	private static HashMap<String, Tile_Data> tiles;
	private static HashMap<String, Object_Data> objects;
	private static HashMap<String, Creature_Data> creatures;
	private static HashMap<String, Biome_Data> biomes;
	private static HashMap<String, Particle_Data> particles;

	private static ArrayList<String> item_names;
	private static ArrayList<String> tile_names;
	private static ArrayList<String> object_names;
	private static ArrayList<String> creature_names;
	private static ArrayList<String> biome_names;
	private static ArrayList<String> particle_names;

	private static ArrayList<Mod> mods;
	
	public static void initialize() {
		items = new HashMap<String, Item_Data>();
		tiles = new HashMap<String, Tile_Data>();
		objects = new HashMap<String, Object_Data>();
		creatures = new HashMap<String, Creature_Data>();
		biomes = new HashMap<String, Biome_Data>();
		particles = new HashMap<String, Particle_Data>();
		
		item_names = new ArrayList<String>();
		tile_names = new ArrayList<String>();
		object_names = new ArrayList<String>();
		creature_names = new ArrayList<String>();
		biome_names = new ArrayList<String>();
		particle_names = new ArrayList<String>();
		
		//Load all vanilla items
		//byte[] source = DataIO.readByte();
		String source = StandardCharsets.UTF_8.decode(DataIO.readResourceFile("/res/data/items.json")).toString();
		JSONObject obj = new JSONObject(source);
		JSONArray arr = obj.getJSONArray("items");
		for (int i = 0; i < arr.length(); i++)
		{
			try {
				loadItem(arr.getJSONObject(i));
			} catch (Exception e) {
				Logger.crit("Couldn't load item: [" + i + "]");
				e.printStackTrace();
				System.exit(-1);
			}
		}
		
		//Load all vanilla tiles
		source = StandardCharsets.UTF_8.decode(DataIO.readResourceFile("/res/data/tiles.json")).toString();
		obj = new JSONObject(source);
		arr = obj.getJSONArray("tiles");
		for (int i = 0; i < arr.length(); i++)
		{
			try {
				loadTile(arr.getJSONObject(i));
			} catch (Exception e) {
				Logger.crit("Couldn't load tile: [" + i + "]");
				e.printStackTrace();
				System.exit(-1);
			}
		}
		
		//Load all vanilla objects
		source = StandardCharsets.UTF_8.decode(DataIO.readResourceFile("/res/data/objects.json")).toString();
		obj = new JSONObject(source);
		arr = obj.getJSONArray("objects");
		for (int i = 0; i < arr.length(); i++)
		{
			try {
				loadObject(arr.getJSONObject(i));
			} catch (Exception e) {
				Logger.crit("Couldn't load object: [" + i + "]");
				e.printStackTrace();
				System.exit(-1);
			}
		}
		
		//Load all vanilla creatures
		source = StandardCharsets.UTF_8.decode(DataIO.readResourceFile("/res/data/creatures.json")).toString();
		obj = new JSONObject(source);
		arr = obj.getJSONArray("creatures");
		for (int i = 0; i < arr.length(); i++)
		{
			try {
				loadCreature(arr.getJSONObject(i));
			} catch (Exception e) {
				Logger.crit("Couldn't load creature: [" + i + "]");
				e.printStackTrace();
				System.exit(-1);
			}
		}
		
		//Load all vanilla biomes
		source = StandardCharsets.UTF_8.decode(DataIO.readResourceFile("/res/data/biomes.json")).toString();
		obj = new JSONObject(source);
		arr = obj.getJSONArray("biomes");
		for (int i = 0; i < arr.length(); i++)
		{
			try {
				loadBiome(arr.getJSONObject(i));
			} catch (Exception e) {
				Logger.crit("Couldn't load biome: [" + i + "]");
				e.printStackTrace();
				System.exit(-1);
			}
		}
		
		//Load all vanilla particles
		source = StandardCharsets.UTF_8.decode(DataIO.readResourceFile("/res/data/particles.json")).toString();
		obj = new JSONObject(source);
		arr = obj.getJSONArray("particles");
		for (int i = 0; i < arr.length(); i++)
		{
			try {
				loadParticle(arr.getJSONObject(i));
			} catch (Exception e) {
				Logger.crit("Couldn't load particle: [" + i + "]");
				e.printStackTrace();
				System.exit(-1);
			}
		}
		
		//Load all modded stuff
		Logger.info("Loading all mods");
		mods = new ArrayList<Mod>();
		loadAllMods();
		
		Logger.info("Successfully loaded " + items.size() + " items, " + tiles.size() + " tiles, " + objects.size() + " objects, " + creatures.size() + " creatures, " + biomes.size() + " biomes and " + particles.size() + " particles.");
	
	
		//Generate all indexes
		for (int i = 0; i < tiles.size(); i++) {
			tiles.get(tile_names.get(i)).index = i;
		}
		for (int i = 0; i < items.size(); i++) {
			items.get(item_names.get(i)).index = i;
		}
		for (int i = 0; i < creatures.size(); i++) {
			creatures.get(creature_names.get(i)).index = i;
		}
		for (int i = 0; i < biomes.size(); i++) {
			biomes.get(biome_names.get(i)).index = i;
		}
		for (int i = 0; i < particles.size(); i++) {
			particles.get(particle_names.get(i)).index = i;
		}
		for (int i = 0; i < objects.size(); i++) {
			objects.get(object_names.get(i)).index = i;
		}
	}
	
	public static void modUpdates() {
		for (int i = 0; i < mods.size(); i++) {
			mods.get(i).update();
		}
	}
	
	public static void modRendering(Renderer renderer) {
		for (int i = 0; i < mods.size(); i++) {
			mods.get(i).draw(renderer);
		}
	}
	
	public static void modCleanup() {
		for (int i = 0; i < mods.size(); i++) {
			mods.get(i).cleanup();
		}
	}
	
	public static void modSave() {
		for (int i = 0; i < mods.size(); i++) {
			mods.get(i).save();
		}
	}
	
	private static void loadAllMods() {
		String modsFolder = Main.game.getDataPath() + "mods/";
		
		FileNameExtensionFilter extensionFilter = new FileNameExtensionFilter("N/A", "jar");
		File file = new File(modsFolder);
		
		File files[] = file.listFiles();
		if (files == null) return; // no mods found
		for (File child : files) {
		    if(extensionFilter.accept(child)) {
		        Logger.info("Loading mod: \"" + child.getName() + "\"");
		        Mod loadedMod = loadMod(child);
		        if (loadedMod == null) continue;
		        Logger.info("Mod: \"" + loadedMod.getName() + "\" Version: \"" + loadedMod.getVersion() + "\" By: \"" + loadedMod.getCreator() + "\" successfully loaded!");
		        loadedMod.setup();
		    }
		}
	}
	
	private static Mod loadMod(File modJar) {
		
		try {
			URL url = modJar.toURI().toURL();
			URL[] urls = new URL[]{url};

			@SuppressWarnings("resource") ////////////////////////////////////////
			ClassLoader cl = new URLClassLoader(urls);
			Class<?> cls = cl.loadClass("Main");
			
			//Load mod class
			Mod modClass = (Mod) cls.newInstance();
			if (modClass == null) return null;
			
			//Setup and add
			mods.add(modClass);
			return modClass;
		} catch (Exception e) {
			e.printStackTrace();
			Logger.crit("Error loading mod: \"" + modJar.getName() + "\". Skipping...");
		}
		return null;  
	}
	
	public static void loadItem(JSONObject json) throws JSONException {
		//Generic data
	    String name = json.getString("name");
	    String data_name = json.getString("data_name");
	    int stack = json.getInt("stack");
	    float melee_damage = json.getFloat("melee");
	    float knock_mult = json.getFloat("knock");
	    float break_speed = json.getFloat("break");
	    int placed = json.getInt("placed");
	    
	    //Load all textures
	    int texture = 0;
	    int texture_last = 0;
	    String texturePath = json.getString("texture");
	    if (!texturePath.equals("null")) {
		    MultitextureReturnData texturedata = TextureLoader.loadMultitexture(texturePath);
		    texture = texturedata.first_index;
		    texture_last = texturedata.last_index;
	    }
	    
	    
	    //Add new item	    
	    Item_Data object = new Item_Data(name, data_name, texture, texture_last, stack, melee_damage, knock_mult, break_speed, placed);
	    loadItem(object);
	}
	
	public static void loadItem(Item_Data data) {
	    Logger.debug("Loaded item: " + data.toString());
	    items.put(data.data_name, data);
	    item_names.add(data.data_name);
	}
	
	public static void loadTile(JSONObject json) throws JSONException {
		//Generic data
	    String name = json.getString("name");
	    String data_name = json.getString("data_name");

	    //Load all textures
	    int texture = 0;
	    int texture_last = 0;
	    String texturePath = json.getString("texture");
	    if (!texturePath.equals("null")) {
		    MultitextureReturnData texturedata = TextureLoader.loadMultitexture(texturePath);
		    texture = texturedata.first_index;
		    texture_last = texturedata.last_index;
	    }
	    
	    //Add new tile
	    Tile_Data object = new Tile_Data(name, data_name, texture, texture_last);
	    loadTile(object);
	}
	
	public static void loadTile(Tile_Data data) {
	    Logger.debug("Loaded tile: " + data.toString());
	    tiles.put(data.data_name, data);
	    tile_names.add(data.data_name);
	}
	
	public static void loadObject(JSONObject json) throws JSONException {
		//Generic data
		String name = json.getString("name");
	    String data_name = json.getString("data_name");
	    boolean wall = json.getBoolean("wall");
	    boolean destroyable = json.getBoolean("destroyable");
	    float red = json.getFloat("color.r");
	    float green = json.getFloat("color.g");
	    float blue = json.getFloat("color.b");
	    float alpha = json.getFloat("color.a");
	    int health = json.getInt("health");
	    int drop = json.getInt("drop");
	    
	    //Load all textures
	    int texture = 0;
	    int texture_last = 0;
	    String texturePath = json.getString("texture");
	    if (!texturePath.equals("null")) {
		    MultitextureReturnData texturedata = TextureLoader.loadMultitexture(texturePath);
		    texture = texturedata.first_index;
		    texture_last = texturedata.last_index;
	    }
	    
	    //Add new object
	    Object_Data object = new Object_Data(name, data_name, texture, texture_last, wall, destroyable, new Vector4f(red, green, blue, alpha), health, drop);
	    loadObject(object);
	}
	
	public static void loadObject(Object_Data data) {
	    Logger.debug("Loaded object: " + data.toString());
	    objects.put(data.data_name, data);
	    object_names.add(data.data_name);
	}
	
	public static void loadBiome(JSONObject json) throws JSONException {
		String name = json.getString("name");
		String data_name = json.getString("data_name");
	    BetweenBiomes between[] = null;
	    
	    float height = 0.0f;
	    try {
		    height = json.getFloat("height");
		} catch (Exception e) 
	    {
			//It was biome between biomes
			JSONArray arr2 = json.getJSONArray("between");
			between = new BetweenBiomes[arr2.length()];
			for (int j = 0; j < arr2.length(); j++)
			{
				between[j] = new BetweenBiomes(arr2.getJSONObject(j).getString("biome_0"),
						arr2.getJSONObject(j).getString("biome_1"));
			}
	    }

	    //Heightmap
	    ArrayList<BiomeTileHeight_Data> height_map = new ArrayList<BiomeTileHeight_Data>();
	    JSONArray arrd = json.getJSONArray("height_map");
		for (int d = 0; d < arrd.length(); d++)
		{
		    float heights = arrd.getJSONObject(d).getFloat("height");
		    String tile = arrd.getJSONObject(d).getString("tile");
		    
		    //Objects
		    ArrayList<BiomeTileHeightObjects_Data> heightObjects = new ArrayList<BiomeTileHeightObjects_Data>();
		    JSONArray arrd2 = arrd.getJSONObject(d).getJSONArray("objects");
		    for (int i = 0; i < arrd2.length(); i++) {
			    String object = arrd2.getJSONObject(i).getString("object");
			    float rarity = arrd2.getJSONObject(i).getFloat("rarity");
			    
			    heightObjects.add(new BiomeTileHeightObjects_Data(object, rarity));
			}
		    
		    BiomeTileHeight_Data height_data = new BiomeTileHeight_Data(heights, tile, heightObjects);
		    height_map.add(height_data);
		}
	    
		Biome_Data object = new Biome_Data(name, data_name, between, height, height_map);
		loadBiome(object);
	}
	
	public static void loadBiome(Biome_Data data) {
	    Logger.debug("Loaded biome: " + data.toString());
	    biomes.put(data.data_name, data);
	    biome_names.add(data.data_name);
	}

	public static void loadCreature(JSONObject json) throws JSONException {

		//Generic data
		String name = json.getString("name");
		String data_name = json.getString("data_name");
	    float walkspeed = json.getFloat("walkspeed");
	    boolean friendly = json.getBoolean("friendly");
	    boolean ghost = json.getBoolean("ghost");
	    float knockback = json.getFloat("knockback");
	    float melee = json.getFloat("melee");
	    float health = json.getFloat("health");
	    float regen = json.getFloat("regen");
	    float stamina = json.getFloat("stamina");
	    float staminaregen = json.getFloat("staminaregen");
	    float red = json.getFloat("color.r");
	    float green = json.getFloat("color.g");
	    float blue = json.getFloat("color.b");
	    float alpha = json.getFloat("color.a");
	    
	    //Load all textures
	    int texture = 0;
	    int texture_last = 0;
	    String texturePath = json.getString("texture");
	    if (!texturePath.equals("null")) {
		    MultitextureReturnData texturedata = TextureLoader.loadMultitexture(texturePath);
		    texture = texturedata.first_index;
		    texture_last = texturedata.last_index;
	    }

	    //Drops
	    ArrayList<Drop> drops = new ArrayList<Drop>();
	    JSONArray arrd = json.getJSONArray("drops");
		for (int d = 0; d < arrd.length(); d++)
		{
		    String d_item = arrd.getJSONObject(d).getString("item");
		    int d_min = arrd.getJSONObject(d).getInt("min");
		    int d_max = arrd.getJSONObject(d).getInt("max");
		    
			Drop drop = new Drop(d_item, d_min, d_max);
			drops.add(drop);
		}
	    
	    Creature_Data object = new Creature_Data(name, data_name, texture, texture_last, walkspeed, friendly, ghost, knockback, melee, health, regen, stamina, staminaregen, drops, new Vector4f(red, green, blue, alpha));
	    loadCreature(object);
	}
	
	public static void loadCreature(Creature_Data data) {
	    Logger.debug("Loaded creature: " + data.toString());
	    creatures.put(data.data_name, data);
	    creature_names.add(data.data_name);
	}
		
	public static void loadParticle(JSONObject json) throws JSONException {
		//Generic data
	    String name = json.getString("name");
		String data_name = json.getString("data_name");
	    float life_time = json.getFloat("life_time");

	    //Load all textures
	    int texture = 0;
	    int texture_last = 0;
	    String texturePath = json.getString("texture");
	    if (!texturePath.equals("null")) {
		    MultitextureReturnData texturedata = TextureLoader.loadMultitexture(texturePath);
		    texture = texturedata.first_index;
		    texture_last = texturedata.last_index;
	    }
	    
	    //Add new tile
	    Particle_Data object = new Particle_Data(name, data_name, texture, texture_last, life_time);
	    loadParticle(object);
	}
	
	public static void loadParticle(Particle_Data data) {
	    Logger.debug("Loaded particle: " + data.toString());
	    particles.put(data.data_name, data);
	    particle_names.add(data.data_name);
	}
	
	//Save data
	public static void writeData(byte[] data, String path) {
		DataIO.writeByte(Main.game.getSavePath() + path, data);
	}
	public static void writeDataBuffer(ByteBuffer data, String path) {
		DataIO.writeByteBuffer(Main.game.getSavePath() + path, data);
	}
	
	//Load data
	public static byte[] readData(String path) {
		return DataIO.readByte(Main.game.getSavePath() + path);
	}
	public static ByteBuffer readDataBuffer(String path) {
		return DataIO.readByteBuffer(Main.game.getSavePath() + path);
	}
	
	//--------------
	//DATA FORMATING
	//--------------
	
	//INT
	public static int[] bytesToInts(byte[] input) {
		int[] destination = null;
		ByteBuffer.wrap(input).asIntBuffer().get(destination);
		return destination;
	}
	public static byte[] intsToBytes(int[] input) {
		ByteBuffer bytes = ByteBuffer.allocate(Integer.BYTES * input.length);
		bytes.asIntBuffer().put(input);
		return bytes.array();
	}
	
	//FLOAT
	public static float[] bytesToFloats(byte[] input) {
		float[] destination = null;
		ByteBuffer.wrap(input).asFloatBuffer().get(destination);
		return destination;
	}
	public static byte[] floatToBytes(float[] input) {
		ByteBuffer bytes = ByteBuffer.allocate(Float.BYTES * input.length);
		bytes.asFloatBuffer().put(input);
		return bytes.array();
	}
	
	//SHORT
	public static short[] bytesToShorts(byte[] input) {
		short[] destination = null;
		ByteBuffer.wrap(input).asShortBuffer().get(destination);
		return destination;
	}
	public static byte[] shortsToBytes(short[] input) {
		ByteBuffer bytes = ByteBuffer.allocate(Short.BYTES * input.length);
		bytes.asShortBuffer().put(input);
		return bytes.array();
	}
	
	//LONG
	public static long[] bytesToLongs(byte[] input) {
		long[] destination = null;
		ByteBuffer.wrap(input).asLongBuffer().get(destination);
		return destination;
	}
	public static byte[] longsToBytes(long[] input) {
		ByteBuffer bytes = ByteBuffer.allocate(Long.BYTES * input.length);
		bytes.asLongBuffer().put(input);
		return bytes.array();
	}
	
	//String
	public static String bytesToString(byte[] input) {
		return new String(input);
	}
	public static byte[] stringToBytes(String input) {
		return input.getBytes();
	}
	
	//-------
	//GETTERS
	//-------
	public static int getParticleCount() {
		return particles.size();
	}
	
	public static int getObjectCount() {
		return objects.size();
	}
	
	public static int getTileCount() {
		return tiles.size();
	}
	
	public static int getBiomeCount() {
		return biomes.size();
	}
	
	public static int getCreatureCount() {
		return creatures.size();
	}
	
	public static int getItemCount() {
		return items.size();
	}
	
	public static Particle_Data getParticle(String data_name) {
		return particles.get(data_name);
	}
	
	public static Particle_Data getParticle(int id) {
		return particles.get(particle_names.get(id));
	}
	
	public static Object_Data getObject(String data_name) {
		return objects.get(data_name);
	}
	
	public static Object_Data getObject(int id) {
		return objects.get(object_names.get(id));
	}
	
	public static Tile_Data getTile(String data_name) {
		return tiles.get(data_name);
	}
	
	public static Tile_Data getTile(int id) {
		return tiles.get(tile_names.get(id));
	}
	
	public static Item_Data getItem(String data_name) {
		return items.get(data_name);
	}
	
	public static Item_Data getItem(int id) {
		return items.get(item_names.get(id));
	}
	
	public static Biome_Data getBiome(String data_name) {
		return biomes.get(data_name);
	}
	
	public static Biome_Data getBiome(int id) {
		return biomes.get(biome_names.get(id));
	}
	
	public static Creature_Data getCreature(String data_name) {
		return creatures.get(data_name);
	}
	
	public static Creature_Data getCreature(int id) {
		return creatures.get(creature_names.get(id));
	}
	
		
	//---------
	//DATATYPES
	//---------
	
	public static class Particle_Data {
		public int index;
		public String name = "null";
		public String data_name = "null";
		public int texture = 0;
		public int texture_last = 0;
		public float life_time = 0.0f;
		
		public Particle_Data(String _name, String _data_name, int _texture, int _texture_last, float _life_time) {
			name = _name;
			data_name = _data_name;
			texture = _texture;
			texture_last = _texture_last;
			life_time = _life_time;
		}
		
		@Override
		public String toString() {
			String temp = "name: " + name + ", data_name: " + data_name + ", texture: " + texture + ", texture_last: " + texture_last + ", life_time: " + life_time;
			return temp;
		}
	}
	
	public static class Item_Data
	{
		public int index;
		public String name = "null";
		public String data_name = "null";
		public int texture = 0;
		public int texture_last = 0;

		public int stack_size = 0;

		public float melee_damage = 0;
		public float melee_kb = 0;
		public float break_speed = 0;

		public int placedAs;

		public Item_Data(String _name, String _data_name, int _texture, int _texture_last, int _stack, float _melee_damage, float _melee_kb, float _break_speed, int _placedAs) {
			name = _name;
			data_name = _data_name;
			texture = _texture;
			texture_last = _texture_last;
			stack_size = _stack;
			melee_damage = _melee_damage;
			melee_kb = _melee_kb;
			break_speed = _break_speed;
			placedAs = _placedAs;
		}
		
		@Override
		public String toString() {
			String temp = "name: " + name + ", data_name: " + data_name + ", texture: " + texture + ", texture_last: " + texture_last + ", stack_size: " + stack_size;
			temp += ", melee_damage: " + melee_damage + ", melee_kb: " + melee_kb + ", break_speed: " + break_speed + ", placedAs: " + placedAs;
			return temp;
		}
	}

	public static class Tile_Data
	{
		public String name = "null";
		public String data_name = "null";
		public int index;
		public int texture = 0;
		public int texture_last = 0;

		public Tile_Data(String _name, String _data_name, int _texture, int _texture_last) {
			name = _name;
			data_name = _data_name;
			texture = _texture;
			texture_last = _texture_last;
		}
		
		@Override
		public String toString() {
			String temp = "name: " + name + ", data_name: " + data_name + ", texture: " + texture + ", texture_last: " + texture_last;
			return temp;
		}
	}

	public static class Object_Data
	{
		public int index;
		public String name = "null";
		public String data_name = "null";
		public int texture = 0;
		public int texture_last = 0;
		public boolean wall = false;
		public boolean destroyable = false;
		public Vector4f color = new Vector4f(0.0f);

		public int health = 0;
		public int dropsAs = 0;

		public Object_Data(String _name, String _data_name, int _texture, int _last_texture, boolean _wall, boolean _destroyable, Vector4f _color, int _health, int _dropsAs) {
			name = _name;
			data_name = _data_name;
			texture = _texture;
			wall = _wall;
			texture_last = _last_texture;
			destroyable = _destroyable;
			color = _color;

			health = _health;
			dropsAs = _dropsAs;
		}
		
		@Override
		public String toString() {
			String temp = "name: " + name + ", data_name: " + data_name + ", texture: " + texture + ", texture_last: " + texture_last;
			temp += ", wall: " + wall + ", destroyable: " + destroyable + ", color: (" + color.x + ", " + color.y + ", " + color.z + ", " + color.w + "), health: " + health + "dropsAs: " + dropsAs;
			return temp;
		}
		
		public int getTexture(boolean rightAir, boolean topAir, boolean leftAir, boolean bottomAir, boolean topRightAir, boolean topLeftAir, boolean bottomLeftAir, boolean bottomRightAir) {
			if (texture_last == texture) return texture;
			
			int index = texture;

			//boolean allSidesAir = rightAir && topAir && leftAir && bottomAir;
			//boolean allCornersAir = topRightAir && topLeftAir && bottomLeftAir && bottomRightAir;
			boolean noSideAir = !rightAir && !topAir && !leftAir && !bottomAir;
			//boolean noCornerAir = !topRightAir && !topLeftAir && !bottomLeftAir && !bottomRightAir;

			if (!rightAir && topAir && leftAir && !bottomAir && !bottomRightAir) {
				index += 0 + 0 * 16;
			}
			else if (!rightAir && topAir && !leftAir && !bottomAir && !bottomLeftAir && !bottomRightAir) {
				index += 1 + 0 * 16;
			}
			else if (rightAir && topAir && !leftAir && !bottomAir && !bottomLeftAir) {
				index += 2 + 0 * 16;
			}
			else if (!rightAir && !topAir && leftAir && !bottomAir && !topRightAir && !bottomRightAir) {
				index += 0 + 1 * 16;
			}
			else if (!rightAir && !topAir && !leftAir && !bottomAir && !topRightAir && !topLeftAir && !bottomLeftAir && !bottomRightAir) {
				index += 1 + 1 * 16;
			}
			else if (rightAir && !topAir && !leftAir && !bottomAir && !topLeftAir && !bottomLeftAir) {
				index += 2 + 1 * 16;
			}
			else if (!rightAir && !topAir && leftAir && bottomAir && !topRightAir) {
				index += 0 + 2 * 16;
			}
			else if (!rightAir && !topAir && !leftAir && bottomAir && !topRightAir && !topLeftAir) {
				index += 1 + 2 * 16;
			}
			else if (rightAir && !topAir && !leftAir && bottomAir && !topLeftAir) {
				index += 2 + 2 * 16;
			}

			else if (noSideAir && !topRightAir && !topLeftAir && !bottomLeftAir && bottomRightAir) {
				index += 3 + 0 * 16;
			}
			else if (noSideAir && !topRightAir && !topLeftAir && bottomLeftAir && bottomRightAir) {
				index += 4 + 0 * 16;
			}
			else if (noSideAir && !topRightAir && !topLeftAir && bottomLeftAir && !bottomRightAir) {
				index += 5 + 0 * 16;
			}
			else if (noSideAir && topRightAir && !topLeftAir && !bottomLeftAir && bottomRightAir) {
				index += 3 + 1 * 16;
			}
			else if (noSideAir && topRightAir && topLeftAir && bottomLeftAir && bottomRightAir) {
				index += 4 + 1 * 16;
			}
			else if (noSideAir && !topRightAir && topLeftAir && bottomLeftAir && !bottomRightAir) {
				index += 5 + 1 * 16;
			}
			else if (noSideAir && topRightAir && !topLeftAir && !bottomLeftAir && !bottomRightAir) {
				index += 3 + 2 * 16;
			}
			else if (noSideAir && topRightAir && topLeftAir && !bottomLeftAir && !bottomRightAir) {
				index += 4 + 2 * 16;
			}
			else if (noSideAir && !topRightAir && topLeftAir && !bottomLeftAir && !bottomRightAir) {
				index += 5 + 2 * 16;
			}

			else if (!rightAir && topAir && !leftAir && bottomAir) {
				index += 6 + 0 * 16;
			}
			else if (rightAir && topAir && leftAir && !bottomAir) {
				index += 7 + 0 * 16;
			}
			else if (rightAir && !topAir && leftAir && !bottomAir) {
				index += 6 + 1 * 16;
			}
			else if (rightAir && !topAir && leftAir && bottomAir) {
				index += 7 + 1 * 16;
			}
			else if (!rightAir && topAir && leftAir && bottomAir) {
				index += 6 + 2 * 16;
			}
			else if (rightAir && topAir && !leftAir && bottomAir) {
				index += 7 + 2 * 16;
			}

			else if (!rightAir && topAir && leftAir && !bottomAir && bottomRightAir) {
				index += 8 + 0 * 16;
			}
			else if (!rightAir && topAir && !leftAir && !bottomAir && bottomRightAir && bottomLeftAir) {
				index += 9 + 0 * 16;
			}
			else if (rightAir && topAir && !leftAir && !bottomAir && bottomLeftAir) {
				index += 10 + 0 * 16;
			}
			else if (!rightAir && !topAir && leftAir && !bottomAir && topRightAir && bottomRightAir) {
				index += 8 + 1 * 16;
			}
			else if (!rightAir && !topAir && !leftAir && !bottomAir && topRightAir && topLeftAir && bottomRightAir && bottomLeftAir) {
				index += 9 + 1 * 16;
			}
			else if (rightAir && !topAir && !leftAir && !bottomAir && topLeftAir && bottomLeftAir) {
				index += 10 + 1 * 16;
			}
			else if (!rightAir && !topAir && leftAir && bottomAir && topRightAir) {
				index += 8 + 2 * 16;
			}
			else if (!rightAir && !topAir && !leftAir && bottomAir && topRightAir && topLeftAir) {
				index += 9 + 2 * 16;
			}
			else if (rightAir && !topAir && !leftAir && bottomAir && topLeftAir) {
				index += 10 + 2 * 16;
			}

			else if (noSideAir && topRightAir && topLeftAir && bottomLeftAir && !bottomRightAir) {
				index += 11 + 0 * 16;
			}
			else if (noSideAir && topRightAir && topLeftAir && !bottomLeftAir && bottomRightAir) {
				index += 12 + 0 * 16;
			}
			else if (noSideAir && !topRightAir && topLeftAir && bottomLeftAir && bottomRightAir) {
				index += 13 + 0 * 16;
			}
			else if (noSideAir && topRightAir && !topLeftAir && bottomLeftAir && bottomRightAir) {
				index += 14 + 0 * 16;
			}

			else if (!rightAir && !topAir && leftAir && !bottomAir && !topRightAir && bottomRightAir) {
				index += 11 + 1 * 16;
			}
			else if (rightAir && !topAir && !leftAir && !bottomAir && !topLeftAir && bottomLeftAir) {
				index += 12 + 1 * 16;
			}
			else if (!rightAir && !topAir && leftAir && !bottomAir && topRightAir && !bottomRightAir) {
				index += 11 + 2 * 16;
			}
			else if (rightAir && !topAir && !leftAir && !bottomAir && topLeftAir && !bottomLeftAir) {
				index += 12 + 2 * 16;
			}

			else if (!rightAir && topAir && !leftAir && !bottomAir && !bottomLeftAir && bottomRightAir) {
				index += 13 + 1 * 16;
			}
			else if (!rightAir && topAir && !leftAir && !bottomAir && bottomLeftAir && !bottomRightAir) {
				index += 14 + 1 * 16;
			}
			else if (!rightAir && !topAir && !leftAir && bottomAir && !topLeftAir && topRightAir) {
				index += 13 + 2 * 16;
			}
			else if (!rightAir && !topAir && !leftAir && bottomAir && topLeftAir && !topRightAir) {
				index += 14 + 2 * 16;
			}

			else if (noSideAir && bottomLeftAir && topRightAir) {
				index += 15 + 0 * 16;
			}
			else if (noSideAir && bottomRightAir && topLeftAir) {
				index += 15 + 1 * 16;
			}

			else {
			index += 15 + 2 * 16;
			}

			return index;
		}
	}
	
	public static class Creature_Data
	{
		public int index;
		public String name = "null";
		public String data_name = "null";
		public int texture = 0;
		public int texture_last = 0;
		public boolean friendly = false;
		public boolean ghost = false;
		public Vector4f color = new Vector4f(0.0f);
		public float knockback = 0.0f;
		public float meleeDamage = 0.0f;

		public float health = 0.0f;
		public float healthgain = 0.0f;
		public float stamina = 0.0f;
		public float staminagain = 0.0f;

		public float walkSpeed = 0.0f;
		
		public ArrayList<Drop> drops;

		
		@Override
		public String toString() {
			String temp = "name: " + name + ", data_name: " + data_name + ", texture: " + texture + ", texture_last: " + texture_last;
			temp += ", friendly: " + friendly + ", ghost: " + ghost + ", color: (" + color.x + ", " + color.y + ", " + color.z + ", " + color.w + "), knockback: " + knockback + "meleeDamage: " + meleeDamage;
			temp += ", health: " + health + ", healthgain: " + healthgain + ", stamina: " + stamina + ", staminagain: " + staminagain;
			temp += ", walkSpeed: " + walkSpeed;
			temp += "drops: (" + drops.size() + " objects)";
			return temp;
		}
		
		public static class Drop {
			public String item;
			public int min; //min count of this drops
			public int max; //max count of this drops
			
			public Drop(String _item, int _min, int _max) {
				item = _item;
				min = _min;
				max = _max;
			}
		}

		public Creature_Data(String _name, String _data_name, int _texture, int _texture_last, float _walkSpeed, boolean _friendly, boolean _ghost, float _kb, float _melee, float _hp, float _hpg, float _st, float _stg, ArrayList<Drop> _drops, Vector4f _color) {
			name = _name;
			data_name = _data_name;
			walkSpeed = _walkSpeed;
			friendly = _friendly;
			ghost = _ghost;
			knockback = _kb;
			meleeDamage = _melee;
			health = _hp;
			healthgain = _hpg;
			stamina = _st;
			staminagain = _stg;
			color = _color;
			texture = _texture;
			texture_last = _texture_last;
			
			drops = _drops;
		}
	}

	public static class Biome_Data
	{
		public int index;
		public String name = "null";
		public String data_name = "null";
		public float height = 0;
		public ArrayList<BiomeTileHeight_Data> heightMap;
		public BetweenBiomes between[];
		
		@Override
		public String toString() {
			String temp = "name: " + name + ", data_name: " + data_name + ", height: " + height;
			temp += "heightMap: (" + heightMap.size() + " objects)";
			if (between != null) temp += "between: (" + between.length + " objects)";
			return temp;
		}
		
		public static class BetweenBiomes {
			public String biome_0;
			public String biome_1;
			
			public BetweenBiomes(String _biome_0, String _biome_1) {
				this.biome_0 = _biome_0;
				this.biome_1 = _biome_1;
			}
		}

		public static class BiomeTileHeight_Data
		{
			public float height;
			public String tile;
			public ArrayList<BiomeTileHeightObjects_Data> objects;
			
			public static class BiomeTileHeightObjects_Data {
				public String object;
				public float rarity;
				
				public BiomeTileHeightObjects_Data(String _object, float _rarity) {
					object = _object;
					rarity = _rarity;
				}
			}
			
			public BiomeTileHeight_Data(float _height, String _tile, ArrayList<BiomeTileHeightObjects_Data> _objects) {
				height = _height;
				objects = _objects;
				tile = _tile;
			}
		}

		public Biome_Data(String _name, String _data_name, BetweenBiomes _between[], float _height, ArrayList<BiomeTileHeight_Data> _heightMap) {
			name = _name;
			data_name = _data_name;
			heightMap = _heightMap;
			height = _height;
			between = _between;
		}
	}

}
