package com.antigravity.cryptowallet.ui.settings

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.antigravity.cryptowallet.ui.theme.ThemeType
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val themeRepository: com.antigravity.cryptowallet.data.repository.ThemeRepository,
    private val walletRepository: com.antigravity.cryptowallet.data.wallet.WalletRepository
) : ViewModel() {
    
    val currentTheme = themeRepository.currentTheme

    fun setTheme(theme: ThemeType) {
        themeRepository.setTheme(theme)
    }

    fun hasMnemonic(): Boolean = walletRepository.hasMnemonic()
}
