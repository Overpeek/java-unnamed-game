#version 430 core

layout(binding = 0, rgba32f) uniform image2D framebuffer;

uniform vec3 eye;
uniform vec3 ray00;
uniform vec3 ray01;
uniform vec3 ray10;
uniform vec3 ray11;

uniform float slider0;
uniform float slider1;
uniform float slider2;

uniform sampler2D floor_texture;




float map(float value, float low1, float high1, float low2, float high2) {
	return low2 + (value - low1) * (high2 - low2) / (high1 - low1);
}

#define MAX_SCENE_BOUNDS 1000.0
#define NUM_BOXES 3

struct box {
	vec3 min;
	vec3 max;
	vec3 color;
	float reflectivity;
	float intensivity;
};

struct sphere {
	vec3 pos;
	float radius;
	vec3 color;
	float reflectivity;
	float intensivity;
};

const box boxes[] = {
	//	position					size					color					reflectivity	intensivity
	/* The ground */
	{ 	vec3(-5.0, -0.1, -5.0), 	vec3(5.0, 0.0, 5.0), 	vec3(1.0, 1.0, 1.0), 	0.0, 			0.0 },
	/* Box in the middle */
	{ 	vec3(-0.5, 0.0, -0.5), 		vec3(0.5, 1.0, 0.5), 	vec3(0.0, 1.0, 0.0), 	1.0, 			0.0 },
	/* Box next to the middle */
	{ 	vec3(-1.5, 0.0, -1.5), 		vec3(-0.5, 1.0, -0.5), 	vec3(0.0, 0.0, 1.0), 	1.0, 			0.0 }
};

const sphere sphere0 =
	//	position				radius	color					reflectivity	intensivity
	{ 	vec3(2.0, 1.0, -2.0), 	1.0f, 	vec3(1.0, 1.0, 1.0), 	0.0, 			1.0 };

const box light = box(vec3(slider1-0.1, slider2-0.1, -0.1), vec3(slider1+0.1, slider2+0.1, 0.1), vec3(1.0, 1.0, 1.0), 0.0, 0.0);

struct hitinfo {
	vec2 lambda;
	int bi;
};

struct rayData {
	vec3 position;
	vec3 direction;
	hitinfo last_hit;
	float energy;
};

vec2 intersectBox(inout rayData ray, const box b, out vec3 normal) {
	vec3 tMin = (b.min - ray.position) / ray.direction;
	vec3 tMax = (b.max - ray.position) / ray.direction;
	vec3 t1 = min(tMin, tMax);
	vec3 t2 = max(tMin, tMax);
	float tNear = max(max(t1.x, t1.y), t1.z);
	float tFar = min(min(t2.x, t2.y), t2.z);
	return vec2(tNear, tFar);
}

vec2 intersectSphere(inout rayData ray, const sphere s, out vec3 normal)
{
	// intersection
    vec3 oc = ray.position - s.position;
    float b = dot( oc, ray.direction );
    float c = dot( oc, oc ) - s.radius*s.radius;
    float h = b*b - c;
    if( h<0.0 ) return vec2(-1.0); // no intersection
    h = sqrt( h );
    vec2 lambda = vec2( -b-h, -b+h );

    // normal
    vec3 intersection_point = ray.position + (ray.direction * lambda.x);
    normal = normalize(intersection_point-s.position);

    return lambda;
}

bool intersectBoxes(inout rayData ray) {
	float smallest = MAX_SCENE_BOUNDS;
	bool found = false;
	for (int i = 0; i < NUM_BOXES; i++) {
		if (i == ray.last_hit.bi) continue; // skip if testing object that it just reflected off of

		vec2 lambda = intersectBox(ray, boxes[i]);
		if (lambda.x > 0.0 && lambda.x < lambda.y && lambda.x < smallest) {
			ray.last_hit.lambda = lambda;
			ray.last_hit.bi = i;
			smallest = lambda.x;
			found = true;
		}
	}
	return found;
}

bool intersectEverything(inout rayData ray) {

	vec2 lambda = intersectSphere(ray, sphere0);
	if (lambda.x > 0.0 && lambda.x < lambda.y) {
		ray.last_hit.lambda = lambda;
		ray.last_hit.bi = i;

		return true;
	}

	return false;

}

bool intersectLight(inout rayData ray) {
	float smallest = MAX_SCENE_BOUNDS;
	bool found = false;


	for (int i = 0; i < NUM_BOXES; i++) {
		if (i == ray.last_hit.bi) continue; // skip if testing object that it just reflected off of

		vec2 lambda = intersectEverything(ray, boxes[i]);
		if (lambda.x > 0.0 && lambda.x < lambda.y && lambda.x < smallest) {
			ray.last_hit.lambda = lambda;
			ray.last_hit.bi = i;
			smallest = lambda.x;
		}
	}
	vec2 lambda = intersectEverything(ray, light);
	if (lambda.x > 0.0 && lambda.x < lambda.y && lambda.x < smallest) {
		ray.last_hit.lambda = lambda;
		ray.last_hit.bi = 2;
		smallest = lambda.x;
		found = true;
	}

	return found;
}

