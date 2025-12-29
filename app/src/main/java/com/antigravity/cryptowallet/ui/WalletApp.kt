package com.antigravity.cryptowallet.ui

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.antigravity.cryptowallet.ui.onboarding.IntroScreen
import com.antigravity.cryptowallet.ui.wallet.WalletScreen

@Composable
fun WalletApp(startDestination: String = "intro") {
    val navController = rememberNavController()
    
    NavHost(navController = navController, startDestination = startDestination) {
        composable("intro") {
            IntroScreen(
                onCreateWallet = { navController.navigate("create_wallet") },
                onImportWallet = { navController.navigate("import_wallet") }
            )
        }
        composable("create_wallet") {
            com.antigravity.cryptowallet.ui.onboarding.CreateWalletScreen(
                onWalletCreated = {
                    navController.navigate("home") {
                        popUpTo("intro") { inclusive = true }
                    }
                }
            )
        }
        composable("import_wallet") {
            com.antigravity.cryptowallet.ui.onboarding.ImportWalletScreen(
                onWalletImported = {
                    navController.navigate("home") {
                        popUpTo("intro") { inclusive = true }
                    }
                }
            )
        }
        composable("home") {
            MainScreen(
                onNavigateToSecuritySetup = { navController.navigate("security_setup") },
                onNavigateToRevealSeed = { navController.navigate("reveal_seed_verify") },
                onNavigateToTransfer = { navController.navigate("transfer") },
                onNavigateToAppInfo = { navController.navigate("app_info") },
                onNavigateToTokenDetail = { symbol -> navController.navigate("token_detail/$symbol") }
            )
        }

        composable("transfer") {
            com.antigravity.cryptowallet.ui.wallet.TransferScreen(
                onBack = { navController.popBackStack() },
                onTransactionSuccess = { amount, symbol, recipient ->
                    navController.navigate("transaction_success/$amount/$symbol/$recipient")
                }
            )
        }

        composable("app_info") {
            com.antigravity.cryptowallet.ui.settings.AppInfoScreen(
                onBack = { navController.popBackStack() }
            )
        }
        
        composable("unlock") {
            val viewModel = androidx.hilt.navigation.compose.hiltViewModel<com.antigravity.cryptowallet.ui.security.SecurityViewModel>()
            com.antigravity.cryptowallet.ui.security.LockScreen(
                mode = com.antigravity.cryptowallet.ui.security.LockMode.UNLOCK,
                onUnlock = {
                    navController.navigate("home") {
                        popUpTo("unlock") { inclusive = true }
                    }
                },
                checkPin = { viewModel.checkPin(it) },
                biometricEnabled = viewModel.isBiometricEnabled()
            )
        }

        composable("reveal_seed_verify") {
            val viewModel = androidx.hilt.navigation.compose.hiltViewModel<com.antigravity.cryptowallet.ui.security.SecurityViewModel>()
            com.antigravity.cryptowallet.ui.security.LockScreen(
                mode = com.antigravity.cryptowallet.ui.security.LockMode.UNLOCK,
                onUnlock = {
                    navController.navigate("show_seed") {
                        popUpTo("reveal_seed_verify") { inclusive = true }
                    }
                },
                checkPin = { viewModel.checkPin(it) },
                biometricEnabled = viewModel.isBiometricEnabled()
            )
        }

        composable("show_seed") {
            val viewModel = androidx.hilt.navigation.compose.hiltViewModel<com.antigravity.cryptowallet.ui.security.SecurityViewModel>()
            com.antigravity.cryptowallet.ui.security.ShowSeedScreen(
                mnemonic = viewModel.getMnemonic(),
                onBack = { navController.popBackStack() }
            )
        }

        composable("security_setup") {
            val viewModel = androidx.hilt.navigation.compose.hiltViewModel<com.antigravity.cryptowallet.ui.security.SecurityViewModel>()
            com.antigravity.cryptowallet.ui.security.LockScreen(
                mode = com.antigravity.cryptowallet.ui.security.LockMode.SETUP,
                onPinSet = { pin ->
                    viewModel.setPin(pin)
                    navController.popBackStack()
                },
                onUnlock = {}, // Not used in setup
                biometricEnabled = false
            )
        }

        composable("unlock") {
            val viewModel = androidx.hilt.navigation.compose.hiltViewModel<com.antigravity.cryptowallet.ui.security.SecurityViewModel>()
            com.antigravity.cryptowallet.ui.security.LockScreen(
                mode = com.antigravity.cryptowallet.ui.security.LockMode.UNLOCK,
                onPinSet = {},
                onUnlock = {
                    navController.navigate("home") {
                        popUpTo("unlock") { inclusive = true }
                    }
                },
                biometricEnabled = viewModel.isBiometricEnabled()
            )
        }
        composable(
            route = "transaction_success/{amount}/{symbol}/{recipient}",
            arguments = listOf(
                androidx.navigation.navArgument("amount") { type = androidx.navigation.NavType.StringType },
                androidx.navigation.navArgument("symbol") { type = androidx.navigation.NavType.StringType },
                androidx.navigation.navArgument("recipient") { type = androidx.navigation.NavType.StringType }
            )
        ) { backStackEntry ->
            val amount = backStackEntry.arguments?.getString("amount") ?: "0"
            val symbol = backStackEntry.arguments?.getString("symbol") ?: ""
            val recipient = backStackEntry.arguments?.getString("recipient") ?: ""
            
            com.antigravity.cryptowallet.ui.wallet.TransactionSuccessScreen(
                amount = amount,
                symbol = symbol,
                recipient = recipient,
                onDone = {
                    navController.navigate("home") {
                        popUpTo("home") { inclusive = true }
                    }
                }
            )
        composable(
            route = "token_detail/{symbol}",
            arguments = listOf(
                androidx.navigation.navArgument("symbol") { type = androidx.navigation.NavType.StringType }
            )
        ) { backStackEntry ->
            val symbol = backStackEntry.arguments?.getString("symbol") ?: ""
            com.antigravity.cryptowallet.ui.wallet.TokenDetailScreen(
                symbol = symbol,
                onBack = { navController.popBackStack() }
            )
        }
    }
}
