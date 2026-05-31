package com.rork.cryptobotai.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rork.cryptobotai.data.AiBotEngine
import com.rork.cryptobotai.data.BinanceAccountService
import com.rork.cryptobotai.data.MarketRepository
import com.rork.cryptobotai.data.TechnicalAnalysisEngine
import com.rork.cryptobotai.data.model.Coin
import com.rork.cryptobotai.data.model.Signal
import com.rork.cryptobotai.data.model.Trade
import com.rork.cryptobotai.data.model.TradeSide
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Snapshot of the entire app state observed by the UI. */
data class BotUiState(
    val isLoading: Boolean = true,
    val coins: List<Coin> = emptyList(),
    val signals: List<Signal> = emptyList(),
    val trades: List<Trade> = emptyList(),
    val botActive: Boolean = false,
    val startingBalance: Double = 10_000.0,
    val balance: Double = 10_000.0,
    val tradeSizeUsd: Double = 250.0,
    val lastUpdatedLabel: String = "",
    val errorBanner: String? = null,
    val tradingMode: TradingMode = TradingMode.DEMO
) {
    val totalPnl: Double get() = balance - startingBalance
    val totalPnlPercent: Double
        get() = if (startingBalance == 0.0) 0.0 else totalPnl / startingBalance * 100
    val winCount: Int get() = trades.count { it.pnlUsd >= 0 }
    val winRate: Int
        get() = if (trades.isEmpty()) 0 else (winCount * 100 / trades.size)
    val openTradesValue: Double get() = trades.filter { it.isOpen }.sumOf { it.amountUsd }
}

enum class TradingMode { DEMO, LIVE }

class BotViewModel : ViewModel() {

    private val market = MarketRepository()
    private val engine = AiBotEngine()
    private val accountService = BinanceAccountService()

    private val _uiState = MutableStateFlow(BotUiState())
    val uiState: StateFlow<BotUiState> = _uiState.asStateFlow()

    private var autoTradeRunning = false
    private var lastUpdateMs: Long = 0L

    /** Provider that yields Binance API credentials when the user has linked their account. */
    private var credentialsProvider: (() -> Pair<String, String>?)? = null

    init {
        refresh()
        startLivePolling()
    }

    /** Called from the Account tab when credentials become available / change. */
    fun setAccountProvider(provider: () -> Pair<String, String>?) {
        credentialsProvider = provider
        val creds = provider()
        val live = creds != null && creds.first.isNotBlank()
        _uiState.update { it.copy(tradingMode = if (live) TradingMode.LIVE else TradingMode.DEMO) }
    }

