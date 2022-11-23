package com.example.stressmeter.ui.results

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class ResultsViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "A graph showing your Stress Levels"
    }
    val text: LiveData<String> = _text
}