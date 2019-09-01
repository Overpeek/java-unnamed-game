package utility;

public class vec2 {
	
	public float x, y;
	
	
	
	/*
	 * Constructors
	 * **/
	
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
	
	
	
	/*
	 * Basic functions
	 * **/
	
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
	
	public vec2 addLocal(vec2 other) {
		return new vec2(this.x + other.x, this.y + other.y);
	}
	
	public vec2 addLocal(float x, float y) {
		return new vec2(this.x + x, this.y + y);
	}
	
	public vec2 sub(vec2 other) {
		this.x -= other.x;
		this.y -= other.y;
		return this;
	}
	
	public vec2 sub(float x, float y) {
		this.x -= x;
		this.y -= y;
		return this;
	}
	
	public vec2 subLocal(vec2 other) {
		return new vec2(this.x - other.x, this.y - other.y);
	}
	
	public vec2 subLocal(float x, float y) {
		return new vec2(this.x - x, this.y - y);
	}
	
	public vec2 mul(vec2 other) {
		this.x *= other.x;
		this.y *= other.y;
		return this;
	}
	
	public vec2 mul(float x, float y) {
		this.x *= x;
		this.y *= y;
		return this;
	}
	
	public vec2 mul(float val) {
		this.x *= val;
		this.y *= val;
		return this;
	}
	
	public vec2 mulLocal(vec2 other) {
		return new vec2(this.x * other.x, this.y * other.y);
	}
	
	public vec2 mulLocal(float x, float y) {
		return new vec2(this.x * x, this.y * y);
	}
	
	public vec2 mulLocal(float val) {
		return new vec2(this.x * val, this.y * val);
	}
	
	public vec2 div(vec2 other) {
		this.x /= other.x;
		this.y /= other.y;
		return this;
	}
	
	public vec2 div(float x, float y) {
		this.x /= x;
		this.y /= y;
		return this;
	}
	
	public vec2 div(float val) {
		this.x /= val;
		this.y /= val;
		return this;
	}
	
	public vec2 divLocal(vec2 other) {
		return new vec2(this.x / other.x, this.y / other.y);
	}
	
	public vec2 divLocal(float x, float y) {
		return new vec2(this.x / x, this.y / y);
	}
	
	public vec2 divLocal(float val) {
		return new vec2(this.x / val, this.y / val);
	}

	public vec2 setZero() {
		this.x = 0.0f;
		this.y = 0.0f;
		
		return this;
	}
	
	
	
	/*
	 * Advanced functions
	 * */
	
	public vec2 abs() {
		x = Maths.abs(x);
		y = Maths.abs(y);
		
		return this;
	}
	
	public vec2 absLocal() {
		return new vec2(Maths.abs(x), Maths.abs(y));
	}
	
	public vec2 invert() {
		return mul(-1.0f);
	}
	
	public vec2 invertLocal() {
		return mulLocal(-1.0f);
	}
	
	public vec2 normalize() {
		float len = length();
		if (len <= 0) return null;
		mul(1.0f / len);
		return this;			
	}
	
	public vec2 normalizeLocal() {
		float len = length();
		if (len <= 0) return null;
		
		return new vec2(x, y).mul(1.0f / len);			
	}
	
	public vec2 mostSignificant() {
		if (Maths.abs(x) > Maths.abs(y)) {
			y = 0.0f;
		} else  {
			x = 0.0f;
		}
		return this;
	}
	
	public vec2 mostSignificantLocal() {
		vec2 cloned = clone();
		if (Maths.abs(cloned.x) > Maths.abs(cloned.y)) {
			cloned.y = 0.0f;
		} else  {
			cloned.x = 0.0f;
		}
		return cloned;
	}
	
	
	
	/*
	 * Static functions
	 * **/
	
	public static vec2 lerp(vec2 a, vec2 b, float t) {
		vec2 returned = new vec2();
		returned.x = a.x + t * (b.x - a.x);
		returned.y = a.y + t * (b.y - a.y);
		return returned;
	}
	
	public static vec2 rotate(vec2 center, vec2 point, float angle) {
		float s = Maths.sin(angle);
		float c = Maths.cos(angle);

		// translate point back to origin:
		point.x -= center.x;
		point.y -= center.y;

		// rotate point
		float xnew = point.x * c - point.y * s;
		float ynew = point.x * s + point.y * c;

		// translate point back:
		point.x = xnew + center.x;
		point.y = ynew + center.y;
		
		return point;
	}
	
	public static float dot(vec2 a, vec2 b) {
		return a.x * b.x + a.y * b.y;
	}

	public static void absToOut(vec2 in, vec2 out) {
	    out.x = Maths.abs(in.x);
	    out.y = Maths.abs(in.y);
	}

	public static float cross(vec2 a, vec2 b) {
	    return a.x * b.y - a.y * b.x;
	}

	public static vec2 cross(vec2 a, float s) {
	    return new vec2(s * a.y, -s * a.x);
	}

	public static vec2 cross(float s, vec2 a) {
	    return new vec2(-s * a.y, s * a.x);
	}

	public static void crossToOut(float s, vec2 a, vec2 out) {
	     float tempY = s * a.x;
	    out.x = -s * a.y;
	    out.y = tempY;
	}

	public static void crossToOutUnsafe(float s, vec2 a, vec2 out) {
	    assert (out != a);
	    out.x = -s * a.y;
	    out.y = s * a.x;
	}

	public static void negateToOut(vec2 a, vec2 out) {
	    out.x = -a.x;
	    out.y = -a.y;
	}

	public static vec2 min(vec2 a, vec2 b) {
	    return new vec2(a.x < b.x ? a.x : b.x, a.y < b.y ? a.y : b.y);
	}

	public static vec2 max(vec2 a, vec2 b) {
	    return new vec2(a.x > b.x ? a.x : b.x, a.y > b.y ? a.y : b.y);
	}

	public static void minToOut(vec2 a, vec2 b, vec2 out) {
	    out.x = a.x < b.x ? a.x : b.x;
	    out.y = a.y < b.y ? a.y : b.y;
	}

	public static void maxToOut(vec2 a, vec2 b, vec2 out) {
	    out.x = a.x > b.x ? a.x : b.x;
	    out.y = a.y > b.y ? a.y : b.y;
	}
	
	
	/*
	 * Others
	 * **/
	
	public boolean isValid() {
		return !Float.isNaN(x) && !Float.isInfinite(x) && !Float.isNaN(y) && !Float.isInfinite(y);
	}
	
	public float length() {
		return (float) Math.sqrt(x*x + y*y);
	}
	
	public float length(vec2 other) {
		return this.addLocal(other.invert()).length();
	}
	
	public float lengthSquared() {
		return (float) x*x + y*y;
	}
	
	public float lengthSquared(vec2 other) {
		return this.addLocal(other.invert()).lengthSquared();
	}
	
	public float dot(vec2 other) {
		return this.x * other.x + this.y * other.y;
	}
	
	public vec3 tovec3(float z) {
		return new vec3(x, y, z);
	}
	
	public vec2 set(vec2 vec) {
		this.x = vec.x;
		this.y = vec.y;
		
		return this;
	}
	
	public vec2 set(float x, float y) {
		this.x = x;
		this.y = y;
		
		return this;
	}
	
	@Override
	public String toString() {
		return "vec2[" + x + "," + y + "]";
	}
	
	@Override
	public vec2 clone() {
		return new vec2(x, y);
	}
	
}
