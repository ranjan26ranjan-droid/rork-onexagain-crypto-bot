package com.rork.cryptobotai.ui.navigation

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CandlestickChart
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.CandlestickChart
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.ReceiptLong
import androidx.compose.material.icons.outlined.SmartToy
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.rork.cryptobotai.ui.AccountViewModel
import com.rork.cryptobotai.ui.BotViewModel
import com.rork.cryptobotai.ui.components.PulsingDot
import com.rork.cryptobotai.ui.screens.AccountScreen
import com.rork.cryptobotai.ui.screens.BotScreen
import com.rork.cryptobotai.ui.screens.DashboardScreen
import com.rork.cryptobotai.ui.screens.HistoryScreen
import com.rork.cryptobotai.ui.screens.MarketScreen
import com.rork.cryptobotai.ui.theme.Background
import com.rork.cryptobotai.ui.theme.NeonGreen
import com.rork.cryptobotai.ui.theme.Stroke
import com.rork.cryptobotai.ui.theme.SurfaceDark
import com.rork.cryptobotai.ui.theme.TextMuted
import com.rork.cryptobotai.ui.theme.TextPrimary
import com.rork.cryptobotai.ui.theme.TextSecondary

private enum class Tab(
    val title: String,
    val filledIcon: ImageVector,
    val outlinedIcon: ImageVector
) {
    DASHBOARD("Dashboard", Icons.Filled.Dashboard, Icons.Outlined.Dashboard),
    BOT("AI Bot", Icons.Filled.SmartToy, Icons.Outlined.SmartToy),
    MARKET("Market", Icons.Filled.CandlestickChart, Icons.Outlined.CandlestickChart),
    HISTORY("History", Icons.Filled.ReceiptLong, Icons.Outlined.ReceiptLong),
    ACCOUNT("Account", Icons.Filled.AccountBalanceWallet, Icons.Outlined.AccountBalanceWallet)
}

@Composable
fun AppNavigation() {
    val botViewModel: BotViewModel = viewModel()
    val accountViewModel: AccountViewModel = viewModel()
    val botState by botViewModel.uiState.collectAsStateWithLifecycle()
    val accountState by accountViewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    var tab by remember { mutableStateOf(Tab.DASHBOARD) }

    Scaffold(
        containerColor = Background,
        topBar = { AppTopBar(botState.botActive, accountState.isLinked, onRefresh = botViewModel::refresh) },
        bottomBar = {
            BottomBar(current = tab, onSelect = { tab = it })
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            AnimatedContent(
                targetState = tab,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "tab"
            ) { current ->
                when (current) {
                    Tab.DASHBOARD -> DashboardScreen(
                        state = botState,
                        onDismissBanner = botViewModel::dismissBanner,
                        onOpenBot = { tab = Tab.BOT }
                    )
                    Tab.BOT -> BotScreen(
                        state = botState,
                        onToggleBot = botViewModel::toggleBot,
                        onSetTradeSize = botViewModel::setTradeSize,
                        onExecuteTop = botViewModel::executeTopSignal
                    )
                    Tab.MARKET -> MarketScreen(state = botState)
                    Tab.HISTORY -> HistoryScreen(state = botState)
                    Tab.ACCOUNT -> AccountScreen(
                        state = accountState,
                        onLink = accountViewModel::linkAccount,
                        onUnlink = accountViewModel::unlinkAccount,
                        onRefresh = accountViewModel::refreshAccount,
                        onSetKey = accountViewModel::setApiKey,
                        onSetSecret = accountViewModel::setSecretKey,
                        onDismissMessages = accountViewModel::dismissMessages,
                        onInit = { ctx ->
                            accountViewModel.init(ctx)
                            botViewModel.setAccountProvider { accountViewModel.getApiCredentials() }
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun AppTopBar(botActive: Boolean, accountLinked: Boolean, onRefresh: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Background)
            .padding(start = 18.dp, end = 14.dp, top = 14.dp, bottom = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(Brush.linearGradient(listOf(NeonGreen, Color(0xFF00B894)))),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Filled.Bolt, contentDescription = null, tint = Color(0xFF06140D), modifier = Modifier.size(20.dp))
        }
        Spacer(Modifier.width(10.dp))
        Column {
            Text("CryptoBot AI", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (botActive) {
                    PulsingDot(size = 6.dp)
                    Spacer(Modifier.width(5.dp))
                    Text(
                        if (accountLinked) "Live trading" else "Trading (demo)",
                        color = if (accountLinked) NeonGreen else Color(0xFFFFB020),
                        fontSize = 11.sp
                    )
                } else {
                    Text(
                        if (accountLinked) "Account linked" else "Autonomous trading",
                        color = TextMuted,
                        fontSize = 11.sp
                    )
                }
            }
        }
        Spacer(Modifier.weight(1f))
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(CircleShape)
                .background(SurfaceDark)
                .clickable { onRefresh() },
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Filled.Refresh, contentDescription = "Refresh", tint = TextSecondary, modifier = Modifier.size(20.dp))
        }
    }
}

@Composable
private fun BottomBar(current: Tab, onSelect: (Tab) -> Unit) {
    Column {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(Stroke)
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(SurfaceDark)
                .navigationBarsPadding()
                .padding(vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Tab.entries.forEach { item ->
                val selected = item == current
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(14.dp))
                        .clickable { onSelect(item) }
                        .padding(vertical = 4.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        if (selected) item.filledIcon else item.outlinedIcon,
                        contentDescription = item.title,
                        tint = if (selected) NeonGreen else TextMuted,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        item.title,
                        color = if (selected) NeonGreen else TextMuted,
                        fontSize = 9.sp,
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                    )
                }
            }
        }
    }
}
