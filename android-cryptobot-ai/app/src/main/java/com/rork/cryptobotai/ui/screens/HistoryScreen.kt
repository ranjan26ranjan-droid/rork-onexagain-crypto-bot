package com.rork.cryptobotai.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Inbox
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.North
import androidx.compose.material.icons.filled.South
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rork.cryptobotai.data.model.Trade
import com.rork.cryptobotai.data.model.TradeSide
import com.rork.cryptobotai.ui.BotUiState
import com.rork.cryptobotai.ui.components.GlassCard
import com.rork.cryptobotai.ui.theme.BearRed
import com.rork.cryptobotai.ui.theme.BullGreen
import com.rork.cryptobotai.ui.theme.ElectricBlue
import com.rork.cryptobotai.ui.theme.NeonCyan
import com.rork.cryptobotai.ui.theme.NeonGreen
import com.rork.cryptobotai.ui.theme.Stroke
import com.rork.cryptobotai.ui.theme.SurfaceCard
import com.rork.cryptobotai.ui.theme.SurfaceElevated
import com.rork.cryptobotai.ui.theme.TextMuted
import com.rork.cryptobotai.ui.theme.TextPrimary
import com.rork.cryptobotai.ui.theme.TextSecondary
import com.rork.cryptobotai.ui.theme.WarnAmber
import com.rork.cryptobotai.util.formatPercent
import com.rork.cryptobotai.util.formatUsd
import com.rork.cryptobotai.util.timeAgo

@Composable
fun HistoryScreen(state: BotUiState) {
    if (state.trades.isEmpty()) {
        EmptyHistory()
        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp, 8.dp, 16.dp, 24.dp),
        verticalArrangement = Arrangement.spacedBy(0.dp)
    ) {
        item { SummaryCard(state) }
        item { Spacer(Modifier.height(16.dp)) }

        // Section title
        item {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Trade History", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Spacer(Modifier.weight(1f))
                Text("${state.trades.size} trades", color = TextMuted, fontSize = 12.sp)
            }
        }

        // Column headers — 8‑column professional layout
        item { TradeTableHeader() }

        // Trade rows
        items(state.trades, key = { it.id }) { trade ->
            TradeTableRow(trade)
        }
    }
}

@Composable
private fun TradeTableHeader() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("SYMBOL", color = TextMuted, fontSize = 9.sp, fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1.0f))
        Text("SIDE", color = TextMuted, fontSize = 9.sp, fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(0.85f))
        Text("CONFL", color = TextMuted, fontSize = 9.sp, fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(0.85f))
        Text("E/W", color = TextMuted, fontSize = 9.sp, fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1.05f))
        Text("ICT", color = TextMuted, fontSize = 9.sp, fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(0.9f))
        Text("DELTA", color = TextMuted, fontSize = 9.sp, fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1.0f))
        Text("NEWS", color = TextMuted, fontSize = 9.sp, fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1.0f))
        Text("PROFIT", color = TextMuted, fontSize = 9.sp, fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(0.85f),
            textAlign = androidx.compose.ui.text.style.TextAlign.End
        )
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp)
            .height(1.dp)
            .background(Stroke)
    )
}

