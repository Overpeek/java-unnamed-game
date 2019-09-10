package applications;

import graphics.Renderer;
import graphics.TextLabelTexture;
import graphics.TextLabelTexture.alignment;
import graphics.primitives.Quad;
import utility.Colors;
import utility.Keys;
import utility.Maths;
import utility.vec2;

public abstract class GUIWindow {
	
	private final static float border = 0.005f;
	private final static float bar_height = 0.08f;
	
	protected vec2 position;
	protected vec2 size;
	protected Renderer renderer;
	protected TextLabelTexture label;
	public String name;
	public boolean hidden = true;
	
	private boolean dragging;
	private vec2 drag_start_offset;
	
	public abstract void draw(float window_aspect);
	public abstract void cursor(float m_x, float m_y, int button, int m_action);
	
	protected void buttons(float m_x, float m_y, int button, int m_action) {
		if (m_action == Keys.PRESS && button == Keys.MOUSE_BUTTON_LEFT) {
			// Hide button
			float hide_x = m_x - (position.x-(-border-size.x+bar_height));
			float hide_y = m_y - (position.y-bar_height);
			if (hide_x > 0.0f && hide_x < bar_height && hide_y > 0.0f && hide_y < bar_height) {
				hidden = true;
			}
			
			// Window moving
			float move_x = m_x - (position.x);
			float move_y = m_y - (position.y-bar_height);
			if (move_x > 0.0f && move_x < size.x && move_y > 0.0f && move_y < bar_height) {
				dragging = true;
				drag_start_offset = position.subLocal(m_x, m_y);
			}
		} else if (m_action == Keys.RELEASE && button == Keys.MOUSE_BUTTON_LEFT) {
			dragging = false;
		}
		
		if (dragging) {
			position = drag_start_offset.addLocal(m_x, m_y);
		}
	}
	
	protected void drawFrame(float window_aspect) {
		
		// Prevent dragging out of window
		position.x = Maths.clamp(position.x, -window_aspect, window_aspect-size.x);
		position.y = Maths.clamp(position.y, -1.0f+bar_height, 1.0f-size.y);
				
		
		renderer.clear();
		// Bar
		renderer.submit(new Quad(position.subLocal(border, bar_height), new vec2(size.x, bar_height).mul(1.0f + border*2.0f), 0, Colors.GREY));
		
		renderer.submit(new Quad(position.subLocal(-border-size.x+bar_height, bar_height), new vec2(bar_height, bar_height).mul(1.0f + border*2.0f), 0, Colors.RED));
		
		// Box
		renderer.submit(new Quad(position.subLocal(new vec2(border*2.0f)), size.addLocal(new vec2(border*4.0f)), 0, Colors.BLACK));
		renderer.submit(new Quad(position.subLocal(new vec2(border)), size.addLocal(new vec2(border*2.0f)), 0, Colors.GREY));
		renderer.submit(new Quad(position, size, 0, Colors.BLACK));
		
		renderer.draw();
		if (label != null) {
			label.submit(position.subLocal(0.0f, bar_height * 0.6f), new vec2(bar_height), alignment.CENTER_LEFT);
		}
			
//		final float border = 0.01f;
		
//		StreamQuadData[] quads = new StreamQuadData[2];
//		quads[0] = new StreamQuadData(new vec3(0.0f).addLocal(new vec3(-border)), size.addLocal(new vec2(border * 2.0f)), 0, new vec4(0.1f, 0.1f, 0.1f, 1.0f));
//		quads[1] = new StreamQuadData(new vec3(0.0f), size, 0, new vec4(0.2f, 0.2f, 0.2f, 1.0f));

//		StreamQuadRenderer.streamQuadRender(quads);
		
	}
	
}
