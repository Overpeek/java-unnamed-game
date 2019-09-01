package utility;

public class Button {
	
	private vec2 position;
	private vec2 size;
	private Callback callback;

	

	public Button(float x, float y, float w, float h) {
		this(x, y, w, h, null);
	}
	
	public Button(float x, float y, float w, float h, Callback callback) {
		this.position = new vec2(x, y);
		this.size = new vec2(w, h);
		this.callback = callback;
	}
	
	/*
	 * cursor positions (preferrably)
	 * 
	 * returns true if hovered over
	 * 
	 * calls callback if button pressed while hovering over
	 * */
	public boolean update(float x, float y, boolean pressed) {
		boolean hover_over = false;
		if ((x > this.position.x && x < this.position.x + this.size.x) && (y > this.position.y && y < this.position.y + this.size.y)) {
			hover_over = true;
		}
		
		if (hover_over && pressed) {
			if (callback != null) callback.callback();
		}
		
		return hover_over;
	}
	
}
