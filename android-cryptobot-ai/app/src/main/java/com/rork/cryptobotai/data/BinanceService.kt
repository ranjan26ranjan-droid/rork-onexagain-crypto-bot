package com.rork.cryptobotai.data

import com.rork.cryptobotai.data.model.BinanceKline
import com.rork.cryptobotai.data.model.BinanceTicker
import com.rork.cryptobotai.data.model.Coin
import com.rork.cryptobotai.data.model.coinImageUrl
import com.rork.cryptobotai.data.model.coinNameMap
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.android.Android
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

/**
 * Fetches live market data from the Binance public REST API.
 * No API key required for market data endpoints.
 *
 * Endpoints used:
 *  - GET /api/v3/ticker/24hr          → 24h price change stats for all pairs
 *  - GET /api/v3/klines               → OHLCV candlesticks (used as sparklines)
 */
class BinanceService {

    private val client = HttpClient(Android) {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                isLenient = true
            })
        }
    }

    /** Fetch all USDT tickers with 24h stats, then map to [Coin] models (top 40 by volume). */
    suspend fun fetchTopCoins(): List<Coin> {
        val allTickers: List<BinanceTicker> = client.get("https://api.binance.com/api/v3/ticker/24hr").body()

        val usdtPairs = allTickers
            .filter { it.symbol.endsWith("USDT") && !it.symbol.endsWith("UPUSDT") && !it.symbol.endsWith("DOWNUSDT") }
            .sortedByDescending { it.quoteVolume.toDoubleOrNull() ?: 0.0 }
            .take(40)

        // Build a list of symbols to fetch sparklines for in parallel
        val sparklineMap = fetchSparklines(usdtPairs.map { it.symbol })

        return usdtPairs.mapIndexed { index, ticker ->
            val baseAsset = ticker.symbol.removeSuffix("USDT")
            val price = ticker.lastPrice.toDoubleOrNull() ?: 0.0
            val change = ticker.priceChangePercent.toDoubleOrNull() ?: 0.0
            val vol = ticker.quoteVolume.toDoubleOrNull() ?: 0.0

            Coin(
                id = baseAsset.lowercase(),
                symbol = baseAsset.lowercase(),
                name = coinNameMap[baseAsset] ?: baseAsset,
                image = coinImageUrl(baseAsset),
                currentPrice = price,
                rank = index + 1,
                change24h = change,
                volume = vol,
                sparkline = sparklineMap[ticker.symbol]
            )
        }
    }

    /**
     * Fetches 1h klines for the last 7 days (168 candles) for each symbol.
     * Maps close prices into our [Sparkline] model for the chart.
     */
    private suspend fun fetchSparklines(symbols: List<String>): Map<String, com.rork.cryptobotai.data.model.Sparkline> {
        val result = mutableMapOf<String, com.rork.cryptobotai.data.model.Sparkline>()
        for (symbol in symbols) {
            try {
                val klines: List<BinanceKline> = client.get("https://api.binance.com/api/v3/klines") {
                    url {
                        parameters.append("symbol", symbol)
                        parameters.append("interval", "4h")
                        parameters.append("limit", "42")
                    }
                }.body()
                val prices = klines.mapNotNull { kline ->
                    (kline.getOrNull(4) as? String)?.toDoubleOrNull()
                }
                if (prices.isNotEmpty()) {
                    result[symbol] = com.rork.cryptobotai.data.model.Sparkline(price = prices)
                }
            } catch (_: Exception) {
                // If klines fail for one symbol, skip it — the UI handles empty sparklines
            }
        }
        return result
    }
}
