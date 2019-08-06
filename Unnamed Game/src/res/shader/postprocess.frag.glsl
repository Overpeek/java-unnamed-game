#version 400 core

layout(location = 0) out vec4 color;


in vec2 shader_uv;
in vec4 shader_color;
flat in int shader_id;

uniform sampler2D unif_texture;
uniform int unif_effect = 0;
uniform float unif_t = 0;
uniform float unif_effect_scale = 1.0f;

float gauss_kernel[] = float[](0.035822f, 0.05879f, 0.086425f, 0.113806f, 0.13424f, 0.141836f, 0.13424f, 0.113806f, 0.086425f, 0.05879f, 0.035822f);

float edge_kernel[] = float[](
	-1, -1, -1,
	-1,  8, -1,
	-1, -1, -1
);

float pixelSizeX;
float pixelSizeY;

float rand(vec2 co) {
	return fract(sin(dot(co.xy, vec2(12.9898, 78.233))) * 43758.5453);
}

void main()
{
	pixelSizeX = 1.0 / textureSize(unif_texture, 0).x;
	pixelSizeY = 1.0 / textureSize(unif_texture, 0).y;
	if (unif_effect == 1) {
		color = vec4(0.0);

		//Horitzontal gaussian blur
		for (int i = -5; i < 6; i++) {
			float ioff = pixelSizeX * i;
			color += texture(unif_texture, vec2(shader_uv.x + ioff, shader_uv.y)) * gauss_kernel[i + 5];
		}
	}
	else if (unif_effect == 2) {
		color = vec4(0.0);

		//Vertical gaussian blur
		for (int i = -5; i < 6; i++) {
			float ioff = pixelSizeY * i;
			color += texture(unif_texture, vec2(shader_uv.x, shader_uv.y + ioff)) * gauss_kernel[i + 5];
		}
	}
	else if (unif_effect == 3) {
		//Inverted colors
		color = vec4(1.0, 1.0, 1.0, 2.0) - texture(unif_texture, shader_uv);
	}
	else if (unif_effect == 4) {
		//Edge detection
		color += texture(unif_texture, vec2(shader_uv.x - pixelSizeX, shader_uv.y - pixelSizeY));
		color += texture(unif_texture, vec2(shader_uv.x - pixelSizeX, shader_uv.y));
		color += texture(unif_texture, vec2(shader_uv.x - pixelSizeX, shader_uv.y + pixelSizeY));
		color += 0;// texture(unif_texture, vec2(UV.x, UV.y - pixelSizeY));
		color += 0;// texture(unif_texture, vec2(UV.x, UV.y));
		color += 0;// texture(unif_texture, vec2(UV.x, UV.y + pixelSizeY));
		color += -texture(unif_texture, vec2(shader_uv.x + pixelSizeX, shader_uv.y - pixelSizeY));
		color += -texture(unif_texture, vec2(shader_uv.x + pixelSizeX, shader_uv.y));
		color += -texture(unif_texture, vec2(shader_uv.x + pixelSizeX, shader_uv.y + pixelSizeY));
	}
	else if (unif_effect == 5) {
		float time = unif_t * unif_effect_scale;
		float size = unif_effect_scale;
		color = texture(unif_texture, vec2(shader_uv.x + sin(shader_uv.y * 20.0f + time) * size / 100.0f, shader_uv.y + sin(shader_uv.x * 10.0f + time) * size / 40.0f));
		color.a = 1.0;
	}
	else {
		//Normal
		//color = vec4(1.0f, 0.0f, 0.5f, 1.0f);
		color = texture(unif_texture, shader_uv);
	}

	color *= shader_color;
}
