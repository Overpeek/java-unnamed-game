package world;

import java.util.ArrayList;

import org.joml.Vector2f;
import org.joml.Vector3f;

import creatures.Creature;
import creatures.Item;
import creatures.Zombie;
import graphics.Renderer;
import graphics.Renderer.VertexData;
import logic.Database;
import logic.Game;
import logic.Settings;
import utility.Colors;
import utility.Logger;
import utility.Maths;

public class Map {

	public static class MapTile {
		int m_tile;
		int m_object;
		int m_objectHealth;

		MapTile(int tile, int object, int hp) {
			m_tile = tile;
			m_object = object;
			m_objectHealth = hp;
		}

		MapTile() {
			m_tile = 0;
			m_object = 0;
			m_objectHealth = 0;
		}
	};

	private MapTile tiles[][];
	private String name;

	private ArrayList<Creature> creatures;
	
	
	
	public boolean create(String _name) {
		Logger.out("Generating map...  ");
		name = _name;

		
		long time = System.nanoTime();
		//Get noisemaps
		FastNoiseSIMD* noise = FastNoiseSIMD::NewFastNoiseSIMD(time);

	#define NOISEMAP_WITH_GENERIC_SETTINGS noise->GetSimplexFractalSet(0, 0, 0, MAP_SIZE, MAP_SIZE, 1, 1.0f)
		
		//Biomemap 0
		noise = FastNoiseSIMD::NewFastNoiseSIMD(time + 0); noise->SetFractalOctaves(MAP_BIOME_OCTA); noise->SetFrequency(MAP_BIOME_FREQ);
		m_biomenoise1 = NOISEMAP_WITH_GENERIC_SETTINGS;

		//Biomemap 1
		noise = FastNoiseSIMD::NewFastNoiseSIMD(time + 1); noise->SetFractalOctaves(MAP_BIOME_OCTA); noise->SetFrequency(MAP_BIOME_FREQ);
		m_biomenoise2 = NOISEMAP_WITH_GENERIC_SETTINGS;
		
		//Heightmap
		noise = FastNoiseSIMD::NewFastNoiseSIMD(time + 2); noise->SetFractalOctaves(MAP_OCTA); noise->SetFrequency(MAP_FREQ);
		m_mapnoise = NOISEMAP_WITH_GENERIC_SETTINGS;
		
		//Plantmap 0
		noise = FastNoiseSIMD::NewFastNoiseSIMD(time + 3); noise->SetFractalOctaves(MAP_PLANT1_OCTA); noise->SetFrequency(MAP_PLANT1_FREQ);
		m_plantnoise1 = NOISEMAP_WITH_GENERIC_SETTINGS;
		
		//Plantmap 1
		noise = FastNoiseSIMD::NewFastNoiseSIMD(time + 4); noise->SetFractalOctaves(MAP_PLANT2_OCTA); noise->SetFrequency(MAP_PLANT2_FREQ);
		m_plantnoise2 = NOISEMAP_WITH_GENERIC_SETTINGS;

		
		//Create world based on noisemaps
	#pragma omp parallel for
		for (int x = 0; x < MAP_SIZE; x++)
		{
			for (int y = 0; y < MAP_SIZE; y++)
			{
				int tile, object;
				getInfoFromNoise(tile, object, x, y);
				short health = Database::objects[object].health;
				m_tiles[x][y] = new MapTile(tile, object, health);
			}
		}

		//Cleanup
		noise->FreeNoiseSet(m_biomenoise1);
		noise->FreeNoiseSet(m_biomenoise2);
		noise->FreeNoiseSet(m_mapnoise);
		noise->FreeNoiseSet(m_plantnoise1);
		noise->FreeNoiseSet(m_plantnoise2);



		//Print status
		std::cout << "SUCCESS" << std::endl;

		return true;
	}

	Database::Data_Biome *Map::getTileBiome(float x, float y) {
		x = wrapCoordinate(x, MAP_SIZE);
		y = wrapCoordinate(y, MAP_SIZE);

		float height0 = (m_biomenoise1[int(x + y * MAP_SIZE)] + 1.0f) / 2.0f;
		float height1 = (m_biomenoise2[int(x + y * MAP_SIZE)] + 1.0f) / 2.0f;

		Database::Data_Biome *biome = Database::getBiome(height0, height1);
		return biome;
	}

