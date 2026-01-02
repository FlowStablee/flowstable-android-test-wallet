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

    suspend fun getBalance(rpcUrl: String, address: String): BigInteger = withContext(Dispatchers.IO) {
        try {
            val web3j = Web3j.build(HttpService(rpcUrl))
            val ethGetBalance = web3j.ethGetBalance(address, DefaultBlockParameterName.LATEST).send()
            ethGetBalance.balance
        } catch (e: Exception) {
            e.printStackTrace()
            BigInteger.ZERO
        }
    }

    suspend fun sendEth(rpcUrl: String, credentials: Credentials, toAddress: String, amountWei: BigInteger): String = withContext(Dispatchers.IO) {
        try {
            val web3j = Web3j.build(HttpService(rpcUrl))
            val ethGetTransactionCount = web3j.ethGetTransactionCount(credentials.address, DefaultBlockParameterName.LATEST).send()
            val nonce = ethGetTransactionCount.transactionCount

            val gasPrice = web3j.ethGasPrice().send().gasPrice
            val gasLimit = BigInteger.valueOf(21000)

            val rawTransaction = RawTransaction.createEtherTransaction(
                nonce, gasPrice, gasLimit, toAddress, amountWei
            )

            val signedMessage = TransactionEncoder.signMessage(rawTransaction, credentials)
            val hexValue = Numeric.toHexString(signedMessage)

            val ethSendTransaction = web3j.ethSendRawTransaction(hexValue).send()
            ethSendTransaction.transactionHash ?: throw Exception(ethSendTransaction.error?.message ?: "Unknown error")
        } catch (e: Exception) {
            e.printStackTrace()
            throw e
        }
    }

    suspend fun getTokenBalance(rpcUrl: String, tokenAddress: String, walletAddress: String): BigInteger = withContext(Dispatchers.IO) {
        try {
            val web3j = Web3j.build(HttpService(rpcUrl))
            val functionCode = "0x70a08231" // balanceOf(address)
            val paddedAddress = "000000000000000000000000" + walletAddress.removePrefix("0x")
            val data = functionCode + paddedAddress
            
            val ethCall = web3j.ethCall(
                org.web3j.protocol.core.methods.request.Transaction.createEthCallTransaction(walletAddress, tokenAddress, data),
                DefaultBlockParameterName.LATEST
            ).send()
            
            if (ethCall.value == "0x" || ethCall.value == null) return@withContext BigInteger.ZERO
            
            Numeric.toBigInt(ethCall.value)
        } catch (e: Exception) {
            e.printStackTrace()
            BigInteger.ZERO
        }
    }

    suspend fun sendToken(rpcUrl: String, credentials: Credentials, tokenAddress: String, toAddress: String, amount: BigInteger): String = withContext(Dispatchers.IO) {
        try {
            val web3j = Web3j.build(HttpService(rpcUrl))
            val ethGetTransactionCount = web3j.ethGetTransactionCount(credentials.address, DefaultBlockParameterName.LATEST).send()
            val nonce = ethGetTransactionCount.transactionCount

            val gasPrice = web3j.ethGasPrice().send().gasPrice
            val gasLimit = BigInteger.valueOf(100000)

            val functionCode = "0xa9059cbb" // transfer(address,uint256)
            val paddedTo = toAddress.removePrefix("0x").padStart(64, '0')
            val paddedAmount = amount.toString(16).padStart(64, '0')
            val data = functionCode + paddedTo + paddedAmount

            val rawTransaction = RawTransaction.createTransaction(
                nonce, gasPrice, gasLimit, tokenAddress, data
            )

            val signedMessage = TransactionEncoder.signMessage(rawTransaction, credentials)
            val hexValue = Numeric.toHexString(signedMessage)

            val ethSendTransaction = web3j.ethSendRawTransaction(hexValue).send()
            ethSendTransaction.transactionHash ?: throw Exception(ethSendTransaction.error?.message ?: "Unknown error")
        } catch (e: Exception) {
            e.printStackTrace()
            throw e
        }
    }
}
