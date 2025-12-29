package com.antigravity.cryptowallet.ui.security

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.antigravity.cryptowallet.ui.components.BrutalistHeader
import com.antigravity.cryptowallet.ui.theme.BrutalBlack
import com.antigravity.cryptowallet.ui.theme.BrutalWhite

@Composable
fun ShowSeedScreen(
    mnemonic: String,
    onBack: () -> Unit
) {
    val words = mnemonic.split(" ")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BrutalWhite)
            .padding(16.dp)
    ) {
        BrutalistHeader("Seed Phrase")
        
        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Write down these 12 words in order and keep them safe. Anyone with these words can access your wallet.",
            fontSize = 14.sp,
            color = BrutalBlack,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.weight(1f)
        ) {
            itemsIndexed(words) { index, word ->
                SeedWordItem(index + 1, word)
            }
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

@Composable
fun SeedWordItem(index: Int, word: String) {
    Row(
        modifier = Modifier
            .border(2.dp, BrutalBlack)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "$index.",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = BrutalBlack.copy(alpha = 0.5f),
            modifier = Modifier.width(24.dp)
        )
        Text(
            text = word,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = BrutalBlack
        )
    }
}

// Extension to Box for clickable without ripples (keeping brutalist feel)
