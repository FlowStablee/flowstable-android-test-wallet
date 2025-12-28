package com.antigravity.cryptowallet.ui.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.antigravity.cryptowallet.data.wallet.TransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val repository: TransactionRepository
) : ViewModel() {

    val transactions = repository.transactions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        // Add a demo receive transaction if we don't have real data yet (optional, for demo)
        viewModelScope.launch {
            // In a real app, we'd fetch from an indexer here.
            // For now, let's just make sure the UI works.
            // repository.addTransaction("0x123...", "0xSomeone", "0xMe", "1.5", "ETH", "receive", "success", "eth")
        }
    }
}
