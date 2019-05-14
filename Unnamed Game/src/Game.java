import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.Version;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL20;

import audio.Audio;
import graphics.Framebuffer;
import graphics.GlyphTexture;
import graphics.Renderer;
import graphics.Shader;
import graphics.Texture;
import graphics.Window;
import utility.Application;
import utility.GameLoop;
import utility.Keys;
import utility.Logger;

public class Game extends Application {

	public final float INITIAL_WINDOW_ASPECT = (16.0f / 9.0f);
	public final int INITIAL_WINDOW_WIDTH = 1280;
	public final int INITIAL_WINDOW_HEIGHT = (int)(INITIAL_WINDOW_WIDTH / INITIAL_WINDOW_ASPECT);
	public final String APP_VERSION = "0.4.0";
	public final String APP_NAME = "Unnamed Game - " + APP_VERSION + " (Java Port)";
	public final float DEBUG_ZOOM = 1.0f;

	private Shader shader;
	private Shader point_shader;
	private Shader post_shader;
	
	private Framebuffer framebuffer1;
	private Framebuffer framebuffer2;
	
	private Renderer renderer;
	private GlyphTexture glyphs;
	private Texture texture;
	private Window window;
	private Audio audioHit;
	private Audio audioSwing;
	private Audio audioCollect;
	private GameLoop gameloop;
	
	private int n = 0;
	private int counter = 0;
	

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
		if (window.keyPress(GLFW.GLFW_KEY_UP)) {
			audioHit.play();
		}
		if (window.keyPress(GLFW.GLFW_KEY_DOWN)) {
			audioSwing.play();
		}
		if (window.keyPress(GLFW.GLFW_KEY_RIGHT)) {
			audioCollect.play();
		}
		shader.setUniform1f("brightness", window.getScrollTotal());
		//System.out.println(window.getScrollTotal());
		shader.setUniform2f("mouse", new Vector2f(window.getMouseX(), INITIAL_WINDOW_HEIGHT - window.getMouseY()));
		//System.out.println(window.getMouseX() + ", " + window.getMouseY());
		
		
		//Rendering
		//---------
		
		//Render framebuffer
		//framebuffer1.bind();
		window.clear();
		shader.enable();
		
		counter++;
		if (counter > 200) {
			counter = 0;
			n = (n + 1) % 224;
		}
		//System.out.println(n);
		
		framebuffer1.bind();
		framebuffer1.clear();
		renderer.submitQuad(new Vector3f(-0.5f, -0.5f, 0.0f), new Vector2f(1.0f, 1.0f), n, new Vector4f(1.0f, 1.0f, 1.0f, 1.0f));
		renderer.draw(glyphs.getTexture().getId(), glyphs.getTexture().getType());
		
		//for (int i = 0; i < 0; i++) {
		//	framebuffer2.bind();
		//	window.clear();
		//	post_shader.setUniform1i("unif_effect", 1);
		//	renderer.submitQuad(new Vector3f(-1.0f, 1.0f, 0.0f), new Vector2f(2.0f, -2.0f), 0, new Vector4f(1.0f, 1.0f, 1.0f, 1.0f));
		//	renderer.draw(framebuffer1.getTexture().getId(), framebuffer1.getTexture().getType());
        //
		//	framebuffer1.bind();
		//	window.clear();
		//	post_shader.setUniform1i("unif_effect", 2);
		//	renderer.submitQuad(new Vector3f(-1.0f, 1.0f, 0.0f), new Vector2f(2.0f, -2.0f), 0, new Vector4f(1.0f, 1.0f, 1.0f, 1.0f));
		//	renderer.draw(framebuffer2.getTexture().getId(), framebuffer2.getTexture().getType());
		//}
		
		Framebuffer.unbind();
		window.clear();
		post_shader.setUniform1i("unif_effect", 3);
		renderer.submitQuad(new Vector3f(-1.0f, 1.0f, 0.0f), new Vector2f(2.0f, -2.0f), 0, new Vector4f(1.0f, 1.0f, 1.0f, 1.0f));
		renderer.draw(framebuffer1.getTexture().getId(), framebuffer1.getTexture().getType());
		
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
		shader = new Shader("res/shader/texture.vert.glsl", "res/shader/texture.frag.glsl");
		//post_shader = new Shader("res/shader/texture.vert.glsl", "res/shader/texture.frag.glsl");
		point_shader = new Shader("res/shader/geometrytexture.vert.glsl", "res/shader/geometrytexture.frag.glsl", "res/shader/geometrytexture.geom.glsl");
		Logger.out("All shaders created successfully!");

		resize(window.getWidth(), window.getHeight());
		
		//Renderer
		renderer = new Renderer();
		
		//Texture
		texture = Texture.loadTextureAtlas(16, 16, 16, "res/texture/atlas.png");
		glyphs = GlyphTexture.loadFont("res/font/arial.ttf", 23);
		
		//Audio
		audioHit = Audio.loadAudio("res/audio/hit.ogg");
		audioSwing = Audio.loadAudio("res/audio/swing.ogg");
		audioCollect = Audio.loadAudio("res/audio/collect.ogg");
		
		//Framebuffers
		framebuffer1 = Framebuffer.createFramebuffer(window.getWidth(), window.getHeight());
		framebuffer2 = Framebuffer.createFramebuffer(window.getWidth(), window.getHeight());
		
		//System and software info
		System.out.println("LWJGL " + Version.getVersion());
		System.out.println("Renderer " + GL20.glGetString(GL20.GL_RENDERER));
		
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

		//Matrices
		Matrix4f projection = new Matrix4f().ortho(-window.aspect() * DEBUG_ZOOM, window.aspect() * DEBUG_ZOOM, DEBUG_ZOOM, -DEBUG_ZOOM, 0.0f, 10000.0f);
		shader.setUniformMat4("pr_matrix", projection);
		point_shader.setUniformMat4("pr_matrix", projection);
		projection = new Matrix4f().ortho(-1.0f, 1.0f, 1.0f, -1.0f, 0.0f, 1.0f);
		post_shader.setUniformMat4("pr_matrix", projection);
		
		//Other
		post_shader.setUniform1i("unif_effect", 0);
		point_shader.setUniform1i("ortho", 1);
	}
	
}