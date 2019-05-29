package utility;

import java.util.Random;
import utility.SimplexNoise;

public class Maths {

	private static Random rand;
	private static SimplexNoise noise;
	
	
	
	public static void seed(float seed) {
		rand = new Random((long) seed);
		noise = new SimplexNoise((int) seed);
	}
	
	public static float random(float min, float max) {
		if (rand == null) seed(0);
		
		return map(rand.nextFloat(), 0.0f, 1.0f, min, max);
	}

	public static float map(float value, float low1, float high1, float low2, float high2) {
		return low2 + (value - low1) * (high2 - low2) / (high1 - low1);
	}
	
	public static float noise(float x, float y, float min, float max) {
		if (noise == null) seed(0);
		
		return map((float) noise.noise(x + 0.01f, y + 0.01f, 0.01f), 0.0f, 1.0f, min, max);
	}
	
}
