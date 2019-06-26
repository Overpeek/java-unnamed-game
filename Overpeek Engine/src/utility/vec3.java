package utility;

public class vec3 {
	
	public float x, y, z;
	
	public vec3(float x, float y, float z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}
	
	public vec3(vec3 xyz) {
		this.x = xyz.x;
		this.y = xyz.y;
		this.z = xyz.z;
	}
	
	public vec3(vec2 xy, float z) {
		this.x = xy.x;
		this.y = xy.y;
		this.z = z;
	}
	
	public vec3(float val) {
		this.x = val;
		this.y = val;
		this.z = val;
	}
	
	public vec3() {
		this.x = 0.0f;
		this.y = 0.0f;
		this.z = 0.0f;
	}
	
	public float length() {
		return (float) Math.sqrt(x*x + y*y + z*z);
	}
	
	public vec3 add(vec3 other) {
		this.x += other.x;
		this.y += other.y;
		this.z += other.z;
		return this;
	}
	
	public vec3 add(float x, float y, float z) {
		this.x += x;
		this.y += y;
		this.z += z;
		return this;
	}
	
	public vec3 mult(vec3 other) {
		this.x *= other.x;
		this.y *= other.y;
		this.z *= other.z;
		return this;
	}
	
	public vec3 mult(float x, float y, float z) {
		this.x *= x;
		this.y *= y;
		this.z *= z;
		return this;
	}
	
	public vec3 mult(float val) {
		this.x *= val;
		this.y *= val;
		this.z *= val;
		return this;
	}
	
	@Override
	public String toString() {
		return "(" + x + ", " + y + ", " + z + ")";
	}
	
}
