package logic;

import graphics.Renderer;

public abstract class Mod {
	
	public abstract String getName();
	public abstract String getDataName();
	public abstract String getVersion();
	public abstract String getCreator();
	
	public abstract void setup();
	public abstract void update();
	public abstract void cleanup();
	public abstract void draw(Renderer renderer);
	public abstract void save();

}
