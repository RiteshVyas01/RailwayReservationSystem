package railwayreservation;

import java.sql.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DBConnection {

    private static final Logger LOGGER = Logger.getLogger(DBConnection.class.getName());
    private static boolean driverLoaded = false;
    private static boolean connected = false;
    private static String lastError = "";

    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            driverLoaded = true;
        } catch (ClassNotFoundException e) {
            lastError = "MySQL Driver not found in classpath: " + e.getMessage();
            LOGGER.log(Level.SEVERE, lastError, e);
        }
    }

    public static boolean isDriverLoaded() {
        return driverLoaded;
    }

    public static boolean isConnected() {
        return connected;
    }

    public static String getLastError() {
        return lastError;
    }

    public static Connection getConnection() throws SQLException {
        DBConfig config = DBConfig.getInstance();
        return DriverManager.getConnection(config.getJdbcUrl(), config.getUsername(), config.getPassword());
    }

    public static boolean testConnection(String host, int port, String database, String user, String pass) {
        String testUrl = "jdbc:mysql://" + host + ":" + port + "/" + database +
                "?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC&connectTimeout=3000";
        try (Connection conn = DriverManager.getConnection(testUrl, user, pass)) {
            return conn != null && !conn.isClosed();
        } catch (SQLException e) {
            lastError = e.getMessage();
            return false;
        }
    }

    public static synchronized boolean initializeDatabase() {
        DBConfig config = DBConfig.getInstance();
        if (!driverLoaded) {
            lastError = "MySQL JDBC Driver (com.mysql.cj.jdbc.Driver) is missing.";
            connected = false;
            return false;
        }

        // 1. Ensure database exists
        try (Connection serverConn = DriverManager.getConnection(
                config.getServerJdbcUrl() + "&connectTimeout=4000",
                config.getUsername(),
                config.getPassword());
             Statement stmt = serverConn.createStatement()) {

            stmt.executeUpdate("CREATE DATABASE IF NOT EXISTS `" + config.getDatabase() +
                    "` CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci");
        } catch (SQLException e) {
            LOGGER.warning("Could not create database via root connection, trying direct DB connection: " + e.getMessage());
        }

        // 2. Connect to the database and create tables
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {

            // Users Table
            stmt.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS users (" +
                            "id INT AUTO_INCREMENT PRIMARY KEY, " +
                            "name VARCHAR(100) NOT NULL, " +
                            "username VARCHAR(50) NOT NULL UNIQUE, " +
                            "password VARCHAR(100) NOT NULL, " +
                            "phone VARCHAR(20) NOT NULL, " +
                            "role VARCHAR(20) DEFAULT 'USER', " +
                            "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                            ") ENGINE=InnoDB"
            );

            // Trains Table
            stmt.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS trains (" +
                            "number VARCHAR(20) PRIMARY KEY, " +
                            "name VARCHAR(100) NOT NULL, " +
                            "source VARCHAR(100) NOT NULL, " +
                            "destination VARCHAR(100) NOT NULL, " +
                            "departure VARCHAR(20) NOT NULL, " +
                            "arrival VARCHAR(20) NOT NULL, " +
                            "seats INT NOT NULL, " +
                            "fare DOUBLE NOT NULL" +
                            ") ENGINE=InnoDB"
            );

            // Tickets Table
            stmt.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS tickets (" +
                            "pnr VARCHAR(30) PRIMARY KEY, " +
                            "username VARCHAR(50) NOT NULL, " +
                            "train_number VARCHAR(20) NOT NULL, " +
                            "train_name VARCHAR(100) NOT NULL, " +
                            "source VARCHAR(100) NOT NULL, " +
                            "destination VARCHAR(100) NOT NULL, " +
                            "passenger_name VARCHAR(100) NOT NULL, " +
                            "age INT NOT NULL, " +
                            "gender VARCHAR(10) NOT NULL, " +
                            "seats INT NOT NULL, " +
                            "total_fare DOUBLE NOT NULL, " +
                            "booking_time VARCHAR(30) NOT NULL, " +
                            "journey_date VARCHAR(20), " +
                            "travel_class VARCHAR(30) DEFAULT 'Sleeper (SL)', " +
                            "berth VARCHAR(30) DEFAULT 'No Preference', " +
                            "status VARCHAR(20) DEFAULT 'CONFIRMED', " +
                            "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                            "FOREIGN KEY (train_number) REFERENCES trains(train_number) ON UPDATE CASCADE" +
                            ") ENGINE=InnoDB"
            );

            // Safe column additions if table was created in an earlier version
            try { stmt.executeUpdate("ALTER TABLE tickets ADD COLUMN travel_class VARCHAR(30) DEFAULT 'Sleeper (SL)'"); } catch (SQLException ignored) {}
            try { stmt.executeUpdate("ALTER TABLE tickets ADD COLUMN berth VARCHAR(30) DEFAULT 'No Preference'"); } catch (SQLException ignored) {}

            // 3. Seed Users if empty
            try (ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM users")) {
                if (rs.next() && rs.getInt(1) == 0) {
                    stmt.executeUpdate(
                            "INSERT INTO users (name, email, password, phone) " +
                                    "VALUES ('Administrator', 'admin@gmail.com', 'admin123', '9876543210')"
                    );
                    ;
                }
            }

            // 4. Seed Trains if empty
            try (ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM trains")) {
                if (rs.next() && rs.getInt(1) == 0) {
                    seedTrains(conn);
                }
            }

            connected = true;
            lastError = "";
            return true;

        } catch (SQLException e) {
            connected = false;
            lastError = e.getMessage();
            LOGGER.log(Level.WARNING, "MySQL initialization failed: " + e.getMessage());
            return false;
        }
    }

    private static void seedTrains(Connection conn) throws SQLException {
        String insertSql = "INSERT INTO trains (number, name, source, destination, departure, arrival, seats, fare) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(insertSql)) {
            Object[][] initialTrains = {
                    {"12001", "Rajdhani Express", "Delhi", "Mumbai", "16:55", "08:35", 120, 1650.0},
                    {"12952", "Mumbai Rajdhani", "Mumbai", "Delhi", "17:00", "08:35", 115, 1620.0},
                    {"12957", "Swarna Jayanti Express", "Delhi", "Ahmedabad", "19:55", "07:30", 95, 980.0},
                    {"12413", "Pooja Express", "Ajmer", "Delhi", "14:30", "21:20", 110, 760.0},
                    {"22987", "Agra Superfast", "Agra", "Jaipur", "07:10", "11:20", 85, 520.0},
                    {"12916", "Ashram Express", "Delhi", "Ahmedabad", "15:20", "06:10", 125, 1100.0},
                    {"12955", "Jaipur Superfast", "Jaipur", "Mumbai", "14:35", "07:45", 140, 1450.0},
                    {"12015", "Ajmer Shatabdi", "New Delhi", "Ajmer", "06:15", "12:40", 100, 1250.0},
                    {"20977", "Vande Bharat Express", "Ajmer", "Delhi", "06:10", "10:30", 180, 1350.0},
                    {"20978", "Vande Bharat Express", "Delhi", "Ajmer", "16:10", "20:30", 180, 1350.0},
                    {"12968", "Jaipur Express", "Jaipur", "Chandigarh", "18:20", "06:40", 105, 920.0},
                    {"12462", "Mandore Express", "Delhi", "Jodhpur", "21:20", "06:50", 130, 890.0},
                    {"12915", "Ashram Express", "Ahmedabad", "Delhi", "18:30", "07:20", 120, 1100.0},
                    {"12414", "Delhi Intercity", "Jaipur", "Delhi", "06:00", "10:30", 150, 650.0},
                    {"12956", "Jaipur Mumbai Express", "Jaipur", "Mumbai", "20:00", "09:30", 135, 1450.0},
                    {"12464", "Rajasthan Sampark Kranti", "Delhi", "Jaipur", "17:45", "22:15", 145, 720.0},
                    {"22996", "Bandikui Superfast", "Jaipur", "Delhi", "08:30", "13:00", 125, 680.0},
                    {"12963", "Mewar Express", "Nizamuddin", "Udaipur", "19:00", "07:30", 115, 1050.0},
                    {"12964", "Mewar Express", "Udaipur", "Delhi", "18:00", "06:30", 115, 1050.0},
                    {"12431", "Rajasthan Rajdhani", "Delhi", "Jodhpur", "20:30", "06:45", 130, 1400.0}
            };

            for (Object[] row : initialTrains) {
                ps.setString(1, (String) row[0]);
                ps.setString(2, (String) row[1]);
                ps.setString(3, (String) row[2]);
                ps.setString(4, (String) row[3]);
                ps.setString(5, (String) row[4]);
                ps.setString(6, (String) row[5]);
                ps.setInt(7, (Integer) row[6]);
                ps.setDouble(8, (Double) row[7]);
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }
}
