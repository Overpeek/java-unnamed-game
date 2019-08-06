package mod;

import org.json.JSONObject;

import graphics.*;
import logic.*;
import utility.*;

public class Main extends Mod {
	
	private int texture = 0;
	private float time = 0.0f;
	
	@Override
	public void setup() { //This method is called right after this mod has been loaded
		//Logger.debug(getName() + " Setup");
		texture = TextureLoader.load("res/mods/test.png");
		Database.loadItem(new Database.Item_Data("Template item", "moditem", texture, texture, 99, 0.0f, 0.0f, 0.0f, 0));
		
		//Insert TestModCreature into database
		Class<?> clazz = TestModCreature.class;
		JSONObject creature_data = DataIO.loadJSONObject("testmodcreature.json");
		Database.loadCreature(creature_data, clazz);
	}

	@Override
	public void update() { //This method is called every update
		//Logger.debug(getName() + " Update");
		time += 0.1f;
	}

	@Override
	public void cleanup() { //This method is called before game closes
		//Logger.debug(getName() + " Cleaning up this mod");
	}

	@Override
	public void draw(Renderer renderer) { //This method is called every render cycle
		renderer.points.submitVertex(new VertexData(new vec3(-0.25f + (float)Math.cos(time) / 5.0f, -0.25f + (float)Math.sin(time) / 5.0f, 0.0f), new vec2(0.5f, 0.5f), texture, Colors.WHITE));
	}

	@Override
	public void save() { //This method is called every time the game saves
		DataIO.writeInt("savedata.modded", new int[] { 0, 532, 54, 25426, 23542 });
	}

	@Override
	public String getName() { //Mod name
		return "Template mod";
	}

	@Override
	public String getVersion() { //Mod version
		return "0.0.0.0";
	}

	@Override
	public String getCreator() { //Mod creator (doesn't have to be visible)
		return "Unknown creator";
	}
	
}
