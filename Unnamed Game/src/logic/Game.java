package logic;

import audio.Audio;
import creatures.Creature;
import creatures.Player;
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
import world.Map;

public class Game extends Application {

	private Shader multi_texture_shader;
	private Shader single_texture_shader;
	private Shader point_shader;
	private Shader post_shader;
	
	private Renderer blur_renderer;
	private Renderer normal_renderer;
	private Renderer gui_renderer;
	private GlyphTexture glyphs;
	private Window window;
	private Map map;
	private Inventory inventory;
	private Creature player;
	private boolean justPaused;
	private Gui gui;
	private Texture splashScreen;
	private String loadedMapName;
	
	public boolean paused;
	public boolean advancedDebugMode;
	public boolean debugMode;
	
	
	
	//Main update loop
	@Override
	public void update() {
		float playerSpeed = Database.getCreature("player").walkSpeed;

		if (window.key(Keys.KEY_LEFT_SHIFT)) playerSpeed *= 2;
		if (window.key(Keys.KEY_S)) { player.setAcc(new vec2(player.getAcc().x, playerSpeed)); }
		if (window.key(Keys.KEY_D)) { player.setAcc(new vec2(playerSpeed, player.getAcc().y)); }
		if (window.key(Keys.KEY_W)) { player.setAcc(new vec2(player.getAcc().x, -playerSpeed)); }
		if (window.key(Keys.KEY_A)) { player.setAcc(new vec2(-playerSpeed, player.getAcc().y)); }
		player.update(0, Settings.UPDATES_PER_SECOND);
		if (Settings.DEBUG_REGEN_WORLD) {
			loadWorld(Settings.WORLD_NAME, true); //Debug regen world
		}
		gui.update(Settings.UPDATES_PER_SECOND);
		Database.modUpdates();
	}

	
	
	//Main render loop
	@Override
	public void render(float corrector) {
		if (window == null || window.shouldClose()) gameloop.stop();
		window.clear();

		//world_renderer.clear();
		//world_renderer.submitVertex(new VertexData(0.0f, 0.0f, 0.0f, 1.0f, 1.0f, Database.objects.get(tile).texture, 1.0f, 1.0f, 1.0f, 1.0f));
		//label.rebake("OBJ: " + Database.objects.get(tile).name + ", " + Database.objects.get(tile).texture, glyphs);
		//resize(window.getWidth(), window.getHeight());
		//world_renderer.drawAsPoints(TextureLoader.getTextureId(), TextureLoader.getTextureType());
		//
		//single_texture_shader.enable();
		//world_renderer.submitBakedText(new vec3(-1.0f, -1.0f, 0.0f), new vec2(0.2f, 0.2f), label, Colors.WHITE);
		
		//World tiles
		if (!paused || justPaused) {
			blur_renderer.points.clear();
			normal_renderer.points.clear();
			map.submitToRenderer(blur_renderer, -player.getPos().x - player.getVel().x * corrector / Settings.UPDATES_PER_SECOND, -player.getPos().y - player.getVel().y * corrector / Settings.UPDATES_PER_SECOND, corrector);
  
			player.draw(blur_renderer, -player.getPos().x - player.getVel().x * corrector / Settings.UPDATES_PER_SECOND, -player.getPos().y - player.getVel().y * corrector / Settings.UPDATES_PER_SECOND, corrector, renderScale());
			gui.renderWithBlur(blur_renderer, normal_renderer);
			gui.renderWithoutBlur(blur_renderer);
			Database.modRendering(blur_renderer);
			//m_inventory->render(m_worldrenderer.get());
		}
		//
		point_shader.enable();
		blur_renderer.points.draw(TextureLoader.getTexture());
		normal_renderer.points.draw(TextureLoader.getTexture());
		
		single_texture_shader.setUniform1i("usetex", 1);
		TextLabelTexture.drawQueue();

		//single_texture_shader.enable();
		//label.draw(new vec3(-window.getAspect(), -1.0f, 0.0f), new vec2(0.2f, 0.2f), Colors.WHITE);
		
		////Gui
		//m_gui->renderNoBlur(m_guirenderer.get());
  //
		////Flush
		//if (!paused) {
		//	m_worldrenderer->draw(m_shader.get(), m_pointshader.get(), oe::TextureManager::getTexture(0), true);
		//}
		//else if (justPaused) {
		//	m_worldrenderer->drawToFramebuffer(m_shader.get(), m_pointshader.get(), oe::TextureManager::getTexture(0), true, false);
		//	m_postshader->enable();
		//	for (int i = 0; i < 16; i++) {
		//		m_postshader->setUniform1i("unif_effect", 1);
		//		m_worldrenderer->drawFramebufferToFramebuffer(m_postshader.get(), "unif_texture", true);
		//		m_postshader->setUniform1i("unif_effect", 2);
		//		m_worldrenderer->drawFramebufferToFramebuffer(m_postshader.get(), "unif_texture", false);
		//	}
		//	m_postshader->setUniform1i("unif_effect", 0);
		//	m_worldrenderer->drawFramebuffer(m_postshader.get(), "unif_texture", false);
		//}
		//else if (paused) {
		//	m_postshader->enable();
		//	m_postshader->setUniform1i("unif_effect", 0);
		//	m_worldrenderer->drawFramebuffer(m_postshader.get(), "unif_texture", false);
		//}
		//
		//m_guirenderer->draw(m_shader.get(), m_pointshader.get(), oe::TextureManager::getTexture(0), true);
  //
		////Other
		//justPaused = false;
  //
  //
		window.update();
		window.input();
	}

	
	//Main cleanup
	@Override
	public void cleanup() {
		Database.modCleanup();
	}
	
