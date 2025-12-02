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
import com.example.familywallet.ui.validarEmail

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaOlvidoPassword(
    authVM: AuthViewModel,
    onEnviado: () -> Unit,
    onVolverLogin: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    var enviado by remember { mutableStateOf(false) }
    var loading by remember { mutableStateOf(false) }

    // flag para no mostrar error al entrar
    var emailTouched by remember { mutableStateOf(false) }

    val rawEmailError = validarEmail(email)
    val emailErrorToShow = if (emailTouched) rawEmailError else null
    val formOk = rawEmailError == null && !loading

    Scaffold { inner ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(inner)
                .padding(24.dp),
            contentAlignment = Alignment.Center
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
                        emailTouched = true
                        error = null
                        enviado = false
                    },
                    label = { Text("Correo electrónico") },
                    singleLine = true,
                    isError = emailErrorToShow != null,
                    supportingText = { emailErrorToShow?.let { Text(it) } },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    modifier = Modifier.fillMaxWidth(0.85f)
                )

                Button(
                    onClick = {
                        loading = true
                        authVM.enviarResetPassword(
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
                        .fillMaxWidth(0.6f)
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




