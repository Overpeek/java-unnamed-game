package graphics;

import java.nio.FloatBuffer;

import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;

public class SphereRenderer implements GenericRenderer {

	private static int MAX_SPHERES = 60000;
	private static int RESOLUTION = 32;
	private static int MAX_VERTICES = MAX_SPHERES * RESOLUTION * 3;

	private VertexArray vertexArray;
	private Buffer arrayBufferObject;

	private FloatBuffer arrayBuffer;
	private int vertex_count;
	
	private boolean buffer_mapped;

	
	public SphereRenderer() {
		FloatBuffer arrayBuffer = BufferUtils.createFloatBuffer(MAX_VERTICES * VertexData.componentCount());
		buffer_mapped = false;
		vertex_count = 0;
		
		vertexArray = new VertexArray();
		arrayBufferObject = new Buffer(arrayBuffer, GL20.GL_ARRAY_BUFFER, VertexData.componentCount(), GL15.GL_DYNAMIC_DRAW);
		
		GL20.glEnableVertexAttribArray(0);
		GL20.glVertexAttribPointer(0, 3, GL11.GL_FLOAT, false, VertexData.sizeof(), VertexData.attribPos());
		GL20.glEnableVertexAttribArray(1);
		GL20.glVertexAttribPointer(1, 2, GL11.GL_FLOAT, false, VertexData.sizeof(), VertexData.attribUV());
		GL20.glEnableVertexAttribArray(2);
		GL20.glVertexAttribPointer(2, 1, GL11.GL_FLOAT, false, VertexData.sizeof(), VertexData.attribTex());
		GL20.glEnableVertexAttribArray(3);
		GL20.glVertexAttribPointer(3, 4, GL11.GL_FLOAT, false, VertexData.sizeof(), VertexData.attribCol());

	}

	@Override
	public void begin() {
		arrayBuffer = arrayBufferObject.mapBuffer().asFloatBuffer();
		arrayBuffer.clear();
		buffer_mapped = true;
	}

	@Override
	public void end() {
		arrayBufferObject.unmapBuffer();
		buffer_mapped = false;
	}

	@Override
	public void submitVertex(VertexData data) {
		if (!buffer_mapped) begin();
		
		arrayBuffer.put(data.get());
		vertex_count++;
	}

	@Override
	public void submit(Vector3f _pos, Vector2f _size, int _id, Vector4f _color) {
		VertexData centerVertexData = new VertexData(new Vector3f(_pos.x, _pos.y, 0.0f), new Vector2f(0.0f, 0.0f), _id, _color);

		for (int i = 0; i < RESOLUTION; i++)
		{
			//Center
			submitVertex(centerVertexData);

			//First
			float xoff = (float) (Math.cos(i / (float)RESOLUTION * Math.PI * 2.0f) * _size.x);
			float yoff = (float) (Math.sin(i / (float)RESOLUTION * Math.PI * 2.0f) * _size.y);

			centerVertexData.x += xoff;
			centerVertexData.y += yoff;
			submitVertex(centerVertexData);
			centerVertexData.x -= xoff;
			centerVertexData.y -= yoff;

			//Second
			xoff = (float) (Math.cos((i + 1) / (float)RESOLUTION * Math.PI * 2.0f) * _size.x);
			yoff = (float) (Math.sin((i + 1) / (float)RESOLUTION * Math.PI * 2.0f) * _size.y);

			centerVertexData.x += xoff;
			centerVertexData.y += yoff;
			submitVertex(centerVertexData);
			centerVertexData.x -= xoff;
			centerVertexData.y -= yoff;
		}
	}

	@Override
	public void clear() {
		vertex_count = 0;
	}

	@Override
	public void draw(Texture texture) {
		draw(texture.getId(), texture.getType());
	}

	@Override
	public void draw(int texture, int textureType) {
		//Stop submitting
		if (buffer_mapped) end();
		
		//Return if no vertices to render
		if (vertex_count == 0)
			return;

		// Binding
		GL13.glActiveTexture(GL13.GL_TEXTURE0);
		if (textureType != 0) GL11.glBindTexture(textureType, texture);
		vertexArray.bind();
		
		//Actual drawing
		GL11.glDrawArrays(GL11.GL_TRIANGLES, 0, vertex_count);
	}

}
