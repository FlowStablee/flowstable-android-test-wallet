package com.antigravity.cryptowallet.ui.onboarding

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.TextSnippet
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.antigravity.cryptowallet.data.wallet.WalletRepository
import com.antigravity.cryptowallet.ui.components.BrutalistButton
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ImportWalletViewModel @Inject constructor(
    private val walletRepository: WalletRepository
) : ViewModel() {
    var isLoading by androidx.compose.runtime.mutableStateOf(false)
        private set

    fun importWallet(phrase: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            isLoading = true
            val success = walletRepository.importWallet(phrase)
            isLoading = false
            onResult(success)
        }
    }

    fun importPrivateKey(privateKey: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            isLoading = true
            val success = walletRepository.importPrivateKey(privateKey)
            isLoading = false
            onResult(success)
        }
    }
}

@Composable
fun ImportWalletScreen(
    onWalletImported: () -> Unit,
    viewModel: ImportWalletViewModel = hiltViewModel()
) {
    var importType by remember { mutableStateOf(0) } // 0: Phrase, 1: Private Key
    var input by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp)
    ) {
        Spacer(modifier = Modifier.height(32.dp))
        
        // Header
        Text(
            text = "Import Wallet",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Black,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = "Restore your existing wallet using a seed phrase or private key",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray,
            modifier = Modifier.padding(top = 8.dp)
        )
        
        Spacer(modifier = Modifier.height(32.dp))

        // Import Type Selector Cards
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            ImportTypeCard(
                label = "Seed Phrase",
                icon = Icons.Default.TextSnippet,
                isSelected = importType == 0,
                onClick = { importType = 0; input = ""; error = null },
                modifier = Modifier.weight(1f)
            )
            ImportTypeCard(
                label = "Private Key",
                icon = Icons.Default.Key,
                isSelected = importType == 1,
                onClick = { importType = 1; input = ""; error = null },
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Input Field
        OutlinedTextField(
            value = input,
            onValueChange = { input = it; error = null },
            label = { Text(if (importType == 0) "Enter Seed Phrase" else "Enter Private Key") },
            placeholder = { 
                Text(
                    if (importType == 0) "word1 word2 word3 ... (12 or 24 words)" 
                    else "0x... or raw 64-character hex"
                ) 
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp),
            singleLine = false,
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.3f),
                focusedTextColor = MaterialTheme.colorScheme.onBackground,
                unfocusedTextColor = MaterialTheme.colorScheme.onBackground,
                cursorColor = MaterialTheme.colorScheme.primary
            )
        )
        
        if (error != null) {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE)),
                modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = error!!,
                    color = Color(0xFFC62828),
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(16.dp),
                    textAlign = TextAlign.Center
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))
        
        // Security Note
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "🔒 Your seed phrase or private key never leaves your device and is stored securely using encrypted storage.",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray,
                modifier = Modifier.padding(16.dp),
                textAlign = TextAlign.Center
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))

        BrutalistButton(
            text = if (viewModel.isLoading) "Importing..." else "Import Wallet",
            enabled = !viewModel.isLoading,
            onClick = {
                if (!viewModel.isLoading) {
                    val trimmedInput = input.trim()
                    if (trimmedInput.isEmpty()) {
                        error = "Please enter your ${if (importType == 0) "seed phrase" else "private key"}"
                    } else {
                        if (importType == 0) {
                            viewModel.importWallet(trimmedInput) { success ->
                                if (success) {
                                    onWalletImported()
                                } else {
                                    error = "Invalid seed phrase. Please check your words and try again."
                                }
                            }
                        } else {
                            viewModel.importPrivateKey(trimmedInput) { success ->
                                if (success) {
                                    onWalletImported()
                                } else {
                                    error = "Invalid private key format. Please verify and try again."
                                }
                            }
                        }
                    }
                }
            },
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
fun ImportTypeCard(
    label: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor by animateColorAsState(
        targetValue = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
        label = "bg"
    )
    val contentColor by animateColorAsState(
        targetValue = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
        label = "content"
    )
    
    Card(
        modifier = modifier
            .height(100.dp)
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        shape = RoundedCornerShape(16.dp),
        border = if (!isSelected) androidx.compose.foundation.BorderStroke(1.dp, Color.LightGray) else null
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = label,
                fontWeight = FontWeight.Bold,
                color = contentColor,
                fontSize = 13.sp
            )
        }
    }
}
