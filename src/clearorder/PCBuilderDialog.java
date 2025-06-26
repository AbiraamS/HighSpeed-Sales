package clearorder;

import javax.swing.*;
import java.awt.*;

/**
 * A dialog that displays the PC Builder interface in a popup window
 * Used when clicking "Create Order" buttons on Dashboard or Order List
 */
public class PCBuilderDialog extends JDialog {
    
    private MainFrame.PCBuilderPanel pcBuilderPanel;
    private Object orderCreationListener;

    public PCBuilderDialog(JFrame parent, Object listener) {
        super(parent, "Order Creation", true);
        this.orderCreationListener = listener;
        
        // Get screen dimensions to make the dialog 80% of screen size
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int width = (int)(screenSize.width * 0.8);
        int height = (int)(screenSize.height * 0.8);
        
        setSize(width, height);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout());

        try {
            // Create PC Builder panel
            pcBuilderPanel = new MainFrame.PCBuilderPanel();
            
            // Change the title to "Order Creation"
            Component[] components = pcBuilderPanel.getComponents();
            for (Component comp : components) {
                if (comp instanceof JPanel) {
                    JPanel panel = (JPanel) comp;
                    Component[] panelComponents = panel.getComponents();
                    for (Component c : panelComponents) {
                        if (c instanceof JLabel) {
                            JLabel label = (JLabel) c;
                            if ("PC Builder".equals(label.getText())) {
                                label.setText("Order Creation");
                                break;
                            }
                        }
                    }
                }
            }
            
            // Set up the order creation listener if possible
            if (listener instanceof MainFrame.OrderCreationListener) {
                MainFrame.OrderCreationListener ocl = (MainFrame.OrderCreationListener) listener;
                pcBuilderPanel.setOrderCreationListener(new MainFrame.OrderCreationListener() {
                    @Override
                    public void addCustomer(String id, String name, String email, String phone) {
                        // Forward to the original listener
                        if (ocl != null) {
                            ocl.addCustomer(id, name, email, phone);
                        }
                    }
                    
                    @Override
                    public void addOrder(String orderId, String orderNumber, String status, String customerName, 
                                    String date, String invoiceId, java.util.List<MainFrame.PCBuilderPanel.ProductInfo> selectedProducts) {
                        // Forward to the original listener
                        if (ocl != null) {
                            ocl.addOrder(orderId, orderNumber, status, customerName, date, invoiceId, selectedProducts);
                        }
                        
                        // Close the dialog after order is placed
                        dispose();
                        
                        // Refresh the order list if parent is MainFrame
                        if (parent instanceof MainFrame) {
                            MainFrame mainFrame = (MainFrame) parent;
                            Component currentPanel = mainFrame.getCurrentPanel();
                            if (currentPanel instanceof MainFrame.OrderPanel) {
                                ((MainFrame.OrderPanel) currentPanel).loadOrderData();
                            }
                        }
                    }
                });
            }
            
            // Add the PC Builder panel to this dialog
            add(pcBuilderPanel, BorderLayout.CENTER);
            
        } catch (Exception e) {
            // Fallback to simple dialog if PC Builder panel fails
            System.err.println("Failed to create PC Builder panel: " + e.getMessage());
            e.printStackTrace();
            
            JLabel label = new JLabel("Order Creation Dialog - PC Builder Panel Failed to Load", JLabel.CENTER);
            label.setFont(new Font("Arial", Font.BOLD, 16));
            add(label, BorderLayout.CENTER);
            
            JButton closeButton = new JButton("Close");
            closeButton.addActionListener(evt -> dispose());
            add(closeButton, BorderLayout.SOUTH);
        }
    }
}
