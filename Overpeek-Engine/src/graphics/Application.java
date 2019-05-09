package graphics;

import org.lwjgl.opengl.*;

import audio.Audio;
import utility.Texture;

import org.joml.*;
import org.lwjgl.Version;
import org.lwjgl.glfw.GLFW;


public class Application implements Runnable {

	public static final float INITIAL_WINDOW_ASPECT = (16.0f / 9.0f);
	public static final int INITIAL_WINDOW_WIDTH = 1280;
	public static final int INITIAL_WINDOW_HEIGHT = (int)(INITIAL_WINDOW_WIDTH / INITIAL_WINDOW_ASPECT);
	public static final String APP_VERSION = "0.4.0";
	public static final String APP_NAME = "Overpeek Engine Test - " + APP_VERSION + " (Java Port)";
	
	private boolean running = false;
	private Shader shader;
	private Renderer renderer;
	private Texture texture;
	private Window window;
	private Audio audioHit;
	private Audio audioSwing;
	private Audio audioCollect;
	
	
	public void init() {
		running = true;

		//Window
		window = new Window(INITIAL_WINDOW_WIDTH, INITIAL_WINDOW_HEIGHT, APP_NAME, Window.WINDOW_RESIZEABLE);
		window.clearColor(0.2f, 0.2f, 0.2f, 1.0f);
		//window.setIcon("res/texture/icon.png");
		
		//Shaders
		shader = new Shader("res/shader/texture.vert.glsl", "res/shader/texture.frag.glsl");
		Matrix4f ortho = new Matrix4f().ortho2D(-window.aspect(), window.aspect(), 1.0f, -1.0f);
		shader.SetUniformMat4("pr_matrix", ortho);
		
		//Renderer
		renderer = new Renderer();
		
		//Texture
		texture = Texture.loadTextureAtlas(16, 16, 16, "res/texture/atlas.png");
		
		//Audio
		audioHit = Audio.loadAudio("res/audio/hit.ogg");
		audioSwing = Audio.loadAudio("res/audio/swing.ogg");
		audioCollect = Audio.loadAudio("res/audio/collect.ogg");
		
		//System and software info
		System.out.println("LWJGL " + Version.getVersion());
		System.out.println("Renderer " + GL20.glGetString(GL20.GL_RENDERER));
	}
	
	public void update() {
		
	}
	
	public void render() {
		window.clear();
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
		//System.out.println(window.getMouseX() + ", " + window.getMouseY());
		
		//Rendering
		int ttid = (int)(System.nanoTime() / 5000000);
		int textureid = ttid % 255;
		shader.enable();
		renderer.submitQuad(new Vector3f(-0.5f, -0.5f, 0.0f), new Vector2f(1.0f, 1.0f), textureid, new Vector4f(1.0f, 1.0f, 1.0f, 1.0f));
		renderer.draw(texture.getId(), texture.getType());
		
		window.update();
	}
	
	public void cleanup() {
		
	}
	
	public void run() {
		init();
	    long lastTime = System.nanoTime();
	    double delta = 0.0;
	    double ns = 1000000000.0 / 60.0;
	    long timer = System.currentTimeMillis();
	    int updates = 0;
	    int frames = 0;
	    while(running){
	        long now = System.nanoTime();
	        delta += (now - lastTime) / ns;
	        lastTime = now;
	        if (delta >= 1.0) {
	            update();
	            updates++;
	            delta--;
	        }
	        render();
	        frames++;
	        if (System.currentTimeMillis() - timer > 1000) {
	            timer += 1000;
	            System.out.println(updates + " ups, " + frames + " fps");
	            updates = 0;
	            frames = 0;
	        }
	        if(window.shouldClose()) running = false;
	    }

	    cleanup();
	}
}
