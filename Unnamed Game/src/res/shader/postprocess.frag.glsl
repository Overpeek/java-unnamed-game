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

float rand(float n) {
	return fract(sin(n) * 43758.5453123);
}

vec4 permute(vec4 x){return mod(((x*34.0)+1.0)*x, 289.0);}
vec4 taylorInvSqrt(vec4 r){return 1.79284291400159 - 0.85373472095314 * r;}

float noise(float p){
	float fl = floor(p);
	float fc = fract(p);
	return mix(rand(fl), rand(fl + 1.0), fc);
}

float simplex(vec3 v){
	  const vec2  C = vec2(1.0/6.0, 1.0/3.0) ;
	  const vec4  D = vec4(0.0, 0.5, 1.0, 2.0);

	// First corner
	  vec3 i  = floor(v + dot(v, C.yyy) );
	  vec3 x0 =   v - i + dot(i, C.xxx) ;

	// Other corners
	  vec3 g = step(x0.yzx, x0.xyz);
	  vec3 l = 1.0 - g;
	  vec3 i1 = min( g.xyz, l.zxy );
	  vec3 i2 = max( g.xyz, l.zxy );

	  //  x0 = x0 - 0. + 0.0 * C
	  vec3 x1 = x0 - i1 + 1.0 * C.xxx;
	  vec3 x2 = x0 - i2 + 2.0 * C.xxx;
	  vec3 x3 = x0 - 1. + 3.0 * C.xxx;

	// Permutations
	  i = mod(i, 289.0 );
	  vec4 p = permute( permute( permute(
				 i.z + vec4(0.0, i1.z, i2.z, 1.0 ))
			   + i.y + vec4(0.0, i1.y, i2.y, 1.0 ))
			   + i.x + vec4(0.0, i1.x, i2.x, 1.0 ));

	// Gradients
	// ( N*N points uniformly over a square, mapped onto an octahedron.)
	  float n_ = 1.0/7.0; // N=7
	  vec3  ns = n_ * D.wyz - D.xzx;

	  vec4 j = p - 49.0 * floor(p * ns.z *ns.z);  //  mod(p,N*N)

	  vec4 x_ = floor(j * ns.z);
	  vec4 y_ = floor(j - 7.0 * x_ );    // mod(j,N)

	  vec4 x = x_ *ns.x + ns.yyyy;
	  vec4 y = y_ *ns.x + ns.yyyy;
	  vec4 h = 1.0 - abs(x) - abs(y);

	  vec4 b0 = vec4( x.xy, y.xy );
	  vec4 b1 = vec4( x.zw, y.zw );

	  vec4 s0 = floor(b0)*2.0 + 1.0;
	  vec4 s1 = floor(b1)*2.0 + 1.0;
	  vec4 sh = -step(h, vec4(0.0));

	  vec4 a0 = b0.xzyw + s0.xzyw*sh.xxyy ;
	  vec4 a1 = b1.xzyw + s1.xzyw*sh.zzww ;

	  vec3 p0 = vec3(a0.xy,h.x);
	  vec3 p1 = vec3(a0.zw,h.y);
	  vec3 p2 = vec3(a1.xy,h.z);
	  vec3 p3 = vec3(a1.zw,h.w);

	//Normalise gradients
	  vec4 norm = taylorInvSqrt(vec4(dot(p0,p0), dot(p1,p1), dot(p2, p2), dot(p3,p3)));
	  p0 *= norm.x;
	  p1 *= norm.y;
	  p2 *= norm.z;
	  p3 *= norm.w;

	// Mix final noise value
	  vec4 m = max(0.6 - vec4(dot(x0,x0), dot(x1,x1), dot(x2,x2), dot(x3,x3)), 0.0);
	  m = m * m;
	  return 42.0 * dot( m*m, vec4( dot(p0,x0), dot(p1,x1),
									dot(p2,x2), dot(p3,x3) ) );
}

float octave_simplex(vec3 x, int octaves) {
	float v = 0.0;
	float a = 0.5;
	vec3 shift = vec3(100);
	for (int i = 0; i < octaves; ++i) {
		v += a * simplex(x);
		x = x * 2.0 + shift;
		a *= 0.5;
	}
	return v;
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
	else if (unif_effect == 6) {
		float height = (octave_simplex(vec3(shader_uv * 2.0f, unif_t * 0.1f), 4) + 1.0f) / 2.0f;

		vec3 color0 = vec3(1.0f, 0.0f, 0.5f);
		vec3 color1 = vec3(0.1f, 0.5f, 1.0f);

		color.rgb = mix(color0, color1, height);
		color.a = 1.0;
	}
	else if (unif_effect == 7) {
		vec2 position = shader_uv;
//		position.x += simplex(vec3(unif_t * 0.0001f, -100.0f, 0.0f)) * 50.0f;
//		position.y += simplex(vec3(unif_t * 0.0001f,  100.0f, 0.0f)) * 50.0f;

		float height = simplex(vec3(position * 120.0f, unif_t * 0.002f));
		if (height > 0.85f) {
			float color_randomizer = (simplex(vec3(position * 80.0f, 234.534f)) + 1.0f) / 2.0f;
			float color_randomizer2 = (simplex(vec3(position * 80.0f, -894.54f)) + 1.0f) / 2.0f;

			vec3 color_0 = vec3(1.0f, 0.0f, 0.0f);
			vec3 color_1 = vec3(0.0f, 0.0f, 1.0f);
			vec3 color_2 = vec3(1.0f, 1.0f, 1.0f);

			color.rgb = mix(color_2, mix(color_0, color_1, color_randomizer), color_randomizer2);
		}
		else color.rgb = vec3(0.0f);
		color.a = 1.0;
	}
	else {
		//Normal
		//color = vec4(1.0f, 0.0f, 0.5f, 1.0f);
		color = texture(unif_texture, shader_uv);
	}

	color *= shader_color;
}
