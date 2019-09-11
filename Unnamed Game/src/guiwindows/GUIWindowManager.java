package guiwindows;

import applications.GUIWindow;
import graphics.TextLabelTexture;
import logic.ShaderManager;

public class GUIWindowManager {
	
	public static GUIWindow settings;
	private static float aspect;
	
	
	public static void initialize() {
		settings = new Settings();
	}
	
	public static void submit() {
		ShaderManager.single_texture_shader.setUniform1i("usetex", 0);
		settings.clampPosition(aspect);
		settings.draw();
		ShaderManager.single_texture_shader.setUniform1i("usetex", 1);
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
	
	public static void resize(int width, int height) {
		if (height == 0) height = 1;
		aspect = (float)width / (float)height;
	}

}
