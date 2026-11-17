package railwayreservation;

import java.util.ArrayList;
import java.util.List;

public class DataStore {

    private final List<User> users = new ArrayList<>();
    private final List<Train> trains = new ArrayList<>();
    private final List<Ticket> tickets = new ArrayList<>();

    public DataStore() {
        seed();
    }

    public List<User> getUsers() {
        return users;
    }

    public List<Train> getTrains() {
        return trains;
    }

    public List<Ticket> getTickets() {
        return tickets;
    }

    public void addTicket(Ticket ticket) {
        tickets.add(ticket);
    }

    private void seed() {
        users.add(new User("Administrator", "admin", "admin123", "9876543210", "ADMIN"));

        trains.add(new Train("12001", "Rajdhani Express", "Delhi", "Mumbai", "16:55", "08:35", 120, 1650));
        trains.add(new Train("12952", "Mumbai Rajdhani", "Mumbai", "Delhi", "17:00", "08:35", 115, 1620));
        trains.add(new Train("12957", "Swarna Jayanti Express", "Delhi", "Ahmedabad", "19:55", "07:30", 95, 980));
        trains.add(new Train("12413", "Pooja Express", "Ajmer", "Delhi", "14:30", "21:20", 110, 760));
        trains.add(new Train("22987", "Agra Superfast", "Agra", "Jaipur", "07:10", "11:20", 85, 520));
        trains.add(new Train("12916", "Ashram Express", "Delhi", "Ahmedabad", "15:20", "06:10", 125, 1100));
        trains.add(new Train("12955", "Jaipur Superfast", "Jaipur", "Mumbai", "14:35", "07:45", 140, 1450));
        trains.add(new Train("12015", "Ajmer Shatabdi", "New Delhi", "Ajmer", "06:15", "12:40", 100, 1250));
        trains.add(new Train("20977", "Vande Bharat Express", "Ajmer", "Delhi", "06:10", "10:30", 180, 1350));
        trains.add(new Train("20978", "Vande Bharat Express", "Delhi", "Ajmer", "16:10", "20:30", 180, 1350));
        trains.add(new Train("12968", "Jaipur Express", "Jaipur", "Chandigarh", "18:20", "06:40", 105, 920));
        trains.add(new Train("12462", "Mandore Express", "Delhi", "Jodhpur", "21:20", "06:50", 130, 890));
        trains.add(new Train("12915", "Ashram Express", "Ahmedabad", "Delhi", "18:30", "07:20", 120, 1100));
        trains.add(new Train("12414", "Delhi Intercity", "Jaipur", "Delhi", "06:00", "10:30", 150, 650));
        trains.add(new Train("12956", "Jaipur Mumbai Express", "Jaipur", "Mumbai", "20:00", "09:30", 135, 1450));
        trains.add(new Train("12464", "Rajasthan Sampark Kranti", "Delhi", "Jaipur", "17:45", "22:15", 145, 720));
        trains.add(new Train("22996", "Bandikui Superfast", "Jaipur", "Delhi", "08:30", "13:00", 125, 680));
        trains.add(new Train("12963", "Mewar Express", "Nizamuddin", "Udaipur", "19:00", "07:30", 115, 1050));
        trains.add(new Train("12964", "Mewar Express", "Udaipur", "Delhi", "18:00", "06:30", 115, 1050));
        trains.add(new Train("12431", "Rajasthan Rajdhani", "Delhi", "Jodhpur", "20:30", "06:45", 130, 1400));
    }

    public void save() {
        // Maintained in MySQL or in-memory fallback
    }

    public static DataStore load() {
        return new DataStore();
    }
}
