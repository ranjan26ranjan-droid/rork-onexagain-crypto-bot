package com.rork.cryptobotai.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.LinkOff
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Wallet
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.rork.cryptobotai.data.model.BinanceBalance
import com.rork.cryptobotai.ui.AccountUiState
import com.rork.cryptobotai.ui.AccountViewModel
import com.rork.cryptobotai.ui.components.GlassCard
import com.rork.cryptobotai.ui.theme.Background
import com.rork.cryptobotai.ui.theme.BearRed
import com.rork.cryptobotai.ui.theme.BullGreen
import com.rork.cryptobotai.ui.theme.NeonCyan
import com.rork.cryptobotai.ui.theme.NeonGreen
import com.rork.cryptobotai.ui.theme.Stroke
import com.rork.cryptobotai.ui.theme.SurfaceCard
import com.rork.cryptobotai.ui.theme.SurfaceDark
import com.rork.cryptobotai.ui.theme.SurfaceElevated
import com.rork.cryptobotai.ui.theme.TextMuted
import com.rork.cryptobotai.ui.theme.TextPrimary
import com.rork.cryptobotai.ui.theme.TextSecondary

@Composable
fun AccountScreen(
    state: AccountUiState,
    onLink: () -> Unit,
    onUnlink: () -> Unit,
    onRefresh: () -> Unit,
    onSetKey: (String) -> Unit,
    onSetSecret: (String) -> Unit,
    onDismissMessages: () -> Unit,
    onInit: (android.content.Context) -> Unit
) {
    val context = LocalContext.current
    LaunchedEffect(Unit) { onInit(context) }

    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(16.dp, 8.dp, 16.dp, 24.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // ── Status banner ──
        item {
            AnimatedVisibility(
                visible = state.successMessage != null,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                StatusBanner(state.successMessage ?: "", isError = false, onDismiss = onDismissMessages)
            }
        }
        item {
            AnimatedVisibility(
                visible = state.error != null,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                StatusBanner(state.error ?: "", isError = true, onDismiss = onDismissMessages)
            }
        }

        // ── Connection status card ──
        item {
            ConnectionCard(state, onRefresh)
        }

        if (!state.isLinked) {
            // ── Link account form ──
            item { LinkAccountForm(state, onSetKey, onSetSecret, onLink) }
        } else {
            // ── Balances ──
            if (state.balances.isNotEmpty()) {
                item {
                    Spacer(Modifier.height(4.dp))
                    Text("Wallet Balances", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                }
                item { BalanceHeader() }
                items(state.balances, key = { it.asset }) { balance ->
                    BalanceRow(balance)
                }
            }

            // ── Unlink button ──
            item {
                Spacer(Modifier.height(8.dp))
                Button(
                    onClick = onUnlink,
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = BearRed.copy(alpha = 0.15f)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, BearRed.copy(alpha = 0.3f))
                ) {
                    Icon(Icons.Filled.LinkOff, contentDescription = null, tint = BearRed, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Unlink Account", color = BearRed, fontWeight = FontWeight.SemiBold)
                }
            }
        }

        // ── QR Code to download app ──
        item {
            Spacer(Modifier.height(8.dp))
            QrCodeSection()
        }
    }
}

@Composable
private fun StatusBanner(message: String, isError: Boolean, onDismiss: () -> Unit) {
    val bg = if (isError) Color(0xFF3D1218) else Color(0xFF0E2A1C)
    val fg = if (isError) BearRed else BullGreen

    Row(
        modifier = Modifier
            .fillMaxWidth().clip(RoundedCornerShape(14.dp))
            .background(bg).padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            if (isError) Icons.Filled.Close else Icons.Filled.CheckCircle,
            contentDescription = null, tint = fg, modifier = Modifier.size(18.dp)
        )
        Spacer(Modifier.width(8.dp))
        Text(message, color = fg, fontSize = 13.sp, modifier = Modifier.weight(1f))
        Icon(Icons.Filled.Close, contentDescription = "Dismiss", tint = TextSecondary,
            modifier = Modifier.size(16.dp).clickable { onDismiss() })
    }
}

@Composable
private fun ConnectionCard(state: AccountUiState, onRefresh: () -> Unit) {
    GlassCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier.size(44.dp).clip(RoundedCornerShape(14.dp))
                    .background(if (state.isLinked) NeonGreen.copy(alpha = 0.12f) else SurfaceElevated),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    if (state.isLinked) Icons.Filled.Link else Icons.Filled.Wallet,
                    contentDescription = null,
                    tint = if (state.isLinked) NeonGreen else TextSecondary,
                    modifier = Modifier.size(22.dp)
                )
            }
            Spacer(Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text("Binance Account", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text(
                    if (state.isLinked) "Connected & verified" else "Not connected",
                    color = if (state.isLinked) NeonGreen else TextMuted, fontSize = 12.sp
                )
            }
            if (state.isLinked) {
                IconButton(onClick = onRefresh, modifier = Modifier.size(36.dp)) {
                    Icon(Icons.Filled.Refresh, contentDescription = "Refresh", tint = TextSecondary,
                        modifier = Modifier.size(18.dp))
                }
            }
        }
    }
}

