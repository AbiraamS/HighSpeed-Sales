package clearorder;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;

import clearorder.dao.CustomerDAO;
import clearorder.dao.InvoiceDAO;
import clearorder.dao.OrderDAO;
import clearorder.dao.ProductDAO;
import clearorder.model.Customer;
import clearorder.model.Invoice;
import clearorder.model.Order;
import clearorder.model.Product;
import clearorder.util.DatabaseConnection;

public class CreateOrderFrame extends JFrame {
    private JComboBox<String> customerComboBox;
    private JList<Product> productList;
    private JList<Product> selectedProductList;
    private JButton addButton;
    private JButton removeButton;
    private JButton createOrderButton;
    private JLabel totalPriceLabel;
    private double totalPrice = 0.0;
    private CustomerDAO customerDAO;
    private ProductDAO productDAO;
    private OrderDAO orderDAO;
    private InvoiceDAO invoiceDAO;

    public CreateOrderFrame() {
        customerDAO = new CustomerDAO();
        productDAO = new ProductDAO();
        orderDAO = new OrderDAO();
        invoiceDAO = new InvoiceDAO();

        setTitle("Create New Order");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        // Create main panel with BorderLayout
        JPanel mainPanel = new JPanel(new BorderLayout());
        add(mainPanel);

        // Create top panel for customer selection
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topPanel.add(new JLabel("Select Customer:"));
        customerComboBox = new JComboBox<>();
        updateCustomerComboBox();
        topPanel.add(customerComboBox);
        mainPanel.add(topPanel, BorderLayout.NORTH);

        // Create center panel with product lists
        JPanel centerPanel = new JPanel(new GridLayout(1, 3, 10, 0));
        
        // Available products panel
        JPanel availableProductsPanel = new JPanel(new BorderLayout());
        availableProductsPanel.setBorder(BorderFactory.createTitledBorder("Available Products"));
        productList = new JList<>(new DefaultListModel<>());
        productList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        JScrollPane availableScrollPane = new JScrollPane(productList);
        availableProductsPanel.add(availableScrollPane, BorderLayout.CENTER);
        centerPanel.add(availableProductsPanel);

        // Buttons panel
        JPanel buttonsPanel = new JPanel(new GridLayout(2, 1, 0, 10));
        addButton = new JButton(">>");
        removeButton = new JButton("<<");
        buttonsPanel.add(addButton);
        buttonsPanel.add(removeButton);
        centerPanel.add(buttonsPanel);

        // Selected products panel
        JPanel selectedProductsPanel = new JPanel(new BorderLayout());
        selectedProductsPanel.setBorder(BorderFactory.createTitledBorder("Selected Products"));
        selectedProductList = new JList<>(new DefaultListModel<>());
        selectedProductList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        JScrollPane selectedScrollPane = new JScrollPane(selectedProductList);
        selectedProductsPanel.add(selectedScrollPane, BorderLayout.CENTER);
        centerPanel.add(selectedProductsPanel);

        mainPanel.add(centerPanel, BorderLayout.CENTER);

        // Create bottom panel for total price and create order button
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        totalPriceLabel = new JLabel("Total Price: $0.00");
        createOrderButton = new JButton("Create Order");
        bottomPanel.add(totalPriceLabel);
        bottomPanel.add(createOrderButton);
        mainPanel.add(bottomPanel, BorderLayout.SOUTH);

        // Add action listeners
        addButton.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                addSelectedProducts();
            }
        });
        removeButton.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                removeSelectedProducts();
            }
        });
        createOrderButton.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                createOrder();
            }
        });

        // Load products
        loadProducts();
    }

    private void updateCustomerComboBox() {
        customerComboBox.removeAllItems();
        List<Customer> customers = customerDAO.getAllCustomers();
        for (Customer customer : customers) {
            customerComboBox.addItem(customer.getCustomerName());
        }
    }

    private void loadProducts() {
        DefaultListModel<Product> model = (DefaultListModel<Product>) productList.getModel();
        model.clear();
        List<Product> products = productDAO.getAllProducts();
        for (Product product : products) {
            if ("Im Lager".equals(product.getAvailability())) {
                model.addElement(product);
            }
        }
    }

    private void addSelectedProducts() {
        DefaultListModel<Product> selectedModel = (DefaultListModel<Product>) selectedProductList.getModel();
        DefaultListModel<Product> availableModel = (DefaultListModel<Product>) productList.getModel();
        
        for (Product product : productList.getSelectedValuesList()) {
            selectedModel.addElement(product);
            availableModel.removeElement(product);
            totalPrice += product.getPrice();
        }
        
        updateTotalPriceLabel();
        
        // Check compatibility after adding products
        checkBasicCompatibility();
    }

    private void removeSelectedProducts() {
        DefaultListModel<Product> selectedModel = (DefaultListModel<Product>) selectedProductList.getModel();
        DefaultListModel<Product> availableModel = (DefaultListModel<Product>) productList.getModel();
        
        for (Product product : selectedProductList.getSelectedValuesList()) {
            selectedModel.removeElement(product);
            availableModel.addElement(product);
            totalPrice -= product.getPrice();
        }
        
        updateTotalPriceLabel();
    }

    private void updateTotalPriceLabel() {
        totalPriceLabel.setText(String.format("Total Price: $%.2f", totalPrice));
    }

    /**
     * Check basic compatibility between selected components
     * Currently checks CPU and Motherboard socket compatibility
     */
    private void checkBasicCompatibility() {
        DefaultListModel<Product> model = (DefaultListModel<Product>) selectedProductList.getModel();
        String cpuSocket = null;
        String motherboardSocket = null;
        String cpuName = "";
        String motherboardName = "";
        
        // Find CPU and Motherboard sockets
        for (int i = 0; i < model.getSize(); i++) {
            Product product = model.getElementAt(i);
            String productType = product.getProductType();
            String socketType = product.getSocketType();
            
            if ("CPU".equalsIgnoreCase(productType) && socketType != null && !socketType.trim().isEmpty()) {
                cpuSocket = socketType;
                cpuName = product.getProductName();
                System.out.println("Found CPU: " + cpuName + " with socket: " + cpuSocket);
            }
            if ("Motherboard".equalsIgnoreCase(productType) && socketType != null && !socketType.trim().isEmpty()) {
                motherboardSocket = socketType;
                motherboardName = product.getProductName();
                System.out.println("Found Motherboard: " + motherboardName + " with socket: " + motherboardSocket);
            }
        }
        
        // Simple compatibility check
        if (cpuSocket != null && motherboardSocket != null && !cpuSocket.equals(motherboardSocket)) {
            String message = "⚠️ Compatibility Warning ⚠️\n\n" +
                           "CPU Socket: " + cpuSocket + " (" + cpuName + ")\n" +
                           "Motherboard Socket: " + motherboardSocket + " (" + motherboardName + ")\n\n" +
                           "These components are not compatible!\n" +
                           "Would you like to continue anyway?";
            
            int choice = JOptionPane.showConfirmDialog(this, 
                message,
                "Socket Compatibility Warning", 
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);
                
            if (choice == JOptionPane.NO_OPTION) {
                System.out.println("User chose to cancel due to compatibility issue");
            } else {
                System.out.println("User chose to continue despite compatibility warning");
            }
        } else if (cpuSocket != null && motherboardSocket != null && cpuSocket.equals(motherboardSocket)) {
            System.out.println("✓ CPU and Motherboard sockets are compatible: " + cpuSocket);
        }
    }

    private void createOrder() {
        System.out.println("=== DEBUG: Starting order creation ===");
        
        if (customerComboBox.getSelectedIndex() == -1) {
            JOptionPane.showMessageDialog(this, "Please select a customer.");
            return;
        }

        if (selectedProductList.getModel().getSize() == 0) {
            JOptionPane.showMessageDialog(this, "Please select at least one product.");
            return;
        }

        try {
            // Get selected customer
            String selectedCustomer = (String) customerComboBox.getSelectedItem();
            List<Customer> customers = customerDAO.getAllCustomers();
            Customer customer = null;
            for (Customer c : customers) {
                if (c.getCustomerName().equals(selectedCustomer)) {
                    customer = c;
                    break;
                }
            }

            if (customer == null) {
                JOptionPane.showMessageDialog(this, "Error: Customer not found.");
                return;
            }

            // Create order
            Order order = new Order();
            order.setOrderId(generateOrderId());
            order.setCustomerName(customer.getCustomerName());
            order.setOrderDate(new Date(System.currentTimeMillis()));
            order.setStatus("Ausstehend");
            orderDAO.addOrder(order);

            // Create invoice
            Invoice invoice = new Invoice();
            invoice.setInvoiceId(generateInvoiceId());
            invoice.setOrderId(order.getOrderId());
            invoice.setTotalPrice(totalPrice);
            invoice.setInvoiceDate(new Date(System.currentTimeMillis()));
            invoice.setStatus("Ausstehend");
            invoiceDAO.addInvoice(invoice);

            // Save selected products to order_products table
            saveProductsToOrderProductsTable(order.getOrderId());
            
            // Debug: Check what was saved
            // OrderProductsDebugger.printOrderProducts(order.getOrderId());

            JOptionPane.showMessageDialog(this, "Order created successfully!");
            dispose();

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error creating order: " + e.getMessage());
        }
    }

    private int generateOrderId() {
        return (int) (System.currentTimeMillis() % Integer.MAX_VALUE);
    }

    private int generateInvoiceId() {
        return (int) (System.currentTimeMillis() % Integer.MAX_VALUE);
    }

    /**
     * Find the product ID from the database based on name and manufacturer
     * @param product The product to find the ID for
     * @return The product ID, or 0 if not found
     */
    private int findProductId(Product product) {
        // If the product already has a valid product ID, use it
        String productIdStr = product.getProductId();
        if (productIdStr != null && !productIdStr.equals("0") && !productIdStr.isEmpty()) {
            try {
                int productIdInt = Integer.parseInt(productIdStr);
                if (productIdInt > 0) {
                    System.out.println("Using existing product ID: " + productIdInt + " for " + product.getProductName());
                    return productIdInt;
                }
            } catch (NumberFormatException e) {
                System.out.println("Product ID is not numeric: " + productIdStr);
            }
        }
        
        // Query the database directly for this product
        try (Connection conn = DatabaseConnection.getConnection()) {
            // First try exact match by name and manufacturer
            String sql = "SELECT product_id FROM products WHERE product_name = ? AND manufacturer_name = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, product.getProductName());
            stmt.setString(2, product.getManufacturerName());
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                int id = rs.getInt("product_id");
                System.out.println("Found exact match in DB for " + product.getProductName() + " with ID: " + id);
                rs.close();
                stmt.close();
                return id;
            }
            rs.close();
            stmt.close();
            
            // Try by name only
            sql = "SELECT product_id FROM products WHERE product_name = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, product.getProductName());
            rs = stmt.executeQuery();
            
            if (rs.next()) {
                int id = rs.getInt("product_id");
                System.out.println("Found product by name only in DB: " + product.getProductName() + " with ID: " + id);
                rs.close();
                stmt.close();
                return id;
            }
            rs.close();
            stmt.close();
            
            // Try by product type
            sql = "SELECT product_id FROM products WHERE product_type = ? LIMIT 1";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, product.getProductType());
            rs = stmt.executeQuery();
            
            if (rs.next()) {
                int id = rs.getInt("product_id");
                System.out.println("Found product by type in DB: " + product.getProductType() + " with ID: " + id);
                rs.close();
                stmt.close();
                return id;
            }
            rs.close();
            stmt.close();
            
            // If all direct DB lookups fail, try the in-memory approach
            List<Product> allProducts = productDAO.getAllProducts();
            System.out.println("Searching for product ID among " + allProducts.size() + " products for: " + product.getProductName());
            
            // First try to find an exact match by name and manufacturer
            for (Product dbProduct : allProducts) {
                if (dbProduct.getProductName().equals(product.getProductName()) &&
                    dbProduct.getManufacturerName().equals(product.getManufacturerName())) {
                    
                    // Return the actual product_id from the database
                    String dbProductIdStr = dbProduct.getProductId();
                    try {
                        int dbProductId = Integer.parseInt(dbProductIdStr);
                        System.out.println("Found exact match for " + product.getProductName() + " with ID: " + dbProductId);
                        return dbProductId;
                    } catch (NumberFormatException e) {
                        System.out.println("Invalid product ID format: " + dbProductIdStr);
                    }
                }
            }
            
            // If no exact match by name and manufacturer, try just by name
            for (Product dbProduct : allProducts) {
                if (dbProduct.getProductName().equalsIgnoreCase(product.getProductName())) {
                    String dbProductIdStr = dbProduct.getProductId();
                    try {
                        int dbProductId = Integer.parseInt(dbProductIdStr);
                        System.out.println("Found product by name only: " + product.getProductName() + " with ID: " + dbProductId);
                        return dbProductId;
                    } catch (NumberFormatException e) {
                        System.out.println("Invalid product ID format: " + dbProductIdStr);
                    }
                }
            }
            
            // If still not found, try by product type
            for (Product dbProduct : allProducts) {
                if (dbProduct.getProductType().equalsIgnoreCase(product.getProductType())) {
                    String dbProductIdStr = dbProduct.getProductId();
                    try {
                        int dbProductId = Integer.parseInt(dbProductIdStr);
                        System.out.println("Found product by type: " + product.getProductType() + " with ID: " + dbProductId);
                        return dbProductId;
                    } catch (NumberFormatException e) {
                        System.out.println("Invalid product ID format: " + dbProductIdStr);
                    }
                }
            }
        } catch (SQLException e) {
            System.out.println("Error finding product ID in database: " + e.getMessage());
        }
        
        // If still not found, try to get any product from the database
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT product_id FROM products ORDER BY product_id LIMIT 1")) {
            
            if (rs.next()) {
                int id = rs.getInt("product_id");
                System.out.println("WARNING: Could not find product ID for: " + product.getProductName() + 
                                 ". Using first available product: " + id);
                return id;
            }
        } catch (SQLException e) {
            System.out.println("Error getting fallback product ID: " + e.getMessage());
        }
        
        // If all else fails, use a safe default but log the issue
        System.out.println("ERROR: No products found in database. Using default ID 1 for: " + product.getProductName());
        return 1;  // Default to product #1
    }

    /**
     * Saves selected products to the order_products table using the new normalized structure
     * This creates a permanent record of which products were included in the order
     * Each product gets its own row with individual quantity and component type
     * @param orderId The order ID to link the products to
     */
    private void saveProductsToOrderProductsTable(int orderId) {
        System.out.println("\n===== SAVING PRODUCTS TO ORDER_PRODUCTS TABLE (NEW NORMALIZED STRUCTURE) =====");
        System.out.println("Order ID: " + orderId);
        
        try {
            Connection conn = DatabaseConnection.getConnection();
            
            // First check if this order already has products
            String checkSql = "SELECT COUNT(*) FROM order_products WHERE order_id = ?";
            PreparedStatement checkStmt = conn.prepareStatement(checkSql);
            checkStmt.setInt(1, orderId);
            ResultSet checkRs = checkStmt.executeQuery();
            
            if (checkRs.next() && checkRs.getInt(1) > 0) {
                System.out.println("Order already has products. Deleting existing entries...");
                String deleteSql = "DELETE FROM order_products WHERE order_id = ?";
                PreparedStatement deleteStmt = conn.prepareStatement(deleteSql);
                deleteStmt.setInt(1, orderId);
                int deleted = deleteStmt.executeUpdate();
                System.out.println("Deleted " + deleted + " existing product entries");
                deleteStmt.close();
            }
            checkRs.close();
            checkStmt.close();
            
            DefaultListModel<Product> selectedModel = (DefaultListModel<Product>) selectedProductList.getModel();
            System.out.println("Selected products count: " + selectedModel.getSize());
            
            if (selectedModel.getSize() == 0) {
                System.out.println("No products selected, nothing to save.");
                conn.close();
                return;
            }
            
            // NEW APPROACH: Insert one row per product (normalized structure)
            String sql = "INSERT INTO order_products (order_id, product_id, quantity, component_type) VALUES (?, ?, ?, ?)";
            PreparedStatement stmt = conn.prepareStatement(sql);
            
            int savedCount = 0;
            for (int i = 0; i < selectedModel.getSize(); i++) {
                Product product = selectedModel.getElementAt(i);
                
                // Find the actual product ID
                int productId = findProductId(product);
                
                if (productId > 0) {
                    int quantity = (product.getItemCount() > 0 ? product.getItemCount() : 1);
                    String componentType = product.getProductType(); // Use product type as component type
                    
                    stmt.setInt(1, orderId);
                    stmt.setInt(2, productId);  // Now inserting individual product ID as int
                    stmt.setInt(3, quantity);
                    stmt.setString(4, componentType);
                    
                    int result = stmt.executeUpdate();
                    if (result > 0) {
                        savedCount++;
                        System.out.println("SUCCESS: Saved product - " + product.getProductName() + 
                                         " (ID: " + productId + ", Qty: " + quantity + ", Type: " + componentType + ")");
                    } else {
                        System.out.println("ERROR: Failed to save product - " + product.getProductName());
                    }
                } else {
                    System.out.println("WARNING: Could not find product ID for: " + product.getProductName() + " - skipping");
                }
            }
            
            stmt.close();
            
            System.out.println("Summary: Saved " + savedCount + " out of " + selectedModel.getSize() + " products");
            
            // Verify the save was successful
            String verifySql = "SELECT product_id, quantity, component_type FROM order_products WHERE order_id = ? ORDER BY product_id";
            PreparedStatement verifyStmt = conn.prepareStatement(verifySql);
            verifyStmt.setInt(1, orderId);
            ResultSet verifyRs = verifyStmt.executeQuery();
            
            System.out.println("VERIFICATION: Products saved for order " + orderId + ":");
            int verifyCount = 0;
            while (verifyRs.next()) {
                int savedProductId = verifyRs.getInt("product_id");
                int savedQuantity = verifyRs.getInt("quantity");
                String savedComponentType = verifyRs.getString("component_type");
                System.out.println("  Product ID: " + savedProductId + ", Quantity: " + savedQuantity + 
                                 ", Component Type: " + savedComponentType);
                verifyCount++;
            }
            
            if (verifyCount == 0) {
                System.out.println("VERIFICATION FAILED: No data found in order_products for order " + orderId);
            } else {
                System.out.println("VERIFICATION SUCCESS: " + verifyCount + " products found in database");
            }
            
            verifyRs.close();
            verifyStmt.close();
            conn.close();
            
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Error saving products to order_products table: " + e.getMessage());
        }
        
        System.out.println("===== SAVE COMPLETE =====\n");
    }
}