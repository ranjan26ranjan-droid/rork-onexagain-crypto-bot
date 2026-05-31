package com.rork.cryptobotai.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** A market coin with live price data from CoinGecko. */
@Serializable
data class Coin(
    val id: String,
    val symbol: String,
    val name: String,
    @SerialName("image") val image: String = "",
    @SerialName("current_price") val currentPrice: Double = 0.0,
    @SerialName("market_cap_rank") val rank: Int = 0,
    @SerialName("price_change_percentage_24h") val change24h: Double = 0.0,
    @SerialName("total_volume") val volume: Double = 0.0,
    @SerialName("sparkline_in_7d") val sparkline: Sparkline? = null
) {
    val sparklinePoints: List<Double>
        get() = sparkline?.price ?: emptyList()
}

@Serializable
data class Sparkline(
    val price: List<Double> = emptyList()
)
