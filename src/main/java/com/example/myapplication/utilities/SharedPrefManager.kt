package com.example.myapplication.utilities

import android.content.Context
import com.example.myapplication.model.HighScore
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

object SharedPrefManager {

    fun saveHighScores(highScores: List<HighScore>, context: Context) {
        val prefs = context.getSharedPreferences("high_scores", Context.MODE_PRIVATE)
        val json = Gson().toJson(highScores)
        prefs.edit().putString("top_scores", json).apply()
    }

    fun loadHighScores(context: Context): List<HighScore> {
        val prefs = context.getSharedPreferences("high_scores", Context.MODE_PRIVATE)
        val json = prefs.getString("top_scores", null)
        return if (json != null) {
            val type = object : TypeToken<List<HighScore>>() {}.type
            Gson().fromJson(json, type)
        } else emptyList()
    }
}
