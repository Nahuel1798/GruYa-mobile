package com.example.gruya.ui.components

import androidx.compose.foundation.layout.RowScope
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight

/**
 * TopAppBar estandarizado extraído de [ScreenScaffold].
 *
 * Útil cuando la pantalla usa [androidx.compose.material3.BottomSheetScaffold]
 * u otro layout que no puede usar [ScreenScaffold] directamente, pero necesita
 * la misma TopAppBar consistente con ícono de retroceso RTL-aware y estilo
 * unificado.
 *
 * @param title Título por defecto (usado cuando [titleContent] es null).
 * @param modifier Modificador para la TopAppBar.
 * @param onBack Callback de navegación hacia atrás. Si es null, no se muestra el botón.
 * @param actions Slot de acciones (recibe [RowScope]).
 * @param containerColor Color de fondo de la TopAppBar.
 * @param scrollBehavior Comportamiento de scroll (pinned, enterAlways, etc.).
 * @param titleContent Slot personalizado para el título. Cuando se provee, reemplaza
 *   el [Text] por defecto renderizado a partir de [title]. Útil para títulos
 *   con subtítulo o estilos personalizados.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppTopAppBar(
    title: String,
    modifier: Modifier = Modifier,
    onBack: (() -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {},
    containerColor: Color = MaterialTheme.colorScheme.surface,
    scrollBehavior: TopAppBarScrollBehavior? = null,
    titleContent: @Composable (() -> Unit)? = null
) {
    TopAppBar(
        modifier = modifier,
        title = {
            if (titleContent != null) {
                titleContent()
            } else {
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        navigationIcon = {
            if (onBack != null) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Volver"
                    )
                }
            }
        },
        actions = actions,
        scrollBehavior = scrollBehavior,
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = containerColor
        )
    )
}
