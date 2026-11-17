package railwayreservation;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;

public class BookingDialog extends JDialog {

    private final RailwayService service;
    private final User user;
    private final Train train;
    private final String defaultDate;
    private final Runnable onBookedCallback;

    private JTextField nameField;
    private JSpinner ageSpinner;
    private JComboBox<String> genderCombo;
    private JSpinner seatsSpinner;
    private JTextField dateField;
    private JComboBox<String> classCombo;
    private JComboBox<String> berthCombo;
    private JLabel totalFareLabel;

    public BookingDialog(Frame owner, RailwayService service, User user, Train train, String defaultDate, Runnable onBookedCallback) {
        super(owner, "Book Ticket - " + train.getName(), true);
        this.service = service;
        this.user = user;
        this.train = train;
        this.defaultDate = defaultDate;
        this.onBookedCallback = onBookedCallback;

        setSize(560, 720);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout());
        setResizable(false);

        buildUI();
    }

    private void buildUI() {
        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBackground(Color.WHITE);
        content.setBorder(new EmptyBorder(22, 28, 22, 28));

        // Train Summary Card
        JPanel trainCard = new JPanel(new GridLayout(2, 2, 10, 6));
        trainCard.setBackground(AppColors.LIGHT_BLUE);
        trainCard.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(186, 230, 253), 1, true),
                new EmptyBorder(12, 16, 12, 16)
        ));

        JLabel tName = new JLabel("🚆 " + train.getNumber() + " " + train.getName());
        tName.setFont(new Font("Segoe UI", Font.BOLD, 14));
        tName.setForeground(AppColors.NAVY);

        JLabel tRoute = new JLabel("Route: " + train.getSource() + " ➔ " + train.getDestination(), SwingConstants.RIGHT);
        tRoute.setFont(new Font("Segoe UI", Font.BOLD, 13));
        tRoute.setForeground(AppColors.BLUE_TEXT);

        JLabel tTime = new JLabel("Dep: " + train.getDeparture() + " | Arr: " + train.getArrival());
        tTime.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        tTime.setForeground(AppColors.TEXT_SECONDARY);

        JLabel tSeats = new JLabel("Available: " + train.getSeats() + " seats  (Base: ₹" + train.getFare() + ")", SwingConstants.RIGHT);
        tSeats.setFont(new Font("Segoe UI", Font.BOLD, 12));
        tSeats.setForeground(AppColors.DARK_GREEN);

        trainCard.add(tName);
        trainCard.add(tRoute);
        trainCard.add(tTime);
        trainCard.add(tSeats);
        trainCard.setMaximumSize(new Dimension(Integer.MAX_VALUE, 75));
        trainCard.setAlignmentX(Component.LEFT_ALIGNMENT);

        content.add(trainCard);
        content.add(Box.createVerticalStrut(15));

        // Passenger Details Heading
        JLabel heading = new JLabel("Passenger Details & Preferences");
        heading.setFont(new Font("Segoe UI", Font.BOLD, 15));
        heading.setForeground(AppColors.TEXT);
        heading.setAlignmentX(Component.LEFT_ALIGNMENT);
        content.add(heading);
        content.add(Box.createVerticalStrut(12));

        // Name Field
        nameField = Theme.field();
        nameField.setText(user != null ? user.getName() : "");
        content.add(createRow("Primary Passenger Name:", nameField));
        content.add(Box.createVerticalStrut(8));

        // Age & Gender Row
        JPanel ageGenderRow = new JPanel(new GridLayout(1, 2, 12, 0));
        ageGenderRow.setOpaque(false);
        ageGenderRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 65));
        ageGenderRow.setAlignmentX(Component.LEFT_ALIGNMENT);

        ageSpinner = new JSpinner(new SpinnerNumberModel(28, 1, 120, 1));
        ageSpinner.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        ageSpinner.setPreferredSize(new Dimension(0, 42));

        genderCombo = Theme.combo("Male", "Female", "Other");

        JPanel ageP = new JPanel();
        ageP.setLayout(new BoxLayout(ageP, BoxLayout.Y_AXIS));
        ageP.setOpaque(false);
        JLabel aLabel = new JLabel("Age:");
        aLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        aLabel.setForeground(AppColors.TEXT_SECONDARY);
        ageP.add(aLabel);
        ageP.add(Box.createVerticalStrut(3));
        ageP.add(ageSpinner);

        JPanel genderP = new JPanel();
        genderP.setLayout(new BoxLayout(genderP, BoxLayout.Y_AXIS));
        genderP.setOpaque(false);
        JLabel gLabel = new JLabel("Gender:");
        gLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        gLabel.setForeground(AppColors.TEXT_SECONDARY);
        genderP.add(gLabel);
        genderP.add(Box.createVerticalStrut(3));
        genderP.add(genderCombo);

        ageGenderRow.add(ageP);
        ageGenderRow.add(genderP);
        content.add(ageGenderRow);
        content.add(Box.createVerticalStrut(8));

        // Class & Berth Row
        JPanel classBerthRow = new JPanel(new GridLayout(1, 2, 12, 0));
        classBerthRow.setOpaque(false);
        classBerthRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 65));
        classBerthRow.setAlignmentX(Component.LEFT_ALIGNMENT);

        classCombo = Theme.combo(
                "Sleeper (SL) [1x Fare]",
                "AC 3 Tier (3A) [1.25x Fare]",
                "AC 2 Tier (2A) [1.6x Fare]",
                "AC First Class (1A) [2.2x Fare]",
                "Chair Car (CC) [1.15x Fare]"
        );

        berthCombo = Theme.combo(
                "No Preference",
                "Lower Berth",
                "Middle Berth",
                "Upper Berth",
                "Side Lower",
                "Side Upper",
                "Window Seat"
        );

        JPanel classP = new JPanel();
        classP.setLayout(new BoxLayout(classP, BoxLayout.Y_AXIS));
        classP.setOpaque(false);
        JLabel cLabel = new JLabel("Travel Class:");
        cLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        cLabel.setForeground(AppColors.TEXT_SECONDARY);
        classP.add(cLabel);
        classP.add(Box.createVerticalStrut(3));
        classP.add(classCombo);

        JPanel berthP = new JPanel();
        berthP.setLayout(new BoxLayout(berthP, BoxLayout.Y_AXIS));
        berthP.setOpaque(false);
        JLabel bLabel = new JLabel("Berth Preference:");
        bLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        bLabel.setForeground(AppColors.TEXT_SECONDARY);
        berthP.add(bLabel);
        berthP.add(Box.createVerticalStrut(3));
        berthP.add(berthCombo);

        classBerthRow.add(classP);
        classBerthRow.add(berthP);
        content.add(classBerthRow);
        content.add(Box.createVerticalStrut(8));

        // Seats & Date Row
        JPanel seatsDateRow = new JPanel(new GridLayout(1, 2, 12, 0));
        seatsDateRow.setOpaque(false);
        seatsDateRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 65));
        seatsDateRow.setAlignmentX(Component.LEFT_ALIGNMENT);

        int maxBookable = Math.max(1, Math.min(6, train.getSeats()));
        seatsSpinner = new JSpinner(new SpinnerNumberModel(1, 1, maxBookable, 1));
        seatsSpinner.setFont(new Font("Segoe UI", Font.BOLD, 14));
        seatsSpinner.setPreferredSize(new Dimension(0, 42));

        dateField = Theme.field();
        dateField.setText(defaultDate != null && !defaultDate.equalsIgnoreCase("Select Date") ? defaultDate : "Upcoming");

        JPanel seatsP = new JPanel();
        seatsP.setLayout(new BoxLayout(seatsP, BoxLayout.Y_AXIS));
        seatsP.setOpaque(false);
        JLabel sLabel = new JLabel("No. of Seats (Max 6):");
        sLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        sLabel.setForeground(AppColors.TEXT_SECONDARY);
        seatsP.add(sLabel);
        seatsP.add(Box.createVerticalStrut(3));
        seatsP.add(seatsSpinner);

        JPanel dateP = new JPanel();
        dateP.setLayout(new BoxLayout(dateP, BoxLayout.Y_AXIS));
        dateP.setOpaque(false);
        JLabel dLabel = new JLabel("Travel Date:");
        dLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        dLabel.setForeground(AppColors.TEXT_SECONDARY);
        dateP.add(dLabel);
        dateP.add(Box.createVerticalStrut(3));
        dateP.add(dateField);

        seatsDateRow.add(seatsP);
        seatsDateRow.add(dateP);
        content.add(seatsDateRow);
        content.add(Box.createVerticalStrut(15));

        // Total Fare Box
        JPanel fareBox = new JPanel(new BorderLayout());
        fareBox.setBackground(AppColors.LIGHT_GREEN);
        fareBox.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(167, 243, 208), 1, true),
                new EmptyBorder(12, 16, 12, 16)
        ));
        fareBox.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
        fareBox.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel fareTitle = new JLabel("Total Payable Amount:");
        fareTitle.setFont(new Font("Segoe UI", Font.BOLD, 14));
        fareTitle.setForeground(AppColors.GREEN_TEXT);

        totalFareLabel = new JLabel("₹" + String.format("%.2f", train.getFare()), SwingConstants.RIGHT);
        totalFareLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        totalFareLabel.setForeground(AppColors.DARK_GREEN);

        fareBox.add(fareTitle, BorderLayout.WEST);
        fareBox.add(totalFareLabel, BorderLayout.EAST);
        content.add(fareBox);
        content.add(Box.createVerticalStrut(18));

        // Re-calculate fare when seats or class changes
        Runnable updateFare = () -> {
            int count = (Integer) seatsSpinner.getValue();
            String selClass = (String) classCombo.getSelectedItem();
            double farePerSeat = Ticket.calculateClassFare(train.getFare(), selClass);
            double total = farePerSeat * count;
            totalFareLabel.setText("₹" + String.format("%.2f", total));
        };

        seatsSpinner.addChangeListener(e -> updateFare.run());
        classCombo.addActionListener(e -> updateFare.run());

        // Action Buttons
        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        btnRow.setOpaque(false);
        btnRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 45));
        btnRow.setAlignmentX(Component.LEFT_ALIGNMENT);

        JButton cancelBtn = Theme.button("Cancel", AppColors.MUTED);
        JButton confirmBtn = Theme.button("Confirm & Pay", AppColors.GREEN);

        cancelBtn.addActionListener(e -> dispose());
        confirmBtn.addActionListener(e -> processBooking());

        btnRow.add(cancelBtn);
        btnRow.add(confirmBtn);
        content.add(btnRow);

        add(content, BorderLayout.CENTER);
    }

    private JPanel createRow(String labelText, JComponent field) {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setOpaque(false);
        p.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, 65));

        JLabel l = new JLabel(labelText);
        l.setFont(new Font("Segoe UI", Font.BOLD, 12));
        l.setForeground(AppColors.TEXT_SECONDARY);

        p.add(l);
        p.add(Box.createVerticalStrut(3));
        p.add(field);
        return p;
    }

    private void processBooking() {
        String passengerName = nameField.getText().trim();
        if (passengerName.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter passenger name.", "Required Field", JOptionPane.WARNING_MESSAGE);
            nameField.requestFocus();
            return;
        }

        int age = (Integer) ageSpinner.getValue();
        String gender = (String) genderCombo.getSelectedItem();
        int seats = (Integer) seatsSpinner.getValue();
        String date = dateField.getText().trim();
        String travelClass = (String) classCombo.getSelectedItem();
        String berth = (String) berthCombo.getSelectedItem();

        String username = (user != null) ? user.getUsername() : "guest";

        Ticket ticket = service.bookTicket(username, train, passengerName, age, gender, seats, date, travelClass, berth);

        if (ticket != null) {
            dispose();
            if (onBookedCallback != null) {
                onBookedCallback.run();
            }
            new TicketReceiptDialog(getOwner(), ticket).setVisible(true);
        } else {
            JOptionPane.showMessageDialog(this,
                    "Failed to complete reservation.\nRequested seats may no longer be available.",
                    "Booking Failed",
                    JOptionPane.ERROR_MESSAGE);
        }
    }
}
