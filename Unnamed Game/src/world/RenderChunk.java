package world;

import graphics.QuadRenderer;
import logic.Database;
import logic.Main;
import logic.Settings;
import logic.TextureLoader;
import utility.Colors;
import utility.vec2;
import utility.vec3;
import utility.vec4;
import world.Map.MapTile;



public class RenderChunk {
	
	private static final int MAX_QUADS = Settings.CHUNK_SIZE * Settings.CHUNK_SIZE * 2; // both tiles and objects
	
	private QuadRenderer chunk_renderer;
	private int chunk_x, chunk_y;
	
	
	/**
	 * index must increment with two (tile and object)
	 * */
	private void submitMapTile(MapTile tile, int tile_x, int tile_y, int index) {
		
		int tile_texture = Database.getTile(tile.tile).texture;
		chunk_renderer.overrideSubmit(new vec3(tile_x, tile_y, 0).mult(Settings.TILE_SIZE), new vec2(Settings.TILE_SIZE), tile_texture, Colors.WHITE, index);
		
		vec4 color = Colors.WHITE;
		if (Database.getObject("air").index == tile.object) color = Colors.TRANSPARENT;
		
		int obj_texture = Main.game.getMap().getObjectTexture(tile_x, tile_y);
		chunk_renderer.overrideSubmit(new vec3(tile_x, tile_y, 0).mult(Settings.TILE_SIZE), new vec2(Settings.TILE_SIZE), obj_texture, color, index + 1);
	}
	
	public static RenderChunk generateChunkMesh(int start_x, int start_y) {

		RenderChunk returned = new RenderChunk();
		returned.chunk_x = start_x;
		returned.chunk_y = start_y;
		
		returned.chunk_renderer = QuadRenderer.maxQuads(MAX_QUADS);
		for (int i = 0; i < Settings.CHUNK_SIZE; i++) {
			for (int j = 0; j < Settings.CHUNK_SIZE; j++) {
				returned.submitMapTile(Main.game.getMap().getTile(returned.chunk_x + i, returned.chunk_y + j), (returned.chunk_x + i), (returned.chunk_y + j), (i * Settings.CHUNK_SIZE + j) * 2);
			}
		}
		
		return returned;
	}
	
	public void drawChunkMesh() {
		
		chunk_renderer.overrideQuadCount(MAX_QUADS);
		
		//Draw only if visible
		float player_middle_x = Main.game.getPlayer().getPos().x;
		float player_middle_y = Main.game.getPlayer().getPos().y;
		
		float chunk_middle_x = chunk_x + Settings.CHUNK_SIZE / 2.0f;
		float chunk_middle_y = chunk_y + Settings.CHUNK_SIZE / 2.0f;
		
		final float draw_debug_dst = 2;
		float window_width_as_tiles = draw_debug_dst / Settings.TILE_SIZE;
		float window_height_as_tiles = draw_debug_dst * Main.game.getWindow().getAspect() / Settings.TILE_SIZE;
		
		if ( Math.abs(player_middle_x - chunk_middle_x) - Settings.CHUNK_SIZE / 2.0f > window_width_as_tiles ) return;
		if ( Math.abs(player_middle_y - chunk_middle_y) - Settings.CHUNK_SIZE / 2.0f > window_height_as_tiles ) return;
		
		//Drawing
		chunk_renderer.draw(TextureLoader.getTexture());
		
	}
	
	
	
	public void updateTile(int in_chunk_x, int in_chunk_y) {
		
		chunk_renderer.begin();

		submitMapTile(Main.game.getMap().getTile((chunk_x + in_chunk_x), (chunk_y + in_chunk_y)), (chunk_x + in_chunk_x), (chunk_y + in_chunk_y), (in_chunk_x * Settings.CHUNK_SIZE + in_chunk_y) * 2);
		
		chunk_renderer.end();
		
	}
	
}
