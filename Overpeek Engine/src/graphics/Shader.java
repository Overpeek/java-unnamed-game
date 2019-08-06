package graphics;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL32;
import org.lwjgl.opengl.GL40;

import utility.Loader;
import utility.Logger;
import utility.mat4;
import utility.vec2;
import utility.vec3;
import utility.vec4;


public class Shader {
	
	private static Window window = null;

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
	
	public static Shader singleTextureShader() {
		String vertsource = "#version 330 core\n" +
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
							"}\n";
		
		String fragsource = "#version 330 core\n" +
							"\n" +
							"in vec2 shader_uv;\n" +
							"in vec4 shader_color;\n" +
							"flat in int shader_id;\n" +
							"\n" +
							"layout(location = 0) out vec4 color;\n" +
							"\n" +
							"uniform sampler2D tex;\n" +
							"uniform int usetex = 1;\n" +
							"\n" +
							"void main()\n" +
							"{\n" +
							"	if (usetex != 0) color = texture(tex, vec2(shader_uv)) * shader_color;\n" +
							"	else color = shader_color;\n" +
							"}\n";
		
		return loadFromSources(vertsource, fragsource, false);
	}
	
	public static Shader multiTextureShader() {
		String vertsource = "#version 330 core\n" +
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
							"}\n";

		String fragsource = "#version 330 core\n" +
							"\n" +
							"in vec2 shader_uv;\n" +
							"in vec4 shader_color;\n" +
							"flat in int shader_id;\n" +
							"\n" +
							"layout(location = 0) out vec4 color;\n" +
							"\n" +
							"uniform sampler2DArray tex;\n" +
							"uniform int usetex = 1;\n" +
							"\n" +
							"void main()\n" +
							"{\n" +
							"	if (usetex != 0) color = texture(tex, vec3(shader_uv, shader_id)) * shader_color;\n" +
							"	else color = shader_color;\n" +
							"}\n";
		
		return loadFromSources(vertsource, fragsource, false);
	}
	
	/**
	 * usePath - use String filepaths("/res/shader.glsl") instead of Strings containing sources("#version 330 core\n" + "etc")
	 * */
	public static Shader loadFromSources(String vertex, String fragment, boolean usePath) {
		Shader shader = new Shader();
		
		//Vertex shader
		int vertexShader = 0;
		if (usePath)
			vertexShader = shader.loadShaderFile(GL20.GL_VERTEX_SHADER, vertex);
		else
			vertexShader = shader.loadShader(GL20.GL_VERTEX_SHADER, vertex);

		//Fragment shader
		int fragmentShader = 0;
		if (usePath)
			fragmentShader =  shader.loadShaderFile(GL20.GL_FRAGMENT_SHADER, fragment);
		else
			fragmentShader =  shader.loadShader(GL20.GL_FRAGMENT_SHADER, fragment);
		
		//Shader program
		shader.shaderProgram = GL20.glCreateProgram();
		GL20.glAttachShader(shader.shaderProgram, vertexShader);
		GL20.glAttachShader(shader.shaderProgram, fragmentShader);
		GL20.glLinkProgram(shader.shaderProgram);

		//Get shader program linking error
		shader.programLog("Shader program linking failed!", shader.shaderProgram, GL20.GL_LINK_STATUS);

		//Default projection matrix
		shader.defaultOrthoMatrix();

		//Free up data
		GL20.glDeleteShader(vertexShader);
		GL20.glDeleteShader(fragmentShader);
		
		return shader;
	}
	
	//To get aspect for default ortho matrix
	public static void setActiveWindow(Window _window) {
		window = _window;
	}
	
	/**
	 * 	left: -aspect, right: aspect, top: -1.0, bottom: 1.0, if fails returns left: -1.0, right: 1.0, top: -1.0, bottom: 1.0
	 * */
	public void defaultOrthoMatrix() {
		mat4 pr;
		if (window == null) {
			pr = new mat4().ortho(-window.getAspect(), window.getAspect(), 1.0f, -1.0f);
		}
		else {
			pr = new mat4().ortho(-1.0f, 1.0f, 1.0f, -1.0f);
		}
		
		setUniformMat4("pr_matrix", pr);
	}

	/**
	 * usePath - use String filepaths("/res/shader.glsl") instead of Strings containing sources("#version 330 core\n" + "etc")
	 * */
	public static Shader loadFromSources(String vertex, String fragment, String geometry, boolean usePath) {
		Shader shader = new Shader();
		
		//Vertex shader
		int vertexShader = 0;
		if (usePath)
			vertexShader = shader.loadShaderFile(GL20.GL_VERTEX_SHADER, vertex);
		else
			vertexShader = shader.loadShader(GL20.GL_VERTEX_SHADER, vertex);

		//Fragment shader
		int fragmentShader = 0;
		if (usePath)
			fragmentShader =  shader.loadShaderFile(GL20.GL_FRAGMENT_SHADER, fragment);
		else
			fragmentShader =  shader.loadShader(GL20.GL_FRAGMENT_SHADER, fragment);

		//Geometry shader
		int geometryShader = 0;
		if (usePath)
			geometryShader =  shader.loadShaderFile(GL32.GL_GEOMETRY_SHADER, geometry);
		else
			geometryShader =  shader.loadShader(GL32.GL_GEOMETRY_SHADER, geometry);
		
		//Shader program
		shader.shaderProgram = GL20.glCreateProgram();
		GL20.glAttachShader(shader.shaderProgram, vertexShader);
		GL20.glAttachShader(shader.shaderProgram, fragmentShader);
		GL20.glAttachShader(shader.shaderProgram, geometryShader);
		GL20.glLinkProgram(shader.shaderProgram);

		//Get shader program linking error
		shader.programLog("Shader program linking failed!", shader.shaderProgram, GL20.GL_LINK_STATUS);

		//Default projection matrix
		shader.defaultOrthoMatrix();

		//Free up data
		GL20.glDeleteShader(vertexShader);
		GL20.glDeleteShader(fragmentShader);
		GL20.glDeleteShader(geometryShader);
		
		return shader;
	}
	
	private Shader() {}


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
