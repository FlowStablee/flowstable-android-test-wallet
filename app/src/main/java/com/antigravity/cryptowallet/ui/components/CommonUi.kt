package com.antigravity.cryptowallet.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.draw.clip
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
    inverted: Boolean = false,
    enabled: Boolean = true,
    icon: androidx.compose.ui.graphics.vector.ImageVector? = null
) {
    val backgroundColor = if (inverted) MaterialTheme.colorScheme.background else MaterialTheme.colorScheme.onBackground
    val contentColor = if (inverted) MaterialTheme.colorScheme.onBackground else MaterialTheme.colorScheme.background
    
    // Manual ripple or state effect for "Fast interactions"
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    
    val currentBg = if (!enabled) Color.Gray else if (isPressed) contentColor else backgroundColor
    val currentContent = if (!enabled) Color.LightGray else if (isPressed) backgroundColor else contentColor

    Button(
        onClick = onClick,
        enabled = enabled,
        colors = ButtonDefaults.buttonColors(
            containerColor = currentBg,
            contentColor = currentContent,
            disabledContainerColor = Color.Gray,
            disabledContentColor = Color.LightGray
        ),
        shape = RoundedCornerShape(24.dp), // More rounded as requested
        border = BorderStroke(2.dp, if (inverted) MaterialTheme.colorScheme.onBackground else MaterialTheme.colorScheme.background),
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp),
        interactionSource = interactionSource,
        contentPadding = PaddingValues(horizontal = 16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(
                text = text.uppercase(),
                style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            )
        }
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
                .border(2.dp, MaterialTheme.colorScheme.onBackground, RoundedCornerShape(12.dp))
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
                    color = MaterialTheme.colorScheme.onBackground,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 16.sp
                ),
                keyboardOptions = keyboardOptions,
                keyboardActions = keyboardActions,
                singleLine = singleLine,
                cursorBrush = SolidColor(MaterialTheme.colorScheme.onBackground),
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
        color = MaterialTheme.colorScheme.onBackground,
        modifier = Modifier.padding(vertical = 24.dp)
    )
}

@Composable
fun BrutalistInfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .border(1.dp, MaterialTheme.colorScheme.onBackground, RoundedCornerShape(12.dp))
            .padding(12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label.uppercase(),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontFamily = FontFamily.Monospace,
            color = MaterialTheme.colorScheme.onBackground
        )
    }
}

data class BottomNavItem(
    val title: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val route: String
)

@Composable
fun BrutalistBottomBar(
    items: List<BottomNavItem>,
    currentRoute: String?,
    onItemClick: (String) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .border(2.dp, MaterialTheme.colorScheme.onBackground, RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.background, RoundedCornerShape(20.dp))
            .clip(RoundedCornerShape(20.dp))
            .height(64.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        items.forEach { item ->
            val isSelected = currentRoute == item.route
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .padding(4.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(if (isSelected) MaterialTheme.colorScheme.onBackground else Color.Transparent)
                    .clickable { onItemClick(item.route) }
                    .padding(4.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    androidx.compose.material3.Icon(
                        imageVector = item.icon,
                        contentDescription = item.title,
                        tint = if (isSelected) MaterialTheme.colorScheme.background else MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        text = item.title.uppercase(),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isSelected) MaterialTheme.colorScheme.background else MaterialTheme.colorScheme.onBackground
                    )
                }
            }
        }
    }
}
