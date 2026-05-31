package com.rork.cryptobotai.data

import com.rork.cryptobotai.data.model.Coin

/**
 * Fetches live crypto market data — Binance is the primary source,
 * with a local demo fallback when the network is unavailable.
 */
class MarketRepository {

    private val binance = BinanceService()

    /** Fetch top 40 USDT pairs from Binance with live prices, 24h change, volume & sparklines. */
    suspend fun fetchMarkets(): List<Coin> {
        return try {
            binance.fetchTopCoins().ifEmpty { demoCoins() }
        } catch (_: Exception) {
            demoCoins()
        }
    }

    /** Fallback demo coins used when the network is unavailable. */
    fun demoCoins(): List<Coin> = listOf(
        Coin("bitcoin", "btc", "Bitcoin", currentPrice = 67432.0, rank = 1, change24h = 2.4, volume = 2.8e10),
        Coin("ethereum", "eth", "Ethereum", currentPrice = 3521.0, rank = 2, change24h = 3.1, volume = 1.4e10),
        Coin("solana", "sol", "Solana", currentPrice = 178.4, rank = 3, change24h = 6.8, volume = 4.2e9),
        Coin("binancecoin", "bnb", "BNB", currentPrice = 612.0, rank = 4, change24h = -1.2, volume = 1.8e9),
        Coin("ripple", "xrp", "XRP", currentPrice = 0.62, rank = 5, change24h = 1.5, volume = 1.1e9),
        Coin("cardano", "ada", "Cardano", currentPrice = 0.45, rank = 6, change24h = -2.3, volume = 6.4e8),
        Coin("dogecoin", "doge", "Dogecoin", currentPrice = 0.16, rank = 7, change24h = 9.2, volume = 1.2e9),
        Coin("avalanche-2", "avax", "Avalanche", currentPrice = 38.1, rank = 8, change24h = 4.7, volume = 5.1e8)
    )
}
