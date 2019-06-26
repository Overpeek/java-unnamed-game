package world;

import java.nio.ByteBuffer;
import java.util.ArrayList;

import creatures.Creature;
import creatures.Player;
import graphics.Renderer;
import graphics.VertexData;
import logic.Database;
import logic.Database.Biome_Data;
import logic.Main;
import logic.Settings;
import utility.Colors;
import utility.Logger;
import utility.Maths;
import utility.SimplexNoise_octave;
import utility.vec2;
import utility.vec3;

public class Map {

	public static class MapTile {
		public int tile;
		public int object;
		public int objectHealth;

		MapTile(int tile, int object, int hp) {
			this.tile = tile;
			this.object = object;
			this.objectHealth = hp;
		}
	};

	private MapTile tiles[][];
	private String name;

	private ArrayList<Creature> creatures;
	private Player player;

	private SimplexNoise_octave mapnoise;
	private SimplexNoise_octave biomenoise1;
	private SimplexNoise_octave biomenoise2;
	private SimplexNoise_octave plantnoise1;
	private SimplexNoise_octave plantnoise2;
	
	private final int tileCount = Settings.MAP_SIZE * Settings.MAP_SIZE;
	private int processedTileCount = 0;

	
	public Map() {
		creatures = new ArrayList<Creature>();
		name = "null";
	}
	
	public boolean create(String _name, int seed) {
		Logger.info("Generating map...  ");
		name = _name;
		
		long time = seed;
		if (time == 0) {
			time = System.currentTimeMillis();			
		}
		mapnoise = new SimplexNoise_octave((int) time);
		mapnoise.setFreq(Settings.MAP_FREQ);
		biomenoise1 = new SimplexNoise_octave((int) time + 1);
		biomenoise1.setFreq(Settings.MAP_BIOME_FREQ);
		biomenoise2 = new SimplexNoise_octave((int) time + 2);
		biomenoise2.setFreq(Settings.MAP_BIOME_FREQ);
		plantnoise1 = new SimplexNoise_octave((int) time + 3);
		plantnoise1.setFreq(Settings.MAP_PLANT1_FREQ);
		plantnoise2 = new SimplexNoise_octave((int) time + 4);
		plantnoise2.setFreq(Settings.MAP_PLANT2_FREQ);
		
		
		//Create world based on noisemaps
		tiles = new MapTile[Settings.MAP_SIZE][Settings.MAP_SIZE];
		processedTileCount = 0;
		for (int x = 0; x < Settings.MAP_SIZE; x++)
		{
			for (int y = 0; y < Settings.MAP_SIZE; y++)
			{
				MapTile tile = getInfoFromNoise(x, y);
				tiles[x][y] = tile;
				processedTileCount++;
			}
		}



		//Print status
		Logger.info("Map generation successful");

		return true;
	}

	//public Biome_Data getTileBiome(float x, float y) {
	//	x = wrapCoordinate(x, Settings.MAP_SIZE);
	//	y = wrapCoordinate(y, Settings.MAP_SIZE);
    //
	//	
	//	float height0 = Maths.map((float) biomenoise1.noiseOctave(x, y, Settings.OCTAVES, Settings.PERSISTANCE, Settings.LACUNARITY), -1.0f, 1.0f, 0.0f, 1.0f);
	//	float height1 = Maths.map((float) biomenoise2.noiseOctave(x, y, Settings.OCTAVES, Settings.PERSISTANCE, Settings.LACUNARITY), -1.0f, 1.0f, 0.0f, 1.0f);
    //
	//	Biome_Data biome = getBiome(height0, height1);
	//	//biome = Database.getBiome(0.7f, 0.7f);
	//	//Logger.debug("Biome: " + biome.name);
	//	return biome;
	//}

	private int getInfoFromNoiseIfLoop(Biome_Data biome, float x, float y, int index) {
		for (int i = 0; i < biome.heightMap.get(index).objects.size(); i++) {
			float chance = Maths.random(0.0f, 1.0f);
			
			if (chance > biome.heightMap.get(index).objects.get(i).rarity) {
				return Database.getObject(biome.heightMap.get(index).objects.get(i).object).index;
			}
		}
		
		return Database.getObject("air").index;
	}

