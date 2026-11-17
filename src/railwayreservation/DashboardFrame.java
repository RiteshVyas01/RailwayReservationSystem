package railwayreservation;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class DashboardFrame extends JFrame {

    private final RailwayService service;
    private final User user;

    // Search Fields
    private JTextField fromField;
    private JTextField toField;
    private JTextField dateField;

    // Trains Table
    private DefaultTableModel trainsTableModel;
    private JTable trainsTable;
    private List<Train> currentTrainsList;

    // Bookings Table & Controls
    private DefaultTableModel bookingsTableModel;
    private JTable bookingsTable;
    private List<Ticket> currentBookingsList;
    private JTextField ticketSearchField;
    private JComboBox<String> statusFilterCombo;
    private JComboBox<String> scopeFilterCombo;
    private JLabel bookingSummaryLabel;

    // PNR Inquiry
    private JTextField pnrSearchField;
    private JPanel pnrResultPanel;

    // Admin Fleet Table
    private DefaultTableModel adminTrainsModel;
    private JTable adminTrainsTable;

    // Stats Labels
    private JLabel totalTrainsLabel;
    private JLabel myBookingsLabel;
    private JLabel activeTicketsLabel;

    // Database Status Indicator
    private JButton dbStatusBtn;

    public DashboardFrame(RailwayService service) {
        this(service, new User("Guest User", "guest", "YES", "", "USER"));
    }

    public DashboardFrame(RailwayService service, User user) {
        this.service = (service != null) ? service : new RailwayService(DataStore.load());
        this.user = (user != null) ? user : new User("Guest User", "guest", "YES", "", "USER");

        setTitle("RailConnect - Railway Reservation System");
        setSize(1320, 840);
        setMinimumSize(new Dimension(1100, 720));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        buildUI();
        refreshAllData();
    }

    private void buildUI() {
        // ==========================================
        // TOP APP BAR
        // ==========================================
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(Color.WHITE);
        topBar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, AppColors.BORDER),
                new EmptyBorder(14, 25, 14, 25)
        ));

        // Brand & User Info
        JPanel leftBrand = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        leftBrand.setOpaque(false);

        JLabel logo = new JLabel("🚆");
        logo.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 28));

        JPanel titleBlock = new JPanel();
        titleBlock.setLayout(new BoxLayout(titleBlock, BoxLayout.Y_AXIS));
        titleBlock.setOpaque(false);

        JLabel appTitle = new JLabel("RailConnect");
        appTitle.setFont(new Font("Segoe UI", Font.BOLD, 20));
        appTitle.setForeground(AppColors.NAVY);

        JLabel userGreeting = new JLabel("Logged in as: " + user.getName() + " (" + user.getRole() + ")");
        userGreeting.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        userGreeting.setForeground(AppColors.MUTED);

        titleBlock.add(appTitle);
        titleBlock.add(userGreeting);

        leftBrand.add(logo);
        leftBrand.add(titleBlock);
        topBar.add(leftBrand, BorderLayout.WEST);

        // Right Actions (DB Status, Refresh, Settings, Logout)
        JPanel rightActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        rightActions.setOpaque(false);

        dbStatusBtn = new JButton("🟢 MySQL Connected");
        dbStatusBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        dbStatusBtn.setFocusPainted(false);
        dbStatusBtn.setContentAreaFilled(false);
        dbStatusBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        dbStatusBtn.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(AppColors.BORDER, 1, true),
                new EmptyBorder(6, 12, 6, 12)
        ));
        dbStatusBtn.addActionListener(e -> new DBConfigDialog(this, service, this::refreshAllData).setVisible(true));

        JButton refreshBtn = Theme.createRoundedButton("🔄 Refresh", AppColors.BG, AppColors.BORDER, AppColors.TEXT, 6);
        refreshBtn.addActionListener(e -> refreshAllData());

        JButton logoutBtn = Theme.createRoundedButton("Logout", Color.WHITE, AppColors.LIGHT_RED, AppColors.RED, 6);
        logoutBtn.setBorder(new LineBorder(AppColors.BORDER, 1, true));
        logoutBtn.addActionListener(e -> {
            int res = JOptionPane.showConfirmDialog(this, "Are you sure you want to log out?", "Logout Confirmation", JOptionPane.YES_NO_OPTION);
            if (res == JOptionPane.YES_OPTION) {
                dispose();
                new LoginFrame(service).setVisible(true);
            }
        });

        rightActions.add(dbStatusBtn);
        rightActions.add(refreshBtn);
        rightActions.add(logoutBtn);
        topBar.add(rightActions, BorderLayout.EAST);

        add(topBar, BorderLayout.NORTH);

        // ==========================================
        // CENTER TABS & BODY
        // ==========================================
        JPanel mainContainer = new JPanel(new BorderLayout());
        mainContainer.setBackground(AppColors.BG);

        // Top Stats Banner
        mainContainer.add(createStatsHeader(), BorderLayout.NORTH);

        // Main Tabbed Pane
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Segoe UI", Font.BOLD, 14));
        tabbedPane.setBackground(Color.WHITE);
        tabbedPane.setBorder(new EmptyBorder(10, 20, 20, 20));

        tabbedPane.addTab("  🚆 Search & Book Trains  ", createSearchAndBookTab());
        tabbedPane.addTab("  🎫 Booked Tickets & Cancellation  ", createBookedTicketsTab());
        tabbedPane.addTab("  🔍 PNR Status Inquiry  ", createPnrInquiryTab());

        if (user.isAdmin()) {
            tabbedPane.addTab("  🛠️ Train Management (Admin)  ", createAdminTab());
        }

        mainContainer.add(tabbedPane, BorderLayout.CENTER);
        add(mainContainer, BorderLayout.CENTER);
    }

    // ============================================================
    // DYNAMIC STATS BANNER
    // ============================================================
    private JPanel createStatsHeader() {
        JPanel statsPanel = new JPanel(new GridLayout(1, 3, 16, 0));
        statsPanel.setBackground(AppColors.BG);
        statsPanel.setBorder(new EmptyBorder(16, 20, 8, 20));

        totalTrainsLabel = new JLabel("0");
        myBookingsLabel = new JLabel("0");
        activeTicketsLabel = new JLabel("0");

        statsPanel.add(createStatCard("🚆", totalTrainsLabel, "AVAILABLE TRAINS", "Total active trains in MySQL database", AppColors.PRIMARY_BLUE));
        statsPanel.add(createStatCard("🎫", myBookingsLabel, "TOTAL BOOKINGS", "Lifetime reservations recorded", AppColors.GREEN));
        statsPanel.add(createStatCard("🎟", activeTicketsLabel, "CONFIRMED TICKETS", "Active upcoming journeys", AppColors.ORANGE));

        return statsPanel;
    }

    private JPanel createStatCard(String icon, JLabel numLabel, String title, String sub, Color color) {
        JPanel card = Theme.card();
        card.setLayout(new BorderLayout(14, 0));
        card.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(AppColors.BORDER, 1, true),
                new EmptyBorder(14, 18, 14, 18)
        ));

        JLabel iconLbl = new JLabel(icon);
        iconLbl.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 32));

        JPanel textCol = new JPanel();
        textCol.setLayout(new BoxLayout(textCol, BoxLayout.Y_AXIS));
        textCol.setOpaque(false);

        numLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        numLabel.setForeground(color);

        JLabel tLbl = new JLabel(title);
        tLbl.setFont(new Font("Segoe UI", Font.BOLD, 11));
        tLbl.setForeground(AppColors.TEXT_SECONDARY);

        JLabel sLbl = new JLabel(sub);
        sLbl.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        sLbl.setForeground(AppColors.MUTED);

        textCol.add(numLabel);
        textCol.add(Box.createVerticalStrut(2));
        textCol.add(tLbl);
        textCol.add(sLbl);

        card.add(iconLbl, BorderLayout.WEST);
        card.add(textCol, BorderLayout.CENTER);
        return card;
    }

    // ============================================================
    // TAB 1: SEARCH & BOOK TRAINS
    // ============================================================
    private JPanel createSearchAndBookTab() {
        JPanel panel = new JPanel(new BorderLayout(0, 15));
        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(16, 16, 16, 16));

        // Search Form Box
        JPanel searchBox = new JPanel(new GridBagLayout());
        searchBox.setBackground(AppColors.LIGHT_BLUE);
        searchBox.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(186, 230, 253), 1, true),
                new EmptyBorder(16, 16, 16, 16)
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(0, 6, 0, 6);

        fromField = Theme.field();
        fromField.setToolTipText("Enter source station (e.g. Delhi, Jaipur, Mumbai)");

        toField = Theme.field();
        toField.setToolTipText("Enter destination station (e.g. Mumbai, Delhi, Ahmedabad)");

        dateField = Theme.field();
        dateField.setText(new SimpleDateFormat("dd-MM-yyyy").format(new Date()));
        dateField.setEditable(false);
        dateField.setCursor(new Cursor(Cursor.HAND_CURSOR));
        dateField.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                showCalendarPicker();
            }
        });

        JButton searchBtn = Theme.button("🔍 Search Trains", AppColors.PRIMARY_BLUE);
        searchBtn.addActionListener(e -> executeTrainSearch());

        JButton showAllBtn = Theme.button("Show All", AppColors.MUTED);
        showAllBtn.addActionListener(e -> {
            fromField.setText("");
            toField.setText("");
            executeTrainSearch();
        });

        gbc.gridx = 0; gbc.weightx = 1.0;
        searchBox.add(createLabeledField("From Station:", fromField), gbc);

        gbc.gridx = 1; gbc.weightx = 1.0;
        searchBox.add(createLabeledField("To Station:", toField), gbc);

        gbc.gridx = 2; gbc.weightx = 0.8;
        searchBox.add(createLabeledField("Travel Date (Click to pick):", dateField), gbc);

        gbc.gridx = 3; gbc.weightx = 0.4;
        gbc.insets = new Insets(18, 6, 0, 6);
        searchBox.add(searchBtn, gbc);

        gbc.gridx = 4; gbc.weightx = 0.3;
        searchBox.add(showAllBtn, gbc);

        panel.add(searchBox, BorderLayout.NORTH);

        // Trains Table
        String[] columns = {"Train No", "Train Name", "From Station", "To Station", "Departure", "Arrival", "Available Seats", "Fare (₹)"};
        trainsTableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };

        trainsTable = new JTable(trainsTableModel);
        Theme.styleTable(trainsTable);

        trainsTable.getColumnModel().getColumn(0).setPreferredWidth(80);
        trainsTable.getColumnModel().getColumn(1).setPreferredWidth(200);
        trainsTable.getColumnModel().getColumn(6).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object val, boolean isSel, boolean hasFoc, int r, int c) {
                Component comp = super.getTableCellRendererComponent(t, val, isSel, hasFoc, r, c);
                setHorizontalAlignment(SwingConstants.CENTER);
                try {
                    int s = Integer.parseInt(val.toString());
                    comp.setForeground(s > 20 ? AppColors.DARK_GREEN : (s > 0 ? AppColors.ORANGE_TEXT : AppColors.RED));
                    setFont(new Font("Segoe UI", Font.BOLD, 13));
                } catch (Exception ignored) {}
                return comp;
            }
        });

        // Double-click to book
        trainsTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    bookSelectedTrain();
                }
            }
        });

        JScrollPane tableScroll = new JScrollPane(trainsTable);
        tableScroll.setBorder(new LineBorder(AppColors.BORDER, 1));
        panel.add(tableScroll, BorderLayout.CENTER);

        // Bottom Action Bar
        JPanel bottomBar = new JPanel(new BorderLayout());
        bottomBar.setOpaque(false);

        JLabel hint = new JLabel("💡 Tip: Select a train row and click 'Book Selected Train' or double-click any train to book.");
        hint.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        hint.setForeground(AppColors.MUTED);

        JButton bookBtn = Theme.button("🎟️ Book Selected Train ➔", AppColors.GREEN);
        bookBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        bookBtn.addActionListener(e -> bookSelectedTrain());

        bottomBar.add(hint, BorderLayout.WEST);
        bottomBar.add(bookBtn, BorderLayout.EAST);
        panel.add(bottomBar, BorderLayout.SOUTH);

        return panel;
    }

    // ============================================================
    // TAB 2: BOOKED TICKETS & CANCELLATION
    // ============================================================
    private JPanel createBookedTicketsTab() {
        JPanel panel = new JPanel(new BorderLayout(0, 12));
        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(16, 16, 16, 16));

        // Top Filter & Search Controls
        JPanel filterBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 4));
        filterBar.setOpaque(false);

        JLabel searchLbl = new JLabel("Search Tickets:");
        searchLbl.setFont(new Font("Segoe UI", Font.BOLD, 12));

        ticketSearchField = Theme.field();
        ticketSearchField.setPreferredSize(new Dimension(220, 38));
        ticketSearchField.setToolTipText("Search by PNR, Passenger, or Train...");
        ticketSearchField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                refreshBookings();
            }
        });

        JLabel statusLbl = new JLabel("Status:");
        statusLbl.setFont(new Font("Segoe UI", Font.BOLD, 12));

        statusFilterCombo = Theme.combo("All Status", "CONFIRMED", "CANCELLED");
        statusFilterCombo.setPreferredSize(new Dimension(140, 38));
        statusFilterCombo.addActionListener(e -> refreshBookings());

        filterBar.add(searchLbl);
        filterBar.add(ticketSearchField);
        filterBar.add(statusLbl);
        filterBar.add(statusFilterCombo);

        if (user.isAdmin()) {
            JLabel scopeLbl = new JLabel("Scope:");
            scopeLbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
            scopeFilterCombo = Theme.combo("My Bookings", "All System Bookings");
            scopeFilterCombo.setPreferredSize(new Dimension(170, 38));
            scopeFilterCombo.addActionListener(e -> refreshBookings());
            filterBar.add(scopeLbl);
            filterBar.add(scopeFilterCombo);
        }

        // Action Buttons Row (View Ticket, Cancel Ticket, Copy PNR, Export CSV)
        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 4));
        btnRow.setOpaque(false);

        JButton viewTicketBtn = Theme.button("📄 View E-Ticket", AppColors.PRIMARY_BLUE);
        JButton cancelTicketBtn = Theme.button("❌ Cancel Ticket", AppColors.RED);
        JButton copyPnrBtn = Theme.button("📋 Copy PNR", AppColors.BG);
        copyPnrBtn.setForeground(AppColors.TEXT);
        copyPnrBtn.setBorder(new LineBorder(AppColors.BORDER, 1, true));

        JButton exportBtn = Theme.button("💾 Export CSV", AppColors.DARK_GREEN);

        viewTicketBtn.addActionListener(e -> viewSelectedTicket());
        cancelTicketBtn.addActionListener(e -> cancelSelectedTicket());
        copyPnrBtn.addActionListener(e -> copySelectedPnr());
        exportBtn.addActionListener(e -> exportBookingsToCsv());

        btnRow.add(viewTicketBtn);
        btnRow.add(cancelTicketBtn);
        btnRow.add(copyPnrBtn);
        btnRow.add(exportBtn);

        JPanel topContainer = new JPanel(new BorderLayout());
        topContainer.setOpaque(false);
        topContainer.add(filterBar, BorderLayout.WEST);
        topContainer.add(btnRow, BorderLayout.EAST);
        panel.add(topContainer, BorderLayout.NORTH);

        // Table
        String[] cols = {"PNR", "Passenger", "Age/Sex", "Train", "Route", "Date", "Class", "Seats", "Fare (₹)", "Status"};
        bookingsTableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };

        bookingsTable = new JTable(bookingsTableModel);
        Theme.styleTable(bookingsTable);

        // Status Column Badge
        bookingsTable.getColumnModel().getColumn(9).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object val, boolean isSel, boolean hasFoc, int r, int c) {
                String status = (val != null) ? val.toString() : "CONFIRMED";
                boolean isCancelled = "CANCELLED".equalsIgnoreCase(status);
                return Theme.createBadge(status,
                        isCancelled ? AppColors.LIGHT_RED : AppColors.LIGHT_GREEN,
                        isCancelled ? AppColors.RED_TEXT : AppColors.GREEN_TEXT);
            }
        });

        // Double-click to view ticket receipt
        bookingsTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    viewSelectedTicket();
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    showTableContextMenu(e);
                }
            }

            @Override
            public void mousePressed(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    showTableContextMenu(e);
                }
            }
        });

        JScrollPane scroll = new JScrollPane(bookingsTable);
        scroll.setBorder(new LineBorder(AppColors.BORDER, 1));
        panel.add(scroll, BorderLayout.CENTER);

        // Bottom Summary Bar
        JPanel bottomBar = new JPanel(new BorderLayout());
        bottomBar.setOpaque(false);
        bottomBar.setBorder(new EmptyBorder(8, 4, 4, 4));

        bookingSummaryLabel = new JLabel("Showing 0 bookings");
        bookingSummaryLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        bookingSummaryLabel.setForeground(AppColors.TEXT_SECONDARY);

        JLabel rightHint = new JLabel("💡 Right-click any ticket row for cancellation and quick options.");
        rightHint.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        rightHint.setForeground(AppColors.MUTED);

        bottomBar.add(bookingSummaryLabel, BorderLayout.WEST);
        bottomBar.add(rightHint, BorderLayout.EAST);
        panel.add(bottomBar, BorderLayout.SOUTH);

        return panel;
    }

    private void showTableContextMenu(MouseEvent e) {
        int r = bookingsTable.rowAtPoint(e.getPoint());
        if (r >= 0 && r < bookingsTable.getRowCount()) {
            bookingsTable.setRowSelectionInterval(r, r);
        } else {
            return;
        }

        Ticket t = currentBookingsList.get(r);
        JPopupMenu menu = new JPopupMenu();

        JMenuItem viewItem = new JMenuItem("📄 View E-Ticket Receipt");
        viewItem.addActionListener(ev -> viewSelectedTicket());
        menu.add(viewItem);

        JMenuItem copyItem = new JMenuItem("📋 Copy PNR (" + t.getPnr() + ")");
        copyItem.addActionListener(ev -> copySelectedPnr());
        menu.add(copyItem);

        menu.addSeparator();

        JMenuItem cancelItem = new JMenuItem("❌ Cancel Ticket");
        if (t.isCancelled()) {
            cancelItem.setEnabled(false);
            cancelItem.setText("❌ Ticket Already Cancelled");
        } else {
            cancelItem.setForeground(AppColors.RED);
            cancelItem.addActionListener(ev -> cancelSelectedTicket());
        }
        menu.add(cancelItem);

        menu.show(e.getComponent(), e.getX(), e.getY());
    }

    // ============================================================
    // TAB 3: PNR INQUIRY
    // ============================================================
    private JPanel createPnrInquiryTab() {
        JPanel outer = new JPanel(new BorderLayout());
        outer.setBackground(Color.WHITE);
        outer.setBorder(new EmptyBorder(25, 30, 25, 30));

        JPanel topSearch = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        topSearch.setOpaque(false);

        JLabel label = new JLabel("Enter PNR Number:");
        label.setFont(new Font("Segoe UI", Font.BOLD, 14));
        label.setForeground(AppColors.NAVY);

        pnrSearchField = Theme.field();
        pnrSearchField.setPreferredSize(new Dimension(280, 44));

        JButton searchBtn = Theme.button("🔍 Check PNR Status", AppColors.PRIMARY_BLUE);
        searchBtn.addActionListener(e -> executePnrSearch());
        pnrSearchField.addActionListener(e -> executePnrSearch());

        topSearch.add(label);
        topSearch.add(pnrSearchField);
        topSearch.add(searchBtn);
        outer.add(topSearch, BorderLayout.NORTH);

        pnrResultPanel = new JPanel(new BorderLayout());
        pnrResultPanel.setOpaque(false);
        pnrResultPanel.setBorder(new EmptyBorder(25, 0, 0, 0));

        JLabel placeholder = new JLabel("Enter an 8-character PNR code above to inspect booking status and train schedule.", SwingConstants.CENTER);
        placeholder.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        placeholder.setForeground(AppColors.MUTED);
        pnrResultPanel.add(placeholder, BorderLayout.CENTER);

        outer.add(pnrResultPanel, BorderLayout.CENTER);

        return outer;
    }

    // ============================================================
    // TAB 4: ADMIN TRAIN MANAGEMENT
    // ============================================================
    private JPanel createAdminTab() {
        JPanel panel = new JPanel(new BorderLayout(0, 15));
        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(16, 16, 16, 16));

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        JLabel title = new JLabel("Train Fleet Management (Admin Portal)");
        title.setFont(new Font("Segoe UI", Font.BOLD, 18));
        title.setForeground(AppColors.NAVY);

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        btnRow.setOpaque(false);

        JButton addTrainBtn = Theme.button("➕ Add New Train", AppColors.GREEN);
        JButton deleteTrainBtn = Theme.button("🗑️ Delete Selected Train", AppColors.RED);

        addTrainBtn.addActionListener(e -> showAddTrainDialog());
        deleteTrainBtn.addActionListener(e -> deleteSelectedTrain());

        btnRow.add(addTrainBtn);
        btnRow.add(deleteTrainBtn);

        header.add(title, BorderLayout.WEST);
        header.add(btnRow, BorderLayout.EAST);
        panel.add(header, BorderLayout.NORTH);

        String[] cols = {"Number", "Name", "From", "To", "Departure", "Arrival", "Total Seats", "Fare (₹)"};
        adminTrainsModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };

        adminTrainsTable = new JTable(adminTrainsModel);
        Theme.styleTable(adminTrainsTable);

        JScrollPane scroll = new JScrollPane(adminTrainsTable);
        scroll.setBorder(new LineBorder(AppColors.BORDER, 1));
        panel.add(scroll, BorderLayout.CENTER);

        return panel;
    }

    // ============================================================
    // ACTIONS & DATA REFRESH
    // ============================================================

    public void refreshAllData() {
        updateDbStatus();
        executeTrainSearch();
        refreshBookings();
        refreshStats();
        if (user.isAdmin()) {
            refreshAdminTrains();
        }
    }

    private void updateDbStatus() {
        boolean connected = service.isDatabaseConnected();
        if (connected) {
            dbStatusBtn.setText("🟢 MySQL Connected");
            dbStatusBtn.setForeground(AppColors.DARK_GREEN);
        } else {
            dbStatusBtn.setText("🟡 MySQL Disconnected (Configure)");
            dbStatusBtn.setForeground(AppColors.ORANGE_TEXT);
        }
    }

    private void executeTrainSearch() {
        String from = fromField != null ? fromField.getText().trim() : "";
        String to = toField != null ? toField.getText().trim() : "";

        currentTrainsList = service.searchTrains(from, to);
        trainsTableModel.setRowCount(0);

        for (Train t : currentTrainsList) {
            trainsTableModel.addRow(new Object[]{
                    t.getNumber(),
                    t.getName(),
                    t.getSource(),
                    t.getDestination(),
                    t.getDeparture(),
                    t.getArrival(),
                    t.getSeats(),
                    String.format("%.2f", t.getFare())
            });
        }
    }

    private void refreshBookings() {
        if (bookingsTableModel == null) return;

        String kw = ticketSearchField != null ? ticketSearchField.getText().trim() : "";
        String status = statusFilterCombo != null ? (String) statusFilterCombo.getSelectedItem() : "All Status";
        if ("All Status".equalsIgnoreCase(status)) status = "ALL";

        boolean showAll = false;
        if (user.isAdmin() && scopeFilterCombo != null) {
            showAll = "All System Bookings".equalsIgnoreCase((String) scopeFilterCombo.getSelectedItem());
        }

        currentBookingsList = service.searchTickets(user.getUsername(), kw, status, showAll);
        bookingsTableModel.setRowCount(0);

        int confirmedCount = 0;
        int cancelledCount = 0;
        double totalExpense = 0;

        for (Ticket t : currentBookingsList) {
            if (t.isCancelled()) {
                cancelledCount++;
            } else {
                confirmedCount++;
                totalExpense += t.getTotalFare();
            }

            bookingsTableModel.addRow(new Object[]{
                    t.getPnr(),
                    t.getPassengerName(),
                    t.getAge() + " / " + t.getGender(),
                    t.getTrainNumber() + " " + t.getTrainName(),
                    t.getSource() + " ➔ " + t.getDestination(),
                    t.getJourneyDate(),
                    t.getTravelClass(),
                    t.getSeats(),
                    String.format("%.2f", t.getTotalFare()),
                    t.getStatus()
            });
        }

        if (bookingSummaryLabel != null) {
            bookingSummaryLabel.setText("Showing: " + currentBookingsList.size() + " tickets  |  " +
                    "Confirmed: " + confirmedCount + "  |  " +
                    "Cancelled: " + cancelledCount + "  |  " +
                    "Total Confirmed Value: ₹" + String.format("%.2f", totalExpense));
        }
    }

    private void refreshStats() {
        int[] stats = service.getStats(user.getUsername());
        totalTrainsLabel.setText(String.valueOf(stats[0]));
        myBookingsLabel.setText(String.valueOf(stats[1]));
        activeTicketsLabel.setText(String.valueOf(stats[2]));
    }

    private void refreshAdminTrains() {
        if (adminTrainsModel == null) return;
        List<Train> list = service.getTrains();
        adminTrainsModel.setRowCount(0);
        for (Train t : list) {
            adminTrainsModel.addRow(new Object[]{
                    t.getNumber(),
                    t.getName(),
                    t.getSource(),
                    t.getDestination(),
                    t.getDeparture(),
                    t.getArrival(),
                    t.getSeats(),
                    String.format("%.2f", t.getFare())
            });
        }
    }

    private void bookSelectedTrain() {
        int row = trainsTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select a train from the list to book.", "No Train Selected", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Train selected = currentTrainsList.get(row);
        if (selected.getSeats() <= 0) {
            JOptionPane.showMessageDialog(this, "Sorry, this train is fully booked. Please select another train.", "Seats Not Available", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String date = dateField.getText().trim();
        new BookingDialog(this, service, user, selected, date, this::refreshAllData).setVisible(true);
    }

    private void viewSelectedTicket() {
        int row = bookingsTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select a booking from the table to view.", "No Ticket Selected", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Ticket ticket = currentBookingsList.get(row);
        new TicketReceiptDialog(this, ticket).setVisible(true);
    }

    private void copySelectedPnr() {
        int row = bookingsTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select a ticket first.", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        Ticket ticket = currentBookingsList.get(row);
        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(ticket.getPnr()), null);
        JOptionPane.showMessageDialog(this, "Copied PNR: " + ticket.getPnr(), "Copied", JOptionPane.INFORMATION_MESSAGE);
    }

    private void cancelSelectedTicket() {
        int row = bookingsTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select a booking from the table to cancel.", "No Ticket Selected", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Ticket ticket = currentBookingsList.get(row);
        if (ticket.isCancelled()) {
            JOptionPane.showMessageDialog(this, "This ticket is already cancelled.", "Already Cancelled", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        // Detailed Cancellation Confirmation Dialog
        JPanel cancelPrompt = new JPanel(new GridLayout(0, 1, 6, 6));
        cancelPrompt.add(new JLabel("<html><b>Are you sure you want to cancel this reservation?</b></html>"));
        cancelPrompt.add(new JLabel("• PNR: " + ticket.getPnr()));
        cancelPrompt.add(new JLabel("• Train: " + ticket.getTrainNumber() + " - " + ticket.getTrainName()));
        cancelPrompt.add(new JLabel("• Passenger: " + ticket.getPassengerName() + " (" + ticket.getSeats() + " seat(s))"));
        cancelPrompt.add(new JLabel("• Original Fare: ₹" + String.format("%.2f", ticket.getTotalFare())));
        cancelPrompt.add(new JLabel("<html><font color='#059669'><b>• Refund Policy:</b> 100% Refund (₹" + String.format("%.2f", ticket.getTotalFare()) + ")</font></html>"));
        cancelPrompt.add(new JLabel("<html><font color='#2563eb'>• Seats will be immediately restored to train inventory in MySQL.</font></html>"));

        int confirm = JOptionPane.showConfirmDialog(this,
                cancelPrompt,
                "Cancel Ticket Confirmation",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            boolean success = service.cancelTicket(user.getUsername(), ticket.getPnr());
            if (success) {
                JOptionPane.showMessageDialog(this,
                        "✓ Ticket cancelled successfully!\n" +
                                "Seats have been released back to train " + ticket.getTrainNumber() + " in MySQL.\n" +
                                "Refund of ₹" + String.format("%.2f", ticket.getTotalFare()) + " processed.",
                        "Ticket Cancelled",
                        JOptionPane.INFORMATION_MESSAGE);
                refreshAllData();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to cancel ticket. Please check permissions or connection.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void exportBookingsToCsv() {
        if (currentBookingsList == null || currentBookingsList.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No bookings available to export.", "Empty", JOptionPane.WARNING_MESSAGE);
            return;
        }

        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new File("RailConnect_Bookings.csv"));
        int res = chooser.showSaveDialog(this);
        if (res == JFileChooser.APPROVE_OPTION) {
            File f = chooser.getSelectedFile();
            try (FileWriter fw = new FileWriter(f)) {
                fw.write("PNR,Passenger,Age,Gender,TrainNumber,TrainName,Route,Date,Class,Seats,Fare,Status,BookedTime\n");
                for (Ticket t : currentBookingsList) {
                    fw.write(String.format("\"%s\",\"%s\",%d,\"%s\",\"%s\",\"%s\",\"%s ➔ %s\",\"%s\",\"%s\",%d,%.2f,\"%s\",\"%s\"\n",
                            t.getPnr(), t.getPassengerName(), t.getAge(), t.getGender(),
                            t.getTrainNumber(), t.getTrainName(), t.getSource(), t.getDestination(),
                            t.getJourneyDate(), t.getTravelClass(), t.getSeats(), t.getTotalFare(),
                            t.getStatus(), t.getBookingTime()));
                }
                JOptionPane.showMessageDialog(this, "Bookings exported successfully to:\n" + f.getAbsolutePath(), "Export Complete", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error exporting CSV: " + ex.getMessage(), "Export Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void executePnrSearch() {
        String pnr = pnrSearchField.getText().trim();
        if (pnr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter a valid PNR number.", "Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Ticket t = service.getTicketByPnr(pnr);
        pnrResultPanel.removeAll();

        if (t == null) {
            JLabel notFound = new JLabel("❌ No ticket found matching PNR: " + pnr, SwingConstants.CENTER);
            notFound.setFont(new Font("Segoe UI", Font.BOLD, 15));
            notFound.setForeground(AppColors.RED);
            pnrResultPanel.add(notFound, BorderLayout.CENTER);
        } else {
            JPanel card = Theme.card();
            card.setLayout(new BorderLayout(20, 20));
            card.setBorder(BorderFactory.createCompoundBorder(
                    new LineBorder(AppColors.BORDER, 1, true),
                    new EmptyBorder(20, 25, 20, 25)
            ));

            JPanel header = new JPanel(new BorderLayout());
            header.setOpaque(false);
            JLabel title = new JLabel("PNR: " + t.getPnr() + "  |  " + t.getTrainNumber() + " " + t.getTrainName());
            title.setFont(new Font("Segoe UI", Font.BOLD, 16));
            title.setForeground(AppColors.NAVY);

            JLabel status = Theme.createBadge(t.getStatus(),
                    t.isCancelled() ? AppColors.LIGHT_RED : AppColors.LIGHT_GREEN,
                    t.isCancelled() ? AppColors.RED_TEXT : AppColors.GREEN_TEXT);

            header.add(title, BorderLayout.WEST);
            header.add(status, BorderLayout.EAST);
            card.add(header, BorderLayout.NORTH);

            JPanel grid = new JPanel(new GridLayout(4, 2, 15, 10));
            grid.setOpaque(false);
            grid.add(new JLabel("Journey Route: " + t.getSource() + " ➔ " + t.getDestination()));
            grid.add(new JLabel("Travel Date: " + t.getJourneyDate()));
            grid.add(new JLabel("Passenger: " + t.getPassengerName() + " (" + t.getAge() + ", " + t.getGender() + ")"));
            grid.add(new JLabel("Class / Berth: " + t.getTravelClass() + " / " + t.getBerth()));
            grid.add(new JLabel("Reserved Seats: " + t.getSeats() + " Seat(s)"));
            grid.add(new JLabel("Total Fare: ₹" + String.format("%.2f", t.getTotalFare())));
            grid.add(new JLabel("Booked At: " + t.getBookingTime()));
            grid.add(new JLabel("Booked By: @" + t.getUsername()));
            card.add(grid, BorderLayout.CENTER);

            JPanel pnrActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
            pnrActions.setOpaque(false);

            JButton printBtn = Theme.button("📄 Full E-Ticket Receipt", AppColors.PRIMARY_BLUE);
            printBtn.addActionListener(e -> new TicketReceiptDialog(this, t).setVisible(true));
            pnrActions.add(printBtn);

            if (!t.isCancelled()) {
                JButton cancelBtn = Theme.button("❌ Cancel This Ticket", AppColors.RED);
                cancelBtn.addActionListener(e -> {
                    boolean ok = service.cancelTicket(user.getUsername(), t.getPnr());
                    if (ok) {
                        JOptionPane.showMessageDialog(this, "Ticket cancelled successfully!", "Cancelled", JOptionPane.INFORMATION_MESSAGE);
                        refreshAllData();
                        executePnrSearch();
                    }
                });
                pnrActions.add(cancelBtn);
            }

            card.add(pnrActions, BorderLayout.SOUTH);
            pnrResultPanel.add(card, BorderLayout.NORTH);
        }

        pnrResultPanel.revalidate();
        pnrResultPanel.repaint();
    }

    private void showAddTrainDialog() {
        JTextField numF = Theme.field();
        JTextField nameF = Theme.field();
        JTextField srcF = Theme.field();
        JTextField dstF = Theme.field();
        JTextField depF = Theme.field();
        JTextField arrF = Theme.field();
        JTextField seatsF = Theme.field();
        JTextField fareF = Theme.field();

        JPanel p = new JPanel(new GridLayout(0, 2, 10, 8));
        p.add(new JLabel("Train Number:")); p.add(numF);
        p.add(new JLabel("Train Name:")); p.add(nameF);
        p.add(new JLabel("Source Station:")); p.add(srcF);
        p.add(new JLabel("Destination Station:")); p.add(dstF);
        p.add(new JLabel("Departure (HH:MM):")); p.add(depF);
        p.add(new JLabel("Arrival (HH:MM):")); p.add(arrF);
        p.add(new JLabel("Total Seats:")); p.add(seatsF);
        p.add(new JLabel("Fare (₹):")); p.add(fareF);

        int res = JOptionPane.showConfirmDialog(this, p, "Add New Train", JOptionPane.OK_CANCEL_OPTION);
        if (res == JOptionPane.OK_OPTION) {
            try {
                Train newTrain = new Train(
                        numF.getText().trim(),
                        nameF.getText().trim(),
                        srcF.getText().trim(),
                        dstF.getText().trim(),
                        depF.getText().trim(),
                        arrF.getText().trim(),
                        Integer.parseInt(seatsF.getText().trim()),
                        Double.parseDouble(fareF.getText().trim())
                );
                boolean ok = service.addTrain(newTrain);
                if (ok) {
                    JOptionPane.showMessageDialog(this, "Train added successfully to MySQL!", "Train Added", JOptionPane.INFORMATION_MESSAGE);
                    refreshAllData();
                } else {
                    JOptionPane.showMessageDialog(this, "Failed to add train. Check train number uniqueness.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Invalid inputs: " + ex.getMessage(), "Input Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void deleteSelectedTrain() {
        int r = adminTrainsTable.getSelectedRow();
        if (r < 0) {
            JOptionPane.showMessageDialog(this, "Select a train to delete.", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String trainNum = (String) adminTrainsModel.getValueAt(r, 0);
        int res = JOptionPane.showConfirmDialog(this, "Are you sure you want to delete train " + trainNum + " from MySQL?", "Delete Train", JOptionPane.YES_NO_OPTION);
        if (res == JOptionPane.YES_OPTION) {
            service.deleteTrain(trainNum);
            refreshAllData();
        }
    }

    private void showCalendarPicker() {
        JDialog dialog = new JDialog(this, "Select Travel Date", true);
        dialog.setSize(380, 360);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());

        Calendar cal = Calendar.getInstance();
        JPanel calPanel = new JPanel(new BorderLayout(0, 10));
        calPanel.setBorder(new EmptyBorder(12, 12, 12, 12));

        JPanel top = new JPanel(new BorderLayout());
        JButton prev = new JButton("‹");
        JButton next = new JButton("›");
        JLabel monthLabel = new JLabel("", SwingConstants.CENTER);
        monthLabel.setFont(new Font("Segoe UI", Font.BOLD, 15));

        top.add(prev, BorderLayout.WEST);
        top.add(monthLabel, BorderLayout.CENTER);
        top.add(next, BorderLayout.EAST);
        calPanel.add(top, BorderLayout.NORTH);

        JPanel daysGrid = new JPanel(new GridLayout(0, 7, 2, 2));
        calPanel.add(daysGrid, BorderLayout.CENTER);

        Runnable drawCal = () -> {
            daysGrid.removeAll();
            String[] heads = {"Su", "Mo", "Tu", "We", "Th", "Fr", "Sa"};
            for (String h : heads) {
                JLabel l = new JLabel(h, SwingConstants.CENTER);
                l.setFont(new Font("Segoe UI", Font.BOLD, 11));
                l.setForeground(AppColors.MUTED);
                daysGrid.add(l);
            }

            monthLabel.setText(new SimpleDateFormat("MMMM yyyy").format(cal.getTime()));

            Calendar first = (Calendar) cal.clone();
            first.set(Calendar.DAY_OF_MONTH, 1);
            int startDay = first.get(Calendar.DAY_OF_WEEK) - 1;
            int maxDays = cal.getActualMaximum(Calendar.DAY_OF_MONTH);

            for (int i = 0; i < startDay; i++) daysGrid.add(new JLabel(""));

            for (int d = 1; d <= maxDays; d++) {
                final int day = d;
                JButton dayBtn = new JButton(String.valueOf(d));
                dayBtn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
                dayBtn.setFocusPainted(false);
                dayBtn.setBackground(Color.WHITE);
                dayBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
                dayBtn.addActionListener(e -> {
                    Calendar picked = (Calendar) cal.clone();
                    picked.set(Calendar.DAY_OF_MONTH, day);
                    dateField.setText(new SimpleDateFormat("dd-MM-yyyy").format(picked.getTime()));
                    dialog.dispose();
                });
                daysGrid.add(dayBtn);
            }
            daysGrid.revalidate();
            daysGrid.repaint();
        };

        prev.addActionListener(e -> { cal.add(Calendar.MONTH, -1); drawCal.run(); });
        next.addActionListener(e -> { cal.add(Calendar.MONTH, 1); drawCal.run(); });

        drawCal.run();
        dialog.add(calPanel, BorderLayout.CENTER);
        dialog.setVisible(true);
    }

    private JPanel createLabeledField(String labelText, JComponent field) {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setOpaque(false);

        JLabel l = new JLabel(labelText);
        l.setFont(new Font("Segoe UI", Font.BOLD, 12));
        l.setForeground(AppColors.TEXT_SECONDARY);

        p.add(l);
        p.add(Box.createVerticalStrut(4));
        p.add(field);
        return p;
    }
}
