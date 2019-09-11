package stages;

import graphics.Renderer;
import graphics.TextLabelTexture;
import graphics.TextLabelTexture.alignment;
import graphics.Texture;
import graphics.primitives.Primitive.Primitives;
import graphics.primitives.Quad;
import logic.AsyncLoader;
import logic.AsyncMapLoader;
import logic.AsyncResLoader;
import logic.ShaderManager;
import stages.GameStateManager.GameStates;
import utility.Colors;
import utility.vec2;

public class Loader extends State {
	
	private TextLabelTexture loadDescription;
	private Texture splash_screen;
	private Renderer renderer;
	
	private String load_description;
	private float load_state;
	
	private AsyncLoader async_loader;
	private GameStates after_complete;
	
	
	
	public void loadRes(GameStates after_complete) {
		this.load_description = "Loading resources...";
		this.async_loader = new AsyncResLoader();
		this.after_complete = after_complete;
	}
	
	public void loadMap(String name, GameStates after_complete) {
		this.load_description = "Loading map...";
		this.async_loader = new AsyncMapLoader(name);
		this.after_complete = after_complete;
	}
	
	public void createMap(String name, int seed, GameStates after_complete) {
		this.load_description = "Generating map...";
		this.async_loader = new AsyncMapLoader(name, seed);
		this.after_complete = after_complete;
	}
	
	public void finish() {
		async_loader.finish();
		GameStateManager.setState(after_complete);
	}

	@Override
	public void render(float preupdate_scale) {

		//Check loadstate and send it to loading screen
		load_state = async_loader.queryLoadState();
		if (load_state == 1.0f) finish();
		
		GameStateManager.window.setSwapInterval(2); //  No need to run loading screen with full power lol
		vec2 pos = new vec2(-0.5f, -0.5f);
		vec2 size = new vec2(1.0f, 1.0f);
		GameStateManager.window.clear(1.0f, 1.0f, 1.0f, 1.0f);
		
		// Back texture
		ShaderManager.single_texture_shader.setUniform1i("usetex", 1);
		splash_screen.bind();
		renderer.clear();
		renderer.submit(new Quad(pos, size, 0, Colors.WHITE));
		renderer.draw();
		
		// Loading bar
		ShaderManager.single_texture_shader.setUniform1i("usetex", 0);
		renderer.clear();
		pos = new vec2(-0.8f, 0.8f);
		renderer.submit(new Quad(pos, new vec2(1.6f, 0.1f), 0, Colors.BLACK));
		pos = new vec2(-0.79f, 0.81f);
		renderer.submit(new Quad(pos, new vec2(load_state * 1.58f, 0.08f), 0, Colors.RED));
		renderer.draw();
		
		// Description
		if (load_description != null) {
			loadDescription.rebake("$2" + load_description);
			loadDescription.submit(new vec2(-0.8f, 0.7f), new vec2(0.1f), alignment.TOP_LEFT);
			TextLabelTexture.drawQueue(true);
		}
	}

	@Override
	public void init() {
		load_state = 0;

		splash_screen = Texture.loadTextureSingle("res/texture/splash.png");
		renderer = new Renderer(Primitives.Quad, 40);
		loadDescription = TextLabelTexture.bakeToTexture(".");
		if (async_loader == null) throw new IllegalStateException(); // No load type
		
		async_loader.init();
		new Thread(async_loader).start();
	}

	@Override
	public void cleanup() {
	}

	@Override
	public void update() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void resize(int width, int height) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void keyPress(int key, int action) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void buttonPress(int button, int action) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mousePos(float x, float y) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void scroll(float x_delta, float y_delta) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void charCallback(char character) {
		// TODO Auto-generated method stub
		
	}

}
