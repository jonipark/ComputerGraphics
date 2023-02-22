import vision.gears.webglmath.*
import kotlin.math.*
import kotlin.js.Date
import org.khronos.webgl.WebGLRenderingContext as GL //# GL# we need this for the constants declared ˙HUN˙ a constansok miatt kell


class GameObject(vararg val meshes : Mesh): UniformProvider("gameObject") {
  val modelMatrix by Mat4()
  val position = Vec3()
  var roll = 0.0f
  val scale = Vec3(0.1f, 0.1f, 0.1f)
  var pitch = 0f
  var yaw = 0f
  var parent = this

	init { 
    addComponentsAndGatherUniforms(*meshes)
  }
	fun update() {

    // model matrix of children based on parent
    if (parent != this) {
      modelMatrix.set().
        scale(scale).
        rotate(roll).
        rotate(pitch, 1.0f, 0.0f, 0.0f).
        rotate(yaw, 0.0f, 1.0f, 0.0f).
        translate(position).
        rotate(parent.roll).
        rotate(parent.pitch, 1.0f, 0.0f, 0.0f).
        rotate(parent.yaw, 0.0f, 1.0f, 0.0f).
        translate(parent.position)

    }
    else {
      modelMatrix.set().
        scale(scale).
        rotate(roll).
        rotate(pitch, 1.0f, 0.0f, 0.0f).
        rotate(yaw, 0.0f, 1.0f, 0.0f).
        translate(position)
    }
  
  }

  open class Motion(open val gameObject : GameObject) {

    open var targetPosition = Vec2()
    open var velocity = 0.0f
    open val acceleration = 0.1f
    open var mouseDown = false 
    open var mouseCoords = Vec2()

  	open operator fun invoke(dt : Float, t: Float, keysPressed : Set<String>,
  		spawn : ArrayList<GameObject>, toRemove : ArrayList<GameObject>) {

  		gameObject["textureScale"]?.set(1f, 1f)
      if ("UP" in keysPressed) {
        velocity += acceleration
        if ("LEFT" in keysPressed){
          gameObject.yaw += 0.03f
        }
        // if right is pressed, move the chevy and wheel right and rotate the wheels
        if ("RIGHT" in keysPressed){
          gameObject.yaw -= 0.03f
        }
      }
      else if ("DOWN" in keysPressed) {
        velocity -= acceleration
        if ("LEFT" in keysPressed){
          gameObject.yaw += 0.03f
        }
        // if right is pressed, move the chevy and wheel right and rotate the wheels
        if ("RIGHT" in keysPressed){
          gameObject.yaw -= 0.03f
        }
      } 
      else {
        velocity = 0.0f
      }

      val newX = gameObject.position.x + sin(gameObject.yaw) * velocity * dt
      val newZ = gameObject.position.z + cos(gameObject.yaw) * velocity * dt
      gameObject.position.set(newX, gameObject.position.y, newZ)
  	}
  }

  



  var move = Motion(this)
}