	int Map::getInfoFromNoiseIfLoop(Database::Data_Biome *biome, float x, float y, int index) {
		if (biome->heightMap[index].grassId != 0) {
			if ((m_plantnoise1[int(x + y * MAP_SIZE)] + 1.0f) / 2.0f > biome->heightMap[index].grassRarity) return biome->heightMap[index].grassId;
		}
		if (biome->heightMap[index].plantId != 0) {
			if ((m_plantnoise2[int(x + y * MAP_SIZE)] + 1.0f) / 2.0f > biome->heightMap[index].plantRarity) return biome->heightMap[index].plantId;
		}
		return 0;
	}

	void Map::getInfoFromNoise(int &tileId, int &objId, float x, float y) {
		Database::Data_Biome *biome = getTileBiome(x, y);
		if (!biome) oe::Logger::out("Biome was null pointer", oe::error);

		if (biome->heightMap.size() == 1) {
			tileId = biome->heightMap[0].id;
			objId = getInfoFromNoiseIfLoop(biome, x, y, 0);
		}
		else {
			float height1 = (m_mapnoise[int(x + y * MAP_SIZE)] + 1.0f) / 2.0f;
			for (int i = 0; i < biome->heightMap.size(); i++)
			{
				if (height1 <= biome->heightMap[i].height) {
					tileId = biome->heightMap[i].id;
					objId = getInfoFromNoiseIfLoop(biome, x, y, i);
					break;
				}
			}
		}
	}

	public String saveLocation(String path) {
		String m_name = "test";
		return path + m_name + "\\";
	}

	public Creature addCreature(float x, float y, int id, boolean item) {
		switch (id) {
		case 1: // Zombie
			creatures.add(new Zombie(x, y));
			break;
		case 2: // Item
			creatures.add(new Item(x, y, id));
			break;
		default:
			Logger.out("Invalid creature id: " + id, Logger.type.WARNING);
			break;
		}

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
		newItem.setVel_x(Maths.random(-0.2f, 0.2f));
		newItem.setVel_y(Maths.random(-0.2f, 0.2f));
		return newItem;
	}

