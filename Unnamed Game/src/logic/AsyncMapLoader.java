package logic;

import utility.Logger;
import world.Map;

public class AsyncMapLoader implements Runnable {
	
	private Map map = null;
	private int action = -1;
	private int seed = 0;
	private String name = null;
	private int state = 0;
	
	public void loadMap(Map map, String mapName) {
		this.name = mapName;
		this.action = 0;
		this.state = 0;
		this.map = map;
	}
	
	public void createMap(Map map, String mapName, int seed) {
		this.name = mapName;
		this.action = 1;
		this.state = 0;
		this.seed = seed;
		this.map = map;	
	}
	
	public Map getLoadedMap() {
		return map;
	}

	@Override
	public void run() {
		if (action == 0) { //Load
			action = 0;
			if (!map.load(name)) state = -1;
		}
		else if (action == 1) { //Create
			action = 0;
			if (!map.create(name, seed)) state = -1;
		}
		else {
			action = 0;
			//Unknown
		}
	}
	
	
	//Returns float between 0.0f-1.0f and -1 if failed
	public float queryLoadState() {
		if (map == null) return 0.0f;
		if (state == -1) return -1;
		
		float tiles = map.tileState();
		return tiles;
	}

}
