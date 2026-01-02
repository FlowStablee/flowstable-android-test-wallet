package com.antigravity.cryptowallet.ui.onboarding

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    onNavigateNext: () -> Unit
) {
    var startAnimation by remember { mutableStateOf(false) }
    val duration = 2000

    val scale by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0.5f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "scale"
    )

    val rotation by animateFloatAsState(
        targetValue = if (startAnimation) 360f else 0f,
        animationSpec = tween(durationMillis = 1500, easing = FastOutSlowInEasing),
        label = "rotation"
    )

    LaunchedEffect(Unit) {
        startAnimation = true
        delay(2500) // 2.5 seconds splash
        onNavigateNext()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Animated Logo Box
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .scale(scale)
                    .rotate(rotation)
                    .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(24.dp))
                    .border(4.dp, MaterialTheme.colorScheme.onBackground, RoundedCornerShape(24.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "FS",
                    color = MaterialTheme.colorScheme.onPrimary,
                    fontSize = 48.sp,
                    fontWeight = FontWeight.Black
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Text with Brutalist style
            Text(
                text = "FLOWSTABLE",
                style = MaterialTheme.typography.displayMedium,
                color = MaterialTheme.colorScheme.onBackground,
                fontWeight = FontWeight.Black,
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Secure. Decentralized. Brutal.",
                style = MaterialTheme.typography.bodyLarge,
                color = Color.Gray
            )
        }

        // Bottom Loading Bar (Visual only)
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 64.dp)
                .width(200.dp)
                .height(8.dp)
                .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(4.dp))
                .border(1.dp, MaterialTheme.colorScheme.onBackground, RoundedCornerShape(4.dp))
        ) {
            val progress by animateFloatAsState(
                targetValue = if (startAnimation) 1f else 0f,
                animationSpec = tween(durationMillis = 2000),
                label = "progress"
            )
            
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(progress)
                    .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(4.dp))
            )
        }
    }
}
