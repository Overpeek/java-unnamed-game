import java.awt.Font;

import graphics.GlyphTexture;
import graphics.Renderer;
import graphics.Shader;
import graphics.TextLabelTexture;
import graphics.Texture;
import graphics.Window;
import utility.Application;
import utility.Colors;
import utility.Keys;
import utility.Logger;
import utility.mat4;
import utility.vec2;
import utility.vec3;
import utility.vec4;

public class RayMarching extends Application {
	
	
	

	// pos (x, y, z) radius (w)
	final vec4 object = new vec4(0.5f, 0.3f, 0.0f, 0.3f);

	// pos (x, y) looking at (z)
	final vec3 light = new vec3(0.0f, -2.0f, 0.0f);
	final vec3 camera = new vec3(0.0f, 0.0f, 0.5f);
	final float cameraFov = (float) (Math.PI / 2.0f);
	float cameraLookingX = 0.0f;
	float cameraLookingY = 0.0f;
	float power = 1.0f;
	float iteratios = 16.0f;
	float time = 0.0f;

	Renderer renderer;
	Shader raymarch_shader;
	Shader normal_shader;
	TextLabelTexture fps_text_label;
	Texture skybox;

	@Override
	public void update() {
		// TODO Auto-generated method stub
		time += 0.01f;
		float speed = 1.0f;
		if (window.key(Keys.KEY_RIGHT_SHIFT)) speed *= 3.0f;
		if (window.key(Keys.KEY_LEFT_CONTROL)) speed /= 10.0f;
		if (window.key(Keys.KEY_W)) { camera.z -= 0.05f * Math.cos(cameraLookingX) * speed; camera.x -= 0.05f * Math.sin(cameraLookingX) * speed; }
		if (window.key(Keys.KEY_S)) { camera.z += 0.05f * Math.cos(cameraLookingX) * speed; camera.x += 0.05f * Math.sin(cameraLookingX) * speed; }
		if (window.key(Keys.KEY_A)) { camera.x += 0.05f * Math.cos(cameraLookingX) * speed; camera.z -= 0.05f * Math.sin(cameraLookingX) * speed; }
		if (window.key(Keys.KEY_D)) { camera.x -= 0.05f * Math.cos(cameraLookingX) * speed; camera.z += 0.05f * Math.sin(cameraLookingX) * speed; }
		if (window.key(Keys.KEY_SPACE)) camera.y -= 0.05f * speed;
		if (window.key(Keys.KEY_LEFT_SHIFT)) camera.y += 0.05f * speed;
		if (window.key(Keys.KEY_UP)) power -= 0.01f * speed;
		if (window.key(Keys.KEY_DOWN)) power += 0.01f * speed;
		if (window.key(Keys.KEY_RIGHT)) iteratios -= 0.1f;
		if (window.key(Keys.KEY_LEFT)) iteratios += 0.1f;
		
		light.x = (float) Math.cos(time);
		light.z = (float) Math.sin(time);
		
		raymarch_shader.setUniform3f("camera", camera); 
		raymarch_shader.setUniform3f("light", light); 
		mat4 vw = new mat4().rotateY(cameraLookingX).rotateX(cameraLookingY);
		raymarch_shader.setUniformMat4("vw_matrix", vw);
		raymarch_shader.setUniform1f("power", power);
		raymarch_shader.setUniform1f("time", time);
		raymarch_shader.setUniform1i("iterations", Math.round(iteratios));
		
		Logger.info("Power: " + power + ", Iterations: " + Math.round(iteratios));
	}

	@Override
	public void render(float preupdate_scale) {
		// TODO Auto-generated method stub
		raymarch_shader.enable();
		renderer.quads.submit(new vec3(-1.0f, -1.0f, 0.0f), new vec2(2.0f, 2.0f), 0, Colors.BLACK);
		renderer.quads.draw(skybox);

		normal_shader.enable();
		fps_text_label.rebake("FPS: " + gameloop.getFps());
		fps_text_label.queueDraw(new vec3(-1.0f, -1.0f, 0.0f), new vec2(0.1f, 0.1f));
	}

	@Override
	public void cleanup() {
		// TODO Auto-generated method stub

	}

	@Override
	public void init() {
		window = new Window(800, 800, "Ray Marching - Eemeli Lehtonen", Window.WINDOW_DEBUGMODE);
		window.setCurrentApp(this);
		renderer = new Renderer();
		window.clearColor(0.0f, 0.0f, 0.0f, 1.0f);
		mat4 pr = new mat4().ortho(-1.0f, 1.0f, 1.0f, -1.0f);
		raymarch_shader = Shader.loadFromSources("/res/vert.glsl", "/res/frag.glsl", true);
		raymarch_shader.setUniformMat4("pr_matrix", pr);
		raymarch_shader.disable();
		
		normal_shader = Shader.loadFromSources("/res/texture-single.vert.glsl", "/res/texture-single.frag.glsl", true);
		normal_shader.setUniformMat4("pr_matrix", pr);
		GlyphTexture glyphs = GlyphTexture.loadFont(new Font("arial", Font.BOLD, 64));
		TextLabelTexture.initialize(window, glyphs, normal_shader);
		fps_text_label = TextLabelTexture.bakeToTexture("FPS: 0");
		String sources[] = {
			"/res/right.png",
		    "/res/left.png",
		    "/res/top.png",
		    "/res/bottom.png",
		    "/res/front.png",
		    "/res/back.png"
		};
		skybox = Texture.loadCubeMap(128, sources);
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
		float sens = 1.0f;
		x *= sens;
		y *= sens;
		cameraLookingX += x;
		cameraLookingY += y;
		
		window.setCursor(400.0f, 400.0f);
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
