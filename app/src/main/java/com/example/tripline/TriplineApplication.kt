package com.example.tripline

import android.app.Application
import com.example.tripline.data.database.AppDatabase
import com.example.tripline.data.map.MapService
import com.example.tripline.data.network.ExchangeService
import com.example.tripline.data.repository.ExchangeRepository
import com.example.tripline.data.repository.ExpenseRepository
import com.example.tripline.data.repository.IncomeRepository
import com.example.tripline.data.repository.MapRepository

class TriplineApplication : Application() {
    // Lazy initialization for database
    val database by lazy {
        AppDatabase.getDatabase(this)
    }

    // Lazy initialization for repositories
    val expenseRepository by lazy {
        ExpenseRepository(database.expenseDao())
    }

    val incomeRepository by lazy {
        IncomeRepository(database.incomeDao())
    }

    val exchangeService by lazy {
        ExchangeService(this)
    }

    val exchangeRepository by lazy {
        ExchangeRepository(exchangeService)
    }

    val mapService by lazy {
        MapService(this)
    }

    val mapRepository by lazy {
        MapRepository(mapService)
    }
}