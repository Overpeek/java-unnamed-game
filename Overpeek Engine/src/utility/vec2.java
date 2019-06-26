package utility;

public class vec2 {
	
	public float x, y;
	
	public vec2(float x, float y) {
		this.x = x;
		this.y = y;
	}
	
	public vec2(float val) {
		this.x = val;
		this.y = val;
	}
	
	public vec2(vec2 xy) {
		this.x = xy.x;
		this.y = xy.y;
	}
	
	public vec2() {
		this.x = 0.0f;
		this.y = 0.0f;
	}
	
	public float length() {
		return (float) Math.sqrt(x*x + y*y);
	}
	
	public float length(vec2 other) {
		return this.add(other.negate()).length();
	}
	
	public vec2 add(vec2 other) {
		this.x += other.x;
		this.y += other.y;
		return this;
	}
	
	public vec2 add(float x, float y) {
		this.x += x;
		this.y += y;
		return this;
	}
	
	public vec2 mult(vec2 other) {
		this.x *= other.x;
		this.y *= other.y;
		return this;
	}
	
	public vec2 mult(float x, float y) {
		this.x *= x;
		this.y *= y;
		return this;
	}
	
	public vec2 mult(float val) {
		this.x *= val;
		this.y *= val;
		return this;
	}
	
	public vec2 negate() {
		return mult(-1.0f);
	}
	
	@Override
	public String toString() {
		return "(" + x + ", " + y + ")";
	}
	
}
