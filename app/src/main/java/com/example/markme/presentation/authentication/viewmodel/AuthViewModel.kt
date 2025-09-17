package com.example.markme.presentation.authentication.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.markme.data.local.SessionManager
import com.example.markme.data.local.User
import com.example.markme.domain.repository.AuthRepository
import com.example.markme.presentation.authentication.AuthState
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _uiState = MutableStateFlow<AuthState>(AuthState.Idle)
    val uiState: StateFlow<AuthState> = _uiState.asStateFlow()

    private val _currentUser = MutableStateFlow<AuthState>(AuthState.Idle)
    val currentUser: StateFlow<AuthState> = _currentUser.asStateFlow()

    fun signIn(email: String, password: String) {
        viewModelScope.launch {
            _uiState.value = AuthState.Loading
            try {
                val user = authRepository.signIn(email, password)
                if (user != null) {
                    sessionManager.saveLoggedInUser(user.id, user.email, user.role)
                    _uiState.value = AuthState.SignedIn(user)
                } else {
                    _uiState.value = AuthState.Error("Invalid email or password")
                }
            } catch (e: Exception) {
                _uiState.value = AuthState.Error(e.message ?: "Sign-in failed")
            }
        }
    }

    fun logout(){
        // Clear persisted session and reset UI state
        sessionManager.clear()
        _uiState.value = AuthState.Idle
    }
    fun signUp(user: User) {
        viewModelScope.launch {
            _uiState.value = AuthState.Loading
            try {
                // Let repository enforce email uniqueness and throw if duplicate
                authRepository.signUp(user)
                sessionManager.saveLoggedInUser(user.id, user.email, user.role)
                _uiState.value = AuthState.SignedUp(user)
            } catch (e: IllegalArgumentException) {
                // Duplicate account or validation from repository
                _uiState.value = AuthState.Error(e.message ?: "User already exists")
            } catch (e: Exception) {
                _uiState.value = AuthState.Error(e.message ?: "Sign-up failed")
            }
        }
    }

    fun reset() {
        _uiState.value = AuthState.Idle
    }

    fun doesUserAlreadyExist(email: String,password: String){
        viewModelScope.launch {
            authRepository.doesUserAlreadyExists(email, password)
        }
    }
}