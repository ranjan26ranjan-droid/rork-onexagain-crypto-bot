package com.rork.cryptobotai.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Response from Binance GET /api/v3/account — authenticated. */
@Serializable
data class BinanceAccountInfo(
    @SerialName("makerCommission") val makerCommission: Int = 0,
    @SerialName("takerCommission") val takerCommission: Int = 0,
    @SerialName("buyerCommission") val buyerCommission: Int = 0,
    @SerialName("sellerCommission") val sellerCommission: Int = 0,
    @SerialName("canTrade") val canTrade: Boolean = false,
    @SerialName("canWithdraw") val canWithdraw: Boolean = false,
    @SerialName("canDeposit") val canDeposit: Boolean = false,
    @SerialName("updateTime") val updateTime: Long = 0,
    @SerialName("accountType") val accountType: String = "",
    val balances: List<BinanceBalance> = emptyList()
)

@Serializable
data class BinanceBalance(
    val asset: String,
    val free: String,
    val locked: String
)

/** Response from Binance POST /api/v3/order — place an order. */
@Serializable
data class BinanceOrderResult(
    val symbol: String = "",
    @SerialName("orderId") val orderId: Long = 0,
    @SerialName("clientOrderId") val clientOrderId: String = "",
    @SerialName("transactTime") val transactTime: Long = 0,
    val price: String = "",
    @SerialName("origQty") val origQty: String = "",
    @SerialName("executedQty") val executedQty: String = "",
    @SerialName("cummulativeQuoteQty") val cummulativeQuoteQty: String = "",
    val status: String = "",
    val type: String = "",
    val side: String = "",
    val fills: List<BinanceFill> = emptyList()
)

@Serializable
data class BinanceFill(
    val price: String = "",
    val qty: String = "",
    val commission: String = "",
    @SerialName("commissionAsset") val commissionAsset: String = ""
)
