package com.antigravity.cryptowallet.ui.card

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.antigravity.cryptowallet.data.wallet.AssetRepository
import com.antigravity.cryptowallet.data.wallet.WalletRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CardViewModel @Inject constructor(
    private val walletRepository: WalletRepository,
    private val assetRepository: AssetRepository
) : ViewModel() {
    
    var isCardFrozen by mutableStateOf(false)
    var cardNumber by mutableStateOf("**** **** **** 8829")
    var cardExpiry by mutableStateOf("12/28")
    var cardCvv by mutableStateOf("***")
    var cardHolderName by mutableStateOf("BRUTALIST USER")
    
    var spendingLimitUsd by mutableStateOf("$0.00")
    var showSensitiveData by mutableStateOf(false)

    init {
        observeAssets()
    }

    private fun observeAssets() {
        viewModelScope.launch {
            assetRepository.assets.collectLatest { assets ->
                // Calculate spending limit based on USDC/USDT balances (simulated)
                val stableBalance = assets.filter { 
                    it.symbol.contains("USD") 
                }.sumOf { it.rawBalance * it.price }
                
                spendingLimitUsd = String.format("$%.2f", stableBalance)
            }
        }
    }

    fun toggleFreeze() {
        isCardFrozen = !isCardFrozen
    }

    fun toggleSensitiveData() {
        showSensitiveData = !showSensitiveData
        if (showSensitiveData) {
            cardNumber = "4532 1902 8374 8829"
            cardCvv = "729"
        } else {
            cardNumber = "**** **** **** 8829"
            cardCvv = "***"
        }
    }
}
