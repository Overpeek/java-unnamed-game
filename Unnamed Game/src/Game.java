import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector4f;

import audio.Audio;
import graphics.Framebuffer;
import graphics.GlyphTexture;
import graphics.Renderer;
import graphics.Shader;
import graphics.TextLabelTexture;
import graphics.Texture;
import graphics.Window;
import utility.Application;
import utility.Keys;
import utility.Logger;

public class Game extends Application {

	public final float INITIAL_WINDOW_ASPECT = (16.0f / 9.0f);
	public final int INITIAL_WINDOW_WIDTH = 1280;
	public final int INITIAL_WINDOW_HEIGHT = (int)(INITIAL_WINDOW_WIDTH / INITIAL_WINDOW_ASPECT);
	public final String APP_VERSION = "0.4.0";
	public final String APP_NAME = "Unnamed Game - " + APP_VERSION + " (Java Port)";
	public final float DEBUG_ZOOM = 1.0f;

	private Shader multi_texture_shader;
	private Shader single_texture_shader;
	private Shader point_shader;
	private Shader post_shader;
	
	private Framebuffer framebuffer1;
	private Framebuffer framebuffer2;
	
	private TextLabelTexture label;
	private Renderer renderer;
	private GlyphTexture glyphs;
	private Texture texture;
	private Window window;
	private Audio audioHit;
	private Audio audioSwing;
	private Audio audioCollect;
	

	//Main update loop
	@Override
	public void update() {
		// TODO Auto-generated method stub
		
	}

	
	//Main render loop
	@Override
	public void render() {
		// TODO Auto-generated method stub
		if (window.shouldClose() || window.key(Keys.KEY_ESCAPE)) gameloop.stop();
		
		window.input();

		//Input
		if (window.keyPress(Keys.KEY_UP)) {
			audioHit.play();
		}
		if (window.keyPress(Keys.KEY_DOWN)) {
			audioSwing.play();
		}
		if (window.keyPress(Keys.KEY_RIGHT)) {
			audioCollect.play();
		}
		
		
		//Rendering
		//---------
		
		//Render framebuffer
		//framebuffer1.bind();
		window.clear();
		
		//Logger.out("FPS: " + gameloop.getFps(), Logger.type.INFO);
		
		post_shader.enable();
		renderer.submitQuad(new Vector3f(-1.0f, 0.0f, 0.0f), new Vector2f(0.07f * label.getFrameBuffer().aspect() / window.aspect(), -0.07f), 0, new Vector4f(1.0f, 1.0f, 1.0f, 1.0f));
		renderer.draw(label.getFrameBuffer().getTexture().getId(), label.getFrameBuffer().getTexture().getType());
		
		
		window.update();
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
		window = new Window(INITIAL_WINDOW_WIDTH, INITIAL_WINDOW_HEIGHT, APP_NAME, Window.WINDOW_RESIZEABLE);
		window.setSwapInterval(0);
		window.setClearColor(0.1f, 0.1f, 0.1f, 1.0f);
		window.setIcon("res/texture/icon.png");
		window.setLineWidth(5.0f);
		window.setBackFaceCulling(false);

		//Loading screen (barely noticeable lol)
		Shader tmpshader = new Shader("res/shader/texture-single.vert.glsl", "res/shader/texture-single.frag.glsl");
		Matrix4f projection = new Matrix4f().ortho(-window.aspect(), window.aspect(), 1.0f, -1.0f, 0.0f, 10000.0f);
		tmpshader.setUniformMat4("pr_matrix", projection);
		//guirenderer = std::unique_ptr<oe::Renderer>(new oe::Renderer("res/font/arial.ttf", m_window.get()));
		//oe::TextureManager::loadTexture("res/texture/splash.png", 3);
		//m_window->clear();
		//glm::vec3 pos = glm::vec3(-0.5f, -0.5f, 0.0f);
		//glm::vec2 size = glm::vec2(1.0);
		//m_guirenderer->quadRenderer->submitVertex(oe::VertexData(glm::vec3(pos.x, pos.y, pos.z), glm::vec2(0.0f, 0.0f), 0, OE_COLOR_WHITE));
		//m_guirenderer->quadRenderer->submitVertex(oe::VertexData(glm::vec3(pos.x, pos.y + size.y, pos.z), glm::vec2(0.0f, 1.0f), 0, OE_COLOR_WHITE));
		//m_guirenderer->quadRenderer->submitVertex(oe::VertexData(glm::vec3(pos.x + size.x, pos.y + size.y, pos.z), glm::vec2(1.0f, 1.0f), 0, OE_COLOR_WHITE));
		//m_guirenderer->quadRenderer->submitVertex(oe::VertexData(glm::vec3(pos.x + size.x, pos.y, pos.z), glm::vec2(1.0f, 0.0f), 0, OE_COLOR_WHITE));
        //
		//m_guirenderer->draw(tmpshader, tmpshader, oe::TextureManager::getTexture(3), false);
		//m_window->update();
		
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
		
		//Renderer
		renderer = new Renderer();
		
		//Texture
		texture = Texture.loadTextureAtlas(16, 16, 16, "res/texture/atlas.png");
		glyphs = GlyphTexture.loadFont("res/font/arial.ttf", 256);
		Logger.out("Font and textures loaded!");
		
		//Audio
		Logger.out("Loading audio");
		audioHit = Audio.loadAudio("res/audio/hit.ogg");
		audioSwing = Audio.loadAudio("res/audio/swing.ogg");
		audioCollect = Audio.loadAudio("res/audio/collect.ogg");
		
		//Framebuffers
		Logger.out("Creating framebuffers");
		framebuffer1 = Framebuffer.createFramebuffer(window.getWidth(), window.getHeight());
		framebuffer2 = Framebuffer.createFramebuffer(window.getWidth(), window.getHeight());
		
		//Baked text
		Logger.out("Baking text");
		TextLabelTexture.initialize();
		label = TextLabelTexture.bakeTextToTexture("This is one really long example. TRARNSUJOFABNFOIAUWFHUOWAHFHWAOFHOWAUFOUWHFOUFSNAKLFANWIO!" + gameloop.getFps(), glyphs);
		
		//System and software info
		System.out.println("LWJGL " + window.getLWJGL());
		System.out.println("Renderer " + window.getRenderer());


		resize(window.getWidth(), window.getHeight());
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

	
	//Mouse position callback
	@Override
	public void mousePos(int x, int y) {
		// TODO Auto-generated method stub
		
	}


	//Window resize callbacks
	@Override
	public void resize(int width, int height) {
		// TODO Auto-generated method stub
		window.viewport();
		
		//Matrices
		Matrix4f projection = new Matrix4f().ortho(-window.aspect() * DEBUG_ZOOM, window.aspect() * DEBUG_ZOOM, DEBUG_ZOOM, -DEBUG_ZOOM, 0.0f, 10000.0f);
		multi_texture_shader.setUniformMat4("pr_matrix", projection);
		single_texture_shader.setUniformMat4("pr_matrix", projection);
		point_shader.setUniformMat4("pr_matrix", projection);
		projection = new Matrix4f().ortho(-1.0f, 1.0f, 1.0f, -1.0f, 0.0f, 1.0f);
		post_shader.setUniformMat4("pr_matrix", projection);
		
		//Other
		post_shader.setUniform1i("unif_effect", 0);
		point_shader.setUniform1i("ortho", 1);
	}
	
}