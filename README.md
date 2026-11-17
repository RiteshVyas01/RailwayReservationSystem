# RailConnect - Modern Railway Reservation System 🚆

A modern, high-performance Java Swing & MySQL application for Indian Railway reservations, ticketing, PNR status tracking, and fleet management.

---

## 🌟 Key Features & Improvements

### 1. 🗄️ Robust MySQL Database Integration
- **Automatic Schema Initialization**: Auto-creates database `railway_db` and tables (`users`, `trains`, `tickets`) on first launch.
- **Initial Data Seeding**: Automatically populates 20 major Indian Railway express trains (Rajdhani, Shatabdi, Vande Bharat, etc.) and the default administrator account (`admin` / `admin123`).
- **Configurable Connection**: Configured via `db.properties` or via the in-app **"MySQL Database Settings"** dialog with live connection testing.
- **Transaction Safety**: Seat reservation and ticket cancellation run within ACID database transactions (preventing overbooking and ensuring inventory consistency).
- **Standalone SQL Script**: Includes [`schema.sql`](file:///c:/Users/rites/IdeaProjects/RailwayReservationSystem/schema.sql) for reference or direct execution in MySQL Workbench.
- **Graceful Fallback**: If MySQL service is momentarily stopped or credentials need adjustment, the application gracefully alerts the user and runs in offline mode without crashing.

### 2. 🎨 Modern & Intuitive User Interface
- **Modern Design System**: Polished color palette (Navy `#0F2341`, Railway Blue `#0E74D6`, Emerald Green `#10B981`, Card White `#FFFFFF`), clean borders, and system-wide text anti-aliasing.
- **Sign In & Register Card**: Unified tabbed login panel with one-click **"⚡ Quick Demo Admin Login"** and instant user registration.
- **Search & Book Trains**:
  - Live search filters for Source and Destination stations.
  - Interactive calendar date picker.
  - Interactive table displaying Train No, Name, Route, Timings, Available Seats, and Fare.
  - One-click or double-click to book.
- **Booking Modal Dialog**:
  - Selected train summary card.
  - Passenger information input (Name, Age, Gender, Seats 1–6).
  - Live total fare calculation.
- **Printable E-Ticket Receipt**:
  - Boarding pass design with PNR number, route, passenger details, booking timestamp, and simulated barcode.
  - One-click **"Copy Details"** to clipboard.
- **My Bookings & Management**:
  - Table showing user's booked tickets with real-time status badges (`CONFIRMED` / `CANCELLED`).
  - View ticket receipt anytime.
  - Cancel ticket button with confirmation dialog and automatic seat refund.
- **PNR Status Inquiry**:
  - Fast PNR lookup card displaying complete itinerary, passenger details, and booking status.
- **Admin Fleet Management (Admin Only)**:
  - Add new trains with schedule, capacity, and fare.
  - Delete trains from the database.
- **Dynamic Stats Header**:
  - Live counts for Available Trains, Lifetime Bookings, and Confirmed Active Tickets.

---

## 🛠️ MySQL Setup Guide

1. **Start MySQL Service**:
   Ensure your MySQL service is running. On Windows, you can start it via PowerShell (as Administrator):
   ```powershell
   Start-Service MySQL80
   # or
   net start MySQL80
   ```
2. **Configure Database Credentials**:
   Edit [`db.properties`](file:///c:/Users/rites/IdeaProjects/RailwayReservationSystem/db.properties) in the project root:
   ```properties
   db.host=localhost
   db.port=3306
   db.database=railway_db
   db.username=root
   db.password=YOUR_MYSQL_PASSWORD
   ```
   *(You can also configure this directly in the app by clicking the **"MySQL Status"** button at the top/bottom of the window.)*

3. **MySQL Connector JAR**:
   The required library [`mysql-connector-j-8.4.0.jar`](file:///c:/Users/rites/IdeaProjects/RailwayReservationSystem/lib/mysql-connector-j-8.4.0.jar) is included in the `lib/` directory and pre-configured in the IntelliJ module file.

---

## 🚀 Running the Project

### Option A: In IntelliJ IDEA
1. Open the project folder in IntelliJ IDEA.
2. Ensure the JDK is set to OpenJDK 26 (or JDK 17+).
3. Right-click [`Main.java`](file:///c:/Users/rites/IdeaProjects/RailwayReservationSystem/src/railwayreservation/Main.java) and click **Run 'Main.main()'**.

### Option B: Via Terminal / Command Prompt
```powershell
# Compile
javac -cp "lib/mysql-connector-j-8.4.0.jar" -d "out/production/RailwayReservationSystem" src/railwayreservation/*.java

# Run
java -cp "out/production/RailwayReservationSystem;lib/mysql-connector-j-8.4.0.jar" railwayreservation.Main
```

---

## 👤 Default Credentials
- **Username**: `admin`
- **Password**: `admin123`
- *(Or click "Create Account / Register" to create your personal account)*
