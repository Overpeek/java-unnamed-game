package stages;

public abstract class State {

	public abstract void update();
	public abstract void render(float preupdate_scale);
	public abstract void cleanup();
	public abstract void init();
	public abstract void resize(int width, int height);
	public abstract void keyPress(int key, int action);
	public abstract void buttonPress(int button, int action);
	public abstract void mousePos(float x, float y);
	public abstract void scroll(float x_delta, float y_delta);
	public abstract void charCallback(char character);

}
