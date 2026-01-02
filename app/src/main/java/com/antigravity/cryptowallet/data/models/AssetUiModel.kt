package com.antigravity.cryptowallet.data.models

data class AssetUiModel(
    val id: String, // unique id (e.g. eth-native, eth-0xdac...)
    val symbol: String,
    val name: String,
    val balance: String,
    val balanceUsd: String,
    val iconUrl: String?,
    val networkName: String,
    val rawBalance: Double,
    val price: Double
)
