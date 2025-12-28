package com.antigravity.cryptowallet.ui.browser

import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
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
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.antigravity.cryptowallet.ui.theme.BrutalBlack
import com.antigravity.cryptowallet.ui.theme.BrutalWhite

@Composable
fun BrowserScreen() {
    var url by remember { mutableStateOf("https://google.com") }
    var inputUrl by remember { mutableStateOf("https://google.com") }
    var webView: WebView? by remember { mutableStateOf(null) }

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
        AndroidView(
            factory = { context ->
                WebView(context).apply {
                    settings.javaScriptEnabled = true
                    settings.domStorageEnabled = true
                    webViewClient = object : WebViewClient() {
                        override fun onPageFinished(view: WebView?, url: String?) {
                            super.onPageFinished(view, url)
                            if (url != null) inputUrl = url
                        }
                    }
                    loadUrl(url)
                    webView = this
                }
            },
            update = {
                if (it.url != url) {
                    it.loadUrl(url)
                }
            },
            modifier = Modifier.weight(1f)
        )

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
