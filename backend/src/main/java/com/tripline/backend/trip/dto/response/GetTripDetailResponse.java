package com.tripline.backend.trip.dto.response;

public record GetTripDetailResponse(
        String tripId,
        String title,
        String startDate,
        String status
) {
}
