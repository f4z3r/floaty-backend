package ch.floaty.controller;

import ch.floaty.domain.*;
import ch.floaty.generated.FlightDto;
import org.modelmapper.ModelMapper;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;

@RestController
public class FlightController {
    private final IFlightRepository flightRepository;
    private final IUserRepository userRepository;
    private ModelMapper modelMapper = new ModelMapper();

    public FlightController(IFlightRepository flightRepository, IUserRepository userRepository) {
        this.flightRepository = flightRepository;
        this.userRepository = userRepository;
    }

    @GetMapping("/flights")
    public List<FlightDto> findAllFlight() {
        System.out.println("Find all flight.");
        Iterable<Flight> flights = flightRepository.findAll();
        return StreamSupport.stream(flights.spliterator(), false)
                .map(flight -> modelMapper.map(flight, FlightDto.class))
                .collect(toList());
    }

    @GetMapping("/flights/{userId}")
    public List<FlightDto> findFlightsByUser(@PathVariable Long userId) {
        return userRepository.findById(userId)
                .map(user -> {
                    List<Flight> flights = flightRepository.findByUser(user);
                    return flights.stream()
                            .map(flight -> modelMapper.map(flight, FlightDto.class))
                            .collect(Collectors.toList());
                })
                .orElse(emptyList());
    }

    @PostMapping("/flights")
    public FlightDto saveFlight(@Validated @RequestBody FlightDto flightDto) {
        System.out.println("Save flight.");
        User user = new User();
        user.setId(flightDto.getUserId());
        Flight flight = new Flight(user, flightDto.getTakeoff(), flightDto.getDuration(), flightDto.getDate());
        Long flightId = ((List<Flight>) flightRepository.findAll()).stream().map(Flight::getId).max(Long::compareTo).orElse(0L) + 1;
        flight.setId(flightId);
        return modelMapper.map(flightRepository.save(flight), FlightDto.class);
    }

}
