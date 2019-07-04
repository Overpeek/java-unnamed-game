import java.awt.Font;

import graphics.GlyphTexture;
import graphics.Renderer;
import graphics.Shader;
import graphics.TextLabelTexture;
import graphics.Window;
import utility.Application;
import utility.Button;
import utility.Callback;
import utility.Colors;
import utility.GameLoop;
import utility.Keys;
import utility.mat4;
import utility.vec2;
import utility.vec3;
import utility.vec4;

public class Main extends Application {
	
	Renderer renderer;
	Shader single_shader;
	Shader multi_shader;
	Curve curves[][];
	int w = 1;
	int h = 1;
	float t = 0;
	
	vec2 windowPosBeforeHold;
	vec2 cursorPosBeforeHold;
	boolean holding = false;
	TextLabelTexture text;
	GlyphTexture glyphs;
	
	
	@Override
	public void update() {
		for (int x = 0; x < w; x++) {
			for (int y = 0; y < h; y++) {
				curves[x][y].update();
			}
		}
		text.rebake("Test jWäÅ. FPS: " + gameloop.getFps());
	}

	int n = 0;
	@Override
	public void render(float corrector) {
		renderer.spheres.clear();
		renderer.points.clear();
		renderer.lines.clear();

		//renderer.spheres.submit(new vec3(0.0f, 0.0f, 0.0f), new vec2(0.5f, 0.5f), 32, 0, new vec4(1.0f, 1.0f, 1.0f, 0.5f), new vec4(1.0f));
		//renderer.points.submit(new vec3(0.0f, 0.7f, 0.0f), new vec2(0.5f, 0.5f), 0, Colors.WHITE);
		for (int x = 0; x < w; x++) {
			for (int y = 0; y < h; y++) {
				curves[x][y].draw(renderer);
			}
		}

		single_shader.setUniform1i("usetex", 1);
		text.queueDraw(new vec3(-1.0f, 0.0f, 0.0f), new vec2(0.1f));
		TextLabelTexture.drawQueue();
		
		single_shader.setUniform1i("usetex", 0);
		renderer.spheres.draw(0, 0);
		renderer.points.draw(0, 0);
		renderer.lines.draw(0, 0);
	}

	@Override
	public void cleanup() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void init() {
		window = new Window(800, 800, "Lissajous Curve", Window.WINDOW_DEBUGMODE); //Window.WINDOW_TRANSPARENT | Window.WINDOW_BORDERLESS | Window.WINDOW_MULTISAMPLE_X8);
		window.clearColor(new vec4(0.2f, 0.2f, 0.2f, 1.0f));
		window.setSwapInterval(0);
		window.setCurrentApp(this);
		window.addButton(new Button(1.0f - 0.03f, -1.0f, 0.03f, 0.03f, Colors.RED, 1, new Callback() {
			@Override
			public void callback() {
				gameloop.stop();
			}
		}).setHide(true, 0.2f));
		window.addButton(new Button(1.0f - 0.07f, -1.0f, 0.03f, 0.03f, Colors.YELLOW, 1, new Callback() {
			@Override
			public void callback() {
				window.hideToTray();
			}
		}).setHide(true, 0.2f));
		
		//window.setLineWidth(2.0f);
		renderer = new Renderer();
		single_shader = Shader.loadFromSources("/texture-single.vert.glsl", "/texture-single.frag.glsl", true);
		multi_shader = Shader.multiTextureShader();
		mat4 pr_matrix = new mat4().ortho(-1.0f, 1.0f, 1.0f, -1.0f);
		single_shader.setUniform1i("usetex", 1);
		single_shader.setUniformMat4("pr_matrix", pr_matrix);
		multi_shader.setUniform1i("usetex", 1);
		multi_shader.setUniformMat4("pr_matrix", pr_matrix);
		
		glyphs = GlyphTexture.loadFont(new Font("arial", Font.PLAIN, 64));
		TextLabelTexture.initialize(window, glyphs, single_shader);
		text = TextLabelTexture.bakeToTexture("Test");
		single_shader.enable();
		
		curves = new Curve[w][h];
		for (int x = 0; x < w; x++) {
			for (int y = 0; y < h; y++) {
				curves[x][y] = new Curve(
						//new vec2(Maths.map(x, 0.0f, w - 1, -0.8f, 0.8f), Maths.map(y, 0.0f, h - 1, -0.8f, 0.8f)), 
						new vec2(0.0f, 0.0f), 
						0.75f / w,
						new vec2(x + 1, y + 1).mult(1.0f)
				);
			}
		}
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
		if (action == Keys.PRESS && button == Keys.MOUSE_BUTTON_LEFT) {
			holding = true;
			windowPosBeforeHold = window.getWindowPos();
			cursorPosBeforeHold = window.getCursor();
		}
		if (action == Keys.RELEASE && button == Keys.MOUSE_BUTTON_LEFT) {
			holding = false;
		}
	}

	@Override
	public void mousePos(float x, float y) {
		if (holding) {
			vec2 cursormove = cursorPosBeforeHold.mult(-1.0f, -1.0f).add(x, y);
			windowPosBeforeHold = windowPosBeforeHold.add(cursormove.mult(window.getWidth() / 2.0f, window.getHeight() / 2.0f));
			window.setWindowPos(windowPosBeforeHold);
		}

		
	}

	@Override
	public void scroll(float x_delta, float y_delta) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void charCallback(char character) {
		// TODO Auto-generated method stub
		
	}
	
	
	

	public static void main(String[] args) {
		new Thread(new GameLoop(144, new Main()).enableAutoManage()).start();
	}
	
}
