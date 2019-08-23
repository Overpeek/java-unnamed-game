package utility;

import java.util.Random;

public class Maths {

	private static Random rand;
	private static SimplexNoise_octave noise;
	
	public static final float PI = 3.141592653589793238462643383f;
	
	
	public static void seed(long seed) {
		rand = new Random(seed);
		noise = new SimplexNoise_octave((int) seed);
	}
	
	public static void seed() {
		seed(System.currentTimeMillis());
	}
	
	public static float random(float min, float max) {
		if (rand == null) seed();
		
		return map(rand.nextFloat(), 0.0f, 1.0f, min, max);
	}

	public static float map(float value, float low1, float high1, float low2, float high2) {
		return low2 + (value - low1) * (high2 - low2) / (high1 - low1);
	}
	
	public static float noise(float x, float y, float min, float max) {
		if (noise == null) seed();
		
		return map((float) noise.noise(x + 0.01f, y + 0.01f, 0.01f), -1.0f, 1.0f, min, max);
	}
	
	public static float clamp(float value, float min, float max) {
		if (value >= max) value = max;
		if (value <= min) value = min;
		
		return value;
	}
	
	public static float cos(float value) {
		return (float) Math.cos(value);
	}
	
	public static float sin(float value) {
		return (float) Math.sin(value);
	}
	
	public static float tan(float value) {
		return (float) Math.tan(value);
	}
	
	public static float abs(float value) {
		return Math.abs(value);
	}
	
	public static boolean isInRange(float value, float min, float max) {
		if (value == clamp(value, min, max))
			return true;
		
		return false;
	}
	
}
