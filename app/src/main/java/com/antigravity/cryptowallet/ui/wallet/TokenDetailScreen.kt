package com.antigravity.cryptowallet.ui.wallet

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.antigravity.cryptowallet.ui.components.BrutalistHeader

@Composable
fun TokenDetailScreen(
    symbol: String,
    onBack: () -> Unit
) {
    // Mock Data based on symbol
    val color = when(symbol.uppercase()) {
        "ETH" -> Color(0xFF627EEA)
        "BNB" -> Color(0xFFF3BA2F)
        "USDT" -> Color(0xFF26A17B)
        "USDC" -> Color(0xFF2775CA)
        "LINK" -> Color(0xFF2A5ADA)
        "CAKE" -> Color(0xFFD1884F)
        else -> MaterialTheme.colorScheme.primary
    }
    
    val price = when(symbol.uppercase()) {
        "ETH" -> "$2,245.12"
        "BNB" -> "$312.45"
        "USDT" -> "$1.00"
        "Link" -> "$14.50"
        else -> "$0.00"
    }

    val change = "+2.4%"
    val isPositive = true

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
    ) {
        // Header
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.onBackground)
            }
            BrutalistHeader(symbol)
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Price & Balance
        Text("Current Price", fontSize = 12.sp, color = Color.Gray)
        Row(verticalAlignment = Alignment.Bottom) {
            Text(price, fontSize = 48.sp, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.onBackground)
            Spacer(modifier = Modifier.width(8.dp))
            Text(change, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = if (isPositive) Color(0xFF00C853) else Color.Red, modifier = Modifier.padding(bottom = 8.dp))
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Graph
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .border(2.dp, MaterialTheme.colorScheme.onBackground)
                .padding(16.dp)
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val path = Path()
                val w = size.width
                val h = size.height
                
                // Simple random-ish looking line
                path.moveTo(0f, h * 0.7f)
                path.cubicTo(w * 0.2f, h * 0.9f, w * 0.4f, h * 0.2f, w * 0.6f, h * 0.5f)
                path.cubicTo(w * 0.8f, h * 0.8f, w * 0.9f, h * 0.1f, w, h * 0.3f)

                drawPath(
                    path = path,
                    color = color,
                    style = Stroke(width = 4.dp.toPx())
                )
            }
            Text("Growth Graph (7D)", fontSize = 10.sp, color = Color.Gray, modifier = Modifier.align(Alignment.TopEnd))
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Contract Address
        Text("Contract Address", fontSize = 12.sp, color = Color.Gray)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp)
                .border(1.dp, MaterialTheme.colorScheme.onBackground)
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                "0x71C...9A23", 
                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                color = MaterialTheme.colorScheme.onBackground
            )
            Icon(Icons.Default.ContentCopy, contentDescription = "Copy", tint = MaterialTheme.colorScheme.onBackground, modifier = Modifier.size(16.dp))
        }

        Spacer(modifier = Modifier.height(24.dp))

        // History
        Text("Recent Transactions", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(5) { i ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f))
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(if (i % 2 == 0) "Received" else "Sent", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
                        Text("Dec ${20-i}", fontSize = 10.sp, color = Color.Gray)
                    }
                    Text(
                        "${if (i % 2 == 0) "+" else "-"}${10 + i * 2} $symbol", 
                        fontWeight = FontWeight.Bold, 
                        color = if (i % 2 == 0) Color(0xFF00C853) else Color.Red
                    )
                }
            }
        }
    }
}
