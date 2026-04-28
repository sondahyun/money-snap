package com.tripline.backend.trip.repository;

import com.tripline.backend.trip.entity.Trip;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TripRepository extends JpaRepository<Trip, String> {
}
