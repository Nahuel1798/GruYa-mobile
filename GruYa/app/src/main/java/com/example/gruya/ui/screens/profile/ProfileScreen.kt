package com.example.gruya.ui.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
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
import com.example.gruya.domain.model.ProviderProfile
import com.example.gruya.domain.model.ServiceType
import com.example.gruya.ui.components.AppTextField
import com.example.gruya.ui.theme.GruYaTheme

@Composable
fun ProfileScreen(
    onLogout: () -> Unit,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    LaunchedEffect(Unit) {
        viewModel.loadUser()
    }

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    
    ProfileContent(
        uiState = uiState,
        onLogout = {
            viewModel.logout()
            onLogout()
        },
        onUpdateProfile = { fName, lName, email, phone ->
            viewModel.updateProfile(fName, lName, email, phone)
        },
        onUpdateProviderProfile = { companyName, address, serviceType, description ->
            viewModel.updateProviderProfile(companyName, address, serviceType, description)
        },
        onToggleProviderEdit = {
            if (uiState.isEditingProvider) {
                viewModel.cancelProviderEdit()
            } else {
                viewModel.startEditProvider()
            }
        },
        onCancelProviderEdit = {
            viewModel.cancelProviderEdit()
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileContent(
    uiState: ProfileUiState,
    onLogout: () -> Unit,
    onUpdateProfile: (String, String, String, String) -> Unit,
        onUpdateProviderProfile: (String, String, ServiceType, String) -> Unit = { _, _, _, _ -> },
    onToggleProviderEdit: () -> Unit = {},
    onCancelProviderEdit: () -> Unit = {}
) {
    var isEditing by remember { mutableStateOf(false) }
    var editedFirstName by remember(uiState.user) { mutableStateOf(uiState.user?.firstName ?: "") }
    var editedLastName by remember(uiState.user) { mutableStateOf(uiState.user?.lastName ?: "") }
    var editedEmail by remember(uiState.email) { mutableStateOf(uiState.email) }
    var editedPhone by remember(uiState.user) { mutableStateOf(uiState.user?.phone ?: "") }

    val userFieldsValid = editedFirstName.isNotBlank() && editedLastName.isNotBlank() && editedEmail.isNotBlank()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Mi Perfil",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
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
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState()),
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
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Información de la cuenta",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary
                        )

                        if (isEditing) {
                            Row {
                                IconButton(onClick = { isEditing = false }) {
                                    Icon(Icons.Default.Close, contentDescription = "Cancelar", tint = MaterialTheme.colorScheme.error)
                                }
                                IconButton(
                                    onClick = {
                                        onUpdateProfile(editedFirstName, editedLastName, editedEmail, editedPhone)
                                        isEditing = false
                                    },
                                    enabled = userFieldsValid
                                ) {
                                    Icon(
                                        Icons.Default.Check,
                                        contentDescription = "Guardar",
                                        tint = if (userFieldsValid) MaterialTheme.colorScheme.primary
                                               else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                                    )
                                }
                            }
                        } else {
                            IconButton(onClick = { isEditing = true }) {
                                Icon(Icons.Default.Edit, contentDescription = "Editar", tint = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }
                    
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
            Spacer(modifier = Modifier.height(10.dp))

            // Provider Profile Section
            if (uiState.isLoadingProvider) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            if (uiState.providerProfile != null || uiState.providerError != null) {
                Spacer(modifier = Modifier.height(12.dp))
                ProviderProfileSection(
                    providerProfile = uiState.providerProfile,
                    isEditingProvider = uiState.isEditingProvider,
                    isSavingProvider = uiState.isSavingProvider,
                    providerError = uiState.providerError,
                    providerCompanyName = uiState.providerCompanyName,
                    providerAddress = uiState.providerAddress,
                    providerServiceType = uiState.providerServiceType,
                    onToggleEdit = onToggleProviderEdit,
                    onCancelEdit = onCancelProviderEdit,
                    onSave = onUpdateProviderProfile
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            if (uiState.error != null) {
                Text(
                    text = uiState.error,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
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
                Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null)
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
    if (isEditing) {
        AppTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = label,
            leadingIcon = icon,
            isError = value.isBlank(),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
        )
    } else {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProviderProfileSection(
    providerProfile: ProviderProfile?,
    isEditingProvider: Boolean,
    isSavingProvider: Boolean,
    providerError: String?,
    providerCompanyName: String,
    providerAddress: String,
    providerServiceType: ServiceType,
    onToggleEdit: () -> Unit,
    onCancelEdit: () -> Unit,
    onSave: (String, String, ServiceType, String) -> Unit
) {
    var editedCompanyName by remember(isEditingProvider, providerCompanyName) { mutableStateOf(providerCompanyName) }
    var editedAddress by remember(isEditingProvider, providerAddress) { mutableStateOf(providerAddress) }
    var editedServiceType by remember(isEditingProvider, providerServiceType) { mutableStateOf(providerServiceType) }
    var editedDescription by remember(isEditingProvider, providerProfile?.description) { mutableStateOf(providerProfile?.description ?: "") }

    val providerFieldsValid = editedCompanyName.isNotBlank() && editedAddress.isNotBlank() && editedDescription.isNotBlank()

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            // Header with edit button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Perfil de Proveedor",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )

                if (providerProfile != null) {
                    if (isEditingProvider) {
                        Row {
                            IconButton(onClick = onCancelEdit) {
                                Icon(
                                    Icons.Default.Close,
                                    contentDescription = "Cancelar",
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
                            if (!isSavingProvider) {
                                IconButton(
                                    onClick = {
                                        onSave(editedCompanyName, editedAddress, editedServiceType, editedDescription)
                                    },
                                    enabled = providerFieldsValid
                                ) {
                                    Icon(
                                        Icons.Default.Check,
                                        contentDescription = "Guardar",
                                        tint = if (providerFieldsValid) MaterialTheme.colorScheme.primary
                                               else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                                    )
                                }
                            }
                        }
                    } else {
                        IconButton(onClick = onToggleEdit) {
                            Icon(
                                Icons.Default.Edit,
                                contentDescription = "Editar",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (isEditingProvider) {
                // Edit mode
                AppTextField(
                    value = editedCompanyName,
                    onValueChange = { editedCompanyName = it },
                    placeholder = "Nombre de empresa",
                    leadingIcon = Icons.Default.Business,
                    isError = editedCompanyName.isBlank()
                )
                Spacer(modifier = Modifier.height(8.dp))

                AppTextField(
                    value = editedDescription,
                    onValueChange = { editedDescription = it },
                    placeholder = "Descripción",
                    leadingIcon = Icons.Default.Description,
                    isError = editedDescription.isBlank()
                )
                Spacer(modifier = Modifier.height(8.dp))

                AppTextField(
                    value = editedAddress,
                    onValueChange = { editedAddress = it },
                    placeholder = "Dirección",
                    leadingIcon = Icons.Default.LocationOn,
                    isError = editedAddress.isBlank()
                )
                Spacer(modifier = Modifier.height(8.dp))

                // ServiceType selector
                var expanded by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {
                    OutlinedTextField(
                        value = editedServiceType.displayName,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Tipo de servicio") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(type = ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        ServiceType.entries.forEach { type ->
                            DropdownMenuItem(
                                text = { Text(type.displayName) },
                                onClick = {
                                    editedServiceType = type
                                    expanded = false
                                }
                            )
                        }
                    }
                }

                if (isSavingProvider) {
                    Spacer(modifier = Modifier.height(8.dp))
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                }
            } else {
                // Read-only mode
                if (providerProfile != null) {
                    // Availability badge
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(
                                    if (providerProfile.isAvailable)
                                        Color(0xFF4CAF50)
                                    else
                                        Color(0xFF9E9E9E)
                                )
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (providerProfile.isAvailable) "Disponible" else "No disponible",
                            fontSize = 13.sp,
                            color = if (providerProfile.isAvailable) Color(0xFF4CAF50) else Color(0xFF9E9E9E)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    ProviderDetailRow(icon = Icons.Default.Business, label = "Empresa", value = providerProfile.companyName)
                    HorizontalDivider(color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                    ProviderDetailRow(icon = Icons.Default.Category, label = "Tipo de servicio", value = providerProfile.serviceType.displayName)
                    HorizontalDivider(color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                    ProviderDetailRow(icon = Icons.Default.LocationOn, label = "Dirección", value = providerProfile.address)
                    HorizontalDivider(color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                    ProviderDetailRow(icon = Icons.Default.Description, label = "Descripción", value = providerProfile.description)

                } else {
                    // Error state or empty
                    Text(
                        text = providerError ?: "Sin perfil de proveedor",
                        color = if (providerError != null) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
            }

            // Provider error message
            if (providerError != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = providerError,
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 14.sp
                )
            }
        }
    }
}

@Composable
fun ProviderDetailRow(icon: ImageVector, label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
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
            Text(
                text = value,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
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
