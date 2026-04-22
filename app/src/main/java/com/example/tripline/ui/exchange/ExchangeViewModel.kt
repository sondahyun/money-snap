package com.example.tripline.ui.exchange

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tripline.data.network.Exchange
import com.example.tripline.data.repository.ExchangeRepository
import java.text.DecimalFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlinx.coroutines.launch


class ExchangeViewModel (val exchangeRepository: ExchangeRepository) : ViewModel() {

    // DB 관련 참고 부분
    /*
    // Flow 를 사용하여 지속 관찰
    val allRefs : LiveData<List<RefEntity>> = refRepository.allRefs.asLiveData()

    // one-shot 결과를 확인하고자 할 때 사용
    private var _name = MutableLiveData<String>()
    val nameData = _name

    // viewModelScope 는 Dispatcher.Main 이므로 긴시간이 걸리는 IO 작업은 Dispatchers.IO 에서 작업
    fun findName(id: Int) = viewModelScope.launch {
        var result : String
        withContext(Dispatchers.IO) {
            result = refRepository.getNameById(id)
        }
        _name.value = result
    }
    */

    private val _exchangeItems = MutableLiveData<List<ExchangeRateItemUiModel>>()
    val exchangeItems: LiveData<List<ExchangeRateItemUiModel>> = _exchangeItems

    fun getExchanges(authkey: String, searchdate: String, data: String) = viewModelScope.launch {
        try {
            Log.d("MoneyFragment", "ExchangeViewModel 입장 성공")
            val currentExchanges = exchangeRepository.getExchanges(authkey, searchdate, data).orEmpty()
            if (currentExchanges.isNotEmpty()) {
                val previousBusinessDayData = findPreviousBusinessDayExchanges(authkey, searchdate, data)
                val previousExchangeMap = previousBusinessDayData?.second
                    ?.associateBy { it.curUnit.trim() }
                    .orEmpty()
                val comparisonLabel = previousBusinessDayData?.first?.format(COMPARISON_LABEL_FORMATTER)
                _exchangeItems.value = currentExchanges.map { exchange ->
                    buildUiModel(
                        exchange = exchange,
                        previousExchange = previousExchangeMap[exchange.curUnit.trim()],
                        comparisonLabel = comparisonLabel,
                    )
                }
                Log.d("MoneyFragment", "환율 정보 불러오기 성공: ${currentExchanges.size}개의 데이터")
            } else {
                _exchangeItems.value = emptyList()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            _exchangeItems.value = emptyList()
        }
    }

    private suspend fun findPreviousBusinessDayExchanges(
        authkey: String,
        searchdate: String,
        data: String,
    ): Pair<LocalDate, List<Exchange>>? {
        val selectedDate = LocalDate.parse(searchdate, API_DATE_FORMATTER)
        for (offset in 1..7) {
            val candidateDate = selectedDate.minusDays(offset.toLong())
            val candidateExchanges = exchangeRepository.getExchanges(
                authkey = authkey,
                searchdate = candidateDate.format(API_DATE_FORMATTER),
                data = data,
            ).orEmpty()

            if (candidateExchanges.isNotEmpty()) {
                return candidateDate to candidateExchanges
            }
        }
        return null
    }

    private fun buildUiModel(
        exchange: Exchange,
        previousExchange: Exchange?,
        comparisonLabel: String?,
    ): ExchangeRateItemUiModel {
        val currentRate = exchange.dealBasR.toRateValue()
        val previousRate = previousExchange?.dealBasR?.toRateValue()

        if (currentRate == null || previousRate == null) {
            return ExchangeRateItemUiModel(
                currencyCode = exchange.curUnit,
                currencyName = exchange.curNm,
                currentRateText = exchange.dealBasR,
                changeValueText = "",
                changePercentText = null,
                comparisonLabelText = "매매 기준율",
                changeDirection = ChangeDirection.NONE,
            )
        }

        val difference = currentRate - previousRate
        val differencePercent = if (previousRate == 0.0) 0.0 else (difference / previousRate) * 100.0
        val direction = when {
            difference > 0.000_001 -> ChangeDirection.UP
            difference < -0.000_001 -> ChangeDirection.DOWN
            else -> ChangeDirection.FLAT
        }

        val changeValueText = when (direction) {
            ChangeDirection.UP -> "▲ ${difference.toSignedNumber()}"
            ChangeDirection.DOWN -> "▼ ${difference.toSignedNumber()}"
            ChangeDirection.FLAT -> "보합"
            ChangeDirection.NONE -> ""
        }

        val changePercentText = when (direction) {
            ChangeDirection.NONE -> null
            ChangeDirection.FLAT -> "0.00%"
            else -> "${differencePercent.toSignedPercent()}%"
        }

        return ExchangeRateItemUiModel(
            currencyCode = exchange.curUnit,
            currencyName = exchange.curNm,
            currentRateText = exchange.dealBasR,
            changeValueText = changeValueText,
            changePercentText = changePercentText,
            comparisonLabelText = comparisonLabel?.let { "전 영업일 ($it) 대비" } ?: "전 영업일 대비",
            changeDirection = direction,
        )
    }

    private fun String.toRateValue(): Double? {
        return replace(",", "").trim().toDoubleOrNull()
    }

    private fun Double.toSignedNumber(): String {
        return "${if (this >= 0) "+" else "-"}${NUMBER_FORMAT.format(kotlin.math.abs(this))}"
    }

    private fun Double.toSignedPercent(): String {
        return String.format(
            Locale.getDefault(),
            "%s%.2f",
            if (this >= 0) "+" else "-",
            kotlin.math.abs(this),
        )
    }

    companion object {
        private val API_DATE_FORMATTER: DateTimeFormatter = DateTimeFormatter.BASIC_ISO_DATE
        private val COMPARISON_LABEL_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern("M.d")
        private val NUMBER_FORMAT = DecimalFormat("#,##0.00")
    }

}
