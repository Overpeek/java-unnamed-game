package utility;

public class Console extends GUIWindow {

	public Console(vec2 position, vec2 size) {
		this.position = position;
		this.size = size;
	}
	
	@Override
	public void draw() {
		drawFrame();
	}

	@Override
	public void input() {
		// TODO Auto-generated method stub
		
	}
	
	
	
}
