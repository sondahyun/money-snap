package com.example.tripline.data.repository

import android.util.Log
import com.example.tripline.data.network.Exchange
import com.example.tripline.data.network.ExchangeService
import kotlinx.coroutines.flow.Flow

class ExchangeRepository(private val exchangeService: ExchangeService) {

    suspend fun getExchanges(authkey: String, searchdate: String, data: String): List<Exchange>? {
        try {
            Log.d("MoneyFragment", "ExchangeRepository 입장 성공")
            val response = exchangeService.getExchanges(authkey, searchdate, data)
            Log.d("ExchangeRepository", "Response: $response")
            return response
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }

}