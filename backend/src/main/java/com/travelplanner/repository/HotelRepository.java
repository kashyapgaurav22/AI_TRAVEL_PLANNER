package com.travelplanner.repository;

import com.travelplanner.model.Hotel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface HotelRepository extends JpaRepository<Hotel, Long> {
    Optional<Hotel> findByNameAndLocation(String name, String location);
}
