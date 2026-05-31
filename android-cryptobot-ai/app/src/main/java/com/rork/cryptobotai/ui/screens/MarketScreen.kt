package com.rork.cryptobotai.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rork.cryptobotai.ui.BotUiState
import com.rork.cryptobotai.ui.theme.NeonGreen
import com.rork.cryptobotai.ui.theme.SurfaceElevated
import com.rork.cryptobotai.ui.theme.TextSecondary

private enum class MarketSort(val label: String) {
    RANK("Top"), GAINERS("Gainers"), LOSERS("Losers")
}

@Composable
fun MarketScreen(state: BotUiState) {
    var sort by remember { mutableStateOf(MarketSort.RANK) }

    val coins = remember(state.coins, sort) {
        when (sort) {
            MarketSort.RANK -> state.coins.sortedBy { it.rank }
            MarketSort.GAINERS -> state.coins.sortedByDescending { it.change24h }
            MarketSort.LOSERS -> state.coins.sortedBy { it.change24h }
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp, 8.dp, 16.dp, 24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                MarketSort.entries.forEach { option ->
                    FilterChip(
                        label = option.label,
                        selected = sort == option,
                        onClick = { sort = option }
                    )
                }
            }
        }
        items(coins, key = { it.id }) { coin ->
            MoverRow(coin)
        }
    }
}

@Composable
private fun FilterChip(label: String, selected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(if (selected) NeonGreen.copy(alpha = 0.15f) else SurfaceElevated)
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 9.dp)
    ) {
        Text(
            label,
            color = if (selected) NeonGreen else TextSecondary,
            fontWeight = FontWeight.SemiBold,
            fontSize = 13.sp
        )
    }
}
