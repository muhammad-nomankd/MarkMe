package com.example.markme.presentation.home.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.markme.data.local.User
import com.example.markme.domain.model.UserRole
import com.example.markme.domain.repository.AttendanceRepository
import com.example.markme.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@HiltViewModel
class UserManagementViewModel @Inject constructor(
    private val repository: AttendanceRepository, private val authRepository: AuthRepository
) : ViewModel() {

    private val _users = MutableStateFlow<List<User>>(emptyList())
    val users: StateFlow<List<User>> = _users.asStateFlow()

    private val _createUserState = MutableStateFlow<CreateUserState>(CreateUserState.Idle)
    val createUserState: StateFlow<CreateUserState> = _createUserState.asStateFlow()

    fun loadUsers() {
        viewModelScope.launch {
            repository.getAllUsers().collect { users ->
                _users.value = users
            }
        }
    }

    fun createUser(name: String, email: String, password: String) {
        viewModelScope.launch {
            try {
                _createUserState.value = CreateUserState.Loading
                val user = User(
                    fullName = name, email = email, password = password, role = UserRole.STUDENT
                )
                authRepository.signUp(user)
                _createUserState.value = CreateUserState.Success
                loadUsers() // Refresh the list
            } catch (e: Exception) {
                _createUserState.value = CreateUserState.Error(e.message ?: "Failed to create user")
            }
        }
    }

    fun resetCreateUserState() {
        _createUserState.value = CreateUserState.Idle
    }
}

sealed class CreateUserState {
    object Idle : CreateUserState()
    object Loading : CreateUserState()
    object Success : CreateUserState()
    data class Error(val message: String) : CreateUserState()
}
