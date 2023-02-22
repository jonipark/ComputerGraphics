import org.w3c.dom.HTMLCanvasElement
import org.khronos.webgl.WebGLRenderingContext as GL //# GL# we need this for the constants declared ˙HUN˙ a constansok miatt kell
import kotlin.js.Date
import kotlin.math.*
import vision.gears.webglmath.*
import kotlin.random.*
import org.w3c.dom.events.*

class Scene (
  val gl : WebGL2RenderingContext){

  val vsIdle = Shader(gl, GL.VERTEX_SHADER, "idle-vs.glsl")
  val vsRay = Shader(gl, GL.VERTEX_SHADER, "ray-vs.glsl")
  val vsTransform = Shader(gl, GL.VERTEX_SHADER, "transform-vs.glsl")
  val fsSolid = Shader(gl, GL.FRAGMENT_SHADER, "solid-fs.glsl")
  val fsTextured = Shader(gl, GL.FRAGMENT_SHADER, "textured-fs.glsl")
  val solidProgram = Program(gl, vsTransform, fsSolid, Program.PC)
  val texturedProgram = Program(gl, vsTransform, fsTextured, Program.PNT)
  val triangleGeometry = TriangleGeometry(gl)
  val texturedQuadGeometry = TexturedQuadGeometry(gl)



  val jsonLoader = JsonLoader()

  // val material1 = Material(texturedProgram).apply {
  //     this["colorTexture"]?.set(Texture2D(gl, "media/slowpoke/YadonDh.png"))
  // }
  // val slowpokeYaw = (PI / 2).toFloat()
  // val slowpokeMeshes = jsonLoader.loadMeshes(
  //   gl, "media/slowpoke/Slowpoke.json",
  //   Material(texturedProgram).apply {
  //     this["colorTexture"]?.set(Texture2D(gl, "media/slowpoke/YadonDh.png"))
  //   },
  //   Material(texturedProgram).apply {
  //     this["colorTexture"]?.set(Texture2D(gl, "media/slowpoke/YadonEyeDh.png"))
  //     },
  // )

  // car mesh
  val carMeshes = jsonLoader.loadMeshes(
    gl, "media/3d.json/chevy/chassis.json",
    Material(texturedProgram).apply {
      this["colorTexture"]?.set(Texture2D(gl, "media/3d.json/chevy/chevy.png"))
    }
  )
  val wheelMeshes = jsonLoader.loadMeshes(
    gl, "media/3d.json/chevy/wheel.json",
    Material(texturedProgram).apply {
      this["colorTexture"]?.set(Texture2D(gl, "media/3d.json/chevy/chevy.png"))
    }
  )
  
  val chevy = GameObject(*carMeshes).apply{
    scale.set(0.1f, 0.1f, 0.1f)
  }

  val wheelFrontLeft = GameObject(*wheelMeshes).apply{
    position.set(0.7f, -0.4f, 1.4f)
    parent = chevy
    move = object : GameObject.Motion(this) {
      override operator fun invoke(dt : Float, t: Float, keysPressed : Set<String>,
  		spawn : ArrayList<GameObject>, toRemove : ArrayList<GameObject>) {
        gameObject["textureScale"]?.set(1f, 1f)

        if ("UP" in keysPressed) {
          gameObject.pitch += 0.1f
        }
        if ("DOWN" in keysPressed) {
          gameObject.pitch -= 0.1f
        }

        if ("LEFT" in keysPressed) {
          gameObject.yaw = 0.2f
        } else if ("RIGHT" in keysPressed) {
          gameObject.yaw = -0.2f
        } else {
          gameObject.yaw = 0.0f
        }
      }
    }
  }

  val wheelFrontRight = GameObject(*wheelMeshes).apply{
    position.set(-0.7f, -0.4f, 1.4f)
    move = object : GameObject.Motion(this) {
      override operator fun invoke(dt : Float, t: Float, keysPressed : Set<String>,
  		spawn : ArrayList<GameObject>, toRemove : ArrayList<GameObject>) {
        gameObject["textureScale"]?.set(1f, 1f)

        if ("UP" in keysPressed) {
          gameObject.pitch += 0.1f
        }
        if ("DOWN" in keysPressed) {
          gameObject.pitch -= 0.1f
        }

        if ("LEFT" in keysPressed) {
          gameObject.yaw = 0.2f
        } else if ("RIGHT" in keysPressed) {
          gameObject.yaw = -0.2f
        } else {
          gameObject.yaw = 0.0f
        }
      }
    }
    parent = chevy
  }

  val wheelBackLeft = GameObject(*wheelMeshes).apply{
    position.set(0.7f, -0.4f, -1.07f)
    move = object : GameObject.Motion(this) {
      override operator fun invoke(dt : Float, t: Float, keysPressed : Set<String>,
  		spawn : ArrayList<GameObject>, toRemove : ArrayList<GameObject>) {
        gameObject["textureScale"]?.set(1f, 1f)

        if ("UP" in keysPressed) {
          gameObject.pitch += 0.1f
        }
        if ("DOWN" in keysPressed) {
          gameObject.pitch -= 0.1f
        }
      }
    }
    parent = chevy
  }

  val wheelBackRight = GameObject(*wheelMeshes).apply{
    position.set(-0.7f, -0.4f, -1.07f)
    move = object : GameObject.Motion(this) {
      override operator fun invoke(dt : Float, t: Float, keysPressed : Set<String>,
  		spawn : ArrayList<GameObject>, toRemove : ArrayList<GameObject>) {
        gameObject["textureScale"]?.set(1f, 1f)

        if ("UP" in keysPressed) {
          gameObject.pitch += 0.1f
        }
        if ("DOWN" in keysPressed) {
          gameObject.pitch -= 0.1f
        }
      }
    }
    parent = chevy
  }

  val grassTexture = Texture2D(gl, "media/grass.jpg")
  val grassMaterial = Material(texturedProgram).apply{
    this["colorTexture"]?.set(grassTexture)
  }

  val grassMesh = Mesh(grassMaterial, texturedQuadGeometry)

  val solidMaterial = Material(solidProgram).apply {
    this["solidColor"]?.set(1f, 0f, 0f)
  }

  


  val initCameraHeight = 10f
  val initCameraWidth = 10f
  // val camera = OrthoCamera(windowSize = Vec2(initCameraWidth, initCameraHeight), orientation = 0f)
  val camera = PerspectiveCamera(*Program.all)
  var canvasHeight = 0f
  var canvasWidth = 0f

  val redTri = Mesh(solidMaterial, triangleGeometry)
  val blueTri = Mesh(solidMaterial, triangleGeometry)
  val avatar = Mesh(solidMaterial, triangleGeometry)

  val gameObjects = ArrayList<GameObject>()
  val selectedObjects = ArrayList<GameObject>()
  val spawn = ArrayList<GameObject>()
  val toRemove = ArrayList<GameObject>()

  var mIsHeld = false 

  init {
    // var slowpokeObject = GameObject(*slowpokeMeshes)
    var grassObject = GameObject(grassMesh).apply {
      position.set(0.0f, -0.8f, 0.0f)
      move = object : GameObject.Motion(this) {
        override operator fun invoke(dt : Float, t: Float, keysPressed : Set<String>,
        spawn : ArrayList<GameObject>, toRemove : ArrayList<GameObject>) {
          gameObject["textureScale"]?.set(1f, 1f)
        }
      }
    }
    grassObject.scale.x *= 600.0f
    grassObject.scale.y *= 600.0f
    // slowpokeObject.yaw = slowpokeYaw
    grassObject.pitch = (PI/2).toFloat()
    gameObjects += grassObject
    // gameObjects += slowpokeObjec
    gameObjects += chevy
    gameObjects += wheelFrontLeft
    gameObjects += wheelFrontRight
    gameObjects += wheelBackLeft
    gameObjects += wheelBackRight
  }



  val timeAtFirstFrame = Date().getTime()
  var timeAtLastFrame = timeAtFirstFrame


  fun resize(canvas : HTMLCanvasElement) {
    gl.viewport(0, 0, canvas.width, canvas.height)//#viewport# tell the rasterizer which part of the canvas to draw to ˙HUN˙ a raszterizáló ide rajzoljon
    // camera.setAspectRatio(canvas.width.toFloat() / canvas.height.toFloat())
    canvasWidth = canvas.width.toFloat()
    canvasHeight = canvas.height.toFloat()
  }

  @Suppress("UNUSED_PARAMETER")
  fun update(keysPressed : Set<String>) {
    val timeAtThisFrame = Date().getTime()
    val dt = (timeAtThisFrame - timeAtLastFrame).toFloat() / 1000.0f
    val t = (timeAtThisFrame - timeAtFirstFrame).toFloat() / 1000.0f

    timeAtLastFrame = timeAtThisFrame
    // console.log("keysPressed", keysPressed)
    camera.move(dt, keysPressed)

    gl.clearColor(1f, 1f, 1f, 1.0f)//## red, green, blue, alpha in [0, 1]
    gl.clearDepth(1.0f)//## will be useful in 3D ˙HUN˙ 3D-ben lesz hasznos
    gl.clear(GL.COLOR_BUFFER_BIT or GL.DEPTH_BUFFER_BIT)//#or# bitwise OR of flags
    gl.useProgram(solidProgram.glProgram)

    gl.enable(GL.BLEND)
    gl.blendFunc(
     GL.SRC_ALPHA,
     GL.ONE_MINUS_SRC_ALPHA)

    // enable depth testing to avoid further away parts showing through
    gl.enable(GL.DEPTH_TEST)

    gameObjects.forEach {
      it.move(dt, t, keysPressed, spawn, toRemove)
    }
    gameObjects.addAll(spawn)
    gameObjects.removeAll(toRemove)
    gameObjects.forEach {
      it.update()
    }
    gameObjects.forEach {
      it.draw(camera)
    }
    //handleKeyPresses(keysPressed) 
  }

  fun handleKeyPresses(keysPressed : Set<String>) {

    // if up is pressed, move the chevy forward
    // if down is pressed, move the chevy backward
    if ("UP" in keysPressed) {
      chevy.position.z += 0.1f
      // roll wheels
      wheelFrontLeft.pitch += 0.1f
      wheelFrontRight.pitch += 0.1f
      wheelBackLeft.pitch += 0.1f
      wheelBackRight.pitch += 0.1f
    }
    if ("DOWN" in keysPressed) {
      chevy.position.z -= 0.1f
      // roll wheels
      wheelFrontLeft.pitch -= 0.1f
      wheelFrontRight.pitch -= 0.1f
      wheelBackLeft.pitch -= 0.1f
      wheelBackRight.pitch -= 0.1f
    }

    // if left is pressed, move the chevy and wheel left and rotate the wheels
    if ("LEFT" in keysPressed){
      chevy.yaw += 0.1f
    }
    // if right is pressed, move the chevy and wheel right and rotate the wheels
    if ("RIGHT" in keysPressed){
      chevy.yaw -= 0.1f
    }
    

    // // zoom camera
    // if ("Z" in keysPressed) {
    //   camera.windowSize.x *= 0.99f 
    //   camera.windowSize.y *= 0.99f
    //   camera.update()
    // }
    // if ("X" in keysPressed) {
    //   camera.windowSize.x *= 1.01f
    //   camera.windowSize.y *= 1.01f
    //   camera.update()
    // }



    // // scroll camera
    // if ("I" in keysPressed) {
    //   camera.position.y = min(5f, camera.position.y + 0.1f)
    //   camera.update()
    // }
    // if ("J" in keysPressed) {
    //   camera.position.x = max(-5f, camera.position.x - 0.1f) 
    //   camera.update()
    // }
    // if ("K" in keysPressed) {
    //   camera.position.y = max(-5f, camera.position.y - 0.1f) 
    //   camera.update()
    // }
    // if ("L" in keysPressed) {
    //   camera.position.x = min(5f, camera.position.x + 0.1f) 
    //   camera.update()
    // }

    // mIsHeld = ("M" in keysPressed)
  
  }

  fun handleClick(x: Int, y: Int) {
    camera.mouseDown()
    // val worldCoords = worldCoordsFromMouseCoords(x, y)

    // if (mIsHeld) {
    //   changeTargets(worldCoords)
    //   return 
    // }

    // for ((index, gameObject) in gameObjects.withIndex()) {
    //   if (getDistance2D(gameObject.position.xy, worldCoords) < 0.5) {
    //     if (gameObject in selectedObjects) {
    //         selectedObjects.remove(gameObject)
    //     }
    //     else {
    //       selectedObjects += gameObject
    //     }
    //     break 
    //   }
    // }
  }

  fun handleMouseMove(event: Event) {
    if (camera.isDragging) {
      camera.mouseMove(event)
    }
  }

  fun handleMouseUp() {
    camera.mouseUp()
  }

  // fun worldCoordsFromMouseCoords(mouseX : Int, mouseY : Int): Vec2 {
  //   val width = camera.windowSize.x 
  //   val height = camera.windowSize.y

  //   val worldX = -width/2 + camera.position.x + (mouseX / canvasWidth)  * width
  //   val worldY = height/2 - camera.position.y - (mouseY / canvasHeight) * height
  //   val worldCoords = Vec2(worldX, worldY)

  //   return worldCoords
  // }

  // return euclidean distance 
  fun getDistance2D(point1: Vec2, point2: Vec2): Float {
    return sqrt((point1.x - point2.x).pow(2) + (point1.y - point2.y).pow(2))
  }

  fun changeTargets(worldCoords : Vec2) {
    selectedObjects.forEach {
      it.move.targetPosition = worldCoords
    }
  }

  // parent codes - Joni
  // shooter object is parent of flame object

  // fun handleMouseDown(x: Float, y: Float) {
  //   val worldCoords = getWorldCoordFromClick(x, y)
  //   if (isMPressed) {
  //     // move shooter to the target position
  //     for (shooter in selectedObjects) {
  //       shooter.move.targetPosition = worldCoords
  //       if (!shooter.move.isMoving) {
  //         shooter.move.isMoving = false
  //       }
  //       val unitDirection = (worldCoords - shooter.position.xy).normalize()
  //       val flamePosition = shooter.position.xy - unitDirection*0.5f
  //       val flameTargetPosition = worldCoords - unitDirection*0.5f

  //       spawnFlame(flamePosition.x, flamePosition.y, shooter.move.moveVelocity, shooter.move.targetRoll, flameTargetPosition)
  //     }
  //   } else {
  //     // click -> shooter shoots bullet
  //     for (shooter in selectedObjects) {
  //       spawnBullet(shooter.position.x, shooter.position.y, worldCoords.x, worldCoords.y)

  //       // move head direction when shoots
  //       val unitDirection = (worldCoords - shooter.position.xy).normalize()
  //       shooter.move.targetRoll = atan2(unitDirection.y, unitDirection.x)// + PI.toFloat()*2) % PI.toFloat()
  //     }
  //   }
  // }

  // fun spawnFlame(x: Float, y: Float, v: Float, r: Float, t: Vec2) : GameObject {
  //   val flame = GameObject(flameMesh).apply {
  //     position.set(x, y)
  //     scale.set(0.3f, 0.3f)
      
  //     move = object : GameObject.Motion(this) {
  //       override var isMoving = true
  //       override var rollVelocity = 100.0f
  //       override var moveVelocity = v
  //       override var targetRoll = r
  //       override var targetPosition = t
  //     }
  //   }
  //   gameObjects += flame
  //   flameObjects += flame
  //   return flame
  // }

}