package railwayreservation;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class LoginFrame extends JFrame {

    private final RailwayService service;

    // Login Form Components
    private JTextField loginUserField;
    private JPasswordField loginPassField;

    // Register Form Components
    private JTextField regNameField;
    private JTextField regUserField;
    private JPasswordField regPassField;
    private JTextField regPhoneField;

    private CardLayout formCardLayout;
    private JPanel formCards;
    private JButton tabSignInBtn;
    private JButton tabRegisterBtn;
    private JLabel dbStatusLabel;

    public LoginFrame() {
        this(new RailwayService(DataStore.load()));
    }

    public LoginFrame(RailwayService service) {
        this.service = (service != null) ? service : new RailwayService(DataStore.load());

        setTitle("RailConnect - Railway Reservation System");
        setSize(980, 620);
        setMinimumSize(new Dimension(850, 550));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        buildUI();
        updateDbStatus();
    }

    private void buildUI() {
        // ==========================================
        // LEFT BRANDING HERO PANEL
        // ==========================================
        JPanel leftPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                GradientPaint gp = new GradientPaint(0, 0, AppColors.NAVY, 0, getHeight(), new Color(10, 22, 45));
                g2.setPaint(gp);
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.dispose();
            }
        };

        leftPanel.setPreferredSize(new Dimension(420, 0));
        leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));
        leftPanel.setBorder(new EmptyBorder(55, 45, 45, 45));

        JLabel logo = new JLabel("🚆");
        logo.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 52));

        JLabel brandTitle = new JLabel("RailConnect");
        brandTitle.setFont(new Font("Segoe UI", Font.BOLD, 32));
        brandTitle.setForeground(Color.WHITE);

        JLabel tagLine = new JLabel("Next-Gen Railway Reservation");
        tagLine.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        tagLine.setForeground(new Color(186, 215, 250));

        leftPanel.add(logo);
        leftPanel.add(Box.createVerticalStrut(10));
        leftPanel.add(brandTitle);
        leftPanel.add(Box.createVerticalStrut(4));
        leftPanel.add(tagLine);
        leftPanel.add(Box.createVerticalStrut(35));

        // Feature Highlights
        leftPanel.add(createFeatureItem("⚡", "Live MySQL Database Support", "Persistent ACID-compliant train & ticket storage"));
        leftPanel.add(Box.createVerticalStrut(18));
        leftPanel.add(createFeatureItem("🎫", "Instant E-Ticket & PNR", "Real-time seat reservations with e-ticket receipts"));
        leftPanel.add(Box.createVerticalStrut(18));
        leftPanel.add(createFeatureItem("🔒", "Role-Based Access Control", "Dedicated passenger portal and admin train management"));

        leftPanel.add(Box.createVerticalGlue());

        // Database Status Pill at Bottom
        JPanel dbPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        dbPanel.setOpaque(false);
        dbPanel.setCursor(new Cursor(Cursor.HAND_CURSOR));

        dbStatusLabel = new JLabel("⚙️ Checking database...");
        dbStatusLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        dbStatusLabel.setForeground(Color.WHITE);

        dbPanel.add(dbStatusLabel);
        dbPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                new DBConfigDialog(LoginFrame.this, service, LoginFrame.this::updateDbStatus).setVisible(true);
            }
        });

        leftPanel.add(dbPanel);

        add(leftPanel, BorderLayout.WEST);

        // ==========================================
        // RIGHT AUTHENTICATION PANEL
        // ==========================================
        JPanel rightWrapper = new JPanel(new GridBagLayout());
        rightWrapper.setBackground(AppColors.BG);

        JPanel authCard = Theme.card();
        authCard.setPreferredSize(new Dimension(440, 480));
        authCard.setLayout(new BorderLayout());

        // Top Switcher Tabs (Sign In / Register)
        JPanel tabHeader = new JPanel(new GridLayout(1, 2, 8, 0));
        tabHeader.setOpaque(false);
        tabHeader.setBorder(new EmptyBorder(0, 0, 20, 0));

        tabSignInBtn = createTabButton("Sign In", true);
        tabRegisterBtn = createTabButton("Register", false);

        tabSignInBtn.addActionListener(e -> switchTab(true));
        tabRegisterBtn.addActionListener(e -> switchTab(false));

        tabHeader.add(tabSignInBtn);
        tabHeader.add(tabRegisterBtn);
        authCard.add(tabHeader, BorderLayout.NORTH);

        // Card Layout for Form Switch
        formCardLayout = new CardLayout();
        formCards = new JPanel(formCardLayout);
        formCards.setOpaque(false);

        formCards.add(createLoginForm(), "LOGIN");
        formCards.add(createRegisterForm(), "REGISTER");

        authCard.add(formCards, BorderLayout.CENTER);

        rightWrapper.add(authCard);
        add(rightWrapper, BorderLayout.CENTER);
    }

    private JPanel createFeatureItem(String icon, String title, String desc) {
        JPanel row = new JPanel(new BorderLayout(12, 0));
        row.setOpaque(false);

        JLabel iconLabel = new JLabel(icon);
        iconLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 22));

        JPanel textCol = new JPanel();
        textCol.setLayout(new BoxLayout(textCol, BoxLayout.Y_AXIS));
        textCol.setOpaque(false);

        JLabel t = new JLabel(title);
        t.setFont(new Font("Segoe UI", Font.BOLD, 13));
        t.setForeground(Color.WHITE);

        JLabel d = new JLabel(desc);
        d.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        d.setForeground(new Color(160, 180, 205));

        textCol.add(t);
        textCol.add(Box.createVerticalStrut(2));
        textCol.add(d);

        row.add(iconLabel, BorderLayout.WEST);
        row.add(textCol, BorderLayout.CENTER);
        return row;
    }

    private JButton createTabButton(String text, boolean active) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setFocusPainted(false);
        btn.setContentAreaFilled(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        setTabStyle(btn, active);
        return btn;
    }

    private void setTabStyle(JButton btn, boolean active) {
        if (active) {
            btn.setForeground(AppColors.PRIMARY_BLUE);
            btn.setBorder(BorderFactory.createMatteBorder(0, 0, 3, 0, AppColors.PRIMARY_BLUE));
        } else {
            btn.setForeground(AppColors.MUTED);
            btn.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, AppColors.BORDER));
        }
    }

    private void switchTab(boolean showLogin) {
        setTabStyle(tabSignInBtn, showLogin);
        setTabStyle(tabRegisterBtn, !showLogin);
        formCardLayout.show(formCards, showLogin ? "LOGIN" : "REGISTER");
    }

    // ==========================================
    // LOGIN FORM
    // ==========================================
    private JPanel createLoginForm() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setOpaque(false);

        JLabel welcome = new JLabel("Welcome Back!");
        welcome.setFont(new Font("Segoe UI", Font.BOLD, 20));
        welcome.setForeground(AppColors.TEXT);

        JLabel sub = new JLabel("Enter your credentials to access your account");
        sub.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        sub.setForeground(AppColors.MUTED);

        panel.add(welcome);
        panel.add(Box.createVerticalStrut(2));
        panel.add(sub);
        panel.add(Box.createVerticalStrut(20));

        loginUserField = Theme.field();
        loginPassField = Theme.passwordField();

        panel.add(createFieldWrapper("Username", loginUserField));
        panel.add(Box.createVerticalStrut(12));
        panel.add(createFieldWrapper("Password", loginPassField));
        panel.add(Box.createVerticalStrut(20));

        JButton loginBtn = Theme.button("LOGIN TO RAILCONNECT", AppColors.PRIMARY_BLUE);
        loginBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 45));
        loginBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        loginBtn.addActionListener(e -> executeLogin());

        loginPassField.addActionListener(e -> executeLogin());
        loginUserField.addActionListener(e -> loginPassField.requestFocus());

        panel.add(loginBtn);
        panel.add(Box.createVerticalStrut(14));

        // Quick Demo Admin Fill
        JPanel demoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        demoPanel.setOpaque(false);
        demoPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JButton demoBtn = new JButton("⚡ Quick Demo Admin Login (admin / admin123)");
        demoBtn.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        demoBtn.setForeground(AppColors.PRIMARY_BLUE);
        demoBtn.setContentAreaFilled(false);
        demoBtn.setBorderPainted(false);
        demoBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        demoBtn.addActionListener(e -> {
            loginUserField.setText("admin");
            loginPassField.setText("admin123");
            executeLogin();
        });

        demoPanel.add(demoBtn);
        panel.add(demoPanel);

        return panel;
    }

    // ==========================================
    // REGISTER FORM
    // ==========================================
    private JPanel createRegisterForm() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setOpaque(false);

        JLabel title = new JLabel("Create RailConnect Account");
        title.setFont(new Font("Segoe UI", Font.BOLD, 19));
        title.setForeground(AppColors.TEXT);

        JLabel sub = new JLabel("Sign up to start booking railway tickets instantly");
        sub.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        sub.setForeground(AppColors.MUTED);

        panel.add(title);
        panel.add(Box.createVerticalStrut(2));
        panel.add(sub);
        panel.add(Box.createVerticalStrut(15));

        regNameField = Theme.field();
        regUserField = Theme.field();
        regPassField = Theme.passwordField();
        regPhoneField = Theme.field();

        panel.add(createFieldWrapper("Full Name", regNameField));
        panel.add(Box.createVerticalStrut(8));

        JPanel midRow = new JPanel(new GridLayout(1, 2, 10, 0));
        midRow.setOpaque(false);
        midRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 65));
        midRow.setAlignmentX(Component.LEFT_ALIGNMENT);

        midRow.add(createFieldWrapper("Username", regUserField));
        midRow.add(createFieldWrapper("Phone", regPhoneField));
        panel.add(midRow);
        panel.add(Box.createVerticalStrut(8));

        panel.add(createFieldWrapper("Password", regPassField));
        panel.add(Box.createVerticalStrut(16));

        JButton registerBtn = Theme.button("REGISTER ACCOUNT", AppColors.GREEN);
        registerBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 45));
        registerBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        registerBtn.addActionListener(e -> executeRegister());

        panel.add(registerBtn);

        return panel;
    }

    private JPanel createFieldWrapper(String labelText, JComponent field) {
        JPanel wrapper = new JPanel();
        wrapper.setLayout(new BoxLayout(wrapper, BoxLayout.Y_AXIS));
        wrapper.setOpaque(false);
        wrapper.setAlignmentX(Component.LEFT_ALIGNMENT);
        wrapper.setMaximumSize(new Dimension(Integer.MAX_VALUE, 65));

        JLabel label = new JLabel(labelText);
        label.setFont(new Font("Segoe UI", Font.BOLD, 12));
        label.setForeground(AppColors.TEXT_SECONDARY);

        wrapper.add(label);
        wrapper.add(Box.createVerticalStrut(3));
        wrapper.add(field);
        return wrapper;
    }

    private void executeLogin() {
        String username = loginUserField.getText().trim();
        String password = new String(loginPassField.getPassword());

        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter both username and password.", "Required Fields", JOptionPane.WARNING_MESSAGE);
            return;
        }

        User user = service.authenticate(username, password);
        if (user == null) {
            JOptionPane.showMessageDialog(this,
                    "Invalid username or password.\nFor demo, try: admin / admin123",
                    "Login Failed",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        dispose();
        new DashboardFrame(service, user).setVisible(true);
    }

    private void executeRegister() {
        String name = regNameField.getText().trim();
        String username = regUserField.getText().trim();
        String password = new String(regPassField.getPassword());
        String phone = regPhoneField.getText().trim();

        if (name.isEmpty() || username.isEmpty() || password.isEmpty() || phone.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill in all registration fields.", "Required Fields", JOptionPane.WARNING_MESSAGE);
            return;
        }

        boolean success = service.register(name, username, password, phone);
        if (success) {
            JOptionPane.showMessageDialog(this, "Registration successful! You can now sign in.", "Welcome", JOptionPane.INFORMATION_MESSAGE);
            loginUserField.setText(username);
            loginPassField.setText(password);
            switchTab(true);
        } else {
            JOptionPane.showMessageDialog(this, "Username already exists. Please choose another username.", "Registration Failed", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void updateDbStatus() {
        boolean connected = service.isDatabaseConnected();
        if (connected) {
            dbStatusLabel.setText("🟢 MySQL: Connected (Click to configure)");
            dbStatusLabel.setForeground(new Color(134, 239, 172));
        } else {
            dbStatusLabel.setText("🟡 MySQL: Disconnected (Click to configure)");
            dbStatusLabel.setForeground(new Color(253, 224, 71));
        }
    }
}
