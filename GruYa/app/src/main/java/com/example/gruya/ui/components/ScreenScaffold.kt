package com.example.gruya.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

/**
 * Scaffold reutilizable con TopAppBar estandarizado.
 *
 * Centraliza el patrón Scaffold + TopAppBar + back button que se repite
 * en más de 10 pantallas, usando [Icons.AutoMirrored.Filled.ArrowBack]
 * como ícono de retroceso consistente y RTL-aware.
 *
 * @param title Título de la barra superior.
 * @param titleColor Color del título. null = hereda de [TopAppBar].
 * @param subtitle Texto secundario opcional debajo del título.
 * @param onBack Callback de navegación hacia atrás. Si es null, no se muestra el botón.
 * @param actions Slot de acciones en la TopAppBar (recibe [RowScope]).
 * @param containerColor Color de fondo del Scaffold.
 * @param snackbarHost Slot para SnackbarHost personalizado.
 * @param bottomBar Slot para la barra inferior.
 * @param topBarVisible Si es false, la TopAppBar no se renderiza (animación slide in/out).
 * @param scrollBehavior Comportamiento de scroll para la TopAppBar (pinned, enterAlways, etc.).
 * @param content Contenido principal de la pantalla.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScreenScaffold(
    modifier: Modifier = Modifier,
    title: String,
    onBack: (() -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {},
    containerColor: Color = MaterialTheme.colorScheme.surface,
    snackbarHost: @Composable () -> Unit = {},
    bottomBar: @Composable () -> Unit = {},
    topBarVisible: Boolean = true,
    scrollBehavior: TopAppBarScrollBehavior? = null,
    titleColor: Color? = null,
    subtitle: String? = null,
    content: @Composable (PaddingValues) -> Unit
) {
    Scaffold(
        modifier = modifier,
        containerColor = containerColor,
        snackbarHost = snackbarHost,
        bottomBar = bottomBar,
        topBar = {
            AnimatedVisibility(
                visible = topBarVisible,
                enter = slideInVertically(initialOffsetY = { -it }),
                exit = slideOutVertically(targetOffsetY = { -it })
            ) {
                AppTopAppBar(
                    title = title,
                    onBack = onBack,
                    actions = actions,
                    containerColor = containerColor,
                    scrollBehavior = scrollBehavior,
                    titleColor = titleColor,
                    subtitle = subtitle
                )
            }
        },
        content = content
    )
}
