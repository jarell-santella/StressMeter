package com.example.stressmeter.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class HomeViewModel() : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "Touch the image that best captures how stressed you feel right now"
    }
    val text: LiveData<String> = _text

    // Starting grid is random
    var grid = MutableLiveData<Int>().apply {
        value = (0..2).random()
    }

    // Generate new random grid after launching app
    init {
        grid.value = (0..2).random()
    }

    // Take current grid value and increase by 1, find the remainder when divided by 3 to get current page
    fun incrementGridValue() {
        grid.value = grid.value?.plus(1)?.mod(3)
    }
}