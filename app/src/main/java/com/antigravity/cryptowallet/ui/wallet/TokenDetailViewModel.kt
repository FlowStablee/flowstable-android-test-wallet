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
                val ethBalance = BigDecimal(rawBalance).divide(BigDecimal.TEN.pow(decimals))
                balance = String.format("%.4f %s", ethBalance, symbol)
            } catch (e: Exception) {
                e.printStackTrace()
                balance = "Error"
            }
        }

        // 3. Fetch Coin Info and Price from CoinGecko
        viewModelScope.launch {
            try {
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
                    else -> "ethereum"
                }
                
                // Fetch Info
                val info = coinRepository.getCoinInfo(id)
                val rawDescription = info.description.en
                description = rawDescription.replace(Regex("<.*?>"), "") 
                    .take(300) + (if (rawDescription.length > 300) "..." else "")

                // Extract Contract Address
                val rawAddr = info.platforms?.entries?.firstOrNull()?.value
                contractAddress = if (!rawAddr.isNullOrEmpty()) rawAddr else "Native Token"

                // Fetch Chart
                val chart = coinRepository.getMarketChart(id)
                graphPoints = chart.prices.map { it[1] }
                
                // Fetch Price
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
