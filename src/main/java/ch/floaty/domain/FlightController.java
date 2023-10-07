package ch.floaty.domain;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

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
    public List<FlightDto> findFlightsByUser(@PathVariable Long userId) {
        return userRepository.findById(userId)
                .map(user -> {
                    List<Flight> flights = flightRepository.findByUser(user);
                    return flights.stream()
                            .map(flight -> new FlightDto(
                                    flight.getId(),
                                    user.getId(),
                                    flight.getTakeoff(),
                                    flight.getDuration(),
                                    flight.getFlightdate()))
                            .collect(Collectors.toList());
                })
                .orElse(Collections.emptyList());
    }

    @PostMapping("/flights")
    public Flight saveUser(@Validated @RequestBody FlightDto flightdto) {
        System.out.println("Save flight.");
        User user = new User();
        user.setId(flightdto.getUserId());
        Flight flight = new Flight(user, flightdto.getTakeoff(), flightdto.getDuration(), flightdto.getFlightdate());
        Long flightId = ((List<Flight>) flightRepository.findAll()).stream().map(Flight::getId).max(Long::compareTo).orElse(0L) + 1;
        flight.setId(flightId);
        return flightRepository.save(flight);
    }

}
