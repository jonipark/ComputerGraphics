import vision.gears.webglmath.*

class OrthoCamera(
	val position : Vec2 = Vec2(),
	val windowSize : Vec2 = Vec2(2f, 2f),
	var orientation : Float = 0f 
	) : UniformProvider("camera") {

	val viewProjMatrix by Mat4()

	var aspectRatio : Float = 1.0f


	fun update() {
		// set to identity matrix first and then transform
		viewProjMatrix.set().
			scale(0.5f, 0.5f, 1.0f).
			scale(windowSize).
			rotate(orientation).
			translate(position).
			invert()
	}

	fun setAspectRatio(a : Float) {
		aspectRatio = a
		windowSize.x = windowSize.y * a
		update()
	}

	init {
		update()
	}


}