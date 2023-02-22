#version 300 es

in vec4 vertexPosition; //#vec4# A four-element vector [x,y,z,w].; We leave z and w alone.; They will be useful later for 3D graphics and transformations. #vertexPosition# attribute fetched from vertex buffer according to input layout spec
in vec3 vertexColor;

uniform struct {
//	vec3 position;
//	vec3 scale;	
	mat4 modelMatrix;
} gameObject;

uniform struct{
	mat4 viewProjMatrix;
} camera;

out vec3 color;
out vec4 position;

void main(void) {
	vec4 worldPosition = vertexPosition * gameObject.modelMatrix;
	gl_Position = worldPosition * camera.viewProjMatrix;
	color = vertexColor;
//  gl_Position = vertexPosition; //#gl_Position# built-in output, required
//  gl_Position.xyz *= gameObject.scale;
//  gl_Position.xyz += gameObject.position;
  position = vertexPosition;
}