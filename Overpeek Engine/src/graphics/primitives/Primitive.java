package graphics.primitives;

import java.nio.IntBuffer;

import org.lwjgl.opengl.GL11;

public abstract class Primitive {

	
	protected final static int circle_res = 16;
	
	public static enum Primitives {
		
		
		Quad(4, 6, GL11.GL_TRIANGLES), Triangle(3, 3, GL11.GL_TRIANGLES), Line(2, 2, GL11.GL_LINES), Point(1, 1, GL11.GL_POINTS), Circle(1 + circle_res, 3 * circle_res, GL11.GL_TRIANGLES);
		
		public final int vertex_count;
		public final int index_count;
		public final int gl_primitive_type;
		Primitives(int vertex_count, int index_count, int gl_primitive_type) {
			this.vertex_count = vertex_count;
			this.index_count = index_count;
			this.gl_primitive_type = gl_primitive_type;
		}
		
		public void fillIndexBufferData(IntBuffer buffer) {
			int buffer_size = buffer.capacity();
			if (this.equals(Quad)) {

				int index_counter = 0;
				for (int i = 0; i < buffer_size / index_count; i++) {
					buffer.put(index_counter + 0);
					buffer.put(index_counter + 1);
					buffer.put(index_counter + 2);
					buffer.put(index_counter + 0);
					buffer.put(index_counter + 2);
					buffer.put(index_counter + 3);
					
					index_counter += 4;
				}
				
			} else if (this.equals(Circle)) {

				int index_counter = 0;
				for (int i = 0; i < buffer_size / index_count; i++) {
					
					int res_counter = 1;
					for (int j = 0; j < circle_res - 1; j++) {
						buffer.put(index_counter + 0);
						buffer.put(index_counter + res_counter);
						buffer.put(index_counter + res_counter + 1);
						res_counter++;
					}

					buffer.put(index_counter + 0);
					buffer.put(index_counter + 1);
					buffer.put(index_counter + circle_res);
					
					index_counter += circle_res + 1;
				}
				
			} else {

				for (int i = 0; i < buffer_size; i++) {
					buffer.put(i);
				}
				
			}
		}
		
	}
	
	public VertexData vertexData[] = null;
	
	public float[] getData() {
		float data[] = new float[vertexData.length * VertexData.componentCount];
		for (int i = 0; i < vertexData.length; i++) {
			for (int j = 0; j < VertexData.componentCount; j++) {
				data[i + j * vertexData.length] = vertexData[i].get()[j];
			}
		}
		
		return data;
	}

}
