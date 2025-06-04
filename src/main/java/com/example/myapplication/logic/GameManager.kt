package com.example.myapplication.logic

class GameManager(private val lifeCount:Int = 3){
    var astroidScore: Int = 0
        private set

    var coinScore: Int = 0
        private set

    var currentIndex: Int = 0
        private set

    var crashes: Int = 0
        private set

    var distance = 0
        private set

    val isGameOver: Boolean
        get() = crashes == lifeCount

    fun astroidScore(){
        astroidScore ++
    }

    fun coinScore(){
        coinScore++
    }

    fun getCoins(): Int {
        return coinScore
    }
    fun crush() {
        crashes++
    }

    fun incrementDistance() {
        distance++
    }

}