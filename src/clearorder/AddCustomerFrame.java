package clearorder;

import java.awt.Component;

public class AddCustomerFrame {
    public static void showDialog(Component parent) {
        // Use the consistent style from MainFrame for adding customers
        MainFrame.showAddCustomerDialog(parent);
    }
}