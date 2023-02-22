import vision.gears.webglmath.*

class Material(program: Program) : UniformProvider("material") {
	init {
		addComponentsAndGatherUniforms(program)
	}
}