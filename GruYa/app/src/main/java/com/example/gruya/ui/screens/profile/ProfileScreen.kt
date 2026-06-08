package com.example.gruya.ui.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.example.gruya.ui.theme.GruYaTheme

@Composable
fun ProfileScreen(
    onLogout: () -> Unit,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    
    ProfileContent(
        uiState = uiState,
        onLogout = {
            viewModel.logout()
            onLogout()
        },
        onUpdateProfile = { fName, lName, email, phone ->
            viewModel.updateProfile(fName, lName, email, phone)
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileContent(
    uiState: ProfileUiState,
    onLogout: () -> Unit,
    onUpdateProfile: (String, String, String, String) -> Unit
) {
    var isEditing by remember { mutableStateOf(false) }
    var editedFirstName by remember(uiState.user) { mutableStateOf(uiState.user?.firstName ?: "") }
    var editedLastName by remember(uiState.user) { mutableStateOf(uiState.user?.lastName ?: "") }
    var editedEmail by remember(uiState.email) { mutableStateOf(uiState.email) }
    var editedPhone by remember(uiState.user) { mutableStateOf(uiState.user?.phone ?: "") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (isEditing) "Editar Perfil" else "Mi Perfil",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                },
                actions = {
                    if (isEditing) {
                        IconButton(onClick = { isEditing = false }) {
                            Icon(Icons.Default.Close, contentDescription = "Cancelar", tint = MaterialTheme.colorScheme.error)
                        }
                        IconButton(onClick = {
                            onUpdateProfile(editedFirstName, editedLastName, editedEmail, editedPhone)
                            isEditing = false
                        }) {
                            Icon(Icons.Default.Check, contentDescription = "Guardar", tint = MaterialTheme.colorScheme.primary)
                        }
                    } else {
                        IconButton(onClick = { isEditing = true }) {
                            Icon(Icons.Default.Edit, contentDescription = "Editar", tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(20.dp))

            // Avatar Section
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .border(
                        3.dp,
                        MaterialTheme.colorScheme.primary,
                        CircleShape
                    )
                    .background(
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                    ),
                contentAlignment = Alignment.Center
            ){
                if (uiState.avatarUrl != null) {
                    AsyncImage(
                        model = uiState.avatarUrl,
                        contentDescription = "Avatar",
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        modifier = Modifier.size(70.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = uiState.name.ifEmpty { "Cargando..." },
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground
            )

            Text(
                text = uiState.email,
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Info Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "Información de la cuenta",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    ProfileInfoRow(
                        icon = Icons.Default.Badge,
                        label = "Nombre",
                        value = if (isEditing) editedFirstName else uiState.user?.firstName ?: "-",
                        isEditing = isEditing,
                        onValueChange = { editedFirstName = it }
                    )
                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                    )
                    ProfileInfoRow(
                        icon = Icons.Default.PersonOutline,
                        label = "Apellido",
                        value = if (isEditing) editedLastName else uiState.user?.lastName ?: "-",
                        isEditing = isEditing,
                        onValueChange = { editedLastName = it }
                    )
                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                    )
                    ProfileInfoRow(
                        icon = Icons.Default.Email,
                        label = "Email",
                        value = if (isEditing) editedEmail else uiState.email,
                        isEditing = isEditing,
                        onValueChange = { editedEmail = it }
                    )
                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                    )
                    ProfileInfoRow(
                        icon = Icons.Default.Phone,
                        label = "Teléfono",
                        value = if (isEditing) editedPhone else uiState.user?.phone ?: "No registrado",
                        isEditing = isEditing,
                        onValueChange = { editedPhone = it }
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            if (uiState.error != null) {
                Text(
                    text = uiState.error,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }

            // Logout Button
            Button(
                onClick = onLogout,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(55.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondary,
                    contentColor = MaterialTheme.colorScheme.onSecondary
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Default.Logout, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "Cerrar Sesión",
                    fontWeight = FontWeight.Bold
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
        }

        if (uiState.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize().background(
                    MaterialTheme.colorScheme.scrim.copy(alpha = 0.2f)
                ),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    color = MaterialTheme.colorScheme.secondary
                )
            }
        }
    }
}

@Composable
fun ProfileInfoRow(
    icon: ImageVector,
    label: String,
    value: String,
    isEditing: Boolean = false,
    onValueChange: (String) -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = if (isEditing) 4.dp else 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f)
            )
            if (isEditing) {
                TextField(
                    value = value,
                    onValueChange = onValueChange,
                    modifier = Modifier.fillMaxWidth(),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        disabledContainerColor = Color.Transparent,
                    ),
                    textStyle = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    ),
                    singleLine = true
                )
            } else {
                Text(
                    text = value,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@PreviewScreenSizes
@Preview(showBackground = true)
@Composable
fun ProfileScreenPreview() {
    GruYaTheme {
        ProfileContent(
            uiState = ProfileUiState(
                name = "Juan Pérez",
                email = "juan.perez@example.com",
                isLoading = false
            ),
            onLogout = {},
            onUpdateProfile = { _, _, _, _ -> }
        )
    }
}
