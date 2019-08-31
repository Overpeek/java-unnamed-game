package world;

import graphics.Renderer;
import graphics.primitives.Primitive.Primitives;
import graphics.primitives.Quad;
import logic.CompiledSettings;
import logic.Database;
import logic.Main;
import logic.TextureLoader;
import utility.Colors;
import utility.vec2;
import utility.vec4;
import world.Map.MapTile;



public class RenderChunk {
	
	private static final int MAX_QUADS = CompiledSettings.CHUNK_SIZE * CompiledSettings.CHUNK_SIZE * 2; // both tiles and objects
	
	private Renderer chunk_renderer;
	private int chunk_x, chunk_y;
	
	
	/**
	 * index must increment with two (tile and object)
	 * */
	private void submitMapTile(MapTile tile, int tile_x, int tile_y, int index) {
		
		int tile_texture = Database.getTile(tile.tile).texture;
		chunk_renderer.submitOverride(new Quad(new vec2(tile_x, tile_y).mul(CompiledSettings.TILE_SIZE), new vec2(CompiledSettings.TILE_SIZE), tile_texture, Colors.WHITE), index);
		
		vec4 color = Colors.WHITE;
		if (Database.getObject("air").index == tile.object) color = Colors.TRANSPARENT;
		
		int obj_texture = Main.game.getMap().getObjectTexture(tile_x, tile_y);
		chunk_renderer.submitOverride(new Quad(new vec2(tile_x, tile_y).mul(CompiledSettings.TILE_SIZE), new vec2(CompiledSettings.TILE_SIZE), obj_texture, color), index + 1);
	}
	
	public static RenderChunk generateChunkMesh(int start_x, int start_y) {

		RenderChunk returned = new RenderChunk();
		returned.chunk_x = start_x;
		returned.chunk_y = start_y;
		
		returned.chunk_renderer = new Renderer(Primitives.Quad, MAX_QUADS);
		for (int i = 0; i < CompiledSettings.CHUNK_SIZE; i++) {
			for (int j = 0; j < CompiledSettings.CHUNK_SIZE; j++) {
				returned.submitMapTile(Main.game.getMap().getTile(returned.chunk_x + i, returned.chunk_y + j), (returned.chunk_x + i), (returned.chunk_y + j), (i * CompiledSettings.CHUNK_SIZE + j) * 2);
			}
		}
		
		return returned;
	}
	
	public void drawChunkMesh() {
		
		chunk_renderer.overridePrimitiveCount(MAX_QUADS);
		
		//Draw only if visible
		float player_middle_x = Main.game.getPlayer().getPos().x;
		float player_middle_y = Main.game.getPlayer().getPos().y;
		
		float chunk_middle_x = chunk_x + CompiledSettings.CHUNK_SIZE / 2.0f;
		float chunk_middle_y = chunk_y + CompiledSettings.CHUNK_SIZE / 2.0f;
		
		final float draw_debug_dst = 2;
		float window_width_as_tiles = draw_debug_dst / CompiledSettings.TILE_SIZE;
		float window_height_as_tiles = draw_debug_dst * Main.game.getWindow().getAspect() / CompiledSettings.TILE_SIZE;
		
		if ( Math.abs(player_middle_x - chunk_middle_x) - CompiledSettings.CHUNK_SIZE / 2.0f > window_width_as_tiles ) return;
		if ( Math.abs(player_middle_y - chunk_middle_y) - CompiledSettings.CHUNK_SIZE / 2.0f > window_height_as_tiles ) return;
		
		//Drawing
		TextureLoader.getTexture().bind();
		chunk_renderer.draw();
		
	}
	
	
	
	public void updateTile(int in_chunk_x, int in_chunk_y) {
		
		submitMapTile(Main.game.getMap().getTile((chunk_x + in_chunk_x), (chunk_y + in_chunk_y)), (chunk_x + in_chunk_x), (chunk_y + in_chunk_y), (in_chunk_x * CompiledSettings.CHUNK_SIZE + in_chunk_y) * 2);		
		
	}
	
}
