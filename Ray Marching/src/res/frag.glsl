#version 400 core

layout(location = 0) out vec4 color;

in vec2 shader_uv;
in vec4 shader_color;
flat in int shader_id;

uniform mat4 vw_matrix = mat4(1.0);

#define pi 3.14159
#define cameraFov pi / 2.0
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

vec4 getColorAndDst(vec3 point) {
	//float d0 = sdSphere(point - vec3( sin(time), 0.0f,  0.0f), 0.3f);
	//float d1 = sdSphere(point - vec3(-0.5f, 0.0f,  0.5f), 0.3f);
	//float d2 = sdSphere(point - vec3( 0.5f, 0.0f, -0.5f), 0.3f);
	//float d3 = sdSphere(point - vec3(-0.5f, 0.0f, -0.5f), 0.3f);
	//float d4 = sdBox(point - vec3(0.0f, -0.6f, 0.0f), vec3(0.1f, 0.5f, 0.5f));
	//float d5 = sdBox(point - vec3(0.0f, 0.6f, 0.0f), vec3(0.1f, 0.5f, 0.5f));
	float d6 = opRep(point, vec3(10.0f, 10.0f, 10.0f));// mandelbulb(point);
	//float d6 = mandelbulb(point);
	//float closest = min(d0, min(d1, min(d2, min(d3, min(d4, min(d5, d6))))));
	float closest = d6;

	vec3 col = vec3(1.0, 1.0, 1.0);

	return vec4(col, closest);
}

float distToObjs(vec3 point) {
	return getColorAndDst(point).w;
}

vec3 estimateNormal(vec3 p) {
	float d = distToObjs(p);
	vec2 e = vec2(d / 2.0f, 0.0);
	vec3 n = d - vec3(
			distToObjs(p-e.xyy),
			distToObjs(p-e.yxy),
			distToObjs(p-e.yyx));

    return normalize(n);
}

float map(float value, float low1, float high1, float low2, float high2) {
	return low2 + (value - low1) * (high2 - low2) / (high1 - low1);
}

float testSeeLight(vec3 p) {
	int raySteps = 64;
	bool pathToLight = true;
	vec3 lightDirection = vec3(light.x - p.x, light.y - p.y, light.z - p.z);
	vec3 normal = normalize(estimateNormal(p));
	p += normal * EPSILON * 2.0;

	for (int j = 0; j < raySteps; j++) {
		float dist = distToObjs(vec3(p.x, p.y, p.z));
		float dstToLight = length(lightDirection);
		float travel = min(dist, dstToLight);

		//Hit something, no light access
		if (dist < EPSILON) {
			pathToLight = false;
			break;
		}

		//Distance to light is the smallest
		if (dstToLight < EPSILON) {
			pathToLight = true;
			break;
		}

		p += normalize(lightDirection) * travel;
	}

	if (!pathToLight) return 0.0;

	return 10.0f / pow(length(lightDirection), 2.0);
}

float rand(vec2 co){
    return fract(sin(dot(co.xy ,vec2(12.9898,78.233))) * 43758.5453);
}

vec4 rayMarch(vec3 pos, vec3 dir, int steps, float maxDistance) {
	float distanceOrigin = 0.0;
	//						light	dist	br
	vec4 returndata = vec4(	0, 		0, 		0, 0);
	vec3 rayColor = vec3(1.0, 1.0, 1.0);
	float rayBrightness = 0.0;
	int bounces = 0;
	int maxBounces = 8;
	vec3 lightDirection = normalize(vec3(light.x - pos.x, light.y - pos.y, light.z - pos.z));
	vec3 lastHit;
	int stepsUsed = 0;
	bool hit = false;

	for (int j = 0; j < steps; j++) {
		//stepsUsed++;
		float dist = distToObjs(pos);

		//If over max dist
		if (distanceOrigin > maxDistance) {
			hit = false;
			break;
		}

		//If near surface
		if (dist < EPSILON) {
			hit = true;
			break;
		}

		pos += dir * dist;
		distanceOrigin += dist;
	}
	returndata = vec4(pos.xyz, int(hit));

	return returndata;
}

void main()
{
	float rayX = map(gl_FragCoord.x, 0.0, 800.0, -cameraFov / 2.0, cameraFov / 2.0);
	float rayY = map(gl_FragCoord.y, 0.0, 800.0, -cameraFov / 2.0, cameraFov / 2.0);
	vec3 directionVector = (vw_matrix * normalize(vec4(-rayX, -rayY, -1.0, 0.0))).xyz;
	vec3 rayOrigin = vec3(camera.x, camera.y, camera.z);

	vec3 rayColor = vec3(1.0);



	for (int i = 0; i < 3; ++i) {
		vec4 rayData = rayMarch(rayOrigin, directionVector, 128, 1000.0);

		if (rayData.w == 1) { //ray hit something
			//rayColor *= getColorAndDst(rayData.xyz).xyz;
			rayOrigin = rayData.xyz;
			rayOrigin -= directionVector * EPSILON;
			vec3 normal = normalize(estimateNormal(rayOrigin));

			directionVector = reflect(directionVector, normal);
		} else { //ray did not hit anything
			rayColor = texture(skybox, normalize(vec3(directionVector.x, -directionVector.y, directionVector.z))).rgb;
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




	color = vec4(rayColor, 1.0);
}
