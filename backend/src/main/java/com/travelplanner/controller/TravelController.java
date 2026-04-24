package com.travelplanner.controller;

import com.travelplanner.dto.TravelPlanRequest;
import com.travelplanner.dto.TravelPlanResponse;
import com.travelplanner.dto.TripHistoryResponse;
import com.travelplanner.dto.TripUpdateRequest;
import com.travelplanner.service.TravelService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/travel")
public class TravelController {

    private final TravelService travelService;

    public TravelController(TravelService travelService) {
        this.travelService = travelService;
    }

    @PostMapping("/plan")
    public ResponseEntity<TravelPlanResponse> planTrip(@Valid @RequestBody TravelPlanRequest request,
                                                       Authentication authentication) {
        return ResponseEntity.ok(travelService.planTrip(authentication.getName(), request));
    }

    @GetMapping("/history")
    public ResponseEntity<List<TripHistoryResponse>> getHistory(Authentication authentication) {
        return ResponseEntity.ok(travelService.getHistory(authentication.getName()));
    }

    @PutMapping("/update")
    public ResponseEntity<TravelPlanResponse> updateTrip(@Valid @RequestBody TripUpdateRequest request,
                                                         Authentication authentication) {
        return ResponseEntity.ok(travelService.updateTrip(authentication.getName(), request));
    }
}
