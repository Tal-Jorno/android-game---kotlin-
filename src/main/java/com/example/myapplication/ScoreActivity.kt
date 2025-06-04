package com.example.myapplication

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.myapplication.model.HighScore
import com.example.myapplication.utilities.SharedPrefManager
import com.google.android.gms.location.*
import com.google.android.material.textview.MaterialTextView

class ScoreActivity : AppCompatActivity() {

    private lateinit var score_LBL_status: MaterialTextView
    private lateinit var score_LBL_score: MaterialTextView
    private lateinit var score_LBL_distance: MaterialTextView
    private lateinit var score_LBL_coins: MaterialTextView
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback
    private val LOCATION_PERMISSION_REQUEST_CODE = 1001

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_score)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        findViews()
        initViews()
        checkLocationPermission()

        findViewById<Button>(R.id.score_BTN_backToMenu).setOnClickListener {
            val intent = Intent(this, MenuActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun findViews() {
        score_LBL_status = findViewById(R.id.score_LBL_status)
        score_LBL_score = findViewById(R.id.score_LBL_score)
        score_LBL_distance = findViewById(R.id.score_LBL_distance)
        score_LBL_coins = findViewById(R.id.score_LBL_coins)
    }

    private fun initViews() {
        val bundle = intent.extras
        val message = bundle?.getString("message", "END GAME 💀")
        val astroidScore = bundle?.getInt("Asteroid passed", 0)
        val distance = bundle?.getInt("Distance", 0)
        val coins = bundle?.getInt("Coins", 0)

        score_LBL_status.text = message
        score_LBL_score.text = "Asteroid Passed: $astroidScore"
        score_LBL_distance.text = "Distance: $distance"
        score_LBL_coins.text = "Coins: $coins"

        requestLocationAndSave(distance ?: 0)
    }

    private fun requestLocationAndSave(distance: Int) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED) {
            Log.d("SAVE_SCORE", "Location permission not granted.")
            saveFallbackScore(distance)
            return
        }

        locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 1000).build()
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                val location = result.lastLocation
                if (location != null) {
                    val lat = location.latitude
                    val lng = location.longitude
                    val newScore = HighScore(distance, lat, lng)
                    val oldScores = SharedPrefManager.loadHighScores(this@ScoreActivity).toMutableList()
                    oldScores.add(newScore)
                    oldScores.sortByDescending { it.distance }
                    val top10 = oldScores.take(10)
                    Log.d("SAVE_SCORE", "Saving score with active location: $lat, $lng")
                    SharedPrefManager.saveHighScores(top10, this@ScoreActivity)
                } else {
                    Log.d("SAVE_SCORE", "Location is null in active request")
                    saveFallbackScore(distance)
                }
                fusedLocationClient.removeLocationUpdates(this)
            }
        }

        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, mainLooper)
    }

    private fun saveFallbackScore(distance: Int) {
        val newScore = HighScore(distance, 0.0, 0.0)
        val oldScores = SharedPrefManager.loadHighScores(this).toMutableList()
        oldScores.add(newScore)
        oldScores.sortByDescending { it.distance }
        val top10 = oldScores.take(10)
        Log.d("SAVE_SCORE", "Saved fallback score with dummy location")
        SharedPrefManager.saveHighScores(top10, this)
    }

    private fun checkLocationPermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        }
    }
}
