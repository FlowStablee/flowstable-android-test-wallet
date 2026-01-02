package com.antigravity.cryptowallet.data.models

data class MarketChartResponse(
    val prices: List<List<Double>>, // [timestamp, price]
    val market_caps: List<List<Double>>,
    val total_volumes: List<List<Double>>
)

data class CoinInfoResponse(
    val id: String,
    val symbol: String,
    val name: String,
    val description: Description,
    val platforms: Map<String, String>? = null
)

data class Description(
    val en: String
)
