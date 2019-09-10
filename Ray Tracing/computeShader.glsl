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
uniform float slider3;
uniform float slider4;
uniform float time;

uniform sampler2D floor_texture;



#define MAX_SCENE_BOUNDS 1000.0
#define EPSILON 0.0001

// if 1 hard shadows, else resolution for soft shadow
#define SHADOW_RAY_RES 1
#define SHADOW_RAYS
//#define DRAW_NORMALS

/* 
0 - Inverse distance
1 - Inverse distance^2
2 - Inverse sqrt(distance)
*/
#define LIGHT_FUNC 0



float rand(vec2 co){
    return fract(sin(dot(co.xy ,vec2(12.9898,78.233))) * 43758.5453);
}

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

	//	position											size					color					reflectivity	intensivity		type
	{ 	vec3(-5.0, -2.0, -5.0), 							vec3(5.0, -1.0, 5.0), 	vec3(1.0, 1.0, 1.0), 	0.0, 			0.0,			1 }, 	/* The ground */
	{ 	vec3(-0.1, 8.0, -0.1), 								vec3(0.1, 8.1, 0.1), 	vec3(1.0, 1.0, 1.0), 	0.0, 			2.0,			1 }, 	/* The ground */
	{ 	vec3(-0.5, 0.0, -0.5), 								vec3(0.5, 1.0, 0.5), 	vec3(0.0, 1.0, 0.0), 	1.0, 			0.0,			1 }, 	/* Box in the middle */
	{ 	vec3(-1.5, 0.0, -1.5), 								vec3(-0.5, 1.0, -0.5), 	vec3(0.0, 0.0, 1.0), 	1.0, 			0.0,			1 }, 	/* Box next to the middle */
	{ 	vec3( 2.0, 1.0 + sin(time), -2.0), 					vec3(0.5), 				vec3(1.0, 1.0, 1.0), 	0.0, 			0.0,			0 }, 	/* White ball */
	{ 	vec3( slider1, slider3 + cos(time), slider2), 		vec3(0.5), 				vec3(1.0, 1.0, 1.0), 	1.0, 			0.2,			0 }, 	/* White ball light */
	{ 	vec3(-2.0, 1.0 + sin(time * 2.0), -2.0), 			vec3(0.5), 				vec3(0.0, 1.0, 1.0), 	0.5, 			0.0,			0 }, 	/* Cyan ball light */
	{ 	vec3(-2.0, 1.0 + sin(time * 2.0), 2.0), 			vec3(0.5), 				vec3(0.0, 1.0, 1.0), 	1.0, 			0.0,			0 }, 	/* Cyan ball */

};
#define NUM_OBJECTS 8

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

vec3 objectRandPoint(const object obj, float random) {
	float x, y, z;
	vec3 p;

	switch(obj.type) {
	case 0: // sphere
		x = rand(obj.argument1.xy + random) - 0.5;
		y = rand(obj.argument1.xz + random) - 0.5;
		z = rand(obj.argument1.yz + random) - 0.5;

		p = normalize(vec3(x, y, z));
		p *= rand(obj.argument2.xy + random) * obj.argument2.x;
		p += obj.argument1;
		return p;
	case 1: // box
		x = rand(obj.argument1.xy + random) - 0.5;
		y = rand(obj.argument1.xz + random) - 0.5;
		z = rand(obj.argument1.yz + random) - 0.5;
		
		p = vec3(
			map(x, -0.5, 0.5, obj.argument1.x, obj.argument2.x),
			map(y, -0.5, 0.5, obj.argument1.y, obj.argument2.y),
			map(z, -0.5, 0.5, obj.argument1.z, obj.argument2.z));
		return p;
	}

	return vec3(0);
}

bool intersect(inout rayData ray) {
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

	return found;
}

bool intersect2(in rayData ray) {
	return intersect(ray);
}

vec3 normal(in rayData ray) {
	return objectNormal(ray.last_hit.position, objects[ray.last_hit.bi]);
}

float shadowRayPoint(in rayData ray, int object, vec3 surface_normal, vec3 point) {
	ray.direction = normalize(point - ray.position);
	bool found = intersect(ray);
	if (found && ray.last_hit.bi == object) {
		float dst = ray.last_hit.lambda.x;

#if LIGHT_FUNC == 1
		float intensivity = objects[object].intensivity / (dst*dst);
#elif LIGHT_FUNC == 2 
		float intensivity = objects[object].intensivity / sqrt(dst);
#else 		
		float intensivity = objects[object].intensivity / dst;
#endif

		return intensivity * max(dot(ray.direction, surface_normal) + EPSILON, 0.0);
	}
	return 0.0f;
}

