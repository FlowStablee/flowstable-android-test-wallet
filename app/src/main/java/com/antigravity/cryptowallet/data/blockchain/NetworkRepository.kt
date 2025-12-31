package com.antigravity.cryptowallet.data.blockchain

import java.math.BigInteger
import javax.inject.Inject
import javax.inject.Singleton

data class Network(
    val id: String,
    val name: String,
    val rpcUrl: String,
    val chainId: Int,
    val symbol: String,
    val coingeckoId: String,
    val explorerUrl: String? = null,
    val iconUrl: String? = null
)

@Singleton
class NetworkRepository @Inject constructor() {
    private var _activeNetworkId = "eth"
    
    val networks = listOf(
        Network("eth", "Ethereum", "https://eth.llamarpc.com", 1, "ETH", "ethereum", "https://etherscan.io", "https://assets.coingecko.com/coins/images/279/small/ethereum.png"),
        Network("bsc", "BNB Chain", "https://binance.llamarpc.com", 56, "BNB", "binancecoin", "https://bscscan.com", "https://assets.coingecko.com/coins/images/825/small/bnb-icon2_2x.png"),
        Network("matic", "Polygon", "https://polygon.llamarpc.com", 137, "POL", "matic-network", "https://polygonscan.com", "https://assets.coingecko.com/coins/images/4713/small/matic-token-icon.png"),
        Network("base", "Base", "https://base.llamarpc.com", 8453, "ETH", "ethereum", "https://basescan.org", "https://assets.coingecko.com/coins/images/30983/small/base.png"),
        Network("arb", "Arbitrum", "https://arbitrum.llamarpc.com", 42161, "ETH", "ethereum", "https://arbiscan.io", "https://assets.coingecko.com/coins/images/16547/small/photo_2023-03-29_21.47.00.jpeg"),
        Network("op", "Optimism", "https://optimism.llamarpc.com", 10, "ETH", "ethereum", "https://optimistic.etherscan.io", "https://assets.coingecko.com/coins/images/25244/small/Optimism.png")
    )
    
    val activeNetwork: Network
        get() = getNetwork(_activeNetworkId)

    fun getNetwork(id: String) = networks.find { it.id == id } ?: networks.first()
    
    fun setActiveNetwork(id: String) {
        _activeNetworkId = id
    }
}
