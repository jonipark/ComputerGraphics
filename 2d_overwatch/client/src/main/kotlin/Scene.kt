import org.w3c.dom.HTMLCanvasElement
import org.khronos.webgl.WebGLRenderingContext as GL //# GL# we need this for the constants declared ˙HUN˙ a constansok miatt kell
import kotlin.js.Date
import kotlin.math.*
import vision.gears.webglmath.*

// create a global variable score and lives
public var score = 0
public var lives = 10

class Scene (
  val gl : WebGL2RenderingContext){

  val vsIdle = Shader(gl, GL.VERTEX_SHADER, "idle-vs.glsl")
  val vsTextured = Shader(gl, GL.VERTEX_SHADER, "textured-vs.glsl")
  val fsSolid = Shader(gl, GL.FRAGMENT_SHADER, "solid-fs.glsl")
  val fsPurple = Shader(gl, GL.FRAGMENT_SHADER, "purple-fs.glsl") 
  val fsTextured = Shader(gl, GL.FRAGMENT_SHADER, "textured-fs.glsl")

  val solidProgram = Program(gl, vsIdle, fsSolid, Program.PC)
  val purpleProgram = Program(gl, vsIdle, fsPurple, Program.PC)  
  val texturedProgram = Program(gl, vsTextured, fsTextured)

  val quadGeometry = TexturedQuadGeometry(gl)
  val triangleGeometry = TriangleGeometry(gl)

  val redMaterial = Material(solidProgram).apply{
    this["solidColor"]?.set(1f, 0f, 0f)
  }
  val blueMaterial = Material(solidProgram).apply {
    this["solidColor"]?.set(0f, 0f, 1f)
  }  

  // add background material with an image
  val backgroundTexture = Texture2D(gl, "media/background.png")
  val backgroundMaterial = Material(texturedProgram).apply{
    this["colorTexture"]?.set(backgroundTexture)
  }

  val anaTexture = Texture2D(gl, "media/ana.png")
  val anaMaterial = Material(texturedProgram).apply{
    this["colorTexture"]?.set(anaTexture)
  }
  
  val mercyTexture = Texture2D(gl, "media/mercy.png")
  val mercyMaterial = Material(texturedProgram).apply{
    this["colorTexture"]?.set(mercyTexture)
  }

  val lucioTexture = Texture2D(gl, "media/lucio.png")
  val lucioMaterial = Material(texturedProgram).apply{
    this["colorTexture"]?.set(lucioTexture)
  }

  val powerTexture = Texture2D(gl, "media/power.png")
  val selectedMaterial = Material(texturedProgram).apply{
    this["colorTexture"]?.set(powerTexture)
  }

  val doomTexture = Texture2D(gl, "media/doom.png")
  val doomMaterial = Material(texturedProgram).apply{
    this["colorTexture"]?.set(doomTexture)
  }

  val bulletTexture = Texture2D(gl, "media/bullet.png")
  val bulletMaterial = Material(texturedProgram).apply{
    this["colorTexture"]?.set(bulletTexture)
  }

  val boomTexture = Texture2D(gl, "media/boom.png")
  val boomMaterial = Material(texturedProgram).apply{
    this["colorTexture"]?.set(boomTexture)
  }

  val flameTexture = Texture2D(gl, "media/flame.png")
  val flameMaterial = Material(texturedProgram).apply{
    this["colorTexture"]?.set(flameTexture)
  }

  val blueTriMesh = Mesh(blueMaterial, triangleGeometry)
  val triMesh = Mesh(redMaterial, triangleGeometry)  
  val anaMesh = Mesh(anaMaterial, quadGeometry)  
  val mercyMesh = Mesh(mercyMaterial, quadGeometry) 
  val lucioMesh = Mesh(lucioMaterial, quadGeometry) 
  val selectedMesh = Mesh(selectedMaterial, quadGeometry)
  val doomMesh = Mesh(doomMaterial, quadGeometry)
  val bulletMesh = Mesh(bulletMaterial, quadGeometry)  
  val boomMesh = Mesh(boomMaterial, quadGeometry)  
  val flameMesh = Mesh(flameMaterial, quadGeometry) 
  val backgroundMesh = Mesh(backgroundMaterial, quadGeometry)
  // make flame mesh semi transparent
  // flameMesh.material.program.gl.blendFunc(GL.SRC_ALPHA, GL.ONE_MINUS_SRC_ALPHA)

  val gameObjects = ArrayList<GameObject>()
  val shooterObjects = ArrayList<GameObject>()
  val selectedObjects = ArrayList<GameObject>()  
  val doomObjects = ArrayList<GameObject>()
  val bulletObjects = ArrayList<GameObject>()
  val boomObjects = ArrayList<GameObject>()
  val flameObjects = ArrayList<GameObject>()

  // camera
  val camera = OrthoCamera(orientation = 0f, windowSize = Vec2(5f, 5f))
  var canvasHeight = 0f
  var canvasWidth = 0f 

  // time
  val timeAtFirstFrame = Date().getTime()
  var timeAtLastFrame =  timeAtFirstFrame

  // m key pressed
  var isMPressed = false

  init {

    // background
    val background = GameObject(backgroundMesh).apply {
      position.set(0f, 0f)
      scale.set(5f, 2.5f)
    }
    gameObjects.add(background)

    // shooter objects
    spawnShooter(-3.0f, -1.0f)
    spawnShooter(-3.0f, 0.0f)
    spawnShooter(-3.0f, 1.0f)

    // enemy objects
    spawnDoom(0.5f, -1.0f)
    spawnDoom(0.5f, 1.0f)

  }

  fun resize(canvas : HTMLCanvasElement) {
    gl.viewport(0, 0, canvas.width, canvas.height)//#viewport# tell the rasterizer which part of the canvas to draw to ˙HUN˙ a raszterizáló ide rajzoljon
    camera.setAspectRatio(canvas.width.toFloat() / canvas.height.toFloat())
    canvasWidth = canvas.width.toFloat()
    canvasHeight = canvas.height.toFloat()
  }

  @Suppress("UNUSED_PARAMETER")
  fun update(keysPressed : Set<String>) {

    gl.enable(GL.BLEND)
    gl.blendFunc(
     GL.SRC_ALPHA,
     GL.ONE_MINUS_SRC_ALPHA)

    val timeAtThisFrame = Date().getTime() 
    val dt = (timeAtThisFrame - timeAtLastFrame).toFloat() / 1000.0f
    val t  = (timeAtThisFrame - timeAtFirstFrame).toFloat() / 1000.0f    
    timeAtLastFrame = timeAtThisFrame

    gl.clearColor(1.0f, 1.0f, 1.0f, 1.0f)//## red, green, blue, alpha in [0, 1]
    gl.clearDepth(1.0f)//## will be useful in 3D ˙HUN˙ 3D-ben lesz hasznos
    gl.clear(GL.COLOR_BUFFER_BIT or GL.DEPTH_BUFFER_BIT)//#or# bitwise OR of flags


    handleKeyPresses(keysPressed)
    removeBullet()
    removeBoom(dt, t)
    handleCollision(t)

    gameObjects.forEach{ it.update() }
    gameObjects.forEach{ it.draw(camera) }    

    // change color for selected objects
    selectedObjects.forEach{
      it.move.isMoving = isMPressed
      it.using(selectedMaterial).draw(camera)
    }

    gameObjects.forEach{ it.move(dt, t) }
  }


  fun handleKeyPresses(keysPressed : Set<String>) {
    // 2. Selection: Space pick
    //TODO: Add cooldown
    if ("SPACE" in keysPressed) {
      if (selectedObjects.isEmpty()) {
        selectedObjects += shooterObjects[0]
      }
      else {
        //TODO: once selected object is changes, previous selected object's head should be reset
        for ((index, gameObject) in shooterObjects.withIndex()) {
          // one selected object for now
          if (selectedObjects[0] == gameObject) {
            selectedObjects.remove(gameObject)

            if (index < shooterObjects.size-1) {
              selectedObjects += shooterObjects[index+1]
            } else {
              selectedObjects += shooterObjects[0]
            }
            break
          } 
        }
      }
    }

    // 3. Move
    if ("M" in keysPressed){
      isMPressed = true
    } else {
      isMPressed = false
    }
  }


  fun handleMouseDown(x: Float, y: Float) {
    val worldCoords = getWorldCoordFromClick(x, y)
    if (isMPressed) {
      // move shooter to the target position
      for (shooter in selectedObjects) {
        shooter.move.targetPosition = worldCoords
        if (!shooter.move.isMoving) {
          shooter.move.isMoving = false
        }
        val unitDirection = (worldCoords - shooter.position.xy).normalize()
        val flamePosition = shooter.position.xy - unitDirection*0.5f
        val flameTargetPosition = worldCoords - unitDirection*0.5f

        spawnFlame(flamePosition.x, flamePosition.y, shooter.move.moveVelocity, shooter.move.targetRoll, flameTargetPosition)
      }
    } else {
      // click -> shooter shoots bullet
      for (shooter in selectedObjects) {
        spawnBullet(shooter.position.x, shooter.position.y, worldCoords.x, worldCoords.y)

        // move head direction when shoots
        val unitDirection = (worldCoords - shooter.position.xy).normalize()
        shooter.move.targetRoll = atan2(unitDirection.y, unitDirection.x)// + PI.toFloat()*2) % PI.toFloat()
      }
    }
  }

  fun handleMouseUp() {
    removeFlame()
  }

  fun getWorldCoordFromClick(x : Float, y : Float): Vec2 {
    val ivp = Mat4(camera.viewProjMatrix).invert()
    val newX = x/canvasWidth * 2 - 1
    val newY = y/canvasHeight * (-2) + 1
    val worldPositions = Vec4(newX, newY, 0f, 1f) * ivp
    return worldPositions.xy
  }

  fun spawnShooter(x: Float, y: Float) : GameObject {
    val meshList = listOf(anaMesh, mercyMesh, lucioMesh)
    val mesh = meshList[(0..2).random()]
    val shooter = GameObject(mesh).apply {
      position.set(x, y)
      scale.set(0.4f, 0.4f)
    }
    gameObjects += shooter
    shooterObjects += shooter
    return shooter
  }

  fun spawnDoom(x: Float, y: Float) : GameObject {
    val enemy = GameObject(doomMesh).apply {
      position.set(x, y)
      scale.set(0.5f, 0.5f)
      move = object : GameObject.Motion(this) {
        override operator fun invoke(dt : Float, t : Float) {
          gameObject["textureScale"]?.set(1f, 1f)
          val a = 0.01f
          val dX = a*(2*sin(2*t)*cos(t))
          val dY = a*(2*sin(2*t)*sin(t))
          val newX = gameObject.position.x + dX * moveVelocity
          val newY = gameObject.position.y + dY * moveVelocity
          gameObject.position.xy = Vec2(newX, newY) 
        }
      }
    }
    gameObjects += enemy
    doomObjects += enemy
    return enemy
  }

  fun spawnBullet(x: Float, y: Float, targetX: Float, targetY: Float) : GameObject {
    val bullet = GameObject(bulletMesh).apply {
      position.set(x, y)
      scale.set(0.05f, 0.05f)
      
      move = object : GameObject.Motion(this) {
        override var targetPosition = Vec2(targetX, targetY)
        override var isMoving = true
        override var moveVelocity = 3.0f

        override operator fun invoke(dt : Float, t : Float) {
          gameObject["textureScale"]?.set(1f, 1f)
          super.invoke(dt, t)
        }
      }
    }
    gameObjects += bullet
    bulletObjects += bullet
    return bullet
  }

  fun removeBullet() {
    for (bullet in ArrayList(bulletObjects)){
      if ((bullet.move.targetPosition - bullet.position.xy).length() < 0.5f){
        bulletObjects.remove(bullet)
        gameObjects.remove(bullet)
      }
    }
  }

  fun spawnBoom(x: Float, y: Float, t: Float) : GameObject {
    val boom = GameObject(boomMesh).apply {
      spawnedTime = t
      scale.set(0.5f, 0.5f)
      position.set(x, y)
      move = object : GameObject.Motion(this) {
        override operator fun invoke(dt : Float, t : Float) {
          gameObject["textureScale"]?.set(6f, 6f)
          gameObject["textureOffset"]?.set(floor(t*20.0f),floor(t*20.0f/6.0f)) 
        }
      }
    }
    gameObjects += boom
    boomObjects += boom
    return boom
  }

  fun spawnFlame(x: Float, y: Float, v: Float, r: Float, t: Vec2) : GameObject {
    val flame = GameObject(flameMesh).apply {
      position.set(x, y)
      scale.set(0.3f, 0.3f)
      
      move = object : GameObject.Motion(this) {
        override var isMoving = true
        override var rollVelocity = 100.0f
        override var moveVelocity = v
        override var targetRoll = r
        override var targetPosition = t
      }
    }
    gameObjects += flame
    flameObjects += flame
    return flame
  }

  fun removeBoom(dt : Float, t : Float) {
    for (boom in ArrayList(boomObjects)){
      if ((t - boom.spawnedTime) / dt > 36.0f){
        boomObjects.remove(boom)
        gameObjects.remove(boom)
      }
    }
  }

  fun removeFlame() {
    for (flame in ArrayList(flameObjects)){
      flameObjects.remove(flame)
      gameObjects.remove(flame)
    }
  }

  fun handleCollision(t : Float) {
    // shooter doom collisions
    //TODO: re-spawn shooter
    for (shooter in ArrayList(shooterObjects)) {
      for (doom in ArrayList(doomObjects)){
        if (isCollided(shooter, doom)){
          gameObjects.remove(shooter)
          shooterObjects.remove(shooter)
          if (shooter in selectedObjects){
            selectedObjects.remove(shooter)
          }
          spawnBoom(shooter.position.x, shooter.position.y, t)

          //TODO: cooldown timer
          val randY = (-10..10).random()/10.0f
          spawnShooter(-3.0f, randY)

          lives -= 1
          if (currLives == 0) {
            currLives = 10
            currScore = 0
          }
        }
      }
    } 

    // bullet doom collisions
    for (bullet in ArrayList(bulletObjects)) {
      for (doom in ArrayList(doomObjects)){
        if (isCollided(bullet, doom)){
          gameObjects.remove(bullet)
          gameObjects.remove(doom)
          bulletObjects.remove(bullet)
          doomObjects.remove(doom)
          spawnBoom(doom.position.x, doom.position.y, t)
        
          //TODO: cooldown timer
          val randY = (-10..10).random()/10.0f
          spawnDoom(0.5f, randY)

          score += 1
        }
      }
    }
  }

  fun isCollided(x : GameObject, y : GameObject) : Boolean {
    return (x.position.xy - y.position.xy).length() < 0.5
  }

  fun getLives() : Int {
    return lives
  }

  fun getScore() : Int {
    return score
  }

}

