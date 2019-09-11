package guiwindows;

import applications.GUIWindow;
import graphics.Renderer;
import graphics.TextLabelTexture;
import graphics.primitives.Primitive.Primitives;
import utility.vec2;

public class Settings extends GUIWindow {

	public Settings() {
		this.position = new vec2(-0.5f, -0.5f);
		this.size = new vec2(1.0f);
		this.name = "Settings";

		this.label = TextLabelTexture.bakeToTexture(name);
		this.renderer = new Renderer(Primitives.Quad, 10);
	}

	@Override
	public void draw() {
		if (!hidden) drawFrame();
	}

	@Override
	public void cursor(float m_x, float m_y, int button, int m_action) {
		buttons(m_x, m_y, button, m_action);
	}

}
