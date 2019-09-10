package main;

import java.awt.Font;
import java.io.File;
import java.io.IOException;
import java.nio.IntBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.GL42;
import org.lwjgl.opengl.GL43;

import graphics.GlyphTexture;
import graphics.Renderer;
import graphics.Shader;
import graphics.Shader.ShaderException;
import graphics.TextLabelTexture;
import graphics.TextLabelTexture.alignment;
import graphics.Texture;
import graphics.Window;
import graphics.primitives.Quad;
import utility.Application;
import utility.Colors;
import utility.DataIO;
import utility.Debug.DebugSlider;
import utility.Keys;
import utility.mat4;
import utility.vec2;
import utility.vec3;

public class Main extends Application {

	final int res = 600;
	Shader normal;
	Shader compute;
	Texture texture;
	Texture floor_texture;
	Camera camera;
	int workGroupSizeX;
	int workGroupSizeY;
	float t = 0.0f;
	float time = 0.0f;
	Renderer quad_renderer;
	
	boolean can_draw; // no errors in shaders
	
	DebugSlider dsShader0;
	DebugSlider dsShader1;
	DebugSlider dsShader2;
	DebugSlider dsShader3;
	DebugSlider dsShader4;
	DebugSlider ds2;
	DebugSlider ds3;
	DebugSlider ds4;
	DebugSlider ds5;
	
	int recompileTimer = 0;
	
	TextLabelTexture fps;
	
	@Override
	public void update() {
		t = ds2.getSliderValue();
		time += 0.01f;

		recompileTimer++;
		if (recompileTimer > gameloop.getUps()) {
			// compileNewShader();
			recompileTimer = 0;
		}
		
		if (can_draw) {
			compute.bind();
			camera.setLookAt(new Vector3f((float)Math.cos(t) * ds3.getSliderValue(), ds4.getSliderValue(), (float)Math.sin(t) * ds3.getSliderValue()), new Vector3f(0.0f, 0.5f, 0.0f), new Vector3f(0.0f, -1.0f, 0.0f));
			compute.setUniform3f("eye", new vec3(camera.getPosition().x, camera.getPosition().y, camera.getPosition().z));
			Vector3f eyeRay = new Vector3f();
			camera.getEyeRay(-1, -1, eyeRay);
			compute.setUniform3f("ray00", new vec3(eyeRay.x, eyeRay.y, eyeRay.z));
			camera.getEyeRay(-1, 1, eyeRay);
			compute.setUniform3f("ray01", new vec3(eyeRay.x, eyeRay.y, eyeRay.z));
			camera.getEyeRay(1, -1, eyeRay);
			compute.setUniform3f("ray10", new vec3(eyeRay.x, eyeRay.y, eyeRay.z));
			camera.getEyeRay(1, 1, eyeRay);
			compute.setUniform3f("ray11", new vec3(eyeRay.x, eyeRay.y, eyeRay.z));
			compute.setUniform1f("slider0", dsShader0.getSliderValue());
			compute.setUniform1f("slider1", dsShader1.getSliderValue());
			compute.setUniform1f("slider2", dsShader2.getSliderValue());
			compute.setUniform1f("slider3", dsShader3.getSliderValue());			
			compute.setUniform1f("slider4", dsShader4.getSliderValue());			
			compute.setUniform1f("time", time);
		}
		
	}

