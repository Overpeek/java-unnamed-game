package logic;

import java.util.ArrayList;

import org.joml.Vector4f;
import org.json.*;

import logic.Database.Biome_Data.BiomeTileHeight_Data;
import logic.Database.Creature_Data.Drop;
import logic.TextureLoader.MultitextureReturnData;
import utility.DataIO;
import utility.Logger;

public class Database {

	public static final float MAP_FREQ =		0.05f;
	public static final float MAP_BIOME_FREQ =	MAP_FREQ / 4.0f;
	public static final float MAP_PLANT1_FREQ =	0.1f;
	public static final float MAP_PLANT2_FREQ =	0.1f;
           
	public static final float MAP_OCTA =		3;
	public static final float MAP_BIOME_OCTA =	4;
	public static final float MAP_PLANT1_OCTA =	2;
	public static final float MAP_PLANT2_OCTA =	2;
	
	public static ArrayList<Item_Data> items;
	public static ArrayList<Tile_Data> tiles;
	public static ArrayList<Object_Data> objects;
	public static ArrayList<Creature_Data> creatures;
	public static ArrayList<Biome_Data> biomes;
	public static ArrayList<Particle_Data> particles;
	
	public static void initialize() {
		
		//Load all items
		//--------------
		items = new ArrayList<Item_Data>();
		byte[] source = DataIO.readByte("res/data/items.json");
		JSONObject obj = new JSONObject(new String(source));

		JSONArray arr = obj.getJSONArray("items");
		for (int i = 0; i < arr.length(); i++)
		{
			//Generic data
		    String name = arr.getJSONObject(i).getString("name");
		    int id = arr.getJSONObject(i).getInt("id");
		    int stack = arr.getJSONObject(i).getInt("stack");
		    float melee_damage = arr.getJSONObject(i).getFloat("melee");
		    float knock_mult = arr.getJSONObject(i).getFloat("knock");
		    float break_speed = arr.getJSONObject(i).getFloat("break");
		    int placed = arr.getJSONObject(i).getInt("placed");
		    
		    //Load all textures
		    int texture = 0;
		    int texture_last = 0;
		    String texturePath = arr.getJSONObject(i).getString("texture");
		    if (!texturePath.equals("null")) {
			    MultitextureReturnData texturedata = TextureLoader.loadMultitexture(texturePath);
			    texture = texturedata.first_index;
			    texture_last = texturedata.last_index;
		    }
		    
		    //Add new item
		    Item_Data object = new Item_Data(name, id, texture, texture_last, stack, melee_damage, knock_mult, break_speed, placed);
		    items.add(object);
		}
		
		//Load all tiles
		//--------------
		tiles = new ArrayList<Tile_Data>();
		source = DataIO.readByte("res/data/tiles.json");
		obj = new JSONObject(new String(source));

		arr = obj.getJSONArray("tiles");
		for (int i = 0; i < arr.length(); i++)
		{
			//Generic data
		    String name = arr.getJSONObject(i).getString("name");
		    int id = arr.getJSONObject(i).getInt("id");

		    //Load all textures
		    int texture = 0;
		    int texture_last = 0;
		    String texturePath = arr.getJSONObject(i).getString("texture");
		    if (!texturePath.equals("null")) {
			    MultitextureReturnData texturedata = TextureLoader.loadMultitexture(texturePath);
			    texture = texturedata.first_index;
			    texture_last = texturedata.last_index;
		    }
		    
		    //Add new tile
		    Tile_Data object = new Tile_Data(name, id, texture, texture_last);
		    tiles.add(object);
		}
		
		//Load all objects
		//--------------
		objects = new ArrayList<Object_Data>();
		source = DataIO.readByte("res/data/objects.json");
		obj = new JSONObject(new String(source));

		arr = obj.getJSONArray("objects");
		for (int i = 0; i < arr.length(); i++)
		{
			//Generic data
			String name = arr.getJSONObject(i).getString("name");
		    int id = arr.getJSONObject(i).getInt("id");
		    boolean wall = arr.getJSONObject(i).getBoolean("wall");
		    boolean destroyable = arr.getJSONObject(i).getBoolean("destroyable");
		    float red = arr.getJSONObject(i).getFloat("color.r");
		    float green = arr.getJSONObject(i).getFloat("color.g");
		    float blue = arr.getJSONObject(i).getFloat("color.b");
		    float alpha = arr.getJSONObject(i).getFloat("color.a");
		    int health = arr.getJSONObject(i).getInt("health");
		    int drop = arr.getJSONObject(i).getInt("drop");
		    
		    //Load all textures
		    int texture = 0;
		    int texture_last = 0;
		    String texturePath = arr.getJSONObject(i).getString("texture");
		    if (!texturePath.equals("null")) {
			    MultitextureReturnData texturedata = TextureLoader.loadMultitexture(texturePath);
			    texture = texturedata.first_index;
			    texture_last = texturedata.last_index;
		    }
		    
		    //Add new object
		    Object_Data object = new Object_Data(name, id, texture, texture_last, wall, destroyable, new Vector4f(red, green, blue, alpha), health, drop);
		    objects.add(object);
		}
		
		//Load all creatures
		//--------------
		creatures = new ArrayList<Creature_Data>();
		source = DataIO.readByte("res/data/creatures.json");
		obj = new JSONObject(new String(source));

		arr = obj.getJSONArray("creatures");
		for (int i = 0; i < arr.length(); i++)
		{
			//Generic data
			String name = arr.getJSONObject(i).getString("name");
		    int id = arr.getJSONObject(i).getInt("id");
		    float walkspeed = arr.getJSONObject(i).getFloat("walkspeed");
		    boolean friendly = arr.getJSONObject(i).getBoolean("friendly");
		    boolean ghost = arr.getJSONObject(i).getBoolean("ghost");
		    float knockback = arr.getJSONObject(i).getFloat("knockback");
		    float melee = arr.getJSONObject(i).getFloat("melee");
		    float health = arr.getJSONObject(i).getFloat("health");
		    float regen = arr.getJSONObject(i).getFloat("regen");
		    float stamina = arr.getJSONObject(i).getFloat("stamina");
		    float staminaregen = arr.getJSONObject(i).getFloat("staminaregen");
		    float red = arr.getJSONObject(i).getFloat("color.r");
		    float green = arr.getJSONObject(i).getFloat("color.g");
		    float blue = arr.getJSONObject(i).getFloat("color.b");
		    float alpha = arr.getJSONObject(i).getFloat("color.a");
		    
		    //Load all textures
		    int texture = 0;
		    int texture_last = 0;
		    String texturePath = arr.getJSONObject(i).getString("texture");
		    if (!texturePath.equals("null")) {
			    MultitextureReturnData texturedata = TextureLoader.loadMultitexture(texturePath);
			    texture = texturedata.first_index;
			    texture_last = texturedata.last_index;
		    }

		    //Drops
		    ArrayList<Drop> drops = new ArrayList<Drop>();
		    JSONArray arrd = arr.getJSONObject(i).getJSONArray("drops");
			for (int d = 0; d < arrd.length(); d++)
			{
			    int d_id = arrd.getJSONObject(d).getInt("id");
			    int d_min = arrd.getJSONObject(d).getInt("min");
			    int d_max = arrd.getJSONObject(d).getInt("max");
			    
				Drop drop = new Drop(d_id, d_min, d_max);
				drops.add(drop);
			}
		    
		    Creature_Data object = new Creature_Data(name, id, texture, texture_last, walkspeed, friendly, ghost, knockback, melee, health, regen, stamina, staminaregen, drops, new Vector4f(red, green, blue, alpha));
		    creatures.add(object);
		}
		
		//Load all biomes
		//--------------
		biomes = new ArrayList<Biome_Data>();
		source = DataIO.readByte("res/data/biomes.json");
		obj = new JSONObject(new String(source));

		arr = obj.getJSONArray("biomes");
		for (int i = 0; i < arr.length(); i++)
		{
			String name = arr.getJSONObject(i).getString("name");
		    int id = arr.getJSONObject(i).getInt("id");
		    float temperature = arr.getJSONObject(i).getFloat("temperature");
		    float humidity = arr.getJSONObject(i).getFloat("humidity");
		    float noise_scale = arr.getJSONObject(i).getFloat("noise_scale");

		    //Drops
		    ArrayList<BiomeTileHeight_Data> height_map = new ArrayList<BiomeTileHeight_Data>();
		    JSONArray arrd = arr.getJSONObject(i).getJSONArray("height_map");
			for (int d = 0; d < arrd.length(); d++)
			{
			    float height = arrd.getJSONObject(d).getFloat("height");
			    int tile_id = arrd.getJSONObject(d).getInt("tile_id");
			    int grass_id = arrd.getJSONObject(d).getInt("grass_id");
			    float grass_rarity = arrd.getJSONObject(d).getFloat("grass_rarity");
			    int plant_id = arrd.getJSONObject(d).getInt("plant_id");
			    float plant_rarity = arrd.getJSONObject(d).getFloat("plant_rarity");
			    
			    BiomeTileHeight_Data height_data = new BiomeTileHeight_Data(height, tile_id, grass_id, grass_rarity, plant_id, plant_rarity);
			    height_map.add(height_data);
			}
		    
			Biome_Data object = new Biome_Data(name, id, temperature, humidity, noise_scale, height_map);
			biomes.add(object);
		}
		
		//Load all tiles
		//--------------
		particles = new ArrayList<Particle_Data>();
		source = DataIO.readByte("res/data/particles.json");
		obj = new JSONObject(new String(source));

		arr = obj.getJSONArray("particles");
		for (int i = 0; i < arr.length(); i++)
		{
			//Generic data
		    String name = arr.getJSONObject(i).getString("name");
		    int id = arr.getJSONObject(i).getInt("id");
		    float life_time = arr.getJSONObject(i).getFloat("life_time");

		    //Load all textures
		    int texture = 0;
		    int texture_last = 0;
		    String texturePath = arr.getJSONObject(i).getString("texture");
		    if (!texturePath.equals("null")) {
			    MultitextureReturnData texturedata = TextureLoader.loadMultitexture(texturePath);
			    texture = texturedata.first_index;
			    texture_last = texturedata.last_index;
		    }
		    
		    //Add new tile
		    Particle_Data object = new Particle_Data(name, id, texture, texture_last, life_time);
		    particles.add(object);
		}
		
		
		Logger.out("Successfully loaded " + items.size() + " items, " + tiles.size() + " tiles, " + objects.size() + " objects, " + creatures.size() + " creatures, " + biomes.size() + " biomes and " + particles.size() + " particles.");
	}
	
