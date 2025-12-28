package com.antigravity.cryptowallet.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val hash: String,
    val fromAddress: String,
    val toAddress: String,
    val value: String, // Stored as string to handle BigIntegers/Decimals safely
    val symbol: String,
    val timestamp: Long,
    val type: String, // "send", "receive", "swap"
    val status: String, // "pending", "success", "failed"
    val network: String // "eth", "bsc", "matic"
)
