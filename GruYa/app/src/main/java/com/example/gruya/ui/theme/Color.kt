package com.example.gruya.ui.theme

import androidx.compose.ui.graphics.Color

// --- Core Palette ---
val PrimaryYellow = Color(0xFFFFB95F)    // Amarillo firma GruYa
val SecondaryGrey = Color(0xFF64748B)    // Gris azulado moderno
val TertiarySlate = Color(0xFF94A3B8)    // Slate claro

// --- Light Theme ---
val PrimaryLight = PrimaryYellow
val OnPrimaryLight = Color(0xFF1A1C1E)

val SecondaryLight = SecondaryGrey
val OnSecondaryLight = Color(0xFFFFFFFF)

val TertiaryLight = TertiarySlate
val OnTertiaryLight = Color(0xFFFFFFFF)

val BackgroundLight = Color(0xFFF8FAFC)  // Fondo muy claro, casi blanco
val OnBackgroundLight = Color(0xFF0F172A)

val SurfaceLight = Color(0xFFFFFFFF)      // Cards blancas
val OnSurfaceLight = Color(0xFF0F172A)
val SurfaceVariantLight = Color(0xFFF1F5F9) // Contraste suave para cards/inputs
val OnSurfaceVariantLight = Color(0xFF475569)

val OutlineLight = Color(0xFFCBD5E1)     // Bordes sutiles
val ErrorLight = Color(0xFFEF4444)

// --- Dark Theme (Minimalist & Premium) ---
val PrimaryDark = PrimaryYellow
val OnPrimaryDark = Color(0xFF0F1113)

val SecondaryDark = Color(0xFF94A3B8)
val OnSecondaryDark = Color(0xFF0F172A)

val TertiaryDark = Color(0xFF475569)
val OnTertiaryDark = Color(0xFFF8FAFC)

val BackgroundDark = Color(0xFF0B0C0E)   // Negro profundo
val OnBackgroundDark = Color(0xFFF1F5F9)

val SurfaceDark = Color(0xFF1E2025)      // Color para Cards (más contraste con el fondo)
val OnSurfaceDark = Color(0xFFF1F5F9)
val SurfaceVariantDark = Color(0xFF2B2F36) // Variante para elementos destacados o estados hover
val OnSurfaceVariantDark = Color(0xFF94A3B8)

val OutlineDark = Color(0xFF3F444D)      // Bordes un poco más visibles para minimalismo definido
val ErrorDark = Color(0xFFF87171)

// --- Status Colors ---
val Success = Color(0xFF22C55E)
val Warning = Color(0xFFF59E0B)
val Info = Color(0xFF3B82F6)
