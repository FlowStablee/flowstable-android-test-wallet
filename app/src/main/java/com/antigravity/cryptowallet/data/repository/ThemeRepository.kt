package com.antigravity.cryptowallet.data.repository

import android.content.SharedPreferences
import com.antigravity.cryptowallet.ui.theme.ThemeType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ThemeRepository @Inject constructor(
    private val sharedPreferences: SharedPreferences
) {
    private val _currentTheme = MutableStateFlow(getSavedTheme())
    val currentTheme = _currentTheme.asStateFlow()

    fun setTheme(theme: ThemeType) {
        sharedPreferences.edit().putString("app_theme", theme.name).apply()
        _currentTheme.value = theme
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
