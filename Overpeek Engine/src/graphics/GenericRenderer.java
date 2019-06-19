package graphics;

import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector4f;

public interface GenericRenderer {
	
	public abstract void begin();
	public abstract void end();
	
	public abstract void clear();
	public abstract void submitVertex(VertexData data);
	public abstract void draw(int texture, int textureType);
	public abstract void draw(Texture texture);
	public abstract void submit(Vector3f pos, Vector2f size, int id, Vector4f color);
	
}
