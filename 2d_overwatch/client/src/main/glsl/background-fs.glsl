#version 300 es

precision highp float;

in vec4 texCoord;
in vec4 rayDir;

uniform struct {
  samplerCube envTexture; 
} material;

uniform struct{
  mat4 rayDirMatrix;
  vec3 position;
} camera;

uniform struct{
  mat4 surface;
  mat4 clipper;
} quadrics[8];

out vec4 fragmentColor;

float intersectQuadric(vec4 e, vec4 d, mat4 A, mat4 B){
	float a = dot(d * A, d);
	float b = dot(d * A, e) + dot(e * A, d);
	float c = dot(e * A, e);

	float disc = b*b-4.0*a*c;
	if(disc < 0.0)
		return -1.0;

	float t1 = (-b + sqrt(disc)) / (2.0*a);
	vec4 hit1 = e + d * t1;
//	if(fract(hit1.x) > 0.5)
	if( dot(hit1 * B, hit1) > 0.0)
		t1 = -1.0;
	float t2 = (-b - sqrt(disc)) / (2.0*a);
	vec4 hit2 = e + d * t2;	
//	if(fract(hit2.x) > 0.5)
	if( dot(hit2 * B, hit2) > 0.0)
		t2 = -1.0;

	return (t1<0.0)?t2:((t2<0.0)?t1:min(t1, t2));
}

bool bestIntersect(vec4 e, vec4 d, out float bestT, out int bestIndex){
	bestT = 9001.0; 
	for(int i=0; i<2; i++){
		float t = intersectQuadric(e, d,
			quadrics[i].surface,
			quadrics[i].clipper);
		if(t < bestT && t > 0.0){
			bestT = t;
			bestIndex = i;
		}
	}
	return bestT < 9000.0;
}

void main(void) {
	vec4 e = vec4(camera.position, 1);
	vec4 d = vec4(normalize(rayDir.xyz), 0);

	float t;	
	int i;
	if(bestIntersect(e, d, t, i)) {
		vec4 hit = e + d * t;
		vec3 normal = normalize( (hit * quadrics[i].surface + quadrics[i].surface * hit).xyz );
		if(dot(d.xyz, normal) > 0.0)
			normal = - normal;
		fragmentColor = vec4(normal.xxx, 1);
	} else {
	  fragmentColor = texture(material.envTexture, rayDir.xyz);
	}
}