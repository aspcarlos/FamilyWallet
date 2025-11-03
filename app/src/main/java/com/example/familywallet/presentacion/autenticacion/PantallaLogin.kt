package com.example.familywallet.presentacion.autenticacion

import android.content.Intent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
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
    val context = LocalContext.current

    var email by remember { mutableStateOf("") }
    var pass by remember { mutableStateOf("") }
    var showPass by remember { mutableStateOf(false) }
    var loading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    // NUEVO: flags para no mostrar errores al entrar
    var emailTouched by remember { mutableStateOf(false) }
    var passTouched by remember { mutableStateOf(false) }

    val rawEmailError = validarEmail(email)
    val rawPassError  = if (pass.isBlank()) "La contraseña es obligatoria" else null

    // Solo los mostramos si el usuario ha tocado el campo
    val emailErrorToShow = if (emailTouched) rawEmailError else null
    val passErrorToShow  = if (passTouched)  rawPassError  else null

    val canSubmit  = rawEmailError == null && rawPassError == null && !loading

    val requiereVerificacion = remember(error) {
        error?.startsWith("Debes verificar tu correo") == true
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
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
                onValueChange = {
                    email = it
                    emailTouched = true
                    error = null
                },
                label = { Text("Correo") },
                isError = emailErrorToShow != null,
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                    keyboardType = KeyboardType.Email
                ),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            emailErrorToShow?.let { Text(it, color = MaterialTheme.colorScheme.error) }

            OutlinedTextField(
                value = pass,
                onValueChange = {
                    pass = it
                    passTouched = true
                    error = null
                },
                label = { Text("Contraseña") },
                singleLine = true,
                isError = passErrorToShow != null,
                visualTransformation = if (showPass) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    TextButton(onClick = { showPass = !showPass }) {
                        Text(if (showPass) "Ocultar" else "Ver")
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )
            passErrorToShow?.let { Text(it, color = MaterialTheme.colorScheme.error) }

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
                        onError = { msg ->
                            loading = false
                            error = msg
                        }
                    )
                },
                enabled = canSubmit,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (loading) "Entrando..." else "Entrar")
            }

            // Mensaje de error general
            error?.let {
                Text(it, color = MaterialTheme.colorScheme.error)
            }

            // CTA específico si falta verificación: abrir app de correo
            if (requiereVerificacion) {
                OutlinedButton(
                    onClick = {
                        val intent = Intent(Intent.ACTION_MAIN).apply {
                            addCategory(Intent.CATEGORY_APP_EMAIL)
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        }
                        runCatching { context.startActivity(intent) }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Abrir mi correo")
                }
                Text(
                    "Revisa la bandeja de entrada y spam. Si no te llega, vuelve a intentar iniciar sesión para reenviar el enlace.",
                    style = MaterialTheme.typography.bodySmall
                )
            }

            TextButton(onClick = onOlvido) { Text("¿Olvidaste tu contraseña?") }
            TextButton(onClick = onRegistro) { Text("Crear cuenta") }
        }
    }
}






