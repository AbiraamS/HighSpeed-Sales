package clearorder;

import javax.swing.SwingUtilities;

public class Main {
    public static void main(String[] args) {
        System.out.println("Starting ClearOrder application...");
        SwingUtilities.invokeLater(() -> {
            try {
                System.out.println("Creating LoginDialog...");
                LoginDialog loginDialog = new LoginDialog();
                System.out.println("LoginDialog created, showing...");
                loginDialog.setVisible(true);
                if (loginDialog.isSucceeded()) {
                    System.out.println("Login successful, creating MainFrame...");
                    MainFrame frame = new MainFrame(loginDialog.getLoggedInUser());
                    frame.setVisible(true);
                    System.out.println("MainFrame created and shown.");
                } else {
                    System.out.println("Login failed or cancelled, exiting.");
                    System.exit(0);
                }
            } catch (Exception e) {
                System.err.println("Error starting application: " + e.getMessage());
                e.printStackTrace();
                System.exit(1);
            }
        });
    }
}