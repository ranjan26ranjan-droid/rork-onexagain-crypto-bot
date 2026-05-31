package com.rork.cryptobotai.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.North
import androidx.compose.material.icons.filled.South
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.rork.cryptobotai.data.model.Coin
import com.rork.cryptobotai.data.model.Trade
import com.rork.cryptobotai.data.model.TradeSide
import com.rork.cryptobotai.ui.BotUiState
import com.rork.cryptobotai.ui.components.GlassCard
import com.rork.cryptobotai.ui.components.PulsingDot
import com.rork.cryptobotai.ui.components.Sparkline
import com.rork.cryptobotai.ui.theme.BearRed
import com.rork.cryptobotai.ui.theme.BullGreen
import com.rork.cryptobotai.ui.theme.ElectricBlue
import com.rork.cryptobotai.ui.theme.NeonGreen
import com.rork.cryptobotai.ui.theme.Stroke
import com.rork.cryptobotai.ui.theme.SurfaceElevated
import com.rork.cryptobotai.ui.theme.TextMuted
import com.rork.cryptobotai.ui.theme.TextPrimary
import com.rork.cryptobotai.ui.theme.TextSecondary
import com.rork.cryptobotai.util.formatPercent
import com.rork.cryptobotai.util.formatUsd

@Composable
fun DashboardScreen(
    state: BotUiState,
    onDismissBanner: () -> Unit,
    onOpenBot: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp, 8.dp, 16.dp, 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            AnimatedVisibility(
                visible = state.errorBanner != null,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                BannerRow(state.errorBanner ?: "", onDismissBanner)
            }
        }
        item { PortfolioCard(state, onOpenBot) }
        item { StatGrid(state) }

        // ── Recent Trades with multi‑factor columns ────────────
        if (state.trades.isNotEmpty()) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Recent Trades", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Spacer(Modifier.weight(1f))
                    Text("${state.trades.size} total", color = TextMuted, fontSize = 12.sp)
                }
            }
            item { CompactTradeHeader() }
            items(state.trades.take(5), key = { it.id }) { trade ->
                CompactTradeRow(trade)
            }
        }

        item {
            Text("Top Movers", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 18.sp,
                modifier = Modifier.padding(top = 4.dp))
        }
        items(state.coins.take(8), key = { it.id }) { coin ->
            MoverRow(coin)
        }
    }
}

@Composable
private fun BannerRow(message: String, onDismiss: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth().clip(RoundedCornerShape(14.dp))
            .background(Color(0xFF2A2113)).padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("⚠️  $message", color = Color(0xFFFFB020), fontSize = 13.sp, modifier = Modifier.weight(1f))
        Icon(Icons.Filled.Close, contentDescription = "Dismiss", tint = TextSecondary,
            modifier = Modifier.size(18.dp).clickable { onDismiss() })
    }
}

@Composable
private fun PortfolioCard(state: BotUiState, onOpenBot: () -> Unit) {
    val positive = state.totalPnl >= 0
    val accent = if (positive) BullGreen else BearRed
    val animatedBalance by animateFloatAsState(
        targetValue = state.balance.toFloat(), animationSpec = tween(700), label = "balance"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth().clip(RoundedCornerShape(24.dp))
            .background(Brush.linearGradient(listOf(Color(0xFF0E2A1C), Color(0xFF0A1419))))
            .padding(20.dp)
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Portfolio Value", color = TextSecondary, fontSize = 13.sp)
                Spacer(Modifier.weight(1f))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(if (state.botActive) NeonGreen.copy(alpha = 0.15f) else SurfaceElevated)
                        .clickable { onOpenBot() }
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    if (state.botActive) {
                        PulsingDot(size = 8.dp)
                        Spacer(Modifier.width(6.dp))
                    }
                    Text(
                        if (state.botActive) "BOT LIVE" else "BOT OFF",
                        color = if (state.botActive) NeonGreen else TextSecondary,
                        fontSize = 11.sp, fontWeight = FontWeight.Bold
                    )
                }
            }
            Spacer(Modifier.height(10.dp))
            Text(formatUsd(animatedBalance.toDouble()), color = TextPrimary, fontSize = 40.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(if (positive) Icons.Filled.TrendingUp else Icons.Filled.TrendingDown,
                    contentDescription = null, tint = accent, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text("${formatUsd(state.totalPnl, withSign = true)}  (${formatPercent(state.totalPnlPercent)})",
                    color = accent, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.weight(1f))
                Text("All time", color = TextMuted, fontSize = 12.sp)
            }
        }
    }
}

@Composable
private fun StatGrid(state: BotUiState) {
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        StatTile("Win Rate", "${state.winRate}%", Modifier.weight(1f), NeonGreen)
        StatTile("Trades", "${state.trades.size}", Modifier.weight(1f), TextPrimary)
        StatTile("Signals", "${state.signals.size}", Modifier.weight(1f), TextPrimary)
    }
}