	public static class Particle_Data {
		public String name = "null";
		public int id = 0;
		public int texture = 0;
		public int texture_last = 0;
		public float life_time = 0.0f;
		
		public Particle_Data(String _name, int _id, int _texture, int _texture_last, float _life_time) {
			name = _name;
			id = _id;
			texture = _texture;
			texture_last = _texture_last;
			life_time = _life_time;
		}
	}
	
	public static class Item_Data
	{
		public String name = "null";
		public int id = 0;
		public int texture = 0;
		public int texture_last = 0;

		public int stack_size = 0;

		public float melee_damage = 0;
		public float melee_kb = 0;
		public float break_speed = 0;

		public int placedAs;

		public Item_Data(String _name, int _id, int _texture, int _texture_last, int _stack, float _melee_damage, float _melee_kb, float _break_speed, int _placedAs) {
			name = _name;
			id = _id;
			texture = _texture;
			texture_last = _texture_last;
			stack_size = _stack;
			melee_damage = _melee_damage;
			melee_kb = _melee_kb;
			break_speed = _break_speed;
			placedAs = _placedAs;
		}
	}

	public static class Tile_Data
	{
		public String name = "null";
		public int id = 0;
		public int texture = 0;
		public int texture_last = 0;

		public Tile_Data(String _name, int _id, int _texture, int _texture_last) {
			name = _name;
			id = _id;
			texture = _texture;
			texture_last = _texture_last;
		}
	}

