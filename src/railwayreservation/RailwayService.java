package railwayreservation;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class RailwayService {

    private static final Logger LOGGER = Logger.getLogger(RailwayService.class.getName());
    private final DataStore fallbackDataStore;
    private boolean useDatabase = false;

    public RailwayService() {
        this(new DataStore());
    }

    public RailwayService(DataStore dataStore) {
        this.fallbackDataStore = (dataStore != null) ? dataStore : new DataStore();
        init();
    }

    public boolean init() {
        useDatabase = DBConnection.initializeDatabase();
        return useDatabase;
    }

    public boolean isDatabaseConnected() {
        return useDatabase && DBConnection.isConnected();
    }

    public boolean reconnectDatabase() {
        return init();
    }

    // ============================================================
    // AUTHENTICATION
    // ============================================================

    public User authenticate(String username, String password) {
        if (username == null || password == null) return null;

        if (useDatabase) {
            String sql = "SELECT name, username, password, phone, role FROM users WHERE LOWER(username) = LOWER(?) AND password = ?";
            try (Connection conn = DBConnection.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, username.trim());
                ps.setString(2, password);

                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return new User(
                                rs.getString("name"),
                                rs.getString("username"),
                                rs.getString("password"),
                                rs.getString("phone"),
                                rs.getString("role")
                        );
                    }
                }
            } catch (SQLException e) {
                LOGGER.log(Level.WARNING, "Database auth failed, falling back to memory: " + e.getMessage());
            }
        }

        // In-memory fallback
        for (User user : fallbackDataStore.getUsers()) {
            if (user.getUsername().equalsIgnoreCase(username.trim()) && user.getPassword().equals(password)) {
                return user;
            }
        }
        return null;
    }

    // ============================================================
    // REGISTRATION
    // ============================================================

    public boolean register(String name, String username, String password, String phone) {
        if (name == null || username == null || password == null || phone == null) return false;
        name = name.trim();
        username = username.trim();
        phone = phone.trim();

        if (name.isEmpty() || username.isEmpty() || password.isEmpty()) return false;

        if (useDatabase) {
            String checkSql = "SELECT COUNT(*) FROM users WHERE LOWER(username) = LOWER(?)";
            String insertSql = "INSERT INTO users (name, username, password, phone, role) VALUES (?, ?, ?, ?, 'USER')";

            try (Connection conn = DBConnection.getConnection()) {
                try (PreparedStatement checkPs = conn.prepareStatement(checkSql)) {
                    checkPs.setString(1, username);
                    try (ResultSet rs = checkPs.executeQuery()) {
                        if (rs.next() && rs.getInt(1) > 0) {
                            return false; // username exists
                        }
                    }
                }

                try (PreparedStatement insertPs = conn.prepareStatement(insertSql)) {
                    insertPs.setString(1, name);
                    insertPs.setString(2, username);
                    insertPs.setString(3, password);
                    insertPs.setString(4, phone);
                    int affected = insertPs.executeUpdate();
                    if (affected > 0) {
                        fallbackDataStore.getUsers().add(new User(name, username, password, phone, "USER"));
                        return true;
                    }
                }
            } catch (SQLException e) {
                LOGGER.log(Level.WARNING, "Database registration failed: " + e.getMessage());
            }
        }

        // Fallback in-memory registration
        for (User u : fallbackDataStore.getUsers()) {
            if (u.getUsername().equalsIgnoreCase(username)) {
                return false;
            }
        }
        fallbackDataStore.getUsers().add(new User(name, username, password, phone, "USER"));
        return true;
    }

    // ============================================================
    // TRAINS LIST & SEARCH
    // ============================================================

    public List<Train> getTrains() {
        if (useDatabase) {
            String sql = "SELECT number, name, source, destination, departure, arrival, seats, fare FROM trains ORDER BY number";
            try (Connection conn = DBConnection.getConnection();
                 Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(sql)) {

                List<Train> list = new ArrayList<>();
                while (rs.next()) {
                    list.add(new Train(
                            rs.getString("number"),
                            rs.getString("name"),
                            rs.getString("source"),
                            rs.getString("destination"),
                            rs.getString("departure"),
                            rs.getString("arrival"),
                            rs.getInt("seats"),
                            rs.getDouble("fare")
                    ));
                }
                return list;
            } catch (SQLException e) {
                LOGGER.log(Level.WARNING, "Database getTrains failed, falling back to memory: " + e.getMessage());
            }
        }
        return fallbackDataStore.getTrains();
    }

    public List<Train> searchTrains(String source, String destination) {
        String s = (source != null) ? source.trim() : "";
        String d = (destination != null) ? destination.trim() : "";

        if (useDatabase) {
            StringBuilder sql = new StringBuilder("SELECT number, name, source, destination, departure, arrival, seats, fare FROM trains WHERE 1=1 ");
            List<String> params = new ArrayList<>();

            if (!s.isEmpty() && !s.equalsIgnoreCase("From Station") && !s.equalsIgnoreCase("All")) {
                sql.append("AND LOWER(source) LIKE LOWER(?) ");
                params.add("%" + s + "%");
            }
            if (!d.isEmpty() && !d.equalsIgnoreCase("To Station") && !d.equalsIgnoreCase("All")) {
                sql.append("AND LOWER(destination) LIKE LOWER(?) ");
                params.add("%" + d + "%");
            }
            sql.append("ORDER BY number");

            try (Connection conn = DBConnection.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql.toString())) {
                for (int i = 0; i < params.size(); i++) {
                    ps.setString(i + 1, params.get(i));
                }

                try (ResultSet rs = ps.executeQuery()) {
                    List<Train> list = new ArrayList<>();
                    while (rs.next()) {
                        list.add(new Train(
                                rs.getString("number"),
                                rs.getString("name"),
                                rs.getString("source"),
                                rs.getString("destination"),
                                rs.getString("departure"),
                                rs.getString("arrival"),
                                rs.getInt("seats"),
                                rs.getDouble("fare")
                        ));
                    }
                    return list;
                }
            } catch (SQLException e) {
                LOGGER.log(Level.WARNING, "Database searchTrains failed, falling back to memory: " + e.getMessage());
            }
        }

        // Fallback
        List<Train> result = new ArrayList<>();
        for (Train train : fallbackDataStore.getTrains()) {
            boolean sourceMatch = s.isEmpty() || s.equalsIgnoreCase("From Station") || s.equalsIgnoreCase("All") ||
                    train.getSource().toLowerCase().contains(s.toLowerCase());
            boolean destinationMatch = d.isEmpty() || d.equalsIgnoreCase("To Station") || d.equalsIgnoreCase("All") ||
                    train.getDestination().toLowerCase().contains(d.toLowerCase());

            if (sourceMatch && destinationMatch) {
                result.add(train);
            }
        }
        return result;
    }

    // ============================================================
    // BOOK TICKET (TRANSACTION SAFE)
    // ============================================================

    public Ticket bookTicket(String username,
                             Train train,
                             String passengerName,
                             int age,
                             String gender,
                             int seats) {
        return bookTicket(username, train, passengerName, age, gender, seats, "Upcoming", "Sleeper (SL)", "No Preference");
    }

    public Ticket bookTicket(String username,
                             Train train,
                             String passengerName,
                             int age,
                             String gender,
                             int seats,
                             String journeyDate) {
        return bookTicket(username, train, passengerName, age, gender, seats, journeyDate, "Sleeper (SL)", "No Preference");
    }

    public Ticket bookTicket(String username,
                             Train train,
                             String passengerName,
                             int age,
                             String gender,
                             int seats,
                             String journeyDate,
                             String travelClass,
                             String berth) {

        if (train == null || seats <= 0 || username == null) return null;

        if (useDatabase) {
            String checkSeatSql = "SELECT seats, fare, name, source, destination FROM trains WHERE number = ? FOR UPDATE";
            String updateSeatSql = "UPDATE trains SET seats = seats - ? WHERE number = ? AND seats >= ?";
            String insertTicketSql = "INSERT INTO tickets (pnr, username, train_number, train_name, source, destination, " +
                    "passenger_name, age, gender, seats, total_fare, booking_time, journey_date, travel_class, berth, status) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 'CONFIRMED')";

            Connection conn = null;
            try {
                conn = DBConnection.getConnection();
                conn.setAutoCommit(false);

                int availableSeats = 0;
                String trainName = train.getName();
                String source = train.getSource();
                String destination = train.getDestination();

                try (PreparedStatement psCheck = conn.prepareStatement(checkSeatSql)) {
                    psCheck.setString(1, train.getNumber());
                    try (ResultSet rs = psCheck.executeQuery()) {
                        if (rs.next()) {
                            availableSeats = rs.getInt("seats");
                            trainName = rs.getString("name");
                            source = rs.getString("source");
                            destination = rs.getString("destination");
                        } else {
                            conn.rollback();
                            return null;
                        }
                    }
                }

                if (availableSeats < seats) {
                    conn.rollback();
                    return null; // Not enough seats
                }

                // Decrement seats
                try (PreparedStatement psUpdate = conn.prepareStatement(updateSeatSql)) {
                    psUpdate.setInt(1, seats);
                    psUpdate.setString(2, train.getNumber());
                    psUpdate.setInt(3, seats);
                    int updated = psUpdate.executeUpdate();
                    if (updated == 0) {
                        conn.rollback();
                        return null;
                    }
                }

                // Generate Ticket
                Ticket ticket = new Ticket(username, train, passengerName, age, gender, seats, journeyDate, travelClass, berth);

                // Insert into tickets
                try (PreparedStatement psInsert = conn.prepareStatement(insertTicketSql)) {
                    psInsert.setString(1, ticket.getPnr());
                    psInsert.setString(2, username);
                    psInsert.setString(3, train.getNumber());
                    psInsert.setString(4, trainName);
                    psInsert.setString(5, source);
                    psInsert.setString(6, destination);
                    psInsert.setString(7, passengerName);
                    psInsert.setInt(8, age);
                    psInsert.setString(9, gender);
                    psInsert.setInt(10, seats);
                    psInsert.setDouble(11, ticket.getTotalFare());
                    psInsert.setString(12, ticket.getBookingTime());
                    psInsert.setString(13, ticket.getJourneyDate());
                    psInsert.setString(14, ticket.getTravelClass());
                    psInsert.setString(15, ticket.getBerth());

                    psInsert.executeUpdate();
                }

                conn.commit();
                train.reserve(seats);
                fallbackDataStore.addTicket(ticket);
                return ticket;

            } catch (SQLException e) {
                LOGGER.log(Level.WARNING, "Database booking transaction failed: " + e.getMessage(), e);
                if (conn != null) {
                    try { conn.rollback(); } catch (SQLException ignored) {}
                }
            } finally {
                if (conn != null) {
                    try {
                        conn.setAutoCommit(true);
                        conn.close();
                    } catch (SQLException ignored) {}
                }
            }
        }

        // In-memory booking fallback
        if (!train.reserve(seats)) {
            return null;
        }

        Ticket ticket = new Ticket(username, train, passengerName, age, gender, seats, journeyDate, travelClass, berth);
        fallbackDataStore.addTicket(ticket);
        return ticket;
    }

    // ============================================================
    // CANCEL TICKET (TRANSACTION SAFE)
    // ============================================================

    public boolean cancelTicket(String username, String pnr) {
        if (pnr == null || username == null) return false;

        if (useDatabase) {
            String checkTicketSql = "SELECT train_number, seats, status FROM tickets WHERE pnr = ? " +
                    ("admin".equalsIgnoreCase(username) ? "" : "AND LOWER(username) = LOWER(?) ") +
                    "FOR UPDATE";
            String updateTicketSql = "UPDATE tickets SET status = 'CANCELLED' WHERE pnr = ?";
            String restoreSeatsSql = "UPDATE trains SET seats = seats + ? WHERE number = ?";

            Connection conn = null;
            try {
                conn = DBConnection.getConnection();
                conn.setAutoCommit(false);

                String trainNum = null;
                int seatsToRestore = 0;

                try (PreparedStatement psCheck = conn.prepareStatement(checkTicketSql)) {
                    psCheck.setString(1, pnr.trim());
                    if (!"admin".equalsIgnoreCase(username)) {
                        psCheck.setString(2, username.trim());
                    }
                    try (ResultSet rs = psCheck.executeQuery()) {
                        if (rs.next()) {
                            String status = rs.getString("status");
                            if ("CANCELLED".equalsIgnoreCase(status)) {
                                conn.rollback();
                                return false; // Already cancelled
                            }
                            trainNum = rs.getString("train_number");
                            seatsToRestore = rs.getInt("seats");
                        } else {
                            conn.rollback();
                            return false;
                        }
                    }
                }

                // Update ticket status
                try (PreparedStatement psUpdateTicket = conn.prepareStatement(updateTicketSql)) {
                    psUpdateTicket.setString(1, pnr.trim());
                    psUpdateTicket.executeUpdate();
                }

                // Restore train seats
                try (PreparedStatement psRestore = conn.prepareStatement(restoreSeatsSql)) {
                    psRestore.setInt(1, seatsToRestore);
                    psRestore.setString(2, trainNum);
                    psRestore.executeUpdate();
                }

                conn.commit();

                // Update in-memory copy if present
                for (Ticket t : fallbackDataStore.getTickets()) {
                    if (t.getPnr().equalsIgnoreCase(pnr)) {
                        t.setCancelled(true);
                        break;
                    }
                }
                for (Train tr : fallbackDataStore.getTrains()) {
                    if (tr.getNumber().equals(trainNum)) {
                        tr.release(seatsToRestore);
                        break;
                    }
                }

                return true;

            } catch (SQLException e) {
                LOGGER.log(Level.WARNING, "Database ticket cancellation failed: " + e.getMessage(), e);
                if (conn != null) {
                    try { conn.rollback(); } catch (SQLException ignored) {}
                }
            } finally {
                if (conn != null) {
                    try {
                        conn.setAutoCommit(true);
                        conn.close();
                    } catch (SQLException ignored) {}
                }
            }
        }

        // In-memory fallback
        for (Ticket ticket : fallbackDataStore.getTickets()) {
            if (ticket.getPnr().equalsIgnoreCase(pnr.trim()) &&
                    ("admin".equalsIgnoreCase(username) || ticket.getUsername().equalsIgnoreCase(username.trim())) &&
                    !ticket.isCancelled()) {

                for (Train train : fallbackDataStore.getTrains()) {
                    if (train.getNumber().equals(ticket.getTrainNumber())) {
                        train.release(ticket.getSeats());
                        break;
                    }
                }
                ticket.setCancelled(true);
                return true;
            }
        }
        return false;
    }

    // ============================================================
    // TICKETS LISTS & ADVANCED SEARCH
    // ============================================================

    public List<Ticket> getUserTickets(String username) {
        return searchTickets(username, "", "ALL", false);
    }

    public List<Ticket> getAllTickets() {
        return searchTickets(null, "", "ALL", true);
    }

    public List<Ticket> searchTickets(String username, String keyword, String statusFilter, boolean showAll) {
        String kw = (keyword != null) ? keyword.trim() : "";
        String st = (statusFilter != null) ? statusFilter.trim() : "ALL";

        if (useDatabase) {
            StringBuilder sql = new StringBuilder(
                    "SELECT pnr, username, train_number, train_name, source, destination, " +
                            "passenger_name, age, gender, seats, total_fare, booking_time, journey_date, travel_class, berth, status " +
                            "FROM tickets WHERE 1=1 "
            );
            List<String> params = new ArrayList<>();

            if (!showAll && username != null && !username.isBlank()) {
                sql.append("AND LOWER(username) = LOWER(?) ");
                params.add(username.trim());
            }

            if (!st.equalsIgnoreCase("ALL")) {
                sql.append("AND status = ? ");
                params.add(st.toUpperCase());
            }

            if (!kw.isEmpty()) {
                sql.append("AND (LOWER(pnr) LIKE LOWER(?) OR LOWER(passenger_name) LIKE LOWER(?) OR LOWER(train_number) LIKE LOWER(?) OR LOWER(train_name) LIKE LOWER(?)) ");
                params.add("%" + kw + "%");
                params.add("%" + kw + "%");
                params.add("%" + kw + "%");
                params.add("%" + kw + "%");
            }

            sql.append("ORDER BY created_at DESC, booking_time DESC");

            try (Connection conn = DBConnection.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql.toString())) {

                for (int i = 0; i < params.size(); i++) {
                    ps.setString(i + 1, params.get(i));
                }

                try (ResultSet rs = ps.executeQuery()) {
                    List<Ticket> list = new ArrayList<>();
                    while (rs.next()) {
                        list.add(new Ticket(
                                rs.getString("pnr"),
                                rs.getString("username"),
                                rs.getString("train_number"),
                                rs.getString("train_name"),
                                rs.getString("source"),
                                rs.getString("destination"),
                                rs.getString("passenger_name"),
                                rs.getInt("age"),
                                rs.getString("gender"),
                                rs.getInt("seats"),
                                rs.getDouble("total_fare"),
                                rs.getString("booking_time"),
                                rs.getString("journey_date"),
                                rs.getString("travel_class"),
                                rs.getString("berth"),
                                rs.getString("status")
                        ));
                    }
                    return list;
                }
            } catch (SQLException e) {
                LOGGER.log(Level.WARNING, "Database searchTickets failed: " + e.getMessage());
            }
        }

        // Fallback filter
        List<Ticket> result = new ArrayList<>();
        for (Ticket ticket : fallbackDataStore.getTickets()) {
            boolean userMatch = showAll || (username == null || username.isBlank() || ticket.getUsername().equalsIgnoreCase(username.trim()));
            boolean statusMatch = st.equalsIgnoreCase("ALL") || ticket.getStatus().equalsIgnoreCase(st);
            boolean kwMatch = kw.isEmpty() ||
                    ticket.getPnr().toLowerCase().contains(kw.toLowerCase()) ||
                    ticket.getPassengerName().toLowerCase().contains(kw.toLowerCase()) ||
                    ticket.getTrainNumber().toLowerCase().contains(kw.toLowerCase()) ||
                    ticket.getTrainName().toLowerCase().contains(kw.toLowerCase());

            if (userMatch && statusMatch && kwMatch) {
                result.add(ticket);
            }
        }
        return result;
    }

    public Ticket getTicketByPnr(String pnr) {
        if (pnr == null || pnr.isBlank()) return null;

        if (useDatabase) {
            String sql = "SELECT pnr, username, train_number, train_name, source, destination, " +
                    "passenger_name, age, gender, seats, total_fare, booking_time, journey_date, travel_class, berth, status " +
                    "FROM tickets WHERE LOWER(pnr) = LOWER(?)";

            try (Connection conn = DBConnection.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, pnr.trim());
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return new Ticket(
                                rs.getString("pnr"),
                                rs.getString("username"),
                                rs.getString("train_number"),
                                rs.getString("train_name"),
                                rs.getString("source"),
                                rs.getString("destination"),
                                rs.getString("passenger_name"),
                                rs.getInt("age"),
                                rs.getString("gender"),
                                rs.getInt("seats"),
                                rs.getDouble("total_fare"),
                                rs.getString("booking_time"),
                                rs.getString("journey_date"),
                                rs.getString("travel_class"),
                                rs.getString("berth"),
                                rs.getString("status")
                        );
                    }
                }
            } catch (SQLException e) {
                LOGGER.log(Level.WARNING, "Database getTicketByPnr failed: " + e.getMessage());
            }
        }

        for (Ticket ticket : fallbackDataStore.getTickets()) {
            if (ticket.getPnr().equalsIgnoreCase(pnr.trim())) {
                return ticket;
            }
        }
        return null;
    }

    // ============================================================
    // TRAIN MANAGEMENT (ADMIN)
    // ============================================================

    public boolean addTrain(Train train) {
        if (train == null) return false;

        if (useDatabase) {
            String sql = "INSERT INTO trains (number, name, source, destination, departure, arrival, seats, fare) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
            try (Connection conn = DBConnection.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, train.getNumber());
                ps.setString(2, train.getName());
                ps.setString(3, train.getSource());
                ps.setString(4, train.getDestination());
                ps.setString(5, train.getDeparture());
                ps.setString(6, train.getArrival());
                ps.setInt(7, train.getSeats());
                ps.setDouble(8, train.getFare());
                return ps.executeUpdate() > 0;
            } catch (SQLException e) {
                LOGGER.log(Level.WARNING, "Database addTrain failed: " + e.getMessage());
                return false;
            }
        }

        fallbackDataStore.getTrains().add(train);
        return true;
    }

    public boolean deleteTrain(String trainNumber) {
        if (trainNumber == null) return false;

        if (useDatabase) {
            String sql = "DELETE FROM trains WHERE number = ?";
            try (Connection conn = DBConnection.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, trainNumber.trim());
                return ps.executeUpdate() > 0;
            } catch (SQLException e) {
                LOGGER.log(Level.WARNING, "Database deleteTrain failed: " + e.getMessage());
                return false;
            }
        }

        return fallbackDataStore.getTrains().removeIf(t -> t.getNumber().equalsIgnoreCase(trainNumber.trim()));
    }

    // ============================================================
    // STATS
    // ============================================================

    public int[] getStats(String username) {
        int totalTrains = 0;
        int totalBookings = 0;
        int activeTickets = 0;

        if (useDatabase) {
            try (Connection conn = DBConnection.getConnection()) {
                // Trains
                try (Statement stmt = conn.createStatement();
                     ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM trains")) {
                    if (rs.next()) totalTrains = rs.getInt(1);
                }

                // Bookings
                String bSql = (username != null && !username.isBlank()) ?
                        "SELECT COUNT(*) FROM tickets WHERE LOWER(username) = LOWER(?)" :
                        "SELECT COUNT(*) FROM tickets";
                try (PreparedStatement ps = conn.prepareStatement(bSql)) {
                    if (username != null && !username.isBlank()) ps.setString(1, username.trim());
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) totalBookings = rs.getInt(1);
                    }
                }

                // Active
                String aSql = (username != null && !username.isBlank()) ?
                        "SELECT COUNT(*) FROM tickets WHERE LOWER(username) = LOWER(?) AND status = 'CONFIRMED'" :
                        "SELECT COUNT(*) FROM tickets WHERE status = 'CONFIRMED'";
                try (PreparedStatement ps = conn.prepareStatement(aSql)) {
                    if (username != null && !username.isBlank()) ps.setString(1, username.trim());
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) activeTickets = rs.getInt(1);
                    }
                }

                return new int[]{totalTrains, totalBookings, activeTickets};
            } catch (SQLException e) {
                LOGGER.log(Level.WARNING, "Database getStats failed: " + e.getMessage());
            }
        }

        totalTrains = fallbackDataStore.getTrains().size();
        for (Ticket t : fallbackDataStore.getTickets()) {
            if (username == null || username.isBlank() || t.getUsername().equalsIgnoreCase(username.trim())) {
                totalBookings++;
                if (!t.isCancelled()) activeTickets++;
            }
        }
        return new int[]{totalTrains, totalBookings, activeTickets};
    }
}
