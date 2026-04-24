package com.travelplanner.dto;

import java.util.ArrayList;
import java.util.List;

public class TravelPlanResponse {

    private Long tripId;
    private String destination;
    private Double estimatedCost;
    private List<String> itinerary = new ArrayList<>();
    private List<AiPlanResponse.HotelDto> hotels = new ArrayList<>();
    private List<AiPlanResponse.AttractionDto> attractions = new ArrayList<>();

    public Long getTripId() {
        return tripId;
    }

    public void setTripId(Long tripId) {
        this.tripId = tripId;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public Double getEstimatedCost() {
        return estimatedCost;
    }

    public void setEstimatedCost(Double estimatedCost) {
        this.estimatedCost = estimatedCost;
    }

    public List<String> getItinerary() {
        return itinerary;
    }

    public void setItinerary(List<String> itinerary) {
        this.itinerary = itinerary;
    }

    public List<AiPlanResponse.HotelDto> getHotels() {
        return hotels;
    }

    public void setHotels(List<AiPlanResponse.HotelDto> hotels) {
        this.hotels = hotels;
    }

    public List<AiPlanResponse.AttractionDto> getAttractions() {
        return attractions;
    }

    public void setAttractions(List<AiPlanResponse.AttractionDto> attractions) {
        this.attractions = attractions;
    }
}
