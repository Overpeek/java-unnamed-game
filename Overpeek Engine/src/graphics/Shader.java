package graphics;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL32;

import utility.Loader;
import utility.Logger;
import utility.mat4;
import utility.vec2;
import utility.vec3;
import utility.vec4;


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
			InputStream is = Loader.loadRes(path);
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
			"#version 330 core\r\n" + 
			"layout(location = 0) in vec3 vertex_pos;\r\n" + 
			"layout(location = 1) in vec2 texture_uv;\r\n" + 
			"layout(location = 2) in float texture_id;\r\n" + 
			"layout(location = 3) in vec4 vertex_color;\r\n" + 
			"\r\n" + 
			"out vec2 shader_uv;\r\n" + 
			"out vec4 shader_color;\r\n" + 
			"\r\n" + 
			"uniform mat4 pr_matrix;\r\n" + 
			"\r\n" + 
			"void main()\r\n" + 
			"{\r\n" + 
			"	gl_Position = pr_matrix * vec4(vertex_pos.x, vertex_pos.y, vertex_pos.z, 1.0f);\r\n" + 
			"	shader_uv = texture_uv;\r\n" + 
			"	shader_color = vertex_color;\r\n" + 
			"}\r\n"
		);

		//Fragment shader
		int fragmentShader = loadShader(GL20.GL_FRAGMENT_SHADER,
			"#version 330 core\r\n" + 
			"layout(location = 0) out vec4 color;\r\n" + 
			"\r\n" + 
			"in vec2 shader_uv;\r\n" + 
			"in vec4 shader_color;\r\n" + 
			"\r\n" + 
			"uniform sampler2D tex;\r\n" + 
			"uniform int usetex = 0;\r\n" + 
			"\r\n" + 
			"void main()\r\n" + 
			"{\r\n" + 
			"	vec4 textureColor = vec4(1.0f, 0.0f, 1.0f, 1.0f);\r\n" + 
			"	if (usetex == 0)\r\n" + 
			"		textureColor = shader_color;\r\n" + 
			"	else\r\n" + 
			"		textureColor = shader_color * texture(tex, shader_uv);\r\n" + 
			"\r\n" + 
			"	color = textureColor;\r\n" + 
			"}\r\n"
		);
		
		//Shader program
		shaderProgram = GL20.glCreateProgram();
		GL20.glAttachShader(shaderProgram, vertexShader);
		GL20.glAttachShader(shaderProgram, fragmentShader);
		GL20.glLinkProgram(shaderProgram);

		//Get shader program linking error
		programLog("Shader program linking failed!", shaderProgram, GL20.GL_LINK_STATUS);

		//Default projection matrix
		mat4 pr = new mat4().ortho(-1.0f, 1.0f, 1.0f, -1.0f);
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
	public void setUniform2f(String name, vec2 value) { enable(); GL20.glUniform2f(getUniformLocation(name), value.x, value.y); }
	public void setUniform3f(String name, vec3 value) { enable(); GL20.glUniform3f(getUniformLocation(name), value.x, value.y, value.z); }
	public void setUniform4f(String name, vec4 value) { enable(); GL20.glUniform4f(getUniformLocation(name), value.x, value.y, value.z, value.w); }
	public void setUniform1i(String name, int value) { enable(); GL20.glUniform1i(getUniformLocation(name), value); }
	public void setUniform2i(String name, vec2 value) { enable(); GL20.glUniform2i(getUniformLocation(name), (int)value.x, (int)value.y); }
	public void setUniform3i(String name, vec3 value) { enable(); GL20.glUniform3i(getUniformLocation(name), (int)value.x, (int)value.y, (int)value.z); }
	public void setUniform4i(String name, vec4 value) { enable(); GL20.glUniform4i(getUniformLocation(name), (int)value.x, (int)value.y, (int)value.z, (int)value.w); }
	public void setUniformMat4(String name, mat4 value) { enable();	GL20.glUniformMatrix4fv(getUniformLocation(name), false, value.getAsBuffer()); }
	
}
