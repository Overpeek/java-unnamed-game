import java.io.IOException;
import java.util.ArrayList;

import graphics.Renderer;
import graphics.Renderer.Type;
import graphics.Shader;
import graphics.Window;
import graphics.primitives.Primitive.Primitives;
import utility.Application;
import utility.Keys;
import utility.Logger;
import utility.Maths;
import utility.vec2;
import utility.physics.Physics;
import utility.physics.PhysicsBox;
import utility.physics.PhysicsCircle;
import utility.physics.PhysicsObject;
import utility.physics.PhysicsTriangle;

class Main extends Application {
	
	private Renderer quad_renderer;
	private Renderer circle_renderer;
	private Renderer triangle_renderer;
	private Shader shader;
	
	private Physics physics;
	private ArrayList<PhysicsObject> physicsObjects;
	
	
	
	public static void main(String args[]) throws IOException {
		new Main().start(144);
	}
	
	private void worldSetup() {
		physicsObjects = new ArrayList<PhysicsObject>();
		physics = new Physics(new vec2(0.0f, 9.81f));
		
		physicsObjects.add(new PhysicsBox(new vec2(6.0f, 6.0f), new vec2(10.0f, 0.5f), true, -0.2f, physics));
		physicsObjects.add(new PhysicsBox(new vec2(-6.0f, 6.0f), new vec2(10.0f, 0.5f), true, 0.2f, physics));
		physicsObjects.add(new PhysicsBox(new vec2(10.0f, 0.0f), new vec2(0.5f, 10.0f), true, 0.0f, physics));
		physicsObjects.add(new PhysicsBox(new vec2(-10.0f, 0.0f), new vec2(0.5f, 10.0f), true, 0.0f, physics));
		
		physicsObjects.add(new PhysicsCircle(new vec2(0.0f, 0.0f), 1.2f, false, physics));

		//physicsObjects.add(new PhysicsBox(new vec2(0.0f, 8.0f), new vec2(16.0f, 1.0f), true, 0.0f, physics));
	}

	@Override
	public void update() {
		for (PhysicsObject po : physicsObjects) {
			
			// wrap around box (Y)
			if (po.getPosition().y > 12.0f) {
				po.setPosition(new vec2(po.getPosition().x, -12.0f));
			} else if (po.getPosition().y < -12.0f) {
				po.setPosition(new vec2(po.getPosition().x, 12.0f));
			}
			
			// mirror edges (X)
			if (po.getPosition().x > 12.0f || po.getPosition().x < -12.0f) {
				po.setLinearVelocity(po.getLinearVelocity().mul(-1.0f, 1.0f));
			}
		}
		
		Logger.debug("FPS: " + gameloop.getFps());
		physics.step(144.0f, 32);
	}

	float t = 0.0f;
	@Override
	public void render(float preupdate_scale) {
		shader.bind();
		
		quad_renderer.clear();
		circle_renderer.clear();
		triangle_renderer.clear();
		for (PhysicsObject po : physicsObjects) {
			if (po instanceof PhysicsBox){
				PhysicsBox asB = (PhysicsBox) po;
				asB.submit(quad_renderer);
			} else if (po instanceof PhysicsCircle){
				PhysicsCircle asB = (PhysicsCircle) po;
				asB.submit(circle_renderer);
			} else if (po instanceof PhysicsTriangle){
				PhysicsTriangle asB = (PhysicsTriangle) po;
				asB.submit(triangle_renderer);
			}
		}
		triangle_renderer.draw();
		circle_renderer.draw();
		quad_renderer.draw();
	}

	@Override
	public void cleanup() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void init() {
		final int res = 800;
		window = new Window(res, res, "Engine Test", this, Window.WINDOW_MULTISAMPLE_X2 | Window.WINDOW_TRANSPARENT | Window.POLYGON_LINE);
		window.setSwapInterval(0);
		window.clearColor(0.0f, 0.0f, 0.0f, 0.8f);
		gameloop.enableAutoManage();
		quad_renderer = new Renderer(Primitives.Quad, Type.Dynamic, Type.Static);
		circle_renderer = new Renderer(Primitives.Circle, Type.Dynamic, Type.Static);
		triangle_renderer = new Renderer(Primitives.Triangle, Type.Dynamic, Type.Static);

		shader = Shader.singleTextureShader();
		shader.setUniform1i("usetex", 0);
		
		// jBox2D
		worldSetup();
	}

	@Override
	public void resize(int width, int height) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void keyPress(int key, int action) {
		
		if (key == Keys.KEY_SPACE && action != Keys.RELEASE) {

			PhysicsBox b = new PhysicsBox(new vec2(Maths.random(-5.0f, 5.0f), -10.0f), new vec2(Maths.random(0.2f, 1.0f)), false, 0.0f, physics);
			b.setRestitution(0.1f);
			PhysicsCircle c = new PhysicsCircle(new vec2(Maths.random(-5.0f, 5.0f), -10.0f), Maths.random(0.2f, 1.0f), false, physics);
			c.setRestitution(0.1f);
			PhysicsTriangle t = new PhysicsTriangle(new vec2(Maths.random(-5.0f, 5.0f), -10.0f), new vec2(Maths.random(0.2f, 1.0f)), false, 0.0f, physics);
			t.setRestitution(0.1f);

			physicsObjects.add(b);
			physicsObjects.add(c);
			physicsObjects.add(t);
			
		}
		
		if (key == Keys.KEY_R && action != Keys.RELEASE) {

			worldSetup();
			
		}
		
	}

	@Override
	public void buttonPress(int button, int action) {
		physics.mouseButton(button, action);
	}

	@Override
	public void mousePos(float x, float y) {
		physics.mouseMove(x, y);
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