package com.antigravity.cryptowallet.data.blockchain

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.web3j.protocol.Web3j
import org.web3j.protocol.core.DefaultBlockParameterName
import org.web3j.protocol.http.HttpService
import org.web3j.crypto.Credentials
import org.web3j.crypto.RawTransaction
import org.web3j.crypto.TransactionEncoder
import org.web3j.utils.Numeric
import java.math.BigDecimal
import java.math.BigInteger
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BlockchainService @Inject constructor() {

    // Gas buffers are now dynamic based on transaction type

    // 1. Reusable OkHttpClient with increased timeouts
    private val okHttpClient: okhttp3.OkHttpClient = okhttp3.OkHttpClient.Builder()
        .connectTimeout(45, java.util.concurrent.TimeUnit.SECONDS)
        .readTimeout(45, java.util.concurrent.TimeUnit.SECONDS)
        .writeTimeout(45, java.util.concurrent.TimeUnit.SECONDS)
        .retryOnConnectionFailure(true)
        .pingInterval(30, java.util.concurrent.TimeUnit.SECONDS) // Keep connections alive
        .build()

    // 2. Cache Web3j instances per RPC URL to avoid heavy object creation
    private val web3jCache = java.util.concurrent.ConcurrentHashMap<String, Web3j>()

    private fun getWeb3j(rpcUrl: String): Web3j {
        return web3jCache.computeIfAbsent(rpcUrl) { url ->
            // Pass the shared OkHttpClient to HttpService
            Web3j.build(HttpService(url, okHttpClient, false))
        }
    }

    suspend fun getBalance(rpcUrl: String, address: String): BigInteger = withContext(Dispatchers.IO) {
        try {
            // Guard with timeout to prevent infinite hanging
            kotlinx.coroutines.withTimeout(15_000L) {
                val web3j = getWeb3j(rpcUrl)
                val ethGetBalance = web3j.ethGetBalance(address, DefaultBlockParameterName.LATEST).send()
                ethGetBalance.balance
            }
        } catch (e: Exception) {
            e.printStackTrace()
            // Graceful degradation
            BigInteger.ZERO
        }
    }

    suspend fun sendEth(rpcUrl: String, credentials: Credentials, toAddress: String, amountWei: BigInteger): String = withContext(Dispatchers.IO) {
        try {
            // Longer timeout for transactions
            kotlinx.coroutines.withTimeout(60_000L) {
                val web3j = getWeb3j(rpcUrl)

                // 1. Get Nonce
                val ethGetTransactionCount = web3j.ethGetTransactionCount(credentials.address, DefaultBlockParameterName.LATEST).send()
                val nonce = ethGetTransactionCount.transactionCount

                // 2. Get Gas Price
                val gasPrice = web3j.ethGasPrice().send().gasPrice

                // 3. Estimate Gas
                // Check if recipient is a contract
                val ethGetCode = web3j.ethGetCode(toAddress, DefaultBlockParameterName.LATEST).send()
                val isContract = ethGetCode.code != "0x"

                val estimateTransaction = org.web3j.protocol.core.methods.request.Transaction.createEtherTransaction(
                    credentials.address,
                    nonce,
                    gasPrice,
                    null, // limit unknown yet
                    toAddress,
                    amountWei
                )
                
                val estimatedGas = try {
                    val result = web3j.ethEstimateGas(estimateTransaction).send()
                    if (result.hasError()) {
                        // Fallback for standard transfers if estimate fails (rare but possible on some nodes)
                         if (isContract) BigInteger.valueOf(100_000) else BigInteger.valueOf(21_000)
                    } else {
                        result.amountUsed
                    }
                } catch (e: Exception) {
                    if (isContract) BigInteger.valueOf(100_000) else BigInteger.valueOf(21_000)
                }

                // 4. Apply Buffer
                // ETH -> EOA: +10%, ETH -> Contract: +30%
                val buffer = if (isContract) 1.30 else 1.10
                val gasLimit = BigDecimal(estimatedGas).multiply(BigDecimal(buffer)).toBigInteger()

                // 5. Pre-flight Balance Check (Total Cost = Gas * Price + Value)
                val totalCost = (gasLimit * gasPrice) + amountWei
                val balance = web3j.ethGetBalance(credentials.address, DefaultBlockParameterName.LATEST).send().balance
                if (balance < totalCost) {
                    throw Exception("Insufficient balance to cover transfer + gas fees. Required: $totalCost, Available: $balance")
                }

                // 6. Sign & Send
                val rawTransaction = RawTransaction.createEtherTransaction(
                    nonce, gasPrice, gasLimit, toAddress, amountWei
                )

                val signedMessage = TransactionEncoder.signMessage(rawTransaction, credentials)
                val hexValue = Numeric.toHexString(signedMessage)

                val ethSendTransaction = web3j.ethSendRawTransaction(hexValue).send()
                ethSendTransaction.transactionHash ?: throw Exception(ethSendTransaction.error?.message ?: "Transaction failed without hash")
            }
        } catch (e: Exception) {
            e.printStackTrace()
            throw e
        }
    }

    suspend fun getTokenBalance(rpcUrl: String, tokenAddress: String, walletAddress: String): BigInteger = withContext(Dispatchers.IO) {
        try {
            kotlinx.coroutines.withTimeout(15_000L) {
                val web3j = getWeb3j(rpcUrl)
                val functionCode = "0x70a08231" // balanceOf(address)
                val paddedAddress = "000000000000000000000000" + walletAddress.removePrefix("0x")
                val data = functionCode + paddedAddress
                
                val ethCall = web3j.ethCall(
                    org.web3j.protocol.core.methods.request.Transaction.createEthCallTransaction(walletAddress, tokenAddress, data),
                    DefaultBlockParameterName.LATEST
                ).send()
                
                if (ethCall.value == "0x" || ethCall.value == null) return@withTimeout BigInteger.ZERO
                
                Numeric.toBigInt(ethCall.value)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            BigInteger.ZERO
        }
    }

    suspend fun sendToken(rpcUrl: String, credentials: Credentials, tokenAddress: String, toAddress: String, amount: BigInteger): String = withContext(Dispatchers.IO) {
        try {
            kotlinx.coroutines.withTimeout(60_000L) {
                val web3j = getWeb3j(rpcUrl)
                
                // 1. Get Nonce
                val ethGetTransactionCount = web3j.ethGetTransactionCount(credentials.address, DefaultBlockParameterName.LATEST).send()
                val nonce = ethGetTransactionCount.transactionCount

                // 2. Get Gas Price
                val gasPrice = web3j.ethGasPrice().send().gasPrice

                // 3. Prepare Data & Estimate
                val functionCode = "0xa9059cbb" // transfer(address,uint256)
                val paddedTo = toAddress.removePrefix("0x").padStart(64, '0')
                val paddedAmount = amount.toString(16).padStart(64, '0')
                val data = functionCode + paddedTo + paddedAmount

                val estimateTransaction = org.web3j.protocol.core.methods.request.Transaction.createFunctionCallTransaction(
                    credentials.address,
                    nonce,
                    gasPrice,
                    null,
                    tokenAddress,
                    data
                )

                val estimatedGas = try {
                     val result = web3j.ethEstimateGas(estimateTransaction).send()
                     if (result.hasError()) {
                         BigInteger.valueOf(100_000) // Fallback
                     } else {
                         result.amountUsed
                     }
                } catch (e: Exception) {
                    BigInteger.valueOf(100_000)
                }

                // 4. Apply Buffer
                // ERC-20 Transfers: +30%
                val buffer = 1.30
                val gasLimit = BigDecimal(estimatedGas).multiply(BigDecimal(buffer)).toBigInteger()

                 // 5. Check Gas Money (ETH Balance > Gas Limit * Gas Price)
                val gasCost = gasLimit * gasPrice
                val ethBalance = web3j.ethGetBalance(credentials.address, DefaultBlockParameterName.LATEST).send().balance
                if (ethBalance < gasCost) {
                    throw Exception("Insufficient ETH for gas fees. Required: $gasCost, Available: $ethBalance")
                }

                // 6. Sign & Send
                val rawTransaction = RawTransaction.createTransaction(
                    nonce, gasPrice, gasLimit, tokenAddress, data
                )

                val signedMessage = TransactionEncoder.signMessage(rawTransaction, credentials)
                val hexValue = Numeric.toHexString(signedMessage)

                val ethSendTransaction = web3j.ethSendRawTransaction(hexValue).send()
                ethSendTransaction.transactionHash ?: throw Exception(ethSendTransaction.error?.message ?: "Unknown error")
            }
        } catch (e: Exception) {
            e.printStackTrace()
            throw e
        }
    }
}
