#version 330 core

layout(location = 0) out vec4 color;

in vec2 shader_uv;
in vec4 shader_color;
flat in int shader_id;

uniform sampler2DArray tex;
uniform vec2 mouse = vec2(0.0, 0.0);
uniform float brightness = 100.0f;


void main()
{
	color = texture(tex, vec3(shader_uv, shader_id)) * shader_color;


	float x_d = gl_FragCoord.x - mouse.x;
	float y_d = gl_FragCoord.y - mouse.y;

	float d = sqrt((x_d)*(x_d) + (y_d)*(y_d));
	//float d = abs(x_d) + abs(y_d);
	if (d > 0.01) {
		color.x *= 1.0f + (brightness / d);
		color.y *= 1.0f + (brightness / d);
		color.z *= 1.0f + (brightness / d);
	}

	//color = vec4(1.0f, 0.5f, 0.0f, 1.0f);
}
