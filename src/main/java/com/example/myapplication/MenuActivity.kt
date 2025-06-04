package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.databinding.ActivityMenuBinding
import com.google.android.material.button.MaterialButton

class MenuActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMenuBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMenuBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnStartButtons.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.putExtra("SENSOR_MODE", false)
            startActivity(intent)
        }

        binding.btnStartSensor.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.putExtra("SENSOR_MODE", true)
            startActivity(intent)
        }

        binding.menuBTNHighscores.setOnClickListener {
            val intent = Intent(this, HighScoresScreenActivity::class.java)
            startActivity(intent)
        }

        findViewById<MaterialButton>(R.id.btn_exit).setOnClickListener {
            finishAffinity()
        }
    }
}
