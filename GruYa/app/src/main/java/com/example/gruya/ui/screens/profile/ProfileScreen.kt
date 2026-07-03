package com.example.gruya.ui.screens.profile

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
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
        viewModel.loadAll()
    }

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri ->
            uri?.let { viewModel.updateAvatar(it) }
        }
    )
    
    ProfileContent(
        uiState = uiState,
        onLogout = {
            viewModel.logout()
            onLogout()
        },
        onUpdateProfile = { fName, lName, email, phone ->
            viewModel.updateProfile(fName, lName, email, phone)
        },
        onUpdateAvatar = {
            photoPickerLauncher.launch(
                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
            )
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
        },
        onOpenPasswordDialog = viewModel::onOpenPasswordDialog,
        onClosePasswordDialog = viewModel::onClosePasswordDialog,
        onOldPasswordChange = viewModel::onOldPasswordChange,
        onNewPasswordChange = viewModel::onNewPasswordChange,
        onConfirmPasswordChange = viewModel::onConfirmPasswordChange,
        onUpdatePassword = viewModel::updatePassword
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileContent(
    uiState: ProfileUiState,
    onLogout: () -> Unit,
    onUpdateProfile: (String, String, String, String) -> Unit,
    onUpdateAvatar: () -> Unit = {},
    onUpdateProviderProfile: (String, String, ServiceType, String) -> Unit = { _, _, _, _ -> },
    onToggleProviderEdit: () -> Unit = {},
    onCancelProviderEdit: () -> Unit = {},
    onOpenPasswordDialog: () -> Unit = {},
    onClosePasswordDialog: () -> Unit = {},
    onOldPasswordChange: (String) -> Unit = {},
    onNewPasswordChange: (String) -> Unit = {},
    onConfirmPasswordChange: (String) -> Unit = {},
    onUpdatePassword: () -> Unit = {}
) {
    var isEditing by remember { mutableStateOf(false) }
    var editedFirstName by remember(uiState.user) { mutableStateOf(uiState.user?.firstName ?: "") }
    var editedLastName by remember(uiState.user) { mutableStateOf(uiState.user?.lastName ?: "") }
    var editedEmail by remember(uiState.email) { mutableStateOf(uiState.email) }
    var editedPhone by remember(uiState.user) { mutableStateOf(uiState.user?.phone ?: "") }

    val userFieldsValid = editedFirstName.isNotBlank() && editedLastName.isNotBlank() && editedEmail.isNotBlank()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { 
                    Text(
                        "Mi Perfil",
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    ) 
                },
                actions = {
                    IconButton(onClick = onLogout) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Logout,
                            contentDescription = "Cerrar Sesión",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header Section
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                Color.Transparent
                            )
                        )
                    )
                    .padding(vertical = 32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    // Avatar with Edit Badge
                    Box(
                        contentAlignment = Alignment.BottomEnd,
                        modifier = Modifier.clickable { onUpdateAvatar() }
                    ) {
                        Surface(
                            modifier = Modifier
                                .size(120.dp)
                                .clip(CircleShape)
                                .border(4.dp, MaterialTheme.colorScheme.surface, CircleShape),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            tonalElevation = 2.dp
                        ) {
                            if (uiState.isUpdatingAvatar) {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(32.dp),
                                        strokeWidth = 3.dp
                                    )
                                }
                            } else if (uiState.avatarUrl != null) {
                                AsyncImage(
                                    model = uiState.avatarUrl,
                                    contentDescription = "Avatar",
                                    modifier = Modifier.fillMaxSize()
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = null,
                                    modifier = Modifier
                                        .size(80.dp)
                                        .padding(20.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                        
                        // Small Edit icon overlay
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(32.dp),
                            shadowElevation = 4.dp
                        ) {
                            Icon(
                                Icons.Default.CameraAlt,
                                contentDescription = null,
                                modifier = Modifier.padding(6.dp),
                                tint = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = uiState.name.ifEmpty { "Usuario" },
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onBackground
                    )

                    Text(
                        text = uiState.email,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Information Sections
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
            ) {
                SectionHeader(
                    title = "Información Personal",
                    isEditing = isEditing,
                    onEditClick = { isEditing = true },
                    onCancelClick = { isEditing = false },
                    onSaveClick = {
                        onUpdateProfile(editedFirstName, editedLastName, editedEmail, editedPhone)
                        isEditing = false
                    },
                    saveEnabled = userFieldsValid
                )

                ElevatedCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.elevatedCardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .padding(20.dp)
                            .animateContentSize()
                    ) {
                        ProfileInfoRow(
                            icon = Icons.Outlined.Person,
                            label = "Nombre",
                            value = if (isEditing) editedFirstName else uiState.user?.firstName ?: "-",
                            isEditing = isEditing,
                            onValueChange = { editedFirstName = it }
                        )
                        
                        ProfileInfoRow(
                            icon = Icons.Outlined.Badge,
                            label = "Apellido",
                            value = if (isEditing) editedLastName else uiState.user?.lastName ?: "-",
                            isEditing = isEditing,
                            onValueChange = { editedLastName = it }
                        )
                        
                        ProfileInfoRow(
                            icon = Icons.Outlined.Email,
                            label = "Email",
                            value = if (isEditing) editedEmail else uiState.email,
                            isEditing = isEditing,
                            onValueChange = { editedEmail = it }
                        )
                        
                        ProfileInfoRow(
                            icon = Icons.Outlined.Phone,
                            label = "Teléfono",
                            value = if (isEditing) editedPhone else uiState.user?.phone ?: "No registrado",
                            isEditing = isEditing,
                            onValueChange = { editedPhone = it }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Provider Profile Section
                if (uiState.isProvider) {
                    SectionHeader(
                        title = "Perfil de Proveedor",
                        isEditing = uiState.isEditingProvider,
                        onEditClick = onToggleProviderEdit,
                        onCancelClick = onCancelProviderEdit,
                        onSaveClick = { /* Handled inside ProviderProfileSection component */ },
                        showActions = uiState.providerProfile != null && !uiState.isEditingProvider 
                    )

                    if (uiState.isLoadingProvider && uiState.providerProfile == null) {
                        LinearProgressIndicator(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 16.dp),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    
                    ProviderProfileSection(
                        providerProfile = uiState.providerProfile,
                        isEditingProvider = uiState.isEditingProvider,
                        isSavingProvider = uiState.isSavingProvider,
                        providerError = uiState.providerError,
                        providerCompanyName = uiState.providerCompanyName,
                        providerAddress = uiState.providerAddress,
                        providerServiceType = uiState.providerServiceType,
                        onCancelEdit = onCancelProviderEdit,
                        onSave = onUpdateProviderProfile,
                        isLoading = uiState.isLoadingProvider && uiState.providerProfile == null
                    )
                }

                if (uiState.error != null) {
                    ErrorCard(uiState.error)
                }

                Spacer(modifier = Modifier.height(40.dp))
                
                // Secondary Actions / Settings
                Text(
                    text = "Ajustes de cuenta",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(start = 4.dp, bottom = 12.dp)
                )
                
                AccountActionItem(
                    icon = Icons.Outlined.Lock,
                    label = "Cambiar contraseña",
                    onClick = onOpenPasswordDialog
                )

                Spacer(modifier = Modifier.height(40.dp))
            }
        }

        if (uiState.isPasswordDialogOpen) {
            PasswordChangeDialog(
                uiState = uiState,
                onClose = onClosePasswordDialog,
                onOldPasswordChange = onOldPasswordChange,
                onNewPasswordChange = onNewPasswordChange,
                onConfirmPasswordChange = onConfirmPasswordChange,
                onUpdate = onUpdatePassword
            )
        }

        // Loading Overlay
        AnimatedVisibility(
            visible = uiState.isLoading,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.3f)),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        }
    }
}

@Composable
fun SectionHeader(
    title: String,
    isEditing: Boolean = false,
    onEditClick: () -> Unit = {},
    onCancelClick: () -> Unit = {},
    onSaveClick: () -> Unit = {},
    saveEnabled: Boolean = true,
    showActions: Boolean = true
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        if (showActions) {
            if (isEditing) {
                Row {
                    TextButton(onClick = onCancelClick) {
                        Text("Cancelar", color = MaterialTheme.colorScheme.error)
                    }
                    Button(
                        onClick = onSaveClick,
                        enabled = saveEnabled,
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Guardar")
                    }
                }
            } else {
                TextButton(
                    onClick = onEditClick,
                    contentPadding = PaddingValues(horizontal = 12.dp)
                ) {
                    Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Editar")
                }
            }
        }
    }
}