@Composable
private fun StatTile(label: String, value: String, modifier: Modifier, valueColor: Color) {
    GlassCard(modifier = modifier, contentPadding = androidx.compose.foundation.layout.PaddingValues(14.dp)) {
        Column {
            Text(value, color = valueColor, fontSize = 22.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(2.dp))
            Text(label, color = TextSecondary, fontSize = 12.sp)
        }
    }
}

@Composable
fun MoverRow(coin: Coin) {
    val positive = coin.change24h >= 0
    val accent = if (positive) BullGreen else BearRed
    GlassCard(contentPadding = androidx.compose.foundation.layout.PaddingValues(14.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            CoinAvatar(coin)
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.width(70.dp)) {
                Text(coin.symbol.uppercase(), color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                Text(coin.name, color = TextSecondary, fontSize = 11.sp, maxLines = 1)
            }
            Spacer(Modifier.width(8.dp))
            Sparkline(points = coin.sparklinePoints.ifEmpty { listOf(1.0, 1.0) },
                modifier = Modifier.weight(1f).height(36.dp))
            Spacer(Modifier.width(8.dp))
            Column(horizontalAlignment = Alignment.End) {
                Text(formatUsd(coin.currentPrice), color = TextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                Text(formatPercent(coin.change24h), color = accent, fontSize = 12.sp, fontWeight = FontWeight.Medium)
            }
        }
    }
}

@Composable
fun CoinAvatar(coin: Coin, size: Int = 36) {
    if (coin.image.isNotBlank()) {
        AsyncImage(model = coin.image, contentDescription = coin.name,
            modifier = Modifier.size(size.dp).clip(CircleShape).background(SurfaceElevated))
    } else {
        Box(modifier = Modifier.size(size.dp).clip(CircleShape).background(SurfaceElevated),
            contentAlignment = Alignment.Center) {
            Text(coin.symbol.take(1).uppercase(), color = NeonGreen,
                fontWeight = FontWeight.Bold, fontSize = (size / 2.5).sp)
        }
    }
}

// ── Compact trade table for dashboard (6‑column layout) ──────

@Composable
private fun CompactTradeHeader() {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("SYMBOL", color = TextMuted, fontSize = 9.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1.0f))
        Text("SIDE", color = TextMuted, fontSize = 9.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(0.7f))
        Text("CONFL", color = TextMuted, fontSize = 9.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(0.7f))
        Text("E/W", color = TextMuted, fontSize = 9.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1.1f))
        Text("ICT", color = TextMuted, fontSize = 9.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1.0f))
        Text("PROFIT", color = TextMuted, fontSize = 9.sp, fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(0.8f), textAlign = TextAlign.End)
    }
    Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Stroke.copy(alpha = 0.5f)))
}

@Composable
private fun CompactTradeRow(trade: Trade) {
    val win = trade.pnlUsd >= 0
    val accent = if (win) BullGreen else BearRed
    val buy = trade.side == TradeSide.BUY

    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Symbol
        Row(modifier = Modifier.weight(1.0f), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(5.dp).clip(RoundedCornerShape(2.dp)).background(accent))
            Spacer(Modifier.width(6.dp))
            Text(trade.symbol, color = TextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
        }

        // Side badge
        Box(
            modifier = Modifier.weight(0.7f).clip(RoundedCornerShape(4.dp))
                .background(accent.copy(alpha = 0.14f)).padding(horizontal = 5.dp, vertical = 2.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(if (buy) Icons.Filled.North else Icons.Filled.South,
                    contentDescription = null, tint = accent, modifier = Modifier.size(9.dp))
                Spacer(Modifier.width(1.dp))
                Text(if (buy) "BUY" else "SELL", color = accent, fontSize = 8.sp, fontWeight = FontWeight.Bold)
            }
        }

        // Confluence
        Text("${trade.confluenceScore}",
            color = when {
                trade.confluenceScore >= 78 -> NeonGreen
                trade.confluenceScore >= 60 -> Color(0xFFFFB020)
                else -> TextSecondary
            },
            fontWeight = FontWeight.Bold, fontSize = 12.sp,
            modifier = Modifier.weight(0.7f), textAlign = TextAlign.Center
        )

        // Elliott Wave
        Text(trade.elliottWave.ifBlank { "—" }, color = TextSecondary, fontSize = 9.sp,
            modifier = Modifier.weight(1.1f), maxLines = 1, overflow = TextOverflow.Ellipsis)

        // ICT
        Box(
            modifier = Modifier.weight(1.0f).clip(RoundedCornerShape(3.dp))
                .background(ElectricBlue.copy(alpha = 0.1f)).padding(horizontal = 4.dp, vertical = 2.dp)
        ) {
            Text(trade.ictSignal.ifBlank { "—" }, color = ElectricBlue, fontSize = 8.sp,
                fontWeight = FontWeight.Medium, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }

        // Profit
        Column(modifier = Modifier.weight(0.8f), horizontalAlignment = Alignment.End) {
            Text(formatUsd(trade.pnlUsd, withSign = true), color = accent, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            Text(formatPercent(trade.pnlPercent), color = accent.copy(alpha = 0.8f), fontSize = 9.sp)
        }
    }

    Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Stroke.copy(alpha = 0.25f)))
}
