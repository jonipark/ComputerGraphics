#version 300 es

in vec4 vertexPosition; //#vec4# A four-element vector [x,y,z,w].; We leave z and w alone.; They will be useful later for 3D graphics and transformations. #vertexPosition# attribute fetched from vertex buffer according to input layout spec
in vec2 vertexTexCoord;

uniform struct {
	mat4 modelMatrix;
	vec2 textureOffset;
	vec2 textureScale;
} gameObject;

uniform struct{
	mat4 viewProjMatrix;
} camera;

out vec2 texCoord;
out vec4 position;

void main(void) {
	vec4 worldPosition = vertexPosition * gameObject.modelMatrix;
	gl_Position = worldPosition * camera.viewProjMatrix;
	texCoord = (vertexTexCoord + gameObject.textureOffset) / gameObject.textureScale;
  position = vertexPosition;
}