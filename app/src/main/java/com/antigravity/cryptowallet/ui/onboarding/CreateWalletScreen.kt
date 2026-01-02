package com.antigravity.cryptowallet.ui.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.antigravity.cryptowallet.data.wallet.WalletRepository
import com.antigravity.cryptowallet.ui.components.BrutalistButton
import com.antigravity.cryptowallet.ui.components.BrutalistHeader
import com.antigravity.cryptowallet.ui.theme.BrutalBlack
import com.antigravity.cryptowallet.ui.theme.BrutalWhite
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CreateWalletViewModel @Inject constructor(
    private val walletRepository: WalletRepository
) : ViewModel() {
    var mnemonic by mutableStateOf<List<String>>(emptyList())
    var isCreated by mutableStateOf(false)
    var step by mutableStateOf(CreateWalletStep.ShowPhrase)
    var verificationIndices by mutableStateOf<List<Int>>(emptyList())
    // We use a map for entered words: Index -> Word
    var enteredWords = mutableStateOf(mapOf<Int, String>())
    var verificationError by mutableStateOf<String?>(null)

    var isLoading by mutableStateOf(false)

    fun generateWallet() {
        if (mnemonic.isEmpty()) {
            viewModelScope.launch {
                isLoading = true
                val phrase = walletRepository.createWallet()
                mnemonic = phrase.split(" ")
                isLoading = false
            }
        }
    }

    fun startVerification() {
        verificationIndices = (0 until mnemonic.size).shuffled().take(3).sorted()
        step = CreateWalletStep.VerifyPhrase
    }

    fun updateEnteredWord(index: Int, word: String) {
        enteredWords.value = enteredWords.value.toMutableMap().apply {
            put(index, word)
        }
    }

    fun verifyAndComplete(): Boolean {
        for (index in verificationIndices) {
            val entered = enteredWords.value[index]?.trim() ?: ""
            if (entered != mnemonic[index]) {
                verificationError = "Word #${index + 1} is incorrect."
                return false
            }
        }
        isCreated = true
        return true
    }
}

enum class CreateWalletStep {
    ShowPhrase,
    VerifyPhrase
}

@Composable
fun CreateWalletScreen(
    onWalletCreated: () -> Unit,
    viewModel: CreateWalletViewModel = hiltViewModel()
) {
    LaunchedEffect(Unit) {
        viewModel.generateWallet()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BrutalWhite)
            .border(2.dp, BrutalBlack, RoundedCornerShape(12.dp))
            .clip(RoundedCornerShape(12.dp))
            .padding(12.dp),
    ) {
        if (viewModel.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                androidx.compose.material3.CircularProgressIndicator(color = BrutalBlack)
            }
        } else if (viewModel.step == CreateWalletStep.ShowPhrase) {
            BrutalistHeader("Secret Phrase")

            Text(
                text = "WRITE THIS DOWN. IF YOU LOSE IT, YOU LOSE YOUR FUNDS FOREVER.",
                color = BrutalBlack,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.weight(1f)
            ) {
                itemsIndexed(viewModel.mnemonic) { index, word ->
                    Box(
                        modifier = Modifier
                            .border(1.dp, BrutalBlack, RoundedCornerShape(8.dp))
                            .clip(RoundedCornerShape(8.dp))
                            .padding(12.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Row {
                            Text(
                                text = "${index + 1}. ",
                                fontWeight = FontWeight.Bold,
                                color = BrutalBlack
                            )
                            Text(
                                text = word,
                                color = BrutalBlack
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            BrutalistButton(
                text = "I Have Saved It",
                onClick = { viewModel.startVerification() }
            )
        } else {
            BrutalistHeader("Verify Phrase")
            
            Text(
                text = "Enter the requested words to verify you saved them.",
                color = BrutalBlack,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.weight(1f)
            ) {
                viewModel.verificationIndices.forEach { index ->
                    Column {
                        Text(
                            text = "Word #${index + 1}",
                            fontWeight = FontWeight.Bold,
                            color = BrutalBlack,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                        androidx.compose.material3.OutlinedTextField(
                            value = viewModel.enteredWords.value[index] ?: "",
                            onValueChange = { viewModel.updateEnteredWord(index, it) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = BrutalBlack,
                                unfocusedBorderColor = BrutalBlack,
                                focusedTextColor = BrutalBlack,
                                unfocusedTextColor = BrutalBlack,
                                cursorColor = BrutalBlack
                            )
                        )
                    }
                }
                
                if (viewModel.verificationError != null) {
                    Text(
                        text = viewModel.verificationError!!,
                        color = androidx.compose.ui.graphics.Color.Red,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))

            BrutalistButton(
                text = "Verify & Create",
                onClick = {
                    if (viewModel.verifyAndComplete()) {
                        onWalletCreated()
                    }
                }
            )
        }
    }
}
