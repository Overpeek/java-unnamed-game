package graphics;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.FloatBuffer;

import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector2i;
import org.joml.Vector3f;
import org.joml.Vector3i;
import org.joml.Vector4f;
import org.joml.Vector4i;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL32;
import org.lwjgl.system.MemoryStack;

import utility.Logger;


public class Shader {
	
	private int shaderProgram;

	private int loadShader(int shadertype, String shaderText) {
		int shaderId;
		shaderId = GL20.glCreateShader(shadertype);
		GL20.glShaderSource(shaderId, shaderText);
		GL20.glCompileShader(shaderId);

		//Get errors
		shaderLog("Shader compilation failed!", shaderId, GL20.GL_COMPILE_STATUS);
		return shaderId;
	}

	private int loadShaderFile(int shadertype, String path) {
		//Load and compile
		StringBuilder shaderSource = new StringBuilder();
		try {
			InputStream is = Class.class.getResourceAsStream(path);
			BufferedReader reader = new BufferedReader(new InputStreamReader(is));
			String line;
			
			while((line = reader.readLine()) != null) {
				shaderSource.append(line).append("\n");
			}
			
			reader.close();
		} catch (Exception e) {
			Logger.out("Shader file could not be loaded! (" + path +")", Logger.type.ERROR);
		}
		
		return loadShader(shadertype, shaderSource.toString());
	}

	private void shaderLog(String text, int shaderId, int type) {
		int success[] = new int[1];
		GL20.glGetShaderiv(shaderId, type, success);
		if (success[0] == 0)
		{
			String infoLogString = GL20.glGetShaderInfoLog(shaderId);
			Logger.out(text + "\n" + infoLogString, Logger.type.ERROR);
		}
	}

	private void programLog(String text, int program, int type) {
		int success[] = new int[1];
		GL20.glGetProgramiv(program, type, success);
		if (success[0] == 0)
		{
			String infoLogString = GL20.glGetProgramInfoLog(program);
			Logger.out(text + "\n" + infoLogString, Logger.type.ERROR);
		}
	}

	public Shader() {
		//Vertex shader
		int vertexShader = loadShader(GL20.GL_VERTEX_SHADER, 
			"#version 330 core\n" +
			"layout(location = 0) in vec3 vertex_pos;\n" +
			"layout(location = 1) in vec2 texture_uv;\n" +
			"layout(location = 2) in float texture_id;\n" +
			"layout(location = 3) in vec4 vertex_color;\n" +

			"out vec2 shader_uv;\n" +
			"out vec4 shader_color;\n" +
			"flat out int shader_id;\n" +
			"\n" +
			"uniform mat4 pr_matrix = mat4(1.0);\n" +
			"uniform mat4 ml_matrix = mat4(1.0);\n" +
			"uniform mat4 vw_matrix = mat4(1.0);\n" +
			"\n" +
			"void main()\n" +
			"{\n" +
			"   mat4 mvp = pr_matrix * vw_matrix * ml_matrix;\n" +
			"	gl_Position = mvp * vec4(vertex_pos.x, vertex_pos.y, vertex_pos.z, 1.0f);\n" +
			"	shader_uv = texture_uv;\n" +
			"	shader_id = int(floor(texture_id));\n" +
			"	shader_color = vertex_color;\n" +
			"}\n"
		);

		//Fragment shader
		int fragmentShader = loadShader(GL20.GL_FRAGMENT_SHADER,
			"#version 330 core\n" +
			"\n" +
			"in vec2 shader_uv;\n" +
			"in vec4 shader_color;\n" +
			"flat in int shader_id;\n" +
			"\n" +
			"layout(location = 0) out vec4 color;\n" +
			"\n" +
			"uniform sampler2DArray tex;\n" +
			"uniform int textured = 0;\n" +
			"\n" +
			"void main()\n" +
			"{\n" +
			"	if (textured != 0) color = texture(tex, vec3(shader_uv, shader_id)) * shader_color;\n" +
			"	else color = shader_color;\n" +
			"}\n"
		);
		
		//Shader program
		shaderProgram = GL20.glCreateProgram();
		GL20.glAttachShader(shaderProgram, vertexShader);
		GL20.glAttachShader(shaderProgram, fragmentShader);
		GL20.glLinkProgram(shaderProgram);

		//Get shader program linking error
		programLog("Shader program linking failed!", shaderProgram, GL20.GL_LINK_STATUS);

		//Default projection matrix
		Matrix4f pr = new Matrix4f().ortho2D(-1.0f, 1.0f, 1.0f, -1.0f);
		enable();
		setUniformMat4("pr_matrix", pr);

		//Free up data
		GL20.glDeleteShader(vertexShader);
		GL20.glDeleteShader(fragmentShader);
	}

