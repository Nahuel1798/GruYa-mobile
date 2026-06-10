package com.example.gruya.ui.utils

import com.example.gruya.R

fun getProviderIcon(serviceType: String): Int {
    return when (serviceType.uppercase()) {
        "AUXILIO" -> R.drawable.ic_auxilio
        "GOMERIA" -> R.drawable.ic_gomeria
        "MECANICO" -> R.drawable.ic_mecanico
        else -> R.drawable.ic_auxilio
    }
}