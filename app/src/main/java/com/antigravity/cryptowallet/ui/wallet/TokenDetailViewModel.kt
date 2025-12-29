package com.antigravity.cryptowallet.ui.wallet

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.antigravity.cryptowallet.data.repository.CoinRepository
import com.antigravity.cryptowallet.data.wallet.TransactionRepository
import com.antigravity.cryptowallet.data.wallet.WalletRepository
import com.antigravity.cryptowallet.data.db.TokenDao
import com.antigravity.cryptowallet.data.blockchain.BlockchainService
import com.antigravity.cryptowallet.data.blockchain.NetworkRepository
import com.antigravity.cryptowallet.data.db.TransactionEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject
import java.math.BigDecimal

@HiltViewModel
class TokenDetailViewModel @Inject constructor(
    private val coinRepository: CoinRepository,
    private val transactionRepository: TransactionRepository,
    private val walletRepository: WalletRepository,
    private val tokenDao: TokenDao,
    private val blockchainService: BlockchainService,
    private val networkRepository: NetworkRepository
) : ViewModel() {

    var balance by mutableStateOf("0.0")
        private set

    var description by mutableStateOf("Loading...")
        private set
    
    var price by mutableStateOf("Loading...")
        private set

    var contractAddress by mutableStateOf("")
        private set

    var graphPoints by mutableStateOf<List<Double>>(emptyList())
        private set
    
    var ohlcData by mutableStateOf<List<List<Double>>>(emptyList())
        private set

    var transactions by mutableStateOf<List<TransactionEntity>>(emptyList())
        private set

    private var currentSymbol: String = ""

    val address: String
        get() = walletRepository.getAddress()

    fun loadTokenData(symbol: String) {
        currentSymbol = symbol
        
        // 1. Observe transactions locally filtered by symbol
        viewModelScope.launch {
            transactionRepository.transactions.collect { allTxs ->
                transactions = allTxs.filter { it.symbol.equals(symbol, ignoreCase = true) }
            }
        }

        // 2. Fetch Balance and Refresh Transactions
        viewModelScope.launch {
            try {
                val tokenEntity = tokenDao.getTokenBySymbol(symbol)
                val netId = tokenEntity?.chainId ?: when(symbol.uppercase()) {
                    "BNB" -> "bsc"
                    "MATIC", "POL" -> "matic"
                    "ETH" -> "eth"
                    else -> "eth"
                }
                val network = networkRepository.getNetwork(netId)
                
                // Trigger Transaction Refresh
                val walletAddress = walletRepository.getAddress()
                try {
                    transactionRepository.refreshTransactions(walletAddress, network)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                
                // Get Balance
                val rawBalance = if (tokenEntity?.contractAddress != null) {
                    blockchainService.getTokenBalance(network.rpcUrl, tokenEntity.contractAddress, walletAddress)
                } else {
                    blockchainService.getBalance(network.rpcUrl, walletAddress)
                }
                
                val decimals = tokenEntity?.decimals ?: 18
                val ethBalance = BigDecimal(rawBalance).divide(BigDecimal.TEN.pow(decimals), 4, BigDecimal.ROUND_HALF_UP)
                balance = String.format("%.4f %s", ethBalance, symbol)
            } catch (e: Exception) {
                e.printStackTrace()
                balance = "0.0000 $symbol"
            }
        }

        // 3. Fetch Coin Info and Price with separate Error Handling
        viewModelScope.launch {
            val tokenEntity = tokenDao.getTokenBySymbol(symbol)
            val id = tokenEntity?.coingeckoId ?: when(symbol.uppercase()) {
                "ETH" -> "ethereum"
                "BNB" -> "binancecoin"
                "BTC" -> "bitcoin"
                "USDT" -> "tether"
                "USDC" -> "usd-coin"
                "LINK" -> "chainlink"
                "CAKE" -> "pancakeswap-token"
                "MATIC", "POL" -> "matic-network"
                "SOL" -> "solana"
                else -> "ethereum"
            }

            // Fetch Info
            launch {
                try {
                    val info = coinRepository.getCoinInfo(id)
                    val rawDescription = info.description.en
                    description = rawDescription.replace(Regex("<.*?>"), "") 
                        .take(300) + (if (rawDescription.length > 300) "..." else "")
                    
                    val rawAddr = info.platforms?.entries?.firstOrNull()?.value
                    contractAddress = if (!rawAddr.isNullOrEmpty()) rawAddr else "Native Token"
                } catch (e: Exception) {
                    e.printStackTrace()
                    description = when(symbol.uppercase()) {
                        "ETH" -> "Ethereum is a decentralized, open-source blockchain with smart contract functionality. Ether (ETH) is the native cryptocurrency of the platform."
                        "BNB" -> "BNB is the native cryptocurrency of the Binance ecosystem and powers the Binance Smart Chain."
                        "MATIC", "POL" -> "Polygon is a protocol and a framework for building and connecting Ethereum-compatible blockchain networks."
                        else -> "Failed to load detailed info for $symbol."
                    }
                    contractAddress = if (symbol.uppercase() == "ETH" || symbol.uppercase() == "BNB") "Native Token" else ""
                }
            }

            // Fetch Chart, OHLC and Initial Price
            launch {
                try {
                    // Fetch OHLC for Candlestick (1 year = 365 days)
                    ohlcData = coinRepository.getOHLC(id, "365")
                    
                    // Also fetch regular chart for trend/price if needed (optional, using OHLC close instead)
                    if (ohlcData.isNotEmpty()) {
                        val lastCandle = ohlcData.last()
                        graphPoints = ohlcData.map { it[4] } // Use close prices for mini-trend
                        price = String.format("$%.2f", lastCandle[4])
                    }

                } catch (e: Exception) {
                    e.printStackTrace()
                    // Fallback to regular chart if OHLC fails
                    try {
                        val chart = coinRepository.getMarketChart(id)
                        graphPoints = chart.prices.map { it[1] }
                        val currentPrice = graphPoints.lastOrNull() ?: 0.0
                        price = String.format("$%.2f", currentPrice)
                    } catch (e2: Exception) {
                         // Fallback to Simple Price if Chart fails
                        try {
                            val priceMap = coinRepository.getSimplePrice(id)
                            val simplePrice = priceMap[id]?.get("usd") ?: 0.0
                            price = if (simplePrice > 0) String.format("$%.2f", simplePrice) else "N/A"
                        } catch (e3: Exception) {
                            price = "Error"
                        }
                    }
                    
                    // Generate mock OHLC data if everything fails so UI isn't empty
                    if (ohlcData.isEmpty()) {
                        ohlcData = List(30) { i ->
                            val base = 100.0 + Math.random() * 20.0
                            listOf(i.toDouble(), base, base + 5, base - 5, base + 2)
                        }
                    }
                }
            }
        }
    }
}
