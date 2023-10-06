package ch.floaty.domain;

import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@RestController
public class FlightController {

    private final FlightRepository flightRepository;
    private final UserRepository userRepository;

    public FlightController(FlightRepository flightRepository, UserRepository userRepository) {
        this.flightRepository = flightRepository;
        this.userRepository = userRepository;
    }

    @GetMapping("/flights")
    public List<Flight> findAllFlight() {
        System.out.println("Find all flight.");
        return (List<Flight>) flightRepository.findAll();
    }

    @GetMapping("/flights/{userId}")
    public List<Flight> findFlightsByUser(@PathVariable Long userId) {
        return userRepository.findById(userId)
                .map(flightRepository::findByUser)
                .orElse(Collections.emptyList());
    }

}
