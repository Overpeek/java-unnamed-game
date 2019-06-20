package logic;

import java.io.IOException;
import java.net.URL;

import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;

import audio.Audio;
import creatures.Creature;
import creatures.Player;
import graphics.GlyphTexture;
import graphics.Renderer;
import graphics.Shader;
import graphics.TextLabelTexture;
import graphics.Texture;
import graphics.VertexData;
import graphics.Window;
import utility.Application;
import utility.Colors;
import utility.GameLoop;
import utility.Keys;
import utility.Logger;
import world.Map;

public class Game extends Application {

	public final float INITIAL_WINDOW_ASPECT = (16.0f / 9.0f);
	public final int INITIAL_WINDOW_WIDTH = 1280;
	public final int INITIAL_WINDOW_HEIGHT = (int)(INITIAL_WINDOW_WIDTH / INITIAL_WINDOW_ASPECT);
	public final String APP_VERSION = "0.4.0";
	public final String APP_NAME = "Unnamed Game - " + APP_VERSION + " (Java Port)";

	private Shader multi_texture_shader;
	private Shader single_texture_shader;
	private Shader point_shader;
	private Shader post_shader;
	
	private TextLabelTexture label;
	private Renderer blur_renderer;
	private Renderer normal_renderer;
	private Renderer gui_renderer;
	private Texture texture;
	private GlyphTexture glyphs;
	private Window window;
	private Audio audioHit;
	private Audio audioSwing;
	private Audio audioCollect;
	private Map map;
	private Inventory inventory;
	private Creature player;
	private boolean mainMenu;
	private boolean paused;
	private boolean justPaused;
	private Gui gui;
	
	public boolean advancedDebugMode;
	public boolean debugMode;
	
	
	
