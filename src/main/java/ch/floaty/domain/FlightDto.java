package ch.floaty.domain;

import javax.persistence.Id;

public class FlightDto {

    @Id
    private Long id;

    private Long userid;

    private String takeoff;

    private Long duration;

    public String getFlightdate() {
        return flightdate;
    }

    public void setFlightdate(String flightdate) {
        this.flightdate = flightdate;
    }

    private String flightdate;

    public FlightDto(Long id, Long userid, String takeoff, Long duration, String date) {
        this.id = id;
        this.userid = userid;
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

    public Long getUserid() {
        return userid;
    }

    public void setUserID(Long userId) {
        this.userid = userId;
    }

    public String getTakeoff() {
        return takeoff;
    }

    public void setTakeoff(String takeoff) {
        this.takeoff = takeoff;
    }
}

