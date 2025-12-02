package com.example.familywallet.presentacion.autenticacion

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import com.example.familywallet.ui.validarEmail
import com.example.familywallet.ui.validarPassword
import com.example.familywallet.ui.validarConfirmacion

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
    var emailError by remember { mutableStateOf<String?>(null) }
    var passError by remember { mutableStateOf<String?>(null) }
    var pass2Error by remember { mutableStateOf<String?>(null) }

    fun validar(): Boolean {
        val e = email.trim()

        val eError = validarEmail(e)
        val pError = validarPassword(pass)
        val cError = validarConfirmacion(pass, pass2)

        emailError = eError
        passError = pError
        pass2Error = cError

        generalError = null

        return eError == null && pError == null && cError == null
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

                // EMAIL
                OutlinedTextField(
                    value = email,
                    onValueChange = {
                        email = it
                        // Si quieres validar al escribir:
                        emailError = null
                    },
                    label = { Text("Correo") },
                    enabled = !cargando,
                    modifier = Modifier.fillMaxWidth(),
                    isError = emailError != null
                )
                emailError?.let {
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = it,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                Spacer(Modifier.height(12.dp))

                // PASSWORD
                OutlinedTextField(
                    value = pass,
                    onValueChange = {
                        pass = it
                        // passError = null  // si quieres limpiar al escribir
                    },
                    label = { Text("Contraseña") },
                    visualTransformation = if (showPass)
                        VisualTransformation.None
                    else
                        PasswordVisualTransformation(),
                    enabled = !cargando,
                    modifier = Modifier.fillMaxWidth(),
                    isError = passError != null
                )
                passError?.let {
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = it,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                Spacer(Modifier.height(12.dp))

                // CONFIRMACIÓN PASSWORD
                OutlinedTextField(
                    value = pass2,
                    onValueChange = {
                        pass2 = it
                        // pass2Error = null
                    },
                    label = { Text("Repetir contraseña") },
                    visualTransformation = if (showPass)
                        VisualTransformation.None
                    else
                        PasswordVisualTransformation(),
                    enabled = !cargando,
                    modifier = Modifier.fillMaxWidth(),
                    isError = pass2Error != null
                )
                pass2Error?.let {
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = it,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = { showPass = !showPass }) {
                        Text(if (showPass) "Ocultar" else "Mostrar")
                    }
                }

                // Error general (por ejemplo, Firebase, backend, etc.)
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









