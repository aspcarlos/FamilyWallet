package com.example.familywallet.presentacion.autenticacion

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.familywallet.ui.validarEmail

@Composable
fun PantallaOlvidoPassword(
    vm: AuthViewModel = viewModel(),
    onVolverLogin: () -> Unit = {},
    onEnviado: () -> Unit = {}
) {
    var email by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    var enviado by remember { mutableStateOf(false) }
    var loading by remember { mutableStateOf(false) }

    val emailError = validarEmail(email)
    val formOk = emailError == null && !loading

    Scaffold(containerColor = MaterialTheme.colorScheme.background) { inner ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(inner)
                .padding(24.dp),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text("¿Olvidaste tu contraseña?", style = MaterialTheme.typography.headlineMedium)
                Text(
                    "Te enviaremos un correo para restablecerla.",
                    style = MaterialTheme.typography.bodyMedium
                )

                OutlinedTextField(
                    value = email,
                    onValueChange = {
                        email = it
                        error = null
                        enviado = false
                    },
                    label = { Text("Correo electrónico") },
                    singleLine = true,
                    isError = emailError != null,
                    supportingText = { emailError?.let { Text(it, color = MaterialTheme.colorScheme.error) } },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    modifier = Modifier.fillMaxWidth()
                )

                Button(
                    onClick = {
                        loading = true
                        vm.enviarResetPassword(
                            email = email.trim(),
                            onOk = {
                                loading = false
                                error = null
                                enviado = true
                                onEnviado()
                                onVolverLogin()
                            },
                            onError = { msg ->
                                loading = false
                                enviado = false
                                error = msg
                            }
                        )
                    },
                    enabled = formOk,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                ) {
                    Text(if (loading) "Enviando..." else "Enviar enlace")
                }

                if (enviado) {
                    Text("¡Listo! Revisa tu correo con el enlace de restablecimiento.")
                }
                error?.let { Text(it, color = MaterialTheme.colorScheme.error) }

                TextButton(onClick = onVolverLogin) { Text("Volver a iniciar sesión") }
            }
        }
    }
}


