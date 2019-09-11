package stages;

import java.util.ArrayList;

import graphics.Renderer;
import graphics.TextLabelTexture;
import graphics.TextLabelTexture.alignment;
import graphics.Texture;
import graphics.Window;
import graphics.primitives.Quad;
import guiwindows.GUIWindowManager;
import logic.Main;
import logic.ShaderManager;
import settings.CompiledSettings;
import settings.KeyBindings;
import settings.KeyBindings.KeyBind;
import stages.GameStateManager.GameStates;
import utility.Callback;
import utility.Colors;
import utility.Keys;
import utility.Logger;
import utility.vec2;
import utility.vec4;

public class MainMenu extends State {
	
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
			text.submit(posCurrent.addLocal(size.mulLocal(0.5f)), new vec2(size.y, size.y), alignment.CENTER_CENTER);
		}
		
		public MenuButton(float x, float y, String text, Callback callback, vec2 size, vec4 color) {
			final float move = 0.1f;
			
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
	private Texture name_splash;
	
	private ArrayList<MenuButton> buttons;
	private Window window;
	private TextLabelTexture version_label;
	
	public boolean inMenu;

	
	
	public MainMenu(Window window) {
		this.window = window;
		inMenu = true;
		
		// Buttons
		buttons = new ArrayList<MenuButton>();
		buttons.add(new MenuButton(0.0f, 0.0f, "New", new Callback() {
			
			@Override
			public void callback() {
				GameStateManager.setState(GameStates.LOADING);
				((Loader)GameStates.LOADING.state).createMap("IDK", CompiledSettings.INITIAL_RANDOM_SEED, GameStates.WORLD);
				GameStateManager.init();
				inMenu = false;
			}
		}, new vec2(0.8f, 0.2f), new vec4(0.03f, 0.03f, 0.03f, 1.0f)));
		buttons.add(new MenuButton(0.0f, 0.25f, "Load", new Callback() {
			
			@Override
			public void callback() {
				GameStateManager.setState(GameStates.LOADING);
				((Loader)GameStates.LOADING.state).loadMap("IDK", GameStates.WORLD);
				GameStateManager.init();
				inMenu = false;
			}
		}, new vec2(0.8f, 0.2f), new vec4(0.03f, 0.03f, 0.03f, 1.0f)));
		buttons.add(new MenuButton(0.0f, 0.5f, "Settings", new Callback() {
			
			@Override
			public void callback() {
				GUIWindowManager.settings.hidden = false;
				inMenu = true;
			}
		}, new vec2(0.8f, 0.2f), new vec4(0.03f, 0.03f, 0.03f, 1.0f)));
		buttons.add(new MenuButton(0.0f, 0.75f, "Quit", new Callback() {
			
			@Override
			public void callback() {
				Main.stop();
				inMenu = true;
			}
		}, new vec2(0.8f, 0.2f), new vec4(0.03f, 0.03f, 0.03f, 1.0f)));
		
		// Keys
		KeyBind kb = new KeyBind(new Callback() {
			@Override
			public void callback() {
				Logger.debug("PRESS");
			}
		}, new Callback() {
			@Override
			public void callback() {
				Logger.debug("REPEAT");
			}
		}, new Callback() {
			@Override
			public void callback() {
				Logger.debug("RELEASE");
			}
		});
		kb.setPrimary(Keys.KEY_F1, -1, -1, -1);
		KeyBindings.addNewBindable(kb);
		
		// textures
		name_splash = Texture.loadTextureSingle("res/texture/UNMND.png");
		
		renderer = new Renderer();
		version_label = TextLabelTexture.bakeToTexture(CompiledSettings.MENU_STR);
	}

	@Override
	public void update() {
		if (GUIWindowManager.opened()) {
			for (MenuButton b : buttons) {
				b.update(0.0f, 0.0f, false, 5.0f / CompiledSettings.UPDATES_PER_SECOND);		
			}
		} else {
			for (MenuButton b : buttons) {
				b.update(window.getCursorFast().x, window.getCursorFast().y, window.button(Keys.MOUSE_BUTTON_LEFT), 5.0f / CompiledSettings.UPDATES_PER_SECOND);		
			}
		}
	}

	@Override
	public void render(float preupdate_scale) {
		renderer.clear();

		// Space background
		renderer.submit(new Quad(new vec2(-1.0f, -1.0f), new vec2(2.0f, 2.0f), 0, Colors.WHITE));
		ShaderManager.post_processing_shader.setUniform1i("unif_effect", 7);
		renderer.draw();
		renderer.clear();

		for (MenuButton b : buttons) {
			b.submit(renderer);
		}
		
		Texture.unbind();
		ShaderManager.left_align_single_texture_shader.setUniform1i("usetex", 0); 
		renderer.draw();
		renderer.clear();
		
		// Version label
		version_label.submit(new vec2(window.getAspect() * 2.0f, 1.0f), new vec2(0.05f, 0.05f), alignment.BOTTOM_RIGHT);
		
		// Game name texture
		final float scale = 1.25f;
		name_splash.bind();
		ShaderManager.single_texture_shader.bind();
		ShaderManager.single_texture_shader.setUniform1i("usetex", 1);
		renderer.submit(new Quad(new vec2(-scale * 0.5f, -0.7f), new vec2(scale, scale * 0.25f), 0, Colors.WHITE));
		renderer.draw();
		renderer.clear();
		
		// Text
		ShaderManager.left_align_single_texture_shader.setUniform1i("usetex", 1);
		TextLabelTexture.drawQueue(false);
		
		GUIWindowManager.submit();
	}

	@Override
	public void cleanup() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void init() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void resize(int width, int height) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void keyPress(int key, int action) {
		if(key == Keys.KEY_ESCAPE) {
			inMenu = false;
		}
	}

	@Override
	public void buttonPress(int button, int action) {
		GUIWindowManager.input(window.getCursorFast().x, window.getCursorFast().y, button, action);
	}

	@Override
	public void mousePos(float x, float y) {
		GUIWindowManager.update(x, y);
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
