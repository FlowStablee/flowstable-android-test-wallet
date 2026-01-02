package com.antigravity.cryptowallet.ui.wallet

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.antigravity.cryptowallet.ui.components.BrutalistButton

enum class TransactionStatus {
    SUCCESS,
    FAILED,
    PENDING
}

@Composable
fun TransactionResultScreen(
    status: TransactionStatus = TransactionStatus.SUCCESS,
    amount: String,
    symbol: String,
    recipient: String,
    txHash: String,
    networkName: String = "Ethereum",
    explorerUrl: String? = null,
    onDone: () -> Unit
) {
    val transitionState = remember { MutableTransitionState(false) }
    transitionState.targetState = true
    
    val transition = updateTransition(transitionState, label = "ResultTransition")
    
    val circleProgress by transition.animateFloat(
        transitionSpec = { tween(durationMillis = 600, easing = FastOutSlowInEasing) },
        label = "Circle"
    ) { if (it) 1f else 0f }
    
    val iconProgress by transition.animateFloat(
        transitionSpec = { 
            tween(durationMillis = 400, delayMillis = 600, easing = LinearOutSlowInEasing) 
        },
        label = "Icon"
    ) { if (it) 1f else 0f }

    val backgroundColor = when(status) {
        TransactionStatus.SUCCESS -> Color(0xFF00C853)
        TransactionStatus.FAILED -> Color(0xFFD32F2F)
        TransactionStatus.PENDING -> Color(0xFFFFA000)
    }
    val contentColor = Color.White
    
    val clipboardManager = LocalClipboardManager.current
    val uriHandler = LocalUriHandler.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Animated Icon
        Box(
            modifier = Modifier.size(120.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val strokeWidth = 8.dp.toPx()
                val radius = size.minDimension / 2 - strokeWidth / 2
                
                // Draw Circle
                drawArc(
                    color = contentColor,
                    startAngle = -90f,
                    sweepAngle = 360f * circleProgress,
                    useCenter = false,
                    style = Stroke(width = strokeWidth)
                )

                // Draw Icon based on status
                if (iconProgress > 0) {
                    when(status) {
                        TransactionStatus.SUCCESS -> {
                            // Checkmark
                            val p1 = Offset(center.x - radius * 0.4f, center.y)
                            val p2 = Offset(center.x - radius * 0.1f, center.y + radius * 0.3f)
                            val p3 = Offset(center.x + radius * 0.5f, center.y - radius * 0.4f)
                            
                            val totalLen1 = (p2 - p1).getDistance()
                            val totalLen2 = (p3 - p2).getDistance()
                            val totalLen = totalLen1 + totalLen2
                            val currentLen = totalLen * iconProgress
                            
                            if (currentLen > 0) {
                                val end1 = if (currentLen > totalLen1) p2 else p1 + (p2 - p1) * (currentLen / totalLen1)
                                drawLine(contentColor, p1, end1, strokeWidth, StrokeCap.Round)
                            }
                            if (currentLen > totalLen1) {
                                val len2 = currentLen - totalLen1
                                val end2 = p2 + (p3 - p2) * (len2 / totalLen2)
                                drawLine(contentColor, p2, end2, strokeWidth, StrokeCap.Round)
                            }
                        }
                        TransactionStatus.FAILED -> {
                            // X Mark
                            val offset = radius * 0.35f * iconProgress
                            drawLine(contentColor, 
                                Offset(center.x - offset, center.y - offset),
                                Offset(center.x + offset, center.y + offset),
                                strokeWidth, StrokeCap.Round)
                            drawLine(contentColor,
                                Offset(center.x + offset, center.y - offset),
                                Offset(center.x - offset, center.y + offset),
                                strokeWidth, StrokeCap.Round)
                        }
                        TransactionStatus.PENDING -> {
                            // Clock-like dots
                            val dotRadius = 6.dp.toPx()
                            for (i in 0..2) {
                                val angle = Math.PI / 2 + i * Math.PI * 2 / 3
                                val dotX = center.x + (radius * 0.5f * kotlin.math.cos(angle)).toFloat()
                                val dotY = center.y + (radius * 0.5f * kotlin.math.sin(angle)).toFloat()
                                drawCircle(contentColor.copy(alpha = 0.3f + 0.7f * iconProgress), dotRadius, Offset(dotX, dotY))
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(40.dp))

        // Text Content
        AnimatedVisibility(
            visibleState = transitionState,
            enter = fadeIn(tween(1000, 1000)) + slideInVertically(tween(1000, 1000)) { 50 }
        ) {
             Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = when(status) {
                        TransactionStatus.SUCCESS -> "Transaction Successful"
                        TransactionStatus.FAILED -> "Transaction Failed"
                        TransactionStatus.PENDING -> "Transaction Pending"
                    },
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = contentColor
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "$amount $symbol",
                    fontSize = 36.sp,
                    fontWeight = FontWeight.Black,
                    color = contentColor,
                    fontFamily = FontFamily.Monospace
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "to ${recipient.take(6)}...${recipient.takeLast(4)}",
                    fontSize = 14.sp,
                    color = contentColor.copy(alpha = 0.8f),
                    fontFamily = FontFamily.Monospace
                )

                Spacer(modifier = Modifier.height(24.dp))
                
                // Transaction Hash Card
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.2f)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            "Transaction Hash",
                            fontSize = 12.sp,
                            color = contentColor.copy(alpha = 0.7f)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "${txHash.take(10)}...${txHash.takeLast(8)}",
                                fontSize = 14.sp,
                                color = contentColor,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Medium
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            IconButton(
                                onClick = { clipboardManager.setText(AnnotatedString(txHash)) },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    Icons.Default.ContentCopy,
                                    contentDescription = "Copy",
                                    tint = contentColor,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                        
                        if (explorerUrl != null) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier
                                    .clickable { uriHandler.openUri("$explorerUrl/tx/$txHash") }
                                    .padding(4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.OpenInNew,
                                    contentDescription = "View on Explorer",
                                    tint = contentColor,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    "View on Explorer",
                                    fontSize = 12.sp,
                                    color = contentColor
                                )
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "Network: $networkName",
                    fontSize = 12.sp,
                    color = contentColor.copy(alpha = 0.6f)
                )
             }
        }

        Spacer(modifier = Modifier.weight(1f))

        BrutalistButton(
            text = "DONE",
            onClick = onDone,
            inverted = true,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

// Keep old function for backward compatibility
@Composable
fun TransactionSuccessScreen(
    amount: String,
    symbol: String,
    recipient: String,
    txHash: String,
    onDone: () -> Unit
) {
    TransactionResultScreen(
        status = TransactionStatus.SUCCESS,
        amount = amount,
        symbol = symbol,
        recipient = recipient,
        txHash = txHash,
        onDone = onDone
    )
}
