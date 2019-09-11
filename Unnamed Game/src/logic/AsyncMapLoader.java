package logic;

import world.Map;

public class AsyncMapLoader extends AsyncLoader {
	
	private Map map = null;
	private int action = -1;
	private int seed = 0;
	private String name = null;
	private int state = 0;
	

	
	public Map getLoadedMap() {
		return map;
	}
	
	
	
	public AsyncMapLoader(String name) { // Load map
		this.name = name;
		this.action = 0;
		this.state = 0;
		this.map = new Map(true);
	}
	
	public AsyncMapLoader(String name, int seed) { // Generate map
		this.name = name;
		this.action = 1;
		this.state = 0;
		this.seed = seed;
		this.map = new Map(true);	
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

	@Override
	public void finish() {
		map.generateAllMeshes();
	}
	
	
	//Returns float between 0.0f-1.0f and -1 if failed
	@Override
	public float queryLoadState() {
		if (map == null) return 0.0f;
		if (state == -1) return -1;
		
		float tiles = map.tileState();
		return tiles;
	}



	@Override
	public void init() {}

}
