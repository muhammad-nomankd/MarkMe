package com.example.markme.presentation.authentication

import com.example.markme.data.local.User

sealed class AuthState {
    data object Idle : AuthState()
    data object Loading : AuthState()
    data class SignedIn(val user: User) : AuthState()
    data class SignedUp(val user: User) : AuthState()
    data class Error(val message: String) : AuthState()
}