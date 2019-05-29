package logic;

import javax.naming.LinkLoopException;

import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;

import audio.Audio;
import creatures.Creature;
import creatures.Player;
import graphics.GlyphTexture;
import graphics.Renderer;
import graphics.Renderer.VertexData;
import graphics.Shader;
import graphics.TextLabelTexture;
import graphics.Texture;
import graphics.Window;
import utility.Application;
import utility.Colors;
import utility.Keys;
import utility.Logger;
import world.Map;

public class Game extends Application {

	public static final float INITIAL_WINDOW_ASPECT = (16.0f / 9.0f);
	public static final int INITIAL_WINDOW_WIDTH = 1280;
	public static final int INITIAL_WINDOW_HEIGHT = (int)(INITIAL_WINDOW_WIDTH / INITIAL_WINDOW_ASPECT);
	public static final String APP_VERSION = "0.4.0";
	public static final String APP_NAME = "Unnamed Game - " + APP_VERSION + " (Java Port)";
	public static final float DEBUG_ZOOM = 1.0f;

	private static Shader multi_texture_shader;
	private static Shader single_texture_shader;
	private static Shader point_shader;
	private static Shader post_shader;
	
	private static TextLabelTexture label;
	private static Renderer world_renderer;
	private static Renderer gui_renderer;
	private static Texture texture;
	private static GlyphTexture glyphs;
	private static Window window;
	private static Audio audioHit;
	private static Audio audioSwing;
	private static Audio audioCollect;
	private static Map map;
	private static Inventory inventory;
	private static Creature player;
	private static boolean mainMenu;
	private static boolean paused;
	private static boolean justPaused;

	int oldfps;
	
	
	
	//Main update loop
	@Override
	public void update() {
		// TODO Auto-generated method stub
		if (gameloop.getFps() != oldfps) {
			label.rebake("FPS: " + gameloop.getFps(), glyphs);
			resize(window.getWidth(), window.getHeight());
		}
		oldfps = gameloop.getFps();
	}

	
	
