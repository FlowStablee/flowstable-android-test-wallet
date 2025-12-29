package com.antigravity.cryptowallet.ui.browser

import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import com.antigravity.cryptowallet.ui.components.BrutalistHeader
import com.antigravity.cryptowallet.ui.theme.BrutalBlack
import com.antigravity.cryptowallet.ui.theme.BrutalWhite

data class DApp(
    val name: String,
    val description: String,
    val url: String,
    val iconChar: Char,
    val category: String
)

@Composable
fun BrowserScreen(
    viewModel: BrowserViewModel = hiltViewModel()
) {
    val walletRepository = viewModel.walletRepository
    var url by remember { mutableStateOf("") } // Empty URL means Home
    var inputUrl by remember { mutableStateOf("") }
    var webView: WebView? by remember { mutableStateOf(null) }
    
    val address = walletRepository.getAddress()
    var activeNetwork by remember { mutableStateOf(viewModel.activeNetwork) }
    
    // Web3 Confirmation State
    var pendingRequest by remember { mutableStateOf<Web3Bridge.Web3Request?>(null) }
    var bridgeInstance by remember { mutableStateOf<Web3Bridge?>(null) }
    var showNetworkSelector by remember { mutableStateOf(false) }

    // DApp List
    val dapps = listOf(
        DApp("PancakeSwap", "Top DEX on BNB", "https://pancakeswap.finance", 'P', "DEFI"),
        DApp("Uniswap", "World's largest DEX", "https://app.uniswap.org", 'U', "DEFI"),
        DApp("Sushi", "DeFi Market", "https://www.sushi.com", 'S', "DEFI"),
        DApp("Blockscan", "Explore multiple chains", "https://blockscan.com", 'B', "UTILITIES"),
        DApp("Debank", "Portfolio Tracker", "https://debank.com", 'D', "UTILITIES"),
        DApp("Revoke", "Manage allowances", "https://revoke.cash", 'R', "UTILITIES"),
        DApp("Lens", "Web3 Social", "https://www.lens.xyz", 'L', "SOCIAL")
    )
    
    val groupedDapps = dapps.groupBy { it.category }

    // Handle Back Press to go Home
    BackHandler(enabled = url.isNotEmpty()) {
        if (webView?.canGoBack() == true) {
            webView?.goBack()
        } else {
            url = "" // Go home
            inputUrl = ""
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BrutalWhite)
    ) {
        if (showNetworkSelector) {
            androidx.compose.ui.window.Dialog(onDismissRequest = { showNetworkSelector = false }) {
                Column(
                    modifier = Modifier
                        .background(BrutalWhite)
                        .border(2.dp, BrutalBlack)
                        .padding(24.dp)
                ) {
                    BrutalistHeader("Select Network")
                    Spacer(modifier = Modifier.height(16.dp))
                    LazyColumn {
                        items(viewModel.networks) { net ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { 
                                        viewModel.switchNetwork(net.id)
                                        activeNetwork = viewModel.activeNetwork
                                        showNetworkSelector = false
                                        // RELOAD page to apply new network
                                        webView?.reload()
                                    }
                                    .background(if (activeNetwork.id == net.id) BrutalBlack else BrutalWhite)
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(net.name, color = if (activeNetwork.id == net.id) BrutalWhite else BrutalBlack, fontWeight = FontWeight.Bold)
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                }
            }
        }

        // Top Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .border(2.dp, BrutalBlack)
                .background(BrutalBlack)
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(onClick = { url = ""; inputUrl = "" }) {
                Icon(Icons.Default.Home, contentDescription = "Home", tint = BrutalWhite)
            }
            
            Row(
                modifier = Modifier
                    .border(1.dp, BrutalWhite)
                    .clickable { showNetworkSelector = true }
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(modifier = Modifier.size(6.dp).background(androidx.compose.ui.graphics.Color.Green))
                Spacer(modifier = Modifier.width(6.dp))
                Text(activeNetwork.name.uppercase(), color = BrutalWhite, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            }
        }

        // Validation / Search Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .border(2.dp, BrutalBlack)
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            BasicTextField(
                value = inputUrl,
                onValueChange = { inputUrl = it },
                textStyle = TextStyle(
                    color = BrutalBlack,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 14.sp
                ),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Go),
                keyboardActions = KeyboardActions(onGo = {
                    if (inputUrl.isNotEmpty()) {
                        url = if (!inputUrl.startsWith("http")) "https://$inputUrl" else inputUrl
                    }
                }),
                singleLine = true,
                cursorBrush = SolidColor(BrutalBlack),
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 8.dp),
                decorationBox = { innerTextField ->
                     if (inputUrl.isEmpty()) {
                         Text("Search or type URL", color = Color.Gray)
                     }
                     innerTextField()
                }
            )
            IconButton(onClick = {
                if (inputUrl.isNotEmpty()) {
                    url = if (!inputUrl.startsWith("http")) "https://$inputUrl" else inputUrl
                }
            }) {
                Icon(Icons.Default.Search, contentDescription = "Go", tint = BrutalBlack)
            }
        }

        // Content
        Box(modifier = Modifier.weight(1f)) {
            if (url.isEmpty()) {
                // Discover DApps Home
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    item {
                        Text(
                            "DISCOVER DAPPS", 
                            fontSize = 32.sp, 
                            fontWeight = FontWeight.Black,
                            modifier = Modifier.padding(bottom = 24.dp)
                        )
                    }
                    
                    groupedDapps.forEach { (category, dappList) ->
                        item {
                            Text(category, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 8.dp))
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(16.dp),
                                modifier = Modifier.padding(bottom = 24.dp)
                            ) {
                                items(dappList) { dapp ->
                                    DAppCard(dapp) {
                                        url = dapp.url
                                        inputUrl = dapp.url
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                // WebView
                AndroidView(
                    factory = { context ->
                        WebView(context).apply {
                            settings.javaScriptEnabled = true
                            settings.domStorageEnabled = true
                            settings.databaseEnabled = true
                            settings.loadWithOverviewMode = true
                            settings.useWideViewPort = true
                            
                            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                                settings.mixedContentMode = android.webkit.WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE
                            }
                            
                            val bridge = Web3Bridge(this, address, { activeNetwork.chainId }) { request ->
                                pendingRequest = request
                            }
                            bridgeInstance = bridge
                            addJavascriptInterface(bridge, "androidWallet")
                            
                            webViewClient = object : WebViewClient() {
                                override fun onPageStarted(view: WebView?, url: String?, favicon: android.graphics.Bitmap?) {
                                    super.onPageStarted(view, url, favicon)
                                    // We re-create the bridge in fact because chainId might change
                                    val currentBridge = Web3Bridge(view!!, address, { activeNetwork.chainId }) { request ->
                                        pendingRequest = request
                                    }
                                    bridgeInstance = currentBridge
                                    view.evaluateJavascript(currentBridge.getInjectionJs(), null)
                                }
                                
                                override fun onPageFinished(view: WebView?, url: String?) {
                                    super.onPageFinished(view, url)
                                    if (url != null) inputUrl = url
                                    bridgeInstance?.let { view?.evaluateJavascript(it.getInjectionJs(), null) }
                                }
                            }
                            
                            webChromeClient = android.webkit.WebChromeClient()
                            loadUrl(url)
                            webView = this
                        }
                    },
                    update = {
                        if (it.url != url && url.isNotEmpty()) {
                            it.loadUrl(url)
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }

            // Confirmation Overlay (Same as before)
            pendingRequest?.let { request ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(androidx.compose.ui.graphics.Color.Black.copy(alpha = 0.5f))
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(BrutalWhite)
                            .border(4.dp, BrutalBlack)
                            .padding(24.dp)
                    ) {
                        Text(
                            text = request.method.replace("eth_", "").replace("personal_", "").uppercase(),
                            style = androidx.compose.material3.MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Black
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "A dApp is requesting to ${request.method.lowercase()}.",
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 200.dp)
                                .border(1.dp, BrutalBlack)
                                .padding(8.dp)
                                .verticalScroll(rememberScrollState())
                        ) {
                            Text(
                                text = request.params,
                                fontSize = 12.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(24.dp))
                        
                        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            com.antigravity.cryptowallet.ui.components.BrutalistButton(
                                text = "Reject",
                                onClick = {
                                    bridgeInstance?.sendError(request.id, "User rejected")
                                    pendingRequest = null
                                },
                                modifier = Modifier.weight(1f),
                                inverted = true
                            )
                            com.antigravity.cryptowallet.ui.components.BrutalistButton(
                                text = "Confirm",
                                onClick = {
                                    // Handle logic based on method
                                    handleWeb3Request(request, bridgeInstance, walletRepository) { targetChainId ->
                                        val targetNet = viewModel.networks.find { it.chainId == targetChainId }
                                        if (targetNet != null) {
                                            viewModel.switchNetwork(targetNet.id)
                                            activeNetwork = viewModel.activeNetwork
                                            true
                                        } else {
                                            false
                                        }
                                    }
                                    pendingRequest = null
                                },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        }

        // Navigation Controls (Only show if URL is active)
        if (url.isNotEmpty()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(2.dp, BrutalBlack)
                    .background(BrutalWhite)
                    .padding(4.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                IconButton(onClick = { webView?.goBack() }) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = BrutalBlack)
                }
                IconButton(onClick = { webView?.reload() }) {
                    Icon(Icons.Default.Refresh, contentDescription = "Refresh", tint = BrutalBlack)
                }
                IconButton(onClick = { webView?.goForward() }) {
                    Icon(Icons.Default.ArrowForward, contentDescription = "Forward", tint = BrutalBlack)
                }
            }
        }
    }
}

@Composable
fun DAppCard(dapp: DApp, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .width(140.dp)
            .height(160.dp)
            .border(2.dp, BrutalBlack)
            .clickable(onClick = onClick)
            .padding(12.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(BrutalBlack),
            contentAlignment = Alignment.Center
        ) {
            Text(dapp.iconChar.toString(), color = BrutalWhite, fontWeight = FontWeight.Bold, fontSize = 20.sp)
        }
        
        Column {
            Text(dapp.name, fontWeight = FontWeight.Bold, maxLines = 1)
            Text(dapp.description, fontSize = 10.sp, color = Color.Gray, lineHeight = 12.sp, maxLines = 2)
        }
    }
}

// Logic to handle signing / sending
private fun handleWeb3Request(
    request: Web3Bridge.Web3Request,
    bridge: Web3Bridge?,
    walletRepository: com.antigravity.cryptowallet.data.wallet.WalletRepository,
    onSwitchNetwork: (Long) -> Boolean
) {
    val credentials = walletRepository.activeCredentials ?: return
    
    when (request.method) {
        "personal_sign" -> {
            try {
                // params: [message, address]
                val paramsArray = org.json.JSONArray(request.params)
                val message = paramsArray.getString(0)
                
                // Sign message
                val data = if (message.startsWith("0x")) org.web3j.utils.Numeric.hexStringToByteArray(message) else message.toByteArray()
                val signatureData = org.web3j.crypto.Sign.signPrefixedMessage(data, credentials.ecKeyPair)
                
                val r = org.web3j.utils.Numeric.toHexString(signatureData.r)
                val s = org.web3j.utils.Numeric.toHexString(signatureData.s).removePrefix("0x")
                val v = org.web3j.utils.Numeric.toHexString(signatureData.v).removePrefix("0x")
                val signature = r + s + v
                
                bridge?.sendResponse(request.id, "\"$signature\"")
            } catch (e: Exception) {
                bridge?.sendError(request.id, e.message ?: "Signing failed")
            }
        }
        "wallet_switchEthereumChain" -> {
            try {
                // params: [{ chainId: '0x...' }]
                val paramsArray = org.json.JSONArray(request.params)
                val paramObj = paramsArray.getJSONObject(0)
                val targetChainIdHex = paramObj.getString("chainId")
                val targetChainId = java.lang.Long.decode(targetChainIdHex)
                
                val success = onSwitchNetwork(targetChainId)
                
                if (success) {
                    bridge?.sendResponse(request.id, "null")
                } else {
                     bridge?.sendError(request.id, "Chain ID $targetChainId not supported")
                }
            } catch (e: Exception) {
                bridge?.sendError(request.id, "Switch failed: ${e.message}")
            }
        }
        "eth_requestPermissions" -> {
             // Just accept for now
             bridge?.sendResponse(request.id, "[{\"parentCapability\": \"eth_accounts\"}]")
        }
        "eth_sendTransaction" -> {
            // For now, we mock success for the browser to keep flow going, 
            // In a real one we'd use BlockchainService to send raw transaction.
            bridge?.sendResponse(request.id, "\"0x${"f".repeat(64)}\"") // Mock tx hash
        }
    }
}

