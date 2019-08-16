package graphics;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL40;
import org.lwjgl.opengl.GL43;

import utility.vec2;
import utility.vec3;
import utility.vec4;

public class StreamQuadRenderer {
	
	public static class StreamQuadData {
		
		public VertexData vertA;
		public VertexData vertB;
		public VertexData vertC;
		public VertexData vertD;
		
		public StreamQuadData(VertexData _vertA, VertexData _vertB, VertexData _vertC, VertexData _vertD) {
			vertA = _vertA;
			vertB = _vertB;
			vertC = _vertC;
			vertD = _vertD;
		}

		public StreamQuadData(vec3 _pos, vec2 _size, int _id, vec4 _color) {
			vertA = new VertexData(_pos.x, 			_pos.y, 			_pos.z, 0.0f, 0.0f, _id, _color.x, _color.y, _color.z, _color.w);
			vertB = new VertexData(_pos.x, 			_pos.y + _size.y, 	_pos.z, 0.0f, 1.0f, _id, _color.x, _color.y, _color.z, _color.w);
			vertC = new VertexData(_pos.x + _size.x, 	_pos.y + _size.y, 	_pos.z, 1.0f, 1.0f, _id, _color.x, _color.y, _color.z, _color.w);
			vertD = new VertexData(_pos.x + _size.x, 	_pos.y, 			_pos.z, 1.0f, 0.0f, _id, _color.x, _color.y, _color.z, _color.w);
		}
		
	}
	
	public static void streamQuadRender(StreamQuadData[] quads) {
		FloatBuffer arrayBuffer = BufferUtils.createFloatBuffer(quads.length * 4 * VertexData.componentCount);
		IntBuffer indexBuffer = BufferUtils.createIntBuffer(quads.length * 6 * VertexData.componentCount);
		
		for (int i = 0; i < quads.length; i++) {
			arrayBuffer.put(quads[i].vertA.get());
			arrayBuffer.put(quads[i].vertB.get());
			arrayBuffer.put(quads[i].vertC.get());
			arrayBuffer.put(quads[i].vertD.get());
		}
		arrayBuffer.flip();
		
		int index_counter = 0;
		for (int i = 0; i < quads.length; i++) {
			indexBuffer.put(index_counter + 0);
			indexBuffer.put(index_counter + 1);
			indexBuffer.put(index_counter + 2);
			indexBuffer.put(index_counter + 0);
			indexBuffer.put(index_counter + 2);
			indexBuffer.put(index_counter + 3);
			
			index_counter += 4;
		}
		indexBuffer.flip();
		
		VertexArray vao = new VertexArray();
		Buffer vbo = new Buffer(arrayBuffer, GL20.GL_ARRAY_BUFFER, VertexData.componentCount, GL15.GL_STREAM_DRAW);
		Buffer ibo = new Buffer(indexBuffer, GL40.GL_ELEMENT_ARRAY_BUFFER, VertexData.componentCount, GL15.GL_STATIC_DRAW);
		
		GL20.glEnableVertexAttribArray(0);
		GL20.glVertexAttribPointer(0, 3, GL11.GL_FLOAT, false, VertexData.sizeof, VertexData.attribPos);
		GL20.glEnableVertexAttribArray(1);
		GL20.glVertexAttribPointer(1, 2, GL11.GL_FLOAT, false, VertexData.sizeof, VertexData.attribUV);
		GL20.glEnableVertexAttribArray(2);
		GL20.glVertexAttribPointer(2, 1, GL11.GL_FLOAT, false, VertexData.sizeof, VertexData.attribTex);
		GL20.glEnableVertexAttribArray(3);
		GL20.glVertexAttribPointer(3, 4, GL11.GL_FLOAT, false, VertexData.sizeof, VertexData.attribCol);
		
		GL11.nglDrawElements(GL11.GL_TRIANGLES, quads.length * 6, GL11.GL_UNSIGNED_INT, 0);
		
		vao.delete();
		vbo.delete();
		ibo.delete();
		
		arrayBuffer.clear();
		indexBuffer.clear();
	}
	
}
