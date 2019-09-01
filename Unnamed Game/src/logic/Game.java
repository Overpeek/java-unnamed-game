package logic;

import audio.Audio;
import creatures.Player;
import graphics.GlyphTexture;
import graphics.Renderer;
import graphics.Shader;
import graphics.Shader.ShaderException;
import graphics.TextLabelTexture;
import graphics.Texture;
import graphics.Window;
import graphics.buffers.Framebuffer;
import graphics.primitives.Quad;
import utility.Application;
import utility.Colors;
import utility.Debug.Timer;
import utility.GameLoop;
import utility.Keys;
import utility.Logger;
import utility.SaveManager;
import utility.mat4;
import utility.vec2;
import utility.vec4;
import world.Map;

public class Game extends Application {

	private Shader postprocess_shader;
	private float postprocess_time = 0.0f;
	private Framebuffer postprocess_fb1;
	private Framebuffer postprocess_fb2;
	
	private Shader gui_shader;
	private Renderer gui_renderer;

	private GlyphTexture glyphs;
	private Window window;
	private Map map;
	private Inventory inventory;
	private Gui gui;
	private Texture splashScreen;
	private MainMenu mainMenu;
	
	public boolean paused;
	public boolean advancedDebugMode;
	public boolean debugMode;
	
	public float debugScrollInput = 0;
	
	
	// Main update loop
	@Override
	public void update() {
		postprocess_time += 0.1f;
		postprocess_shader.setUniform1f("unif_t", postprocess_time);
		
		if (mainMenu.inMenu) mainMenu.update(CompiledSettings.UPDATES_PER_SECOND);
		if (!mainMenu.inMenu) {
			
			if (paused) return;
			map.update(CompiledSettings.UPDATES_PER_SECOND);
			gui.update(CompiledSettings.UPDATES_PER_SECOND);
			Database.modUpdates();
			
		}
	}

	

	// Main render loop
	@Override
	public void render(float preupdate_scale) {
		if (window == null || window.shouldClose()) gameloop.stop();
		window.clear();
		if (mainMenu.inMenu) mainMenu.render(preupdate_scale);
		if (!mainMenu.inMenu) {
			// World tiles
	 		if (paused) {
	 			postprocess_fb1.bind();
	 			postprocess_fb1.clear(0.0f, 0.0f, 0.0f, 1.0f);
	 	 		map.draw(preupdate_scale); //Map to framebuffer
	 	 		postprocess_fb1.unbind();
	 	 		postprocess_shader.setUniform1i("unif_effect", 0);

	 			// Multipass framebuffer
	 			postprocess_shader.setUniform1f("unif_effect_scale", debugScrollInput);
	 			postprocess_shader.setUniform1i("unif_effect", 1);
	 			Framebuffer.multipass(new vec2(-1.0f), new vec2(2.0f), postprocess_fb1, postprocess_fb2, 8);
	 			postprocess_shader.setUniform1i("unif_effect", 2);
	 			Framebuffer.multipass(new vec2(-1.0f), new vec2(2.0f), postprocess_fb1, postprocess_fb2, 8);
	 			
	 			postprocess_fb1.drawFullScreen(new vec2(-1.0f), new vec2(2.0f), new vec4(0.7f, 0.7f, 0.7f, 1.0f));
	 		} else {
	 	 		map.draw(preupdate_scale);
	 		}

	 		// GUI
			gui.draw();
			
			gui_shader.setUniform1i("usetex", 1); 
			TextLabelTexture.drawQueue(true);
		}
		
		window.update();
		window.input();
	}

	
	// Main cleanup
	@Override
	public void cleanup() {
		Audio.clean();
		Database.modCleanup();
	}
	
