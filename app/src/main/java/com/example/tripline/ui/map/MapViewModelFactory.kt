package com.example.tripline.ui.map

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.tripline.data.repository.ExchangeRepository
import com.example.tripline.data.repository.MapRepository
import com.example.tripline.ui.exchange.ExchangeViewModel
import com.google.android.gms.maps.MapView

class MapViewModelFactory (private val mapRepository: MapRepository) : ViewModelProvider.Factory {
    // ViewModel 객체를 생성하는 함수를 재정의
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        // 생성하려는 클래스가 FoodViewModel 일 경우 객체 생성
        if (modelClass.isAssignableFrom(MapViewModel::class.java)) {
            return MapViewModel(mapRepository) as T
        }
        return IllegalArgumentException("Unknown ViewModel class") as T
    }

}

