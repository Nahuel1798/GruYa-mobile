package com.example.gruya.ui.screens.payment

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.LocalAtm
import androidx.compose.material3.*
import com.example.gruya.ui.components.ScreenScaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.gruya.domain.model.PaymentMethod
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentScreen(
    assistanceId: Int,
    amount: Double,
    onPaymentSuccess: () -> Unit,
    onNavigateBack: () -> Unit,
    viewModel: PaymentViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(assistanceId, amount) {
        viewModel.initPayment(assistanceId, amount)
    }

    // Auto-navigate back after success for a smoother flow, 
    // but give enough time to see the success state
    LaunchedEffect(uiState.isSuccess, uiState.isFinished) {
        if (uiState.isSuccess && !uiState.isProvider) {
            kotlinx.coroutines.delay(2000)
            onPaymentSuccess()
        }
        if (uiState.isFinished) {
            kotlinx.coroutines.delay(2000)
            onPaymentSuccess()
        }
    }

    ScreenScaffold(
        title = "Realizar Pago",
        onBack = onNavigateBack,
        bottomBar = {
            if (!uiState.isSuccess && !uiState.isFailed) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    tonalElevation = 8.dp,
                    shadowElevation = 16.dp
                ) {
                    Button(
                        onClick = { viewModel.pay() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp)
                            .height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        enabled = uiState.selectedMethod != null && !uiState.isLoading
                    ) {
                        if (uiState.isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = MaterialTheme.colorScheme.onPrimary,
                                strokeWidth = 3.dp
                            )
                        } else {
                            Text("PAGAR $${String.format(Locale.getDefault(), "%.2f", uiState.amount)}", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        }
                    }
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            when {
                uiState.isSuccess -> {
                    PaymentSuccessView(
                        isProvider = uiState.isProvider,
                        isFinished = uiState.isFinished,
                        isLoading = uiState.isLoading,
                        onFinishService = { viewModel.completeService() },
                        onContinue = onPaymentSuccess
                    )
                }
                uiState.isFailed -> {
                    PaymentFailureView(
                        error = uiState.error ?: "Error desconocido",
                        onRetry = { viewModel.clearError() }
                    )
                }
                else -> {
                    PaymentContent(
                        amount = uiState.amount,
                        selectedMethod = uiState.selectedMethod,
                        onMethodSelected = { viewModel.selectMethod(it) },
                        error = uiState.error
                    )
                }
            }
        }
    }
}


@Composable
private fun PaymentContent(
    amount: Double,
    selectedMethod: PaymentMethod?,
    onMethodSelected: (PaymentMethod) -> Unit,
    error: String?
) {
    Text(
        text = "Resumen del Servicio",
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.fillMaxWidth()
    )
    
    Spacer(modifier = Modifier.height(24.dp))
    
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        tonalElevation = 2.dp,
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Total a pagar", style = MaterialTheme.typography.bodyLarge)
                Text(
                    text = "$${String.format(Locale.getDefault(), "%.2f", amount)}",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }

    Spacer(modifier = Modifier.height(32.dp))

    Text(
        text = "Selecciona un método de pago",
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier.fillMaxWidth()
    )

    Spacer(modifier = Modifier.height(16.dp))

    PaymentMethodItem(
        label = "Efectivo",
        icon = Icons.Default.LocalAtm,
        isSelected = selectedMethod == PaymentMethod.EFECTIVO,
        onClick = { onMethodSelected(PaymentMethod.EFECTIVO) }
    )
    Spacer(modifier = Modifier.height(12.dp))
    PaymentMethodItem(
        label = "Tarjeta de Crédito/Débito",
        icon = Icons.Default.CreditCard,
        isSelected = selectedMethod == PaymentMethod.TARJETA,
        onClick = { onMethodSelected(PaymentMethod.TARJETA) }
    )
    Spacer(modifier = Modifier.height(12.dp))
    PaymentMethodItem(
        label = "Mercado Pago",
        icon = Icons.Default.AccountBalanceWallet,
        isSelected = selectedMethod == PaymentMethod.MERCADOPAGO,
        onClick = { onMethodSelected(PaymentMethod.MERCADOPAGO) }
    )

    if (error != null) {
        Spacer(modifier = Modifier.height(24.dp))
        Surface(
            color = MaterialTheme.colorScheme.errorContainer,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = error,
                color = MaterialTheme.colorScheme.onErrorContainer,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(12.dp)
            )
        }
    }
}

@Composable
private fun PaymentMethodItem(
    label: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val borderColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
    val containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f) else Color.Transparent
    
    Surface(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(2.dp, borderColor),
        color = containerColor
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                icon, 
                contentDescription = null, 
                modifier = Modifier.size(24.dp),
                tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                label, 
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.weight(1f))
            RadioButton(
                selected = isSelected,
                onClick = null // Handled by Surface
            )
        }
    }
}

@Composable
private fun PaymentSuccessView(
    isProvider: Boolean,
    isFinished: Boolean,
    isLoading: Boolean,
    onFinishService: () -> Unit,
    onContinue: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = if (isFinished) Icons.Default.CheckCircle else Icons.Default.CheckCircle,
            contentDescription = null,
            modifier = Modifier.size(100.dp),
            tint = Color(0xFF4CAF50)
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = if (isFinished) "¡Servicio Finalizado!" else "¡Pago Exitoso!",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF2E7D32)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = if (isFinished) 
                "El servicio ha sido completado correctamente." 
            else 
                "El pago se ha procesado correctamente.",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(48.dp))
        
        if (isProvider && !isFinished) {
            Button(
                onClick = onFinishService,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(16.dp),
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
                } else {
                    Text("FINALIZAR VIAJE", fontWeight = FontWeight.Bold)
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
        }
        
        OutlinedButton(
            onClick = onContinue,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(16.dp),
            enabled = !isLoading
        ) {
            Text(if (isFinished) "VOLVER AL INICIO" else "VOLVER AL SEGUIMIENTO", fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun PaymentFailureView(
    error: String,
    onRetry: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Cancel,
            contentDescription = null,
            modifier = Modifier.size(100.dp),
            tint = MaterialTheme.colorScheme.error
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "¡Pago Fallido!",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.error
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = error,
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(48.dp))
        Button(
            onClick = onRetry,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.error
            )
        ) {
            Text("REINTENTAR", fontWeight = FontWeight.Bold)
        }
    }
}
