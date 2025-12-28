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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.antigravity.cryptowallet.data.blockchain.BlockchainService
import com.antigravity.cryptowallet.data.blockchain.NetworkRepository
import com.antigravity.cryptowallet.data.wallet.AssetRepository
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

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Settings

@HiltViewModel
class WalletViewModel @Inject constructor(
    private val walletRepository: WalletRepository,
    private val assetRepository: AssetRepository
) : ViewModel() {
    val address: String
        get() = walletRepository.getAddress()
    
    // UI State
    var totalBalanceUsd by mutableStateOf("$0.00")
    var assets by mutableStateOf<List<com.antigravity.cryptowallet.data.models.AssetUiModel>>(emptyList())
    var isRefreshing by mutableStateOf(false)
    
    // Tab State
    var selectedTab by mutableStateOf(0) // 0 = Assets, 1 = NFTs

    init {
        loadData()
    }
    
    fun addToken(address: String, symbol: String, decimals: Int) {
        viewModelScope.launch {
            // Default to ETH chain for now for custom tokens
            assetRepository.addToken(address, symbol, decimals, "eth", symbol)
        }
    }

    fun sendAsset(asset: com.antigravity.cryptowallet.data.models.AssetUiModel, toAddress: String, amount: String) {
        viewModelScope.launch {
            try {
                assetRepository.sendAsset(asset, toAddress, amount)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun refresh() {
        viewModelScope.launch {
            isRefreshing = true
            assetRepository.refreshAssets()
            isRefreshing = false
        }
    }

    private fun loadData() {
        viewModelScope.launch {
            if (!walletRepository.isWalletCreated()) return@launch
            
            // Collect assets
            launch {
                assetRepository.assets.collect { assetList ->
                    assets = assetList
                    val total = assetList.sumByDouble { it.rawBalance * it.price }
                    totalBalanceUsd = String.format("$%.2f", total)
                }
            }
            
            // Trigger initial refresh
            refresh()
        }
    }
}

@Composable
fun WalletScreen(
    viewModel: WalletViewModel = hiltViewModel(),
    onSetupSecurity: () -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BrutalWhite)
            .padding(16.dp)
    ) {
        val clipboardManager = LocalClipboardManager.current
        var showReceiveDialog by remember { mutableStateOf(false) }
        var showAddTokenDialog by remember { mutableStateOf(false) }
        var showSendDialog by remember { mutableStateOf(false) }

        if (showSendDialog) {
            var inputAddress by remember { mutableStateOf("") }
            var inputAmount by remember { mutableStateOf("") }
            var selectedAsset by remember { mutableStateOf(viewModel.assets.firstOrNull()) }
            var isSending by remember { mutableStateOf(false) }

            Dialog(onDismissRequest = { if(!isSending) showSendDialog = false }) {
                Column(
                    modifier = Modifier
                        .background(BrutalWhite)
                        .border(2.dp, BrutalBlack)
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    BrutalistHeader("Send Assets")
                    Spacer(modifier = Modifier.height(16.dp))

                    if (viewModel.assets.isEmpty()) {
                        Text("No assets to send", color = BrutalBlack)
                    } else {
                        // Simple dropdown placeholder or button to loop
                        Column(modifier = Modifier.fillMaxWidth().heightIn(max = 120.dp).verticalScroll(rememberScrollState())) {
                            viewModel.assets.forEach { asset ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { selectedAsset = asset }
                                        .background(if (selectedAsset?.id == asset.id) BrutalBlack else BrutalWhite)
                                        .padding(8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(asset.symbol, color = if (selectedAsset?.id == asset.id) BrutalWhite else BrutalBlack)
                                    Text(asset.balance, color = if (selectedAsset?.id == asset.id) BrutalWhite else BrutalBlack)
                                }
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        androidx.compose.material3.OutlinedTextField(
                            value = inputAddress,
                            onValueChange = { inputAddress = it },
                            label = { Text("To Address") },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !isSending
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        androidx.compose.material3.OutlinedTextField(
                            value = inputAmount,
                            onValueChange = { inputAmount = it },
                            label = { Text("Amount") },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !isSending,
                            keyboardOptions = KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Decimal)
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        BrutalistButton(
                            text = if(isSending) "Sending..." else "Send", 
                            onClick = { 
                                if (inputAddress.isNotEmpty() && inputAmount.isNotEmpty() && selectedAsset != null) {
                                    isSending = true
                                    viewModel.sendAsset(selectedAsset!!, inputAddress, inputAmount)
                                    // Close after a bit or on success. For now simple:
                                    showSendDialog = false
                                }
                            },
                            enabled = !isSending
                        )
                    }
                }
            }
        }

        if (showAddTokenDialog) {
            var inputAddress by remember { mutableStateOf("") }
            var inputSymbol by remember { mutableStateOf("") }
            var inputDecimals by remember { mutableStateOf("18") }

            Dialog(onDismissRequest = { showAddTokenDialog = false }) {
                Column(
                    modifier = Modifier
                        .background(BrutalWhite)
                        .border(2.dp, BrutalBlack)
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    BrutalistHeader("Add Token")
                    Spacer(modifier = Modifier.height(16.dp))

                    androidx.compose.material3.OutlinedTextField(
                        value = inputAddress,
                        onValueChange = { inputAddress = it },
                        label = { Text("Contract Address") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    androidx.compose.material3.OutlinedTextField(
                        value = inputSymbol,
                        onValueChange = { inputSymbol = it },
                        label = { Text("Symbol (e.g. USDC)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    androidx.compose.material3.OutlinedTextField(
                        value = inputDecimals,
                        onValueChange = { inputDecimals = it },
                        label = { Text("Decimals") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    BrutalistButton(text = "Add", onClick = { 
                        if (inputAddress.isNotEmpty() && inputSymbol.isNotEmpty()) {
                            viewModel.addToken(inputAddress, inputSymbol.uppercase(), inputDecimals.toIntOrNull() ?: 18)
                            showAddTokenDialog = false
                        }
                    })
                }
            }
        }

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

                    if (viewModel.address.length > 10) { 
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

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            BrutalistHeader("Dashboard")
            Row {
                androidx.compose.material3.IconButton(
                    onClick = { viewModel.refresh() }
                ) {
                    androidx.compose.material3.Icon(
                        imageVector = Icons.Filled.Refresh,
                        contentDescription = "Refresh",
                        tint = BrutalBlack,
                        modifier = Modifier.size(32.dp)
                    )
                }
                androidx.compose.material3.IconButton(
                    onClick = onSetupSecurity
                ) {
                    androidx.compose.material3.Icon(
                        imageVector = Icons.Filled.Lock,
                        contentDescription = "Security Settings",
                        tint = BrutalBlack,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            text = "TOTAL BALANCE",
            color = BrutalBlack,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = viewModel.totalBalanceUsd,
            color = BrutalBlack,
            fontSize = 42.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = (-1).sp
        )

        Spacer(modifier = Modifier.height(24.dp))
        
        // Tabs
        Row(
            modifier = Modifier.fillMaxWidth().border(1.dp, BrutalBlack)
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .background(if (viewModel.selectedTab == 0) BrutalBlack else BrutalWhite)
                    .clickable { viewModel.selectedTab = 0 }
                    .padding(12.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "Assets", 
                    color = if (viewModel.selectedTab == 0) BrutalWhite else BrutalBlack,
                    fontWeight = FontWeight.Bold
                )
            }
            Box(
                modifier = Modifier
                    .weight(1f)
                    .background(if (viewModel.selectedTab == 1) BrutalBlack else BrutalWhite)
                    .clickable { viewModel.selectedTab = 1 }
                    .padding(12.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "NFTs", 
                    color = if (viewModel.selectedTab == 1) BrutalWhite else BrutalBlack,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (viewModel.selectedTab == 0) {
            // Asset List
            androidx.compose.foundation.lazy.LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(viewModel.assets.size) { index ->
                    val asset = viewModel.assets[index]
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, BrutalBlack)
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(asset.symbol, fontWeight = FontWeight.Bold, color = BrutalBlack)
                            Text(asset.networkName, fontSize = 12.sp, color = androidx.compose.ui.graphics.Color.Gray)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(asset.balanceUsd, fontWeight = FontWeight.Bold, color = BrutalBlack)
                            Text(asset.balance, fontSize = 12.sp, color = BrutalBlack)
                        }
                    }
                }
                
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    BrutalistButton("Add Token +", onClick = { showAddTokenDialog = true }, inverted = true)
                }
            }
        } else {
            // NFTs Placeholder
            Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("No NFTs found", fontWeight = FontWeight.Bold, color = BrutalBlack)
                    Spacer(modifier = Modifier.height(8.dp))
                    BrutalistButton("Find NFTs", onClick = { }, inverted = true)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        // Actions
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            BrutalistButton(
                text = "Send",
                onClick = { showSendDialog = true },
                modifier = Modifier.weight(1f)
            )
            BrutalistButton(
                text = "Receive",
                onClick = { showReceiveDialog = true },
                modifier = Modifier.weight(1f),
                inverted = true
            )
        }
    }
}