@Composable
fun AccountActionItem(
    icon: ImageVector,
    label: String,
    contentColor: Color = MaterialTheme.colorScheme.onSurface,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        color = Color.Transparent
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp, horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = contentColor.copy(alpha = 0.7f),
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.bodyLarge,
                color = contentColor
            )
            Spacer(modifier = Modifier.weight(1f))
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.outlineVariant,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
fun ErrorCard(message: String) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.ErrorOutline, contentDescription = null, tint = MaterialTheme.colorScheme.error)
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = message,
                color = MaterialTheme.colorScheme.onErrorContainer,
                style = MaterialTheme.typography.bodyMedium
            )
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
                .padding(vertical = 8.dp)
        )
    } else {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    letterSpacing = 0.5.sp
                )
                Text(
                    text = value,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
        HorizontalDivider(
            modifier = Modifier.padding(start = 56.dp),
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
        )
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
    onCancelEdit: () -> Unit,
    onSave: (String, String, ServiceType, String) -> Unit,
    isLoading: Boolean = false
) {
    var editedCompanyName by remember(isEditingProvider, providerCompanyName) { mutableStateOf(providerCompanyName) }
    var editedAddress by remember(isEditingProvider, providerAddress) { mutableStateOf(providerAddress) }
    var editedServiceType by remember(isEditingProvider, providerServiceType) { mutableStateOf(providerServiceType) }
    var editedDescription by remember(isEditingProvider, providerProfile?.description) { mutableStateOf(providerProfile?.description ?: "") }

    val providerFieldsValid = editedCompanyName.isNotBlank() && editedAddress.isNotBlank() && editedDescription.isNotBlank()

    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .padding(20.dp)
                .animateContentSize()
        ) {
            if (isEditingProvider) {
                AppTextField(
                    value = editedCompanyName,
                    onValueChange = { editedCompanyName = it },
                    placeholder = "Nombre de empresa",
                    leadingIcon = Icons.Default.Business,
                    isError = editedCompanyName.isBlank()
                )
                Spacer(modifier = Modifier.height(12.dp))

                AppTextField(
                    value = editedDescription,
                    onValueChange = { editedDescription = it },
                    placeholder = "Descripción",
                    leadingIcon = Icons.Default.Description,
                    isError = editedDescription.isBlank()
                )
                Spacer(modifier = Modifier.height(12.dp))

                AppTextField(
                    value = editedAddress,
                    onValueChange = { editedAddress = it },
                    placeholder = "Dirección",
                    leadingIcon = Icons.Default.LocationOn,
                    isError = editedAddress.isBlank()
                )
                Spacer(modifier = Modifier.height(12.dp))

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
                            .menuAnchor(type = ExposedDropdownMenuAnchorType.PrimaryNotEditable),
                        shape = RoundedCornerShape(12.dp)
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
                
                Spacer(modifier = Modifier.height(20.dp))
                
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onCancelEdit) {
                        Text("Cancelar", color = MaterialTheme.colorScheme.error)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = { onSave(editedCompanyName, editedAddress, editedServiceType, editedDescription) },
                        enabled = providerFieldsValid && !isSavingProvider,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        if (isSavingProvider) {
                            CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp, color = MaterialTheme.colorScheme.onPrimary)
                        } else {
                            Text("Guardar Cambios")
                        }
                    }
                }

            } else {
                if (providerProfile != null) {
                    // Availability Badge
                    Surface(
                        color = if (providerProfile.isAvailable) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f) 
                                else MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.padding(bottom = 20.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(if (providerProfile.isAvailable) Color(0xFF4CAF50) else Color(0xFF9E9E9E))
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (providerProfile.isAvailable) "Disponible para servicios" else "Fuera de servicio",
                                style = MaterialTheme.typography.labelLarge,
                                color = if (providerProfile.isAvailable) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    ProviderDetailRow(icon = Icons.Outlined.Business, label = "Empresa", value = providerProfile.companyName)
                    ProviderDetailRow(icon = Icons.Outlined.Category, label = "Tipo de servicio", value = providerProfile.serviceType.displayName)
                    ProviderDetailRow(icon = Icons.Outlined.LocationOn, label = "Dirección", value = providerProfile.address)
                    ProviderDetailRow(icon = Icons.Outlined.Description, label = "Descripción", value = providerProfile.description)

                } else if (!isLoading) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 20.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = providerError ?: "Sin perfil de proveedor configurado",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ProviderDetailRow(icon: ImageVector, label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                letterSpacing = 0.5.sp
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Medium
            )
        }
    }
    HorizontalDivider(
        modifier = Modifier.padding(start = 36.dp),
        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f)
    )
}

