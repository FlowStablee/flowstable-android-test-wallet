package com.antigravity.cryptowallet.ui.security

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.antigravity.cryptowallet.ui.components.BrutalistHeader
import com.antigravity.cryptowallet.ui.theme.BrutalBlack
import com.antigravity.cryptowallet.ui.theme.BrutalWhite

@Composable
fun ShowPrivateKeyScreen(
    privateKey: String,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BrutalWhite)
            .padding(16.dp)
    ) {
        BrutalistHeader("Private Key")
        
        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "NEVER SHARE YOUR PRIVATE KEY. Anyone with this key can steal your funds. Keep it offline and safe.",
            fontSize = 14.sp,
            color = Color.Red,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .border(2.dp, BrutalBlack)
                .background(Color.White)
                .padding(16.dp)
        ) {
            Text(
                text = if (privateKey.startsWith("0x")) privateKey else "0x$privateKey",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = BrutalBlack,
                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                modifier = Modifier.fillMaxWidth(),
                overflow = androidx.compose.ui.text.style.TextOverflow.Visible
            )
        }

        Spacer(modifier = Modifier.height(24.dp))
        
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(BrutalBlack)
                .padding(16.dp)
                .clickable { onBack() },
            contentAlignment = Alignment.Center
        ) {
            Text("DONE", color = BrutalWhite, fontWeight = FontWeight.Black)
        }
    }
}
