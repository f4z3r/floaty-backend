package ch.floaty.domain;

import javax.persistence.Id;

public class FlightDto {

    @Id
    private Long id;

    private Long userId;

    private String takeoff;

    private Long duration;

    public String getFlightdate() {
        return flightdate;
    }

    public void setFlightdate(String flightdate) {
        this.flightdate = flightdate;
    }

    private String flightdate;

    public FlightDto(Long id, Long userId, String takeoff, Long duration, String date) {
        this.id = id;
        this.userId = userId;
        this.takeoff = takeoff;
        this.duration = duration;
        this.flightdate = date;
    }

    public Long getDuration() {
        return duration;
    }

    public void setDuration(Long duration) {
        this.duration = duration;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserID(Long userId) {
        this.userId = userId;
    }

    public String getTakeoff() {
        return takeoff;
    }

    public void setTakeoff(String takeoff) {
        this.takeoff = takeoff;
    }
}

