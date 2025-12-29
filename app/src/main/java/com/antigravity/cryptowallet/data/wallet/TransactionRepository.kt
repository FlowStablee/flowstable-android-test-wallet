package com.antigravity.cryptowallet.data.wallet

import com.antigravity.cryptowallet.data.api.ExplorerApi
import com.antigravity.cryptowallet.data.blockchain.Network
import com.antigravity.cryptowallet.data.db.TransactionDao
import com.antigravity.cryptowallet.data.db.TransactionEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.math.BigDecimal
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TransactionRepository @Inject constructor(
    private val transactionDao: TransactionDao,
    private val explorerApi: ExplorerApi
) {
    val transactions: Flow<List<TransactionEntity>> = transactionDao.getAllTransactions()

    suspend fun refreshTransactions(address: String, network: Network) = withContext(Dispatchers.IO) {
        try {
            // Mapping network IDs to their respective explorer API URLs should ideally be in NetworkRepository
            // For now, let's use a simplified approach since we don't have multiple API bases yet.
            // Assumption: The Retrofit instance for explorerApi is configured for the active network.
            
            val response = explorerApi.getTransactionList(address = address)
            if (response.status == "1") {
                val entities = response.result.map { tx ->
                    val valueEth = BigDecimal(tx.value).divide(BigDecimal.TEN.pow(18)).toPlainString()
                    val type = if (tx.from.lowercase() == address.lowercase()) "send" else "receive"
                    TransactionEntity(
                        hash = tx.hash,
                        fromAddress = tx.from,
                        toAddress = tx.to,
                        value = valueEth,
                        symbol = network.symbol,
                        timestamp = tx.timeStamp.toLong() * 1000,
                        type = type,
                        status = "success",
                        network = network.name
                    )
                }
                transactionDao.insertTransactions(entities)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun addTransaction(
        hash: String,
        from: String,
        to: String,
        value: String,
        symbol: String,
        type: String,
        status: String,
        network: String
    ) {
        val transaction = TransactionEntity(
            hash = hash,
            fromAddress = from,
            toAddress = to,
            value = value,
            symbol = symbol,
            timestamp = System.currentTimeMillis(),
            type = type,
            status = status,
            network = network
        )
        transactionDao.insertTransaction(transaction)
    }
}
