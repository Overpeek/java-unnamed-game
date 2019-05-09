#version 330 core

layout(location = 0) out vec4 color;

in vec2 shader_uv;
in vec4 shader_color;
flat in int shader_id;

uniform sampler2DArray tex;


void main()
{
	color = texture(tex, vec3(shader_uv, shader_id)) * shader_color;
	//color = vec4(1.0f, 0.5f, 0.0f, 1.0f);
}
