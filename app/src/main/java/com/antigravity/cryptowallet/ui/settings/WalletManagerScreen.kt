package com.antigravity.cryptowallet.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import com.antigravity.cryptowallet.data.models.WalletInfo
import com.antigravity.cryptowallet.data.models.WalletType
import com.antigravity.cryptowallet.ui.components.BrutalistButton
import com.antigravity.cryptowallet.ui.components.BrutalistHeader

@Composable
fun WalletManagerScreen(
    onBack: () -> Unit,
    onAddWallet: () -> Unit,
    viewModel: WalletManagerViewModel = hiltViewModel()
) {
    val wallets by viewModel.wallets.collectAsState(initial = emptyList())
    val activeWallet by viewModel.activeWallet.collectAsState(initial = null)
    
    var showDeleteDialog by remember { mutableStateOf<WalletInfo?>(null) }
    var showRenameDialog by remember { mutableStateOf<WalletInfo?>(null) }
    var renameText by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.onBackground)
            }
            BrutalistHeader("Wallets")
            Spacer(modifier = Modifier.weight(1f))
            IconButton(onClick = onAddWallet) {
                Icon(Icons.Default.Add, contentDescription = "Add Wallet", tint = MaterialTheme.colorScheme.primary)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        if (wallets.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("💼", fontSize = 48.sp)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "No wallets yet",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    BrutalistButton(
                        text = "Add Wallet",
                        onClick = onAddWallet
                    )
                }
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(wallets) { wallet ->
                    WalletCard(
                        wallet = wallet,
                        isActive = wallet.id == activeWallet?.id,
                        onSelect = { viewModel.switchWallet(wallet.id) },
                        onRename = {
                            renameText = wallet.name
                            showRenameDialog = wallet
                        },
                        onDelete = { showDeleteDialog = wallet }
                    )
                }
                
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedButton(
                        onClick = onAddWallet,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Add Another Wallet")
                    }
                }
            }
        }
    }

    // Delete Confirmation Dialog
    showDeleteDialog?.let { wallet ->
        AlertDialog(
            onDismissRequest = { showDeleteDialog = null },
            title = { Text("Delete Wallet?", fontWeight = FontWeight.Bold) },
            text = { 
                Column {
                    Text("Are you sure you want to delete \"${wallet.name}\"?")
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "⚠️ This action cannot be undone. Make sure you have backed up your recovery phrase.",
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 12.sp
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteWallet(wallet.id)
                        showDeleteDialog = null
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Rename Dialog
    showRenameDialog?.let { wallet ->
        Dialog(onDismissRequest = { showRenameDialog = null }) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Text(
                        "Rename Wallet",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = renameText,
                        onValueChange = { renameText = it },
                        label = { Text("Wallet Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = { showRenameDialog = null }) {
                            Text("Cancel")
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                if (renameText.isNotBlank()) {
                                    viewModel.renameWallet(wallet.id, renameText)
                                    showRenameDialog = null
                                }
                            }
                        ) {
                            Text("Save")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun WalletCard(
    wallet: WalletInfo,
    isActive: Boolean,
    onSelect: () -> Unit,
    onRename: () -> Unit,
    onDelete: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelect() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isActive) 
                MaterialTheme.colorScheme.primaryContainer 
            else 
                MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isActive) 4.dp else 1.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Wallet Icon
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        if (isActive) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.surfaceVariant,
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (wallet.type == WalletType.MNEMONIC) 
                        Icons.Default.AccountBalanceWallet 
                    else 
                        Icons.Default.Key,
                    contentDescription = null,
                    tint = if (isActive) 
                        MaterialTheme.colorScheme.onPrimary 
                    else 
                        MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        wallet.name,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (isActive) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                            color = MaterialTheme.colorScheme.primary,
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                "ACTIVE",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
                Text(
                    "${wallet.address.take(6)}...${wallet.address.takeLast(4)}",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    if (wallet.type == WalletType.MNEMONIC) "Seed Phrase" else "Private Key",
                    fontSize = 10.sp,
                    color = Color.Gray
                )
            }
            
            Box {
                IconButton(onClick = { showMenu = true }) {
                    Icon(
                        Icons.Default.MoreVert,
                        contentDescription = "More options",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Rename") },
                        onClick = {
                            showMenu = false
                            onRename()
                        },
                        leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) }
                    )
                    DropdownMenuItem(
                        text = { Text("Delete", color = MaterialTheme.colorScheme.error) },
                        onClick = {
                            showMenu = false
                            onDelete()
                        },
                        leadingIcon = { 
                            Icon(
                                Icons.Default.Delete, 
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error
                            ) 
                        }
                    )
                }
            }
        }
    }
}
