package logic;

import graphics.GlyphTexture;
import graphics.Renderer;
import graphics.Shader;
import graphics.TextLabelTexture;
import graphics.primitives.VertexData;
import utility.Application;
import utility.Colors;
import utility.GameLoop;
import utility.Logger;
import utility.Maths;
import utility.mat4;
import utility.vec2;
import utility.vec3;
import utility.vec4;

public class MainMenu extends Application {
	
	static private GameLoop loop;
	static private Renderer renderer;
	static private Shader gui_shader;
	static private Shader single_shader;
	static private Shader post_shader;
	static private TextLabelTexture label;
	 
	static private final float UPS = 5;
	private float anglex = 0.0f;
	private float angley = 0.0f;


	public static void init(Renderer gui_renderer, Shader _gui_shader, Shader _post_shader, GlyphTexture glyphs) {
		renderer = gui_renderer;
		gui_shader = _gui_shader;
		post_shader = _post_shader;
		single_shader = Shader.singleTextureShader();
		
		label = TextLabelTexture.bakeToTexture("$0M$1A$2I$3N $4M$5E$6N$7U");
	}

	public MainMenu() {
		Main.game.getWindow().setCurrentApp(this);
		loop = new GameLoop((int) UPS, this);
		loop.run();
		Main.game.getWindow().setCurrentApp(Main.game);
	}
	

	@Override
	public void update() {
		//
	}

	@Override
	public void render(float preupdate_scale) {
		if (Main.game.getWindow().shouldClose()) loop.stop();
		Main.game.getWindow().clear(0.2f, 0.2f, 0.2f, 1.0f);

		//View angles
		float mx = Main.game.getWindow().getCursor().x, my = Main.game.getWindow().getCursor().y;
		anglex = -mx * 4.0f; angley = -my * 4.0f;

		//Set matrices
		mat4 pr_matrix = new mat4().perspectiveDeg(90.0f, Main.game.getWindow().getAspect());
		mat4 vw_matrix = new mat4()
				.lookAt((float) Math.cos(anglex), angley, (float) Math.sin(anglex),
				//.lookAt(-2.0f, 0.0f, -2.0f,
			             0.0f, 0.0f, 0.0f,
			             0.0f, -1.0f, 0.0f);
		single_shader.setUniformMat4("pr_matrix", pr_matrix);
		single_shader.setUniformMat4("vw_matrix", vw_matrix);

		//Main menu text
		single_shader.setUniform1i("usetex", 1);
		label.queueDrawCentered(new vec3(0.0f, 0.0f, 0.0f), new vec2(0.5f));
		TextLabelTexture.drawQueue(false);

		Main.game.getWindow().update();
		Main.game.getWindow().input();
	}



	@Override
	public void cleanup() {
	}



	@Override
	public void init() {
		
	}



	@Override
	public void resize(int width, int height) {
		loop.stop();
	}



	@Override
	public void keyPress(int key, int action) {
		loop.stop();
	}



	@Override
	public void buttonPress(int button, int action) {
		loop.stop();
	}



	@Override
	public void mousePos(float x, float y) {
		// TODO Auto-generated method stub
		
	}



	@Override
	public void charCallback(char character) {
		loop.stop();
	}



	@Override
	public void scroll(float x_delta, float y_delta) {
		loop.stop();
	}

}
