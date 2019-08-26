package main;

import java.nio.IntBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.GL42;
import org.lwjgl.opengl.GL43;

import graphics.Renderer;
import graphics.Shader;
import graphics.Shader.ShaderException;
import graphics.Texture;
import graphics.Window;
import graphics.primitives.Quad;
import utility.Application;
import utility.Colors;
import utility.Debug.DebugSlider;
import utility.Keys;
import utility.vec2;
import utility.vec3;

public class Main extends Application {

	final int res = 800;
	Shader normal;
	Shader compute;
	Texture texture;
	Texture floor_texture;
	Camera camera;
	int workGroupSizeX;
	int workGroupSizeY;
	float t = 0.0f;
	Renderer quad_renderer;
	
	boolean can_draw; // no errors in shaders
	
	DebugSlider dsShader0;
	DebugSlider dsShader1;
	DebugSlider dsShader2;
	DebugSlider ds2;
	DebugSlider ds3;
	DebugSlider ds4;
	
	@Override
	public void update() {
		t = ds2.getSliderValue();

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
		quad_renderer = new Renderer();
		
		
		//Slider
		dsShader0 = new DebugSlider(0.0f, 10.0f, 1.0f, "Intensity");
		dsShader1 = new DebugSlider(-1.0f, 1.0f, 0.0f, "Light source x");
		dsShader2 = new DebugSlider(0.0f, 20.0f, 3.0f, "Light source y");
		ds2 = new DebugSlider(-(float)Math.PI, (float)Math.PI, 0.2f, "Rotaion");
		ds3 = new DebugSlider(0.0f, 20.0f, 8.0f, "Distance");
		ds4 = new DebugSlider(-5.0f, 10.0f, 2.0f, "Height");
		DebugSlider.complete();
	}

	@Override
	public void resize(int width, int height) {
		// TODO Auto-generated method stub
		
	}
	
	public void compileNewShader() {
		can_draw = false;
		try {
			compute = Shader.loadFromSources("/main/computeShader.glsl", true);
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
