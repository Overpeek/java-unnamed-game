package graphics;

import java.nio.FloatBuffer;

import utility.vec2;
import utility.vec3;
import utility.vec4;

public interface GenericRenderer {
	
	public abstract GenericRenderer construct();
	public abstract GenericRenderer construct(FloatBuffer initialBuffer);
	
	public abstract void begin();
	public abstract void end();
	
	public abstract void clear();
	public abstract void submitVertex(VertexData data);
	public abstract void draw(int texture, int textureType);
	public abstract void draw(Texture texture);
	public abstract void draw();
	public abstract void submit(vec3 pos, vec2 size, int id, vec4 color);
	
}
