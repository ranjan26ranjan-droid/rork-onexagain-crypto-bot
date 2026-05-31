package com.rork.cryptobotai.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Response from Binance GET /api/v3/ticker/24hr — 24-hour rolling window price stats. */
@Serializable
data class BinanceTicker(
    val symbol: String,
    @SerialName("priceChange") val priceChange: String,
    @SerialName("priceChangePercent") val priceChangePercent: String,
    @SerialName("lastPrice") val lastPrice: String,
    @SerialName("highPrice") val highPrice: String,
    @SerialName("lowPrice") val lowPrice: String,
    @SerialName("volume") val volume: String,
    @SerialName("quoteVolume") val quoteVolume: String
)

/** Single candlestick from GET /api/v3/klines. */
typealias BinanceKline = List<Any>

/**
 * Map of top 40 crypto symbols → human names.
 * Binance only gives symbols like "BTCUSDT" — we expand to readable names here.
 */
val coinNameMap: Map<String, String> = mapOf(
    "BTC" to "Bitcoin", "ETH" to "Ethereum", "BNB" to "BNB", "SOL" to "Solana",
    "XRP" to "XRP", "ADA" to "Cardano", "DOGE" to "Dogecoin", "AVAX" to "Avalanche",
    "DOT" to "Polkadot", "LINK" to "Chainlink", "MATIC" to "Polygon", "UNI" to "Uniswap",
    "SHIB" to "Shiba Inu", "LTC" to "Litecoin", "ATOM" to "Cosmos", "NEAR" to "NEAR Protocol",
    "APT" to "Aptos", "ARB" to "Arbitrum", "OP" to "Optimism", "INJ" to "Injective",
    "SUI" to "Sui", "SEI" to "Sei", "TIA" to "Celestia", "RUNE" to "THORChain",
    "PEPE" to "Pepe", "WIF" to "dogwifhat", "BONK" to "Bonk", "JUP" to "Jupiter",
    "ENA" to "Ethena", "STRK" to "StarkNet", "WLD" to "Worldcoin", "RNDR" to "Render",
    "FIL" to "Filecoin", "ICP" to "Internet Computer", "HBAR" to "Hedera", "VET" to "VeChain",
    "GRT" to "The Graph", "AAVE" to "Aave", "ALGO" to "Algorand", "FTM" to "Fantom"
)

/** Generate a coin image URL using the public CoinCap icon CDN. */
fun coinImageUrl(symbol: String): String =
    "https://assets.coincap.io/assets/icons/${symbol.lowercase()}@2x.png"
