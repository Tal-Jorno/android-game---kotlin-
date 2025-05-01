package com.example.myapplication

import android.animation.ObjectAnimator
import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.graphics.Rect
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.animation.LinearInterpolator
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.animation.doOnEnd
import com.google.android.material.button.MaterialButton
import com.google.android.material.textview.MaterialTextView
import com.example.myapplication.logic.GameManager
import com.example.myapplication.utilities.Constants
import kotlin.random.Random
import android.view.MotionEvent
import android.os.Handler
import android.os.Looper
import android.widget.TextView
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager

enum class Direction {
    LEFT, RIGHT
}


class MainActivity : AppCompatActivity() {

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
        val step = 55
        val newX = when (direction) {
            Direction.LEFT -> (spaceShip.x - step).coerceAtLeast(0f)
            Direction.RIGHT -> (spaceShip.x + step).coerceAtMost(gameArea.width - spaceShip.width.toFloat())
        }
        spaceShip.x = newX
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findViews()
        gameManager = GameManager(main_IMG_hearts.size)
        initViews()
        gameArea.post {
            startAsteroidSpawner()


            val size = 200
            val params = FrameLayout.LayoutParams(size, size)

            params.leftMargin = gameArea.width / 2 - spaceShip.width - 100
            params.topMargin = gameArea.height - gameArea.height / 4

            spaceShip.layoutParams = params

            gameArea.addView(spaceShip)

        }
    }

    private fun startAsteroidSpawner() {
        Thread {
            while (gameRunning) {
                runOnUiThread {
                    spawnAsteroid(speed)
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

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        val vibratorManager = getSystemService(Context.VIBRATOR_MANAGER_SERVICE)
                                as VibratorManager
                        val vibrator = vibratorManager.defaultVibrator
                        vibrator.vibrate(
                            VibrationEffect.createOneShot(
                                300,
                                VibrationEffect.DEFAULT_AMPLITUDE
                            )
                        )
                    } else {
                        val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
                        vibrator.vibrate(300)
                    }

                    crashText.alpha = 1f
                    Handler(Looper.getMainLooper()).postDelayed({
                        crashText.alpha = 0f
                    }, 500)

                    refreshUI()
                }
            }
            start()
            this.doOnEnd {
                if (!is_crush && gameRunning) {
                    gameManager.score()
                    refreshUI()
                }
                gameArea.removeView(asteroid)
            }
        }
    }


    private fun initViews() {
        main_LBL_score.text = gameManager.score.toString()

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

        refreshUI()
    }


    private fun refreshUI() {
        //Lost:
        if (gameManager.isGameOver) {
            Log.d("Game Status", "Game Over! " + gameManager.score)
            changeActivity("😭Game Over! ", gameManager.score)
            gameRunning = false
            finish()

        } else { // Ongoing game:
            main_LBL_score.text = gameManager.score.toString()
            if (gameManager.crashes != 0) {
                main_IMG_hearts[main_IMG_hearts.size - gameManager.crashes]
                    .visibility = View.INVISIBLE
            }
        }
    }

    private fun changeActivity(message: String, score: Int) {
        val intent = Intent(this, ScoreActivity::class.java)
        val bundle = Bundle()
        bundle.putString(Constants.BundleKeys.MESSAGE_KEY, message)
        bundle.putInt(Constants.BundleKeys.SCORE_KEY, score)
        intent.putExtras(bundle)
        startActivity(intent)
        finish()
    }

    private fun findViews() {
        gameArea = findViewById(R.id.game_area)
        main_LBL_score = findViewById(R.id.main_LBL_score)
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
}