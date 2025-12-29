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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import com.antigravity.cryptowallet.ui.wallet.TokenDetailViewModel

@Composable
fun TokenDetailScreen(
    symbol: String,
    onBack: () -> Unit,
    viewModel: TokenDetailViewModel = androidx.hilt.navigation.compose.hiltViewModel()
) {
    LaunchedEffect(symbol) {
        viewModel.loadTokenData(symbol)
    }

    val price = viewModel.price
    val description = viewModel.description
    val contractAddress = viewModel.contractAddress
    val points = viewModel.graphPoints
    
    // Determine color based on trend
    val isPositive = if (points.size > 1) points.last() >= points.first() else true
    val trendColor = if (isPositive) Color(0xFF00C853) else Color.Red
    
    // ... UI Structure ...
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
            .verticalScroll(androidx.compose.foundation.rememberScrollState())
    ) {
        // Header
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.onBackground)
            }
            BrutalistHeader(symbol)
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Price
        Text("Current Price", fontSize = 12.sp, color = Color.Gray)
        Text(price, fontSize = 48.sp, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.onBackground)
        
        Spacer(modifier = Modifier.height(32.dp))

        // Graph
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .border(2.dp, MaterialTheme.colorScheme.onBackground)
                .padding(16.dp)
        ) {
            if (points.isNotEmpty()) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val path = Path()
                    val w = size.width
                    val h = size.height
                    
                    val max = points.maxOrNull() ?: 1.0
                    val min = points.minOrNull() ?: 0.0
                    val range = max - min
                    
                    points.forEachIndexed { i, p ->
                        val x = (i.toFloat() / (points.size - 1)) * w
                        // Invert Y because 0 is top
                        val y = h - ((p - min).toFloat() / range.toFloat()) * h
                        
                        if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
                    }

                    drawPath(
                        path = path,
                        color = trendColor,
                        style = Stroke(width = 4.dp.toPx())
                    )
                }
            } else {
                 Text("Loading Graph...", color = Color.Gray, modifier = Modifier.align(Alignment.Center))
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Contract Address
        if (contractAddress.isNotEmpty()) {
            Text("Contract Address", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .border(2.dp, MaterialTheme.colorScheme.onBackground)
                    .padding(12.dp)
            ) {
                Text(
                    text = contractAddress,
                    modifier = Modifier.weight(1f),
                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                    fontSize = 14.sp,
                    maxLines = 1,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Icon(
                    imageVector = Icons.Default.ContentCopy, 
                    contentDescription = "Copy",
                    tint = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.height(24.dp))
        }

        // Description
        Text("About $symbol", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            description, 
            fontSize = 12.sp, 
            lineHeight = 18.sp,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f)
        )
        Spacer(modifier = Modifier.height(24.dp))
        
        // Use Html text if needed, but plain text for now.
    }
}
