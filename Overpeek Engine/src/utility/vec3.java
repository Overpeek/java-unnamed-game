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
	
	public vec3 addLocal(vec3 other) {
		return new vec3(this.x + other.x, this.y + other.y, this.z + other.z);
	}
	
	public vec3 addLocal(float x, float y, float z) {
		return new vec3(this.x + x, this.y + y, this.z + z);
	}
	
	public vec3 sub(vec3 other) {
		this.x -= other.x;
		this.y -= other.y;
		this.z -= other.z;
		return this;
	}
	
	public vec3 sub(float x, float y, float z) {
		this.x -= x;
		this.y -= y;
		this.z -= z;
		return this;
	}
	
	public vec3 subLocal(vec3 other) {
		return new vec3(this.x - other.x, this.y - other.y, this.z - other.z);
	}
	
	public vec3 subLocal(float x, float y, float z) {
		return new vec3(this.x - x, this.y - y, this.z - z);
	}
	
	public vec3 mul(vec3 other) {
		this.x *= other.x;
		this.y *= other.y;
		this.z *= other.z;
		return this;
	}
	
	public vec3 mul(float x, float y, float z) {
		this.x *= x;
		this.y *= y;
		this.z *= z;
		return this;
	}
	
	public vec3 mul(float val) {
		this.x *= val;
		this.y *= val;
		this.z *= val;
		return this;
	}
	
	public vec3 mulLocal(vec3 other) {
		return new vec3(this.x * other.x, this.y * other.y, this.z * other.z);
	}
	
	public vec3 mulLocal(float x, float y, float z) {
		return new vec3(this.x * x, this.y * y, this.z * z);
	}
	
	public vec3 mulLocal(float val) {
		return new vec3(this.x * val, this.y * val, this.z * val);
	}
	
	public vec3 div(vec3 other) {
		this.x /= other.x;
		this.y /= other.y;
		this.z /= other.z;
		return this;
	}
	
	public vec3 div(float x, float y, float z) {
		this.x /= x;
		this.y /= y;
		this.z /= z;
		return this;
	}
	
	public vec3 div(float val) {
		this.x /= val;
		this.y /= val;
		this.z /= val;
		return this;
	}
	
	public vec3 divLocal(vec3 other) {
		return new vec3(this.x / other.x, this.y / other.y, this.z / other.z);
	}
	
	public vec3 divLocal(float x, float y, float z) {
		return new vec3(this.x / x, this.y / y, this.z / z);
	}
	
	public vec3 divLocal(float val) {
		return new vec3(this.x / val, this.y / val, this.z / val);
	}

	public vec3 setZero() {
		this.x = 0.0f;
		this.y = 0.0f;
		this.z = 0.0f;
		
		return this;
	}
	
	public vec3 invert() {
		return mul(-1.0f);
	}
	
	public vec3 invertLocal() {
		return mulLocal(-1.0f);
	}
	
	public vec3 set(vec3 vec) {
		this.x = vec.x;
		this.y = vec.y;
		this.z = vec.z;
		
		return this;
	}
	
	public vec3 set(float x, float y, float z) {
		this.x = x;
		this.y = y;
		this.z = z;
		
		return this;
	}
	
	public static float dot(vec3 a, vec3 b) {
		return a.x * b.x + a.y * b.y + a.z * b.z;
	}

	public static vec3 cross(vec3 a, vec3 b) {
	    return new vec3(a.y * b.z - a.z * b.y, a.z * b.x - a.x * b.z, a.x * b.y - a.y * b.x);
	}

	public static void crossToOut(vec3 a, vec3 b, vec3 out) {
	    final float tempy = a.z * b.x - a.x * b.z;
	    final float tempz = a.x * b.y - a.y * b.x;
	    out.x = a.y * b.z - a.z * b.y;
	    out.y = tempy;
	    out.z = tempz;
	}
	  
	public static void crossToOutUnsafe(vec3 a, vec3 b, vec3 out) {
	    assert(out != b);
	    assert(out != a);
	    out.x = a.y * b.z - a.z * b.y;
	    out.y = a.z * b.x - a.x * b.z;
	    out.z = a.x * b.y - a.y * b.x;
	}
	
	public vec2 vec2() {
		return new vec2(x, y);
	}
	
	@Override
	public String toString() {
		return "vec3[" + x + "," + y + "," + z + "]";
	}
	
	@Override
	public vec3 clone() {
		return new vec3(x, y, z);
	}
	
}
