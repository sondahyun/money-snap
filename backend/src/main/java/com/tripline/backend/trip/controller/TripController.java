package com.tripline.backend.trip.controller;

import com.tripline.backend.global.response.ApiResponse;
import com.tripline.backend.trip.dto.response.GetTripDetailResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/trips")
public class TripController {

    @GetMapping
    public ResponseEntity<ApiResponse<GetTripDetailResponse>> getTripDetail() {
        GetTripDetailResponse response = new GetTripDetailResponse("sample-trip-id", "제주도 2박 3일", "2026-05-01",
            "planned");

        return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.ok(200, "여행 상세 조회 성공", response));
    }
}
