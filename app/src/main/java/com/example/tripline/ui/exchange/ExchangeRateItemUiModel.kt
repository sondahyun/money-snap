package com.example.tripline.ui.exchange

data class ExchangeRateItemUiModel(
    val currencyCode: String,
    val currencyName: String,
    val currentRateText: String,
    val changeValueText: String,
    val changePercentText: String?,
    val comparisonLabelText: String,
    val changeDirection: ChangeDirection,
)

enum class ChangeDirection {
    UP,
    DOWN,
    FLAT,
    NONE,
}
