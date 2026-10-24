package com.sylergy.murajaah.auth.storage

import android.content.Context
import android.content.SharedPreferences
import com.sylergy.murajaah.auth.model.AuthState
import com.sylergy.murajaah.auth.model.UserSession
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.json.Json
import java.util.UUID

interface SessionStorage {
    val authState: StateFlow<AuthState>
    fun startGuestSession(name: String = "Guest Hafidh"): UserSession
    fun saveAuthenticatedSession(userId: String, name: String, email: String, token: String): UserSession
    fun updateSession(update: (UserSession) -> UserSession)
    fun clearSession()
}

class AuthPreferences(context: Context) : SessionStorage {

    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val json = Json { ignoreUnknownKeys = true; encodeDefaults = true }

    private val _authState = MutableStateFlow<AuthState>(AuthState.Loading)
    override val authState: StateFlow<AuthState> = _authState.asStateFlow()

    init {
        loadSession()
    }

    private fun loadSession() {
        val serialized = prefs.getString(KEY_SESSION, null)
        if (serialized != null) {
            try {
                val session = json.decodeFromString<UserSession>(serialized)
                _authState.value = if (session.isGuest) {
                    AuthState.Guest(session)
                } else {
                    AuthState.Authenticated(session)
                }
                return
            } catch (_: Exception) {
                // In case of parsing corruption, fall through to Unauthenticated
            }
        }
        _authState.value = AuthState.Unauthenticated
    }

    override fun startGuestSession(name: String): UserSession {
        val guestId = "guest_" + UUID.randomUUID().toString().take(8)
        val session = UserSession(
            userId = guestId,
            name = name,
            isGuest = true,
            onboarded = false
        )
        saveSession(session)
        return session
    }

    override fun saveAuthenticatedSession(
        userId: String,
        name: String,
        email: String,
        token: String
    ): UserSession {
        val current = (_authState.value as? AuthState.Guest)?.session
        val session = UserSession(
            userId = userId,
            name = name,
            email = email,
            token = token,
            isGuest = false,
            narration = current?.narration ?: "hafs",
            sessionMode = current?.sessionMode ?: "focus",
            revisionStyle = current?.revisionStyle ?: "ordered",
            dailyPages = current?.dailyPages ?: 5,
            dailyMinutes = current?.dailyMinutes ?: 15,
            cardSize = current?.cardSize ?: "ten",
            onboarded = current?.onboarded ?: false
        )
        saveSession(session)
        return session
    }

    override fun updateSession(update: (UserSession) -> UserSession) {
        val currentSession = when (val state = _authState.value) {
            is AuthState.Guest -> state.session
            is AuthState.Authenticated -> state.session
            else -> null
        } ?: return

        val updated = update(currentSession)
        saveSession(updated)
    }

    private fun saveSession(session: UserSession) {
        prefs.edit()
            .putString(KEY_SESSION, json.encodeToString(UserSession.serializer(), session))
            .apply()

        _authState.value = if (session.isGuest) {
            AuthState.Guest(session)
        } else {
            AuthState.Authenticated(session)
        }
    }

    override fun clearSession() {
        prefs.edit().remove(KEY_SESSION).apply()
        _authState.value = AuthState.Unauthenticated
    }

    companion object {
        private const val PREFS_NAME = "murajaah_auth_prefs"
        private const val KEY_SESSION = "user_session"
    }
}

class InMemorySessionStorage : SessionStorage {
    private val _authState = MutableStateFlow<AuthState>(AuthState.Unauthenticated)
    override val authState: StateFlow<AuthState> = _authState.asStateFlow()

    override fun startGuestSession(name: String): UserSession {
        val session = UserSession(
            userId = "guest_test",
            name = name,
            isGuest = true,
            onboarded = false
        )
        _authState.value = AuthState.Guest(session)
        return session
    }

    override fun saveAuthenticatedSession(
        userId: String,
        name: String,
        email: String,
        token: String
    ): UserSession {
        val session = UserSession(
            userId = userId,
            name = name,
            email = email,
            token = token,
            isGuest = false,
            onboarded = false
        )
        _authState.value = AuthState.Authenticated(session)
        return session
    }

    override fun updateSession(update: (UserSession) -> UserSession) {
        val current = when (val state = _authState.value) {
            is AuthState.Guest -> state.session
            is AuthState.Authenticated -> state.session
            else -> null
        } ?: return
        val updated = update(current)
        _authState.value = if (updated.isGuest) AuthState.Guest(updated) else AuthState.Authenticated(updated)
    }

    override fun clearSession() {
        _authState.value = AuthState.Unauthenticated
    }
}
