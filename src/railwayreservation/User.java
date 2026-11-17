package railwayreservation;

import java.io.Serializable;

public class User implements Serializable {

    private String name;
    private String username;
    private String password;
    private String phone;
    private String role; // "USER" or "ADMIN"

    public User(String name,
                String username,
                String password,
                String phone) {
        this(name, username, password, phone, "admin".equalsIgnoreCase(username) ? "ADMIN" : "USER");
    }

    public User(String name,
                String username,
                String password,
                String phone,
                String role) {
        this.name = name;
        this.username = username;
        this.password = password;
        this.phone = phone;
        this.role = (role != null && !role.isBlank()) ? role : ("admin".equalsIgnoreCase(username) ? "ADMIN" : "USER");
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public boolean isAdmin() {
        return "ADMIN".equalsIgnoreCase(role) || "admin".equalsIgnoreCase(username);
    }
}
