package utility;

import java.awt.Color;

public class vec4 {
	
	public float x, y, z, w;
	
	public vec4(float x, float y, float z, float w) {
		this.x = x;
		this.y = y;
		this.z = z;
		this.w = w;
	}
	
	public vec4(float val) {
		this.x = val;
		this.y = val;
		this.z = val;
		this.w = val;
	}
	
	public vec4(Color color) {
		this.x = color.getRed();
		this.y = color.getGreen();
		this.z = color.getBlue();
		this.w = color.getAlpha();
	}
	
	public vec4(vec4 xyzw) {
		this.x = xyzw.x;
		this.y = xyzw.y;
		this.z = xyzw.z;
		this.w = xyzw.w;
	}
	
	public vec4() {
		this.x = 0.0f;
		this.y = 0.0f;
		this.z = 0.0f;
		this.w = 0.0f;
	}
	
	public vec4 random(float min, float max) {
		this.x = Maths.random(min, max);
		this.y = Maths.random(min, max);
		this.z = Maths.random(min, max);
		this.w = Maths.random(min, max);
		
		return this;
	}
	
	public float length() {
		return (float) Math.sqrt(x*x + y*y + z*z + w*w);
	}
	
	public vec4 add(vec4 other) {
		this.x += other.x;
		this.y += other.y;
		this.z += other.z;
		this.w += other.w;
		return this;
	}
	
	public vec4 add(float x, float y, float z, float w) {
		this.x += x;
		this.y += y;
		this.z += z;
		this.w += w;
		return this;
	}
	
	public vec4 mult(vec4 other) {
		this.x *= other.x;
		this.y *= other.y;
		this.z *= other.z;
		this.w *= other.w;
		return this;
	}
	
	public vec4 mult(float x, float y, float z, float w) {
		this.x *= x;
		this.y *= y;
		this.z *= z;
		this.w *= w;
		return this;
	}
	
	public vec4 mult(float val) {
		this.x *= val;
		this.y *= val;
		this.z *= val;
		this.w *= val;
		return this;
	}
	
	public Color toJavaColor() {
		return new Color(x, y, z, w);
	}
	
	@Override
	public String toString() {
		return "vec4[" + x + "," + y + "," + z + "," + w + "]";
	}
	
}
