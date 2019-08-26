package utility;

public abstract class GUIWindow {

	protected vec2 position;
	protected vec2 size;
	
	public abstract void draw();
	public abstract void input();
	
	protected void drawFrame() {
			
//		final float border = 0.01f;
		
//		StreamQuadData[] quads = new StreamQuadData[2];
//		quads[0] = new StreamQuadData(new vec3(0.0f).addLocal(new vec3(-border)), size.addLocal(new vec2(border * 2.0f)), 0, new vec4(0.1f, 0.1f, 0.1f, 1.0f));
//		quads[1] = new StreamQuadData(new vec3(0.0f), size, 0, new vec4(0.2f, 0.2f, 0.2f, 1.0f));

//		StreamQuadRenderer.streamQuadRender(quads);
		
	}
	
}
