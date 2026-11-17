package railwayreservation;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

public class Ticket implements Serializable {

    private String pnr;
    private String username;
    private String trainNumber;
    private String trainName;
    private String source;
    private String destination;
    private String passengerName;
    private int age;
    private String gender;
    private int seats;
    private double totalFare;
    private String bookingTime;
    private String journeyDate;
    private String travelClass;
    private String berth;
    private String status; // "CONFIRMED" or "CANCELLED"

    public Ticket(String username,
                  Train train,
                  String passengerName,
                  int age,
                  String gender,
                  int seats) {
        this(username, train, passengerName, age, gender, seats, "Upcoming", "Sleeper (SL)", "No Preference");
    }

    public Ticket(String username,
                  Train train,
                  String passengerName,
                  int age,
                  String gender,
                  int seats,
                  String journeyDate) {
        this(username, train, passengerName, age, gender, seats, journeyDate, "Sleeper (SL)", "No Preference");
    }

    public Ticket(String username,
                  Train train,
                  String passengerName,
                  int age,
                  String gender,
                  int seats,
                  String journeyDate,
                  String travelClass,
                  String berth) {
        this.pnr = "PNR" + UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
        this.username = username;
        this.trainNumber = train.getNumber();
        this.trainName = train.getName();
        this.source = train.getSource();
        this.destination = train.getDestination();
        this.passengerName = passengerName;
        this.age = age;
        this.gender = gender;
        this.seats = seats;
        this.travelClass = (travelClass != null && !travelClass.isBlank()) ? travelClass : "Sleeper (SL)";
        this.berth = (berth != null && !berth.isBlank()) ? berth : "No Preference";
        this.totalFare = calculateClassFare(train.getFare(), this.travelClass) * seats;
        this.bookingTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm"));
        this.journeyDate = (journeyDate != null && !journeyDate.isBlank()) ? journeyDate : "Upcoming";
        this.status = "CONFIRMED";
    }

    // Full constructor for database reconstruction
    public Ticket(String pnr,
                  String username,
                  String trainNumber,
                  String trainName,
                  String source,
                  String destination,
                  String passengerName,
                  int age,
                  String gender,
                  int seats,
                  double totalFare,
                  String bookingTime,
                  String journeyDate,
                  String travelClass,
                  String berth,
                  String status) {
        this.pnr = pnr;
        this.username = username;
        this.trainNumber = trainNumber;
        this.trainName = trainName;
        this.source = source;
        this.destination = destination;
        this.passengerName = passengerName;
        this.age = age;
        this.gender = gender;
        this.seats = seats;
        this.totalFare = totalFare;
        this.bookingTime = bookingTime;
        this.journeyDate = journeyDate;
        this.travelClass = (travelClass != null && !travelClass.isBlank()) ? travelClass : "Sleeper (SL)";
        this.berth = (berth != null && !berth.isBlank()) ? berth : "No Preference";
        this.status = (status != null && !status.isBlank()) ? status : "CONFIRMED";
    }

    public static double calculateClassFare(double baseFare, String travelClass) {
        if (travelClass == null) return baseFare;
        if (travelClass.contains("1A")) return baseFare * 2.2;
        if (travelClass.contains("2A")) return baseFare * 1.6;
        if (travelClass.contains("3A")) return baseFare * 1.25;
        if (travelClass.contains("CC")) return baseFare * 1.15;
        return baseFare; // Sleeper SL
    }

    public String getPnr() {
        return pnr;
    }

    public String getUsername() {
        return username;
    }

    public String getTrainNumber() {
        return trainNumber;
    }

    public String getTrainName() {
        return trainName;
    }

    public String getSource() {
        return source;
    }

    public String getDestination() {
        return destination;
    }

    public String getPassengerName() {
        return passengerName;
    }

    public int getAge() {
        return age;
    }

    public String getGender() {
        return gender;
    }

    public int getSeats() {
        return seats;
    }

    public double getTotalFare() {
        return totalFare;
    }

    public String getBookingTime() {
        return bookingTime;
    }

    public String getJourneyDate() {
        return journeyDate != null ? journeyDate : "-";
    }

    public void setJourneyDate(String journeyDate) {
        this.journeyDate = journeyDate;
    }

    public String getTravelClass() {
        return travelClass != null ? travelClass : "Sleeper (SL)";
    }

    public void setTravelClass(String travelClass) {
        this.travelClass = travelClass;
    }

    public String getBerth() {
        return berth != null ? berth : "No Preference";
    }

    public void setBerth(String berth) {
        this.berth = berth;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public boolean isCancelled() {
        return "CANCELLED".equalsIgnoreCase(status);
    }

    public void setCancelled(boolean cancelled) {
        this.status = cancelled ? "CANCELLED" : "CONFIRMED";
    }
}