	//Main update loop
	@Override
	public void update() {
		// TODO Auto-generated method stub
		label.rebake("FPS: " + gameloop.getFps() + ", Biome: " + map.getBiome(player.getPos().x, player.getPos().y).name, glyphs);
		resize(window.getWidth(), window.getHeight());

		float playerSpeed = Database.getCreature("player").walkSpeed;

		if (window.key(Keys.KEY_LEFT_SHIFT)) playerSpeed *= 2;
		if (window.key(Keys.KEY_S)) { player.setAcc(new Vector2f(player.getAcc().x, playerSpeed)); }
		if (window.key(Keys.KEY_D)) { player.setAcc(new Vector2f(playerSpeed, player.getAcc().y)); }
		if (window.key(Keys.KEY_W)) { player.setAcc(new Vector2f(player.getAcc().x, -playerSpeed)); }
		if (window.key(Keys.KEY_A)) { player.setAcc(new Vector2f(-playerSpeed, player.getAcc().y)); }
		player.update(0, Settings.UPDATES_PER_SECOND);
		if (Settings.DEBUG_REGEN_WORLD) {
			loadWorld(Settings.WORLD_NAME, true); //Debug regen world
		}
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
		//world_renderer.submitBakedText(new Vector3f(-1.0f, -1.0f, 0.0f), new Vector2f(0.2f, 0.2f), label, Colors.WHITE);
		
		//World tiles
		if (!paused || justPaused) {
			blur_renderer.points.clear();
			normal_renderer.points.clear();
			map.submitToRenderer(blur_renderer, -player.getPos().x - player.getVel().x * corrector / Settings.UPDATES_PER_SECOND, -player.getPos().y - player.getVel().y * corrector / Settings.UPDATES_PER_SECOND, corrector);
  
			player.draw(blur_renderer, -player.getPos().x - player.getVel().x * corrector / Settings.UPDATES_PER_SECOND, -player.getPos().y - player.getVel().y * corrector / Settings.UPDATES_PER_SECOND, corrector, renderScale());
			gui.render(blur_renderer, normal_renderer);
			Database.modRendering(blur_renderer);
			//m_inventory->render(m_worldrenderer.get());
		}
		//
		point_shader.enable();
		blur_renderer.points.draw(TextureLoader.getTextureId(), TextureLoader.getTextureType());
		normal_renderer.points.draw(TextureLoader.getTextureId(), TextureLoader.getTextureType());

		//single_texture_shader.enable();
		//label.draw(new Vector3f(-window.getAspect(), -1.0f, 0.0f), new Vector2f(0.2f, 0.2f), Colors.WHITE);
		
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

	
	//Main init
	@Override
	public void init() {
		paused = false;
		justPaused = false;
		
		//Window
		Logger.out("Creating window");
		window = new Window(INITIAL_WINDOW_WIDTH, INITIAL_WINDOW_HEIGHT, APP_NAME, Window.WINDOW_MULTISAMPLE_X8 | Window.WINDOW_RESIZEABLE | Window.WINDOW_DEBUGMODE);
		window.setSwapInterval(0);
		window.setIcon("/res/texture/icon.png");
		window.setLineWidth(5.0f);
		window.setBackFaceCulling(false);
		window.setClearColor(1.0f, 1.0f, 1.0f, 1.0f);

		//Splash screen
		Shader tmpshader = new Shader("/res/shader/texture-single.vert.glsl", "/res/shader/texture-single.frag.glsl");
		Matrix4f projection = new Matrix4f().ortho(-window.getAspect(), window.getAspect(), 1.0f, -1.0f, 0.0f, 10000.0f);
		tmpshader.setUniformMat4("pr_matrix", projection);
		gui_renderer = new Renderer();
		Texture splashScreen = Texture.loadTextureSingle("/res/texture/splash.png");
		window.clear();
		Vector3f pos = new Vector3f(-0.5f, -0.5f, 0.0f);
		Vector2f size = new Vector2f(1.0f, 1.0f);
		gui_renderer.quads.submitVertex(new VertexData(new Vector3f(pos.x, pos.y, pos.z), 					new Vector2f(0.0f, 0.0f), 0, Colors.WHITE));
		gui_renderer.quads.submitVertex(new VertexData(new Vector3f(pos.x, pos.y + size.y, pos.z), 			new Vector2f(0.0f, 1.0f), 0, Colors.WHITE));
		gui_renderer.quads.submitVertex(new VertexData(new Vector3f(pos.x + size.x, pos.y + size.y, pos.z), new Vector2f(1.0f, 1.0f), 0, Colors.WHITE));
		gui_renderer.quads.submitVertex(new VertexData(new Vector3f(pos.x + size.x, pos.y, pos.z), 			new Vector2f(1.0f, 0.0f), 0, Colors.WHITE));
		tmpshader.enable();
		gui_renderer.quads.draw(splashScreen.getId(), splashScreen.getType());
		window.update();
		
		//Audio
		Logger.out("Creating audio device");
		Audio.init();
		
		//Shaders
		Logger.out("Creating all the shaders");
		post_shader = new Shader("/res/shader/postprocess.vert.glsl", "/res/shader/postprocess.frag.glsl");
		multi_texture_shader = new Shader("/res/shader/texture.vert.glsl", "/res/shader/texture.frag.glsl");
		single_texture_shader = new Shader("/res/shader/texture-single.vert.glsl", "/res/shader/texture-single.frag.glsl");
		//post_shader = new Shader("/res/shader/texture.vert.glsl", "/res/shader/texture.frag.glsl");
		point_shader = new Shader("/res/shader/geometrytexture.vert.glsl", "/res/shader/geometrytexture.frag.glsl", "/res/shader/geometrytexture.geom.glsl");
		Logger.out("All shaders created successfully!");
		
		//Loading resources
		Logger.out("Loading all resources");
		texture = Texture.loadTextureAtlas(16, 16, 16, "/res/texture/atlas.png");
		glyphs = GlyphTexture.loadFont("/res/font/arial.font", 256);
		audioHit = Audio.loadAudio("/res/audio/hit.ogg");
		audioSwing = Audio.loadAudio("/res/audio/swing.ogg");
		audioCollect = Audio.loadAudio("/res/audio/collect.ogg");
		TextLabelTexture.initialize(window, glyphs, single_texture_shader);
		TextureLoader.init(16);
		Database.initialize();
		TextureLoader.finish();
		label = TextLabelTexture.bakeTextToTexture("FPS", glyphs);
		
		//Renderer
		blur_renderer = new Renderer();
		normal_renderer = new Renderer();

		//Game stuff
		float playerX = Settings.MAP_SIZE / 2.0f, playerY = Settings.MAP_SIZE / 2.0f;
		inventory = new Inventory();
		player = new Player(playerX, playerY, inventory);
		map = new Map();
		gui = new Gui(
			Database.getCreature("player").health,
			Database.getCreature("player").stamina,
			Database.getCreature("player").healthgain,
			Database.getCreature("player").staminagain
		);
		
		//Loading world
		loadWorld(Settings.WORLD_NAME, true);
		
		//System and software info
		Logger.out("LWJGL " + window.getLWJGL());
		Logger.out("Renderer " + window.getRenderer());
		
		Logger.debug("Loop is " + gameloop.toString());

		//Main menu
		MainMenu.init(gui_renderer, multi_texture_shader, single_texture_shader, point_shader, post_shader, glyphs, Main.game.texture);
		window.setCurrentApp(this);

		Logger.debug("Loop is now " + gameloop.toString());

		//Reset window
		resize(window.getWidth(), window.getHeight());

		//Ready
		Logger.out("Game ready! Running update and renderloops");
	}

	//Load world with name
	//If not found, create one
	private void loadWorld(String name, boolean create) {
		if (!map.load(name)) {
			Logger.out("Couldn't load world \"" + name + "\"");

			if (create) {
				map.create(name, 5);
			}
		}
		else {
			((Player) player).load();
		}
	}

	
	//Key press callback
	@Override
	public void keyPress(int key, int action) {
		//gui.keyPress(key, action);
		if (action == Keys.PRESS) {
			if (key == Keys.KEY_ESCAPE) { paused = !paused; justPaused = true; return; }
			
			//Cant press or open anything while typing to chat or paused
			//if (gui.chatOpened() || paused) return;

			//Postshader
			if (key == Keys.KEY_F7) { post_shader.enable(); post_shader.setUniform1i("unif_lens", 0); justPaused = true; return; }
			if (key == Keys.KEY_F8) { post_shader.enable(); post_shader.setUniform1i("unif_lens", 1); justPaused = true; return; }

			//Player keys
			if (key == Keys.KEY_E) { player.setPos(new Vector2f(Math.round(player.getPos().x + 0.5f) - 0.5f, Math.round(player.getPos().y + 0.5f) - 0.5f)); return; }

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
		window.setClearColor(0.1f, 0.1f, 0.1f, 1.0f);
		
		//Matrices
		Matrix4f projection = new Matrix4f().ortho(-window.getAspect() * Settings.DEBUG_ZOOM, window.getAspect() * Settings.DEBUG_ZOOM, Settings.DEBUG_ZOOM, -Settings.DEBUG_ZOOM, 0.0f, 10000.0f);
		multi_texture_shader.setUniformMat4("pr_matrix", projection);
		single_texture_shader.setUniformMat4("pr_matrix", projection);
		point_shader.setUniformMat4("pr_matrix", projection);
		projection = new Matrix4f().ortho(-1.0f, 1.0f, -1.0f, 1.0f, -10000.0f, 10000.0f);
		post_shader.setUniformMat4("pr_matrix", projection);
		
		//Other
		post_shader.setUniform1i("unif_effect", 0);
		point_shader.setUniform1i("ortho", 1);
	}


	@Override
	public void charCallback(char character) {
		// TODO Auto-generated method stub
		
	}
	
	public String getResourcePath() {
		return "/res/";
	}
	
	public String getDataPath() {
		return Settings.SAVE_PATH + "/";
	}
	
	public String getSavePath() {
		//"C:/Users/eemel/AppData/Roaming/Unnamed Game" for windows
		return Settings.SAVE_PATH + "/" + map.getWorldName() + "/";
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
	
	public float renderScale() {
		return 720.0f / window.getHeight(); 
	}
	
	public float renderWidthScale() {
		return 1280.0f / window.getWidth(); 
	}
	
	public float defaultAspect() {
		return 1280.0f / 720.0f;
	}


	public Creature getPlayer() {
		return player;
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
	
}