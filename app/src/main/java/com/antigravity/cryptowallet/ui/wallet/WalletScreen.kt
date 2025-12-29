package com.antigravity.cryptowallet.ui.wallet

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.ui.window.Dialog
import androidx.compose.foundation.clickable
import androidx.compose.foundation.border
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.ArrowOutward
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.style.TextAlign
import com.antigravity.cryptowallet.utils.QrCodeGenerator
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


@HiltViewModel
class WalletViewModel @Inject constructor(
    private val walletRepository: WalletRepository,
    private val assetRepository: AssetRepository,
    private val networkRepository: NetworkRepository
) : ViewModel() {
    val address: String
        get() = walletRepository.getAddress()
    
    val networks = networkRepository.networks
    var activeNetwork by mutableStateOf(networkRepository.activeNetwork)
        private set

    // UI State
    var totalBalanceUsd by mutableStateOf("$0.00")
    var assets by mutableStateOf<List<com.antigravity.cryptowallet.data.models.AssetUiModel>>(emptyList())
    var isRefreshing by mutableStateOf(false)
    
    // Tab State
    var selectedTab by mutableStateOf(0) // 0 = Assets, 1 = NFTs

    private var allAssets = listOf<com.antigravity.cryptowallet.data.models.AssetUiModel>()

    init {
        loadData()
    }

    fun switchNetwork(networkId: String) {
        networkRepository.setActiveNetwork(networkId)
        activeNetwork = networkRepository.activeNetwork
        updateDisplayedAssets()
        refresh()
    }
    
    fun addToken(address: String, symbol: String, decimals: Int) {
        viewModelScope.launch {
            assetRepository.addToken(address, symbol, decimals, activeNetwork.id, symbol)
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
                    allAssets = assetList
                    updateDisplayedAssets()
                }
            }
            
            // Trigger initial refresh
            refresh()
        }
    }
    
    private fun updateDisplayedAssets() {
        val filtered = allAssets.filter { 
             it.networkName.equals(activeNetwork.name, ignoreCase = true) 
        }
        
        if (filtered.isEmpty()) {
            // assets = emptyList() // or keep empty
        } else {
             assets = filtered
             
             val total = assets.sumByDouble { it.rawBalance * it.price }
             totalBalanceUsd = String.format("$%.2f", total)
        }
    }

    // Mock generation removed

}

@Composable
fun WalletScreen(
    viewModel: WalletViewModel = hiltViewModel(),
    onSetupSecurity: () -> Unit = {},
    onNavigateToSend: () -> Unit = {},
    onNavigateToTokenDetail: (String) -> Unit = {}
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
        var showNetworkSelector by remember { mutableStateOf(false) }

        if (showNetworkSelector) {
            Dialog(onDismissRequest = { showNetworkSelector = false }) {
                Column(
                    modifier = Modifier
                        .background(BrutalWhite, RoundedCornerShape(24.dp))
                        .border(2.dp, BrutalBlack, RoundedCornerShape(24.dp))
                        .clip(RoundedCornerShape(24.dp))
                        .padding(24.dp)
                ) {
                    BrutalistHeader("Switch Network")
                    Spacer(modifier = Modifier.height(16.dp))
                    LazyColumn {
                        items(viewModel.networks.size) { index ->
                            val net = viewModel.networks[index]
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { 
                                        viewModel.switchNetwork(net.id)
                                        showNetworkSelector = false
                                    }
                                    .background(if (viewModel.activeNetwork.id == net.id) BrutalBlack else BrutalWhite, RoundedCornerShape(12.dp))
                                    .clip(RoundedCornerShape(12.dp))
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(net.name, color = if (viewModel.activeNetwork.id == net.id) BrutalWhite else BrutalBlack, fontWeight = FontWeight.Bold)
                                if (viewModel.activeNetwork.id == net.id) {
                                    Text("ACTIVE", color = BrutalWhite, fontSize = 10.sp, fontWeight = FontWeight.Black)
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                        }
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
                        .background(MaterialTheme.colorScheme.background)
                        .border(2.dp, MaterialTheme.colorScheme.onBackground)
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    BrutalistHeader("Add Token")
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("on ${viewModel.activeNetwork.name}", fontSize = 12.sp, color = Color.Gray)
                    Spacer(modifier = Modifier.height(8.dp))

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
                        .background(MaterialTheme.colorScheme.background)
                        .border(2.dp, MaterialTheme.colorScheme.onBackground)
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    BrutalistHeader("Receive Assets")
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("to ${viewModel.activeNetwork.name}", fontSize = 12.sp, color = Color.Gray)
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
            Column {
                BrutalistHeader("Dashboard")
                Row(
                    modifier = Modifier
                        .clickable { showNetworkSelector = true }
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(modifier = Modifier.size(8.dp).background(Color.Green, androidx.compose.foundation.shape.CircleShape))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        viewModel.activeNetwork.name.uppercase(),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black,
                        color = BrutalBlack
                    )
                }
            }
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
        
        Spacer(modifier = Modifier.height(16.dp))
        
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
            modifier = Modifier.fillMaxWidth().border(1.dp, BrutalBlack, RoundedCornerShape(12.dp)).clip(RoundedCornerShape(12.dp))
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
                            .border(1.dp, BrutalBlack, RoundedCornerShape(16.dp))
                        .clip(RoundedCornerShape(16.dp))
                        .clickable { onNavigateToTokenDetail(asset.symbol) }
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .background(BrutalBlack, androidx.compose.foundation.shape.CircleShape)
                                    .clip(androidx.compose.foundation.shape.CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    asset.symbol.take(1), 
                                    color = BrutalWhite, 
                                    fontWeight = FontWeight.Black,
                                    fontSize = 18.sp
                                )
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Text(asset.symbol, fontWeight = FontWeight.Bold, color = BrutalBlack)
                                Text(asset.networkName, fontSize = 12.sp, color = androidx.compose.ui.graphics.Color.Gray)
                            }
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
                onClick = onNavigateToSend,
                icon = Icons.Default.ArrowOutward,
                modifier = Modifier.weight(1f)
            )
            BrutalistButton(
                text = "Receive",
                onClick = { showReceiveDialog = true },
                icon = Icons.Default.FileDownload,
                modifier = Modifier.weight(1f),
                inverted = true
            )
        }
    }
}
