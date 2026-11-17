package railwayreservation;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class DBConfigDialog extends JDialog {

    private final RailwayService service;
    private final Runnable onConnectedCallback;

    private JTextField hostField;
    private JTextField portField;
    private JTextField dbField;
    private JTextField userField;
    private JPasswordField passField;
    private JLabel statusLabel;

    public DBConfigDialog(Frame owner, RailwayService service, Runnable onConnectedCallback) {
        super(owner, "MySQL Database Settings", true);
        this.service = service;
        this.onConnectedCallback = onConnectedCallback;

        setSize(520, 560);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout());
        setResizable(false);

        buildUI();
    }

    private void buildUI() {
        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBackground(Color.WHITE);
        content.setBorder(new EmptyBorder(25, 30, 25, 30));

        // Header
        JLabel title = new JLabel("⚙️ MySQL Database Configuration");
        title.setFont(new Font("Segoe UI", Font.BOLD, 18));
        title.setForeground(AppColors.NAVY);

        JLabel subtitle = new JLabel("Configure your MySQL Server connection parameters.");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        subtitle.setForeground(AppColors.MUTED);

        content.add(title);
        content.add(Box.createVerticalStrut(4));
        content.add(subtitle);
        content.add(Box.createVerticalStrut(20));

        DBConfig config = DBConfig.getInstance();

        // Fields
        hostField = Theme.field();
        hostField.setText(config.getHost());

        portField = Theme.field();
        portField.setText(String.valueOf(config.getPort()));

        dbField = Theme.field();
        dbField.setText(config.getDatabase());

        userField = Theme.field();
        userField.setText(config.getUsername());

        passField = Theme.passwordField();
        passField.setText(config.getPassword());

        content.add(createFieldRow("Host / Server Address:", hostField));
        content.add(Box.createVerticalStrut(10));

        content.add(createFieldRow("Port (Default: 3306):", portField));
        content.add(Box.createVerticalStrut(10));

        content.add(createFieldRow("Database Name:", dbField));
        content.add(Box.createVerticalStrut(10));

        content.add(createFieldRow("MySQL Username:", userField));
        content.add(Box.createVerticalStrut(10));

        content.add(createFieldRow("MySQL Password:", passField));
        content.add(Box.createVerticalStrut(15));

        // Status Label
        statusLabel = new JLabel(" ");
        statusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        statusLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        content.add(statusLabel);
        content.add(Box.createVerticalStrut(15));

        // Buttons
        JPanel buttonRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttonRow.setOpaque(false);
        buttonRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        buttonRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 45));

        JButton testBtn = Theme.button("Test Connection", AppColors.BLUE);
        JButton saveBtn = Theme.button("Save & Connect", AppColors.GREEN);
        JButton cancelBtn = Theme.button("Close", AppColors.MUTED);

        testBtn.addActionListener(e -> testConnection());
        saveBtn.addActionListener(e -> saveAndConnect());
        cancelBtn.addActionListener(e -> dispose());

        buttonRow.add(testBtn);
        buttonRow.add(saveBtn);
        buttonRow.add(cancelBtn);

        content.add(buttonRow);

        add(content, BorderLayout.CENTER);
    }

    private JPanel createFieldRow(String labelText, JComponent field) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setOpaque(false);
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 70));

        JLabel label = new JLabel(labelText);
        label.setFont(new Font("Segoe UI", Font.BOLD, 12));
        label.setForeground(AppColors.TEXT_SECONDARY);

        panel.add(label);
        panel.add(Box.createVerticalStrut(4));
        panel.add(field);
        return panel;
    }

    private void testConnection() {
        try {
            String host = hostField.getText().trim();
            int port = Integer.parseInt(portField.getText().trim());
            String db = dbField.getText().trim();
            String user = userField.getText().trim();
            String pass = new String(passField.getPassword());

            statusLabel.setForeground(AppColors.MUTED);
            statusLabel.setText("⏳ Testing MySQL connection...");

            SwingUtilities.invokeLater(() -> {
                boolean ok = DBConnection.testConnection(host, port, db, user, pass);
                if (ok) {
                    statusLabel.setForeground(AppColors.DARK_GREEN);
                    statusLabel.setText("✓ Connection successful! MySQL server is reachable.");
                } else {
                    statusLabel.setForeground(AppColors.RED);
                    String err = DBConnection.getLastError();
                    if (err.length() > 65) err = err.substring(0, 65) + "...";
                    statusLabel.setText("✗ Connection failed: " + err);
                }
            });

        } catch (NumberFormatException ex) {
            statusLabel.setForeground(AppColors.RED);
            statusLabel.setText("✗ Port must be a valid number (e.g., 3306).");
        }
    }

    private void saveAndConnect() {
        try {
            String host = hostField.getText().trim();
            int port = Integer.parseInt(portField.getText().trim());
            String db = dbField.getText().trim();
            String user = userField.getText().trim();
            String pass = new String(passField.getPassword());

            DBConfig config = DBConfig.getInstance();
            config.setHost(host);
            config.setPort(port);
            config.setDatabase(db);
            config.setUsername(user);
            config.setPassword(pass);
            config.save();

            boolean ok = service.reconnectDatabase();
            if (ok) {
                JOptionPane.showMessageDialog(this,
                        "Successfully connected to MySQL and initialized railway_db!",
                        "Database Connected",
                        JOptionPane.INFORMATION_MESSAGE);
                if (onConnectedCallback != null) {
                    onConnectedCallback.run();
                }
                dispose();
            } else {
                JOptionPane.showMessageDialog(this,
                        "Saved settings, but connection failed:\n\n" + DBConnection.getLastError() +
                                "\n\nPlease ensure MySQL80 service is running and credentials are correct.",
                        "Connection Warning",
                        JOptionPane.WARNING_MESSAGE);
            }

        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Port must be a valid number.", "Invalid Port", JOptionPane.ERROR_MESSAGE);
        }
    }
}
