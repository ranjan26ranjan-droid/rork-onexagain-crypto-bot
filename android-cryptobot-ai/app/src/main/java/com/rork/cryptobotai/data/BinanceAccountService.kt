package com.rork.cryptobotai.data

import com.rork.cryptobotai.data.model.BinanceAccountInfo
import com.rork.cryptobotai.data.model.BinanceOrderResult
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.android.Android
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

/**
 * Handles authenticated Binance API calls using the user's API key and secret.
 * All private endpoints require HMAC-SHA256 signature of the query string.
 *
 * Endpoints used:
 *  - GET  /api/v3/account   → account balances & permissions
 *  - POST /api/v3/order     → place spot orders (MARKET / LIMIT)
 */
class BinanceAccountService {

    companion object {
        private const val BASE = "https://api.binance.com"
    }

    private val client = HttpClient(Android) {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                isLenient = true
            })
        }
    }

    /** Fetch balances and account info from the user's Binance spot wallet. */
    suspend fun fetchAccount(apiKey: String, secretKey: String): Result<BinanceAccountInfo> = runCatching {
        val query = "timestamp=${System.currentTimeMillis()}"
        val signature = sign(query, secretKey)
        client.get("$BASE/api/v3/account?$query&signature=$signature") {
            header("X-MBX-APIKEY", apiKey)
        }.body<BinanceAccountInfo>()
    }

    /**
     * Place a MARKET buy/sell order on Binance spot.
     * @param apiKey      Binance API key
     * @param secretKey   Binance secret key
     * @param symbol      Trading pair like "BTCUSDT"
     * @param side        "BUY" or "SELL"
     * @param quoteOrderQty  Amount in quote asset (USDT) for MARKET orders
     */
    suspend fun placeMarketOrder(
        apiKey: String,
        secretKey: String,
        symbol: String,
        side: String,
        quoteOrderQty: Double
    ): Result<BinanceOrderResult> = runCatching {
        val timestamp = System.currentTimeMillis()
        val params = "symbol=$symbol&side=$side&type=MARKET&quoteOrderQty=$quoteOrderQty&timestamp=$timestamp"
        val signature = sign(params, secretKey)

        val response = client.post("$BASE/api/v3/order?$params&signature=$signature") {
            header("X-MBX-APIKEY", apiKey)
            contentType(ContentType.Application.Json)
        }

        if (!response.status.isSuccess()) {
            val body = response.bodyAsText()
            throw Exception("Binance order failed: $body")
        }
        response.body<BinanceOrderResult>()
    }

    /**
     * Test connectivity & validate API credentials by pinging the account endpoint.
     * Returns true if the credentials are valid.
     */
    suspend fun validateCredentials(apiKey: String, secretKey: String): Boolean {
        return fetchAccount(apiKey, secretKey).isSuccess
    }

    /** HMAC-SHA256 signature required by Binance for all private endpoints. */
    private fun sign(data: String, secret: String): String {
        val mac = Mac.getInstance("HmacSHA256")
        val spec = SecretKeySpec(secret.toByteArray(), "HmacSHA256")
        mac.init(spec)
        val hash = mac.doFinal(data.toByteArray())
        return hash.joinToString("") { "%02x".format(it) }
    }
}
