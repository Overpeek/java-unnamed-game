#version 330 core
layout(location = 0) in vec2 vertex_pos;
layout(location = 1) in vec2 texture_uv;
layout(location = 2) in float texture_id;
layout(location = 3) in vec4 vertex_color;

out vec2 shader_uv;
out vec4 shader_color;

uniform mat4 pr_matrix;

void main()
{
	gl_Position = pr_matrix * vec4(vertex_pos.x, vertex_pos.y, 0.0f, 1.0f);
	shader_uv = texture_uv;
	shader_color = vertex_color;
}
