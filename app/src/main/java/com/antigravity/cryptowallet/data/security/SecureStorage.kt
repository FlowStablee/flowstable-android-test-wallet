package com.antigravity.cryptowallet.data.security

import android.content.SharedPreferences
import com.antigravity.cryptowallet.data.models.WalletInfo
import com.antigravity.cryptowallet.data.models.WalletType
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SecureStorage @Inject constructor(
    private val encryptedPrefs: SharedPreferences
) {
    private val gson = Gson()

    // Multi-wallet support
    fun getWallets(): List<WalletInfo> {
        val json = encryptedPrefs.getString(KEY_WALLETS, null) ?: return emptyList()
        return try {
            val type = object : TypeToken<List<WalletInfo>>() {}.type
            gson.fromJson(json, type)
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun saveWallets(wallets: List<WalletInfo>) {
        encryptedPrefs.edit()
            .putString(KEY_WALLETS, gson.toJson(wallets))
            .apply()
    }

    fun addWallet(name: String, address: String, type: WalletType): String {
        val wallets = getWallets().toMutableList()
        val id = "wallet_${System.currentTimeMillis()}"
        
        // Deactivate other wallets
        val updatedWallets = wallets.map { it.copy(isActive = false) }.toMutableList()
        
        // Add new wallet as active
        updatedWallets.add(WalletInfo(
            id = id,
            name = name,
            address = address,
            type = type,
            isActive = true
        ))
        
        saveWallets(updatedWallets)
        return id
    }

    fun setActiveWallet(walletId: String) {
        val wallets = getWallets().map { 
            it.copy(isActive = it.id == walletId) 
        }
        saveWallets(wallets)
        encryptedPrefs.edit().putString(KEY_ACTIVE_WALLET, walletId).apply()
    }

    fun getActiveWalletId(): String? {
        return encryptedPrefs.getString(KEY_ACTIVE_WALLET, null)
    }

    fun getActiveWallet(): WalletInfo? {
        return getWallets().find { it.isActive }
    }

    fun deleteWallet(walletId: String) {
        val wallets = getWallets().toMutableList()
        val walletToDelete = wallets.find { it.id == walletId }
        wallets.removeAll { it.id == walletId }
        
        // Delete associated keys
        if (walletToDelete != null) {
            encryptedPrefs.edit()
                .remove("${KEY_MNEMONIC}_$walletId")
                .remove("${KEY_PRIVATE_KEY}_$walletId")
                .apply()
        }
        
        // If deleted wallet was active, activate another one
        if (walletToDelete?.isActive == true && wallets.isNotEmpty()) {
            wallets[0] = wallets[0].copy(isActive = true)
        }
        
        saveWallets(wallets)
    }

    fun renameWallet(walletId: String, newName: String) {
        val wallets = getWallets().map {
            if (it.id == walletId) it.copy(name = newName) else it
        }
        saveWallets(wallets)
    }

    // Store mnemonic per wallet
    fun saveMnemonicForWallet(walletId: String, mnemonic: String) {
        encryptedPrefs.edit()
            .putString("${KEY_MNEMONIC}_$walletId", mnemonic)
            .apply()
    }

    fun getMnemonicForWallet(walletId: String): String? {
        return encryptedPrefs.getString("${KEY_MNEMONIC}_$walletId", null)
    }

    // Store private key per wallet
    fun savePrivateKeyForWallet(walletId: String, privateKey: String) {
        encryptedPrefs.edit()
            .putString("${KEY_PRIVATE_KEY}_$walletId", privateKey)
            .apply()
    }

    fun getPrivateKeyForWallet(walletId: String): String? {
        return encryptedPrefs.getString("${KEY_PRIVATE_KEY}_$walletId", null)
    }

    // Legacy single wallet support (for migration)
    fun saveMnemonic(mnemonic: String) {
        encryptedPrefs.edit()
            .putString(KEY_MNEMONIC, mnemonic)
            .putBoolean(KEY_WALLET_CREATED, true)
            .apply()
    }

    fun getMnemonic(): String? {
        return encryptedPrefs.getString(KEY_MNEMONIC, null)
    }

    fun hasWallet(): Boolean {
        return encryptedPrefs.getBoolean(KEY_WALLET_CREATED, false) || getWallets().isNotEmpty()
    }

    fun clearWallet() {
        encryptedPrefs.edit().clear().apply()
    }

    // Security Features
    fun savePin(pin: String) {
        encryptedPrefs.edit().putString(KEY_PIN, pin).apply()
    }

    fun getPin(): String? {
        return encryptedPrefs.getString(KEY_PIN, null)
    }

    fun hasPin(): Boolean {
        return encryptedPrefs.contains(KEY_PIN)
    }

    fun setBiometricEnabled(enabled: Boolean) {
        encryptedPrefs.edit().putBoolean(KEY_BIOMETRIC_ENABLED, enabled).apply()
    }

    fun isBiometricEnabled(): Boolean {
        return encryptedPrefs.getBoolean(KEY_BIOMETRIC_ENABLED, false)
    }

    fun savePrivateKey(privateKey: String) {
        encryptedPrefs.edit()
            .putString(KEY_PRIVATE_KEY, privateKey)
            .putBoolean(KEY_WALLET_CREATED, true)
            .apply()
    }

    fun getPrivateKey(): String? {
        return encryptedPrefs.getString(KEY_PRIVATE_KEY, null)
    }

    companion object {
        private const val KEY_MNEMONIC = "wallet_mnemonic"
        private const val KEY_WALLET_CREATED = "wallet_created"
        private const val KEY_PIN = "wallet_pin"
        private const val KEY_BIOMETRIC_ENABLED = "biometric_enabled"
        private const val KEY_PRIVATE_KEY = "wallet_private_key"
        private const val KEY_WALLETS = "multi_wallets"
        private const val KEY_ACTIVE_WALLET = "active_wallet_id"
    }
}
