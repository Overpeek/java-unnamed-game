#version 400 core

layout(location = 0) out vec4 color;

in vec2 shader_uv;
in vec4 shader_color;
flat in int shader_id;

uniform mat4 vw_matrix = mat4(1.0);

#define pi 3.14159
#define cameraFov pi / 2.0f
float EPSILON = 0.001;

uniform float time = 0.0;
uniform vec3 light = vec3(0.0, -2.0, 0.0);
uniform vec3 camera = vec3(0.0, 0.0, 0.5);
uniform float cameraLookingX = pi;
uniform float cameraLookingY = -pi / 2.0;
uniform float power = 1.1;
uniform int iterations = 16;
uniform samplerCube skybox;


float mandelbulb(vec3 p) {
	vec3 z = p;
	float dr = 1.0f;
	float r;

	for (int i = 0; i < iterations; i++) {
		r = length(z);
		if (r > 2.0f) break;

		float theta = acos(z.z / r) * power;
		float phi = atan(z.y / z.x) * power;
		float zr = pow(r, power);
		dr = pow(r, power - 1.0f) * power * dr + 1.0f;

		z = zr * vec3(sin(theta) * cos(phi), sin(phi) * sin(theta), cos(theta));
		z += p;
	}
	return 0.5f * log(r) * r / dr;
}

float signedDst(vec3 pointA, vec3 pointB) {
	return sqrt((pointA.x-pointB.x)*(pointA.x-pointB.x) + (pointA.y-pointB.y)*(pointA.y-pointB.y) + (pointA.z-pointB.z)*(pointA.z-pointB.z));
}

float signedDst(vec2 pointA, vec2 pointB) {
	return sqrt((pointA.x-pointB.x)*(pointA.x-pointB.x) + (pointA.y-pointB.y)*(pointA.y-pointB.y));
}

float sdTorus( vec3 p, vec2 t )
{
	vec2 q = vec2(length(p.xz)-t.x,p.y);
	return length(q)-t.y;
}

float sdPlane( vec3 p )
{
	return abs(p.y - 0.5);
}

float sdBox( vec3 p, vec3 b )
{
	vec3 d = abs(p) - b;
	return length(max(d,0.0))
     + min(max(d.x,max(d.y,d.z)),0.0); // remove this line for an only partially signed sdf
}

float sdSphere( vec3 p, float s )
{
	return length(p)-s;
}

float sdDistort(vec3 p) {
	return sin(20.0f * p.x) + sin(20.0f * p.y) + sin(20.0f * p.z);
}

float opRep( in vec3 p, in vec3 c )
{
	vec3 q = mod(p,c)-0.5f*c;
	return mandelbulb(q - vec3(-0.5f, 0.0f, -0.5f));
}

float smoothMin(float a, float b) {
	float k = power;
	float h = max(k-abs(a-b), 0.0) / k;
	return min(a, b) - h*h*h*k/6.0;
}

float map(float value, float low1, float high1, float low2, float high2) {
	return low2 + (value - low1) * (high2 - low2) / (high1 - low1);
}

float rand(vec2 co){
    return fract(sin(dot(co.xy ,vec2(12.9898,78.233))) * 43758.5453);
}

/*
 * 0 - sphere
 * 1 - box
 * */
float distanceToObject(int objectType, vec3 rayPosition, vec3 objectPosition, vec3 size) {
	switch(objectType) {
	case 0:
		return sdSphere(rayPosition - objectPosition, size.x);
		break;
	case 1:
		return sdBox(rayPosition - objectPosition, size);
		break;

	default:
		break;
	}
}

struct object {

	int objectType;
	int index;
	vec3 position;
	vec3 size;
	vec3 color;

} objects[4];

float getDistToObject(object obj, vec3 position) {
	return distanceToObject(obj.objectType, position, obj.position, obj.size);
}

object getClosestObject(vec3 position) {
	float distance = getDistToObject(objects[0], position);
	int index = 0;

	float thisDst = getDistToObject(objects[1], position);
	if (distance > thisDst){
		index = 1;
		distance = thisDst;
	}

	thisDst = getDistToObject(objects[2], position);
	if (distance > thisDst){
		index = 2;
		distance = thisDst;
	}

	thisDst = getDistToObject(objects[3], position);
	if (distance > thisDst){
		index = 3;
		distance = thisDst;
	}

	return objects[index];
}

float getDistToClosestObject(vec3 position) {
	return getDistToObject(getClosestObject(position), position);
}

vec3 estimateNormal(vec3 p) {
	float d = getDistToClosestObject(p);
	vec2 e = vec2(d / 2.0f, 0.0);
	vec3 n = d - vec3(
			getDistToClosestObject(p-e.xyy),
			getDistToClosestObject(p-e.yxy),
			getDistToClosestObject(p-e.yyx));

    return normalize(n);
}

struct rayData
{
	vec3 position;
	vec3 direction;
	vec3 color;

	int bounceCount;
	int lastHitObject;

