package com.example.myapplication

import android.os.Bundle
import android.animation.ObjectAnimator
import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.graphics.Rect
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.media.MediaPlayer
import android.os.*
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.animation.LinearInterpolator
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.animation.doOnEnd
import com.example.myapplication.logic.GameManager
import com.example.myapplication.utilities.Constants
import com.google.android.material.button.MaterialButton
import com.google.android.material.textview.MaterialTextView
import kotlin.random.Random

private lateinit var sensorManager: SensorManager
private var accelerometer: Sensor? = null
private var sensorMode = false

private var crashPlayer: MediaPlayer? = null
private var coinPlayer: MediaPlayer? = null

private var defaultSpeed = 1f
private var boostedSpeed = 2f
private var slowSpeed = 0.5f

enum class Direction {
    LEFT, RIGHT
}

class MainActivity : AppCompatActivity(), SensorEventListener {

    private lateinit var gameArea: FrameLayout
    private var speed = 1f
    private lateinit var main_LBL_score: MaterialTextView
    private lateinit var main_BTN_yes: MaterialButton
    private lateinit var main_BTN_no: MaterialButton
    private lateinit var main_IMG_hearts: Array<AppCompatImageView>
    private lateinit var gameManager: GameManager
    private lateinit var spaceShip: ImageView
    private var gameRunning = true
    private var moveHandler: Handler? = null
    private var moveRunnable: Runnable? = null
    private var isMoving = false
    private lateinit var crashText: TextView
    private lateinit var main_LBL_coins: TextView
    private lateinit var main_LBL_distance: MaterialTextView
    private var distanceHandler: Handler? = null
    private var distanceRunnable: Runnable? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        sensorMode = intent.getBooleanExtra("SENSOR_MODE", false)
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

        crashPlayer = MediaPlayer.create(this, R.raw.crash)
        coinPlayer = MediaPlayer.create(this, R.raw.money)

        findViews()
        gameManager = GameManager(main_IMG_hearts.size)
        initViews()

