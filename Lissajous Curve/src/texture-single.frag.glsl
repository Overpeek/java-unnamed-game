#version 330 core
layout(location = 0) out vec4 color;

in vec2 shader_uv;
in vec4 shader_color;

uniform sampler2D tex;
uniform int usetex = 0;

void main()
{
	vec4 textureColor = vec4(1.0f, 0.0f, 1.0f, 1.0f);
	if (usetex == 0)
		textureColor = shader_color;
	else
		textureColor = shader_color * texture(tex, shader_uv);

	color = textureColor;
}