@Composable
private fun LinkAccountForm(
    state: AccountUiState,
    onSetKey: (String) -> Unit,
    onSetSecret: (String) -> Unit,
    onLink: () -> Unit
) {
    var secretVisible by remember { mutableStateOf(false) }

    GlassCard {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(
                "Link your Binance account to enable real trading powered by AI signals.",
                color = TextSecondary, fontSize = 13.sp
            )

            // API Key
            OutlinedTextField(
                value = state.apiKey,
                onValueChange = onSetKey,
                label = { Text("API Key", fontSize = 12.sp) },
                placeholder = { Text("Paste your Binance API key", color = TextMuted, fontSize = 13.sp) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                colors = darkFieldColors(),
                shape = RoundedCornerShape(12.dp),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
            )

            // Secret Key
            OutlinedTextField(
                value = state.secretKey,
                onValueChange = onSetSecret,
                label = { Text("Secret Key", fontSize = 12.sp) },
                placeholder = { Text("Paste your Binance secret key", color = TextMuted, fontSize = 13.sp) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                visualTransformation = if (secretVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { secretVisible = !secretVisible }) {
                        Icon(
                            if (secretVisible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                            contentDescription = null, tint = TextSecondary, modifier = Modifier.size(20.dp)
                        )
                    }
                },
                colors = darkFieldColors(),
                shape = RoundedCornerShape(12.dp),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(onDone = { onLink() })
            )

            Text(
                "Get your API keys from Binance → Settings → API Management. " +
                        "Enable spot trading permissions only — never enable withdrawals for safety.",
                color = Color(0xFFFFB020), fontSize = 11.sp
            )

            Button(
                onClick = onLink,
                modifier = Modifier.fillMaxWidth().height(50.dp),
                enabled = !state.isLoading && state.apiKey.isNotBlank() && state.secretKey.isNotBlank(),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = NeonGreen,
                    disabledContainerColor = NeonGreen.copy(alpha = 0.2f)
                )
            ) {
                if (state.isLoading) {
                    Text("Verifying...", color = Color(0xFF05080A), fontWeight = FontWeight.Bold)
                } else {
                    Icon(Icons.Filled.Link, contentDescription = null, tint = Color(0xFF05080A), modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Link Account", color = Color(0xFF05080A), fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun BalanceHeader() {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("ASSET", color = TextMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
        Text("FREE", color = TextMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f), textAlign = androidx.compose.ui.text.style.TextAlign.End)
        Text("LOCKED", color = TextMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f), textAlign = androidx.compose.ui.text.style.TextAlign.End)
    }
    Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Stroke.copy(alpha = 0.5f)))
}

@Composable
private fun BalanceRow(balance: BinanceBalance) {
    val free = balance.free.toDoubleOrNull() ?: 0.0
    val locked = balance.locked.toDoubleOrNull() ?: 0.0

    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(balance.asset, color = TextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, modifier = Modifier.weight(1f))
        Text(formatBalance(free), color = TextPrimary, fontSize = 13.sp, modifier = Modifier.weight(1f), textAlign = androidx.compose.ui.text.style.TextAlign.End)
        Text(formatBalance(locked), color = if (locked > 0) TextSecondary else TextMuted, fontSize = 13.sp, modifier = Modifier.weight(1f), textAlign = androidx.compose.ui.text.style.TextAlign.End)
    }
    Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Stroke.copy(alpha = 0.25f)))
}

@Composable
private fun QrCodeSection() {
    val downloadUrl = "https://rork.app/p/7rfwc1hjpm3xdt96bro8r"
    val qrUrl = "https://api.qrserver.com/v1/create-qr-code/?size=220x220&data=${java.net.URLEncoder.encode(downloadUrl, "UTF-8")}&bgcolor=05080A&color=00E676"
    val clipboard = LocalClipboardManager.current

    GlassCard {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.QrCode, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Text("Download CryptoBot AI", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
            Spacer(Modifier.height(4.dp))
            Text("Scan QR to install on your device", color = TextSecondary, fontSize = 12.sp)
            Spacer(Modifier.height(14.dp))

            Box(
                modifier = Modifier.size(220.dp).clip(RoundedCornerShape(18.dp)).background(Color(0xFF0A0A0A)),
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    model = qrUrl,
                    contentDescription = "Download QR Code",
                    modifier = Modifier.size(200.dp)
                )
            }

            Spacer(Modifier.height(14.dp))
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(SurfaceElevated)
                    .clickable { clipboard.setText(AnnotatedString(downloadUrl)) }
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(downloadUrl, color = NeonCyan, fontSize = 11.sp, modifier = Modifier.weight(1f, fill = false))
                Spacer(Modifier.width(8.dp))
                Icon(Icons.Filled.ContentCopy, contentDescription = "Copy", tint = TextSecondary, modifier = Modifier.size(14.dp))
            }
            Spacer(Modifier.height(4.dp))
            Text("Tap to copy link", color = TextMuted, fontSize = 10.sp)
        }
    }
}

@Composable
private fun darkFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedTextColor = TextPrimary,
    unfocusedTextColor = TextPrimary,
    cursorColor = NeonGreen,
    focusedBorderColor = NeonGreen,
    unfocusedBorderColor = Stroke,
    focusedLabelColor = NeonGreen,
    unfocusedLabelColor = TextMuted,
    focusedContainerColor = SurfaceDark,
    unfocusedContainerColor = SurfaceDark
)

private fun formatBalance(value: Double): String {
    return when {
        value >= 1000 -> "%,.2f".format(value)
        value >= 1 -> "%,.4f".format(value)
        value > 0 -> "%,.6f".format(value)
        else -> "0.00"
    }
}