	private TextLabelTexture loadDescription;
	private void drawLoadingScreen(Renderer renderer, float state, String text) {
		vec3 pos = new vec3(-0.5f, -0.5f, 0.0f);
		vec2 size = new vec2(1.0f, 1.0f);
		window.clear(1.0f, 1.0f, 1.0f, 1.0f);
		single_texture_shader.enable();
		
		//Back texture
		single_texture_shader.setUniform1i("usetex", 1);
		renderer.quads.clear();
		renderer.quads.submit(new vec3(pos.x, pos.y, pos.z), size, 0, Colors.WHITE);
		renderer.quads.draw(splashScreen);
		
		//Loading bar
		renderer.quads.clear();
		pos = new vec3(-0.8f, 0.8f, 0.0f);
		renderer.quads.submit(new vec3(pos.x, pos.y, pos.z), new vec2(1.6f, 0.1f), 0, Colors.BLACK);
		pos = new vec3(-0.79f, 0.81f, 0.0f);
		renderer.quads.submit(new vec3(pos.x, pos.y, pos.z), new vec2(state * 1.58f, 0.08f), 0, Colors.RED);
		single_texture_shader.setUniform1i("usetex", 0);
		renderer.quads.draw(splashScreen);
		
		//Description
		if (text != null) {
			single_texture_shader.setUniform1i("usetex", 1);
			loadDescription.rebake(text);
			loadDescription.queueDraw(new vec3(-0.8f, 0.7f, 0.0f), new vec2(0.1f), Colors.WHITE);			
		}
		
		window.update();
		window.input();
	}

	
	//Main init
	@Override
	public void init() {
		paused = false;
		justPaused = false;
		
		//Window
		Logger.info("Creating window");
		window = new Window(Settings.WINDOW_WIDTH, Settings.WINDOW_HEIGHT, Settings.WINDOW_DEFAULT_TITLE, Window.WINDOW_HIDDEN | Window.WINDOW_MULTISAMPLE_X8 | Window.WINDOW_RESIZEABLE | Window.WINDOW_DEBUGMODE);
		window.setSwapInterval(0);
		window.setIcon("/res/texture/icon.png");
		window.setBackFaceCulling(false);
		window.clearColor(1.0f, 1.0f, 1.0f, 1.0f);
		
		//Critical Resources
		Audio.init();
		glyphs = GlyphTexture.loadFont("/res/font/arial.font", 64);
		single_texture_shader = Shader.singleTextureShader(); //("/res/shader/texture-single.vert.glsl", "/res/shader/texture-single.frag.glsl");
		mat4 projection = new mat4().ortho(-window.getAspect(), window.getAspect(), 1.0f, -1.0f);
		single_texture_shader.setUniformMat4("pr_matrix", projection);
		TextLabelTexture.initialize(window, glyphs, single_texture_shader);
		loadDescription = TextLabelTexture.bakeToTexture("Loading");
		gui_renderer = new Renderer();
		splashScreen = Texture.loadTextureSingle("/res/texture/splash.png");
		
		//Splash screen
		window.unhide();
		drawLoadingScreen(gui_renderer, 0.0f, null);
		
		//Start async database loading
		TextureLoader.init(16);
		AsyncResLoader loader = new AsyncResLoader();
		new Thread(loader).start();
		while (true) {
			float state = loader.queryLoadState();
			if (state == 1.0f) break;
			drawLoadingScreen(gui_renderer, state, "Loading resources");			
		}
		
		TextureLoader.finish();
		
		//Rest of the shaders
		Logger.info("Creating all the shaders");
		post_shader = Shader.loadFromSources("/res/shader/postprocess.vert.glsl", "/res/shader/postprocess.frag.glsl", true);
		point_shader = Shader.loadFromSources("/res/shader/geometrytexture.vert.glsl", "/res/shader/geometrytexture.frag.glsl", "/res/shader/geometrytexture.geom.glsl", true);
		multi_texture_shader = Shader.multiTextureShader(); //("/res/shader/texture.vert.glsl", "/res/shader/texture.frag.glsl");
		Logger.info("All shaders created successfully!");
		
		//Renderer
		blur_renderer = new Renderer();
		normal_renderer = new Renderer();

		//Game stuff
		float playerX = Settings.MAP_SIZE / 2.0f, playerY = Settings.MAP_SIZE / 2.0f;
		inventory = new Inventory();
		player = new Player(playerX, playerY, inventory);
		gui = new Gui(
			Database.getCreature("player").health,
			Database.getCreature("player").stamina,
			Database.getCreature("player").healthgain,
			Database.getCreature("player").staminagain
		);

		//Start async map loading
		loadWorld(Settings.WORLD_NAME, true);
		
		//System and software info
		Logger.info("LWJGL " + window.getLWJGL());
		Logger.info("Renderer " + window.getRenderer());
		
		//Main menu
		MainMenu.init(gui_renderer, multi_texture_shader, single_texture_shader, point_shader, post_shader, glyphs);
		window.setCurrentApp(this);

		//Reset window
		resize(window.getWidth(), window.getHeight());

		//Ready
		Logger.info("Game ready! Running update and renderloops");
	}

