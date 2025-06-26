package clearorder;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import clearorder.dao.UserDAO;
import clearorder.model.User;

/**
 * Dialog for editing existing users in the system
 */
public class EditUserDialog extends JDialog {
    
    private JTextField nameField;
    private JComboBox<String> departmentComboBox;
    private JTextArea notesArea;
    private JButton saveButton, cancelButton;
    
    private UserDAO userDAO;
    private User user;
    private boolean userUpdated = false;
    
    public EditUserDialog(JFrame parent, User user) {
        super(parent, "Benutzer bearbeiten", true);
        this.userDAO = new UserDAO();
        this.user = user;
        
        // Set up dialog properties
        setSize(500, 450);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout());
        
        // Create components
        JPanel formPanel = new JPanel();
        formPanel.setLayout(new BoxLayout(formPanel, BoxLayout.Y_AXIS));
        formPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // User ID and Employee Number (read-only)
        JPanel idPanel = createFormField("Benutzer ID", new JLabel(String.valueOf(user.getUserId())));
        formPanel.add(idPanel);
        formPanel.add(Box.createVerticalStrut(10));
        
        JPanel employeeNumberPanel = createFormField("Mitarbeiternummer", new JLabel(String.valueOf(user.getEmployeeNumber())));
        formPanel.add(employeeNumberPanel);
        formPanel.add(Box.createVerticalStrut(10));
        
        // Email (read-only)
        JPanel emailPanel = createFormField("Email", new JLabel(user.getEmployeeEmail()));
        formPanel.add(emailPanel);
        formPanel.add(Box.createVerticalStrut(10));
        
        // Full name field (editable)
        nameField = new JTextField(user.getEmployeeName(), 20);
        JPanel namePanel = createFormField("Name", nameField);
        formPanel.add(namePanel);
        formPanel.add(Box.createVerticalStrut(10));
        
        // Department dropdown (editable)
        String[] departments = {
            "Verkauf", "Montage", "Lagerverwaltung", "Einkauf", "Buchhaltung", "Personalabteilung",
            "Verkauf_Leitung", "Montage_Leitung", "Lagerverwaltung_Leitung", "Einkauf_Leitung", 
            "Buchhaltung_Leitung", "Personalabteilung_Leitung"
        };
        JPanel deptPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        deptPanel.add(new JLabel("Abteilung:"));
        departmentComboBox = new JComboBox<>(departments);
        departmentComboBox.setSelectedItem(user.getDepartment());
        departmentComboBox.setPreferredSize(new Dimension(300, 30));
        deptPanel.add(departmentComboBox);
        formPanel.add(deptPanel);
        formPanel.add(Box.createVerticalStrut(10));
        
        // Last login date (read-only)
        JPanel loginDatePanel = createFormField("Letzter Login", new JLabel(user.getLastLoginDate() != null ? user.getLastLoginDate() : "Nie"));
        formPanel.add(loginDatePanel);
        formPanel.add(Box.createVerticalStrut(10));
        
        // Notes field (editable)
        JPanel notesPanel = new JPanel(new BorderLayout());
        notesPanel.add(new JLabel("Notizen:"), BorderLayout.NORTH);
        notesArea = new JTextArea(5, 20);
        notesArea.setText(user.getNotes());
        notesArea.setLineWrap(true);
        notesArea.setWrapStyleWord(true);
        JScrollPane notesScroll = new JScrollPane(notesArea);
        notesScroll.setPreferredSize(new Dimension(300, 100));
        notesPanel.add(notesScroll, BorderLayout.CENTER);
        formPanel.add(notesPanel);
        
        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        saveButton = new JButton("Speichern");
        cancelButton = new JButton("Abbrechen");
        
        saveButton.setBackground(new Color(51, 122, 220));
        saveButton.setForeground(Color.WHITE);
        cancelButton.setBackground(Color.LIGHT_GRAY);
        
        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);
        
        // Add panels to dialog
        add(formPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
        
        // Add action listeners
        saveButton.addActionListener(e -> updateUser());
        cancelButton.addActionListener(e -> dispose());
    }
    
    private JPanel createFormField(String labelText, JComponent field) {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JLabel label = new JLabel(labelText + ":");
        label.setPreferredSize(new Dimension(150, 20));
        panel.add(label);
        panel.add(field);
        return panel;
    }
    
    private void updateUser() {
        // Validate input
        String fullName = nameField.getText().trim();
        if (fullName.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Bitte geben Sie den Namen des Mitarbeiters ein", 
                    "Validierungsfehler", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        String department = (String) departmentComboBox.getSelectedItem();
        String notes = notesArea.getText().trim();
        
        try {
            // Update the user object
            user.setEmployeeName(fullName);
            user.setDepartment(department);
            user.setNotes(notes);
            
            // Update the user in the database
            boolean success = userDAO.updateUser(user);
            
            if (success) {
                userUpdated = true;
                JOptionPane.showMessageDialog(this, 
                    "Benutzer erfolgreich aktualisiert!",
                    "Erfolg", JOptionPane.INFORMATION_MESSAGE);
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, 
                    "Fehler beim Aktualisieren des Benutzers. Bitte versuchen Sie es erneut.",
                    "Fehler", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, 
                "Fehler: " + ex.getMessage(),
                "Fehler", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    public boolean isUserUpdated() {
        return userUpdated;
    }
    
    public static boolean showDialog(JFrame parent, User user) {
        EditUserDialog dialog = new EditUserDialog(parent, user);
        dialog.setVisible(true);
        return dialog.isUserUpdated();
    }
}
