-- ==========================================================
-- Railway Reservation System - MySQL Database Schema
-- ==========================================================

CREATE DATABASE IF NOT EXISTS railway_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE railway_db;

-- 1. Users Table
CREATE TABLE IF NOT EXISTS users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(100) NOT NULL,
    phone VARCHAR(20) NOT NULL,
    role VARCHAR(20) DEFAULT 'USER',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

-- 2. Trains Table
CREATE TABLE IF NOT EXISTS trains (
    number VARCHAR(20) PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    source VARCHAR(100) NOT NULL,
    destination VARCHAR(100) NOT NULL,
    departure VARCHAR(20) NOT NULL,
    arrival VARCHAR(20) NOT NULL,
    seats INT NOT NULL,
    fare DOUBLE NOT NULL
) ENGINE=InnoDB;

-- 3. Tickets Table
CREATE TABLE IF NOT EXISTS tickets (
    pnr VARCHAR(30) PRIMARY KEY,
    username VARCHAR(50) NOT NULL,
    train_number VARCHAR(20) NOT NULL,
    train_name VARCHAR(100) NOT NULL,
    source VARCHAR(100) NOT NULL,
    destination VARCHAR(100) NOT NULL,
    passenger_name VARCHAR(100) NOT NULL,
    age INT NOT NULL,
    gender VARCHAR(10) NOT NULL,
    seats INT NOT NULL,
    total_fare DOUBLE NOT NULL,
    booking_time VARCHAR(30) NOT NULL,
    journey_date VARCHAR(20),
    travel_class VARCHAR(30) DEFAULT 'Sleeper (SL)',
    berth VARCHAR(30) DEFAULT 'No Preference',
    status VARCHAR(20) DEFAULT 'CONFIRMED',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (train_number) REFERENCES trains(number) ON UPDATE CASCADE
) ENGINE=InnoDB;

-- ==========================================================
-- Initial Seed Data
-- ==========================================================

-- Admin User (Password: admin123)
INSERT IGNORE INTO users (name, username, password, phone, role)
VALUES ('Administrator', 'admin', 'admin123', '9876543210', 'ADMIN');

-- Sample Trains
INSERT IGNORE INTO trains (number, name, source, destination, departure, arrival, seats, fare) VALUES
('12001', 'Rajdhani Express', 'Delhi', 'Mumbai', '16:55', '08:35', 120, 1650.00),
('12952', 'Mumbai Rajdhani', 'Mumbai', 'Delhi', '17:00', '08:35', 115, 1620.00),
('12957', 'Swarna Jayanti Express', 'Delhi', 'Ahmedabad', '19:55', '07:30', 95, 980.00),
('12413', 'Pooja Express', 'Ajmer', 'Delhi', '14:30', '21:20', 110, 760.00),
('22987', 'Agra Superfast', 'Agra', 'Jaipur', '07:10', '11:20', 85, 520.00),
('12916', 'Ashram Express', 'Delhi', 'Ahmedabad', '15:20', '06:10', 125, 1100.00),
('12955', 'Jaipur Superfast', 'Jaipur', 'Mumbai', '14:35', '07:45', 140, 1450.00),
('12015', 'Ajmer Shatabdi', 'New Delhi', 'Ajmer', '06:15', '12:40', 100, 1250.00),
('20977', 'Vande Bharat Express', 'Ajmer', 'Delhi', '06:10', '10:30', 180, 1350.00),
('20978', 'Vande Bharat Express', 'Delhi', 'Ajmer', '16:10', '20:30', 180, 1350.00),
('12968', 'Jaipur Express', 'Jaipur', 'Chandigarh', '18:20', '06:40', 105, 920.00),
('12462', 'Mandore Express', 'Delhi', 'Jodhpur', '21:20', '06:50', 130, 890.00),
('12915', 'Ashram Express', 'Ahmedabad', 'Delhi', '18:30', '07:20', 120, 1100.00),
('12414', 'Delhi Intercity', 'Jaipur', 'Delhi', '06:00', '10:30', 150, 650.00),
('12956', 'Jaipur Mumbai Express', 'Jaipur', 'Mumbai', '20:00', '09:30', 135, 1450.00),
('12464', 'Rajasthan Sampark Kranti', 'Delhi', 'Jaipur', '17:45', '22:15', 145, 720.00),
('22996', 'Bandikui Superfast', 'Jaipur', 'Delhi', '08:30', '13:00', 125, 680.00),
('12963', 'Mewar Express', 'Nizamuddin', 'Udaipur', '19:00', '07:30', 115, 1050.00),
('12964', 'Mewar Express', 'Udaipur', 'Delhi', '18:00', '06:30', 115, 1050.00),
('12431', 'Rajasthan Rajdhani', 'Delhi', 'Jodhpur', '20:30', '06:45', 130, 1400.00);