	@Override
	public void render(float preupdate_scale) {
		window.clear();
		window.input();
		
		if (can_draw) {
			compute.bind();

			floor_texture.bind();
			GL43.glBindImageTexture(0, texture.getId(), 0, false, 0, GL15.GL_WRITE_ONLY, GL30.GL_RGBA32F);
			GL43.glDispatchCompute(res / workGroupSizeX, res / workGroupSizeY, 1);
			GL43.glBindImageTexture(0, 0, 0, false, 0, GL15.GL_READ_WRITE, GL30.GL_RGBA32F);
			GL42.glMemoryBarrier(GL42.GL_SHADER_IMAGE_ACCESS_BARRIER_BIT);
		}
		

		
		//Drawing
		normal.bind();
		texture.bind();

		quad_renderer.clear();
		quad_renderer.submit(new Quad(new vec2(-1.0f), new vec2(2.0f), 0, Colors.WHITE));
		quad_renderer.draw();

		fps.rebake("FPS: " + gameloop.getFps());
		fps.submit(new vec2(0.9f), new vec2(0.1f), alignment.BOTTOM_RIGHT);
		TextLabelTexture.drawQueue(false);
		
		window.update();
		if (window.shouldClose()) gameloop.stop();
	}

	@Override
	public void cleanup() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void init() {
		window = new Window(res, res, "Ray Tracer", this, Window.WINDOW_MULTISAMPLE_X8);
		window.setSwapInterval(0);
		
		texture = Texture.rgba32f(res, res);
		floor_texture = Texture.loadTextureSingle("/main/gravel.png");

		compileNewShader();
		
		camera = new Camera();
		camera.setFrustumPerspective(60.0f, res / res, 1f, 2f);
		camera.setLookAt(new Vector3f(3.0f, 2.0f, 7.0f), new Vector3f(0.0f, 1.5f, 0.0f), new Vector3f(0.0f, -1.0f, 0.0f));
		
		normal = Shader.singleTextureShader();
		normal.setUniformMat4("pr_matrix", new mat4().ortho(-1.0f, 1.0f, 1.0f, -1.0f));
		quad_renderer = new Renderer();
		
		
		//Slider
		dsShader0 = new DebugSlider(0.0f, 10.0f, 1.0f, "Intensity");
		dsShader1 = new DebugSlider(-5.0f, 5.0f, 0.0f, "Light source x");
		dsShader2 = new DebugSlider(-5.0f, 5.0f, 0.0f, "Light source z");
		dsShader3 = new DebugSlider(-1.0f, 10.0f, 0.0f, "Light source z");
		dsShader4 = new DebugSlider(-1.0f, 10.0f, 1.0f, "HDR");
		ds2 = new DebugSlider(-(float)Math.PI, (float)Math.PI, 0.2f, "Rotaion");
		ds3 = new DebugSlider(0.0f, 20.0f, 8.0f, "Distance");
		ds4 = new DebugSlider(-5.0f, 10.0f, 2.0f, "Height");
		ds5 = new DebugSlider(0.0f, 1000.0f, 0.0f, "Shader Recompile");
		DebugSlider.complete();
		
		TextLabelTexture.initialize(window, GlyphTexture.loadFont(new Font("arial", Font.BOLD, 32)));
		fps = TextLabelTexture.bakeToTexture("FPS: 0");
	}

	@Override
	public void resize(int width, int height) {
		// TODO Auto-generated method stub
		
	}
	
	public void compileNewShader() {
		can_draw = false;
		try {
			String source = null;
			try {
				source = DataIO.readTextFile(new File("computeShader.glsl"));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			//compute = Shader.loadFromSources("/main/computeShader.glsl", true);
			compute = Shader.loadFromSources(source, false);
			IntBuffer workGroupSize = BufferUtils.createIntBuffer(3);
			GL20.glGetProgramiv(compute.getProgram(), GL43.GL_COMPUTE_WORK_GROUP_SIZE, workGroupSize);
			workGroupSizeX = workGroupSize.get(0);
			workGroupSizeY = workGroupSize.get(1);
			can_draw = true;
			return;
		} catch (ShaderException e) {
			e.printStackTrace();
			can_draw = false;
			return;
		}
	}

	@Override
	public void keyPress(int key, int action) {
		if (key == Keys.KEY_SPACE && action == Keys.PRESS) {
			compileNewShader();
		}
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


	// Main method
	public static void main(String argv[]) {
		new Main().start(60);
	}
	
}
