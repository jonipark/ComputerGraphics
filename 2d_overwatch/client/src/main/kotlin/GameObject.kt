import vision.gears.webglmath.*
import kotlin.math.*

class GameObject(vararg meshes : Mesh): UniformProvider("gameObject") {
  val modelMatrix by Mat4()
  val position = Vec3()
  val scale = Vec3(1.0f, 1.0f, 1.0f)

  var roll = 0.0f
  var yaw = 0.0f
  var pitch = 0.0f
  var spawnedTime = 0.0f

  init { 
    addComponentsAndGatherUniforms(*meshes)
  }
  fun update() {
  	modelMatrix.set().
      scale(scale).rotate(roll).translate(position)
  } 

  open class Motion(val gameObject : GameObject){
    open var targetPosition = Vec2()
    open var targetRoll = 0.0f
    open var moveVelocity = 0.8f
    open var rollVelocity = 20.0f
    open var isMoving = false

  	open operator fun invoke(
  		dt : Float,
  		t : Float
  		) {
      gameObject["textureScale"]?.set(1f, 1f)
      // how fast you change your head
      val dR = rollVelocity * dt
      if (gameObject.roll - targetRoll > dR) {
        gameObject.roll -= dR
      } else if (gameObject.roll - targetRoll < -dR){
        gameObject.roll += dR
      } else {
        gameObject.roll = targetRoll
      }
  		
      // when avatar reaches the target
      if ((targetPosition - gameObject.position.xy).length() < 0.5f) {
        return 
      }

      // movement
      if (isMoving){
        // automatically move towards the target
        val unitDirection = (targetPosition - gameObject.position.xy).normalize()

        // automatically turn so that they move in the direction of their velocity
        targetRoll = atan2(unitDirection.y, unitDirection.x)// + PI.toFloat()*2) % PI.toFloat()

        val newX = gameObject.position.x + unitDirection.x * dt * moveVelocity
        val newY = gameObject.position.y + unitDirection.y * dt * moveVelocity
        gameObject.position.xy = Vec2(newX, newY) 
      }                                                                                                                                   
  	}
  }
  var move = Motion(this)
}
