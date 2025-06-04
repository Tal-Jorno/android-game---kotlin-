package com.example.myapplication.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class SharedHighScoreViewModel : ViewModel() {
    private val _selectedLocation = MutableLiveData<Pair<Double, Double>>()
    val selectedLocation: LiveData<Pair<Double, Double>> = _selectedLocation

    fun setLocation(lat: Double, lng: Double) {
        _selectedLocation.value = Pair(lat, lng)
    }
}
