package com.example.familywallet.presentacion.autenticacion

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.familywallet.ui.validarEmail

@Composable
fun PantallaLogin(
    vm: AuthViewModel = viewModel(),
    onLoginOk: () -> Unit,
    onRegistro: () -> Unit,
    onOlvido: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var pass by remember { mutableStateOf("") }
    var showPass by remember { mutableStateOf(false) }
    var loading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    val emailError = validarEmail(email)
    val passError  = if (pass.isBlank()) "La contraseña es obligatoria" else null
    val canSubmit  = emailError == null && passError == null && !loading

    Box(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {

            Text("Iniciar sesión", style = MaterialTheme.typography.headlineMedium)

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Correo") },
                isError = emailError != null,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            emailError?.let { Text(it, color = MaterialTheme.colorScheme.error) }

            OutlinedTextField(
                value = pass,
                onValueChange = { pass = it },
                label = { Text("Contraseña") },
                singleLine = true,
                isError = passError != null,
                visualTransformation = if (showPass) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    TextButton(onClick = { showPass = !showPass }) {
                        Text(if (showPass) "Ocultar" else "Ver")
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )
            passError?.let { Text(it, color = MaterialTheme.colorScheme.error) }

            Button(
                onClick = {
                    loading = true
                    error = null

                    vm.login(
                        email = email.trim(),
                        pass  = pass,
                        onOk = {
                            loading = false
                            onLoginOk()
                        },
                        onError = {
                            loading = false
                            error = it
                        }
                    )
                },
                enabled = !loading,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (loading) "Entrando..." else "Entrar")
            }

            // Mensaje de error general (correo no registrado, contraseña mala, etc.)
            error?.let {
                Text(it, color = MaterialTheme.colorScheme.error)
            }

            TextButton(onClick = onOlvido) { Text("¿Olvidaste tu contraseña?") }
            TextButton(onClick = onRegistro) { Text("Crear cuenta") }
        }
    }
}

