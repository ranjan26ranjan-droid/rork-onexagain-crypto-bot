package com.rork.cryptobotai.data.model

/** Direction of a trading signal / position. */
enum class TradeSide { BUY, SELL }

/** Confidence tier produced by the AI engine. */
enum class SignalStrength { STRONG, MODERATE, WEAK }

/** A live AI-generated trading signal shown on the bot screen. */
data class Signal(
    val id: String,
    val coinId: String,
    val symbol: String,
    val name: String,
    val image: String,
    val side: TradeSide,
    val strength: SignalStrength,
    val confidence: Int,
    val entryPrice: Double,
    val targetPrice: Double,
    val stopPrice: Double,
    val reason: String,
    val createdAt: Long,
    // ── Multi‑factor analysis breakdown ──
    val confluenceScore: Int = 0,
    val elliottWaveLabel: String = "",
    val ictBias: String = "",
    val macroStructure: String = "",
    val footprintDelta: String = "",
    val newsSentiment: String = "",
    val riskRewardRatio: Double = 0.0,
    val fullAnalysis: String = ""
)

/** A completed (or open) trade with full AI analysis attached. */
data class Trade(
    val id: String,
    val symbol: String,
    val name: String,
    val image: String,
    val side: TradeSide,
    val amountUsd: Double,
    val entryPrice: Double,
    val exitPrice: Double,
    val pnlUsd: Double,
    val pnlPercent: Double,
    val confidence: Int,
    val analysis: String,
    val isOpen: Boolean,
    val openedAt: Long,
    val closedAt: Long,
    // ── Multi‑factor breakdown ──
    val confluenceScore: Int = 0,
    val elliottWave: String = "",
    val ictSignal: String = "",
    val macroTrend: String = "",
    val orderFlow: String = "",
    val newsSignal: String = "",
    val riskReward: Double = 0.0
)
