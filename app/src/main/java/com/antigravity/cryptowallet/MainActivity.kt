package com.antigravity.cryptowallet

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.antigravity.cryptowallet.ui.WalletApp
import com.antigravity.cryptowallet.ui.theme.CryptoWalletTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var secureStorage: com.antigravity.cryptowallet.data.security.SecureStorage

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        

        
        val startDestination = if (secureStorage.hasWallet()) "home" else "intro"

        setContent {
            CryptoWalletTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = androidx.compose.material3.MaterialTheme.colorScheme.background
                ) {
                    WalletApp(startDestination = startDestination)
                }
            }
        }
    }
}
