package com.example.familywallet

import android.app.Application
import com.google.firebase.FirebaseApp

class FamilyWalletApp : Application() {
    override fun onCreate() {
        super.onCreate()
        // Inicializa Firebase al arrancar la app
        FirebaseApp.initializeApp(this)
    }
}