	private TextLabelTexture loadDescription;
	private void drawLoadingScreen(Renderer renderer, float state, String text) {
		window.setSwapInterval(2); //  No need to run loading screen with full power lol
		vec2 pos = new vec2(-0.5f, -0.5f);
		vec2 size = new vec2(1.0f, 1.0f);
		window.clear(1.0f, 1.0f, 1.0f, 1.0f);
		
		// Back texture
		gui_shader.setUniform1i("usetex", 1);
		splashScreen.bind();
		renderer.clear();
		renderer.submit(new Quad(pos, size, 0, Colors.WHITE));
		renderer.draw();
		
		// Loading bar
		gui_shader.setUniform1i("usetex", 0);
		renderer.clear();
		pos = new vec2(-0.8f, 0.8f);
		renderer.submit(new Quad(pos, new vec2(1.6f, 0.1f), 0, Colors.BLACK));
		pos = new vec2(-0.79f, 0.81f);
		renderer.submit(new Quad(pos, new vec2(state * 1.58f, 0.08f), 0, Colors.RED));
		renderer.draw();
		
		// Description
		if (text != null) {
			loadDescription.rebake("$2" + text);
			loadDescription.submit(new vec2(-0.8f, 0.7f), new vec2(0.1f));
			TextLabelTexture.drawQueue(true);
		}
		
		window.update();
		window.input();
		window.setSwapInterval(0);
	}

	
	// Main init
	@Override
	public void init() {
		paused = false;
		
		// Window
		Logger.info("Creating window");
		window = new Window(CompiledSettings.WINDOW_WIDTH, CompiledSettings.WINDOW_HEIGHT, CompiledSettings.WINDOW_DEFAULT_TITLE, this, Window.WINDOW_HIDDEN | Window.WINDOW_MULTISAMPLE_X8 | Window.WINDOW_RESIZEABLE | Window.WINDOW_DEBUGMODE);
		window.setSwapInterval(0);
		window.setIcon("/res/texture/icon.png");
		window.setBackFaceCulling(false);
		window.clearColor(0.2f, 0.2f, 0.2f, 1.0f);
		
		// Critical Resources
		Audio.init();
		glyphs = GlyphTexture.loadFont("/res/font/arial.font", 64);
		mat4 pr_matrix = new mat4().ortho(-window.getAspect(), window.getAspect(), 1.0f, -1.0f);
		gui_shader = Shader.multiTextureShader();
		gui_shader.setUniformMat4("pr_matrix", pr_matrix);
		TextLabelTexture.initialize(window, glyphs);
		TextLabelTexture.getDefaultShader().setUniformMat4("pr_matrix", pr_matrix);
		loadDescription = TextLabelTexture.bakeToTexture("Loading");
		gui_renderer = new Renderer();
		splashScreen = Texture.loadTextureAtlas(256, 1, 1, 1, "/res/texture/splash.png");
		SaveManager.init(CompiledSettings.GAME_NAME);
		
		// Splash screen
		window.unhide();
		drawLoadingScreen(gui_renderer, 0.0f, null);
		
		// Start async database loading
		TextureLoader.init(16);
		AsyncResLoader loader = new AsyncResLoader();
		new Thread(loader).start();
		while (true) {
			if (window.shouldClose()) gameloop.stop(); //  Can close even while loading! This is revolutionary

			//Check loadstate and send it to loading screen
			float state = loader.queryLoadState();
			if (state == 1.0f) break;
			drawLoadingScreen(gui_renderer, state, "Loading resources");			
		}
		TextureLoader.finish();
		
		// Rest of the shaders
		Logger.info("Creating all the shaders");
		try {
			postprocess_shader = Shader.loadFromSources("/res/shader/postprocess.vert.glsl", "/res/shader/postprocess.frag.glsl", true);
		} catch (ShaderException e) {
			e.printStackTrace();
			Logger.error("Critical shader could not be loaded");
		}
		Logger.info("All shaders created successfully!");

		// Game stuff
		gui = new Gui(
			Database.getCreature("player").health,
			Database.getCreature("player").stamina,
			Database.getCreature("player").healthgain,
			Database.getCreature("player").staminagain
		);
		ParticleManager.init();
		
		// Main menu
		mainMenu = new MainMenu(window, postprocess_shader);

		// Reset window
		resize(window.getWidth(), window.getHeight());

		// Ready
		Logger.info("Game ready! Running update- and renderloops");
	}

