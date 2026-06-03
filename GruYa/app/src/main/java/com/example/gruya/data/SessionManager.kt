package com.example.gruya.data

import android.content.Context

class SessionManager(context: Context) {

    private val prefs = context.getSharedPreferences(
        "session",
        Context.MODE_PRIVATE
    )

    fun saveJwt(token: String) {
        prefs.edit()
            .putString("jwt", token)
            .apply()
    }

    fun getJwt(): String {
        return prefs.getString("jwt", "") ?: ""
    }

    fun clearSession() {
        prefs.edit().clear().apply()
    }
}