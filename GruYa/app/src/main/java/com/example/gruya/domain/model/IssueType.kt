package com.example.gruya.domain.model

enum class IssueType {
    NEUMATICO_PINCHADO,
    SIN_COMBUSTIBLE,
    FALLA_MOTOR,
    NECESITA_REMOLQUE,
    BATERIA_DESCARGADA,
    LLAVE_OLVIDADA
}

val IssueType.displayName: String
    get() = when (this) {
        IssueType.NEUMATICO_PINCHADO -> "Neumático Pinchado"
        IssueType.SIN_COMBUSTIBLE -> "Sin Combustible"
        IssueType.FALLA_MOTOR -> "Falla de Motor"
        IssueType.NECESITA_REMOLQUE -> "Necesita Remolque"
        IssueType.BATERIA_DESCARGADA -> "Batería Descargada"
        IssueType.LLAVE_OLVIDADA -> "Llave Olvidada"
    }
