import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.Version;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL20;

import audio.Audio;
import graphics.Renderer;
import graphics.Shader;
import graphics.Window;
import utility.GameLoop;
import utility.Texture;

public class Game {

	public static final float INITIAL_WINDOW_ASPECT = (16.0f / 9.0f);
	public static final int INITIAL_WINDOW_WIDTH = 1280;
	public static final int INITIAL_WINDOW_HEIGHT = (int)(INITIAL_WINDOW_WIDTH / INITIAL_WINDOW_ASPECT);
	public static final String APP_VERSION = "0.4.0";
	public static final String APP_NAME = "Unnamed Game - " + APP_VERSION + " (Java Port)";

	private static Shader shader;
	private static Renderer renderer;
	private static Texture texture;
	private static Window window;
	private static Audio audioHit;
	private static Audio audioSwing;
	private static Audio audioCollect;
	private static GameLoop gameloop;
	
	
	static Runnable init = new Runnable() {
		@Override
		public void run() {
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
	};
	
	static Runnable update = new Runnable() {
		@Override
		public void run() {
			// TODO Auto-generated method stub
			
		}
	};
	
	static Runnable render = new Runnable() {
		@Override
		public void run() {
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
	};
	
	static Runnable cleanup = new Runnable() {
		@Override
		public void run() {
			// TODO Auto-generated method stub
			
		}
	};
	
	public static void main(String args[]) {
		gameloop = new GameLoop(60, init, update, render, cleanup);
        (new Thread(gameloop)).start();
	}
	
}