	// Load world with name
	// If not found, create one
	public void loadWorld(String name, boolean create) {
		AsyncMapLoader loader = new AsyncMapLoader();
		map = new Map(true);
		
		if (!create) { // load
			loader.loadMap(map, name);
			new Thread(loader).start();
			while (true) {
				if (window.shouldClose()) gameloop.stop(); //  Can close even while loading! This is revolutionary
				
				//Check loadstate and send it to loading screen
				float state = loader.queryLoadState();
				if (state == 1.0f) { map = loader.getLoadedMap(); loader.finish(); return; } //  Load finished
				if (state == -1) { break; } //  Load failed
				drawLoadingScreen(gui_renderer, state, "Loading map");		
				
			}
			Logger.warn("Couldn't load world \"" + name + "\"");	
		}

		if (create) { // create
			loader.createMap(map, name, CompiledSettings.INITIAL_RANDOM_SEED);
			new Thread(loader).start();
			while (true) {
				if (window.shouldClose()) gameloop.stop(); //  Can close even while loading! This is revolutionary

				//Check loadstate and send it to loading screen
				float state = loader.queryLoadState();
				if (state == 1.0f) { map = loader.getLoadedMap(); loader.finish(); return; } // Create finished
				if (state == -1) { break; } // Create failed
				drawLoadingScreen(gui_renderer, state, "Creating map");			
			}
			
			Logger.error("Couldn't create world \"" + name + "\" with seed \"" + 0 + "\"");
		}
	}
	
	public void closeWorld(boolean save) {
		if (save) {
			saveWorld();
		}

		mainMenu.inMenu = true;
	}

	
	// Key press callback
	@Override
	public void keyPress(int key, int action) {
		mainMenu.keyPress(key, action);
		if (!mainMenu.inMenu) {
			
			boolean chatWasOpened = gui.chatOpened();
			gui.keyPress(key, action);
			if (chatWasOpened) return;
			
			if (action == Keys.PRESS) {
				if (key == Keys.KEY_ESCAPE) { paused = !paused; return; }
				
				// Cant press or open anything while typing to chat or paused
				if (paused) return;

				// Postshader
				if (key == Keys.KEY_F7) { postprocess_shader.setUniform1i("unif_lens", 0); return; }
				if (key == Keys.KEY_F8) { postprocess_shader.setUniform1i("unif_lens", 1); return; }

				// Player keys
				if (key == Keys.KEY_E) { map.getPlayer().setPos(new vec2(Math.round(map.getPlayer().getPos().x + 0.5f) - 0.5f, Math.round(map.getPlayer().getPos().y + 0.5f) - 0.5f)); return; }

				// Inventory
				//if (key == Keys.KEY_R) { inventory.setVisible(!inventory.isVisible()); return; }
				if (key == Keys.KEY_ESCAPE) { inventory.setVisible(false); return; }

				// Inventory slot selecting
				if (key == Keys.KEY_1) { inventory.setSelectedSlot(0); return; }
				if (key == Keys.KEY_2) { inventory.setSelectedSlot(1); return; }
				if (key == Keys.KEY_3) { inventory.setSelectedSlot(2); return; }
				if (key == Keys.KEY_4) { inventory.setSelectedSlot(3); return; }
				if (key == Keys.KEY_5) { inventory.setSelectedSlot(4); return; }

				// Debug commands
				// Activate debug and advanced debug modes
				if (key == Keys.KEY_F1) {
					debugMode = !debugMode; 
					if (window.key(Keys.KEY_LEFT_SHIFT)) {
						debugMode = true;
						advancedDebugMode = true;
					}
					else advancedDebugMode = false;
					return;
				}

				// Debug ceil creaures
				if (key == Keys.KEY_F2) {
					map.debugCeilCreatures();
				}

				// Get FPS
				if (key == Keys.KEY_F3) { Logger.info("Fps: " + gameloop.getFps()); return; }
				
				// Clear inventoy
				if (key == Keys.KEY_F4) { inventory.clear(); return; }
				
				// Add creature at player
				if (key == Keys.KEY_F5) { map.addCreature(map.newCreature(map.getPlayer().getPos().x, map.getPlayer().getPos().y + 5.0f, "zombie")); return; }
			}
				
			gui.keyPress(key, action);
			
		}
	}

	
	// Mouse button press callback
	@Override
	public void buttonPress(int button, int action) {
		mainMenu.buttonPress(button, action);
		if (!mainMenu.inMenu) {

			map.getPlayer().buttonPress(button, action);
			
		}
	}


