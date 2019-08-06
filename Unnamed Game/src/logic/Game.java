package logic;

import audio.Audio;
import creatures.Player;
import graphics.Framebuffer;
import graphics.GlyphTexture;
import graphics.Renderer;
import graphics.Shader;
import graphics.TextLabelTexture;
import graphics.Texture;
import graphics.Window;
import utility.Application;
import utility.Colors;
import utility.GameLoop;
import utility.Keys;
import utility.Logger;
import utility.mat4;
import utility.vec2;
import utility.vec3;
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
	private boolean justPaused;
	private Gui gui;
	private Texture splashScreen;
	private String loadedMapName;
	
	public boolean paused;
	public boolean advancedDebugMode;
	public boolean debugMode;
	
	public float debugScrollInput = 0;
	
	
	// Main update loop
	@Override
	public void update() {
		if (Settings.DEBUG_REGEN_WORLD) loadWorld(Settings.WORLD_NAME, true); // Debug regen world

		postprocess_time += 0.1f;
		postprocess_shader.setUniform1f("unif_t", postprocess_time);
		
		if (paused) return;
		map.update(Settings.UPDATES_PER_SECOND);
		gui.update(Settings.UPDATES_PER_SECOND);
		Database.modUpdates();
	}

	

	// Main render loop
	@Override
	public void render(float preupdate_scale) {
		if (window == null || window.shouldClose()) gameloop.stop();
		window.clear();
		
		// Logger.debug("Camera position: " + camera_pos);

		// world_renderer.clear();
		// world_renderer.submitVertex(new VertexData(0.0f, 0.0f, 0.0f, 1.0f, 1.0f, Database.objects.get(tile).texture, 1.0f, 1.0f, 1.0f, 1.0f));
		// label.rebake("OBJ: " + Database.objects.get(tile).name + ", " + Database.objects.get(tile).texture, glyphs);
		// resize(window.getWidth(), window.getHeight());
		// world_renderer.drawAsPoints(TextureLoader.getTextureId(), TextureLoader.getTextureType());
		// 
		// single_texture_shader.enable();
		// world_renderer.submitBakedText(new vec3(-1.0f, -1.0f, 0.0f), new vec2(0.2f, 0.2f), label, Colors.WHITE);



		
		
		// World tiles
 		if (paused) {
 			postprocess_fb1.bind();
 			postprocess_fb1.clear(0.0f, 0.0f, 0.0f, 1.0f);
 	 		map.draw(preupdate_scale); //Map to framebuffer
 	 		postprocess_fb1.unbind();

 			// Multipass framebuffer
 			postprocess_shader.setUniform1f("unif_effect_scale", debugScrollInput);
 			postprocess_shader.setUniform1i("unif_effect", 1);
 			Framebuffer.multipass(postprocess_fb1, postprocess_fb2, 8);
 			postprocess_shader.setUniform1i("unif_effect", 2);
 			Framebuffer.multipass(postprocess_fb1, postprocess_fb2, 8);
 			
 			postprocess_shader.setUniform1i("unif_effect", 0);
 			postprocess_fb1.drawFullScreen(new vec4(0.7f, 0.7f, 0.7f, 1.0f));
 		} else {
 	 		map.draw(preupdate_scale);
 		}

		gui.draw();
// 		// 
// 		point_shader.enable();
// 		blur_renderer.points.draw(TextureLoader.getTexture());
// 		normal_renderer.points.draw(TextureLoader.getTexture());
// 		
		gui_shader.enable();
// 		
		gui_shader.setUniform1i("usetex", 1); 
		TextLabelTexture.drawQueue(true);
		

		// single_texture_shader.enable();
		// label.draw(new vec3(-window.getAspect(), -1.0f, 0.0f), new vec2(0.2f, 0.2f), Colors.WHITE);
		
		// // Gui
		// m_gui->renderNoBlur(m_guirenderer.get());
  // 
		// // Flush
		// if (!paused) {
		// 	m_worldrenderer->draw(m_shader.get(), m_pointshader.get(), oe::TextureManager::getTexture(0), true);
		// }
		// else if (justPaused) {
		// 	m_worldrenderer->drawToFramebuffer(m_shader.get(), m_pointshader.get(), oe::TextureManager::getTexture(0), true, false);
		// 	m_postshader->enable();
		// 	for (int i = 0; i < 16; i++) {
		// 		m_postshader->setUniform1i("unif_effect", 1);
		// 		m_worldrenderer->drawFramebufferToFramebuffer(m_postshader.get(), "unif_texture", true);
		// 		m_postshader->setUniform1i("unif_effect", 2);
		// 		m_worldrenderer->drawFramebufferToFramebuffer(m_postshader.get(), "unif_texture", false);
		// 	}
		// 	m_postshader->setUniform1i("unif_effect", 0);
		// 	m_worldrenderer->drawFramebuffer(m_postshader.get(), "unif_texture", false);
		// }
		// else if (paused) {
		// 	m_postshader->enable();
		// 	m_postshader->setUniform1i("unif_effect", 0);
		// 	m_worldrenderer->drawFramebuffer(m_postshader.get(), "unif_texture", false);
		// }
		// 
		// m_guirenderer->draw(m_shader.get(), m_pointshader.get(), oe::TextureManager::getTexture(0), true);
  // 
		// // Other
		// justPaused = false;
  // 
  // 
		window.update();
		window.input();
	}

	
	// Main cleanup
	@Override
	public void cleanup() {
		Database.modCleanup();
	}
	
	private TextLabelTexture loadDescription;
	private void drawLoadingScreen(Renderer renderer, float state, String text) {
		window.setSwapInterval(2); //  No need to run loading screen with full power lol
		vec3 pos = new vec3(-0.5f, -0.5f, 0.0f);
		vec2 size = new vec2(1.0f, 1.0f);
		window.clear(1.0f, 1.0f, 1.0f, 1.0f);
		
		// Back texture
		gui_shader.setUniform1i("usetex", 1);
		renderer.quads.clear();
		renderer.quads.submit(new vec3(pos.x, pos.y, pos.z), size, 0, Colors.WHITE);
		renderer.quads.draw(splashScreen);
		
		// Loading bar
		gui_shader.setUniform1i("usetex", 0);
		renderer.quads.clear();
		pos = new vec3(-0.8f, 0.8f, 0.0f);
		renderer.quads.submit(new vec3(pos.x, pos.y, pos.z), new vec2(1.6f, 0.1f), 0, Colors.BLACK);
		pos = new vec3(-0.79f, 0.81f, 0.0f);
		renderer.quads.submit(new vec3(pos.x, pos.y, pos.z), new vec2(state * 1.58f, 0.08f), 0, Colors.RED);
		renderer.quads.draw(splashScreen);
		
		// Description
		if (text != null) {
			loadDescription.rebake("$2" + text);
			loadDescription.queueDraw(new vec3(-0.8f, 0.7f, 0.0f), new vec2(0.1f));
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
		justPaused = false;
		
		// Window
		Logger.info("Creating window");
		window = new Window(Settings.WINDOW_WIDTH, Settings.WINDOW_HEIGHT, Settings.WINDOW_DEFAULT_TITLE, Window.WINDOW_HIDDEN | Window.WINDOW_MULTISAMPLE_X8 | Window.WINDOW_RESIZEABLE | Window.WINDOW_DEBUGMODE);
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
		Framebuffer.initDrawing();
		
		
		// Rest of the shaders
		Logger.info("Creating all the shaders");
		postprocess_shader = Shader.loadFromSources("/res/shader/postprocess.vert.glsl", "/res/shader/postprocess.frag.glsl", true);
		Logger.info("All shaders created successfully!");

		// Game stuff
		gui = new Gui(
			Database.getCreature("player").health,
			Database.getCreature("player").stamina,
			Database.getCreature("player").healthgain,
			Database.getCreature("player").staminagain
		);
		ParticleManager.init();

		// Start async map loading
		loadWorld(Settings.WORLD_NAME, true);
		
		// Main menu
		MainMenu.init(gui_renderer, gui_shader, postprocess_shader, glyphs);
		new MainMenu();
		// window.setCurrentApp(this);

		// Reset window
		resize(window.getWidth(), window.getHeight());

		// Ready
		Logger.info("Game ready! Running update- and renderloops");
		
		// /
		map.generateAllMeshes();
	}

	// Load world with name
	// If not found, create one
	public void loadWorld(String name, boolean create) {
		AsyncMapLoader loader = new AsyncMapLoader();
		loadedMapName = name;
		map = new Map();
		loader.loadMap(map, name);
		new Thread(loader).start();
		while (true) {
			if (window.shouldClose()) gameloop.stop(); //  Can close even while loading! This is revolutionary
			
			//Check loadstate and send it to loading screen
			float state = loader.queryLoadState();
			if (state == 1.0f) { map = loader.getLoadedMap(); return; } //  Load finished
			if (state == -1) { loadedMapName = null; break; } //  Load failed
			drawLoadingScreen(gui_renderer, state, "Loading map");		
			
		}
		Logger.warn("Couldn't load world \"" + name + "\"");	

		if (create) {
			loader.createMap(map, name, 0);
			new Thread(loader).start();
			while (true) {
				if (window.shouldClose()) gameloop.stop(); //  Can close even while loading! This is revolutionary

				//Check loadstate and send it to loading screen
				float state = loader.queryLoadState();
				if (state == 1.0f) { map = loader.getLoadedMap(); return; } // Create finished
				if (state == -1) { loadedMapName = null; break; } // Create failed
				drawLoadingScreen(gui_renderer, state, "Creating map");			
			}
			
			Logger.error("Couldn't create world \"" + name + "\" with seed \"" + 0 + "\"");
		}
	}

	
	// Key press callback
	@Override
	public void keyPress(int key, int action) {
		if (!gui.chatOpened()) {
			if (action == Keys.PRESS) {
				if (key == Keys.KEY_ESCAPE) { paused = !paused; justPaused = true; return; }
				
				// Cant press or open anything while typing to chat or paused
				if (gui.chatOpened() || paused) return;

				// Postshader
				if (key == Keys.KEY_F7) { postprocess_shader.setUniform1i("unif_lens", 0); justPaused = true; return; }
				if (key == Keys.KEY_F8) { postprocess_shader.setUniform1i("unif_lens", 1); justPaused = true; return; }

				// Player keys
				if (key == Keys.KEY_E) { map.getPlayer().setPos(new vec2(Math.round(map.getPlayer().getPos().x + 0.5f) - 0.5f, Math.round(map.getPlayer().getPos().y + 0.5f) - 0.5f)); return; }

				// Inventory
				if (key == Keys.KEY_R) { inventory.setVisible(!inventory.isVisible()); return; }
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
		}

		gui.keyPress(key, action);
	}

	
	// Mouse button press callback
	@Override
	public void buttonPress(int button, int action) {
		//  TODO Auto-generated method stub
		
	}


	// Window resize callbacks
	@Override
	public void resize(int width, int height) {
		if (width == 0) width = window.getWidth();
		if (height == 0) height = window.getHeight();


		if (postprocess_fb1 != null) postprocess_fb1.delete();
		postprocess_fb1 = Framebuffer.createFramebuffer(window.getWidth(), window.getHeight());
		if (postprocess_fb2 != null) postprocess_fb2.delete();
		postprocess_fb2 = Framebuffer.createFramebuffer(window.getWidth(), window.getHeight());
		postprocess_fb2.unbind();
		
		window.viewport();
		
		// Matrices
		gui_shader.defaultOrthoMatrix();
		postprocess_shader.defaultOrthoMatrix();
		//projection = new mat4().ortho(-1.0f, 1.0f, -1.0f, 1.0f);
		
		// Other
		postprocess_shader.setUniform1i("unif_effect", 0);
		postprocess_shader.setUniform1i("usetex", 1);
		gui_shader.setUniform1i("usetex", 1);
	}


	@Override
	public void charCallback(char character) {
// 		Logger.debug("Typed character: " + character);
// 		Logger.debug("height: " + glyphs.getGlyphData(character).height + ", y: " + glyphs.getGlyphData(character).y);
		gui.typing(character, 0);
	}
	
	public String getResourcePath() {
		return "/res/";
	}
	
	public String getDataPath() {
		return Settings.SAVE_PATH + "/";
	}
	
	public String getSavePath() {
		// "C:/Users/eemel/AppData/Roaming/Unnamed Game" for windows
		return Settings.SAVE_PATH + "/" + loadedMapName + "/";
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

	@Override
	public void mousePos(float x, float y) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void scroll(float x_delta, float y_delta) {
		debugScrollInput += y_delta / 3.0f;
	}



	public void saveWorld() {
		// TODO Auto-generated method stub
		
	}
	
}