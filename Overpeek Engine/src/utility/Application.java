package utility;

public abstract class Application {
	
	public GameLoop gameloop;

	public abstract void update();
	public abstract void render();
	public abstract void cleanup();
	public abstract void init();
	public abstract void resize(int width, int height);

	public abstract void keyPress(int key, int action);
	public abstract void buttonPress(int button, int action);
	public abstract void mousePos(int x, int y);
	public abstract void charCallback(char character);
	
}
