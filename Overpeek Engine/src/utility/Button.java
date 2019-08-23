package utility;

import graphics.Renderer;
import graphics.Window;

public class Button {
	
	private float x, y, w, h;
	private vec4 c;
	private Callback callback;
	private int drawType;
	private boolean hideWhenNotInWindow;
	private float hidebounds;

	
	/**
	 * @param type (0 for quad, 1 for sphere)
	 * */
	public Button(float x, float y, float w, float h, vec4 color, int type, Callback callback) {
		this.x = x;
		this.y = y;
		this.w = w;
		this.h = h;
		this.c = color;
		this.callback = callback;
		drawType = type;
	}
	
	/**
	 * if true, button disappears when cursor is not in radius
	 * */
	public Button setHide(boolean hideWhenNotInWindow, float r) {
		this.hideWhenNotInWindow = hideWhenNotInWindow;
		this.hidebounds = r;
		return this;
	}
	
	public Button type(int type) {
		drawType = type;
		return this;
	}
	
	public boolean checkPressed(float x, float y, int button) {
		if (button != Keys.MOUSE_BUTTON_LEFT) return false;

		if ((x > this.x && x < this.x + w) && (y > this.y && y < this.y + h)) {
			callback.callback();
			return true;
		}
		
		return false;
	}
	
	public void manualRender(Renderer renderer, Window window) {
		if (hideWhenNotInWindow && (new vec2(x + w / 2.0f, y + w / 2.0f).length(window.getCursor()) > hidebounds)) 
			return;
		
		switch (drawType) {
		case 0:
			renderer.quads.submit(new vec3(x, y, 0.0f), new vec2(w, h), 0, c);
			break;
		case 1:
			renderer.spheres.submit(new vec3(x + w / 2.0f, y + w / 2.0f, 0.0f), new vec2(w, h).mul(0.5f), 32, 0, c);
			break;

		default:
			break;
		}
	}
	
}
