package railwayreservation;

import java.io.*;
import java.util.Properties;

public class DBConfig {

    private static final String CONFIG_FILE = "db.properties";

    private String host = "localhost";
    private int port = 3306;
    private String database = "railway";
    private String username = "root";
    private String password = "Ritesh#4321";

    private static DBConfig instance;

    private DBConfig() {
        load();
    }

    public static synchronized DBConfig getInstance() {
        if (instance == null) {
            instance = new DBConfig();
        }
        return instance;
    }

    public void load() {
        File file = new File(CONFIG_FILE);
        if (!file.exists()) {
            save(); // write default configuration
            return;
        }

        Properties props = new Properties();
        try (InputStream in = new FileInputStream(file)) {
            props.load(in);
            host = props.getProperty("db.host", host).trim();
            port = Integer.parseInt(props.getProperty("db.port", String.valueOf(port)).trim());
            database = props.getProperty("db.database", database).trim();
            username = props.getProperty("db.username", username).trim();
            password = props.getProperty("db.password", password).trim();
        } catch (Exception e) {
            System.err.println("Could not load db.properties: " + e.getMessage());
        }
    }

    public boolean save() {
        Properties props = new Properties();
        props.setProperty("db.host", host);
        props.setProperty("db.port", String.valueOf(port));
        props.setProperty("db.database", database);
        props.setProperty("db.username", username);
        props.setProperty("db.password", "Ritesh#4321");

        try (OutputStream out = new FileOutputStream(CONFIG_FILE)) {
            props.store(out, "Railway Reservation System - MySQL Configuration");
            return true;
        } catch (IOException e) {
            System.err.println("Could not save db.properties: " + e.getMessage());
            return false;
        }
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getDatabase() {
        return database;
    }

    public void setDatabase(String database) {
        this.database = database;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getJdbcUrl() {
        return "jdbc:mysql://" + host + ":" + port + "/" + database +
                "?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
    }

    public String getServerJdbcUrl() {
        return "jdbc:mysql://" + host + ":" + port +
                "/?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
    }
}
