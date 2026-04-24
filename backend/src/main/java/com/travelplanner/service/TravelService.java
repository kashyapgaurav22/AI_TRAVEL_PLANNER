package com.travelplanner.service;

import com.travelplanner.dto.*;
import com.travelplanner.model.Attraction;
import com.travelplanner.model.Hotel;
import com.travelplanner.model.ItineraryItem;
import com.travelplanner.model.Trip;
import com.travelplanner.model.User;
import com.travelplanner.repository.AttractionRepository;
import com.travelplanner.repository.HotelRepository;
import com.travelplanner.repository.TripRepository;
import com.travelplanner.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class TravelService {

    private final TripRepository tripRepository;
    private final UserRepository userRepository;
    private final HotelRepository hotelRepository;
    private final AttractionRepository attractionRepository;
    private final LlmPlannerService llmPlannerService;

    public TravelService(TripRepository tripRepository,
                         UserRepository userRepository,
                         HotelRepository hotelRepository,
                         AttractionRepository attractionRepository,
                         LlmPlannerService llmPlannerService) {
        this.tripRepository = tripRepository;
        this.userRepository = userRepository;
        this.hotelRepository = hotelRepository;
        this.attractionRepository = attractionRepository;
        this.llmPlannerService = llmPlannerService;
    }

    @Transactional
    public TravelPlanResponse planTrip(String email, TravelPlanRequest request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        AiPlanResponse aiResponse = llmPlannerService.generatePlan(
            request.getDestination(),
            request.getBudget(),
            request.getDuration(),
            request.getInterests()
        );

        Trip trip = new Trip();
        trip.setUser(user);
        trip.setDestination(aiResponse.getDestination());
        trip.setBudget(request.getBudget());
        trip.setDuration(request.getDuration());
        trip.setEstimatedCost(aiResponse.getEstimatedCost());
        trip.setCreatedAt(LocalDateTime.now());
        trip.setInterests(new ArrayList<>(request.getInterests()));

        List<ItineraryItem> itineraryItems = new ArrayList<>();
        for (int i = 0; i < aiResponse.getItinerary().size(); i++) {
            ItineraryItem item = new ItineraryItem();
            item.setTrip(trip);
            item.setDay(i + 1);
            item.setActivity(aiResponse.getItinerary().get(i));
            itineraryItems.add(item);
        }
        trip.setItineraryItems(itineraryItems);

        Set<Hotel> hotels = new HashSet<>();
        for (AiPlanResponse.HotelDto hotelDto : aiResponse.getHotels()) {
            Hotel hotel = hotelRepository.findByNameAndLocation(hotelDto.getName(), hotelDto.getLocation())
                    .orElseGet(() -> {
                        Hotel created = new Hotel();
                        created.setName(hotelDto.getName());
                        created.setLocation(hotelDto.getLocation());
                        created.setPrice(hotelDto.getPrice());
                        created.setRating(hotelDto.getRating());
                        return hotelRepository.save(created);
                    });
            hotels.add(hotel);
        }
        trip.setHotels(hotels);

        Set<Attraction> attractions = new HashSet<>();
        for (AiPlanResponse.AttractionDto attractionDto : aiResponse.getAttractions()) {
            Attraction attraction = attractionRepository.findByNameAndLocation(attractionDto.getName(), attractionDto.getLocation())
                    .orElseGet(() -> {
                        Attraction created = new Attraction();
                        created.setName(attractionDto.getName());
                        created.setCategory(attractionDto.getCategory());
                        created.setLocation(attractionDto.getLocation());
                        created.setRating(attractionDto.getRating());
                        return attractionRepository.save(created);
                    });
            attractions.add(attraction);
        }
        trip.setAttractions(attractions);

        Trip savedTrip = tripRepository.save(trip);

        TravelPlanResponse response = new TravelPlanResponse();
        response.setTripId(savedTrip.getId());
        response.setDestination(aiResponse.getDestination());
        response.setEstimatedCost(aiResponse.getEstimatedCost());
        response.setItinerary(aiResponse.getItinerary());
        response.setHotels(aiResponse.getHotels());
        response.setAttractions(aiResponse.getAttractions());

        return response;
    }

    @Transactional(readOnly = true)
    public List<TripHistoryResponse> getHistory(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        List<Trip> trips = tripRepository.findByUserIdOrderByCreatedAtDesc(user.getId());
        List<TripHistoryResponse> responses = new ArrayList<>();

        for (Trip trip : trips) {
            TripHistoryResponse response = new TripHistoryResponse();
            response.setTripId(trip.getId());
            response.setDestination(trip.getDestination());
            response.setBudget(trip.getBudget());
            response.setDuration(trip.getDuration());
            response.setEstimatedCost(trip.getEstimatedCost());
            response.setCreatedAt(trip.getCreatedAt());
            response.setInterests(trip.getInterests());

            List<String> itinerary = trip.getItineraryItems().stream()
                    .sorted((a, b) -> a.getDay().compareTo(b.getDay()))
                    .map(ItineraryItem::getActivity)
                    .toList();
            response.setItinerary(itinerary);

            responses.add(response);
        }

        return responses;
    }

    @Transactional
    public TravelPlanResponse updateTrip(String email, TripUpdateRequest request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        Trip trip = tripRepository.findById(request.getTripId())
                .orElseThrow(() -> new IllegalArgumentException("Trip not found"));

        if (!trip.getUser().getId().equals(user.getId())) {
            throw new IllegalArgumentException("Trip does not belong to user");
        }

        AiPlanResponse aiResponse = llmPlannerService.generatePlan(
            request.getDestination(),
            request.getBudget(),
            request.getDuration(),
            request.getInterests()
        );

        trip.setDestination(aiResponse.getDestination());
        trip.setBudget(request.getBudget());
        trip.setDuration(request.getDuration());
        trip.setEstimatedCost(aiResponse.getEstimatedCost());
        trip.setInterests(new ArrayList<>(request.getInterests()));

        trip.getItineraryItems().clear();
        for (int i = 0; i < aiResponse.getItinerary().size(); i++) {
            ItineraryItem item = new ItineraryItem();
            item.setTrip(trip);
            item.setDay(i + 1);
            item.setActivity(aiResponse.getItinerary().get(i));
            trip.getItineraryItems().add(item);
        }

        trip.getHotels().clear();
        for (AiPlanResponse.HotelDto hotelDto : aiResponse.getHotels()) {
            Hotel hotel = hotelRepository.findByNameAndLocation(hotelDto.getName(), hotelDto.getLocation())
                    .orElseGet(() -> {
                        Hotel created = new Hotel();
                        created.setName(hotelDto.getName());
                        created.setLocation(hotelDto.getLocation());
                        created.setPrice(hotelDto.getPrice());
                        created.setRating(hotelDto.getRating());
                        return hotelRepository.save(created);
                    });
            trip.getHotels().add(hotel);
        }

        trip.getAttractions().clear();
        for (AiPlanResponse.AttractionDto attractionDto : aiResponse.getAttractions()) {
            Attraction attraction = attractionRepository.findByNameAndLocation(attractionDto.getName(), attractionDto.getLocation())
                    .orElseGet(() -> {
                        Attraction created = new Attraction();
                        created.setName(attractionDto.getName());
                        created.setCategory(attractionDto.getCategory());
                        created.setLocation(attractionDto.getLocation());
                        created.setRating(attractionDto.getRating());
                        return attractionRepository.save(created);
                    });
            trip.getAttractions().add(attraction);
        }

        Trip savedTrip = tripRepository.save(trip);

        TravelPlanResponse response = new TravelPlanResponse();
        response.setTripId(savedTrip.getId());
        response.setDestination(savedTrip.getDestination());
        response.setEstimatedCost(savedTrip.getEstimatedCost());
        response.setItinerary(aiResponse.getItinerary());
        response.setHotels(aiResponse.getHotels());
        response.setAttractions(aiResponse.getAttractions());

        return response;
    }

}
