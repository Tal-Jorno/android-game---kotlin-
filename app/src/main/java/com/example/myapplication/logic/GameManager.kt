package com.example.myapplication.logic

class GameManager(private val lifeCount:Int = 3){
    var score: Int = 0
        private set

    var currentIndex: Int = 0
        private set

    var crashes: Int = 0
        private set


    val isGameOver: Boolean
        get() = crashes == lifeCount

    fun score(){
        score ++
    }
    fun crush()
    {
        crashes++
    }

}