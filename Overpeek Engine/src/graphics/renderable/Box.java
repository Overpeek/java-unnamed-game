package graphics.renderable;

public class Box {
	
	private static final int VERTICES = 4;
	private static final int INDICES = 6;
	
	private int render_index;
	private float x, y, w, h;
	private boolean visible;
	
	public Box(float x, float y, float w, float h) {
		visible = true;
	}
	
	public void updatePosition() {
		
	}
	
	
	/*
	 * Getters and setters
	 */
	public int getRenderIndex() {
		return render_index;
	}
	
	public float getX() {
		return x;
	}
	
	public float getY() {
		return y;
	}
	
	public float getW() {
		return w;
	}
	
	public float getH() {
		return h;
	}
	
	public void setX(float x) {
		this.x = x;
	}
	
	public void setY(float y) {
		this.y = y;
	}
	
	public void setW(float w) {
		this.w = w;
	}
	
	public void setH(float h) {
		this.h = h;
	}
}
