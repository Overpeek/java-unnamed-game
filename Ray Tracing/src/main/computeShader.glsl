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
uniform float time;

uniform sampler2D floor_texture;




//#define DRAW_NORMALS
#define MAX_SCENE_BOUNDS 1000.0
#define NUM_OBJECTS 6

float map(float value, float low1, float high1, float low2, float high2) {
	return low2 + (value - low1) * (high2 - low2) / (high1 - low1);
}

struct hitinfo {
	vec2 lambda;
	int bi;
	vec3 position;
};

hitinfo newHitInfo() {
	return hitinfo(vec2(0.0), -1, vec3(0.0));
}

struct rayData {
	vec3 position;
	vec3 direction;
	hitinfo last_hit;
};

struct object {

	vec3 argument1;	// min or center
	vec3 argument2; // max or radius for sphere
	vec3 color;
	float reflectivity;
	float intensivity;

	int type; // 0 sphere, 1 box, ...

};

const object objects[] = {

	//	position						size					color					reflectivity	intensivity		type
	{ 	vec3(-5.0, -2.0, -5.0), 		vec3(5.0, -1.0, 5.0), 	vec3(1.0, 1.0, 1.0), 	0.0, 			0.0,			1 }, 	/* The ground */
	{ 	vec3(-0.5, 0.0, -0.5), 			vec3(0.5, 1.0, 0.5), 	vec3(0.0, 1.0, 0.0), 	1.0, 			0.0,			1 }, 	/* Box in the middle */
	{ 	vec3(-1.5, 0.0, -1.5), 			vec3(-0.5, 1.0, -0.5), 	vec3(0.0, 0.0, 1.0), 	1.0, 			0.0,			1 }, 	/* Box next to the middle */
	{ 	vec3( 2.0, sin(time), -2.0), 	vec3(0.5), 				vec3(1.0, 1.0, 1.0), 	1.0, 			0.0,			0 }, 	/* Box next to the middle */
	{ 	vec3( 2.0, cos(time), 2.0), 	vec3(0.5), 				vec3(1.0, 1.0, 1.0), 	1.0, 			1.0,			0 }, 	/* Box next to the middle */
	{ 	vec3(-2.0, sin(time*2), -2.0), 	vec3(0.5), 				vec3(1.0, 1.0, 1.0), 	1.0, 			0.0,			0 }, 	/* Box next to the middle */

};

vec2 intersectBox(inout rayData ray, const object b) {
	vec3 tMin = (b.argument1 - ray.position) / ray.direction;
	vec3 tMax = (b.argument2 - ray.position) / ray.direction;
	vec3 t1 = min(tMin, tMax);
	vec3 t2 = max(tMin, tMax);
	float tNear = max(max(t1.x, t1.y), t1.z);
	float tFar = min(min(t2.x, t2.y), t2.z);
	return vec2(tNear, tFar);
}

vec3 boxNormal(vec3 position, const object b)
{
	vec3 c = (b.argument1 + b.argument2) * 0.5;
	vec3 p = position - c;
	vec3 d = (b.argument1 - b.argument2) * 0.5;
	float bias = 1.0001;

	vec3 result = vec3(
			int(p.x / abs(d.x) * bias),
	        int(p.y / abs(d.y) * bias),
	        int(p.z / abs(d.z) * bias)
	);

    return normalize(result);
}

vec2 intersectSphere(inout rayData ray, const object s)
{
	vec3 ro = ray.position;
	vec3 rd = ray.direction;
	vec3 ce = s.argument1;
	float ra = s.argument2.x;

    vec3 oc = ro - ce;
    float b = dot( oc, rd );
    float c = dot( oc, oc ) - ra*ra;
    float h = b*b - c;
    if( h<0.0 ) return vec2(-1.0, 0.0); // no intersection
    h = sqrt( h );
    return vec2( -b-h, -b+h );
}

vec3 sphereNormal(vec3 position, const object s) {
    return normalize(position-s.argument1);
}

vec2 intersectObject(inout rayData ray, const object obj) {

	switch(obj.type) {
	case 0: // sphere
		return intersectSphere(ray, obj);
		break;
	case 1: // box
		return intersectBox(ray, obj);
		break;
	}

	return vec2(-1);
}

vec3 objectNormal(vec3 position, const object obj) {

	switch(obj.type) {
	case 0: // sphere
		return sphereNormal(position, obj);
		break;
	case 1: // box
		return boxNormal(position, obj);
		break;
	}

	return vec3(0);
}

vec3 objectCenter(const object obj) {
	switch(obj.type) {
	case 0: // sphere
		return obj.argument1;
		break;
	case 1: // box
		return (obj.argument1 + obj.argument2) * 0.5;
		break;
	}

	return vec3(0);
}

