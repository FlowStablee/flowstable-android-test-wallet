package com.antigravity.cryptowallet.ui.walletconnect

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.antigravity.cryptowallet.ui.components.BrutalistButton
import com.antigravity.cryptowallet.ui.components.BrutalistHeader
import com.antigravity.cryptowallet.ui.theme.BrutalWhite
import com.antigravity.cryptowallet.ui.theme.BrutalBlack

@Composable
fun WalletConnectScreen(
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BrutalWhite)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = BrutalBlack)
            }
            BrutalistHeader("Wallet Connect")
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Text(
            "Scan QR Code",
            fontSize = 24.sp,
            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
            color = BrutalBlack
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Box(
            modifier = Modifier
                .size(300.dp)
                .background(androidx.compose.ui.graphics.Color.LightGray)
                .border(2.dp, BrutalBlack),
            contentAlignment = Alignment.Center
        ) {
            Text("Camera / Scanner Placeholder", color = BrutalBlack)
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Text(
            "Connect your wallet to dApps by scanning their WalletConnect QR code.",
            textAlign = TextAlign.Center,
            color = BrutalBlack
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        BrutalistButton(
            text = "Paste URI",
            onClick = { /* TODO */ },
            modifier = Modifier.fillMaxWidth()
        )
    }
}
