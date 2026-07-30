package com.example.gruya.data

import android.content.Context
import com.example.gruya.domain.model.Role

open class SessionManager(context: Context) {

    private val prefs by lazy {
        context.getSharedPreferences(
            "session",
            Context.MODE_PRIVATE
        )
    }

    fun saveJwt(token: String) {
        prefs.edit()
            .putString("jwt", token)
            .commit()
    }

    fun getJwt(): String {
        return prefs.getString("jwt", "") ?: ""
    }

    fun saveRole(role: Role) {
        prefs.edit()
            .putString("role", role.name)
            .commit()
    }

    fun getRole(): Role? {
        val roleName = prefs.getString("role", null)
        return roleName?.let { Role.valueOf(it) }
    }

    open fun saveUserId(id: Int) {
        prefs.edit()
            .putInt("userId", id)
            .apply()
    }

    open fun getUserId(): Int {
        return prefs.getInt("userId", 0)
    }

    fun clearSession() {
        prefs.edit().clear().apply()
    }
}