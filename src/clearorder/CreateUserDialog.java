package clearorder;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.regex.*;
import clearorder.dao.UserDAO;
import clearorder.model.User;

/**
 * Dialog for creating new users in the system
 */
public class CreateUserDialog extends JDialog {
    
    private JTextField nameField;
    private JLabel emailLabel, passwordLabel;
    private JComboBox<String> departmentComboBox;
    private JTextArea notesArea;
    private JRadioButton normalUserRadio, adminUserRadio;
    private JButton createButton, cancelButton;
    
    private UserDAO userDAO;
    private boolean userCreated = false;
    
    public CreateUserDialog(JFrame parent) {
        super(parent, "Create New User", true);
        this.userDAO = new UserDAO();
        
        // Set up dialog properties
        setSize(500, 500);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout());
        
        // Create components
        JPanel formPanel = new JPanel();
        formPanel.setLayout(new BoxLayout(formPanel, BoxLayout.Y_AXIS));
        formPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // Full name field
        JPanel namePanel = createFormField("Full Name", nameField = new JTextField(20));
        formPanel.add(namePanel);
        formPanel.add(Box.createVerticalStrut(10));
        
        // User type (Normal or Admin)
        JPanel userTypePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        userTypePanel.add(new JLabel("User Type:"));
        ButtonGroup group = new ButtonGroup();
        normalUserRadio = new JRadioButton("Normal User", true);
        adminUserRadio = new JRadioButton("Admin User", false);
        group.add(normalUserRadio);
        group.add(adminUserRadio);
        userTypePanel.add(normalUserRadio);
        userTypePanel.add(adminUserRadio);
        formPanel.add(userTypePanel);
        formPanel.add(Box.createVerticalStrut(10));
        
        // Email field (auto-generated)
        JPanel emailPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        emailPanel.add(new JLabel("Email:"));
        emailLabel = new JLabel("Will be auto-generated");
        emailLabel.setFont(new Font("Arial", Font.ITALIC, 12));
        emailPanel.add(emailLabel);
        formPanel.add(emailPanel);
        formPanel.add(Box.createVerticalStrut(10));
        
        // Password field (auto-generated)
        JPanel passwordPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        passwordPanel.add(new JLabel("Password:"));
        passwordLabel = new JLabel("Will be auto-generated");
        passwordLabel.setFont(new Font("Arial", Font.ITALIC, 12));
        passwordPanel.add(passwordLabel);
        formPanel.add(passwordPanel);
        formPanel.add(Box.createVerticalStrut(10));
        
        // Department dropdown
        String[] departments = {
            "Verkauf", "Montage", "Lagerverwaltung", "Einkauf", "Buchhaltung", "Personalabteilung",
            "Verkauf_Leitung", "Montage_Leitung", "Lagerverwaltung_Leitung", "Einkauf_Leitung", 
            "Buchhaltung_Leitung", "Personalabteilung_Leitung"
        };
        JPanel deptPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        deptPanel.add(new JLabel("Department:"));
        departmentComboBox = new JComboBox<>(departments);
        departmentComboBox.setPreferredSize(new Dimension(300, 30));
        deptPanel.add(departmentComboBox);
        formPanel.add(deptPanel);
        formPanel.add(Box.createVerticalStrut(10));
        
        // Notes field
        JPanel notesPanel = new JPanel(new BorderLayout());
        notesPanel.add(new JLabel("Notes:"), BorderLayout.NORTH);
        notesArea = new JTextArea(5, 20);
        notesArea.setLineWrap(true);
        notesArea.setWrapStyleWord(true);
        JScrollPane notesScroll = new JScrollPane(notesArea);
        notesScroll.setPreferredSize(new Dimension(300, 100));
        notesPanel.add(notesScroll, BorderLayout.CENTER);
        formPanel.add(notesPanel);
        
        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        createButton = new JButton("Create User");
        cancelButton = new JButton("Cancel");
        
        createButton.setBackground(new Color(51, 122, 220));
        createButton.setForeground(Color.WHITE);
        cancelButton.setBackground(Color.LIGHT_GRAY);
        
        buttonPanel.add(createButton);
        buttonPanel.add(cancelButton);
        
        // Add panels to dialog
        add(formPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
        
        // Add action listeners
        normalUserRadio.addActionListener(e -> updateEmailAndPasswordLabels());
        adminUserRadio.addActionListener(e -> updateEmailAndPasswordLabels());
        
        nameField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { updateEmailAndPasswordLabels(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { updateEmailAndPasswordLabels(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { updateEmailAndPasswordLabels(); }
        });
        
        createButton.addActionListener(e -> createUser());
        cancelButton.addActionListener(e -> dispose());
        
        // Update email and password preview initially
        updateEmailAndPasswordLabels();
    }
    
    private JPanel createFormField(String labelText, JComponent field) {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JLabel label = new JLabel(labelText + ":");
        label.setPreferredSize(new Dimension(100, 20));
        panel.add(label);
        panel.add(field);
        return panel;
    }
    
    private void updateEmailAndPasswordLabels() {
        if (nameField.getText().trim().isEmpty()) {
            emailLabel.setText("Will be auto-generated");
            passwordLabel.setText("Will be auto-generated");
            return;
        }
        
        try {
            // Get the next user ID
            int nextUserId = userDAO.getNextUserId();
            
            // Update email preview
            String emailPrefix = adminUserRadio.isSelected() ? "a" : "m";
            String email = emailPrefix + nextUserId + "@highspeed.de";
            emailLabel.setText(email);
            
            // Update password preview
            String passwordPrefix = adminUserRadio.isSelected() ? "admin" : "mitarbeiter";
            String password = passwordPrefix + nextUserId;
            passwordLabel.setText(password + " (Employee ID: " + nextUserId + ")");
        } catch (Exception ex) {
            emailLabel.setText("Error: " + ex.getMessage());
            passwordLabel.setText("Error");
        }
    }
    
    private void createUser() {
        // Validate input
        String fullName = nameField.getText().trim();
        if (fullName.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter the employee's full name", 
                    "Validation Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        String department = (String) departmentComboBox.getSelectedItem();
        String notes = notesArea.getText().trim();
        boolean isAdmin = adminUserRadio.isSelected();
        
        try {
            // Create the user
            boolean success = userDAO.createUser(
                fullName, 
                isAdmin, 
                department, 
                notes
            );
            
            if (success) {                userCreated = true;
                
                // Extract employee number from the password label text
                String passwordText = passwordLabel.getText();
                String employeeNumber = "N/A";
                Matcher matcher = Pattern.compile("Employee ID: (\\d+)").matcher(passwordText);
                if (matcher.find()) {
                    employeeNumber = matcher.group(1);
                }
                
                JOptionPane.showMessageDialog(this, 
                    "User created successfully!\nEmail: " + emailLabel.getText() + 
                    "\nPassword: " + passwordText.split(" \\(")[0] +
                    "\nEmployee ID: " + employeeNumber,
                    "Success", JOptionPane.INFORMATION_MESSAGE);
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, 
                    "Failed to create user. Please try again.",
                    "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, 
                "Error: " + ex.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    public boolean isUserCreated() {
        return userCreated;
    }
    
    public static boolean showDialog(JFrame parent) {
        CreateUserDialog dialog = new CreateUserDialog(parent);
        dialog.setVisible(true);
        return dialog.isUserCreated();
    }
}
