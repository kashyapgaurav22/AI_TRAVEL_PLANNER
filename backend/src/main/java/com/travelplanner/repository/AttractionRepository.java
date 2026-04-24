package com.travelplanner.repository;

import com.travelplanner.model.Attraction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AttractionRepository extends JpaRepository<Attraction, Long> {
    Optional<Attraction> findByNameAndLocation(String name, String location);
}
