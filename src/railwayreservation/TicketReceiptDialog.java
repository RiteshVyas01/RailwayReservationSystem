package railwayreservation;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.io.File;
import java.io.FileWriter;

public class TicketReceiptDialog extends JDialog {

    private final Ticket ticket;

    public TicketReceiptDialog(Window owner, Ticket ticket) {
        super(owner, "RailConnect - E-Ticket Receipt", ModalityType.APPLICATION_MODAL);
        this.ticket = ticket;

        setSize(600, 740);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout());
        setResizable(false);

        buildUI();
    }

    private void buildUI() {
        JPanel container = new JPanel();
        container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));
        container.setBackground(AppColors.BG);
        container.setBorder(new EmptyBorder(18, 24, 18, 24));

        // Ticket Card
        JPanel ticketCard = new JPanel();
        ticketCard.setLayout(new BoxLayout(ticketCard, BoxLayout.Y_AXIS));
        ticketCard.setBackground(Color.WHITE);
        ticketCard.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(AppColors.BORDER, 1, true),
                new EmptyBorder(22, 28, 22, 28)
        ));

        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);

        JLabel brand = new JLabel("🚆 RailConnect Express");
        brand.setFont(new Font("Segoe UI", Font.BOLD, 20));
        brand.setForeground(AppColors.NAVY);

        JLabel statusBadge = Theme.createBadge(
                ticket.isCancelled() ? "CANCELLED" : "CONFIRMED",
                ticket.isCancelled() ? AppColors.LIGHT_RED : AppColors.LIGHT_GREEN,
                ticket.isCancelled() ? AppColors.RED_TEXT : AppColors.GREEN_TEXT
        );

        headerPanel.add(brand, BorderLayout.WEST);
        headerPanel.add(statusBadge, BorderLayout.EAST);
        ticketCard.add(headerPanel);
        ticketCard.add(Box.createVerticalStrut(10));

        // Divider
        JSeparator sep1 = new JSeparator();
        sep1.setForeground(AppColors.BORDER);
        ticketCard.add(sep1);
        ticketCard.add(Box.createVerticalStrut(12));

        // PNR & Date Row
        JPanel pnrRow = new JPanel(new GridLayout(1, 2, 20, 0));
        pnrRow.setOpaque(false);
        pnrRow.add(createInfoBlock("PNR NUMBER", ticket.getPnr(), new Font("Segoe UI", Font.BOLD, 17), AppColors.PRIMARY_BLUE));
        pnrRow.add(createInfoBlock("JOURNEY DATE", ticket.getJourneyDate(), new Font("Segoe UI", Font.BOLD, 15), AppColors.TEXT));
        ticketCard.add(pnrRow);
        ticketCard.add(Box.createVerticalStrut(12));

        // Train Details Row
        JPanel trainRow = new JPanel(new GridLayout(1, 2, 20, 0));
        trainRow.setOpaque(false);
        trainRow.add(createInfoBlock("TRAIN NUMBER & NAME", ticket.getTrainNumber() + " - " + ticket.getTrainName(),
                new Font("Segoe UI", Font.BOLD, 14), AppColors.TEXT));
        trainRow.add(createInfoBlock("ROUTE", ticket.getSource() + " ➔ " + ticket.getDestination(),
                new Font("Segoe UI", Font.BOLD, 14), AppColors.TEXT));
        ticketCard.add(trainRow);
        ticketCard.add(Box.createVerticalStrut(12));

        // Passenger Details Row
        JPanel passRow = new JPanel(new GridLayout(1, 3, 12, 0));
        passRow.setOpaque(false);
        passRow.add(createInfoBlock("PASSENGER", ticket.getPassengerName(), new Font("Segoe UI", Font.BOLD, 13), AppColors.TEXT));
        passRow.add(createInfoBlock("AGE / GENDER", ticket.getAge() + " yrs / " + ticket.getGender(), new Font("Segoe UI", Font.PLAIN, 13), AppColors.TEXT_SECONDARY));
        passRow.add(createInfoBlock("SEATS", ticket.getSeats() + " Seat(s)", new Font("Segoe UI", Font.BOLD, 13), AppColors.TEXT));
        ticketCard.add(passRow);
        ticketCard.add(Box.createVerticalStrut(12));

        // Class & Berth Row
        JPanel classRow = new JPanel(new GridLayout(1, 2, 20, 0));
        classRow.setOpaque(false);
        classRow.add(createInfoBlock("CLASS", ticket.getTravelClass(), new Font("Segoe UI", Font.BOLD, 13), AppColors.BLUE_TEXT));
        classRow.add(createInfoBlock("BERTH PREFERENCE", ticket.getBerth(), new Font("Segoe UI", Font.PLAIN, 13), AppColors.TEXT));
        ticketCard.add(classRow);
        ticketCard.add(Box.createVerticalStrut(12));

        // Divider
        JSeparator sep2 = new JSeparator();
        sep2.setForeground(AppColors.BORDER);
        ticketCard.add(sep2);
        ticketCard.add(Box.createVerticalStrut(12));

        // Fare & Booking Time Row
        JPanel fareRow = new JPanel(new GridLayout(1, 2, 20, 0));
        fareRow.setOpaque(false);
        fareRow.add(createInfoBlock("TOTAL FARE PAID", "₹" + String.format("%.2f", ticket.getTotalFare()),
                new Font("Segoe UI", Font.BOLD, 22), AppColors.DARK_GREEN));
        fareRow.add(createInfoBlock("BOOKED ON", ticket.getBookingTime(), new Font("Segoe UI", Font.PLAIN, 12), AppColors.MUTED));
        ticketCard.add(fareRow);
        ticketCard.add(Box.createVerticalStrut(16));

        // Barcode Simulation
        JPanel barcodePanel = new JPanel();
        barcodePanel.setBackground(new Color(245, 247, 250));
        barcodePanel.setBorder(new EmptyBorder(8, 12, 8, 12));
        JLabel barcode = new JLabel("|||| | ||||| ||| ||||||| | |||| |||||| ||||| | ||||| |||||||| ||||", SwingConstants.CENTER);
        barcode.setFont(new Font("Monospaced", Font.BOLD, 16));
        barcode.setForeground(new Color(60, 60, 60));
        barcodePanel.add(barcode);
        ticketCard.add(barcodePanel);

        container.add(ticketCard);
        container.add(Box.createVerticalStrut(16));

        // Bottom Action Buttons
        JPanel actionRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        actionRow.setOpaque(false);

        JButton copyBtn = Theme.button("📋 Copy Details", AppColors.BLUE);
        JButton saveBtn = Theme.button("💾 Save as Text", AppColors.GREEN);
        JButton closeBtn = Theme.button("Done", AppColors.NAVY);

        copyBtn.addActionListener(e -> {
            String details = getTicketText();
            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(details), null);
            JOptionPane.showMessageDialog(this, "Ticket details copied to clipboard!", "Copied", JOptionPane.INFORMATION_MESSAGE);
        });

        saveBtn.addActionListener(e -> saveTicketToFile());
        closeBtn.addActionListener(e -> dispose());

        actionRow.add(copyBtn);
        actionRow.add(saveBtn);
        actionRow.add(closeBtn);
        container.add(actionRow);

        add(container, BorderLayout.CENTER);
    }

    private String getTicketText() {
        return "========================================================\n" +
                "               RAILCONNECT - INDIAN RAILWAYS             \n" +
                "========================================================\n" +
                "PNR Number    : " + ticket.getPnr() + "\n" +
                "Status        : " + ticket.getStatus() + "\n" +
                "Train         : " + ticket.getTrainNumber() + " - " + ticket.getTrainName() + "\n" +
                "Route         : " + ticket.getSource() + " -> " + ticket.getDestination() + "\n" +
                "Journey Date  : " + ticket.getJourneyDate() + "\n" +
                "Class         : " + ticket.getTravelClass() + "\n" +
                "Berth Pref    : " + ticket.getBerth() + "\n" +
                "Passenger     : " + ticket.getPassengerName() + " (" + ticket.getAge() + " yrs, " + ticket.getGender() + ")\n" +
                "Seats Booked  : " + ticket.getSeats() + "\n" +
                "Total Fare    : ₹" + String.format("%.2f", ticket.getTotalFare()) + "\n" +
                "Booking Time  : " + ticket.getBookingTime() + "\n" +
                "========================================================\n" +
                "       Thank you for traveling with RailConnect!        \n" +
                "========================================================\n";
    }

    private void saveTicketToFile() {
        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new File("Ticket_" + ticket.getPnr() + ".txt"));
        int res = chooser.showSaveDialog(this);
        if (res == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            try (FileWriter fw = new FileWriter(file)) {
                fw.write(getTicketText());
                JOptionPane.showMessageDialog(this, "Ticket saved successfully to:\n" + file.getAbsolutePath(), "Saved", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Could not save ticket: " + ex.getMessage(), "Save Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private JPanel createInfoBlock(String title, String val, Font valFont, Color valColor) {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setOpaque(false);

        JLabel t = new JLabel(title);
        t.setFont(new Font("Segoe UI", Font.BOLD, 11));
        t.setForeground(AppColors.MUTED);

        JLabel v = new JLabel(val);
        v.setFont(valFont);
        v.setForeground(valColor);

        p.add(t);
        p.add(Box.createVerticalStrut(3));
        p.add(v);
        return p;
    }
}
