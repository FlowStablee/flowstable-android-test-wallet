package com.antigravity.cryptowallet.data.blockchain

import java.math.BigInteger
import javax.inject.Inject
import javax.inject.Singleton

data class Network(
    val id: String,
    val name: String,
    val rpcUrl: String,
    val chainId: Long,
    val symbol: String,
    val coingeckoId: String,
    val explorerUrl: String? = null,
    val iconUrl: String? = null
)

@Singleton
class NetworkRepository @Inject constructor() {
    private var _activeNetworkId = "eth"
    
    val networks = listOf(
        Network("eth", "Ethereum", "https://rpc.ankr.com/eth", 1L, "ETH", "ethereum", "https://etherscan.io", "https://assets.coingecko.com/coins/images/279/small/ethereum.png"),
        Network("bsc", "BNB Chain", "https://bsc-dataseed.binance.org", 56L, "BNB", "binancecoin", "https://bscscan.com", "https://assets.coingecko.com/coins/images/825/small/bnb-icon2_2x.png"),
        Network("matic", "Polygon", "https://polygon-rpc.com", 137L, "POL", "matic-network", "https://polygonscan.com", "https://assets.coingecko.com/coins/images/4713/small/matic-token-icon.png"),
        Network("base", "Base", "https://rpc.ankr.com/base", 8453L, "ETH", "ethereum", "https://basescan.org", "https://assets.coingecko.com/coins/images/30983/small/base.png"),
        Network("arb", "Arbitrum One", "https://arb1.arbitrum.io/rpc", 42161L, "ETH", "ethereum", "https://arbiscan.io", "https://assets.coingecko.com/coins/images/16547/small/photo_2023-03-29_21.47.00.jpeg"),
        Network("op", "Optimism", "https://mainnet.optimism.io", 10L, "ETH", "ethereum", "https://optimistic.etherscan.io", "https://assets.coingecko.com/coins/images/25244/small/Optimism.png")
    )
    
    val activeNetwork: Network
        get() = getNetwork(_activeNetworkId)

    fun getNetwork(id: String) = networks.find { it.id == id } ?: networks.first()
    
    fun setActiveNetwork(id: String) {
        _activeNetworkId = id
    }
}
