package logic;

import utility.Logger;
import world.Map;

public class AsyncMapLoader implements Runnable {
	
	private Map map = null;
	private int action = -1;
	private int seed = 0;
	private String name = null;
	private int state = 0;
	
	public void loadMap(String mapName) {
		name = mapName;
		action = 0;
		state = 0;
		new Thread().run();
	}
	
	public void createMap(String mapName, int seed) {
		name = mapName;
		action = 1;
		state = 0;
		this.seed = seed;
		new Thread(new AsyncMapLoader()).run();		
	}
	
	public Map getLoadedMap() {
		return map;
	}

	@Override
	public void run() {
		if (action == 0) { //Load
			map = new Map();
			if (!map.load(name)) state = -1;
		}
		else if (action == 1) { //Create
			map = new Map();
			if (!map.create(name, seed)) state = -1;
		}
		else {
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
