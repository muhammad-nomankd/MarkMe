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
import androidx.compose.material3.Button
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusDirection
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
import com.example.markme.data.local.User
import com.example.markme.domain.model.UserRole
import com.example.markme.presentation.authentication.AuthState
import com.example.markme.presentation.authentication.viewmodel.AuthViewModel
import kotlinx.coroutines.launch

private val FieldVerticalSpacing = 0.dp
private val LabelToFieldSpacing = 4.dp

@Composable
fun SignUpScreen(
    paddingValues: PaddingValues,
    onNavigateBackToSignIn: () -> Unit,
    onNavigateToAdminHome: () -> Unit,
    onNavigateToStudentHome: () -> Unit
) {
    var studentOrAdmin by remember { mutableStateOf("student") }

    var fullName by remember { mutableStateOf("") }

    var email by remember { mutableStateOf("") }

    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }

    // ViewModel and UI state
    val authViewModel: AuthViewModel = hiltViewModel()
    val authState by authViewModel.uiState.collectAsState()
    val isLoading = authState is AuthState.Loading
    val errorMessage = (authState as? AuthState.Error)?.message
    val scope = rememberCoroutineScope()

    // Validation error states
    var fullNameError by remember { mutableStateOf<String?>(null) }
    var emailError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }
    var confirmPasswordError by remember { mutableStateOf<String?>(null) }

    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    val emailFocusRequester = remember { FocusRequester() }
    val passwordFocusRequester = remember { FocusRequester() }
    val confirmPasswordFocusRequester = remember { FocusRequester() }

    fun isValidEmail(input: String): Boolean {
        val emailRegex = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$")
        return emailRegex.matches(input)
    }

    // Trigger sign-up with the selected role; navigation is handled in authState observer
    val onSignUp: () -> Unit = {
        val role = if (studentOrAdmin == "admin") UserRole.ADMIN else UserRole.STUDENT
        val user = User(
            fullName = fullName.trim(), email = email.trim(), role = role, password = password
        )
        scope.launch { authViewModel.signUp(user) }
    }

    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .imePadding()
            .verticalScroll(scrollState)
            .padding(horizontal = 24.dp, vertical = 12.dp)
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
            "Create your account",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground,
            fontWeight = FontWeight.Normal
        )

        Spacer(Modifier.height(8.dp))
        Text(
            "Please fill the details to sign up",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Normal
        )

        // Auth state feedback
        when (authState) {
            is AuthState.Loading -> {
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "Creating account...",
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

            is AuthState.SignedUp -> {
                val user = (authState as AuthState.SignedUp).user
                if (user.role.name == "ADMIN") {
                    onNavigateToAdminHome()
                } else {
                    onNavigateToStudentHome()
                }
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
                        painter = painterResource(id = R.drawable.person_icon),
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

            Spacer(modifier = Modifier.width(4.dp))

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
                    .padding(horizontal = 16.dp, vertical = 10.dp)
                    .weight(1f),
                contentAlignment = Alignment.Center) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.admin_icon),
                        contentDescription = "Admin",
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

        Spacer(modifier = Modifier.height(12.dp))

        // Full name
        Text(
            text = "Full name",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Normal,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(LabelToFieldSpacing))
        OutlinedTextField(
            value = fullName,
            onValueChange = {
                fullName = it
                fullNameError = null
            },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            placeholder = {
                Text(
                    text = "Enter your full name", color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            leadingIcon = {
                Icon(
                    painter = painterResource(id = R.drawable.person_icon),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            isError = fullNameError != null,
            supportingText = {
                fullNameError?.let { Text(text = it, color = MaterialTheme.colorScheme.error) }
            },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Text, imeAction = ImeAction.Next
            ),
            keyboardActions = KeyboardActions(
                onNext = { focusManager.moveFocus(FocusDirection.Down) }),
            shape = RoundedCornerShape(8.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                errorBorderColor = MaterialTheme.colorScheme.error
            )
        )

        Spacer(modifier = Modifier.height(FieldVerticalSpacing))

        // Email
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
                emailError = null
            },
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(emailFocusRequester),
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
            isError = emailError != null,
            supportingText = {
                emailError?.let { Text(text = it, color = MaterialTheme.colorScheme.error) }
            },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email, imeAction = ImeAction.Next
            ),
            keyboardActions = KeyboardActions(
                onNext = { focusManager.moveFocus(FocusDirection.Down) }),
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
                    text = "Create a password", color = MaterialTheme.colorScheme.onSurfaceVariant
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
                keyboardType = KeyboardType.Password, imeAction = ImeAction.Next
            ),
            keyboardActions = KeyboardActions(
                onNext = { focusManager.moveFocus(FocusDirection.Down) }),
            shape = RoundedCornerShape(8.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                errorBorderColor = MaterialTheme.colorScheme.error
            )
        )

        Spacer(modifier = Modifier.height(FieldVerticalSpacing))

        // Confirm Password
        Text(
            text = "Confirm Password",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Normal,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(LabelToFieldSpacing))
        OutlinedTextField(
            value = confirmPassword,
            onValueChange = {
                confirmPassword = it
                confirmPasswordError = null
            },
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(confirmPasswordFocusRequester),
            singleLine = true,
            placeholder = {
                Text(
                    text = "Re-enter your password",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
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
                TextButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                    Text(
                        text = if (confirmPasswordVisible) "Hide" else "Show",
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            },
            isError = confirmPasswordError != null,
            supportingText = {
                confirmPasswordError?.let {
                    Text(
                        text = it, color = MaterialTheme.colorScheme.error
                    )
                }
            },
            visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password, imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
                onDone = {
                    keyboardController?.hide()
                    focusManager.clearFocus(force = true)
                    onSignUp()
                }),
            shape = RoundedCornerShape(8.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                errorBorderColor = MaterialTheme.colorScheme.error
            )
        )

        Spacer(modifier = Modifier.height(FieldVerticalSpacing))

        Button(
            onClick = {
                if (fullName.isBlank() || fullName.trim().length < 2) {
                    fullNameError = "Enter your full name"
                    return@Button
                } else {
                    fullNameError = null
                }
                if (email.isBlank()) {
                    emailError = "Email is required"
                    return@Button
                } else if (!isValidEmail(email.trim())) {
                    emailError = "Enter a valid email"
                    return@Button
                } else {
                    emailError = null
                }
                if (password.isBlank()) {
                    passwordError = "Password is required"
                    return@Button
                } else if (password.length < 6) {
                    passwordError = "Password must be at least 6 characters"
                    return@Button
                } else {
                    passwordError = null
                }
                if (confirmPassword != password) {
                    confirmPasswordError = "Passwords do not match"
                    return@Button
                } else if (confirmPassword.isBlank()) {
                    confirmPasswordError = "Confirm your password"
                    return@Button
                } else {
                    confirmPasswordError = null
                }

                onSignUp()
            },
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
                ),
            enabled = !isLoading
        ) {
            Text(
                text = "Sign Up",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onPrimary
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Back to sign in
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
        ) {
            Text(
                text = "Already have an account? ",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 16.sp,
                textAlign = TextAlign.Center,
            )
            TextButton(onClick = { onNavigateBackToSignIn() }) {
                Text(
                    text = "Sign in",
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp
                )
            }
        }
    }
}
