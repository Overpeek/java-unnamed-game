package logic;
import java.awt.Font;

import graphics.GlyphTexture;
import graphics.TextLabelTexture;
import graphics.Window;
import guiwindows.GUIWindowManager;
import settings.CompiledSettings;
import stages.GameStateManager;
import stages.GameStateManager.GameStates;
import stages.Loader;
import utility.Application;
import utility.Logger;

public class Main extends Application {
	
	private static Main main;
	
	

	public static void main(String args[]) {
		main = new Main();
		main.start(CompiledSettings.UPDATES_PER_SECOND);
	}
	
	public static void stop() {
		main.gameloop.stop();
	}

	
	
	@Override
	public void update() {
		GameStateManager.update();
	}

	@Override
	public void render(float preupdate_scale) {
		if (window.shouldClose()) Main.stop();
		
		window.clear();
		window.input();
		GameStateManager.render(preupdate_scale);
		window.update();
	}

	@Override
	public void cleanup() {
		GameStateManager.cleanup();
	}

	@Override
	public void init() {
		// Open window and config it
		Logger.info("Opening window...");
		window = new Window(CompiledSettings.WINDOW_WIDTH, CompiledSettings.WINDOW_HEIGHT, CompiledSettings.WINDOW_DEFAULT_TITLE, this, Window.WINDOW_HIDDEN | Window.WINDOW_MULTISAMPLE_X8 | Window.WINDOW_RESIZEABLE | Window.WINDOW_DEBUGMODE);
		window.setSwapInterval(0);
		window.setIcon("res/texture/icon.png");
		window.setBackFaceCulling(false);
		window.clearColor(0.2f, 0.2f, 0.2f, 1.0f);
		Logger.debug("Window opened");
		
		// Font
		GlyphTexture glyphs = GlyphTexture.loadFont(new Font("arial", Font.PLAIN, 128));
		
		// Critical Initalizations
		TextLabelTexture.initialize(window, glyphs);
		TextureLoader.init(16); // Start loading resources
		GameStateManager.initialize(window);
		
		// Resource loading
		GameStateManager.setState(GameStates.LOADING);
		((Loader)GameStates.LOADING.state).loadRes(GameStates.MAINMENU);
		GameStateManager.init();
		
		window.unhide();
	}

	@Override
	public void resize(int width, int height) {
		GameStateManager.resize(width, height);
		ShaderManager.resize(width, height);
		GUIWindowManager.resize(width, height);
	}

	@Override
	public void keyPress(int key, int action) {
		GameStateManager.keyPress(key, action);
	}

	@Override
	public void buttonPress(int button, int action) {
		GameStateManager.buttonPress(button, action);
	}

	@Override
	public void mousePos(float x, float y) {
		GameStateManager.mousePos(x, y);
	}

	@Override
	public void scroll(float x_delta, float y_delta) {
		GameStateManager.scroll(x_delta, y_delta);
	}

	@Override
	public void charCallback(char character) {
		GameStateManager.charCallback(character);
	}

	
}
