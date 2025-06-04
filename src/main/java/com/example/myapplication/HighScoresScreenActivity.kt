package com.example.myapplication

import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.commit
import com.example.myapplication.fragments.HighScoreListFragment
import com.example.myapplication.fragments.HighScoreMapFragment
import com.example.myapplication.viewmodel.SharedHighScoreViewModel
import android.widget.Button


class HighScoresScreenActivity : AppCompatActivity() {

    private val sharedViewModel: SharedHighScoreViewModel by viewModels()
    private var mapFragmentAdded = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_high_scores)

        supportFragmentManager.beginTransaction()
            .replace(R.id.high_scores_list_container, HighScoreListFragment())
            .commit()

        findViewById<Button>(R.id.highScore_BTN_backToMenu).setOnClickListener {
            finish()
        }
    }

}

