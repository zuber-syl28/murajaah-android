package com.sylergy.murajaah.auth.model

import kotlinx.serialization.Serializable
import java.time.ZoneId

@Serializable
data class UserSession(
    val userId: String,
    val name: String,
    val email: String? = null,
    val token: String? = null,
    val isGuest: Boolean = true,
    val narration: String = "hafs",
    val sessionMode: String = "focus",
    val revisionStyle: String = "ordered",
    val dailyPages: Int = 5,
    val dailyMinutes: Int? = 15,
    val cardSize: String = "ten",
    val onboarded: Boolean = false,
    val timezone: String = ZoneId.systemDefault().id
)

sealed interface AuthState {
    data object Loading : AuthState
    data object Unauthenticated : AuthState
    data class Guest(val session: UserSession) : AuthState
    data class Authenticated(val session: UserSession) : AuthState
}
