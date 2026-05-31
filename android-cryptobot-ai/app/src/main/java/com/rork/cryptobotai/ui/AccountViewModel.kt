package com.rork.cryptobotai.ui

import android.content.Context
import android.content.SharedPreferences
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import com.rork.cryptobotai.data.BinanceAccountService
import com.rork.cryptobotai.data.model.BinanceAccountInfo
import com.rork.cryptobotai.data.model.BinanceBalance
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AccountUiState(
    val isLinked: Boolean = false,
    val isLoading: Boolean = false,
    val apiKey: String = "",
    val secretKey: String = "",
    val accountInfo: BinanceAccountInfo? = null,
    val balances: List<BinanceBalance> = emptyList(),
    val totalUsdtValue: Double = 0.0,
    val error: String? = null,
    val successMessage: String? = null
)

class AccountViewModel : ViewModel() {

    private val accountService = BinanceAccountService()

    private val _uiState = MutableStateFlow(AccountUiState())
    val uiState: StateFlow<AccountUiState> = _uiState.asStateFlow()

    private var prefs: SharedPreferences? = null

    fun init(context: Context) {
        if (prefs != null) return
        try {
            val masterKey = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)
            prefs = EncryptedSharedPreferences.create(
                "cryptobot_secure_prefs",
                masterKey,
                context,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
        } catch (_: Exception) {
            // Fallback to regular prefs if encryption unavailable
            prefs = context.getSharedPreferences("cryptobot_prefs", Context.MODE_PRIVATE)
        }

        val savedKey = prefs?.getString("binance_api_key", "") ?: ""
        val savedSecret = prefs?.getString("binance_api_secret", "") ?: ""
        if (savedKey.isNotBlank() && savedSecret.isNotBlank()) {
            _uiState.update { it.copy(apiKey = savedKey, secretKey = savedSecret, isLinked = true) }
            fetchAccountInfo()
        }
    }

    fun setApiKey(value: String) {
        _uiState.update { it.copy(apiKey = value, error = null) }
    }

    fun setSecretKey(value: String) {
        _uiState.update { it.copy(secretKey = value, error = null) }
    }

    fun linkAccount() {
        val state = _uiState.value
        val key = state.apiKey.trim()
        val secret = state.secretKey.trim()

        if (key.isBlank() || secret.isBlank()) {
            _uiState.update { it.copy(error = "Please enter both API Key and Secret Key") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            val valid = try {
                accountService.validateCredentials(key, secret)
            } catch (_: Exception) {
                false
            }

            if (valid) {
                prefs?.edit()?.apply {
                    putString("binance_api_key", key)
                    putString("binance_api_secret", secret)
                    apply()
                }
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isLinked = true,
                        successMessage = "Account linked successfully!",
                        error = null
                    )
                }
                fetchAccountInfo()
            } else {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = "Invalid credentials. Check your API Key & Secret and try again."
                    )
                }
            }
        }
    }

    fun unlinkAccount() {
        prefs?.edit()?.apply {
            remove("binance_api_key")
            remove("binance_api_secret")
            apply()
        }
        _uiState.update {
            AccountUiState()
        }
    }

    fun refreshAccount() {
        if (_uiState.value.isLinked) fetchAccountInfo()
    }

    fun dismissMessages() {
        _uiState.update { it.copy(error = null, successMessage = null) }
    }

    fun getApiCredentials(): Pair<String, String>? {
        val state = _uiState.value
        return if (state.isLinked && state.apiKey.isNotBlank() && state.secretKey.isNotBlank()) {
            Pair(state.apiKey, state.secretKey)
        } else null
    }

    private fun fetchAccountInfo() {
        val state = _uiState.value
        if (!state.isLinked) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            accountService.fetchAccount(state.apiKey, state.secretKey)
                .onSuccess { info ->
                    val nonZero = info.balances.filter { bal ->
                        val free = bal.free.toDoubleOrNull() ?: 0.0
                        val locked = bal.locked.toDoubleOrNull() ?: 0.0
                        free + locked > 0.0
                    }
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            accountInfo = info,
                            balances = nonZero,
                            error = null
                        )
                    }
                }
                .onFailure { e ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = "Failed to fetch account: ${e.message}"
                        )
                    }
                }
        }
    }
}