	private MapTile getInfoFromNoise(float x, float y) {
		Biome_Data biome = getBiome(x, y);
		if (biome == null) Logger.error("Biome was null pointer");

		int tileIndex = 0;
		int objIndex = 0;
		float height1 = Maths.map((float) mapnoise.noiseOctave(x, y, Settings.OCTAVES, Settings.PERSISTANCE, Settings.LACUNARITY), -1.0f, 1.0f, 0.0f, 1.0f);
		for (int i = 0; i < biome.heightMap.size(); i++)
		{
			if (height1 <= biome.heightMap.get(i).height) {
				String tileName = biome.heightMap.get(i).tile;
				tileIndex = Database.getTile(tileName).index;
				objIndex = getInfoFromNoiseIfLoop(biome, x, y, i);
				break;
			}
		}
		
		return new MapTile(tileIndex, objIndex, Database.getObject(objIndex).health);
	}

	//id is either item or creature
	public Creature addCreature(float x, float y, int id, boolean is_item) {
		//if (Database.getCreature(creature) == null) { Logger.crit("Not a valid creature: " + creature); return null; }
		
		//TODO: IMPLEMENT BETTER CREATURE ADDING SYSTEM

		return creatures.get(creatures.size() - 1);
	}

	//id is either item or creature
	public Creature addCreature(float x, float y, String id, boolean is_item) {
		//if (Database.getCreature(creature) == null) { Logger.crit("Not a valid creature: " + creature); return null; }
		
		//TODO: IMPLEMENT BETTER CREATURE ADDING SYSTEM

		return creatures.get(creatures.size() - 1);
	}

	public void removeCreature(int i) {
		if (i >= creatures.size())
			return;
		if (creatures.get(i) == null)
			return;
		creatures.remove(i);
	}

	public void removeCreature(Creature creature) {
		for (int i = 0; i < creatures.size(); i++) {
			if (creatures.get(i) == creature) {
				removeCreature(i);
				return;
			}
		}
	}

	public Creature itemDrop(float x, float y, int id) {
		Creature newItem = addCreature(x, y, id, true);
		newItem.setVel(new vec2(Maths.random(-0.2f, 0.2f), Maths.random(-0.2f, 0.2f)));
		return newItem;
	}

	public void submitToRenderer(Renderer world_renderer, float off_x, float off_y, float corrector) {
		// Map rendering
		vec2 player_prediction = new vec2(
				Main.game.getPlayer().getPos().x + Main.game.getPlayer().getVel().x * corrector / Settings.UPDATES_PER_SECOND,
				Main.game.getPlayer().getPos().y + Main.game.getPlayer().getVel().y * corrector / Settings.UPDATES_PER_SECOND);

		int RENDER_HORIZONTAL = (int) (Settings.RENDER_DIST / Main.game.renderWidthScale() * Main.game.defaultAspect());
		int RENDER_VERTICAL = (int) (Settings.RENDER_DIST / Main.game.renderScale());
		for (int x = -RENDER_HORIZONTAL; x < RENDER_HORIZONTAL; x++) {
			for (int y = -RENDER_VERTICAL; y < RENDER_VERTICAL; y++) {
				// Tile to be rendered
				int tile_x = (int) (x + player_prediction.x);
				int tile_y = (int) (y + player_prediction.y);

				// Not trying to render outside of map
				if (tile_x >= Settings.MAP_SIZE || tile_x < 0 || tile_y >= Settings.MAP_SIZE || tile_y < 0)
					continue;

				// Calculate correct positions to render tile at
				MapTile tile = getTile(tile_x, tile_y);
				Database.Tile_Data db_tile = Database.getTile(tile.tile);
				Database.Object_Data db_obj = Database.getObject(tile.object);
				float rx = (tile_x + off_x) * Settings.TILE_SIZE * Main.game.renderScale();
				float ry = (tile_y + off_y) * Settings.TILE_SIZE * Main.game.renderScale();

				// Renter tile
				vec3 pos = new vec3(rx, ry, 0.0f);
				vec2 size = new vec2(Settings.TILE_SIZE * Main.game.renderScale(), Settings.TILE_SIZE * Main.game.renderScale());

				world_renderer.points.submitVertex(new VertexData(pos, size, db_tile.texture, Colors.WHITE));
				//world_renderer.submitQuad(pos, size, db_tile.texture, Colors.WHITE);

				// Render object on tile
				if (!db_obj.data_name.equals("air")) {
					int objTexture = getObjectTexture(tile_x, tile_y);
					pos = new vec3(rx, ry, 0.0f);
					size = new vec2(Settings.TILE_SIZE * Main.game.renderScale(), Settings.TILE_SIZE * Main.game.renderScale());

					world_renderer.points.submitVertex(new VertexData(pos, size, objTexture, db_obj.color));
					//world_renderer.submitQuad(pos, size, objTexture, Colors.WHITE);
				}

			}
		}
	}

