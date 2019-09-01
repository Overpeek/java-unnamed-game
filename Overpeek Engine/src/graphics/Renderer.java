package graphics;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;

import graphics.buffers.IndexBufferObject;
import graphics.buffers.VertexArrayObject;
import graphics.buffers.VertexBufferObject;
import graphics.primitives.Primitive;
import graphics.primitives.Primitive.Primitives;
import graphics.primitives.VertexData;

public class Renderer {
	
	public static enum Type {
		Static(GL15.GL_STATIC_DRAW), Dynamic(GL15.GL_DYNAMIC_DRAW), Stream(GL15.GL_STREAM_DRAW);
		
		private int in_gl;
		Type(int i) {
			in_gl = i;
		}
		
		public int GLINT() {
			return in_gl;
		}
	}
	

	private int MAX_PRIMITIVES;
	private int MAX_VERT;
	private int MAX_INDEX;
	
	private Primitives primitiveType;
	
	private FloatBuffer arrayBufferData;
	private int vertex_count;
	
	private VertexArrayObject vao;
	private VertexBufferObject vbo;
	private IndexBufferObject ibo;


	/*
	 * leave staticVBOBuffer_data null if no initial draw data
	 * **/
	public Renderer(Primitives primitiveType, Type arrayRenderType, Type indexRenderType, FloatBuffer staticVBOBuffer_data, int max_primitives) {
		this.primitiveType = primitiveType;
		vertex_count = 0;
		MAX_PRIMITIVES = max_primitives;
		MAX_VERT = MAX_PRIMITIVES * primitiveType.vertex_count;
		MAX_INDEX = MAX_PRIMITIVES * primitiveType.index_count;

		// Java direct buffers
		if (staticVBOBuffer_data == null)
			arrayBufferData = BufferUtils.createFloatBuffer(MAX_VERT * VertexData.componentCount);			
		else
			arrayBufferData = staticVBOBuffer_data;
		IntBuffer indexBufferData = BufferUtils.createIntBuffer(MAX_INDEX);
		primitiveType.fillIndexBufferData(indexBufferData);
		indexBufferData.flip();
		
		// Shader buffers and attributes
		vao = new VertexArrayObject();
		vbo = new VertexBufferObject(arrayBufferData, VertexData.componentCount, arrayRenderType);
		ibo = new IndexBufferObject(indexBufferData, indexRenderType);
		VertexData.configVBO(vbo);
		vao.addBuffer(vbo);
	}

	public Renderer(Primitives primitiveType, FloatBuffer staticVBOBuffer_data) {
		this(primitiveType, Type.Static, Type.Static, staticVBOBuffer_data, staticVBOBuffer_data.capacity() / primitiveType.vertex_count);
	}

	public Renderer(Primitives primitiveType, Type arrayRenderType, Type indexRenderType, int max_primitives) {
		this(primitiveType, arrayRenderType, indexRenderType, null, max_primitives);
	}

	public Renderer(Primitives primitiveType, Type arrayRenderType, int max_primitives) {
		this(primitiveType, arrayRenderType, Type.Static, max_primitives);
	}

	public Renderer(Primitives primitiveType, Type arrayRenderType, Type indexRenderType) {
		this(primitiveType, arrayRenderType, indexRenderType, 2<<18);
	}

	public Renderer(Primitives primitiveType, Type arrayRenderType) {
		this(primitiveType, arrayRenderType, Type.Static, 2<<18);
	}

	public Renderer(Primitives primitiveType) {
		this(primitiveType, Type.Dynamic, Type.Static, 2<<18);
	}

	public Renderer(Primitives primitiveType, int max_primitives) {
		this(primitiveType, Type.Dynamic, Type.Static, max_primitives);
	}

	/*
	 * same as Renderer(Primitives primitiveType, Type arrayRenderType, Type indexRenderType, int max_primitives)
	 * with arguments Renderer(Primitives.Quad, Type.Dynamic, Type.Static, 2<<18)
	 * **/
	public Renderer() {
		this(Primitives.Quad, Type.Dynamic, Type.Static, 2<<18);
	}
	
	public void submitOverride(Primitive primitive, int index) {
		if(!vbo.isMapped()) {
			ByteBuffer buf = vbo.mapBuffer();
			arrayBufferData = buf.asFloatBuffer();
			arrayBufferData.position(vertex_count * VertexData.componentCount);
		}

		int old_pos = arrayBufferData.position();
		arrayBufferData.position(index * primitiveType.vertex_count * VertexData.componentCount);
		for (int i = 0; i < primitive.vertexData.length; i++) {
			arrayBufferData.put(primitive.vertexData[i].get());
		}
		arrayBufferData.position(old_pos);
	}
	
	public void submit(VertexData vertex) {
		if(!vbo.isMapped()) {
			ByteBuffer buf = vbo.mapBuffer();
			arrayBufferData = buf.asFloatBuffer();
			arrayBufferData.position(vertex_count * VertexData.componentCount);
		}
		
		arrayBufferData.put(vertex.get());
		
		vertex_count += primitiveType.vertex_count;
	}
	
	public void submit(Primitive primitive) {
		if(!vbo.isMapped()) {
			ByteBuffer buf = vbo.mapBuffer();
			arrayBufferData = buf.asFloatBuffer();
			arrayBufferData.position(vertex_count * VertexData.componentCount);
		}
		
		for (int i = 0; i < primitive.vertexData.length; i++) {
			arrayBufferData.put(primitive.vertexData[i].get());
		}
		
		vertex_count += primitiveType.vertex_count;
	}
	
	public void overridePrimitiveCount(int newCount) {
		vertex_count = primitiveType.vertex_count * newCount;
	}
	
	public void clear() {
		vertex_count = 0;
	}
	
	public void draw() {
		// Unmap buffer
		if (vbo.isMapped()) vbo.unmapBuffer();
		
		// Prevent 0 vertex render
		if (vertex_count == 0)
			return;

		// Binding
		vao.bind();
		vbo.bind();
		ibo.bind();
		
		// OpenGL draw call
		GL11.nglDrawElements(primitiveType.gl_primitive_type, vertex_count / primitiveType.vertex_count * primitiveType.index_count, GL11.GL_UNSIGNED_INT, 0);
	}
	
}