	public static class Object_Data
	{
		public String name = "null";
		public int id = 0;
		public int texture = 0;
		public int texture_last = 0;
		public boolean wall = false;
		public boolean destroyable = false;
		public Vector4f color = new Vector4f(0.0f);

		public int health = 0;
		public int dropsAs = 0;

		public Object_Data(String _name, int _id, int _texture, int _last_texture, boolean _wall, boolean _destroyable, Vector4f _color, int _health, int _dropsAs) {
			name = _name;
			id = _id;
			texture = _texture;
			wall = _wall;
			texture_last = _last_texture;
			destroyable = _destroyable;
			color = _color;

			health = _health;
			dropsAs = _dropsAs;
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
		public String name = "null";
		public int id = 0;
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
		
		public static class Drop {
			public int id = 0;
			public int min = 0; //min count of this drops
			public int max = 0; //max count of this drops
			
			public Drop(int _id, int _min, int _max) {
				id = _id;
				min = _min;
				max = _max;
			}
		}

		public Creature_Data(String _name, int _id, int _texture, int _texture_last, float _walkSpeed, boolean _friendly, boolean _ghost, float _kb, float _melee, float _hp, float _hpg, float _st, float _stg, ArrayList<Drop> _drops, Vector4f _color) {
			name = _name;
			walkSpeed = _walkSpeed;
			id = _id;
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
		public String name = "null";
		public int id = 0;
		public float temperature = 0;	//0 - 1.
		public float humidity = 0;		//0 - 1.
		public float noiseScale = 0;
		public ArrayList<BiomeTileHeight_Data> heightMap;

		public static class BiomeTileHeight_Data
		{
			public float height = 0.0f;
			public int id = 0;
			
			public int grassId = 0;
			public float grassRarity = 0.0f;

			public int plantId = 0;
			public float plantRarity = 0.0f;
			
			public BiomeTileHeight_Data(float _height, int _id, int _grassId, float _grassRarity, int _plantId, float _plantRarity) {
				height = _height;
				id = _id;
				grassId = _grassId;
				grassRarity = _grassRarity;
				plantId = _plantId;
				plantRarity = _plantRarity;
			}
		}

		Biome_Data(String _name, int _id, float _temperature, float _humidity, float _noiseScale, ArrayList<BiomeTileHeight_Data> _heightMap) {
			name = _name;
			id = _id;
			temperature = _temperature;
			humidity = _humidity;
			noiseScale = _noiseScale;
			heightMap = _heightMap;
		}
	}

}
