package com.rork.cryptobotai.data

import com.rork.cryptobotai.data.model.Coin
import com.rork.cryptobotai.data.model.Signal
import com.rork.cryptobotai.data.model.SignalStrength
import com.rork.cryptobotai.data.model.Trade
import com.rork.cryptobotai.data.model.TradeSide
import kotlin.math.abs
import kotlin.random.Random

/**
 * The AI trading brain — now powered by a multi‑factor technical‑analysis
 * engine (Elliott Wave, ICT, macro structure, footprint/order flow, news
 * sentiment) with confluence scoring for high‑probability trade signals.
 */
class AiBotEngine {

    private val ta = TechnicalAnalysisEngine()

    /** Analyse coins and return the highest-conviction signals, best first. */
    fun generateSignals(coins: List<Coin>): List<Signal> {
        val now = System.currentTimeMillis()
        return coins
            .filter { it.currentPrice > 0 }
            .map { coin -> scoreCoin(coin, now) }
            .sortedByDescending { it.confidence }
            .take(8)
    }

    private fun scoreCoin(coin: Coin, now: Long): Signal {
        val analysis = ta.analyze(coin)
        val confluence = analysis.confluence

        val bullish = confluence.total >= 50
        val side = if (bullish) TradeSide.BUY else TradeSide.SELL

        val confidence = confluence.total

        val strength = when {
            confidence >= 78 -> SignalStrength.STRONG
            confidence >= 60 -> SignalStrength.MODERATE
            else -> SignalStrength.WEAK
        }

        val entry = confluence.suggestedEntry
        val target = confluence.suggestedTarget
        val stop = confluence.suggestedStop

        // Build a rich reason string from all factors
        val reasonParts = mutableListOf<String>()
        reasonParts += "${analysis.elliottWave.pattern}: ${analysis.elliottWave.currentWave}"
        reasonParts += "ICT: ${analysis.ict.bias.name}${if (analysis.ict.liquiditySweep) " + Liq Sweep" else ""}"
        reasonParts += "Macro: ${analysis.macro.marketStructure}"
        reasonParts += "Delta: ${analysis.footprint.delta}"
        reasonParts += "${analysis.newsSentiment.sentiment} (${analysis.newsSentiment.score.toInt()}/100)"
        val reason = reasonParts.joinToString(" · ")

        // Full multi‑line analysis for detail views
        val fullAnalysis = buildString {
            appendLine("═══ CONFLUENCE: ${confluence.total}/100 — ${confluence.verdict}")
            appendLine("• Elliott Wave: ${analysis.elliottWave.description}")
            appendLine("• ICT: ${analysis.ict.description}")
            appendLine("• Macro: ${analysis.macro.description}")
            appendLine("• Footprint: ${analysis.footprint.description}")
            appendLine("• News: ${analysis.newsSentiment.description}")
            appendLine("• R:R = 1:${String.format("%.1f", confluence.riskRewardRatio)}")
            append("• Position: ${confluence.positionSizeRecommendation}")
        }

        return Signal(
            id = "${coin.id}-$now",
            coinId = coin.id,
            symbol = coin.symbol.uppercase(),
            name = coin.name,
            image = coin.image,
            side = side,
            strength = strength,
            confidence = confidence,
            entryPrice = entry,
            targetPrice = target,
            stopPrice = stop,
            reason = reason,
            createdAt = now,
            confluenceScore = confluence.total,
            elliottWaveLabel = "${analysis.elliottWave.pattern}: ${analysis.elliottWave.currentWave}",
            ictBias = "${analysis.ict.bias.name}${if (analysis.ict.fvgDetected) " + FVG" else ""}",
            macroStructure = analysis.macro.marketStructure,
            footprintDelta = analysis.footprint.absorption,
            newsSentiment = analysis.newsSentiment.sentiment,
            riskRewardRatio = confluence.riskRewardRatio,
            fullAnalysis = fullAnalysis
        )
    }

    /**
     * Simulate executing a signal: produces a closed trade with realistic PnL
     * weighted by the AI's confluence score (higher confidence -> better win odds).
     */
    fun executeSignal(signal: Signal, amountUsd: Double): Trade {
        val now = System.currentTimeMillis()
        val winChance = signal.confidence / 100.0 * 0.88 + 0.06
        val win = Random.nextDouble() < winChance

        val targetMove = abs(signal.targetPrice - signal.entryPrice) / signal.entryPrice
        val stopMove = abs(signal.stopPrice - signal.entryPrice) / signal.entryPrice

        val pnlPercent = if (win) {
            targetMove * (0.55 + Random.nextDouble() * 0.55) * 100
        } else {
            -stopMove * (0.55 + Random.nextDouble() * 0.55) * 100
        }
        val pnlUsd = amountUsd * pnlPercent / 100.0
        val exitPrice = signal.entryPrice * (1 + pnlPercent / 100.0 *
                (if (signal.side == TradeSide.BUY) 1 else -1))

        return Trade(
            id = "trade-$now-${signal.symbol}",
            symbol = signal.symbol,
            name = signal.name,
            image = signal.image,
            side = signal.side,
            amountUsd = amountUsd,
            entryPrice = signal.entryPrice,
            exitPrice = exitPrice,
            pnlUsd = pnlUsd,
            pnlPercent = pnlPercent,
            confidence = signal.confidence,
            analysis = signal.fullAnalysis,
            isOpen = false,
            openedAt = now - Random.nextLong(60_000, 3_600_000),
            closedAt = now,
            confluenceScore = signal.confluenceScore,
            elliottWave = signal.elliottWaveLabel,
            ictSignal = signal.ictBias,
            macroTrend = signal.macroStructure,
            orderFlow = signal.footprintDelta,
            newsSignal = signal.newsSentiment,
            riskReward = signal.riskRewardRatio
        )
    }
}