    /** Full refresh — fetches live Binance data and regenerates AI signals. */
    fun refresh() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val coins = try {
                market.fetchMarkets().also { lastUpdateMs = System.currentTimeMillis() }
            } catch (_: Exception) {
                _uiState.update { it.copy(errorBanner = "Binance feed unavailable — showing backup data") }
                market.demoCoins()
            }
            val signals = engine.generateSignals(coins)
            _uiState.update {
                it.copy(
                    isLoading = false,
                    coins = coins,
                    signals = signals,
                    lastUpdatedLabel = "Updated just now",
                    errorBanner = null
                )
            }
        }
    }

    /**
     * Background polling: refreshes Binance prices & signals every 10 seconds
     * while the bot is active, or every 30 seconds when idle — giving a truly live feel.
     */
    private fun startLivePolling() {
        viewModelScope.launch {
            while (true) {
                delay(if (_uiState.value.botActive) 10_000L else 30_000L)
                val coins = try {
                    market.fetchMarkets().also { lastUpdateMs = System.currentTimeMillis() }
                } catch (_: Exception) {
                    _uiState.value.coins
                }
                val signals = engine.generateSignals(coins)
                _uiState.update {
                    it.copy(
                        coins = coins,
                        signals = signals,
                        lastUpdatedLabel = updatedAgoLabel(),
                        errorBanner = null
                    )
                }
            }
        }
    }

    private fun updatedAgoLabel(): String {
        val elapsed = (System.currentTimeMillis() - lastUpdateMs) / 1000
        return when {
            elapsed < 5 -> "Updated just now"
            elapsed < 60 -> "Updated ${elapsed}s ago"
            else -> "Updated ${elapsed / 60}m ago"
        }
    }

    fun toggleBot() {
        val nowActive = !_uiState.value.botActive
        _uiState.update { it.copy(botActive = nowActive) }
        if (nowActive) startAutoTrading() else autoTradeRunning = false
    }

    fun setTradeSize(size: Double) {
        _uiState.update { it.copy(tradeSizeUsd = size.coerceIn(50.0, 2000.0)) }
    }

    /** Manually fire the top signal once. */
    fun executeTopSignal() {
        val signal = _uiState.value.signals.firstOrNull() ?: return
        runTrade(signal)
    }

    fun dismissBanner() {
        _uiState.update { it.copy(errorBanner = null) }
    }

    private fun startAutoTrading() {
        if (autoTradeRunning) return
        autoTradeRunning = true
        viewModelScope.launch {
            while (autoTradeRunning && _uiState.value.botActive) {
                delay(4500)
                val state = _uiState.value
                val bestSignal = state.signals
                    .filter { it.confidence >= 60 }
                    .maxByOrNull { it.confidence } ?: state.signals.randomOrNull() ?: continue
                runTrade(bestSignal)
            }
        }
    }

    private fun runTrade(signal: Signal) {
        val state = _uiState.value
        val size = state.tradeSizeUsd

        // Check if we should use real Binance trading
        val creds = credentialsProvider?.invoke()
        if (creds != null && creds.first.isNotBlank()) {
            runRealTrade(signal, size, creds)
        } else {
            runDemoTrade(signal, size)
        }
    }

    /** Place a real order on Binance and record the trade. */
    private fun runRealTrade(signal: Signal, amountUsd: Double, creds: Pair<String, String>) {
        viewModelScope.launch {
            val side = if (signal.side == TradeSide.BUY) "BUY" else "SELL"
            val result = accountService.placeMarketOrder(
                apiKey = creds.first,
                secretKey = creds.second,
                symbol = "${signal.symbol}USDT",
                side = side,
                quoteOrderQty = amountUsd
            )

            result.onSuccess { order ->
                val now = System.currentTimeMillis()
                val fillPrice = order.fills.firstOrNull()?.price?.toDoubleOrNull() ?: signal.entryPrice
                val trade = Trade(
                    id = "real-${order.orderId}",
                    symbol = signal.symbol,
                    name = signal.name,
                    image = signal.image,
                    side = signal.side,
                    amountUsd = amountUsd,
                    entryPrice = fillPrice,
                    exitPrice = 0.0,
                    pnlUsd = 0.0,
                    pnlPercent = 0.0,
                    confidence = signal.confidence,
                    analysis = "Real order placed on Binance\n${signal.fullAnalysis}",
                    isOpen = true,
                    openedAt = now,
                    closedAt = 0L,
                    confluenceScore = signal.confluenceScore,
                    elliottWave = signal.elliottWaveLabel,
                    ictSignal = signal.ictBias,
                    macroTrend = signal.macroStructure,
                    orderFlow = signal.footprintDelta,
                    newsSignal = signal.newsSentiment,
                    riskReward = signal.riskRewardRatio
                )
                _uiState.update {
                    it.copy(
                        trades = (listOf(trade) + it.trades).take(60),
                        errorBanner = null
                    )
                }
            }.onFailure { e ->
                _uiState.update {
                    it.copy(
                        trades = (listOf(demoTrade(signal, amountUsd)) + it.trades).take(60),
                        errorBanner = "Binance order failed: ${e.message?.take(60)} — using demo"
                    )
                }
            }
        }
    }

    /** Execute as a demo/simulated trade. */
    private fun runDemoTrade(signal: Signal, amountUsd: Double) {
        val trade = demoTrade(signal, amountUsd)
        _uiState.update {
            it.copy(
                trades = (listOf(trade) + it.trades).take(60),
                balance = it.balance + trade.pnlUsd
            )
        }
    }

    /** Simulate a trade based on AI signal (used for both demo fallback and when account isn't linked). */
    private fun demoTrade(signal: Signal, amountUsd: Double): Trade {
        val now = System.currentTimeMillis()
        val trade = engine.executeSignal(signal, amountUsd)
        return trade
    }
}