	private int getObjectTexture(int tile_x, int tile_y) {
		MapTile thistile = getTile(tile_x, tile_y);
		if (thistile == null) {
			Logger.crit("Tile: " + tile_x + ", " + tile_y + " is null!");
			return 0;
		}

		if (Database.getObject(thistile.object).texture != Database.getObject(thistile.object).texture_last) {
			MapTile right = getTile(tile_x + 1, tile_y);
			MapTile top = getTile(tile_x, tile_y - 1);
			MapTile left = getTile(tile_x - 1, tile_y);
			MapTile bottom = getTile(tile_x, tile_y + 1);

			MapTile topright = getTile(tile_x + 1, tile_y - 1);
			MapTile topleft = getTile(tile_x - 1, tile_y - 1);
			MapTile bottomleft = getTile(tile_x - 1, tile_y + 1);
			MapTile bottomright = getTile(tile_x + 1, tile_y + 1);

			boolean rightAir = (right != null) && !Database.getObject(right.object).wall;
			boolean topAir = (top != null) && !Database.getObject(top.object).wall;
			boolean leftAir = (left != null) && !Database.getObject(left.object).wall;
			boolean bottomAir = (bottom != null) && !Database.getObject(bottom.object).wall;

			boolean topRightAir = (topright != null) && !Database.getObject(topright.object).wall;
			boolean topLeftAir = (topleft != null) && !Database.getObject(topleft.object).wall;
			boolean bottomLeftAir = (bottomleft != null) && !Database.getObject(bottomleft.object).wall;
			boolean bottomRightAir = (bottomright != null) && !Database.getObject(bottomright.object).wall;

			return Database.getObject(thistile.object).getTexture(rightAir, topAir, leftAir, bottomAir, topRightAir,
					topLeftAir, bottomLeftAir, bottomRightAir);
		}
		return Database.getObject(thistile.object).texture;
	}

	private static float wrapCoordinate(float coord, float n) {
		while (coord >= n) {
			coord -= n;
		}
		while (coord < 0) {
			coord += n;
		}
		return coord;
	}
	
	public String getWorldName() {
		return name;
	}

	public MapTile getTile(int x, int y) {
		x = (int) wrapCoordinate(x, Settings.MAP_SIZE);
		y = (int) wrapCoordinate(y, Settings.MAP_SIZE);

		return tiles[x][y];
	}

	public void save() {
		Logger.info("Saving world...  ");
		
		//Load tiles
		final int bytesPerTile = 4 + 4 + 4;
		ByteBuffer tile_data = ByteBuffer.allocate(Settings.MAP_SIZE * Settings.MAP_SIZE * bytesPerTile);
		for (int x = 0; x < Settings.MAP_SIZE; x++)
		{
			for (int y = 0; y < Settings.MAP_SIZE; y++)
			{
				tile_data.putInt(tiles[x][y].tile);
				tile_data.putInt(tiles[x][y].object);
				tile_data.putInt(tiles[x][y].objectHealth);
			}
		}


		//Load creatures
		final int bytesPerCreature = 4 + 4 + 4 + 4;
		ByteBuffer creature_data = ByteBuffer.allocate(creatures.size() * bytesPerCreature);
		for (int i = 0; i < creatures.size(); i++) {
			creature_data.putFloat(creatures.get(i).getPos().x);
			creature_data.putFloat(creatures.get(i).getPos().y);
			creature_data.putInt(creatures.get(i).getId());
			creature_data.putInt((creatures.get(i).isItem() == false) ? 0 : 1);
		}
		
		//Compress data
		//TODO
		
		//Write data
		Database.writeDataBuffer(tile_data, "/world-data/tiles.dat");
		Database.writeDataBuffer(creature_data, "/world-data/creatures.dat");
		
		Logger.info("World saved");
	}

