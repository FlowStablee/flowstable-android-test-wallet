package com.antigravity.cryptowallet.ui.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import com.antigravity.cryptowallet.data.wallet.WalletRepository
import com.antigravity.cryptowallet.ui.components.BrutalistButton
import com.antigravity.cryptowallet.ui.components.BrutalistHeader
import com.antigravity.cryptowallet.ui.components.BrutalistTextField
import com.antigravity.cryptowallet.ui.theme.BrutalWhite
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ImportWalletViewModel @Inject constructor(
    private val walletRepository: WalletRepository
) : ViewModel() {
    fun importWallet(phrase: String): Boolean {
        return walletRepository.importWallet(phrase)
    }

    fun importPrivateKey(privateKey: String): Boolean {
        return walletRepository.importPrivateKey(privateKey)
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
            .background(BrutalWhite)
            .padding(16.dp)
    ) {
        BrutalistHeader("Import Wallet")
        
        Spacer(modifier = Modifier.height(24.dp))

        // Brutalist Tab Selector
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .border(2.dp, com.antigravity.cryptowallet.ui.theme.BrutalBlack, androidx.compose.foundation.shape.RoundedCornerShape(12.dp))
                .clip(androidx.compose.foundation.shape.RoundedCornerShape(12.dp))
        ) {
            listOf("Seed Phrase", "Private Key").forEachIndexed { index, label ->
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .background(if (importType == index) com.antigravity.cryptowallet.ui.theme.BrutalBlack else BrutalWhite)
                        .clickable { 
                            importType = index
                            input = ""
                            error = null
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = label,
                        color = if (importType == index) BrutalWhite else com.antigravity.cryptowallet.ui.theme.BrutalBlack,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        BrutalistTextField(
            value = input,
            onValueChange = { input = it },
            placeholder = if (importType == 0) "Enter 12/24 word seed phrase..." else "Enter your private key (64 chars)...",
            singleLine = false,
            modifier = Modifier.height(150.dp)
        )
        
        if (error != null) {
            Text(
                text = error!!,
                color = Color.Red,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        BrutalistButton(
            text = "Import",
            onClick = {
                val success = if (importType == 0) {
                    viewModel.importWallet(input.trim())
                } else {
                    viewModel.importPrivateKey(input.trim())
                }

                if (success) {
                    onWalletImported()
                } else {
                    error = if (importType == 0) "INVALID SEED PHRASE" else "INVALID PRIVATE KEY"
                }
            }
        )
    }
}
