package com.rork.cryptobotai.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.PauseCircle
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rork.cryptobotai.data.model.Signal
import com.rork.cryptobotai.data.model.SignalStrength
import com.rork.cryptobotai.data.model.TradeSide
import com.rork.cryptobotai.ui.BotUiState
import com.rork.cryptobotai.ui.components.GlassCard
import com.rork.cryptobotai.ui.theme.BearRed
import com.rork.cryptobotai.ui.theme.BullGreen
import com.rork.cryptobotai.ui.theme.ElectricBlue
import com.rork.cryptobotai.ui.theme.NeonCyan
import com.rork.cryptobotai.ui.theme.NeonGreen
import com.rork.cryptobotai.ui.theme.Stroke
import com.rork.cryptobotai.ui.theme.SurfaceElevated
import com.rork.cryptobotai.ui.theme.TextMuted
import com.rork.cryptobotai.ui.theme.TextPrimary
import com.rork.cryptobotai.ui.theme.TextSecondary
import com.rork.cryptobotai.ui.theme.WarnAmber
import com.rork.cryptobotai.util.formatUsd

@Composable
fun BotScreen(
    state: BotUiState,
    onToggleBot: () -> Unit,
    onSetTradeSize: (Double) -> Unit,
    onExecuteTop: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp, 8.dp, 16.dp, 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item { BotPowerCard(state, onToggleBot) }
        item { TradeSizeCard(state, onSetTradeSize, onExecuteTop) }
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("AI Signals", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Spacer(Modifier.width(8.dp))
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(NeonCyan.copy(alpha = 0.15f))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text("LIVE", color = NeonCyan, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
                Spacer(Modifier.weight(1f))
                Text(state.lastUpdatedLabel, color = TextMuted, fontSize = 11.sp)
            }
        }
        items(state.signals, key = { it.id }) { signal ->
            SignalCard(signal)
        }
    }
}

@Composable
private fun BotPowerCard(state: BotUiState, onToggleBot: () -> Unit) {
    val active = state.botActive
    val transition = rememberInfiniteTransition(label = "ring")
    val ringScale by transition.animateFloat(
        initialValue = 1f, targetValue = 1.18f,
        animationSpec = infiniteRepeatable(tween(1400), RepeatMode.Reverse), label = "ringScale"
    )
    val pressScale by animateFloatAsState(if (active) 1f else 0.98f, label = "press")
    val accent by animateColorAsState(if (active) NeonGreen else TextSecondary, label = "accent")

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(
                Brush.radialGradient(
                    colors = if (active) listOf(Color(0xFF0F2A1D), Color(0xFF08110D))
                    else listOf(Color(0xFF14191D), Color(0xFF0A0E11)),
                    radius = 900f
                )
            )
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(contentAlignment = Alignment.Center) {
                if (active) {
                    Box(
                        modifier = Modifier
                            .size(120.dp).scale(ringScale)
                            .clip(CircleShape).background(NeonGreen.copy(alpha = 0.08f))
                    )
                }
                Box(
                    modifier = Modifier
                        .size(108.dp).scale(pressScale).clip(CircleShape)
                        .background(if (active) Brush.linearGradient(listOf(NeonGreen, Color(0xFF00B894)))
                        else Brush.linearGradient(listOf(SurfaceElevated, SurfaceElevated)))
                        .border(1.dp, accent.copy(alpha = 0.4f), CircleShape)
                        .clickable { onToggleBot() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        if (active) Icons.Filled.PauseCircle else Icons.Filled.PlayCircle,
                        contentDescription = "Toggle bot",
                        tint = if (active) Color(0xFF06140D) else TextPrimary,
                        modifier = Modifier.size(56.dp)
                    )
                }
            }
            Spacer(Modifier.height(18.dp))
            Text(if (active) "Bot is trading" else "Bot is paused",
                color = TextPrimary, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(4.dp))
            Text(if (active) "Auto-executing top signals in real time"
                else "Tap to activate autonomous AI trading",
                color = TextSecondary, fontSize = 13.sp)
        }
    }
}

