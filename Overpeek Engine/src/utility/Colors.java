package utility;

public class Colors {

	//Mains
	public static vec4 RED = new vec4(1.0f, 0.0f, 0.0f, 1.0f);
	public static vec4 GREEN = new vec4(0.0f, 1.0f, 0.0f, 1.0f);
	public static vec4 BLUE = new vec4(0.0f, 0.0f, 1.0f, 1.0f);
	
	//Blends
	public static vec4 CYAN = new vec4(0.0f, 1.0f, 1.0f, 1.0f);
	public static vec4 ORANGE = new vec4(1.0f, 0.5f, 0.0f, 1.0f);
	public static vec4 YELLOW = new vec4(1.0f, 1.0f, 0.0f, 1.0f);
	
	//Mono
	public static vec4 WHITE = new vec4(1.0f, 1.0f, 1.0f, 1.0f);
	public static vec4 GREY = new vec4(0.5f, 0.5f, 0.5f, 1.0f);
	public static vec4 BLACK = new vec4(0.0f, 0.0f, 0.0f, 1.0f);
	public static vec4 TRANSPARENT = new vec4(0.0f, 0.0f, 0.0f, 0.0f);
	
	public static vec4 colorCode(int n) {
		switch (n) {
		case 0:
			return WHITE;
		case 1:
			return GREY;
		case 2:
			return BLACK;
		case 3:
			return RED;
		case 4:
			return GREEN;
		case 5:
			return BLUE;
		case 6:
			return CYAN;
		case 7:
			return ORANGE;
		case 8:
			return YELLOW;

		default:
			return WHITE;
		}
	}
	
}
