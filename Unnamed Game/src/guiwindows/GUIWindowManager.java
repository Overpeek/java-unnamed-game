package guiwindows;

import applications.GUIWindow;
import graphics.Shader;
import graphics.TextLabelTexture;

public class GUIWindowManager {
	
	public static GUIWindow settings;
	public static Shader shader;
	
	
	public static void initialize() {
		settings = new Settings();
		shader = Shader.singleTextureShader();
	}
	
	public static void submit(float window_aspect) {
		shader.setUniform1i("usetex", 0);
		settings.draw(window_aspect);
		shader.setUniform1i("usetex", 1);
		TextLabelTexture.drawQueue(false);
	}
	
	public static void input(float m_x, float m_y, int button, int m_action) {
		settings.cursor(m_x, m_y, button, m_action);
	}
	
	public static void update(float m_x, float m_y) {
		settings.cursor(m_x, m_y, -1, -1);
	}
	
	public static boolean opened() {
		return !settings.hidden;
	}

}
