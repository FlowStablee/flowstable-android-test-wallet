package com.antigravity.cryptowallet.data.models

data class WalletInfo(
    val id: String,
    val name: String,
    val address: String,
    val type: WalletType,
    val isActive: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

enum class WalletType {
    MNEMONIC,
    PRIVATE_KEY
}
