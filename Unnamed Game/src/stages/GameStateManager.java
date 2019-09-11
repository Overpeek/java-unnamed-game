package stages;

import graphics.Window;

public class GameStateManager {
	
	public static enum GameStates {
		LOADING, MAINMENU, WORLD;
		
		public State state;
	};
	private static GameStates game_state;
	public static Window window;
	
	
	
	public static void setState(GameStates state) {
		game_state = state;
	}
	
	public static void initialize(Window _window) {
		window = _window;
		GameStates.LOADING.state = new Loader();
		GameStates.MAINMENU.state = new MainMenu(_window);
		GameStates.WORLD.state = new World();
	}

	public static void update() {
		game_state.state.update();
	}

	public static void render(float preupdate_scale) {
		game_state.state.render(preupdate_scale);
	}

	public static void cleanup() {
		game_state.state.cleanup();
	}

	public static void init() {
		game_state.state.init();
	}

	public static void resize(int width, int height) {
		game_state.state.resize(width, height);
	}

	public static void keyPress(int key, int action) {
		game_state.state.keyPress(key, action);
	}

	public static void buttonPress(int button, int action) {
		game_state.state.buttonPress(button, action);
	}

	public static void mousePos(float x, float y) {
		game_state.state.mousePos(x, y);
	}

	public static void scroll(float x_delta, float y_delta) {
		game_state.state.scroll(x_delta, y_delta);
	}

	public static void charCallback(char character) {
		game_state.state.charCallback(character);
	}

}
