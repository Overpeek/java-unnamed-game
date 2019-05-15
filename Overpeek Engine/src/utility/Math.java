package utility;

import java.util.Random;

public class Math {

	private static Random rand;
	
	public static float random(float min, float max) {
		return map(rand.nextFloat(), 0.0f, 1.0f, min, max);
	}

	public static float map(float value, float low1, float high1, float low2, float high2) {
		return low2 + (value - low1) * (high2 - low2) / (high1 - low1);
	}
	
}
