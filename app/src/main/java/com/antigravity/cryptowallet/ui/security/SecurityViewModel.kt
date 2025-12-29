package com.antigravity.cryptowallet.ui.security

import androidx.lifecycle.ViewModel
import com.antigravity.cryptowallet.data.security.SecureStorage
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SecurityViewModel @Inject constructor(
    private val secureStorage: SecureStorage,
    private val walletRepository: com.antigravity.cryptowallet.data.wallet.WalletRepository
) : ViewModel() {

    fun setPin(pin: String) {
        secureStorage.savePin(pin)
        secureStorage.setBiometricEnabled(true) // Default to true on setup
    }

    fun checkPin(inputPin: String): Boolean {
        return inputPin == secureStorage.getPin()
    }

    fun isBiometricEnabled(): Boolean {
        return secureStorage.isBiometricEnabled()
    }

    fun getMnemonic(): String {
        return secureStorage.getMnemonic() ?: ""
    }

    fun getPrivateKey(): String {
        return walletRepository.getPrivateKey()
    }

    fun hasMnemonic(): Boolean {
        return walletRepository.hasMnemonic()
    }
}
