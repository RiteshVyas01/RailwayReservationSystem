package railwayreservation;

import java.io.Serializable;

public class Train implements Serializable {

    private String number;
    private String name;
    private String source;
    private String destination;
    private String departure;
    private String arrival;
    private int seats;
    private double fare;

    public Train(String number,
                 String name,
                 String source,
                 String destination,
                 String departure,
                 String arrival,
                 int seats,
                 double fare) {
        this.number = number;
        this.name = name;
        this.source = source;
        this.destination = destination;
        this.departure = departure;
        this.arrival = arrival;
        this.seats = seats;
        this.fare = fare;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public String getDeparture() {
        return departure;
    }

    public void setDeparture(String departure) {
        this.departure = departure;
    }

    public String getArrival() {
        return arrival;
    }

    public void setArrival(String arrival) {
        this.arrival = arrival;
    }

    public int getSeats() {
        return seats;
    }

    public void setSeats(int seats) {
        this.seats = seats;
    }

    public double getFare() {
        return fare;
    }

    public void setFare(double fare) {
        this.fare = fare;
    }

    // Compatibility getters
    public String getTrainNumber() {
        return number;
    }

    public String getFrom() {
        return source;
    }

    public String getTo() {
        return destination;
    }

    public boolean reserve(int count) {
        if (count <= 0 || seats < count) {
            return false;
        }
        seats -= count;
        return true;
    }

    public void release(int count) {
        if (count > 0) {
            seats += count;
        }
    }

    @Override
    public String toString() {
        return number + " - " + name + " (" + source + " -> " + destination + ")";
    }
}
