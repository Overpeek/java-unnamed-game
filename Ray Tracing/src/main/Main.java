package main;

import java.nio.IntBuffer;

import javax.swing.JSlider;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.GL42;
import org.lwjgl.opengl.GL43;

import graphics.Shader;
import graphics.StreamQuadRenderer;
import graphics.StreamQuadRenderer.StreamQuadData;
import graphics.Texture;
import graphics.Window;
import utility.Application;
import utility.Colors;
import utility.Logger;
import utility.vec2;
import utility.vec3;

public class Main extends Application {

	Shader normal;
	Shader compute;
	Texture texture;
	Camera camera;
	int workGroupSizeX;
	int workGroupSizeY;
	float t = 0.0f;
	StreamQuadData quads[] = new StreamQuadData[1];
	
	JSlider slider = new JSlider(JSlider.HORIZONTAL,-100, 100, 0);
	
	@Override
	public void update() {
		t += 0.01f;
		compute.enable();
		camera.setLookAt(new Vector3f((float)Math.cos(t) * 10.0f, 2.0f, (float)Math.sin(t) * 10.0f), new Vector3f(0.0f, 0.5f, 0.0f), new Vector3f(0.0f, -1.0f, 0.0f));
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
		compute.setUniform3f("light", new vec3(0.0f, 10.0f, 0.0f));
	}

	@Override
	public void render(float preupdate_scale) {
		window.clear();
		window.input();
		
		Logger.debug("FPS: " + gameloop.getFps());

		compute.enable();
		
		GL43.glBindImageTexture(0, texture.getId(), 0, false, 0, GL15.GL_WRITE_ONLY, GL30.GL_RGBA32F);
		int worksizeX = 512;//Util.nextPowerOfTwo(600);
		int worksizeY = 512;//Util.nextPowerOfTwo(600);
		GL43.glDispatchCompute(worksizeX / workGroupSizeX, worksizeY / workGroupSizeY, 1);
		GL43.glBindImageTexture(0, 0, 0, false, 0, GL15.GL_READ_WRITE, GL30.GL_RGBA32F);
		GL42.glMemoryBarrier(GL42.GL_SHADER_IMAGE_ACCESS_BARRIER_BIT);
		
		//Drawing
		normal.enable();
		texture.bind();
		StreamQuadRenderer.streamQuadRender(quads);
		
		window.update();
		if (window.shouldClose()) gameloop.stop();
	}

	@Override
	public void cleanup() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void init() {
		window = new Window(512, 512, "Ray Tracer", Window.WINDOW_MULTISAMPLE_X8);
		
		texture = Texture.rgba32f(, 512);
		
		compute = Shader.loadFromSources("/main/computeShader.glsl", true);
		IntBuffer workGroupSize = BufferUtils.createIntBuffer(3);
		GL20.glGetProgramiv(compute.getProgram(), GL43.GL_COMPUTE_WORK_GROUP_SIZE, workGroupSize);
		workGroupSizeX = workGroupSize.get(0);
		workGroupSizeY = workGroupSize.get(1);
		
		camera = new Camera();
		camera.setFrustumPerspective(60.0f, 512.0f / 512.0f, 1f, 2f);
		camera.setLookAt(new Vector3f(3.0f, 2.0f, 7.0f), new Vector3f(0.0f, 0.5f, 0.0f), new Vector3f(0.0f, -1.0f, 0.0f));
		
		normal = Shader.singleTextureShader();
		quads[0] = new StreamQuadData(new vec3(-1.0f, -1.0f, 0.0f), new vec2(2.0f), 0, Colors.WHITE);
		

		slider.addChangeListener(this);
		
		//Turn on labels at major tick marks.
		framesPerSecond.setMajorTickSpacing(10);
		framesPerSecond.setMinorTickSpacing(1);
		framesPerSecond.setPaintTicks(true);
		framesPerSecond.setPaintLabels(true);
	}
	
	public class Listener extends ChangeListener {
		public void stateChanged(ChangeEvent e) {
		    JSlider source = (JSlider)e.getSource();
		    if (!source.getValueIsAdjusting()) {
		        int fps = (int)source.getValue();
		        if (fps == 0) {
		            if (!frozen) stopAnimation();
		        } else {
		            delay = 1000 / fps;
		            timer.setDelay(delay);
		            timer.setInitialDelay(delay * 10);
		            if (frozen) startAnimation();
		        }
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
