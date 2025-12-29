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
    private val sharedPreferences: android.content.SharedPreferences
) : ViewModel() {
    var currentTheme by mutableStateOf(getSavedTheme())
        private set

    fun setTheme(theme: ThemeType) {
        currentTheme = theme
        sharedPreferences.edit().putString("app_theme", theme.name).apply()
    }

    private fun getSavedTheme(): ThemeType {
        val themeName = sharedPreferences.getString("app_theme", ThemeType.DEFAULT.name)
        return try {
            ThemeType.valueOf(themeName ?: ThemeType.DEFAULT.name)
        } catch (e: Exception) {
            ThemeType.DEFAULT
        }
    }
}
