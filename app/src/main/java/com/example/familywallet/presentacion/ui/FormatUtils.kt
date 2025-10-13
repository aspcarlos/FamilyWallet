package com.example.familywallet.presentacion.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import java.text.NumberFormat
import java.util.Currency

/**
 * Devuelve un NumberFormat memorizado por código de moneda (USD, EUR, MXN, etc.).
 * Cuando el código cambia, Compose recompone y el símbolo se actualiza solo.
 */
@Composable
fun rememberCurrencyFormatter(currencyCode: String): java.text.NumberFormat {
    return remember(currencyCode) {
        NumberFormat.getCurrencyInstance().apply {
            currency = Currency.getInstance(currencyCode)
        }
    }
}




