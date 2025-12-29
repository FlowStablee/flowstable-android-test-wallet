package com.antigravity.cryptowallet.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.antigravity.cryptowallet.ui.components.BrutalistHeader
import com.antigravity.cryptowallet.ui.theme.BrutalBlack
import com.antigravity.cryptowallet.ui.theme.BrutalWhite

@Composable
fun SettingsScreen(
    onSetupSecurity: () -> Unit,
    onViewSeedPhrase: () -> Unit,
    onViewAppInfo: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
    ) {
        BrutalistHeader("Settings")

        LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            item {
                SettingsSection("Security")
                SettingsItem(
                    title = "PIN & Biometrics",
                    subtitle = "Secure your wallet",
                    icon = Icons.Default.Lock,
                    onClick = onSetupSecurity
                )
                SettingsItem(
                    title = "Reveal Seed Phrase",
                    subtitle = "Backup your wallet",
                    icon = Icons.Default.VpnKey,
                    onClick = onViewSeedPhrase
                )
            }

            item {
                SettingsSection("App")
                SettingsItem(
                    title = "About & Appearance",
                    subtitle = "Version, Theme, Info",
                    icon = Icons.Default.Info,
                    onClick = onViewAppInfo
                )
            }
        }
    }
}

@Composable
fun SettingsSection(title: String) {
    Text(
        text = title.uppercase(),
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold,
        color = androidx.compose.ui.graphics.Color.Gray,
        modifier = Modifier.padding(bottom = 8.dp)
    )
}

@Composable
fun SettingsItem(
    title: String,
    subtitle: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, MaterialTheme.colorScheme.onBackground)
            .clickable { onClick() }
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(imageVector = icon, contentDescription = null, tint = MaterialTheme.colorScheme.onBackground)
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(text = title, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
                Text(text = subtitle, fontSize = 12.sp, color = androidx.compose.ui.graphics.Color.Gray)
            }
        }
        Icon(imageVector = Icons.Default.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onBackground)
    }
}
