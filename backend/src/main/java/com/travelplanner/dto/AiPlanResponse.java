package com.travelplanner.dto;

import java.util.ArrayList;
import java.util.List;

public class AiPlanResponse {

    private String destination;
    private Double estimatedCost;
    private List<String> itinerary = new ArrayList<>();
    private List<HotelDto> hotels = new ArrayList<>();
    private List<AttractionDto> attractions = new ArrayList<>();

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

    public List<HotelDto> getHotels() {
        return hotels;
    }

    public void setHotels(List<HotelDto> hotels) {
        this.hotels = hotels;
    }

    public List<AttractionDto> getAttractions() {
        return attractions;
    }

    public void setAttractions(List<AttractionDto> attractions) {
        this.attractions = attractions;
    }

    public static class HotelDto {
        private String name;
        private String location;
        private Double price;
        private Double rating;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getLocation() {
            return location;
        }

        public void setLocation(String location) {
            this.location = location;
        }

        public Double getPrice() {
            return price;
        }

        public void setPrice(Double price) {
            this.price = price;
        }

        public Double getRating() {
            return rating;
        }

        public void setRating(Double rating) {
            this.rating = rating;
        }
    }

    public static class AttractionDto {
        private String name;
        private String category;
        private String location;
        private Double rating;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getCategory() {
            return category;
        }

        public void setCategory(String category) {
            this.category = category;
        }

        public String getLocation() {
            return location;
        }

        public void setLocation(String location) {
            this.location = location;
        }

        public Double getRating() {
            return rating;
        }

        public void setRating(Double rating) {
            this.rating = rating;
        }
    }
}
