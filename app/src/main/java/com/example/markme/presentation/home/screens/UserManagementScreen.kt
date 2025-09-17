package com.example.markme.presentation.home.screens

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.example.markme.data.local.User
import com.example.markme.presentation.home.viewmodel.CreateUserState
import com.example.markme.presentation.home.viewmodel.UserManagementViewModel
import com.example.markme.utils.generateQrCode

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserManagementScreen(
    onBackPressed: () -> Unit,
    paddingValues: PaddingValues,
    viewModel: UserManagementViewModel = hiltViewModel()
) {
    val users by viewModel.users.collectAsState(emptyList())
    val createUserState by viewModel.createUserState.collectAsState()

    var showCreateUserDialog by remember { mutableStateOf(false) }
    var showQrCodes by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.loadUsers()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
    ) {
        // Top Bar
        CenterAlignedTopAppBar(
            title = { Text("User Management") },
            navigationIcon = {
                IconButton(onClick = onBackPressed) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                }
            },
            actions = {
                IconButton(onClick = { showQrCodes = !showQrCodes }) {
                    Icon(
                        imageVector = if (showQrCodes) Icons.Default.QrCode else Icons.Default.QrCode2,
                        contentDescription = "Toggle QR Codes"
                    )
                }
            }
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Action Buttons
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            FilledTonalButton(
                onClick = { showCreateUserDialog = true },
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Default.PersonAdd, contentDescription = null)
                Spacer(Modifier.width(6.dp))
                Text("Add User")
            }

            OutlinedButton(
                onClick = { viewModel.loadUsers() },
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Default.Refresh, contentDescription = null)
                Spacer(Modifier.width(6.dp))
                Text("Refresh")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Users List
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(users) { user ->
                UserCard(
                    user = user, showQrCode = showQrCodes
                )
            }
        }
    }

    if (showCreateUserDialog) {
        CreateUserDialog(
            onDismiss = {
                showCreateUserDialog = false
                viewModel.resetCreateUserState()
            },
            onCreateUser = { name, email, password ->
                viewModel.createUser(name, email, password)
            },
            createUserState = createUserState
        )
    }

    LaunchedEffect(createUserState) {
        if (createUserState is CreateUserState.Success) {
            showCreateUserDialog = false
            viewModel.resetCreateUserState()
        }
    }
}

@Composable
fun UserCard(user: User, showQrCode: Boolean = false) {
    var qrBitmap by remember { mutableStateOf<Bitmap?>(null) }

    LaunchedEffect(user.qrCode, showQrCode) {
        if (showQrCode) qrBitmap = generateQrCode(user.qrCode)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        elevation = CardDefaults.cardElevation(6.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(user.fullName, style = MaterialTheme.typography.titleMedium)
                    Text(
                        text = user.email,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Role: ${user.role.name}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (showQrCode) {
                    qrBitmap?.let { bitmap ->
                        Card(
                            modifier = Modifier.size(80.dp),
                            shape = MaterialTheme.shapes.small,
                            elevation = CardDefaults.cardElevation(2.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) {
                            Image(
                                bitmap = bitmap.asImageBitmap(),
                                contentDescription = "QR Code",
                                modifier = Modifier.padding(8.dp)
                            )
                        }
                    }
                }
            }

            if (showQrCode) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "QR ID: ${user.qrCode}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun CreateUserDialog(
    onDismiss: () -> Unit,
    onCreateUser: (String, String, String) -> Unit,
    createUserState: CreateUserState?
) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    AlertDialog(onDismissRequest = onDismiss, title = { Text("Create New User") }, text = {
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Full Name") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth()
            )

            if (createUserState is CreateUserState.Error) {
                Text(
                    text = createUserState.message,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }, confirmButton = {
        Button(
            onClick = { onCreateUser(name, email, password) },
            enabled = name.isNotBlank() && email.isNotBlank() && password.isNotBlank() && createUserState !is CreateUserState.Loading
        ) {
            if (createUserState is CreateUserState.Loading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(16.dp), color = MaterialTheme.colorScheme.onPrimary
                )
            } else {
                Text("Create")
            }
        }
    }, dismissButton = {
        TextButton(onClick = onDismiss) {
            Text("Cancel")
        }
    })
}