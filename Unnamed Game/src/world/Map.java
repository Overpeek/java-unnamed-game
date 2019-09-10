package world;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;

import creatures.Creature;
import creatures.Item;
import creatures.Player;
import graphics.Renderer;
import graphics.Shader;
import logic.Database;
import logic.Database.Biome_Data;
import settings.CompiledSettings;
import logic.Main;
import logic.Noisemaps;
import logic.ParticleManager;
import logic.TextureLoader;
import utility.Logger;
import utility.Maths;
import utility.SaveManager;
import utility.mat4;
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
		
		int[] getSaveData() {
			return new int[]{ tile, object, objectHealth };
		}
		
		MapTile loadSaveData(int data[]) {
			return new MapTile(data[0], data[1], data[2]);
		}
		
		final static int sizeof = Integer.BYTES * 3;
		
		@Override
		public String toString() {
			return "MapTile[Tile: " + Database.getTile(tile).data_name + ", Object: " + Database.getObject(object).data_name + ", HP: " + objectHealth + "]";
		}
	};

	private int processedTileCount = 0;
	private MapTile tiles[][];
	private RenderChunk chunks[][];
	private Noisemaps noisemaps;
	private String name;
	
	private Shader world_shader;
	private Renderer world_renderer;
	
	private ArrayList<Creature> creatures;
	private Player player;
	
	
	
	public MapTile[][] getTiles() {
		return tiles;
	}
	
	public Map(boolean game) {
		creatures = new ArrayList<Creature>();
		name = "null";

		if (game) {
			if (world_shader == null) world_shader = Shader.multiTextureShader();
			if (world_renderer == null) world_renderer = new Renderer();
			
			player = (Player) new Player().construct(0, 0, null);
		}
	}
	
	public boolean create(String _name, int seed) {
		Logger.info("Generating map...  ");
		name = _name;
		
		long time = seed;
		if (time == 0) {
			time = System.currentTimeMillis();			
		}
		noisemaps = new Noisemaps((int) time);
		
		
		//Create world based on noisemaps
		tiles = new MapTile[CompiledSettings.MAP_SIZE_TILES][CompiledSettings.MAP_SIZE_TILES];
		chunks = new RenderChunk[CompiledSettings.MAP_SIZE_CHUNKS][CompiledSettings.MAP_SIZE_CHUNKS];
		processedTileCount = 0;
		for (int x = 0; x < CompiledSettings.MAP_SIZE_TILES; x++)
		{
			for (int y = 0; y < CompiledSettings.MAP_SIZE_TILES; y++)
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

	public void save() {
		Logger.info("Saving world");
		
		// Load tiles
		int size = MapTile.sizeof;
		ByteBuffer tile_data = ByteBuffer.allocate(CompiledSettings.MAP_SIZE_TILES * CompiledSettings.MAP_SIZE_TILES * size);
		IntBuffer as_int_buffer = tile_data.asIntBuffer();
		for (int x = 0; x < CompiledSettings.MAP_SIZE_TILES; x++)
		{
			for (int y = 0; y < CompiledSettings.MAP_SIZE_TILES; y++)
			{
				as_int_buffer.put(tiles[x][y].getSaveData());
			}
		}


		// Load creatures
		final int bytesPerCreature = 4 + 4 + 4 + 4;
		ByteBuffer creature_data = ByteBuffer.allocate(creatures.size() * bytesPerCreature);
		for (int i = 0; i < creatures.size(); i++) {
			creature_data.putFloat(creatures.get(i).getPos().x);
			creature_data.putFloat(creatures.get(i).getPos().y);
			creature_data.putInt(Database.getCreature(creatures.get(i).getId()).index);
			creature_data.putInt((creatures.get(i).isItem() == false) ? 0 : 1);
		}
		
		// Compress data
		byte[] compressed_tile_data = null;
		byte[] compressed_creature_data = null;
		try {
			compressed_tile_data = SaveManager.compress(tile_data.array());
			compressed_creature_data = SaveManager.compress(creature_data.array());
		} catch (IOException e) {
			e.printStackTrace();
			Logger.crit("Couldn't compress the save! Saving uncompressed.");
		}
		tile_data = ByteBuffer.wrap(compressed_tile_data);
		creature_data = ByteBuffer.wrap(compressed_creature_data);
		
		// Write data
		Database.writeByteBuffer(tile_data, "saves", name, "world-data", "tiles.ung");
		Database.writeByteBuffer(creature_data, "saves", name, "world-data", "creatures.ung");
		
		
		// Other saving
		// Player
		player.save("saves", name, "world-data");
		
		Logger.info("World saved");
	}

	public boolean load(String name2) {
		Logger.info("Loading world...  ");
		name = name2;

		ByteBuffer tile_data = null;
		ByteBuffer creature_data = null;
		try {
			tile_data = Database.readByteBuffer("saves", name, "world-data", "tiles.ung");
			creature_data = Database.readByteBuffer("saves", name, "world-data", "creatures.ung");
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		
		if (tile_data == null) {
			Logger.warn("Couldn't load world \"" + name + "\"");
			return false;
		}
		if (creature_data == null) {
			Logger.warn("0 creatures?");
		}


		// Decompress data
		byte[] uncompressed_tile_data = null;
		byte[] uncompressed_creature_data = null;
		try {
			uncompressed_tile_data = SaveManager.decompress(tile_data.array());
			uncompressed_creature_data = SaveManager.decompress(creature_data.array());
			tile_data = ByteBuffer.wrap(uncompressed_tile_data);
			creature_data = ByteBuffer.wrap(uncompressed_creature_data);
		} catch (IOException e) {
			e.printStackTrace();
			Logger.crit("Couldn't decompress the save! Trying to load uncompressed... (Might fail if wasn't saved uncompressed)");
		}
		
		//Load tiles
		unload();
		processedTileCount = 0;
		tiles = new MapTile[CompiledSettings.MAP_SIZE_TILES][CompiledSettings.MAP_SIZE_TILES];
		chunks = new RenderChunk[CompiledSettings.MAP_SIZE_CHUNKS][CompiledSettings.MAP_SIZE_CHUNKS];
		for (int x = 0; x < CompiledSettings.MAP_SIZE_TILES; x++)
		{
			for (int y = 0; y < CompiledSettings.MAP_SIZE_TILES; y++)
			{
				int tile = tile_data.getInt();
				int object = tile_data.getInt();
				int health = tile_data.getInt();
				tiles[x][y] = new MapTile(
					tile,
					object,
					health
				);
				processedTileCount++;
			}
		}


		//Load creatures
		if (creature_data != null) {
			while(true) {
				try {
					float x = creature_data.getFloat();
					float y = creature_data.getFloat();
					String id = Database.getCreature(creature_data.getInt()).data_name;
					boolean item = (creature_data.getInt() != 0);
					
					Creature newCreature;
					if (item) {
						newCreature = new Item().construct(x, y, id);
					} else {
						newCreature = newCreature(x, y, id);
					}
					
					addCreature(newCreature);
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
		//Logger.debug("Biome: " + biome.data_name);
		if (biome == null) Logger.error("Biome was null pointer");

		int tileIndex = 0;
		int objIndex = 0;
		float height1 = Maths.map((float) noisemaps.mapnoise.noiseOctave(x, y, CompiledSettings.OCTAVES, CompiledSettings.PERSISTANCE, CompiledSettings.LACUNARITY), -1.0f, 1.0f, 0.0f, 1.0f);
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

	public Creature addCreature(Creature creature) {
		creatures.add(creature);
		return creature;
	}

	public Creature newCreature(float x, float y, Database.Creature_Data creature_data) {
		return creature_data.construct().construct(x, y, creature_data.data_name);
	}

	public Creature newCreature(float x, float y, String id) {
		return Database.getCreature(id).construct().construct(x, y, id);
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
			if (creatures.get(i).equals(creature)) {
				removeCreature(i);
				return;
			}
		}
	}

	public Item itemDrop(float x, float y, String id) {
		//TODO:
		Item newItem = (Item) new Item().construct(x, y, id);
		newItem.setVel(new vec2(Maths.random(-0.2f, 0.2f), Maths.random(-0.2f, 0.2f)));
		return newItem;
	}
	
	public void update(float ups) {
		
		for (int i = 0; i < creatures.size(); i++) {
			creatures.get(i).update(ups);
		}
		player.update(ups);

		ParticleManager.update(ups);
	
	}

	public void hit(int x, int y, int dmg) {
		
		MapTile tile = getTile(x, y);
		if (tile.object == 0) return;
		
		tile.objectHealth -= dmg;
		if (tile.objectHealth <= 0) {
			
			// Drop
			addCreature(itemDrop(x, y, Database.getItem(Database.getObject(tile.object).dropsAs).data_name));
			
			// Change to air
			tile.object = 0;
			
			// Updates
			updateCloseTiles(x, y);
		}
		
	}
	
	/**
	 * Returns all creatures within radius
	 * */
	public ArrayList<Creature> findAllCreatures(float x, float y, float radius) {
		ArrayList<Creature> in_radius_creatures = new ArrayList<Creature>();
		
		for (int i = 0; i < creatures.size(); i++)
		{
			if (!Maths.isInRange(creatures.get(i).getPos().x, x - radius, x + radius)) continue;
			if (!Maths.isInRange(creatures.get(i).getPos().y, y - radius, y + radius)) continue;
			in_radius_creatures.add(creatures.get(i));
		}
		
		return in_radius_creatures;
	}
	
	/**
	 * shader must have vw_matrix uniform as view matrix
	 * */
	public void draw(float preupdate_scale) {
		
		vec3 camera_pos = new vec3(player.getPos().mulLocal(-CompiledSettings.TILE_SIZE), -1.0f);
		mat4 vw_matrix = new mat4().move(camera_pos);
		mat4 pr_matrix = new mat4().ortho(-Main.game.getWindow().getAspect(), Main.game.getWindow().getAspect(), 1.0f, -1.0f);
		world_shader.setUniformMat4("vw_matrix", vw_matrix);
		world_shader.setUniformMat4("pr_matrix", pr_matrix);
		
		//All visible chunks
		for (RenderChunk[] chunks2 : chunks) { for (RenderChunk chunk : chunks2) {
			chunk.drawChunkMesh();
		}}
		
		//All visible creatures
		for (Creature creature : creatures) {
			creature.draw(world_renderer, preupdate_scale);
		}
		player.draw(world_renderer, preupdate_scale);
		
		//All particles
		ParticleManager.draw(world_renderer);
		
		TextureLoader.getTexture().bind();
		world_renderer.draw();
		world_renderer.clear();
	}
	
	public void updateCloseTiles(int x, int y) {
		
		for (int xo = -1; xo < 2; xo++) {
			for (int yo = -1; yo < 2; yo++) {
				updateTile(x + xo, y + yo);
			}
		}
		
	}
	
	private void updateTile(int x, int y) {
		
		if (!Maths.isInRange(x, 0, CompiledSettings.MAP_SIZE_TILES)) return;
		if (!Maths.isInRange(y, 0, CompiledSettings.MAP_SIZE_TILES)) return;
		
		RenderChunk chunk = chunks[(int) Math.floor(x / CompiledSettings.CHUNK_SIZE)][(int) Math.floor(y / CompiledSettings.CHUNK_SIZE)];
		chunk.updateTile(x % CompiledSettings.CHUNK_SIZE, y % CompiledSettings.CHUNK_SIZE);
		
	}

	//Updates everyhting
	public void generateAllMeshes() {
		
		for (int x = 0; x < CompiledSettings.MAP_SIZE_CHUNKS; x++) {
			for (int y = 0; y < CompiledSettings.MAP_SIZE_CHUNKS; y++) {
				chunks[x][y] = RenderChunk.generateChunkMesh(x * CompiledSettings.CHUNK_SIZE, y * CompiledSettings.CHUNK_SIZE);
			}
		}
		
	}

	public int getObjectTexture(int tile_x, int tile_y) {

		MapTile thistile = getTile(tile_x, tile_y);
		if (thistile == null) {
			Logger.crit("Tile: " + tile_x + ", " + tile_y + " is null!");
			return 0;
		}

		if (Database.getObject(thistile.object).texture != Database.getObject(thistile.object).texture_last) {
			//get all neighbour walls
			boolean neighbours[][] = new boolean[3][3];
			for (int x = 0; x < neighbours.length; x++) {
				for (int y = 0; y < neighbours[x].length; y++) {
					
					if (!Maths.isInRange(tile_x - 1 + x, 0, CompiledSettings.MAP_SIZE_TILES - 1)) {
						neighbours[x][y] = false;
						continue;
					}
					if (!Maths.isInRange(tile_y - 1 + y, 0, CompiledSettings.MAP_SIZE_TILES - 1)) {
						neighbours[x][y] = false;
						continue;
					}
					
					neighbours[x][y] = !Database.getObject(tiles[tile_x - 1 + x][tile_y - 1 + y].object).wall;
				}
			}

			return Database.getObject(thistile.object).getTexture(neighbours);
		}
		return Database.getObject(thistile.object).texture;
	}
	
	public String getWorldName() {
		return name;
	}
	
	public void setTile(int x, int y, String tile) {
		getTile(x, y).tile = Database.getTile(tile).index;
		updateCloseTiles(x, y);
	}
	
	public void setObject(int x, int y, String object) {
		getTile(x, y).object = Database.getObject(object).index;
		updateCloseTiles(x, y);
	}
	
	public void setTile(int x, int y, int tile) {
		getTile(x, y).tile = tile;
		updateCloseTiles(x, y);
	}
	
	public void setObject(int x, int y, int object) {
		getTile(x, y).object = object;
		updateCloseTiles(x, y);
	}

	public MapTile getTile(int x, int y) {
		x = (int) Maths.clamp(x, 0, CompiledSettings.MAP_SIZE_TILES - 1);
		y = (int) Maths.clamp(y, 0, CompiledSettings.MAP_SIZE_TILES - 1);
		return tiles[x][y];
	}
	
	private Biome_Data getFullBiome(float x, float y) {
		float height0 = Maths.map((float) noisemaps.biomenoise1.noiseOctave(x, y, CompiledSettings.OCTAVES, CompiledSettings.PERSISTANCE, CompiledSettings.LACUNARITY), -1.0f, 1.0f, 0.0f, 1.0f);
		float height1 = Maths.map((float) noisemaps.biomenoise2.noiseOctave(x, y, CompiledSettings.OCTAVES, CompiledSettings.PERSISTANCE, CompiledSettings.LACUNARITY), -1.0f, 1.0f, 0.0f, 1.0f);
		
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
		
		if (applicable_biomes.size() == 0) {
			Logger.crit("height0: " + height0);
			Logger.error("Database.getBiomeCount(): " + Database.getBiomeCount());
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
		final int tileCount = CompiledSettings.MAP_SIZE_TILES * CompiledSettings.MAP_SIZE_TILES;
		
		return (float)processedTileCount / (float)tileCount;
	}
	
	public Player getPlayer() {
		return player;
	}

}
