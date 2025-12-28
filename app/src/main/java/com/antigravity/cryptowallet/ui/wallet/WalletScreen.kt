package com.antigravity.cryptowallet.ui.wallet

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.antigravity.cryptowallet.data.blockchain.BlockchainService
import com.antigravity.cryptowallet.data.blockchain.NetworkRepository
import com.antigravity.cryptowallet.data.wallet.WalletRepository
import com.antigravity.cryptowallet.ui.components.BrutalistButton
import com.antigravity.cryptowallet.ui.components.BrutalistHeader
import com.antigravity.cryptowallet.ui.components.BrutalistInfoRow
import com.antigravity.cryptowallet.ui.theme.BrutalBlack
import com.antigravity.cryptowallet.ui.theme.BrutalWhite
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.math.BigDecimal
import javax.inject.Inject
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.window.Dialog
import com.antigravity.cryptowallet.utils.QrCodeGenerator

@HiltViewModel
class WalletViewModel @Inject constructor(
    private val walletRepository: WalletRepository,
    private val networkRepository: NetworkRepository,
    private val blockchainService: BlockchainService
) : ViewModel() {
    var address by mutableStateOf("Loading...")
    var balance by mutableStateOf("0.00 ETH")

    init {
        loadWallet()
    }

    private fun loadWallet() {
        if (!walletRepository.isWalletCreated()) return
        
        // Ensure wallet is loaded
        if (walletRepository.activeCredentials == null) {
            walletRepository.loadWallet()
        }
        
        address = walletRepository.getAddress()
        fetchBalance()
    }

    private fun fetchBalance() {
        viewModelScope.launch {
            val network = networkRepository.getNetwork("eth") // Default to ETH for now
            val rawBalance = blockchainService.getBalance(network.rpcUrl, address)
            
            // Convert Wei to Eth (Simple division for display)
            val ethBalance = BigDecimal(rawBalance).divide(BigDecimal.TEN.pow(18))
            balance = String.format("%.4f %s", ethBalance, network.symbol)
        }
    }
}

@Composable
fun WalletScreen(
    viewModel: WalletViewModel = hiltViewModel()
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BrutalWhite)
            .padding(16.dp)
    ) {
        val clipboardManager = LocalClipboardManager.current
        var showReceiveDialog by remember { mutableStateOf(false) }

        if (showReceiveDialog) {
            Dialog(onDismissRequest = { showReceiveDialog = false }) {
                Column(
                    modifier = Modifier
                        .background(BrutalWhite)
                        .border(2.dp, BrutalBlack)
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    BrutalistHeader("Receive Assets")
                    Spacer(modifier = Modifier.height(16.dp))

                    if (viewModel.address.length > 10) { // Basic check to ensure address is valid
                        val qrBitmap = remember(viewModel.address) {
                            QrCodeGenerator.generateQrCode(viewModel.address)
                        }
                        Image(
                            bitmap = qrBitmap.asImageBitmap(),
                            contentDescription = "Wallet Address QR Code",
                            modifier = Modifier.size(200.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = viewModel.address,
                        fontWeight = FontWeight.Bold,
                        color = BrutalBlack,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.clickable {
                            clipboardManager.setText(AnnotatedString(viewModel.address))
                        }
                    )
                    Text(
                        text = "(Tap address to copy)",
                        fontSize = 12.sp,
                        color = BrutalBlack
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    BrutalistButton(text = "Close", onClick = { showReceiveDialog = false })
                }
            }
        }

        BrutalistHeader("Dashboard")
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            text = "TOTAL BALANCE",
            color = BrutalBlack,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = viewModel.balance,
            color = BrutalBlack,
            fontSize = 42.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = (-1).sp
        )

        Spacer(modifier = Modifier.height(32.dp))
        
        BrutalistInfoRow("Address", viewModel.address.take(6) + "..." + viewModel.address.takeLast(4))
        BrutalistInfoRow("Network", "Ethereum Mainnet")
        
        Spacer(modifier = Modifier.weight(1f))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(modifier = Modifier.weight(1f)) {
                BrutalistButton(text = "Receive", onClick = { showReceiveDialog = true })
            }
            Box(modifier = Modifier.weight(1f)) {
                BrutalistButton(text = "Send", onClick = { }, inverted = true)
            }
        }
    }
}
