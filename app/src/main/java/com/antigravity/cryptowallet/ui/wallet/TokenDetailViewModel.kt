package com.antigravity.cryptowallet.ui.wallet

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.antigravity.cryptowallet.data.repository.CoinRepository
import com.antigravity.cryptowallet.data.wallet.TransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TokenDetailViewModel @Inject constructor(
    private val coinRepository: CoinRepository,
    private val transactionRepository: TransactionRepository,
    private val walletRepository: com.antigravity.cryptowallet.data.wallet.WalletRepository,
    private val tokenDao: com.antigravity.cryptowallet.data.db.TokenDao,
    private val blockchainService: com.antigravity.cryptowallet.data.blockchain.BlockchainService,
    private val networkRepository: com.antigravity.cryptowallet.data.blockchain.NetworkRepository
) : ViewModel() {

    // ... (keep properties)
    
    var balance by mutableStateOf("0.0")
        private set
    
    // ...

    fun loadTokenData(symbol: String) {
        currentSymbol = symbol
        
        viewModelScope.launch {
            // Find token and network info to fetch balance
            val tokenEntity = tokenDao.getTokenBySymbol(symbol)
            val netId = tokenEntity?.chainId ?: when(symbol.uppercase()) {
                "BNB" -> "bsc"
                "MATIC", "POL" -> "matic"
                "ETH" -> "eth"
                else -> "eth"
            }
            val network = networkRepository.getNetwork(netId)
            
            // Trigger Transaction Refresh
            val address = walletRepository.getAddress()
            try {
                 transactionRepository.refreshTransactions(address, network)
            } catch (e: Exception) {
                e.printStackTrace()
            }
            
            // Get Balance
            try {
                val rawBalance = if (tokenEntity?.contractAddress != null) {
                     blockchainService.getTokenBalance(network.rpcUrl, tokenEntity.contractAddress, address)
                } else {
                     blockchainService.getBalance(network.rpcUrl, address)
                }
                
                val decimals = tokenEntity?.decimals ?: 18
                val ethBalance = java.math.BigDecimal(rawBalance).divide(java.math.BigDecimal.TEN.pow(decimals))
                balance = String.format("%.4f %s", ethBalance, symbol)
            } catch (e: Exception) {
                balance = "Error"
            }
        }
        
        // ... (rest of filtering and price logic)

    val address: String
        get() = walletRepository.getAddress()

    var description by mutableStateOf("Loading...")
        private set
    
    var price by mutableStateOf("Loading...")
        private set

    var contractAddress by mutableStateOf("")
        private set

    var graphPoints by mutableStateOf<List<Double>>(emptyList())
        private set

    var transactions by mutableStateOf<List<com.antigravity.cryptowallet.data.db.TransactionEntity>>(emptyList())
        private set
    
    private var currentSymbol: String = ""

    fun loadTokenData(symbol: String) {
        currentSymbol = symbol
        // Observe transactions locally filtered by symbol
        viewModelScope.launch {
            transactionRepository.transactions.collect { allTxs ->
                transactions = allTxs.filter { it.symbol.equals(symbol, ignoreCase = true) }
            }
        }
        
        viewModelScope.launch {
            try {
                // Resolve Coingecko ID dynamically
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
                    else -> "ethereum" // Fallback
                }
                
                // Fetch Info
                val info = coinRepository.getCoinInfo(id)
                // ...
                // Filter out HTML tags from description if present
                val rawDescription = info.description.en
                description = rawDescription.replace(Regex("<.*?>"), "") 
                    .take(300) + (if (rawDescription.length > 300) "..." else "")

                // Extract Contract Address
                // Prioritize finding one that matches the chain if possible, but for now just pick the first one
                // or "Native" if none found (which is true for ETH/BNB mainnet coins usually)
                 val rawAddress = info.platforms?.entries?.firstOrNull()?.value
                 contractAddress = if (!rawAddress.isNullOrEmpty()) rawAddress else "Native Token"


                // Fetch Chart
                val chart = coinRepository.getMarketChart(id)
                graphPoints = chart.prices.map { it[1] }

                
                // Fetch Price (use last point for now or specialized call)
                val currentPrice = graphPoints.lastOrNull() ?: 0.0
                price = String.format("$%.2f", currentPrice)

            } catch (e: Exception) {
                e.printStackTrace()
                description = "Failed to load info."
                price = "Error"
            }
        }
    }
}
