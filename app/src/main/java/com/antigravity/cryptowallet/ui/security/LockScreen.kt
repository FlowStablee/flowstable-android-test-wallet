package com.antigravity.cryptowallet.ui.security

import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Backspace
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import android.content.Context
import com.antigravity.cryptowallet.ui.theme.BrutalBlack
import com.antigravity.cryptowallet.ui.theme.BrutalWhite

enum class LockMode {
    SETUP,
    UNLOCK
}

@Composable
fun LockScreen(
    mode: LockMode,
    onPinSet: (String) -> Unit = {},
    onUnlock: () -> Unit,
    checkPin: (String) -> Boolean = { false },
    biometricEnabled: Boolean = false
) {
    var pin by remember { mutableStateOf("") }
    var confirmPin by remember { mutableStateOf("") }
    var step by remember { mutableStateOf(if (mode == LockMode.SETUP) 0 else 1) } // 0: enter, 1: confirm (setup only)
    var error by remember { mutableStateOf<String?>(null) }

    val context = LocalContext.current
    
    // Helper to find FragmentActivity
    fun Context.findFragmentActivity(): FragmentActivity? {
        var ctx = this
        while (ctx is android.content.ContextWrapper) {
            if (ctx is FragmentActivity) return ctx
            ctx = ctx.baseContext
        }
        return null
    }

    // Biometric Trigger
    fun showBiometricPrompt() {
        if (!biometricEnabled) return
        
        val fragmentActivity = context.findFragmentActivity()
        if (fragmentActivity != null) {
            val biometricManager = BiometricManager.from(context)
            if (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG) == BiometricManager.BIOMETRIC_SUCCESS) {
                val executor = ContextCompat.getMainExecutor(context)
                val biometricPrompt = BiometricPrompt(fragmentActivity, executor,
                    object : BiometricPrompt.AuthenticationCallback() {
                        override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                            super.onAuthenticationSucceeded(result)
                            onUnlock()
                        }
                    })

                val promptInfo = BiometricPrompt.PromptInfo.Builder()
                    .setTitle("Unlock Wallet")
                    .setSubtitle("Use biometric credential")
                    .setNegativeButtonText("Use PIN")
                    .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG)
                    .build()

                biometricPrompt.authenticate(promptInfo)
            }
        }
    }

    LaunchedEffect(Unit) {
        if (mode == LockMode.UNLOCK && biometricEnabled) {
            showBiometricPrompt()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BrutalWhite)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        
        Text(
            text = when {
                mode == LockMode.UNLOCK -> "Enter PIN"
                step == 0 -> "Set PIN"
                else -> "Confirm PIN"
            },
            color = BrutalBlack,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Dots
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            repeat(4) { index ->
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .background(if (pin.length > index) BrutalBlack else Color.Transparent)
                        .border(2.dp, BrutalBlack, CircleShape)
                )
            }
        }
        
        if (error != null) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(error!!, color = Color.Red, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(48.dp))

        // Numpad
        val buttons = listOf(
            "1", "2", "3",
            "4", "5", "6",
            "7", "8", "9",
            "Bio", "0", "Back"
        )
        
        Column(
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            buttons.chunked(3).forEach { row ->
                Row(
                    horizontalArrangement = Arrangement.spacedBy(32.dp)
                ) {
                    row.forEach { label ->
                        Box(
                            modifier = Modifier
                                .size(80.dp)
                                .clip(CircleShape)
                                .border(1.dp, if (label == "Bio" && !biometricEnabled) Color.LightGray else BrutalBlack, CircleShape)
                                .clickable {
                                    error = null
                                    when (label) {
                                        "Back" -> if (pin.isNotEmpty()) pin = pin.dropLast(1)
                                        "Bio" -> { 
                                            if (biometricEnabled) showBiometricPrompt()
                                        }
                                        else -> {
                                            if (pin.length < 4) {
                                                pin += label
                                                if (pin.length == 4) {
                                                    if (mode == LockMode.UNLOCK) {
                                                        if (checkPin(pin)) {
                                                            onUnlock()
                                                        } else {
                                                            error = "Incorrect PIN"
                                                            pin = ""
                                                        }
                                                    } else {
                                                        // Setup Mode
                                                        if (step == 0) {
                                                            confirmPin = pin
                                                            pin = ""
                                                            step = 1
                                                        } else {
                                                            if (pin == confirmPin) {
                                                                onPinSet(pin)
                                                            } else {
                                                                error = "PINs do not match"
                                                                pin = ""
                                                                step = 0
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                             if (label == "Back") {
                                 Icon(Icons.Default.Backspace, contentDescription = "Back", tint = BrutalBlack)
                             } else if (label == "Bio") {
                                 if (biometricEnabled) {
                                    Icon(Icons.Default.Fingerprint, contentDescription = "Biometric", tint = BrutalBlack)
                                 }
                             } else {
                                 Text(label, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = BrutalBlack)
                             }
                        }
                    }
                }
            }
        }
    }
}