        gameArea.post {
            startAsteroidSpawner()
            startDistanceCounter()

            val size = 200
            val params = FrameLayout.LayoutParams(size, size)
            params.leftMargin = gameArea.width / 2 - spaceShip.width - 100
            params.topMargin = gameArea.height - gameArea.height / 4

            spaceShip.layoutParams = params
            gameArea.addView(spaceShip)
        }
    }

    override fun onResume() {
        super.onResume()
        if (sensorMode) {
            accelerometer?.also {
                sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_GAME)
            }
        }
    }

    override fun onPause() {
        super.onPause()

        if (sensorMode) {
            sensorManager.unregisterListener(this)
        }

        gameRunning = false
        moveHandler?.removeCallbacks(moveRunnable!!)
        distanceHandler?.removeCallbacks(distanceRunnable!!)
    }


    override fun onDestroy() {
        super.onDestroy()
        crashPlayer?.release()
        crashPlayer = null
        coinPlayer?.release()
        coinPlayer = null
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (!sensorMode || event == null) return

        val x = event.values[0]
        val y = event.values[1]

        val moveAmount = (x * -5).toInt()
        val newX = (spaceShip.x + moveAmount).coerceIn(0f, gameArea.width - spaceShip.width.toFloat())
        spaceShip.x = newX

        speed = when {
            y < 3 -> boostedSpeed
            y > 6 -> slowSpeed
            else -> defaultSpeed
        }
    }


    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    private fun startMoving(direction: Direction) {
        if (isMoving) return
        isMoving = true

        moveHandler = Handler(Looper.getMainLooper())
        moveRunnable = object : Runnable {
            override fun run() {
                moveSpaceship(direction)
                moveHandler?.postDelayed(this, 50)
            }
        }
        moveHandler?.post(moveRunnable!!)
    }

    private fun stopMoving() {
        isMoving = false
        moveHandler?.removeCallbacks(moveRunnable!!)
    }

    private fun moveSpaceship(direction: Direction) {
        val step = 100
        val newX = when (direction) {
            Direction.LEFT -> (spaceShip.x - step).coerceAtLeast(0f)
            Direction.RIGHT -> (spaceShip.x + step).coerceAtMost(gameArea.width - spaceShip.width.toFloat())
        }
        spaceShip.x = newX
    }

    private fun startAsteroidSpawner() {
        Thread {
            while (gameRunning) {
                runOnUiThread {
                    spawnAsteroid(speed)
                    if (Random.nextBoolean()) {
                        spawnCoin()
                    }
                }
                Thread.sleep(1000)
            }
        }.start()
    }

    private fun spawnAsteroid(speedMultiplier: Float) {
        val asteroid = ImageView(this)
        asteroid.setImageResource(R.drawable.astroid)
        val size = 100
        val params = FrameLayout.LayoutParams(size, size)
        params.leftMargin = Random.nextInt(0, gameArea.width)
        asteroid.layoutParams = params

        gameArea.addView(asteroid)

        val screenHeight = Resources.getSystem().displayMetrics.heightPixels.toFloat()
        val baseSpeed = 3000L
        val duration = (baseSpeed / speedMultiplier).toLong()

        ObjectAnimator.ofFloat(asteroid, "translationY", 0f, screenHeight).apply {
            this.duration = duration
            var is_crush = false
            interpolator = LinearInterpolator()
            addUpdateListener {
                if (!gameRunning) {
                    cancel()
                }
                val spaceshipRect = Rect()
                spaceShip.getHitRect(spaceshipRect)
                val asteroidRect = Rect()
                asteroid.getHitRect(asteroidRect)

                if (Rect.intersects(spaceshipRect, asteroidRect)) {
                    is_crush = true
                    this.cancel()
                    gameManager.crush()

                    crashPlayer?.release()
                    crashPlayer = MediaPlayer.create(this@MainActivity, R.raw.crash)
                    crashPlayer?.start()

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        val vibratorManager = getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
                        val vibrator = vibratorManager.defaultVibrator
                        vibrator.vibrate(
                            VibrationEffect.createOneShot(300, VibrationEffect.DEFAULT_AMPLITUDE)
                        )
                    } else {
                        val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
                        vibrator.vibrate(300)
                    }

                    crashText.alpha = 1f
                    Handler(Looper.getMainLooper()).postDelayed({ crashText.alpha = 0f }, 500)
                    refreshUI()
                }
            }
            start()
            this.doOnEnd {
                if (!is_crush && gameRunning) {
                    gameManager.astroidScore()
                    refreshUI()
                }
                gameArea.removeView(asteroid)
            }
        }
    }


    private fun spawnCoin() {
        val coin = ImageView(this)
        coin.setImageResource(R.drawable.coin)
        val size = 100
        val params = FrameLayout.LayoutParams(size, size)
        params.leftMargin = Random.nextInt(0, gameArea.width - size)
        coin.layoutParams = params

        gameArea.addView(coin)

        val screenHeight = Resources.getSystem().displayMetrics.heightPixels.toFloat()
        val duration = (3000 / speed).toLong()

        ObjectAnimator.ofFloat(coin, "translationY", 0f, screenHeight).apply {
            this.duration = duration
            var is_crush = false
            interpolator = LinearInterpolator()
            addUpdateListener {
                val spaceshipRect = Rect()
                spaceShip.getHitRect(spaceshipRect)
                val coinRect = Rect()
                coin.getHitRect(coinRect)

                if (Rect.intersects(spaceshipRect, coinRect)) {
                    is_crush = true
                    this.cancel()
                    gameArea.removeView(coin)
                    gameManager.coinScore()

                    coinPlayer?.release()
                    coinPlayer = MediaPlayer.create(this@MainActivity, R.raw.money)
                    coinPlayer?.start()

                    refreshUI()
                }
            }
            doOnEnd {
                if (!is_crush && gameRunning) {
                    refreshUI()
                }
                gameArea.removeView(coin)
            }
            start()
        }
    }


    private fun findViews() {
        gameArea = findViewById(R.id.game_area)
        main_LBL_score = findViewById(R.id.main_LBL_score)
        main_LBL_coins = findViewById(R.id.main_LBL_coins)
        main_LBL_distance = findViewById(R.id.main_LBL_distance)
        main_BTN_yes = findViewById(R.id.main_BTN_yes)
        main_BTN_no = findViewById(R.id.main_BTN_no)
        main_IMG_hearts = arrayOf(
            findViewById(R.id.main_IMG_heart0),
            findViewById(R.id.main_IMG_heart1),
            findViewById(R.id.main_IMG_heart2)
        )
        spaceShip = ImageView(this)
        spaceShip.setImageResource(R.drawable.spaceship)
        crashText = findViewById(R.id.crash_text)
    }

    private fun initViews() {
        main_LBL_score.text = gameManager.astroidScore.toString()

        main_BTN_yes.setOnTouchListener { view, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> startMoving(Direction.LEFT)
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    view.performClick()
                    stopMoving()
                }
            }
            true
        }

        main_BTN_no.setOnTouchListener { view, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> startMoving(Direction.RIGHT)
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    view.performClick()
                    stopMoving()
                }
            }
            true
        }

        findViewById<MaterialButton>(R.id.main_BTN_highscores).setOnClickListener {
            val intent = Intent(this, HighScoresScreenActivity::class.java)
            startActivity(intent)
        }

        refreshUI()
    }


    private fun refreshUI() {
        main_LBL_distance.text = "Distance: ${gameManager.distance}"
        main_LBL_score.text = "Astroid avoided ${gameManager.astroidScore}"
        main_LBL_coins.text = "Coins: ${gameManager.getCoins()}"

        if (gameManager.isGameOver) {
            Log.d("Game Status", "Game Over! Score: ${gameManager.astroidScore}")
            changeActivity("😭 Game Over!", gameManager.astroidScore, gameManager.distance, gameManager.getCoins())
            gameRunning = false
            finish()
        } else {
            if (gameManager.crashes != 0) {
                main_IMG_hearts[main_IMG_hearts.size - gameManager.crashes].visibility = View.INVISIBLE
            }
        }
    }

    private fun startDistanceCounter() {
        distanceHandler = Handler(Looper.getMainLooper())
        distanceRunnable = object : Runnable {
            override fun run() {
                if (gameRunning) {
                    gameManager.incrementDistance()
                    refreshUI()
                    distanceHandler?.postDelayed(this, 500)
                }
            }
        }
        distanceHandler?.postDelayed(distanceRunnable!!, 500)
    }


    private fun changeActivity(message: String, score: Int, distance: Int, coins: Int) {
        val intent = Intent(this, ScoreActivity::class.java)
        val bundle = Bundle()
        bundle.putString(Constants.BundleKeys.MESSAGE_KEY, message)
        bundle.putInt("Asteroid passed", score)
        bundle.putInt("Distance", distance)
        bundle.putInt("Coins", coins)
        bundle.putBoolean("Sensor Mode", sensorMode)
        intent.putExtras(bundle)
        startActivity(intent)
        finish()
    }

}
