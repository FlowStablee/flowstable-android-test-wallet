package com.antigravity.cryptowallet.ui.browser

import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.antigravity.cryptowallet.ui.theme.BrutalBlack
import com.antigravity.cryptowallet.ui.theme.BrutalWhite

import androidx.hilt.navigation.compose.hiltViewModel
import com.antigravity.cryptowallet.data.wallet.WalletRepository

@Composable
fun BrowserScreen(
    viewModel: BrowserViewModel = hiltViewModel()
) {
    val walletRepository = viewModel.walletRepository
    var url by remember { mutableStateOf("https://pancakeswap.finance") }
    var inputUrl by remember { mutableStateOf("https://pancakeswap.finance") }
    var webView: WebView? by remember { mutableStateOf(null) }
    
    val address = walletRepository.getAddress()
    
    // Web3 Confirmation State
    var pendingRequest by remember { mutableStateOf<Web3Bridge.Web3Request?>(null) }
    var bridgeInstance by remember { mutableStateOf<Web3Bridge?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BrutalWhite)
    ) {
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
                    url = if (!inputUrl.startsWith("http")) "https://$inputUrl" else inputUrl
                }),
                singleLine = true,
                cursorBrush = SolidColor(BrutalBlack),
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 8.dp)
            )
            IconButton(onClick = {
                url = if (!inputUrl.startsWith("http")) "https://$inputUrl" else inputUrl
            }) {
                Icon(Icons.Default.Search, contentDescription = "Go", tint = BrutalBlack)
            }
        }

        // WebView
        Box(modifier = Modifier.weight(1f)) {
            AndroidView(
                factory = { context ->
                    WebView(context).apply {
                        settings.javaScriptEnabled = true
                        settings.domStorageEnabled = true
                        settings.databaseEnabled = true
                        settings.loadWithOverviewMode = true
                        settings.useWideViewPort = true
                        
                        // Support Mixed Content (HTTP on HTTPS) - sometimes needed
                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                            settings.mixedContentMode = android.webkit.WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE
                        }
                        
                        val bridge = Web3Bridge(this, address) { request ->
                            pendingRequest = request
                        }
                        bridgeInstance = bridge
                        addJavascriptInterface(bridge, "androidWallet")
                        
                        webViewClient = object : WebViewClient() {
                            override fun onPageStarted(view: WebView?, url: String?, favicon: android.graphics.Bitmap?) {
                                super.onPageStarted(view, url, favicon)
                                view?.evaluateJavascript(bridge.getInjectionJs(), null)
                            }
                            
                            override fun onPageFinished(view: WebView?, url: String?) {
                                super.onPageFinished(view, url)
                                if (url != null) inputUrl = url
                                view?.evaluateJavascript(bridge.getInjectionJs(), null)
                            }
                        }
                        
                        webChromeClient = android.webkit.WebChromeClient()
                        
                        loadUrl(url)
                        webView = this
                    }
                },
                update = {
                    if (it.url != url) {
                        it.loadUrl(url)
                    }
                },
                modifier = Modifier.fillMaxSize()
            )

            // Confirmation Overlay
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
                                    handleWeb3Request(request, bridgeInstance, walletRepository)
                                    pendingRequest = null
                                },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        }

        // Navigation Controls
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

// Logic to handle signing / sending
private fun handleWeb3Request(
    request: Web3Bridge.Web3Request,
    bridge: Web3Bridge?,
    walletRepository: com.antigravity.cryptowallet.data.wallet.WalletRepository
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
        "eth_sendTransaction" -> {
            // For now, we mock success for the browser to keep flow going, 
            // In a real one we'd use BlockchainService to send raw transaction.
            bridge?.sendResponse(request.id, "\"0x${"f".repeat(64)}\"") // Mock tx hash
        }
    }
}
