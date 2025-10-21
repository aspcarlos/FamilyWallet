package com.example.familywallet.presentacion.autenticacion

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.ktx.Firebase
import com.google.firebase.auth.ktx.auth
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class AuthViewModel : ViewModel() {

    private val auth = Firebase.auth

    /** REGISTRO + envío de verificación. Cierra sesión para obligar a verificar. */
    fun registrar(
        email: String,
        pass: String,
        onOk: () -> Unit,
        onError: (String) -> Unit
    ) = viewModelScope.launch {
        try {
            auth.createUserWithEmailAndPassword(email, pass).await()

            // Enviar correo de verificación
            auth.currentUser?.sendEmailVerification()?.await()

            // Importante: cerrar sesión para que no entre sin verificar
            auth.signOut()

            onOk()
        } catch (e: Exception) {
            val msg = when (e) {
                is FirebaseAuthUserCollisionException ->
                    "Ya existe una cuenta con este correo."
                is FirebaseAuthWeakPasswordException ->
                    "La contraseña es demasiado débil (mínimo 6 caracteres)."
                is FirebaseAuthInvalidCredentialsException ->
                    "El correo no es válido."
                else -> e.localizedMessage ?: "No se pudo crear la cuenta."
            }
            onError(msg)
        }
    }

    /** LOGIN. Si no está verificado, reenvía verificación y bloquea acceso. */
    fun login(
        email: String,
        pass: String,
        onOk: () -> Unit,
        onError: (String) -> Unit
    ) = viewModelScope.launch {
        try {
            auth.signInWithEmailAndPassword(email, pass).await()
            val user = auth.currentUser

            if (user != null && !user.isEmailVerified) {
                // reenvía por si no lo recibió
                user.sendEmailVerification().await()
                auth.signOut()
                onError("Debes verificar tu correo. Te hemos enviado un enlace.")
                return@launch
            }

            onOk()
        } catch (e: Exception) {
            val msg = when (e) {
                is FirebaseAuthInvalidCredentialsException ->
                    "Correo o contraseña incorrectos."
                else -> e.localizedMessage ?: "Error al iniciar sesión."
            }
            onError(msg)
        }
    }

    /** Reset password (igual que tenías) */
    fun enviarResetPassword(
        email: String,
        onOk: () -> Unit,
        onError: (String) -> Unit
    ) = viewModelScope.launch {
        try {
            auth.sendPasswordResetEmail(email).await()
            onOk()
        } catch (e: Exception) {
            onError(e.localizedMessage ?: "Error enviando correo de restablecimiento.")
        }
    }

    fun logout() {
        auth.signOut()
    }
}




