package com.antigravity.cryptowallet.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import com.antigravity.cryptowallet.ui.theme.BrutalBlack
import com.antigravity.cryptowallet.ui.theme.BrutalWhite

// STRICT BLACK AND WHITE PALETTE

@Composable
fun BrutalistButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    inverted: Boolean = false
) {
    val backgroundColor = if (inverted) BrutalWhite else BrutalBlack
    val contentColor = if (inverted) BrutalBlack else BrutalWhite
    
    // Manual ripple or state effect for "Fast interactions"
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    
    val currentBg = if (isPressed) contentColor else backgroundColor
    val currentContent = if (isPressed) backgroundColor else contentColor

    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = currentBg,
            contentColor = currentContent
        ),
        shape = RectangleShape, // No rounded corners for brutalism
        border = BorderStroke(2.dp, if (inverted) BrutalBlack else BrutalWhite),
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp),
        interactionSource = interactionSource
    ) {
        Text(
            text = text.uppercase(),
            style = MaterialTheme.typography.labelLarge.copy(
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
        )
    }
}

@Composable
fun BrutalistTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    singleLine: Boolean = true
) {
    Column(modifier = modifier) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .border(2.dp, BrutalBlack, RectangleShape)
                .padding(16.dp)
        ) {
            if (value.isEmpty()) {
                Text(
                    text = placeholder.uppercase(),
                    style = MaterialTheme.typography.bodyLarge.copy(color = Color.Gray),
                    fontFamily = FontFamily.Monospace
                )
            }
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                textStyle = TextStyle(
                    color = BrutalBlack,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 16.sp
                ),
                keyboardOptions = keyboardOptions,
                keyboardActions = keyboardActions,
                singleLine = singleLine,
                cursorBrush = SolidColor(BrutalBlack),
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
fun BrutalistHeader(text: String) {
    Text(
        text = text.uppercase(),
        style = MaterialTheme.typography.displayMedium,
        color = BrutalBlack,
        modifier = Modifier.padding(vertical = 24.dp)
    )
}

@Composable
fun BrutalistInfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .border(1.dp, BrutalBlack, RectangleShape)
            .padding(12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label.uppercase(),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontFamily = FontFamily.Monospace
        )
    }
}
