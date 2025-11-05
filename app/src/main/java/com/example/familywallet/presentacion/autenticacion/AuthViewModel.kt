package com.example.familywallet.presentacion.autenticacion

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.familywallet.datos.repositorios.AuthRepositorio
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.ktx.Firebase
import com.google.firebase.auth.ktx.auth
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class AuthViewModel(
    private val repo: AuthRepositorio
) : ViewModel() {

    private val auth = Firebase.auth

    fun registrar(
        email: String,
        pass: String,
        onOk: () -> Unit,
        onError: (String) -> Unit
    ) = viewModelScope.launch {
        try {
            auth.createUserWithEmailAndPassword(email, pass).await()
            auth.currentUser?.sendEmailVerification()?.await()
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

    fun login(
        email: String,
        pass: String,
        deviceId: String,
        onOk: () -> Unit,
        onError: (String) -> Unit
    ) = viewModelScope.launch {
        try {
            repo.login(email, pass, deviceId)
            onOk()
        } catch (e: Exception) {
            val msg = when (e.message) {
                "ACCOUNT_ALREADY_ACTIVE" ->
                    "Esta cuenta ya está iniciada en otro dispositivo."
                else -> e.message ?: "Error al iniciar sesión"
            }
            onError(msg)
        }
    }

    fun logout() {
        viewModelScope.launch {
            repo.logout()
        }
    }

    fun enviarResetPassword(
        email: String,
        onOk: () -> Unit,
        onError: (String) -> Unit
    ) = viewModelScope.launch {
        try {
            Firebase.auth.sendPasswordResetEmail(email).await()
            onOk()
        } catch (e: Exception) {
            onError(e.localizedMessage ?: "Error enviando correo de restablecimiento.")
        }
    }
}

class AuthViewModelFactory(
    private val repo: AuthRepositorio
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AuthViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AuthViewModel(repo) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}








