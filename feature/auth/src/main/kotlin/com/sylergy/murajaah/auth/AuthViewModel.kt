package com.sylergy.murajaah.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sylergy.murajaah.auth.model.AuthState
import com.sylergy.murajaah.auth.model.UserSession
import com.sylergy.murajaah.auth.storage.SessionStorage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AuthViewModel(
    private val sessionStorage: SessionStorage
) : ViewModel() {

    val authState: StateFlow<AuthState> = sessionStorage.authState

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    fun signInAsGuest(name: String = "Guest Hafidh") {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                sessionStorage.startGuestSession(name)
            } catch (e: Exception) {
                _errorMessage.value = e.localizedMessage ?: "Failed to initialize guest session"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun signInWithCloud(email: String, name: String) {
        viewModelScope.launch {
            if (email.isBlank() || !email.contains("@")) {
                _errorMessage.value = "Please enter a valid email address."
                return@launch
            }
            _isLoading.value = true
            _errorMessage.value = null
            try {
                // Generates token and establishes authenticated session
                val token = "jwt_" + System.currentTimeMillis()
                val userId = "usr_" + email.takeWhile { it != '@' }
                sessionStorage.saveAuthenticatedSession(
                    userId = userId,
                    name = name.ifBlank { "Hafidh" },
                    email = email,
                    token = token
                )
            } catch (e: Exception) {
                _errorMessage.value = e.localizedMessage ?: "Sign in failed"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun markOnboarded() {
        sessionStorage.updateSession { it.copy(onboarded = true) }
    }

    fun updateSettings(
        narration: String? = null,
        sessionMode: String? = null,
        revisionStyle: String? = null,
        dailyPages: Int? = null,
        cardSize: String? = null
    ) {
        sessionStorage.updateSession { current ->
            current.copy(
                narration = narration ?: current.narration,
                sessionMode = sessionMode ?: current.sessionMode,
                revisionStyle = revisionStyle ?: current.revisionStyle,
                dailyPages = dailyPages ?: current.dailyPages,
                cardSize = cardSize ?: current.cardSize
            )
        }
    }

    fun signOut() {
        sessionStorage.clearSession()
    }

    fun clearError() {
        _errorMessage.value = null
    }
}
