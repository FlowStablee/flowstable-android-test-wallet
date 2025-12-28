# 🪐 FlowStable Wallet

[![Android Build](https://github.com/FlowStablee/flowstable-android-test-wallet/actions/workflows/android.yml/badge.svg)](https://github.com/FlowStablee/flowstable-android-test-wallet/actions)
[![License: MIT](https://img.shields.io/badge/License-MIT-black.svg)](https://opensource.org/licenses/MIT)
[![Kotlin](https://img.shields.io/badge/Kotlin-1.9.0-purple.svg)](https://kotlinlang.org/)
[![Compose](https://img.shields.io/badge/Jetpack-Compose-green.svg)](https://developer.android.com/jetpack/compose)

**Antigravity** is a high-performance, non-custodial EVM wallet for Android. Built with a raw, **Brutalist Design Aesthetic**, it prioritizes speed, security, and a uncompromising user experience.

---

## 🛠 Features

### 💎 Wallet Management
- **Non-Custodial**: Your keys, your crypto. Generated via BIP-39 mnemonic phrases.
- **Multi-Chain Support**: Ethereum, Polygon, BSC, Arbitrum, Optimism, and more.
- **Asset Tracking**: Real-time balance updates and price tracking via CoinGecko.
- **Custom Tokens**: Add any ERC-20 token by contract address.
- **QR Support**: Integrated QR code generation for effortless receiving.

### 🌐 Web3 Browser (DApp Gateway)
- **EIP-1193 Injection**: Full support for connecting to dApps (PancakeSwap, Uniswap, etc.).
- **Secure Signatures**: In-app confirmation dialogs for `personal_sign` and `eth_sendTransaction`.
- **Advanced Compatibility**: Optimized WebView with DOM storage and database support.

### 📜 Transaction History
- **Local Persistence**: Full history stored locally via Room DB for instant access.
- **Brutalist UI**: Clear, high-contrast list showing transaction status, amounts, and timestamps.

### 🛡 Security First
- **Biometric Authentication**: Fingerprint and Face Unlock support.
- **PIN Protection**: Custom secure Numpad for app access.
- **AES Encryption**: Seed phrases and private keys encrypted using Android Keystore (TEE-backed).
- **Advanced Privacy**: Anti-screenshot protection and clipboard security.

---

## 🚀 Tech Stack

| Category | Technology |
| :--- | :--- |
| **Language** | Kotlin |
| **UI Framework** | Jetpack Compose (100%) |
| **Blockchain** | Web3j |
| **Architecture** | MVVM + Clean Architecture |
| **DI** | Hilt |
| **Database** | Room |
| **Networking** | Retrofit + OkHttp |
| **Security** | Android Keystore / Biometrics |

---

## 📦 Build Instructions

### Prerequisites
- Android Studio Hedgehog (2023.1.1) or later.
- JDK 17.
- Android SDK 34 (Target).

### Step-by-Step
1. **Clone the repository**
   ```bash
   git clone https://github.com/FlowStablee/flowstable-android-test-wallet.git
   ```
2. **Open in Android Studio**
   Wait for Gradle to sync.
3. **Build APK**
   - Click `Build > Build Bundle(s) / APK(s) > Build APK(s)`.
4. **Run**
   - Use an emulator (API 26+) or a physical device.

---

## 📐 Architecture Overview

The app follows **Modern Android Development (MAD)** practices:
- **UI Layer**: Composable screens observing StateFlows from ViewModels.
- **Domain Layer**: Repositories abstracting data sources (Blockchain, Local DB, API).
- **Data Layer**: Room DAOs, Retrofit Interfaces, and SecureStorage.

---

## 📜 License

Distributed under the MIT License. See `LICENSE` for more information.

---

<p align="center">
  <i>Built with 🖤 by FlowStable Labs </i>
</p>
