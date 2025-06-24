package clearorder;

import clearorder.dao.UserDAO;
import clearorder.model.User;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class LoginDialog extends JDialog {
    private JTextField userField;
    private JPasswordField passwordField;
    private boolean succeeded;
    private User loggedInUser;
    public JPanel mainPanel;
    private UserDAO userDAO;

    public LoginDialog() {
        this.userDAO = new UserDAO();
        setTitle("Sign in to your department");
        setModal(true);
        setUndecorated(true);
        setSize(400, 300); // Slightly taller to accommodate centered content
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        getRootPane().setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200), 1));

        // Main panel with equal left and right margins
        mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBackground(Color.WHITE);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 50, 40, 50)); // Increased side padding

        // Close button at top right
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        topPanel.setBackground(Color.WHITE);
        JButton closeButton = new JButton("×");
        closeButton.setFont(new Font("Arial", Font.PLAIN, 24));
        closeButton.setForeground(new Color(100, 100, 100));
        closeButton.setBorderPainted(false);
        closeButton.setContentAreaFilled(false);
        closeButton.setFocusPainted(false);
        closeButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        closeButton.addActionListener(e -> dispose());
        topPanel.add(closeButton);
        mainPanel.add(topPanel);

        // Center panel for the form
        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        centerPanel.setBackground(Color.WHITE);

        // Title
        JLabel titleLabel = new JLabel("Sign in to your department");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        centerPanel.add(titleLabel);
        centerPanel.add(Box.createVerticalStrut(30)); // More space after title

        // Username field
        JLabel userLabel = new JLabel("Department user name / user id");
        userLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        userLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        centerPanel.add(userLabel);
        centerPanel.add(Box.createVerticalStrut(5));

        userField = new JTextField();
        userField.setFont(new Font("Arial", Font.PLAIN, 14));
        userField.setMaximumSize(new Dimension(300, 35)); // Fixed width
        userField.setAlignmentX(Component.CENTER_ALIGNMENT);
        userField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200)),
                BorderFactory.createEmptyBorder(5, 10, 5, 10)));
        centerPanel.add(userField);
        centerPanel.add(Box.createVerticalStrut(15));

        // Password field
        JLabel passLabel = new JLabel("Password");
        passLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        passLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        centerPanel.add(passLabel);
        centerPanel.add(Box.createVerticalStrut(5));

        JPanel passPanel = new JPanel();
        passPanel.setLayout(new BoxLayout(passPanel, BoxLayout.X_AXIS));
        passPanel.setMaximumSize(new Dimension(300, 35)); // Fixed width
        passPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        passPanel.setBackground(Color.WHITE);
        passPanel.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200)));

        passwordField = new JPasswordField();
        passwordField.setFont(new Font("Arial", Font.PLAIN, 14));
        passwordField.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        passPanel.add(passwordField);

        JButton showHideButton = new JButton("Hide");
        showHideButton.setFont(new Font("Arial", Font.PLAIN, 12));
        showHideButton.setBorderPainted(false);
        showHideButton.setContentAreaFilled(false);
        showHideButton.setFocusPainted(false);
        showHideButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        showHideButton.addActionListener(e -> {
            if (passwordField.getEchoChar() == 0) {
                passwordField.setEchoChar('•');
                showHideButton.setText("Show");
            } else {
                passwordField.setEchoChar((char) 0);
                showHideButton.setText("Hide");
            }
        });
        passPanel.add(showHideButton);
        centerPanel.add(passPanel);
        centerPanel.add(Box.createVerticalStrut(25));

        // Sign in button
        JButton loginButton = new JButton("Sign in");
        loginButton.setFont(new Font("Arial", Font.BOLD, 14));
        loginButton.setForeground(Color.WHITE);
        loginButton.setBackground(new Color(219, 68, 23));
        loginButton.setMaximumSize(new Dimension(300, 40)); // Fixed width
        loginButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        loginButton.setBorderPainted(false);
        loginButton.setFocusPainted(false);
        loginButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        loginButton.addActionListener(e -> login());
        centerPanel.add(loginButton);

        mainPanel.add(centerPanel);
        add(mainPanel);

        // Make the window draggable
        MouseAdapter dragListener = new MouseAdapter() {
            private Point clickPoint;

            @Override
            public void mousePressed(MouseEvent e) {
                clickPoint = e.getPoint();
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                Point dragPoint = e.getLocationOnScreen();
                setLocation(dragPoint.x - clickPoint.x, dragPoint.y - clickPoint.y);
            }
        };
        addMouseListener(dragListener);
        addMouseMotionListener(dragListener);
    }

    private void login() {
        String user = userField.getText();
        String password = new String(passwordField.getPassword());
        
        if (userDAO.validateUser(user, password)) {
            loggedInUser = userDAO.findByEmailOrEmployeeNumber(user);
            if (loggedInUser != null) {
                userDAO.updateLastLoginDate(loggedInUser.getUserId());
                succeeded = true;
                dispose();
            }
        } else {
            JOptionPane.showMessageDialog(this,
                    "Invalid user or password",
                    "Login",
                    JOptionPane.ERROR_MESSAGE);
            succeeded = false;
        }
    }

    public boolean isSucceeded() {
        return succeeded;
    }

    public User getLoggedInUser() {
        return loggedInUser;
    }
}