@Composable
fun PasswordChangeDialog(
    uiState: ProfileUiState,
    onClose: () -> Unit,
    onOldPasswordChange: (String) -> Unit,
    onNewPasswordChange: (String) -> Unit,
    onConfirmPasswordChange: (String) -> Unit,
    onUpdate: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onClose,
        title = { Text("Cambiar Contraseña") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (uiState.passwordSuccess) {
                    Surface(
                        color = Color(0xFFE8F5E9),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            "Contraseña actualizada con éxito",
                            color = Color(0xFF2E7D32),
                            modifier = Modifier.padding(12.dp),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }

                if (uiState.passwordError != null) {
                    Surface(
                        color = MaterialTheme.colorScheme.errorContainer,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            uiState.passwordError,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.padding(12.dp),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }

                AppTextField(
                    value = uiState.oldPassword,
                    onValueChange = onOldPasswordChange,
                    placeholder = "Contraseña actual",
                    leadingIcon = Icons.Default.Lock,
                    keyboardType = KeyboardType.Password,
                    visualTransformation = PasswordVisualTransformation()
                )

                AppTextField(
                    value = uiState.newPassword,
                    onValueChange = onNewPasswordChange,
                    placeholder = "Nueva contraseña",
                    leadingIcon = Icons.Default.LockOpen,
                    keyboardType = KeyboardType.Password,
                    visualTransformation = PasswordVisualTransformation()
                )

                AppTextField(
                    value = uiState.confirmPassword,
                    onValueChange = onConfirmPasswordChange,
                    placeholder = "Confirmar nueva contraseña",
                    leadingIcon = Icons.Default.CheckCircle,
                    keyboardType = KeyboardType.Password,
                    visualTransformation = PasswordVisualTransformation()
                )
            }
        },
        confirmButton = {
            if (!uiState.passwordSuccess) {
                Button(
                    onClick = onUpdate,
                    enabled = !uiState.isUpdatingPassword && 
                             uiState.oldPassword.isNotBlank() && 
                             uiState.newPassword.isNotBlank() && 
                             uiState.confirmPassword.isNotBlank(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    if (uiState.isUpdatingPassword) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        Text("Actualizar")
                    }
                }
            } else {
                Button(onClick = onClose, shape = RoundedCornerShape(12.dp)) {
                    Text("Cerrar")
                }
            }
        },
        dismissButton = {
            if (!uiState.passwordSuccess) {
                TextButton(onClick = onClose) {
                    Text("Cancelar")
                }
            }
        }
    )
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
