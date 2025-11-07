package com.example.familywallet.presentacion.autenticacion

import android.annotation.SuppressLint
import android.provider.Settings
import android.util.Patterns
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

@SuppressLint("HardwareIds")
@Composable
fun PantallaLogin(
    authVM: AuthViewModel,
    onLoginOk: () -> Unit,
    onRegistro: () -> Unit,
    onOlvido: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // ID único del dispositivo
    val deviceId by remember {
        mutableStateOf(
            Settings.Secure.getString(
                context.contentResolver,
                Settings.Secure.ANDROID_ID
            ) ?: "unknown"
        )
    }

    var email by remember { mutableStateOf("") }
    var pass by remember { mutableStateOf("") }
    var showPass by remember { mutableStateOf(false) }
    var loading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    fun validarEmail(e: String): String? =
        if (e.isBlank()) "El correo es obligatorio"
        else if (!Patterns.EMAIL_ADDRESS.matcher(e).matches()) "Correo no válido"
        else null

    fun validarPass(p: String): String? =
        if (p.isBlank()) "La contraseña es obligatoria"
        else if (p.length < 6) "Mínimo 6 caracteres"
        else null

    Scaffold { inner ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(inner)
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "Iniciar sesión",
                    style = MaterialTheme.typography.titleLarge
                )

                Spacer(Modifier.height(24.dp))

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Correo") },
                    enabled = !loading,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(12.dp))

                OutlinedTextField(
                    value = pass,
                    onValueChange = { pass = it },
                    label = { Text("Contraseña") },
                    enabled = !loading,
                    visualTransformation = if (showPass)
                        VisualTransformation.None
                    else
                        PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = { showPass = !showPass }) {
                        Text(if (showPass) "Ocultar" else "Mostrar")
                    }
                }

                error?.let {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = it,
                        color = MaterialTheme.colorScheme.error
                    )
                }

                Spacer(Modifier.height(16.dp))

                Button(
                    onClick = {
                        // Validación rápida
                        val errEmail = validarEmail(email.trim())
                        val errPass = validarPass(pass)

                        if (errEmail != null) {
                            error = errEmail
                            return@Button
                        }
                        if (errPass != null) {
                            error = errPass
                            return@Button
                        }

                        loading = true
                        error = null

                        scope.launch {
                            authVM.login(
                                email = email.trim(),
                                pass = pass,
                                deviceId = deviceId,
                                onOk = {
                                    loading = false
                                    onLoginOk()
                                },
                                onError = { msg ->
                                    loading = false
                                    error = msg
                                }
                            )
                        }
                    },
                    enabled = !loading,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(if (loading) "Entrando..." else "Entrar")
                }

                Spacer(Modifier.height(12.dp))

                TextButton(
                    onClick = onOlvido,
                    enabled = !loading
                ) { Text("He olvidado mi contraseña") }

                Spacer(Modifier.height(8.dp))

                TextButton(
                    onClick = onRegistro,
                    enabled = !loading
                ) { Text("Crear cuenta nueva") }
            }
        }
    }
}








