package com.example.markme.presentation.authentication.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.MailOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.markme.R
import com.example.markme.presentation.authentication.AuthState
import com.example.markme.presentation.authentication.viewmodel.AuthViewModel

private val FieldVerticalSpacing = 0.dp
private val LabelToFieldSpacing = 4.dp

@Composable
fun SignInScreen(
    paddingValues: PaddingValues,
    onNavigateToSignUp: () -> Unit,
    onNavigateToAdminHome: () -> Unit,
    onNavigateToStudentHome: () -> Unit
) {

    var studentOrAdmin by remember { mutableStateOf("student") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    // ViewModel and UI state
    val authViewModel: AuthViewModel = hiltViewModel()
    val authState by authViewModel.uiState.collectAsState()
    val isLoading = authState is AuthState.Loading
    val errorMessage = (authState as? AuthState.Error)?.message

    // Validation error states
    var contactError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }

    // Focus and actions
    val passwordFocusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    fun isValidEmail(input: String): Boolean {
        val emailRegex = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$")
        return emailRegex.matches(input)
    }


    fun validateSignIn(): Boolean {
        var ok = true
        // Validate email only
        if (email.isBlank()) {
            contactError = "Email is required"
            ok = false
        } else if (!isValidEmail(email.trim())) {
            contactError = "Enter a valid email"
            ok = false
        } else {
            contactError = null
        }

        if (password.isBlank()) {
            passwordError = "Password is required"
            ok = false
        } else if (password.length < 6) {
            passwordError = "Password must be at least 6 characters"
            ok = false
        } else {
            passwordError = null
        }
        return ok
    }

    val onSignIn: () -> Unit = {
        val valid = validateSignIn()
        if (valid) {
            authViewModel.signIn(email.trim(), password)
        }
    }

    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .imePadding()
            .verticalScroll(scrollState)
            .padding(horizontal = 24.dp, vertical = 24.dp)
            .widthIn(max = 600.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {


        Box(
            modifier = Modifier
                .size(48.dp)
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary,
                            MaterialTheme.colorScheme.primaryContainer
                        )
                    ), shape = RoundedCornerShape(8.dp)
                ), contentAlignment = Alignment.Center
        ) {
            Column {
                Image(
                    painterResource(R.drawable.white_person),
                    contentDescription = "",
                    modifier = Modifier.size(42.dp)
                )
            }
        }

        Spacer(Modifier.height(12.dp))
        Text(
            "Welcome to MarkMe",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground,
            fontWeight = FontWeight.Normal
        )

        Spacer(Modifier.height(8.dp))
        Text(
            "Please sign in to continue",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Normal
        )

        // Auth state feedback
        when (authState) {
            is AuthState.Loading -> {
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "Signing in...",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            is AuthState.Error -> {
                Spacer(Modifier.height(8.dp))
                Text(
                    text = errorMessage ?: "",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }

            is AuthState.SignedIn -> {
                val user = (authState as AuthState.SignedIn).user
                if (user.role.name == "ADMIN") {
                    onNavigateToAdminHome()
                } else {
                    onNavigateToStudentHome()
                }
                // Reset to prevent repeated navigation on recomposition
                authViewModel.reset()
            }

            else -> { /* no-op */
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    MaterialTheme.colorScheme.surfaceVariant, shape = RoundedCornerShape(24.dp)
                )
                .padding(6.dp)
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .clickable { studentOrAdmin = "student" }
                    .background(
                        if (studentOrAdmin == "student") MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
                        shape = RoundedCornerShape(20.dp)
                    )
                    .border(
                        BorderStroke(
                            1.dp,
                            if (studentOrAdmin == "student") Color.Transparent else MaterialTheme.colorScheme.outline.copy(
                                alpha = 0.5f
                            )
                        ), shape = RoundedCornerShape(20.dp)
                    )
                    .padding(horizontal = 16.dp, vertical = 10.dp)
                    .weight(1f),
                contentAlignment = Alignment.Center) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = painterResource(R.drawable.person_icon),
                        contentDescription = "Student",
                        tint = if (studentOrAdmin == "student") MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        "Student",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Normal,
                        color = if (studentOrAdmin == "student") MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

            }

            Spacer(modifier = Modifier.width(16.dp))
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .clickable { studentOrAdmin = "admin" }
                    .background(
                        if (studentOrAdmin == "admin") MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
                        shape = RoundedCornerShape(20.dp)
                    )
                    .border(
                        BorderStroke(
                            1.dp,
                            if (studentOrAdmin == "admin") Color.Transparent else MaterialTheme.colorScheme.outline.copy(
                                alpha = 0.5f
                            )
                        ), shape = RoundedCornerShape(20.dp)
                    )
                    .weight(1f)
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                contentAlignment = Alignment.Center) {

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.admin_icon),
                        contentDescription = "admin",
                        tint = if (studentOrAdmin == "admin") MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        "Admin",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Normal,
                        color = if (studentOrAdmin == "admin") MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Email Address",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Normal,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(LabelToFieldSpacing))
        OutlinedTextField(
            value = email,
            onValueChange = {
                email = it
                contactError = null
            },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            placeholder = {
                Text(
                    text = "your@email.com", color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Outlined.MailOutline,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            isError = contactError != null,
            supportingText = {
                contactError?.let { Text(text = it, color = MaterialTheme.colorScheme.error) }
            },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email, imeAction = ImeAction.Next
            ),
            keyboardActions = KeyboardActions(
                onNext = { passwordFocusRequester.requestFocus() }),
            shape = RoundedCornerShape(8.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                errorBorderColor = MaterialTheme.colorScheme.error
            )
        )

        Spacer(modifier = Modifier.height(FieldVerticalSpacing))

        // Password
        Text(
            text = "Password",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Normal,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(LabelToFieldSpacing))
        OutlinedTextField(
            value = password,
            onValueChange = {
                password = it
                passwordError = null
            },
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(passwordFocusRequester),
            singleLine = true,
            placeholder = {
                Text(
                    text = "Enter your password", color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Outlined.Lock,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            trailingIcon = {
                TextButton(onClick = { passwordVisible = !passwordVisible }) {
                    Text(
                        text = if (passwordVisible) "Hide" else "Show",
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            },
            isError = passwordError != null,
            supportingText = {
                passwordError?.let { Text(text = it, color = MaterialTheme.colorScheme.error) }
            },
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password, imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
                onDone = {
                    keyboardController?.hide()
                    focusManager.clearFocus(force = true)
                    onSignIn()
                }),
            shape = RoundedCornerShape(8.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                errorBorderColor = MaterialTheme.colorScheme.error
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(
                    brush = Brush.horizontalGradient(
                        listOf(
                            MaterialTheme.colorScheme.primary,
                            MaterialTheme.colorScheme.primaryContainer
                        )
                    )
                )
                .clickable(enabled = !isLoading) { onSignIn() },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = if (studentOrAdmin == "admin") "Admin Access" else "Sign In",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onPrimary
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
        ) {
            Text(
                text = "Don't have an account? ",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 16.sp,
                textAlign = TextAlign.Center,
            )
            TextButton(onClick = { onNavigateToSignUp() }) {
                Text(
                    text = "Sign up",
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp
                )
            }
        }


    }

}