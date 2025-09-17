package com.example.markme.data.local

import android.content.Context
import android.content.SharedPreferences
import com.example.markme.domain.model.UserRole
import androidx.core.content.edit

class SessionManager(private val context: Context) {

    private val prefs: SharedPreferences by lazy {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun saveLoggedInUser(id: String, email: String, role: UserRole) {
        prefs.edit {
            putString(KEY_USER_ID, id)
                .putString(KEY_EMAIL, email)
                .putString(KEY_ROLE, role.name)
        }
    }

    fun getRole(): UserRole? {
        val roleName = prefs.getString(KEY_ROLE, null) ?: return null
        return try {
            UserRole.valueOf(roleName)
        } catch (_: IllegalArgumentException) {
            null
        }
    }

    fun getUserId(): String? = prefs.getString(KEY_USER_ID, null)

    fun getEmail(): String? = prefs.getString(KEY_EMAIL, null)

    fun clear() {
        prefs.edit { clear() }
    }

    companion object {
        private const val PREFS_NAME = "session_prefs"
        private const val KEY_USER_ID = "user_id"
        private const val KEY_EMAIL = "email"
        private const val KEY_ROLE = "role"
    }
}
