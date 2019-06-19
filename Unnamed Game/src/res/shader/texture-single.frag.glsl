#version 330 core
layout(location = 0) out vec4 color;

in vec2 shader_uv;
in vec4 shader_color;

uniform sampler2D tex;

void main()
{
	vec4 textureColor = texture(tex, shader_uv);
	color = textureColor;
	//color = vec4(1.0f, 0.5f, 0.0f, 1.0f);
}
