package com.travelplanner.repository;

import com.travelplanner.model.ItineraryItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ItineraryRepository extends JpaRepository<ItineraryItem, Long> {
}