	// Window resize callbacks
	@Override
	public void resize(int width, int height) {
		if (width == 0) width = window.getWidth();
		if (height == 0) height = window.getHeight();
		
		float aspect = width / height;


		if (postprocess_fb1 != null) postprocess_fb1.delete();
		postprocess_fb1 = new Framebuffer(window.getWidth(), window.getHeight());
		if (postprocess_fb2 != null) postprocess_fb2.delete();
		postprocess_fb2 = new Framebuffer(window.getWidth(), window.getHeight());
		postprocess_fb2.unbind();
		
		window.viewport();
		
		// Matrices
		if (gui_shader != null) {
			mat4 pr_matrix = new mat4().ortho(-aspect, aspect, 1.0f, -1.0f);
			gui_shader.setUniformMat4("pr_matrix", pr_matrix);
			gui_shader.defaultOrthoMatrix();
			gui_shader.setUniform1i("usetex", 1);
		}
		if (postprocess_shader != null) {
			mat4 pr_matrix = new mat4().ortho(-1.0f, 1.0f, 1.0f, -1.0f);
			postprocess_shader.setUniformMat4("pr_matrix", pr_matrix);
			postprocess_shader.setUniform1i("unif_effect", 0);
			postprocess_shader.setUniform1i("usetex", 1);
		}
		//projection = new mat4().ortho(-1.0f, 1.0f, -1.0f, 1.0f);
		
		// Other
	}


	@Override
	public void charCallback(char character) {
// 		Logger.debug("Typed character: " + character);
// 		Logger.debug("height: " + glyphs.getGlyphData(character).height + ", y: " + glyphs.getGlyphData(character).y);
		gui.typing(character, 0);
	}
	
	public GlyphTexture getGlyphs() {
		return glyphs;
	}
	
	public GameLoop getLoop() {
		return gameloop;
	}
	
	public Map getMap() {
		return map;
	}
	
	public Gui getGui() {
		return gui;
	}
	
	public float guiScale() {
		 return 1.0f; // Settings.DEBUG_ZOOM;
	}
	
	public float renderScale() {
		return 720.0f / window.getHeight(); 
	}
	
	public float renderWidthScale() {
		return 1280.0f / window.getWidth(); 
	}
	
	public float defaultAspect() {
		return 1280.0f / 720.0f;
	}

	public Player getPlayer() {
		return (Player) map.getPlayer();
	}

	public Window getWindow() {
		return window;
	}

	public MainMenu getMainMenu() {
		return mainMenu;
	}

	@Override
	public void mousePos(float x, float y) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void scroll(float x_delta, float y_delta) {
		debugScrollInput += y_delta / 3.0f;
	}



	public void saveWorld() {
		gui.addChatLine("Saving..., expect some lag");
		Logger.info("Saving...");
		Timer timer = new Timer();

		map.save();
		
		gui.addChatLine("Save complete (in " + timer.microseconds() + " microseconds)");
	}
	
}