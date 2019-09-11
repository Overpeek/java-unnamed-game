package logic;

import graphics.Shader;
import graphics.Shader.ShaderException;
import graphics.TextLabelTexture;
import utility.mat4;

public class ShaderManager {
	
	public static Shader post_processing_shader = null;
	public static Shader single_texture_shader = null;
	public static Shader multi_texture_shader = null;

	public static Shader world_multi_texture_shader = null;

	public static Shader left_align_single_texture_shader = null;
	
	public static void loadShaders() throws ShaderException {
		post_processing_shader = Shader.loadFromSources("res/shader/postprocess.vert.glsl", "res/shader/postprocess.frag.glsl", true);
		single_texture_shader = Shader.singleTextureShader();
		multi_texture_shader = Shader.multiTextureShader();
		left_align_single_texture_shader = Shader.singleTextureShader();
		world_multi_texture_shader = Shader.multiTextureShader();
	}
	
	public static void resize(int width, int height) {
		if (height == 0) height = 1;
		float aspect_rato = (float)width / (float)height;
		
		// aspect
		mat4 pr_matrix = new mat4().ortho(-aspect_rato, aspect_rato, 1.0f, -1.0f);
		single_texture_shader.setUniformMat4("pr_matrix", pr_matrix);
		multi_texture_shader.setUniformMat4("pr_matrix", pr_matrix);
		world_multi_texture_shader.setUniformMat4("pr_matrix", pr_matrix);
		TextLabelTexture.getDefaultShader().setUniformMat4("pr_matrix", pr_matrix);

		// cube
		pr_matrix = new mat4().ortho(-1.0f, 1.0f, 1.0f, -1.0f);
		post_processing_shader.setUniformMat4("pr_matrix", pr_matrix);

		// left
		pr_matrix = new mat4().ortho(0.0f, aspect_rato * 2.0f, 1.0f, -1.0f);
		left_align_single_texture_shader.setUniformMat4("pr_matrix", pr_matrix);

		// usetex
		post_processing_shader.setUniform1i("usetex", 1);
		single_texture_shader.setUniform1i("usetex", 1);
		multi_texture_shader.setUniform1i("usetex", 1);
		left_align_single_texture_shader.setUniform1i("usetex", 1);

	}

}