	float distance;
};

rayData rayMarch(rayData ray, int maxSteps, float maxDistance) {
	const int maxBounces = 8;
	float distanceOrigin = 0.0;

	for (int j = 0; j < maxSteps; j++) {
		object closestObj = getClosestObject(ray.position);
		float dist = getDistToObject(closestObj, ray.position);

		//If over max dist
		if (distanceOrigin > maxDistance) {
			ray.color *= texture(skybox, normalize(ray.direction)).rgb;
			return ray;
		}

		//If near surface
		if (dist < EPSILON) {

			if (ray.lastHitObject != closestObj.index) {
				ray.bounceCount++;
				ray.lastHitObject = closestObj.index;
				ray.color *= closestObj.color;

				//Reflect before stop
				ray.position -= ray.direction * EPSILON;
				vec3 surfaceRandomness = vec3(rand(ray.position.xy)) / 50.0f * 0.0f;
				vec3 normal = normalize(estimateNormal(ray.position) + surfaceRandomness);
				ray.direction = reflect(ray.direction, normal);
			} else {
				continue;
			}

			return ray;
		}

		ray.position += ray.direction * dist;
		distanceOrigin += dist;
	}

	return ray;
}

void main()
{
	objects[0] = object(0, 0, vec3(-0.5f, 0.5f,  0.5f), vec3(0.1f, 0.2f, 0.3f), vec3(1.0f, 0.5f, 0.5f));
	objects[1] = object(1, 1, vec3(-0.1f, 0.0f,  0.1f), vec3(0.2f, 0.2f, 0.2f), vec3(1.0f, 0.0f, 0.0f));
	objects[2] = object(1, 2, vec3( 0.1f, 0.0f, -0.1f), vec3(0.2f, 0.2f, 0.2f), vec3(0.0f, 1.0f, 0.0f));
	objects[3] = object(1, 3, vec3( 0.0f, 0.1f,  0.0f), vec3(0.2f, 0.2f, 0.2f), vec3(0.0f, 0.0f, 1.0f));

	float aspect = 6.0f / 6.0f;
	float rayX = map(gl_FragCoord.x, 0.0, 600.0, -cameraFov / 2.0 * aspect, cameraFov / 2.0 * aspect);
	float rayY = map(gl_FragCoord.y, 0.0, 600.0, -cameraFov / 2.0, cameraFov / 2.0);
	vec3 directionVector = (vw_matrix * normalize(vec4(-rayX, -rayY, -1.0, 0.0))).xyz;
	vec3 rayColor = vec3(1.1);
	vec3 rayOrigin = camera.xyz;

	rayData ray = rayData(rayOrigin, directionVector, rayColor, 0, -1, 0.0f);

	for (int i = 0; i < 8; ++i) {
		ray = rayMarch(ray, 128, 1000.0);

		if (ray.bounceCount > 0) { //ray hit something
			ray.bounceCount = 0;
			continue;
		} else { //ray did not hit anything
			ray.color *= texture(skybox, normalize(directionVector)).rgb;
			break;
		}
	}

	/*
	if (rayData.w == 1) { //ray hit something
		rayColor *= getColorAndDst(rayData.xyz).xyz;
		rayOrigin = rayData.xyz;
		vec3 normal = normalize(estimateNormal(rayOrigin));
		vec3 colorDiff = vec3(0.0);


		//Reflection
		vec3 reflectO = rayOrigin + normal * EPSILON * 2.0f;
		vec3 reflectD = reflect(directionVector, normal);
		{ /////////////
			vec4 rayData2 = rayMarch(reflectO, reflectD, int(pow(2, 8)), 1000.0);
			if (rayData2.w == 1) { //ray hit something
				colorDiff += getColorAndDst(rayData2.xyz).xyz * 1.0 / 4.0;
			}
			else { //ray hit nothing
				colorDiff += texture(skybox, normalize(vec3(reflectD.x, -reflectD.y, reflectD.z))).rgb * 1.0 / 4.0;
			}
		} /////////////

		//Refraction TODO: improve
		//vec3 refractO = rayOrigin + normal * EPSILON * 2.0f;
		//vec3 refractD = refract(directionVector, normal, 1.0003 / 1.3330);
		//colorDiff += texture(skybox, normalize(vec3(refractD.x, -refractD.y, refractD.z))).rgb * 3.0 / 4.0;
		//{ /////////////
		//	vec4 rayData2 = rayMarch(refractO, refractD, int(pow(2, 8)), 1000.0);
		//	if (rayData2.w == 1) { //ray hit something
		//		rayColor *= getColorAndDst(rayData2.xyz).xyz / 2.0;
		//	}
		//	else { //ray hit nothing
		//	}
		//} /////////////

		rayColor *= colorDiff;

	}
	else { //ray hit nothing
		rayColor *= texture(skybox, normalize(vec3(directionVector.x, -directionVector.y, directionVector.z))).rgb;
	}
	*/




	color = vec4(ray.color, 1.0);
}