@Composable
private fun TradeSizeCard(
    state: BotUiState, onSetTradeSize: (Double) -> Unit, onExecuteTop: () -> Unit
) {
    GlassCard {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Trade Size", color = TextSecondary, fontSize = 13.sp)
                Spacer(Modifier.weight(1f))
                Text(formatUsd(state.tradeSizeUsd), color = NeonGreen, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
            Slider(
                value = state.tradeSizeUsd.toFloat(),
                onValueChange = { onSetTradeSize(it.toDouble()) },
                valueRange = 50f..2000f,
                colors = SliderDefaults.colors(thumbColor = NeonGreen, activeTrackColor = NeonGreen,
                    inactiveTrackColor = Stroke)
            )
            Spacer(Modifier.height(4.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth().clip(RoundedCornerShape(14.dp))
                    .background(NeonGreen.copy(alpha = 0.12f))
                    .clickable { onExecuteTop() }.padding(vertical = 14.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Bolt, contentDescription = null, tint = NeonGreen, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Execute Top Signal Now", color = NeonGreen, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
            }
        }
    }
}

@Composable
fun SignalCard(signal: Signal) {
    val buy = signal.side == TradeSide.BUY
    val accent = if (buy) BullGreen else BearRed
    val strengthColor = when (signal.strength) {
        SignalStrength.STRONG -> NeonGreen
        SignalStrength.MODERATE -> WarnAmber
        SignalStrength.WEAK -> TextSecondary
    }

    GlassCard(borderColor = accent.copy(alpha = 0.25f)) {
        Column {
            // ── HEADER ─────────────────────────────────────────
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(accent.copy(alpha = 0.16f))
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    Text(signal.side.name, color = accent, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
                Spacer(Modifier.width(10.dp))
                Column {
                    Text(signal.symbol, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    Text(signal.name, color = TextSecondary, fontSize = 11.sp)
                }
                Spacer(Modifier.weight(1f))
                Column(horizontalAlignment = Alignment.End) {
                    Text("${signal.confidence}%", color = strengthColor, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Text(signal.strength.name, color = strengthColor, fontSize = 10.sp, fontWeight = FontWeight.Medium)
                }
            }

            Spacer(Modifier.height(10.dp))

            // ── CONFIDENCE BAR ─────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth().height(6.dp)
                    .clip(CircleShape).background(Stroke)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(signal.confidence / 100f).height(6.dp)
                        .clip(CircleShape)
                        .background(Brush.horizontalGradient(listOf(strengthColor.copy(alpha = 0.6f), strengthColor)))
                )
            }

            Spacer(Modifier.height(12.dp))

            // ── ENTRY / TARGET / STOP / R:R ────────────────────
            Row {
                PriceLeg("Entry", formatUsd(signal.entryPrice), TextPrimary, Modifier.weight(1f))
                PriceLeg("Target", formatUsd(signal.targetPrice), BullGreen, Modifier.weight(1f))
                PriceLeg("Stop", formatUsd(signal.stopPrice), BearRed, Modifier.weight(1f))
                PriceLeg("R:R", "1:${String.format("%.1f", signal.riskRewardRatio)}", NeonCyan, Modifier.weight(0.7f))
            }

            Spacer(Modifier.height(10.dp))

            // ── MULTI‑FACTOR GRID ──────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                FactorChip("EW", signal.elliottWaveLabel, BullGreen, Modifier.weight(1f))
                FactorChip("ICT", signal.ictBias, ElectricBlue, Modifier.weight(1f))
                FactorChip("Macro", signal.macroStructure, WarnAmber, Modifier.weight(1f))
            }
            Spacer(Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                FactorChip("Delta", signal.footprintDelta, NeonCyan, Modifier.weight(1f))
                FactorChip("News", signal.newsSentiment,
                    if (signal.newsSentiment.contains("Bullish", ignoreCase = true)) BullGreen else BearRed,
                    Modifier.weight(1f))
                Box(modifier = Modifier.weight(1f)) {} // spacer for alignment
            }

            Spacer(Modifier.height(8.dp))

            // ── FULL ANALYSIS ──────────────────────────────────
            Text(signal.fullAnalysis, color = TextSecondary, fontSize = 11.sp, lineHeight = 15.sp,
                maxLines = 8, overflow = TextOverflow.Ellipsis)
        }
    }
}

@Composable
private fun FactorChip(label: String, value: String, color: Color, modifier: Modifier) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(color.copy(alpha = 0.08f))
            .border(1.dp, color.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
            .padding(horizontal = 6.dp, vertical = 6.dp)
    ) {
        Text(label, color = color.copy(alpha = 0.7f), fontSize = 8.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(2.dp))
        Text(value, color = TextPrimary, fontSize = 9.sp, fontWeight = FontWeight.Medium,
            maxLines = 2, overflow = TextOverflow.Ellipsis, lineHeight = 11.sp)
    }
}

@Composable
private fun PriceLeg(label: String, value: String, color: Color, modifier: Modifier) {
    Column(modifier = modifier) {
        Text(label, color = TextMuted, fontSize = 10.sp)
        Spacer(Modifier.height(2.dp))
        Text(value, color = color, fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
    }
}
