package com.example.familywallet.presentacion.autenticacion

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.familywallet.ui.validarEmail
import com.example.familywallet.ui.validarPassword

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaRegistro(
    authVM: AuthViewModel,
    onRegistroOk: () -> Unit,
    onVolverLogin: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var pass by remember { mutableStateOf("") }
    var pass2 by remember { mutableStateOf("") }

    var emailError by remember { mutableStateOf<String?>(null) }
    var passError by remember { mutableStateOf<String?>(null) }
    var pass2Error by remember { mutableStateOf<String?>(null) }
    var generalError by remember { mutableStateOf<String?>(null) }
    var cargando by remember { mutableStateOf(false) }

    val mismatch by remember(pass, pass2) {
        mutableStateOf(pass.isNotBlank() && pass2.isNotBlank() && pass != pass2)
    }
    val mismatchMsg = if (mismatch) "Las contraseñas no coinciden" else null

    fun validarCampos(): Boolean {
        emailError = validarEmail(email.trim())
        passError = validarPassword(pass)
        pass2Error = when {
            pass2.isBlank() -> "Repite la contraseña"
            else -> null
        }
        return emailError == null && passError == null && pass2Error == null && !mismatch
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(0.9f),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text("Crear cuenta", style = MaterialTheme.typography.headlineMedium)

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it; emailError = null; generalError = null },
                    label = { Text("Correo") },
                    singleLine = true,
                    isError = emailError != null,
                    supportingText = { emailError?.let { Text(it, color = MaterialTheme.colorScheme.error) } },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = pass,
                    onValueChange = { pass = it; passError = null; generalError = null },
                    label = { Text("Contraseña") },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    isError = passError != null || mismatch,
                    supportingText = {
                        (passError ?: mismatchMsg)?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = pass2,
                    onValueChange = { pass2 = it; pass2Error = null; generalError = null },
                    label = { Text("Repite la contraseña") },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    isError = pass2Error != null || mismatch,
                    supportingText = {
                        (pass2Error ?: mismatchMsg)?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                generalError?.let { Text(it, color = MaterialTheme.colorScheme.error) }

                Button(
                    onClick = {
                        if (!validarCampos()) return@Button
                        cargando = true
                        authVM.registrar(
                            email = email.trim(),
                            pass = pass,
                            onOk = {
                                cargando = false
                                generalError = null
                                onRegistroOk()
                            },
                            onError = { msg ->
                                cargando = false
                                generalError = msg
                            }
                        )
                    },
                    enabled = !cargando,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(if (cargando) "Creando..." else "Crear cuenta")
                }

                TextButton(onClick = onVolverLogin) { Text("Volver a iniciar sesión") }
            }
        }
    }
}






