package utility;

public class vecN {
	
	public float values[];
	

	
	/*
	 * Constructors
	 * **/
	
	public vecN(float... value) {
		values = value;
	}
	
	public vecN(float value, int size) {
		values = new float[size];
		for (int i = 0; i < size; i++) {
			values[i] = value;
		}
	}
	
	public vecN(int size) {
		this(0.0f, size);
	}
	
	
	
	/*
	 * Basic functions
	 * **/
	
	public vecN add(vecN other) {
		for (int i = 0; i < Math.min(values.length, other.values.length); i++) { // Loop trough smaller vector to prevent out of bounds error
			values[i] += other.values[i];
		}
		
		return this;
	}
	
	public vecN add(float... values) {
		for (int i = 0; i < Math.min(values.length, values.length); i++) {
			values[i] += values[i];
		}
		
		return this;
	}
	
	public vecN addLocal(vecN other) {
		vecN vec = clone();
		for (int i = 0; i < Math.min(vec.values.length, other.values.length); i++) { // Loop trough smaller vector to prevent out of bounds error
			vec.values[i] += other.values[i];
		}
		
		return vec;
	}
	
	public vecN addLocal(float... values) {
		vecN vec = clone();
		for (int i = 0; i < Math.min(vec.values.length, values.length); i++) {
			vec.values[i] += values[i];
		}
		
		return vec;
	}
	
	public vecN sub(vecN other) {
		for (int i = 0; i < Math.min(values.length, other.values.length); i++) { // Loop trough smaller vector to prevent out of bounds error
			values[i] -= other.values[i];
		}
		
		return this;
	}
	
	public vecN sub(float... values) {
		for (int i = 0; i < Math.min(values.length, values.length); i++) {
			values[i] -= values[i];
		}
		
		return this;
	}
	
	public vecN subLocal(vecN other) {
		vecN vec = clone();
		for (int i = 0; i < Math.min(vec.values.length, other.values.length); i++) { // Loop trough smaller vector to prevent out of bounds error
			vec.values[i] -= other.values[i];
		}
		
		return vec;
	}
	
	public vecN subLocal(float... values) {
		vecN vec = clone();
		for (int i = 0; i < Math.min(vec.values.length, values.length); i++) {
			vec.values[i] -= values[i];
		}
		
		return vec;
	}
	
	public vecN mult(vecN other) {
		for (int i = 0; i < Math.min(values.length, other.values.length); i++) { // Loop trough smaller vector to prevent out of bounds error
			values[i] *= other.values[i];
		}
		
		return this;
	}
	
	public vecN mult(float... values) {
		for (int i = 0; i < Math.min(values.length, values.length); i++) {
			values[i] *= values[i];
		}
		
		return this;
	}
	
	public vecN mult(float val) {
		for (int i = 0; i < values.length; i++) {
			values[i] *= val;
		}
		
		return this;
	}
	
	public vecN multLocal(vecN other) {
		vecN vec = clone();
		for (int i = 0; i < Math.min(vec.values.length, other.values.length); i++) { // Loop trough smaller vector to prevent out of bounds error
			vec.values[i] *= other.values[i];
		}
		
		return vec;
	}
	
	public vecN multLocal(float... values) {
		vecN vec = clone();
		for (int i = 0; i < Math.min(vec.values.length, values.length); i++) {
			vec.values[i] *= values[i];
		}
		
		return vec;
	}
	
	public vecN multLocal(float val) {
		vecN vec = clone();
		for (int i = 0; i < vec.values.length; i++) {
			vec.values[i] *= val;
		}
		
		return vec;
	}
	
	public vecN div(vecN other) {
		for (int i = 0; i < Math.min(values.length, other.values.length); i++) { // Loop trough smaller vector to prevent out of bounds error
			values[i] /= other.values[i];
		}
		
		return this;
	}
	
	public vecN div(float... values) {
		for (int i = 0; i < Math.min(values.length, values.length); i++) {
			values[i] *= values[i];
		}
		
		return this;
	}
	
	public vecN div(float val) {
		for (int i = 0; i < values.length; i++) {
			values[i] *= val;
		}
		
		return this;
	}
	
	public vecN divLocal(vecN other) {
		vecN vec = clone();
		for (int i = 0; i < Math.min(vec.values.length, other.values.length); i++) { // Loop trough smaller vector to prevent out of bounds error
			vec.values[i] /= other.values[i];
		}
		
		return vec;
	}
	
	public vecN divLocal(float... values) {
		vecN vec = clone();
		for (int i = 0; i < Math.min(vec.values.length, values.length); i++) {
			vec.values[i] /= values[i];
		}
		
		return vec;
	}
	
	public vecN divLocal(float val) {
		vecN vec = clone();
		for (int i = 0; i < vec.values.length; i++) {
			vec.values[i] /= val;
		}
		
		return vec;
	}

	public vecN setZero() {
		for (int i = 0; i < values.length; i++) {
			values[i] = 0.0f;
		}
		return this;
	}

	public vecN setZeroLocal() {
		return new vecN(values.length);
	}
	
	
	
	/*
	 * Advanced functions
	 * **/
	
	public vecN abs() {
		for (int i = 0; i < values.length; i++) {
			values[i] = Maths.abs(values[i]);
		}

		return this;
	}
	
	public vecN absLocal() {
		vecN vec = clone();
		for (int i = 0; i < values.length; i++) {
			vec.values[i] = Maths.abs(values[i]);
		}

		return vec;
	}
	
	public vecN invert() {
		return mult(-1.0f);
	}
	
	public vecN invertLocal() {
		return multLocal(-1.0f);
	}
	
	public vecN normalize() {
		float len = length();
		if (len <= 0) return null;
		mult(1.0f / len);
		return this;			
	}
	
	public vecN normalizeLocal() {
		float len = length();
		if (len <= 0) return null;
		
		return clone().mult(1.0f / len);			
	}
	
	
	
	/*
	 * Static functions
	 * **/
	
	public static float dot(vecN a, vecN b) {
		float value = 0.0f;
		for (int i = 0; i < Math.min(a.values.length, b.values.length); i++) {
			value += a.values[i] * b.values[i];
		}
		
		return value;
	}
	
	
	
	/*
	 * Others
	 * **/
	
	public float length() {
		float value = 0.0f;
		for (int i = 0; i < values.length; i++) {
			value += values[i] * values[i];
		}
		
		return (float) Math.sqrt(value);
	}
	
	public float length(vecN other) {
		return this.addLocal(other.invert()).length();
	}
	
	public vecN set(vecN vec) {
		for (int i = 0; i < Math.min(values.length, vec.values.length); i++) { // Loop trough smaller vector to prevent out of bounds error
			values[i] = vec.values[i];
		}
		
		return this;
	}
	
	public vecN set(float... values) {
		for (int i = 0; i < Math.min(values.length, values.length); i++) { // Loop trough smaller vector to prevent out of bounds error
			values[i] = values[i];
		}
		
		return this;
	}
	
	

	
	@Override
	public String toString() {
		String string = "vecN(" + values.length + ")[";
		
		for (int i = 0; i < values.length; i++) {
			string += values[i] + ",";
		}
		return string + "]";
	}
	
	@Override
	public vecN clone() {
		return new vecN(values);
	}

}