vec4 trace(rayData ray) {
	const vec3 lightPosition = vec3(slider1, slider2, 0.0);
	const float epsilon = 0.000001;
	const int bounces = 8;
	vec4 color = vec4(1.0, 1.0, 1.0, 0.0);
	vec3 light_vector = normalize(lightPosition - ray.position);
	vec3 normal_vector = vec3(0.0, 1.0, 0.0);

	for (int b = 0; b < bounces; b++) { // for 8 bounces

		vec3 light_vector = normalize(lightPosition - ray.position);
		float light_distance = length(lightPosition - ray.position);
		{ // test for direct light
			rayData l = ray;
			if (intersectLight(l)) { // hit light source
				color = vec4(1.0, 1.0, 1.0, 1.0);
				break;
			}
		} // test for close light
		{
//			rayData l = rayData(ray.position, light_vector, ray.last_hit, 1.0);
//			if (intersectLight(l)) { // hit light source
//				float brightness = 0.0;
//				brightness = max(dot(normalize(light_vector), normalize(ray.direction)), 0.0) / light_distance;
//				color.a += brightness;
//				//color.rgb += vec3(1.0, 1.0, 1.0);
//			}
		}
		if (intersectEverything(ray)) {
			ray.position = mix(ray.position, ray.position + ray.direction, ray.last_hit.lambda.x);

			vec3 tMin = boxes[ray.last_hit.bi].min;
			vec3 tMax = boxes[ray.last_hit.bi].max;

			if (abs(ray.position.x - tMin.x) < epsilon) {
				normal_vector = vec3(-1.0, 0.0, 0.0);
			} else if (abs(ray.position.x - tMax.x) < epsilon) {
				normal_vector = vec3(1.0, 0.0, 0.0);
			} else if (abs(ray.position.y - tMin.y) < epsilon) {
				normal_vector = vec3(0.0, -1.0, 0.0);
			} else if (abs(ray.position.y - tMax.y) < epsilon) {
				normal_vector = vec3(0.0, 1.0, 0.0);
			} else if (abs(ray.position.z - tMin.z) < epsilon) {
				normal_vector = vec3(0.0, 0.0, -1.0);
			} else if (abs(ray.position.z - tMax.z) < epsilon) {
				normal_vector = vec3(0.0, 0.0, 1.0);
			}

			ray.position += normal_vector * epsilon * 2.0f;
			light_vector = normalize(lightPosition - ray.position);

			if (ray.last_hit.bi == 0) {
				color.rgb *= texture(floor_texture, (ray.position.xz + vec2(5.0, 5.0)) / 10.0).rgb * (1.0 - boxes[ray.last_hit.bi].reflectivity);
			} else {
				color.rgb *= boxes[ray.last_hit.bi].color;
			}

			// Shadow rays
			ray.direction = reflect(ray.direction, normal_vector);
			rayData lightTest = rayData(ray.position, light_vector, hitinfo(vec2(0.0),-1), 1.0);
			if (intersectLight(lightTest)) { // test for light access
				if (boxes[ray.last_hit.bi].reflectivity != 1.0f) {

					// non reflective materials
					float intensity;
					intensity = max(dot(normal_vector, light_vector), 0.0);
					intensity /= length(ray.position - lightPosition) / slider0;
					color.a += intensity / ray.energy * (1.0 - boxes[ray.last_hit.bi].reflectivity);

					// reflective materials
					// calculated after bounce

					// refraction
					// todo:

				}
			}

			ray.energy -= (1.0 - boxes[ray.last_hit.bi].reflectivity);
			if (ray.energy <= 0.0) {
				break;
			}

		} ///endif

	} ///endfor

	return vec4(color.rgb * color.a, 1.0);
}

layout (local_size_x = 16, local_size_y = 8) in;
void main(void) {
	ivec2 pix = ivec2(gl_GlobalInvocationID.xy);
	ivec2 size = imageSize(framebuffer);
	if (pix.x >= size.x || pix.y >= size.y) {
		return;
	}
	vec2 pos = vec2(pix) / vec2(size.x - 1, size.y - 1);
	vec3 dir = mix(mix(ray00, ray01, pos.y), mix(ray10, ray11, pos.y), pos.x);

	rayData ray = rayData(eye, dir, hitinfo(vec2(0.0),-1), 1.0);
	vec4 color = trace(ray);
	imageStore(framebuffer, pix, color);
}
