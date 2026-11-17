package railwayreservation;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public final class Theme {

    private Theme() {}

    public static void setup() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}

        // Enable Anti-Aliased fonts everywhere
        System.setProperty("awt.useSystemAAFontSettings", "on");
        System.setProperty("swing.aatext", "true");
    }

    // ---------------- TYPOGRAPHY ----------------

    public static JLabel title(String text, int size) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.BOLD, size));
        label.setForeground(AppColors.TEXT);
        return label;
    }

    public static JLabel subtitle(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        label.setForeground(AppColors.MUTED);
        return label;
    }

    // ---------------- BUTTONS ----------------

    public static JButton button(String text, Color background) {
        Color hoverColor = darken(background, 0.12f);
        return createRoundedButton(text, background, hoverColor, Color.WHITE, 8);
    }

    public static JButton createRoundedButton(String text, Color bg, Color hoverBg, Color fg, int radius) {
        JButton button = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (getModel().isPressed()) {
                    g2.setColor(darken(hoverBg, 0.1f));
                } else if (getModel().isRollover()) {
                    g2.setColor(hoverBg);
                } else {
                    g2.setColor(bg);
                }
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), radius, radius);
                g2.dispose();
                super.paintComponent(g);
            }
        };

        button.setFont(new Font("Segoe UI", Font.BOLD, 13));
        button.setForeground(fg);
        button.setContentAreaFilled(false);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setOpaque(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setBorder(new EmptyBorder(10, 20, 10, 20));

        return button;
    }

    // ---------------- TEXT FIELD ----------------

    public static JTextField field() {
        return createPlaceholderField("");
    }

    public static JTextField createPlaceholderField(String placeholder) {
        JTextField field = new JTextField();
        field.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        field.setForeground(AppColors.TEXT);
        field.setBackground(Color.WHITE);
        field.setPreferredSize(new Dimension(0, 44));

        LineBorder defaultBorder = new LineBorder(AppColors.BORDER, 1, true);
        LineBorder activeBorder = new LineBorder(AppColors.BORDER_FOCUS, 2, true);

        field.setBorder(BorderFactory.createCompoundBorder(
                defaultBorder,
                new EmptyBorder(0, 14, 0, 14)
        ));

        field.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                field.setBorder(BorderFactory.createCompoundBorder(
                        activeBorder,
                        new EmptyBorder(0, 13, 0, 13)
                ));
            }

            @Override
            public void focusLost(FocusEvent e) {
                field.setBorder(BorderFactory.createCompoundBorder(
                        defaultBorder,
                        new EmptyBorder(0, 14, 0, 14)
                ));
            }
        });

        return field;
    }

    // ---------------- PASSWORD FIELD ----------------

    public static JPasswordField passwordField() {
        JPasswordField field = new JPasswordField();
        field.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        field.setForeground(AppColors.TEXT);
        field.setBackground(Color.WHITE);
        field.setPreferredSize(new Dimension(0, 44));

        LineBorder defaultBorder = new LineBorder(AppColors.BORDER, 1, true);
        LineBorder activeBorder = new LineBorder(AppColors.BORDER_FOCUS, 2, true);

        field.setBorder(BorderFactory.createCompoundBorder(
                defaultBorder,
                new EmptyBorder(0, 14, 0, 14)
        ));

        field.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                field.setBorder(BorderFactory.createCompoundBorder(
                        activeBorder,
                        new EmptyBorder(0, 13, 0, 13)
                ));
            }

            @Override
            public void focusLost(FocusEvent e) {
                field.setBorder(BorderFactory.createCompoundBorder(
                        defaultBorder,
                        new EmptyBorder(0, 14, 0, 14)
                ));
            }
        });

        return field;
    }

    // ---------------- COMBO BOX ----------------

    public static JComboBox<String> combo(String... values) {
        JComboBox<String> combo = new JComboBox<>(values);
        combo.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        combo.setBackground(Color.WHITE);
        combo.setForeground(AppColors.TEXT);
        combo.setPreferredSize(new Dimension(0, 44));
        combo.setBorder(new LineBorder(AppColors.BORDER, 1, true));
        return combo;
    }

    // ---------------- CARDS ----------------

    public static JPanel card() {
        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2.setColor(AppColors.BORDER);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);
                g2.dispose();
            }
        };
        panel.setOpaque(false);
        panel.setBackground(AppColors.CARD);
        panel.setBorder(new EmptyBorder(22, 22, 22, 22));
        return panel;
    }

    // ---------------- TABLE STYLING ----------------

    public static void styleTable(JTable table) {
        table.setRowHeight(42);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        table.setForeground(AppColors.TEXT);
        table.setGridColor(AppColors.BORDER);
        table.setShowVerticalLines(false);
        table.setShowHorizontalLines(true);
        table.setSelectionBackground(AppColors.TABLE_SELECTION);
        table.setSelectionForeground(AppColors.TEXT);
        table.setFillsViewportHeight(true);

        // Header
        JTableHeader header = table.getTableHeader();
        header.setPreferredSize(new Dimension(0, 42));
        header.setFont(new Font("Segoe UI", Font.BOLD, 13));
        header.setBackground(AppColors.TABLE_HEADER_BG);
        header.setForeground(AppColors.TABLE_HEADER_TEXT);
        header.setReorderingAllowed(false);
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, AppColors.BORDER));

        // Default Alternating Row Renderer
        DefaultTableCellRenderer renderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object value, boolean isSelected,
                                                           boolean hasFocus, int row, int col) {
                Component c = super.getTableCellRendererComponent(t, value, isSelected, hasFocus, row, col);
                setBorder(new EmptyBorder(0, 12, 0, 12));

                if (!isSelected) {
                    c.setBackground(row % 2 == 0 ? Color.WHITE : AppColors.TABLE_ROW_ALT);
                }
                return c;
            }
        };
        table.setDefaultRenderer(Object.class, renderer);
    }

    // ---------------- BADGE LABEL ----------------

    public static JLabel createBadge(String text, Color bg, Color fg) {
        JLabel badge = new JLabel(text, SwingConstants.CENTER) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(bg);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 14, 14);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        badge.setFont(new Font("Segoe UI", Font.BOLD, 11));
        badge.setForeground(fg);
        badge.setOpaque(false);
        badge.setBorder(new EmptyBorder(4, 10, 4, 10));
        return badge;
    }

    // ---------------- COLOR UTILITY ----------------

    public static Color darken(Color color, float fraction) {
        int r = Math.max(0, Math.round(color.getRed() * (1 - fraction)));
        int g = Math.max(0, Math.round(color.getGreen() * (1 - fraction)));
        int b = Math.max(0, Math.round(color.getBlue() * (1 - fraction)));
        return new Color(r, g, b, color.getAlpha());
    }
}