	public Shader(String vertexPath, String fragmentPath) {

		//Vertex shader
		int vertexShader = loadShaderFile(GL20.GL_VERTEX_SHADER, vertexPath);

		//Fragment shader
		int fragmentShader = loadShaderFile(GL20.GL_FRAGMENT_SHADER, fragmentPath);


		//Shader program
		shaderProgram = GL20.glCreateProgram();
		GL20.glAttachShader(shaderProgram, vertexShader);
		GL20.glAttachShader(shaderProgram, fragmentShader);
		GL20.glLinkProgram(shaderProgram);


		//Get shader program linking error
		programLog("Shader program linking failed!", shaderProgram, GL20.GL_LINK_STATUS);

		//Free up data
		GL20.glDeleteShader(vertexShader);
		GL20.glDeleteShader(fragmentShader);
	}

	public Shader(String vertexPath, String fragmentPath, String geometryPath) {

		//Vertex shader
		int vertexShader = loadShaderFile(GL20.GL_VERTEX_SHADER, vertexPath);

		//Fragment shader
		int fragmentShader = loadShaderFile(GL20.GL_FRAGMENT_SHADER, fragmentPath);

		//Geometry shader
		int geometryShader = loadShaderFile(GL32.GL_GEOMETRY_SHADER, geometryPath);

		//Shader program
		shaderProgram = GL20.glCreateProgram();
		GL20.glAttachShader(shaderProgram, vertexShader);
		GL20.glAttachShader(shaderProgram, fragmentShader);
		GL20.glAttachShader(shaderProgram, geometryShader);
		GL20.glLinkProgram(shaderProgram);

		//Get shader program linking error
		programLog("Shader program linking failed!", shaderProgram, GL20.GL_LINK_STATUS);

		//Free up data
		GL20.glDeleteShader(vertexShader);
		GL20.glDeleteShader(fragmentShader);
		GL20.glDeleteShader(geometryShader);
	}


	public void enable() { GL20.glUseProgram(shaderProgram); }
	public void disable() { GL20.glUseProgram(0); }
	
	public int getUniformLocation(String name) { return GL20.glGetUniformLocation(shaderProgram, name); }
	
	public void setUniform1f(String name, float value) { enable(); GL20.glUniform1f(getUniformLocation(name), value); }
	public void setUniform2f(String name, Vector2f value) { enable(); GL20.glUniform2f(getUniformLocation(name), value.x, value.y); }
	public void setUniform3f(String name, Vector3f value) { enable(); GL20.glUniform3f(getUniformLocation(name), value.x, value.y, value.z); }
	public void setUniform4f(String name, Vector4f value) { enable(); GL20.glUniform4f(getUniformLocation(name), value.x, value.y, value.z, value.w); }
	public void setUniform1i(String name, int value) { enable(); GL20.glUniform1i(getUniformLocation(name), value); }
	public void setUniform2i(String name, Vector2i value) { enable(); GL20.glUniform2i(getUniformLocation(name), value.x, value.y); }
	public void setUniform3i(String name, Vector3i value) { enable(); GL20.glUniform3i(getUniformLocation(name), value.x, value.y, value.z); }
	public void setUniform4i(String name, Vector4i value) { enable(); GL20.glUniform4i(getUniformLocation(name), value.x, value.y, value.z, value.w); }
	public void setUniformMat4(String name, Matrix4f value) { enable();
		try (MemoryStack stack = MemoryStack.stackPush()) {
			FloatBuffer fb = value.get(stack.mallocFloat(16));
			GL20.glUniformMatrix4fv(getUniformLocation(name), false, fb); 
		}
	}
	
}
