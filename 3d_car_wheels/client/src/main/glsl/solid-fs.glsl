#version 300 es

precision highp float;

uniform struct {
  vec3 solidColor;
} material;

in vec4 position;
out vec4 fragmentColor; //#vec4# A four-element vector [r,g,b,a].; Alpha is opacity, we set it to 1 for opaque.; It will be useful later for transparency.

void main(void) {
  fragmentColor = vec4(material.solidColor, 0);
}
