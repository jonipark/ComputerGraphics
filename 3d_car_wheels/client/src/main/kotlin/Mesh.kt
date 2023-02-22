import vision.gears.webglmath.*

class Mesh(material : Material, geometry : Geometry) : UniformProvider("mesh") {
	init {
		addComponentsAndGatherUniforms(material, geometry)
	}
}