bool intersect(inout rayData ray, out vec3 normal) {
	float smallest = MAX_SCENE_BOUNDS;
	bool found = false;

	// Intersections
	for (int i = 0; i < NUM_OBJECTS; i++) {
		if (i == ray.last_hit.bi) continue; // skip if testing object that it just reflected off of

		vec2 lambda = intersectObject(ray, objects[i]);
		if (lambda.x > 0.0 && lambda.x < lambda.y && lambda.x < smallest) {
			ray.last_hit.lambda = lambda;
			ray.last_hit.bi = i;
			smallest = lambda.x;
			found = true;
		}
	}

	// Normal
	ray.last_hit.position = mix(ray.position, ray.position + ray.direction, ray.last_hit.lambda.x);
	normal = objectNormal(ray.last_hit.position, objects[ray.last_hit.bi]);

	return found;
}

/* returns light accessability (intensivity / dst^2) */
float shadowRay(in rayData ray) {
	// Test for all lights emiters // for now, only points centered at object
	float light = 0.0;

	for (int j = 0; j < NUM_OBJECTS; j++) {
		if (j == ray.last_hit.bi || objects[j].intensivity < 0.01) continue; // skip if testing object that it just reflected off of

		float dst = ray.position - objectCenter(object[j]);
		light += objects[j].intensivity / (dst*dst);
		continue; y

		// test if can see this object
		rayData rayToThisLight = rayData(ray.position, normalize(ray.position - objectCenter(objects[j])), newHitInfo());
		vec3 normal;
		bool found = intersect(rayToThisLight, normal);

		if (found && rayToThisLight.last_hit.bi == j) {
			float dst = rayToThisLight.last_hit.lambda.x;
			light += objects[j].intensivity / (dst*dst);
		}

	}

	return light;
}

vec4 trace(rayData ray) {
	const float epsilon = 0.000001;
	const int bounces = 8;

	vec3 color = vec3(1.0);
	float energy = 0.0;
	vec3 normal_vector;

	for (int b = 0; b < bounces; b++) { // for 8 bounces
		if (intersect(ray, normal_vector)) {
#ifdef DRAW_NORMALS
			color.rgb = normal_vector;
			break;
#endif
			// Assign hitpos as current pos
			ray.position = ray.last_hit.position;

			// Light direction and artifact fixing and reflection
			ray.position += normal_vector * epsilon;
			ray.direction = reflect(ray.direction, normal_vector);

			// Temporal texturing
			if (ray.last_hit.bi == 0) {
				color *= texture(floor_texture, (ray.position.xz + vec2(5.0, 5.0)) / 10.0).rgb * (1.0 - objects[ray.last_hit.bi].reflectivity);
			} else {
				color *= objects[ray.last_hit.bi].color;
			}

			// Object intensivity
			energy += objects[ray.last_hit.bi].intensivity;

			// Shadow rays
			float lightAccess = shadowRay(ray);
			energy += lightAccess * slider0;
			
			//rayData lightTest = rayData(ray.position, light_vector, newHitInfo(), 1.0);
			//if (intersectLight(lightTest)) { // test for light access
			//	if (boxes[ray.last_hit.bi].reflectivity != 1.0) {
//
			//		// non reflective materials
			//		float intensity;
			//		intensity = max(dot(normal_vector, light_vector), 0.0);
			//		intensity /= length(ray.position - lightPosition) / slider0;
			//		color.a += intensity / ray.energy * (1.0 - boxes[ray.last_hit.bi].reflectivity);
//
			//		// reflective materials
			//		// calculated after bounce
//
			//		// refraction
			//		// todo:
//
			//	}
			//}

			if (objects[ray.last_hit.bi].reflectivity < 0.01) break;
			//if (objects[ray.last_hit.bi].intensivity > 0.99) break;

			energy -= (1.0 - objects[ray.last_hit.bi].reflectivity);

		} ///endif

	} ///endfor

	return vec4(color * energy, 1.0);
}

layout (local_size_x = 16, local_size_y = 8) in;
void main(void) {
	ivec2 pix = ivec2(gl_GlobalInvocationID.xy);
	ivec2 size = imageSize(framebuffer);
	if (pix.x >= size.x || pix.y >= size.y) {
		return;
	}
	vec2 pos = vec2(pix) / vec2(size.x - 1, size.y - 1);
	vec3 dir = normalize(mix(mix(ray00, ray01, pos.y), mix(ray10, ray11, pos.y), pos.x));

	rayData ray = rayData(eye, dir, newHitInfo());
	vec4 color = trace(ray);
	imageStore(framebuffer, pix, color);
}