	public boolean load(String name2) {
		Logger.info("Loading world...  ");
		name = name2;

		ByteBuffer tile_data = Database.readDataBuffer("/world-data/tiles.dat");
		ByteBuffer creature_data = Database.readDataBuffer("/world-data/creatures.dat");


		////Uncompress tiledata
		//unsigned long uncompressedSize = MAP_SIZE * MAP_SIZE * 3 * sizeof(short);
		//Byte* uncompressedTiles = new Byte[uncompressedSize];
		//int state = uncompress(uncompressedTiles, &uncompressedSize, tile_data, tile_data_size);
		//if (state != Z_OK) {
		//	oe::Logger::out("Error uncompressing save file! ", state, oe::error);
		//}
		//short int *tileData = (short int*)uncompressedTiles;

		
		//Load tiles
		if (tile_data == null) {
			Logger.warn("Couldn't load world \"" + name + "\"");
			return false;
		}
		unload();
		processedTileCount = 0;
		for (int x = 0; x < Settings.MAP_SIZE; x++)
		{
			for (int y = 0; y < Settings.MAP_SIZE; y++)
			{
				tiles[x][y] = new MapTile(
					tile_data.getInt(),
					tile_data.getInt(),
					tile_data.getInt()
				);
				processedTileCount++;
			}
		}


		//Load creatures
		if (creature_data != null) {
			while(true) {
				try {
					addCreature(creature_data.getFloat(), creature_data.getFloat(), creature_data.getInt(), (creature_data.getInt() != 0));
				} catch (Exception e) {
					break; //All creatures loaded
				}
			}
		}
		
		return true;
	}

	boolean unload() {
		creatures = new ArrayList<Creature>();
		return true;
	}
	
	private Biome_Data getFullBiome(float x, float y) {
		float height0 = Maths.map((float) biomenoise1.noiseOctave(x, y, Settings.OCTAVES, Settings.PERSISTANCE, Settings.LACUNARITY), -1.0f, 1.0f, 0.0f, 1.0f);
		float height1 = Maths.map((float) biomenoise2.noiseOctave(x, y, Settings.OCTAVES, Settings.PERSISTANCE, Settings.LACUNARITY), -1.0f, 1.0f, 0.0f, 1.0f);
		
		ArrayList<Biome_Data> applicable_biomes = new ArrayList<Biome_Data>();
		float selectedHeight = 0.0f;
		
		//Find first biome with good height
		for (int i = 0; i < Database.getBiomeCount(); i++) {
			if (height0 <= Database.getBiome(i).height) {
				//Logger.debug("Biome: " + getBiome(i).name + ", Height: " + height0 + ", " + getBiome(i).height);
				selectedHeight = Database.getBiome(i).height;
				applicable_biomes.add(Database.getBiome(i));

				//Collect all biomes with that height
				for (int j = i+1; j < Database.getBiomeCount(); j++) {
					if (selectedHeight == Database.getBiome(j).height) {
						applicable_biomes.add(Database.getBiome(j));
					}
				}
				break;
			}
		}
		
		//Map height1 (-1.0, 1.0) to biome in array (0, array.lenght)
		int index = 0;
		if (applicable_biomes.size() > 1) {
			index = (int) Math.floor(Maths.map(height1, -1.0f, 1.0f, 0, applicable_biomes.size() - 0.0001f));
		}
		
		return applicable_biomes.get(index);
	}

	public Biome_Data getBiome(float x, float y) {
		final int scale = 3;
		Biome_Data start_biome = getFullBiome(x, y);
		Biome_Data end_biome = null;

		for (int i = 0; i < 1; i++) {
			{
				end_biome = getFullBiome(x + scale, y);
				if (!start_biome.data_name.equals(end_biome.data_name)) {
					break;
				}
			}
			{
				end_biome = getFullBiome(x, y + scale);
				if (!start_biome.data_name.equals(end_biome.data_name)) {
					break;
				}
			}
			{
				end_biome = getFullBiome(x + scale, y + scale);
				if (!start_biome.data_name.equals(end_biome.data_name)) {
					break;
				}
			}
		}
		
		return start_biome;
	}

	public void debugCeilCreatures() {
		//Loop trough all the creatures
		for (int i = 0; i < creatures.size(); i++)
		{
			if (creatures.get(i) != null) {
				creatures.get(i).setPos(new vec2(Math.round(creatures.get(i).getPos().x), Math.round(creatures.get(i).getPos().y)));

				//Change objects around the creature
				for (int x = -1; x < 2; x++) {
					for (int y = -1; y < 2; y++) {
						if (x == 0 && y == 0) continue;
						
						MapTile tmp = getTile((int) (creatures.get(i).getPos().x + x), (int) creatures.get(i).getPos().y + y);
						if (tmp != null) tmp.object = 4;
					}
				}

				return;
			}
		}
	}

	public float tileState() {
		return (float)processedTileCount / (float)tileCount;
	}

}