@Composable
private fun TradeTableRow(trade: Trade) {
    val win = trade.pnlUsd >= 0
    val accent = if (win) BullGreen else BearRed
    val buy = trade.side == TradeSide.BUY
    val bgAnim by animateColorAsState(
        targetValue = if (win) BullGreen.copy(alpha = 0.04f) else BearRed.copy(alpha = 0.04f),
        label = "rowBg"
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(bgAnim)
            .padding(horizontal = 4.dp, vertical = 10.dp)
    ) {
        // ── 8‑COLUMN DATA ROW ──────────────────────────────────
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // ① Symbol
            Row(
                modifier = Modifier.weight(1.0f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(accent)
                )
                Spacer(Modifier.width(4.dp))
                Text(trade.symbol, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 12.sp, maxLines = 1)
            }

            // ② Side badge
            Box(
                modifier = Modifier.weight(0.85f)
                    .clip(RoundedCornerShape(4.dp))
                    .background(accent.copy(alpha = 0.16f))
                    .padding(horizontal = 4.dp, vertical = 2.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(if (buy) Icons.Filled.North else Icons.Filled.South,
                        contentDescription = null, tint = accent, modifier = Modifier.size(9.dp))
                    Spacer(Modifier.width(1.dp))
                    Text(if (buy) "BUY" else "SELL", color = accent, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                }
            }

            // ③ Confluence score
            Column(modifier = Modifier.weight(0.85f), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("${trade.confluenceScore}", color = confluenceColor(trade.confluenceScore),
                    fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Text("/100", color = TextMuted, fontSize = 8.sp)
            }

            // ④ Elliott Wave
            Text(trade.elliottWave.ifBlank { "—" }, color = TextSecondary, fontSize = 10.sp,
                modifier = Modifier.weight(1.05f), maxLines = 1, overflow = TextOverflow.Ellipsis
            )

            // ⑤ ICT signal
            Box(
                modifier = Modifier.weight(0.9f)
                    .clip(RoundedCornerShape(3.dp))
                    .background(ElectricBlue.copy(alpha = 0.12f))
                    .padding(horizontal = 4.dp, vertical = 2.dp)
            ) {
                Text(trade.ictSignal.ifBlank { "—" }, color = ElectricBlue, fontSize = 8.sp,
                    fontWeight = FontWeight.Medium, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }

            // ⑥ Delta / order flow
            Text(trade.orderFlow.ifBlank { "—" }, color = TextSecondary, fontSize = 10.sp,
                modifier = Modifier.weight(1.0f), maxLines = 1, overflow = TextOverflow.Ellipsis
            )

            // ⑦ News sentiment
            val newsColor = when {
                trade.newsSignal.contains("Bullish", ignoreCase = true) -> BullGreen
                trade.newsSignal.contains("Bearish", ignoreCase = true) -> BearRed
                else -> TextSecondary
            }
            Text(trade.newsSignal.ifBlank { "—" }, color = newsColor, fontSize = 9.sp,
                modifier = Modifier.weight(1.0f), maxLines = 1, overflow = TextOverflow.Ellipsis,
                fontWeight = FontWeight.Medium
            )

            // ⑧ Profit
            Column(
                modifier = Modifier.weight(0.85f),
                horizontalAlignment = Alignment.End
            ) {
                Text(formatUsd(trade.pnlUsd, withSign = true), color = accent, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                Text(formatPercent(trade.pnlPercent), color = accent.copy(alpha = 0.8f), fontSize = 9.sp)
            }
        }

        // ── AI ANALYSIS ROW ────────────────────────────────────
        if (trade.analysis.isNotBlank()) {
            Spacer(Modifier.height(6.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(SurfaceElevated.copy(alpha = 0.6f))
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                verticalAlignment = Alignment.Top
            ) {
                Icon(Icons.Filled.Lightbulb, contentDescription = null,
                    tint = NeonCyan.copy(alpha = 0.7f), modifier = Modifier.size(12.dp))
                Spacer(Modifier.width(6.dp))
                Column {
                    Text(trade.analysis, color = TextSecondary, fontSize = 10.sp, lineHeight = 13.sp,
                        maxLines = 5, overflow = TextOverflow.Ellipsis)
                    Spacer(Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("R:R 1:${String.format("%.1f", trade.riskReward)}", color = NeonCyan.copy(alpha = 0.8f),
                            fontSize = 10.sp, fontWeight = FontWeight.Medium)
                        Text("  ·  ", color = TextMuted, fontSize = 10.sp)
                        Text("Confidence: ${trade.confidence}%", color = NeonCyan.copy(alpha = 0.8f),
                            fontSize = 10.sp, fontWeight = FontWeight.Medium)
                        Text("  ·  ", color = TextMuted, fontSize = 10.sp)
                        Text(timeAgo(trade.closedAt), color = TextMuted, fontSize = 10.sp)
                    }
                }
            }
        }

        // Row separator
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp)
                .height(1.dp)
                .background(Stroke.copy(alpha = 0.4f))
        )
    }
}

private fun confluenceColor(score: Int): Color = when {
    score >= 78 -> NeonGreen
    score >= 60 -> WarnAmber
    score >= 42 -> TextSecondary
    else -> BearRed
}

@Composable
private fun SummaryCard(state: BotUiState) {
    val pnl = state.totalPnl
    val positive = pnl >= 0
    val accent = if (positive) BullGreen else BearRed

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(
                Brush.linearGradient(
                    listOf(
                        if (positive) Color(0xFF0E2A1C) else Color(0xFF2A1114),
                        SurfaceCard
                    )
                )
            )
            .padding(20.dp)
    ) {
        Row {
            Column(modifier = Modifier.weight(1f)) {
                Text("Total Profit", color = TextSecondary, fontSize = 12.sp)
                Spacer(Modifier.height(4.dp))
                Text(formatUsd(pnl, withSign = true), color = accent, fontWeight = FontWeight.Bold, fontSize = 28.sp)
                Spacer(Modifier.height(4.dp))
                Text(formatPercent(state.totalPnlPercent), color = accent.copy(alpha = 0.8f), fontSize = 14.sp)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text("Win Rate", color = TextSecondary, fontSize = 12.sp)
                Spacer(Modifier.height(4.dp))
                Text("${state.winRate}%", color = if (state.winRate >= 50) NeonGreen else TextPrimary,
                    fontWeight = FontWeight.Bold, fontSize = 28.sp)
                Spacer(Modifier.height(4.dp))
                Text("${state.winCount}/${state.trades.size} won", color = TextMuted, fontSize = 12.sp)
            }
        }
    }
}

@Composable
private fun EmptyHistory() {
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(NeonGreen.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Filled.Inbox, contentDescription = null, tint = NeonGreen, modifier = Modifier.size(40.dp))
        }
        Spacer(Modifier.height(16.dp))
        Text("No trades yet", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 18.sp)
        Spacer(Modifier.height(6.dp))
        Text(
            "Start the AI bot to begin auto-trading\nand watch your profits roll in.",
            color = TextSecondary, fontSize = 13.sp,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
    }
}
