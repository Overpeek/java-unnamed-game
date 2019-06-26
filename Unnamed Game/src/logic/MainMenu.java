package logic;

import graphics.GlyphTexture;
import graphics.Renderer;
import graphics.Shader;
import graphics.TextLabelTexture;
import graphics.VertexData;
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
	
	private static class Obj {
		float x, y, z;
		float x_vel, y_vel, z_vel;
		float r, g, b, a;
	}
	
	static private GameLoop loop;
	static private Renderer renderer;
	static private Shader multi_shader;
	static private Shader single_shader;
	static private Shader point_shader;
	static private TextLabelTexture label;
	 
	static private final float UPS = 5;
	static private Obj objects[] = new Obj[1000];



	public static void init(Renderer gui_renderer, Shader _multi_shader, Shader _single_shader, Shader _point_shader, Shader _post_shader, GlyphTexture glyphs) {
		renderer = gui_renderer;
		multi_shader = _multi_shader;
		single_shader = _single_shader;
		point_shader = _point_shader;
	
		for (int i = 0; i < 1000; i++)
		{
			objects[i] = new Obj();
			
			objects[i].x = Maths.random(-1.0f, 1.0f);
			objects[i].y = Maths.random(-1.0f, 1.0f);
			objects[i].z = Maths.random(-1.0f, 1.0f);

			objects[i].x_vel = Maths.random(-1.0f, 1.0f);
			objects[i].y_vel = Maths.random(-1.0f, 1.0f);
			objects[i].z_vel = Maths.random(-1.0f, 1.0f);
	
			
			objects[i].r = Maths.random(0.0f, 1.0f);
			objects[i].g = Maths.random(0.0f, 1.0f);
			objects[i].b = Maths.random(0.0f, 1.0f);
			objects[i].a = 1.0f;
		}
	
		point_shader.setUniform1i("ortho", 0);
		
		label = TextLabelTexture.bakeToTexture("$0M$1A$2I$3N $4M$5E$6N$7U");

		Logger.info("Main menu started");
		Application game = new MainMenu();
		loop = new GameLoop((int) UPS, game);
		//game.window = Main.game.getWindow();
		loop.run();
        //(new Thread(loop)).start();
	}



	@Override
	public void update() {
		for (int i = 0; i < 1000; i++)
		{
			objects[i].x += objects[i].x_vel / UPS;
			objects[i].y += objects[i].y_vel / UPS;
			objects[i].z += objects[i].z_vel / UPS;

			if (objects[i].x > 1.0) objects[i].x_vel *= -1;
			if (objects[i].x < -1.0) objects[i].x_vel *= -1;
			if (objects[i].y > 1.0) objects[i].y_vel *= -1;
			if (objects[i].y < -1.0) objects[i].y_vel *= -1;
			if (objects[i].z > 1.0) objects[i].z_vel *= -1;
			if (objects[i].z < -1.0) objects[i].z_vel *= -1;
		}
	}



	float anglex = 0.0f;
	float angley = 0.0f;
	@Override
	public void render(float corrector) {
		if (Main.game.getWindow().shouldClose()) loop.stop();
		Main.game.getWindow().clear();

		float mx = Main.game.getWindow().getCursor().x, my = Main.game.getWindow().getCursor().y;
		//Logger.warn(mx + ", " + my);
		anglex = -mx * 4.0f;
		angley = -my * 4.0f;

		//Set matrices
		mat4 pr_matrix = new mat4().perspectiveRad(90.0f, Main.game.getWindow().getAspect());
		mat4 vw_matrix = new mat4()
				.lookAt((float) Math.cos(anglex), angley, (float) Math.sin(anglex),
			             0.0f, 0.0f, 0.0f,
			             0.0f, -1.0f, 0.0f);
		point_shader.setUniformMat4("pr_matrix", pr_matrix);
		point_shader.setUniformMat4("vw_matrix", vw_matrix);
		single_shader.setUniformMat4("pr_matrix", pr_matrix.mult(vw_matrix));
		multi_shader.setUniformMat4("pr_matrix", pr_matrix.mult(vw_matrix));
		

		for (int i = 0; i < 1000; i++)
		{
			vec3 pos = new vec3(objects[i].x + objects[i].x_vel * corrector / UPS, objects[i].y + objects[i].y_vel * corrector / UPS, objects[i].z + objects[i].z_vel * corrector / UPS);
			vec2 size = new vec2(0.02f);
			vec4 color = new vec4(objects[i].r, objects[i].g, objects[i].b, objects[i].a);

			//if (i != 0 && i != 999)
				//renderer.points.submitVertex(new VertexData(pos, size, 20, color));
			//renderer.points.submitVertex(new VertexData(pos, size, 20, color));
			//m_renderer->lineRenderer->submitVertex(oe::VertexData(pos, size, 20, m_objcol[i]));
			//m_renderer->quadRenderer->submitVertex(oe::VertexData(glm::vec3(pos.x, pos.y, pos.z), glm::vec2(0.0f, 0.0f), 20, m_objcol[i]));
			//m_renderer->quadRenderer->submitVertex(oe::VertexData(glm::vec3(pos.x, pos.y + size.y, pos.z), glm::vec2(0.0f, 1.0f), 20, m_objcol[i]));
			//m_renderer->quadRenderer->submitVertex(oe::VertexData(glm::vec3(pos.x + size.x, pos.y + size.y, pos.z), glm::vec2(1.0f, 1.0f), 20, m_objcol[i]));
			//m_renderer->quadRenderer->submitVertex(oe::VertexData(glm::vec3(pos.x + size.x, pos.y, pos.z), glm::vec2(1.0f, 0.0f), 20, m_objcol[i]));
		}
		point_shader.enable();
		//renderer.points.draw(0, 0);
		renderer.points.clear();
		
		single_shader.setUniform1i("usetex", 1);
		label.queueDrawCentered(new vec3(0.0f, -0.0f, 0.0f), new vec2(0.5f), Colors.WHITE);
		TextLabelTexture.drawQueue();
		//m_renderer->drawToFramebuffer(m_shader, m_point_shader, oe::TextureManager::getTexture(0), true, true);
		//for (int i = 0; i < 8; i++) {
		//	m_post_shader->setUniform1i("unif_effect", 1);
		//	m_renderer->drawFramebufferToFramebuffer(m_post_shader, "unif_texture", false);
		//	m_post_shader->setUniform1i("unif_effect", 2);
		//	m_renderer->drawFramebufferToFramebuffer(m_post_shader, "unif_texture", true);
		//}
		//m_renderer->drawFramebuffer(m_post_shader, "unif_texture", true);

		Main.game.getWindow().update();
		Main.game.getWindow().input();
	}



	@Override
	public void cleanup() {
		single_shader.setUniform1i("usetex", 1);
		point_shader.setUniform1i("usetex", 1);
	}



	@Override
	public void init() {
		Main.game.getWindow().setCurrentApp(this);
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
		// TODO Auto-generated method stub
		
	}



	@Override
	public void scroll(float x_delta, float y_delta) {
		// TODO Auto-generated method stub
		
	}

}
