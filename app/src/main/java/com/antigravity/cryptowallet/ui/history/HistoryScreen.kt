package com.antigravity.cryptowallet.ui.history

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.antigravity.cryptowallet.data.db.TransactionEntity
import com.antigravity.cryptowallet.ui.components.BrutalistHeader
import com.antigravity.cryptowallet.ui.theme.BrutalBlack
import com.antigravity.cryptowallet.ui.theme.BrutalWhite
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun HistoryScreen(
    viewModel: HistoryViewModel = hiltViewModel()
) {
    val transactions by viewModel.transactions.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BrutalWhite)
            .padding(16.dp)
    ) {
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            BrutalistHeader("History")
            IconButton(onClick = { viewModel.refresh() }) { // Changed to IconButton
                Icon(
                    imageVector = Icons.Filled.Refresh, 
                    contentDescription = "Sync",
                    tint = BrutalBlack,
                    modifier = Modifier.size(32.dp)
                )
            }
        }

        if (transactions.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No transactions yet.", color = BrutalBlack)
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                items(transactions) { tx ->
                    TransactionItem(tx)
                }
            }
        }
    }
}

@Composable
fun TransactionItem(tx: TransactionEntity) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, BrutalBlack)
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Icon + Type
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(BrutalBlack),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (tx.type == "receive") Icons.Default.ArrowDownward else Icons.Default.ArrowUpward,
                    contentDescription = tx.type,
                    tint = BrutalWhite
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = tx.type.uppercase(),
                    fontWeight = FontWeight.Bold,
                    color = BrutalBlack
                )
                Text(
                    text = SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault()).format(Date(tx.timestamp)),
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }
        }

        // Amount + Status
        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = "${if(tx.type == "receive") "+" else "-"} ${tx.value} ${tx.symbol}",
                fontWeight = FontWeight.Bold,
                color = if (tx.type == "receive") Color(0xFF006400) else BrutalBlack
            )
            Text(
                text = tx.status.uppercase(),
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = if (tx.status == "success") Color.Gray else Color.Red
            )
        }
    }
}