	//Load world with name
	//If not found, create one
	public void loadWorld(String name, boolean create) {
		AsyncMapLoader loader = new AsyncMapLoader();
		loadedMapName = name;
		loader.loadMap(name);
		new Thread(loader).start();
		while (true) {
			float state = loader.queryLoadState();
			if (state == 1.0f) { map = loader.getLoadedMap(); return; }//Load finished
			if (state == -1) { loadedMapName = null; break; } //Load failed
			drawLoadingScreen(gui_renderer, state, "Loading map");		
			
		}
		Logger.warn("Couldn't load world \"" + name + "\"");	

		if (create) {
			loader.createMap(name, 0);
			new Thread(loader).start();
			while (true) {
				float state = loader.queryLoadState();
				if (state == 1.0f) { map = loader.getLoadedMap(); return; } //Create finished
				if (state == -1) { loadedMapName = null; break; } //Create failed
				drawLoadingScreen(gui_renderer, state, "Creating map");			
			}
			
			Logger.error("Couldn't create world \"" + name + "\" with seed \"" + 0 + "\"");
		}
	}

	
	//Key press callback
	@Override
	public void keyPress(int key, int action) {
		gui.keyPress(key, action);
		if (action == Keys.PRESS) {
			if (key == Keys.KEY_ESCAPE) { paused = !paused; justPaused = true; return; }
			
			//Cant press or open anything while typing to chat or paused
			//if (gui.chatOpened() || paused) return;

			//Postshader
			if (key == Keys.KEY_F7) { post_shader.enable(); post_shader.setUniform1i("unif_lens", 0); justPaused = true; return; }
			if (key == Keys.KEY_F8) { post_shader.enable(); post_shader.setUniform1i("unif_lens", 1); justPaused = true; return; }

			//Player keys
			if (key == Keys.KEY_E) { player.setPos(new vec2(Math.round(player.getPos().x + 0.5f) - 0.5f, Math.round(player.getPos().y + 0.5f) - 0.5f)); return; }

			//Inventory
			if (key == Keys.KEY_R) { inventory.setVisible(!inventory.isVisible()); return; }
			if (key == Keys.KEY_ESCAPE) { inventory.setVisible(false); return; }

			//Inventory slot selecting
			if (key == Keys.KEY_1) { inventory.setSelectedSlot(0); return; }
			if (key == Keys.KEY_2) { inventory.setSelectedSlot(1); return; }
			if (key == Keys.KEY_3) { inventory.setSelectedSlot(2); return; }
			if (key == Keys.KEY_4) { inventory.setSelectedSlot(3); return; }
			if (key == Keys.KEY_5) { inventory.setSelectedSlot(4); return; }

			//Debug commands
			//Activate debug and advanced debug modes
			if (key == Keys.KEY_F1) {
				debugMode = !debugMode; 
				if (window.key(Keys.KEY_LEFT_SHIFT)) {
					debugMode = true;
					advancedDebugMode = debugMode;
				}
				else advancedDebugMode = false;
				return;
			}

			//Debug ceil creaures
			if (key == Keys.KEY_F2) {
				map.debugCeilCreatures();
			}

			//Get FPS
			if (key == Keys.KEY_F3) { Logger.info("Fps: " + gameloop.getFps()); return; }
			
			//Clear inventoy
			if (key == Keys.KEY_F4) { inventory.clear(); return; }
			
			//Add creature at player
			if (key == Keys.KEY_F5) { map.addCreature(player.getPos().x, player.getPos().y + 5.0f, 1, false); return; }
		}
	}

	
	//Mouse button press callback
	@Override
	public void buttonPress(int button, int action) {
		// TODO Auto-generated method stub
		
	}