	//Main render loop
	@Override
	public void render(float corrector) {
		if (window == null || window.shouldClose()) gameloop.stop();
		window.clear();
        
		//World tiles
		if (!paused || justPaused) {
			map.submitToRenderer(world_renderer, -player.getX() - player.getVel_x() * corrector / Settings.UPDATES_PER_SECOND, -player.getY() - player.getVel_y() * corrector / Settings.UPDATES_PER_SECOND, corrector);
        
			//m_player->submitToRenderer(m_worldrenderer.get(), -m_player->getX() - m_player->getVelX() * corrector / Settings.UPDATES_PER_SECOND, -m_player->getY() - m_player->getVelY() * corrector / UPDATES_PER_SECOND, corrector, renderScale());
			//m_gui->renderBlur(m_worldrenderer.get());
			//m_inventory->render(m_worldrenderer.get());
		}
		//
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
		// TODO Auto-generated method stub
		
	}

	
	//Main init
	@Override
	public void init() {
		// TODO Auto-generated method stub
		//Window
		Logger.out("Creating window");
		window = new Window(INITIAL_WINDOW_WIDTH, INITIAL_WINDOW_HEIGHT, APP_NAME, Window.WINDOW_MULTISAMPLE_X8 | Window.WINDOW_RESIZEABLE | Window.WINDOW_DEBUGMODE);
		window.setSwapInterval(0);
		window.setIcon("res/texture/icon.png");
		window.setLineWidth(5.0f);
		window.setBackFaceCulling(false);
		window.setClearColor(1.0f, 1.0f, 1.0f, 1.0f);

		//Loading screen (barely noticeable lol)
		Shader tmpshader = new Shader("res/shader/texture-single.vert.glsl", "res/shader/texture-single.frag.glsl");
		Matrix4f projection = new Matrix4f().ortho(-window.aspect(), window.aspect(), 1.0f, -1.0f, 0.0f, 10000.0f);
		tmpshader.setUniformMat4("pr_matrix", projection);
		gui_renderer = new Renderer();
		Texture splashScreen = Texture.loadTextureSingle("res/texture/splash.png");
		window.clear();
		Vector3f pos = new Vector3f(-0.5f, -0.5f, 0.0f);
		Vector2f size = new Vector2f(1.0f, 1.0f);
		gui_renderer.submitVertex(new VertexData(new Vector3f(pos.x, pos.y, pos.z), 					new Vector2f(0.0f, 0.0f), 0, Colors.WHITE));
		gui_renderer.submitVertex(new VertexData(new Vector3f(pos.x, pos.y + size.y, pos.z), 			new Vector2f(0.0f, 1.0f), 0, Colors.WHITE));
		gui_renderer.submitVertex(new VertexData(new Vector3f(pos.x + size.x, pos.y + size.y, pos.z), 	new Vector2f(1.0f, 1.0f), 0, Colors.WHITE));
		gui_renderer.submitVertex(new VertexData(new Vector3f(pos.x + size.x, pos.y, pos.z), 			new Vector2f(1.0f, 0.0f), 0, Colors.WHITE));
		tmpshader.enable();
		gui_renderer.drawAsQuads(splashScreen.getId(), splashScreen.getType());
		window.update();
		
		//Audio
		Logger.out("Creating audio device");
		Audio.init();
		
		//Shaders
		Logger.out("Creating all the shaders");
		post_shader = new Shader("res/shader/postprocess.vert.glsl", "res/shader/postprocess.frag.glsl");
		multi_texture_shader = new Shader("res/shader/texture.vert.glsl", "res/shader/texture.frag.glsl");
		single_texture_shader = new Shader("res/shader/texture-single.vert.glsl", "res/shader/texture-single.frag.glsl");
		//post_shader = new Shader("res/shader/texture.vert.glsl", "res/shader/texture.frag.glsl");
		point_shader = new Shader("res/shader/geometrytexture.vert.glsl", "res/shader/geometrytexture.frag.glsl", "res/shader/geometrytexture.geom.glsl");
		Logger.out("All shaders created successfully!");
		
		//Loading resources
		Logger.out("Loading all resources");
		texture = Texture.loadTextureAtlas(16, 16, 16, "res/texture/atlas.png");
		glyphs = GlyphTexture.loadFont("res/font/arial.font", 256);
		audioHit = Audio.loadAudio("res/audio/hit.ogg");
		audioSwing = Audio.loadAudio("res/audio/swing.ogg");
		audioCollect = Audio.loadAudio("res/audio/collect.ogg");
		TextLabelTexture.initialize();
		TextureLoader.init(16);
		Database.initialize();
		TextureLoader.finish();
		label = TextLabelTexture.bakeTextToTexture("FPS", glyphs);
		TextLabelTexture.viewPortReset(Game.getWindow());
		
		//Renderer
		world_renderer = new Renderer();
		//for (int x = 0; x < 100; x++) {
		//	for (int y = 0; y < 100; y++) {
		//		world_renderer.submitQuad(new Vector3f(-1.0f + y / 50.0f, -1.0f + x / 50.0f, 0.0f), new Vector2f(0.02f), Database.items.get(0).texture, Colors.WHITE);				
		//	}
		//}
		int texture = Database.particles.get(0).texture;
		world_renderer.submitQuad(new Vector3f(-0.5f, -0.5f, 0.0f), new Vector2f(1.0f), texture, Colors.WHITE);				
		
		//Framebuffers
		//Logger.out("Creating framebuffers");
		//framebuffer1 = Framebuffer.createFramebuffer(window.getWidth(), window.getHeight());
		//framebuffer2 = Framebuffer.createFramebuffer(window.getWidth(), window.getHeight());

		float playerX = Settings.MAP_SIZE / 2.0f, playerY = Settings.MAP_SIZE / 2.0f;
		inventory = new Inventory();
		player = new Player(playerX, playerY, inventory);
		map = new Map();
		
		//System and software info
		Logger.out("LWJGL " + window.getLWJGL());
		Logger.out("Renderer " + window.getRenderer());

		//Main menu
		mainMenu = true;
		MainMenu.init(gui_renderer, multi_texture_shader, single_texture_shader, point_shader, post_shader, glyphs, Game.texture);
		window.setCurrentApp(this);

		//Reset window
		resize(window.getWidth(), window.getHeight());

		//Loading world
		//loadWorld(WORLD_NAME, true);
		paused = false;
		justPaused = false;

		//Ready
		Logger.out("Game ready! Running update and renderloops");
	}

	
	//Key press callback
	@Override
	public void keyPress(int key, int action) {
		// TODO Auto-generated method stub
		
	}

	
	//Mouse button press callback
	@Override
	public void buttonPress(int button, int action) {
		// TODO Auto-generated method stub
		
	}


	//Window resize callbacks
	@Override
	public void resize(int width, int height) {
		// TODO Auto-generated method stub
		window.viewport();
		window.setClearColor(0.1f, 0.1f, 0.1f, 1.0f);
		
		//Matrices
		Matrix4f projection = new Matrix4f().ortho(-window.aspect() * DEBUG_ZOOM, window.aspect() * DEBUG_ZOOM, DEBUG_ZOOM, -DEBUG_ZOOM, 0.0f, 10000.0f);
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
	
	public static Map getMap() {
		return map;
	}
	
	public static float renderScale() {
		return 720.0f / window.getHeight(); 
	}


	public static Creature getPlayer() {
		return player;
	}


	public static Window getWindow() {
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