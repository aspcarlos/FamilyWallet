package com.example.familywallet.ui
// Regex simple para validar formato de email
private val EMAIL_REGEX =
    Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")

// Valida email y devuelve mensaje de error o null si es correcto
fun validarEmail(email: String): String? {
    val e = email.trim()
    return when {
        e.isEmpty() -> "El correo es obligatorio"
        !e.contains("@") -> "El correo debe contener @"
        !EMAIL_REGEX.matches(e) -> "Correo inválido"
        else -> null
    }
}

// Valida contraseña fuerte y devuelve mensaje de error o null si es correcta
fun validarPassword(pass: String): String? {
    if (pass.isBlank()) return "La contraseña es obligatoria"
    if (pass.length < 8) return "Mínimo 8 caracteres"
    if (!pass.any { it.isLowerCase() }) return "Incluye al menos una minúscula"
    if (!pass.any { it.isUpperCase() }) return "Incluye al menos una mayúscula"
    if (!pass.any { it.isDigit() }) return "Incluye al menos un número"
    if (!pass.any { "!@#\$%^&*()-_=+[{]}|;:'\",<.>/?`~".contains(it) })
        return "Incluye al menos un símbolo"
    if (pass.any { it.isWhitespace() }) return "Sin espacios"
    return null
}

// Valida que la confirmación no esté vacía y que coincida con la contraseña
fun validarConfirmacion(pass: String, confirm: String): String? {
    return if (confirm.isBlank()) "Repite la contraseña"
    else if (pass != confirm) "Las contraseñas no coinciden" else null
}


