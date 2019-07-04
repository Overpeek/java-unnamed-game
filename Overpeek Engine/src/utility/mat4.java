package utility;

import java.nio.FloatBuffer;

import org.joml.Matrix4f;
import org.lwjgl.system.MemoryStack;

public class mat4 {

	private Matrix4f matrix;
	
	public mat4() {
		matrix = new Matrix4f();
	}
	
	public mat4 rotateX(float x) {
		matrix = matrix.rotateX(x);
		return this;
	}
	
	public mat4 rotateZ(float z) {
		matrix = matrix.rotateZ(z);
		return this;
	}
	
	public mat4 rotateY(float y) {
		matrix = matrix.rotateY(y);
		return this;
	}
	
	public mat4 add(mat4 other) {
		matrix = matrix.add(other.matrix);
		return this;
	}
	
	public mat4 mult(mat4 other) {
		matrix = matrix.mul(other.matrix);
		return this;
	}
	
	public mat4 ortho(float left, float right, float bottom, float top) {
		matrix = matrix.ortho2D(left, right, bottom, top);
		return this;
	}
	
	public mat4 perspectiveRad(float fow, float aspect) {
		matrix = matrix.perspective(fow, aspect, 0.01f, 1000.0f);
		return this;
	}
	
	public mat4 perspectiveDeg(float fow, float aspect) {
		matrix = matrix.perspective(fow * (float)(Math.PI/180.0f), aspect, 0.01f, 1000.0f);
		return this;
	}
	
	public FloatBuffer getAsBuffer() {
		FloatBuffer fb;
		try (MemoryStack stack = MemoryStack.stackPush()) {
			 fb = matrix.get(stack.mallocFloat(16));
		}
		return fb;
	}
	
	public mat4 lookAt(vec3 eye, vec3 look, vec3 up) {
		matrix = matrix.lookAt(eye.x, eye.y, eye.z, look.x, look.y, look.z, up.x, up.y, up.z);
		return this;
	}
	
	public mat4 lookAt(float eyeX, float eyeY, float eyeZ, float lookX, float lookY, float lookZ, float upX, float upY, float upZ) {
		matrix = matrix.lookAt(eyeX, eyeY, eyeZ, lookX, lookY, lookZ, upX, upY, upZ);
		return this;
	}
	
}