	public void submitToRenderer(Renderer world_renderer, float off_x, float off_y, float corrector) {
		// Map rendering
		Vector2f player_prediction = new Vector2f(
				Game.getPlayer().getX() + Game.getPlayer().getVel_x() * corrector / Settings.UPDATES_PER_SECOND,
				Game.getPlayer().getY() + Game.getPlayer().getVel_y() * corrector / Settings.UPDATES_PER_SECOND);

		int RENDER_HORIZONTAL = (int) (Settings.RENDER_VERTICAL * Game.getWindow().aspect());
		for (int x = -RENDER_HORIZONTAL; x < RENDER_HORIZONTAL; x++) {
			for (int y = -Settings.RENDER_VERTICAL; y < Settings.RENDER_VERTICAL; y++) {
				// Tile to be rendered
				int tile_x = (int) (x + player_prediction.x);
				int tile_y = (int) (y + player_prediction.y);

				// Not trying to render outside of map
				if (tile_x >= Settings.MAP_SIZE || tile_x < 0 || tile_y >= Settings.MAP_SIZE || tile_y < 0)
					continue;

				// Calculate correct positions to render tile at
				MapTile tile = getTile(tile_x, tile_y);
				Database.Tile_Data db_tile = Database.tiles.get(tile.m_tile);
				Database.Object_Data db_obj = Database.objects.get(tile.m_object);
				float rx = (tile_x + off_x) * Settings.TILE_SIZE * Game.renderScale();
				float ry = (tile_y + off_y) * Settings.TILE_SIZE * Game.renderScale();

				// Renter tile
				Vector3f pos = new Vector3f(rx, ry, 0.0f);
				Vector2f size = new Vector2f(Settings.TILE_SIZE).mul(Game.renderScale());

				// renderer->pointRenderer->submitVertex(oe::VertexData(pos, size,
				// db_tile.texture, OE_COLOR_WHITE));

				world_renderer.submitVertex(new VertexData(pos, size, db_tile.texture, Colors.WHITE));
				// renderer->quadRenderer->submitVertex(oe::VertexData(glm::vec3(pos.x, pos.y,
				// pos.z), glm::vec2(0.0f, 0.0f), db_tile.texture, OE_COLOR_WHITE));
				// renderer->quadRenderer->submitVertex(oe::VertexData(glm::vec3(pos.x, pos.y +
				// size.y, pos.z), glm::vec2(0.0f, 1.0f), db_tile.texture, OE_COLOR_WHITE));
				// renderer->quadRenderer->submitVertex(oe::VertexData(glm::vec3(pos.x + size.x,
				// pos.y + size.y, pos.z), glm::vec2(1.0f, 1.0f), db_tile.texture,
				// OE_COLOR_WHITE));
				// renderer->quadRenderer->submitVertex(oe::VertexData(glm::vec3(pos.x + size.x,
				// pos.y, pos.z), glm::vec2(1.0f, 0.0f), db_tile.texture, OE_COLOR_WHITE));

				// Render object on tile
				if (db_obj.id != 0) {
					int objTexture = getObjectTexture(tile_x, tile_y);
					pos = new Vector3f(rx, ry, 0.0f);
					size = new Vector2f(Settings.TILE_SIZE).mul(Game.renderScale());

					// renderer->pointRenderer->submitVertex(oe::VertexData(pos, size, objTexture,
					// glm::vec4(db_object.color, 1.0f)));

					world_renderer.submitVertex(new VertexData(pos, size, objTexture, db_obj.color));
					// renderer->quadRenderer->submitVertex(oe::VertexData(glm::vec3(pos.x, pos.y,
					// pos.z), glm::vec2(0.0f, 0.0f), objTexture, glm::vec4(db_object.color,
					// 1.0f)));
					// renderer->quadRenderer->submitVertex(oe::VertexData(glm::vec3(pos.x, pos.y +
					// size.y, pos.z), glm::vec2(0.0f, 1.0f), objTexture, glm::vec4(db_object.color,
					// 1.0f)));
					// renderer->quadRenderer->submitVertex(oe::VertexData(glm::vec3(pos.x + size.x,
					// pos.y + size.y, pos.z), glm::vec2(1.0f, 1.0f), objTexture,
					// glm::vec4(db_object.color, 1.0f)));
					// renderer->quadRenderer->submitVertex(oe::VertexData(glm::vec3(pos.x + size.x,
					// pos.y, pos.z), glm::vec2(1.0f, 0.0f), objTexture, glm::vec4(db_object.color,
					// 1.0f)));
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

		if (Database.objects.get(thistile.m_object).texture != Database.objects.get(thistile.m_object).texture_last) {
			MapTile right = getTile(tile_x + 1, tile_y);
			MapTile top = getTile(tile_x, tile_y - 1);
			MapTile left = getTile(tile_x - 1, tile_y);
			MapTile bottom = getTile(tile_x, tile_y + 1);

			MapTile topright = getTile(tile_x + 1, tile_y - 1);
			MapTile topleft = getTile(tile_x - 1, tile_y - 1);
			MapTile bottomleft = getTile(tile_x - 1, tile_y + 1);
			MapTile bottomright = getTile(tile_x + 1, tile_y + 1);

			boolean rightAir = (right != null) && !Database.objects.get(right.m_object).wall;
			boolean topAir = (top != null) && !Database.objects.get(top.m_object).wall;
			boolean leftAir = (left != null) && !Database.objects.get(left.m_object).wall;
			boolean bottomAir = (bottom != null) && !Database.objects.get(bottom.m_object).wall;

			boolean topRightAir = (topright != null) && !Database.objects.get(topright.m_object).wall;
			boolean topLeftAir = (topleft != null) && !Database.objects.get(topleft.m_object).wall;
			boolean bottomLeftAir = (bottomleft != null) && !Database.objects.get(bottomleft.m_object).wall;
			boolean bottomRightAir = (bottomright != null) && !Database.objects.get(bottomright.m_object).wall;

			return Database.objects.get(thistile.m_object).getTexture(rightAir, topAir, leftAir, bottomAir, topRightAir,
					topLeftAir, bottomLeftAir, bottomRightAir);
		}
		return Database.objects.get(thistile.m_object).texture;
	}

	float wrapCoordinate(float coord, float n) {
		while (coord >= n) {
			coord -= n;
		}
		while (coord < 0) {
			coord += n;
		}
		return coord;
	}

	MapTile getTile(int x, int y) {
		x = (int) wrapCoordinate(x, Settings.MAP_SIZE);
		y = (int) wrapCoordinate(y, Settings.MAP_SIZE);

		return tiles[x][y];
	}
}
