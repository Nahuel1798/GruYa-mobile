package com.example.gruya.data

import android.content.Context
import com.example.gruya.domain.model.Role

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

    fun saveRole(role: Role) {
        prefs.edit()
            .putString("role", role.name)
            .apply()
    }

    fun getRole(): Role? {
        val roleName = prefs.getString("role", null)
        return roleName?.let { Role.valueOf(it) }
    }

    fun clearSession() {
        prefs.edit().clear().apply()
    }
}