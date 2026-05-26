package com.example.gruya

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel

class MainViewModel: ViewModel() {
    var isLogged by mutableStateOf(false)
        private set

    fun onLoginSuccess(){
        isLogged = true
    }
}