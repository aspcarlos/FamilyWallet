package com.example.familywallet.presentacion.autenticacion

import android.util.Patterns
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

@Composable
fun PantallaRegistro(
    authVM: AuthViewModel,
    onRegistroOk: (String, String) -> Unit,
    onVolverLogin: () -> Unit
) {
    val scope = rememberCoroutineScope()

    var email by remember { mutableStateOf("") }
    var pass by remember { mutableStateOf("") }
    var pass2 by remember { mutableStateOf("") }
    var showPass by remember { mutableStateOf(false) }
    var cargando by remember { mutableStateOf(false) }
    var generalError by remember { mutableStateOf<String?>(null) }

    fun validar(): Boolean {
        val e = email.trim()
        if (e.isBlank()) {
            generalError = "El correo es obligatorio"
            return false
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(e).matches()) {
            generalError = "Correo no válido"
            return false
        }
        if (pass.length < 6) {
            generalError = "La contraseña debe tener al menos 6 caracteres"
            return false
        }
        if (pass != pass2) {
            generalError = "Las contraseñas no coinciden"
            return false
        }
        return true
    }

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
                    text = "Registro",
                    style = MaterialTheme.typography.titleLarge
                )

                Spacer(Modifier.height(24.dp))

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Correo") },
                    enabled = !cargando,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(12.dp))

                OutlinedTextField(
                    value = pass,
                    onValueChange = { pass = it },
                    label = { Text("Contraseña") },
                    visualTransformation = if (showPass)
                        VisualTransformation.None
                    else
                        PasswordVisualTransformation(),
                    enabled = !cargando,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(12.dp))

                OutlinedTextField(
                    value = pass2,
                    onValueChange = { pass2 = it },
                    label = { Text("Repetir contraseña") },
                    visualTransformation = if (showPass)
                        VisualTransformation.None
                    else
                        PasswordVisualTransformation(),
                    enabled = !cargando,
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

                generalError?.let {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = it,
                        color = MaterialTheme.colorScheme.error
                    )
                }

                Spacer(Modifier.height(16.dp))

                Button(
                    onClick = {
                        if (!validar()) return@Button
                        cargando = true
                        generalError = null

                        scope.launch {
                            authVM.registrar(
                                email = email.trim(),
                                pass = pass,
                                onOk = {
                                    cargando = false
                                    generalError = null
                                    // devolvemos datos por si los quieres reusar
                                    onRegistroOk(email.trim(), pass)
                                },
                                onError = { msg ->
                                    cargando = false
                                    generalError = msg
                                }
                            )
                        }
                    },
                    enabled = !cargando,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(if (cargando) "Creando cuenta..." else "Registrarse")
                }

                Spacer(Modifier.height(12.dp))

                TextButton(
                    onClick = onVolverLogin,
                    enabled = !cargando
                ) {
                    Text("Volver al inicio de sesión")
                }
            }
        }
    }
}