	//Window resize callbacks
	@Override
	public void resize(int width, int height) {
		if (width == 0) width = window.getWidth();
		if (height == 0) height = window.getHeight();
		
		// TODO Auto-generated method stub
		window.viewport();
		
		//Matrices
		mat4 projection = new mat4().ortho(-window.getAspect(), window.getAspect(), 1.0f, -1.0f);
		multi_texture_shader.setUniformMat4("pr_matrix", projection);
		single_texture_shader.setUniformMat4("pr_matrix", projection);
		point_shader.setUniformMat4("pr_matrix", projection);
		projection = new mat4().ortho(-1.0f, 1.0f, -1.0f, 1.0f);
		post_shader.setUniformMat4("pr_matrix", projection);
		
		//Other
		post_shader.setUniform1i("unif_effect", 0);
		point_shader.setUniform1i("ortho", 1);
	}


	@Override
	public void charCallback(char character) {
		Logger.debug("Typed character: " + character);
		gui.typing(character, 0);
	}
	
	public String getResourcePath() {
		return "/res/";
	}
	
	public String getDataPath() {
		return Settings.SAVE_PATH + "/";
	}
	
	public String getSavePath() {
		//"C:/Users/eemel/AppData/Roaming/Unnamed Game" for windows
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
		 return 1.0f; //Settings.DEBUG_ZOOM;
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
		return (Player) player;
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
		// TODO Auto-generated method stub
		
	}



	public void saveWorld() {
		// TODO Auto-generated method stub
		
	}
	
}