package logic;

import java.util.ArrayList;

import graphics.Renderer;
import graphics.Shader;
import graphics.TextLabelTexture;
import graphics.Texture;
import graphics.Window;
import graphics.primitives.Quad;
import utility.Callback;
import utility.Colors;
import utility.Keys;
import utility.mat4;
import utility.vec2;
import utility.vec4;

public class MainMenu {
	
	protected static class MenuButton {
		private vec2 size;
		private vec4 color;
		
		private vec2 posCurrent;
		private vec2 posA;
		private vec2 posB;
		
		private float t;
		
		private TextLabelTexture text;
		private Callback callback;
		
		public void update(float x, float y, boolean pressed, float time) {
			boolean hover_over = false;
			if (y > posA.y && y < posA.y + size.y) hover_over  = true;
			
			
			if (hover_over) {
				if (pressed) callback.callback();
				
				t += time;
			} else {
				t -= time;
			}
			
			if (t > 1.0f) {
				t = 1.0f;
			} else if (t < 0.0f) {
				t = 0.0f;
			}
			
			posCurrent = vec2.lerp(posA, posB, t);
		}
		
		public void submit(Renderer renderer) {
			renderer.submit(new Quad(posCurrent, size, 0, color));
			text.submitCentered(posCurrent.addLocal(size.mulLocal(0.5f)), new vec2(size.y, size.y));
		}
		
		public MenuButton(float x, float y, String text, Callback callback, vec2 size, vec4 color) {
			final float move = 0.2f;
			
			this.callback = callback;
			this.posA = new vec2(x, y);
			this.posB = new vec2(x + move, y);
			this.size = size;
			this.posCurrent = posA;
			this.color = color;
			
			this.text = TextLabelTexture.bakeToTexture(text);
		}
	}
	
	private Renderer renderer;
	private Shader shader;
	private Shader post_shader;
	
	private ArrayList<MenuButton> buttons;
	private Window window;
	
	public boolean inMenu;
	 
	public MainMenu(Window window, Shader post_shader) {
		this.window = window;
		this.post_shader = post_shader;
		inMenu = true;
		buttons = new ArrayList<MenuButton>();
		buttons.add(new MenuButton(0.0f, 0.0f, "New", new Callback() {
			
			@Override
			public void callback() {
				Main.game.loadWorld("IDK", true);
				inMenu = false;
			}
		}, new vec2(0.8f, 0.2f), new vec4(0.03f, 0.03f, 0.03f, 1.0f)));
		buttons.add(new MenuButton(0.0f, 0.25f, "Load", new Callback() {
			
			@Override
			public void callback() {
				Main.game.loadWorld("IDK", false);
				inMenu = false;
			}
		}, new vec2(0.8f, 0.2f), new vec4(0.03f, 0.03f, 0.03f, 1.0f)));
		buttons.add(new MenuButton(0.0f, 0.5f, "Quit", new Callback() {
			
			@Override
			public void callback() {
				Main.game.gameloop.stop();
				inMenu = true;
			}
		}, new vec2(0.8f, 0.2f), new vec4(0.03f, 0.03f, 0.03f, 1.0f)));
		
		shader = Shader.singleTextureShader();
		renderer = new Renderer();
		mat4 pr_matrix = new mat4().ortho(0.0f, 2 * window.getAspect(), 1.0f, -1.0f);
		shader.setUniformMat4("pr_matrix", pr_matrix);
		shader.setUniform1i("usetex", 0);
	}

	public void update(float UPS) {
		for (MenuButton b : buttons) {
			b.update(window.getCursorFast().x, window.getCursorFast().y, window.button(Keys.MOUSE_BUTTON_LEFT), 5.0f / UPS);		
		}
	}

	public void render(float preupdate_scale) {

		renderer.submit(new Quad(new vec2(-1.0f, -1.0f), new vec2(2.0f, 2.0f), 0, Colors.WHITE));
		post_shader.setUniform1i("unif_effect", 7);
		renderer.draw();
		renderer.clear();

		for (MenuButton b : buttons) {
			b.submit(renderer);
		}
		
		Texture.unbind();
		shader.setUniform1i("usetex", 0); 
		renderer.draw();
		renderer.clear();
		
		shader.setUniform1i("usetex", 1);
		TextLabelTexture.drawQueue(false);
	}

	public void keyPress(int key, int action) {
		if(key == Keys.KEY_ESCAPE) {
			inMenu = false;
		}
	}

	public void buttonPress(int button, int action) {
		
	}

}