vec3 shadowRayObject(in rayData ray, vec3 surface_normal, int object) {
	// for now, only points centered at object
	float light = 0.0;
	for (int i = 0; i < SHADOW_RAY_RES; i++) {
#if SHADOW_RAY_RES == 1
		vec3 point = objectCenter(objects[object]);
#else
		vec3 point = objectRandPoint(objects[object], i);
#endif
		light += shadowRayPoint(ray, object, surface_normal, point);
	}

	return (objects[object].color * light) / SHADOW_RAY_RES;
}

/* returns light accessability (intensivity / dst^2) */
vec3 shadowRay(in rayData ray, vec3 surface_normal) {
	// Test for all lights emiters
	vec3 light = vec3(0.0);
	//ray.last_hit.bi = -1;

	for (int j = 0; j < NUM_OBJECTS; j++) {
		if (j == ray.last_hit.bi || objects[j].intensivity < 0.01) continue; // skip if testing object that it just reflected off of

		light += shadowRayObject(ray, surface_normal, j);
	}

	return light * (1.0 - objects[ray.last_hit.bi].reflectivity);
}

void traceLoop(in rayData ray, vec3 surface_normal, int interaction_index) {
	if (interaction_index <= 0) return;

	if (intersect(ray)) {
		// New position
		ray.last_hit.position = mix(ray.position, ray.position + ray.direction, ray.last_hit.lambda.x);
		normal_vector = normal(ray);

		// Normal draw
#ifdef DRAW_NORMALS
		energy = 1.0;
		color.rgb = normal_vector;
		break;
#endif
		// Assign hitpos as current pos
		ray.position = ray.last_hit.position;

		// Light direction and artifact fixing and reflection
		ray.position += normal_vector * EPSILON;

		// Temporal texturing
		if (ray.last_hit.bi == 0) {
			color *= texture(floor_texture, (ray.position.xz + vec2(5.0, 5.0)) / 10.0).rgb * (1.0 - objects[ray.last_hit.bi].reflectivity);
		} else {
			color *= objects[ray.last_hit.bi].color;
		}

		// Object intensivity
		energy += objects[ray.last_hit.bi].intensivity * max(dot(normal_vector, ray.direction), 0.0) * 2.0;

		// Shadow rays
#ifdef SHADOW_RAYS
		vec3 lightAccess = shadowRay(ray, normal_vector);
		color += color * lightAccess * slider0;
		energy += length(lightAccess) * slider0;
#else
		energy += 0.2;
#endif
		
		// Reflection -- next bounce
		ray.direction = reflect(ray.direction, normal_vector);

		// Refraction


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
		if (objects[ray.last_hit.bi].intensivity > 0.01) break;

		color -= (1.0 - objects[ray.last_hit.bi].reflectivity);

	} ///endif
	else {
		break;
	}
}

vec4 trace(rayData ray) {
	const int bounces = 8;

	vec3 color = vec3(1.0);
	float energy = 0.0;
	vec3 normal_vector;

	for (int b = 0; b < bounces; b++) { // for 8 bounces
		if (intersect(ray)) {
			// New position
			ray.last_hit.position = mix(ray.position, ray.position + ray.direction, ray.last_hit.lambda.x);
			normal_vector = normal(ray);

			// Normal draw
#ifdef DRAW_NORMALS
			energy = 1.0;
			color.rgb = normal_vector;
			break;
#endif
			// Assign hitpos as current pos
			ray.position = ray.last_hit.position;

			// Light direction and artifact fixing and reflection
			ray.position += normal_vector * EPSILON;

			// Temporal texturing
			if (ray.last_hit.bi == 0) {
				color *= texture(floor_texture, (ray.position.xz + vec2(5.0, 5.0)) / 10.0).rgb * (1.0 - objects[ray.last_hit.bi].reflectivity);
			} else {
				color *= objects[ray.last_hit.bi].color;
			}

			// Object intensivity
			energy += objects[ray.last_hit.bi].intensivity * max(dot(normal_vector, ray.direction), 0.0) * 2.0;

			// Shadow rays
#ifdef SHADOW_RAYS
			vec3 lightAccess = shadowRay(ray, normal_vector);
			color += color * lightAccess * slider0;
			energy += length(lightAccess) * slider0;
#else
			energy += 0.2;
#endif
			
			// Reflection -- next bounce
			ray.direction = reflect(ray.direction, normal_vector);

			// Refraction


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
			if (objects[ray.last_hit.bi].intensivity > 0.01) break;

			color -= (1.0 - objects[ray.last_hit.bi].reflectivity);

		} ///endif
		else {
			break;
		}

	} ///endfor

	color = color * energy * slider4;
	if (length(color) > 1.0) color = normalize(color);

	return vec4(color, 1.0);
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
