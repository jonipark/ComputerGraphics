import vision.gears.webglmath.*

class OrthoCamera(
	val position : Vec2 = Vec2(),
	val windowSize : Vec2 = Vec2(2f, 2f),
	var orientation : Float = 0f 
	) : UniformProvider("camera") {

	val viewProjMatrix by Mat4()

	fun update(){
		viewProjMatrix.set().
			scale(0.5f, 0.5f, 1.0f).
			scale(windowSize).
			rotate(orientation).
			translate(position).
			invert()
	}

	fun setAspectRatio(a : Float){
		windowSize.x = windowSize.y * a
		update()
	}

	init{
		update()
	}

}