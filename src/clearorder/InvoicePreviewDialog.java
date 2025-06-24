package clearorder;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;

import clearorder.dao.CustomerDAO;
import clearorder.dao.InvoiceDAO;
import clearorder.dao.OrderDAO;
import clearorder.util.DatabaseConnection;
import clearorder.dao.PCConfigurationDAO;
import clearorder.dao.ProductDAO;
import clearorder.model.Customer;
import clearorder.model.Invoice;
import clearorder.model.Order;
import clearorder.model.PCConfiguration;
import clearorder.model.Product;

public class InvoicePreviewDialog extends JDialog {
    private Order order;
    private Invoice invoice;
    private Customer customer;
    private List<PCConfiguration> configurations;
    private List<Product> products;
    private DefaultTableModel tableModel;
    private JTable productsTable;
    
    public InvoicePreviewDialog(JFrame parent, int orderId) {
        super(parent, "Rechnungsvorschau", true);
        
        // Load required data
        loadData(orderId);
        
        // If data loading failed, close the dialog
        if (order == null || invoice == null || customer == null) {
            dispose();
            return;
        }
        
        // Set up the dialog
        setupUI();
        
        // Set dialog properties
        setSize(800, 600);
        setLocationRelativeTo(parent);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
    }    private void loadData(int orderId) {
        OrderDAO orderDAO = new OrderDAO();
        InvoiceDAO invoiceDAO = new InvoiceDAO();
        CustomerDAO customerDAO = new CustomerDAO();
        PCConfigurationDAO pcConfigDAO = new PCConfigurationDAO();
        ProductDAO productDAO = new ProductDAO();
        
        // Get order
        order = orderDAO.getOrderById(orderId);
        if (order == null) {
            JOptionPane.showMessageDialog(this, "Fehler: Bestellung nicht gefunden.", "Fehler", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        // Ensure the invoices table exists
        ensureInvoicesTableExists();
        
        // Get invoice or create a new one if it doesn't exist
        invoice = invoiceDAO.getInvoiceByOrderId(orderId);
        if (invoice == null) {
            // Create a new invoice for this order
            if (createInvoiceForOrder(order)) {
                // Try to get the newly created invoice
                invoice = invoiceDAO.getInvoiceByOrderId(orderId);
                if (invoice == null) {
                    JOptionPane.showMessageDialog(this, "Fehler: Rechnung konnte nicht erstellt werden.", "Fehler", JOptionPane.ERROR_MESSAGE);
                    return;
                }
            } else {
                JOptionPane.showMessageDialog(this, "Fehler: Rechnung konnte nicht erstellt werden.", "Fehler", JOptionPane.ERROR_MESSAGE);
                return;
            }
        }
        
        // Get customer
        customer = null;
        for (Customer c : customerDAO.getAllCustomers()) {
            if (c.getCustomerName().equals(order.getCustomerName())) {
                customer = c;
                break;
            }
        }
          if (customer == null) {
            // Create dummy customer if not found
            customer = new Customer(1, order.getCustomerName(), "customer@example.com", "123-456-7890", "Sample Address", "");
            System.out.println("Created dummy customer for: " + order.getCustomerName());        }
        
        // Get configurations (products in order) - only for legacy fallback
        configurations = pcConfigDAO.getConfigurationsByOrderId(orderId);
        
        System.out.println("Found " + configurations.size() + " configurations in pc_configurations table");// Get products associated with this order
        products = new ArrayList<>();
          // First attempt: Try to load products from order_products table (this is the new approach)
        System.out.println("=== INVOICE DEBUG: Loading products from order_products table for order ID: " + orderId + " ===");
        List<Product> productsFromOrderTable = loadProductsFromOrderProductsTable(orderId);
        
        System.out.println("=== INVOICE DEBUG: Found " + productsFromOrderTable.size() + " products in order_products table ===");
        
        if (!productsFromOrderTable.isEmpty()) {
            // We found products in the order_products table!
            products.addAll(productsFromOrderTable);
            System.out.println("=== INVOICE DEBUG: Successfully loaded " + products.size() + " products from order_products table ===");
        } else {
            System.out.println("=== INVOICE DEBUG: No products found in order_products table, falling back to configurations ===");
            System.out.println("This is likely due to an older order before order_products tracking was implemented");
            System.out.println("Looking up " + configurations.size() + " configurations for products");
            
            // First try: Get products by their product_id directly
            for (PCConfiguration config : configurations) {
                int productId = config.getProductId();
                System.out.println("Looking up product with ID: " + productId + " from configuration");
                
                Product product = productDAO.getProductById(productId);
                if (product != null) {
                    // Store the quantity information in the product
                    product.setItemCount(config.getItemCount());
                    products.add(product);
                    System.out.println("Added product to order: " + product.getProductName() + " (ID: " + product.getProductId() + ")");
                } else {
                    System.out.println("Product not found for ID: " + productId);
                }
            }
        }
        
        // If no products found, try matching by product type and name
        if (products.isEmpty()) {
            System.out.println("No products found by ID, trying to match by type/name");
            List<Product> allProducts = productDAO.getAllProducts();
            
            // Map configurations to products by type if possible
            for (PCConfiguration config : configurations) {
                // This is a simplified approach - in a real system, you'd need more 
                // sophisticated matching logic based on your data model
                for (Product product : allProducts) {
                    // If we have any match based on ID, use it
                    if (product.getProductIdAsInt() == config.getProductId()) {
                        Product clonedProduct = new Product(
                            product.getProductId(),
                            product.getProductType(),
                            product.getProductName(),
                            product.getManufacturerName(),
                            product.getPrice(),
                            product.getAvailability(),
                            config.getItemCount(),  // Use the actual ordered quantity
                            product.getDetails()
                        );
                        products.add(clonedProduct);
                        System.out.println("Matched product by ID: " + product.getProductName());
                        break;
                    }
                }
            }        }
        
        // Debug: Report final products loaded
        if (products.isEmpty()) {
            System.out.println("WARNING: No products loaded for order " + orderId);
        } else {
            System.out.println("Successfully loaded " + products.size() + " products for order " + orderId);
            for (Product product : products) {
                System.out.println("  - " + product.getProductName() + " (ID: " + product.getProductId() + ", Qty: " + product.getItemCount() + ")");
            }
        }
    }
    
    private void setupUI() {
        setLayout(new BorderLayout());
        
        // North panel (header)
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        
        JLabel titleLabel = new JLabel("Rechnungsvorschau");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        headerPanel.add(titleLabel, BorderLayout.WEST);
        
        add(headerPanel, BorderLayout.NORTH);
        
        // Center panel (invoice details)
        JPanel detailsPanel = new JPanel(new GridBagLayout());
        detailsPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        
        // Invoice details
        addLabelRow(detailsPanel, gbc, "Rechnungsnummer:", String.valueOf(invoice.getInvoiceId()), 0);
        addLabelRow(detailsPanel, gbc, "Bestellnummer:", String.valueOf(order.getOrderId()), 1);
        addLabelRow(detailsPanel, gbc, "Datum:", new SimpleDateFormat("dd.MM.yyyy").format(invoice.getInvoiceDate()), 2);
        addLabelRow(detailsPanel, gbc, "Status:", order.getStatus(), 3);
        
        // Separator
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        JPanel separator = new JPanel();
        separator.setPreferredSize(new Dimension(1, 20));
        detailsPanel.add(separator, gbc);
        
        // Customer details
        gbc.gridy++;
        JLabel customerTitle = new JLabel("Kundeninformationen:");
        customerTitle.setFont(new Font("Arial", Font.BOLD, 14));
        detailsPanel.add(customerTitle, gbc);
        
        gbc.gridwidth = 1;
        addLabelRow(detailsPanel, gbc, "Name:", customer.getCustomerName(), 6);
        addLabelRow(detailsPanel, gbc, "Adresse:", customer.getAddress(), 7);
        addLabelRow(detailsPanel, gbc, "E-Mail:", customer.getEmail(), 8);
        addLabelRow(detailsPanel, gbc, "Telefon:", customer.getPhone(), 9);
        
        // Products table
        gbc.gridx = 0;
        gbc.gridy = 10;
        gbc.gridwidth = 2;
        JLabel productsTitle = new JLabel("Bestellte Artikel:");
        productsTitle.setFont(new Font("Arial", Font.BOLD, 14));
        detailsPanel.add(productsTitle, gbc);        // Create product table
        tableModel = new DefaultTableModel(
            new String[] {"Artikelname", "Hersteller", "Spezifikationen", "Menge", "Preis"}, 0) {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return false; // Make table read-only
                }
            };
        
        // Load products directly from order_products table and get the total        // Load products directly from order_products table and populate table
        // This will use the corrected loadProductsFromOrderProductsTable method
        if (products.isEmpty()) {
            System.out.println("No products loaded, will create empty table");
        } else {
            System.out.println("Creating table from " + products.size() + " loaded products");
        }
        
        double total = 0.0;
        tableModel.setRowCount(0);
        
        for (Product product : products) {
            double componentTotal = product.getPrice() * product.getItemCount();
            total += componentTotal;
            
            tableModel.addRow(new Object[] {
                product.getProductName(),
                product.getManufacturerName(),
                product.getDetails() != null ? product.getDetails() : product.getProductType(),
                product.getItemCount(),
                String.format("%.2f €", product.getPrice())
            });
            
            System.out.println("Added to table: " + product.getProductName() + 
                             " (Qty: " + product.getItemCount() + ", Price: " + product.getPrice() + "€)");
        }
        
        if (products.isEmpty()) {
            tableModel.addRow(new Object[] {
                "Keine Produkte gefunden",
                "",
                "",
                0,
                "0.00 €"
            });
        }
        
        System.out.println("Table populated with " + products.size() + " products, total: " + total + "€");
        
        productsTable = new JTable(tableModel);
        JScrollPane tableScrollPane = new JScrollPane(productsTable);
        tableScrollPane.setPreferredSize(new Dimension(700, 200));
        
        gbc.gridy = 11;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        detailsPanel.add(tableScrollPane, gbc);
        
        // Total
        gbc.gridy = 12;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0.0;
        gbc.weighty = 0.0;
        gbc.anchor = GridBagConstraints.EAST;
        JLabel totalLabel = new JLabel("Gesamtsumme: " + String.format("%.2f €", total));
        totalLabel.setFont(new Font("Arial", Font.BOLD, 14));
        detailsPanel.add(totalLabel, gbc);
        
        add(new JScrollPane(detailsPanel), BorderLayout.CENTER);
        
        // South panel (buttons)
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 20, 20));        JButton cancelButton = new JButton("Abbrechen");
        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });
          JButton generateButton = new JButton("PDF-Rechnung generieren");
        generateButton.setBackground(new Color(0, 128, 0));
        generateButton.setForeground(Color.WHITE);
        generateButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        generateButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                generatePdf();
            }
        });
        
        buttonPanel.add(generateButton);
        buttonPanel.add(cancelButton);
        add(buttonPanel, BorderLayout.SOUTH);
    }
    
    private void addLabelRow(JPanel panel, GridBagConstraints gbc, String labelText, String valueText, int row) {
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.gridwidth = 1;
        JLabel label = new JLabel(labelText);
        label.setFont(new Font("Arial", Font.BOLD, 12));
        panel.add(label, gbc);
        
        gbc.gridx = 1;
        JLabel value = new JLabel(valueText);
        panel.add(value, gbc);    }    private void generatePdf() {
        // Create a file chooser
        JFileChooser fileChooser = new JFileChooser();        fileChooser.setDialogTitle("PDF-Rechnung speichern als");
        
        // Set default file name
        String defaultFileName = order.getOrderId() + "_" + invoice.getInvoiceId() + ".pdf";
        fileChooser.setSelectedFile(new java.io.File(defaultFileName));
          // Only allow PDF files
        FileNameExtensionFilter filter = new FileNameExtensionFilter("PDF Dokumente", "pdf");
        fileChooser.setFileFilter(filter);
        
        // Show the save dialog
        int result = fileChooser.showSaveDialog(this);
        
        if (result == JFileChooser.APPROVE_OPTION) {
            String filePath = fileChooser.getSelectedFile().getAbsolutePath();              // Make sure the file ends with .pdf
            if (!filePath.toLowerCase().endsWith(".pdf")) {
                filePath += ".pdf";
            }
            
            // Show busy cursor
            setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            
            try {                // Always create products from the table data to ensure what you see is what you get
                List<Product> productsForPdf = new ArrayList<>();
                
                System.out.println("=== DEBUG: TABLE HAS " + tableModel.getRowCount() + " ROWS ===");
                
                for (int i = 0; i < tableModel.getRowCount(); i++) {
                    // Skip "no products found" message row
                    String productName = tableModel.getValueAt(i, 0).toString();
                    if (productName.contains("Keine") || productName.contains("Fehler")) {
                        System.out.println("Skipping error/empty row: " + productName);
                        continue;
                    }
                      String name = productName;
                    String manufacturer = tableModel.getValueAt(i, 1).toString();
                    String details = tableModel.getValueAt(i, 2).toString();
                    Object quantityObj = tableModel.getValueAt(i, 3);  // Column 3 = Quantity
                    Object priceObj = tableModel.getValueAt(i, 4);     // Column 4 = Price
                    
                    System.out.println("Processing table row " + i + ": " + name + " | " + manufacturer + " | " + details + " | Qty: " + quantityObj + " | Price: " + priceObj);
                    
                    int quantity = 1;
                    if (quantityObj instanceof Integer) {
                        quantity = (Integer) quantityObj;
                    } else if (quantityObj instanceof String) {
                        try {
                            quantity = Integer.parseInt(quantityObj.toString());
                        } catch (NumberFormatException e) {
                            quantity = 1;
                        }
                    }
                    
                    double price = 0.0;
                    if (priceObj instanceof String) {
                        String priceStr = priceObj.toString().replace(" €", "").replace("€", "").trim();
                        try {
                            price = Double.parseDouble(priceStr.replace(',', '.'));
                        } catch (NumberFormatException e) {
                            System.out.println("Error parsing price: " + priceStr);
                            price = 0.0;
                        }
                    }
                    
                    Product product = new Product(
                        i+1,          // product_id (generated based on row)
                        "Component",  // type
                        name,         // name
                        manufacturer, // manufacturer
                        price,        // price
                        "In Stock",   // availability
                        quantity,     // item count - use actual quantity
                        details       // details
                    );
                    
                    productsForPdf.add(product);
                    System.out.println("=== ADDED TO PDF: " + name + " (Price: " + price + "€, Qty: " + quantity + ") ===");                }                // Generate PDF invoice using the correctly loaded products from the UI
                System.out.println("Generating PDF invoice with " + productsForPdf.size() + " products");
                
                // Ensure the file ends with .pdf
                if (!filePath.toLowerCase().endsWith(".pdf")) {
                    filePath += ".pdf";
                }                // Use the existing InvoicePdfGenerator's simple method
                boolean success = clearorder.util.InvoicePdfGenerator.generateInvoiceForOrder(order.getOrderId(), filePath);
                
                if (success) {
                    JOptionPane.showMessageDialog(this, 
                        "Die Rechnung wurde erfolgreich als PDF gespeichert: \n" + filePath, 
                        "PDF-Rechnung gespeichert", JOptionPane.INFORMATION_MESSAGE);
                    dispose();
                    
                    // Try to open the file
                    try {
                        java.awt.Desktop.getDesktop().open(new java.io.File(filePath));
                    } catch (Exception e) {
                        System.out.println("Konnte die PDF-Datei nicht automatisch öffnen: " + e.getMessage());
                    }
                } else {
                    JOptionPane.showMessageDialog(this, 
                        "Fehler beim Generieren der PDF-Rechnung.", 
                        "Fehler", JOptionPane.ERROR_MESSAGE);
                }
            } catch (Exception e) {
                System.err.println("Error generating invoice: " + e.getMessage());
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, 
                    "Fehler beim Generieren der Datei: " + e.getMessage(), 
                    "Fehler", JOptionPane.ERROR_MESSAGE);
            } finally {
                // Reset cursor
                setCursor(Cursor.getDefaultCursor());
            }
        }
    }
    
    /**
     * Create a new invoice for an order if one doesn't exist
     * 
     * @param order The order to create an invoice for
     * @return true if invoice creation was successful, false otherwise
     */    private boolean createInvoiceForOrder(Order order) {
        InvoiceDAO invoiceDAO = new InvoiceDAO();
        PCConfigurationDAO pcConfigDAO = new PCConfigurationDAO();
        ProductDAO productDAO = new ProductDAO();
        
        try {
            // Calculate total price
            double totalPrice = 0.0;
            List<PCConfiguration> configs = pcConfigDAO.getConfigurationsByOrderId(order.getOrderId());
            for (PCConfiguration config : configs) {
                // Get the actual product to get its price
                Product product = productDAO.getProductById(config.getProductId());
                if (product != null) {
                    totalPrice += product.getPrice() * config.getItemCount();
                }
            }
            
            // Generate invoice ID
            long invoiceId = generateInvoiceNumber();
            
            // Create invoice with current date
            Invoice newInvoice = new Invoice(
                invoiceId, 
                order.getOrderId(), 
                totalPrice, 
                new java.sql.Date(System.currentTimeMillis()), 
                "Neu"
            );
            
            // Save to database
            return invoiceDAO.addInvoice(newInvoice);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Generate a unique invoice number
     * 
     * @return A unique invoice number
     */
    private long generateInvoiceNumber() {
        // Format: 10-digit number
        return 1000000000L + (long) (Math.random() * 9000000000L);
    }
    
    /**
     * Ensure that the invoices table exists in the database
     * 
     * @return true if the table exists or was created, false otherwise
     */
    private boolean ensureInvoicesTableExists() {
        try (Connection conn = DatabaseConnection.getConnection()) {
            // Check if invoices table exists
            boolean tableExists = false;
            try (ResultSet tables = conn.getMetaData().getTables(null, null, "invoices", null)) {
                if (tables.next()) {
                    tableExists = true;
                }
            }
            
            // Create table if it doesn't exist
            if (!tableExists) {
                try (PreparedStatement pstmt = conn.prepareStatement(
                        "CREATE TABLE invoices (" +
                        "invoice_id BIGINT PRIMARY KEY, " +
                        "order_id INT NOT NULL, " +
                        "total_price DOUBLE NOT NULL, " +
                        "invoice_date DATE NOT NULL, " +
                        "status VARCHAR(50) NOT NULL" +
                        ")")) {
                    pstmt.executeUpdate();
                    return true;
                }
            }
            return tableExists;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;        }
    }    /**
     * Loads products for an order from the order_products table
     * @param orderId The order ID to load products for
     * @return List of products in the order
     */private List<Product> loadProductsFromOrderProductsTable(int orderId) {
        List<Product> orderProducts = new ArrayList<>();
        ProductDAO productDAO = new ProductDAO();
        
        System.out.println("\n===== LOADING PRODUCTS FROM ORDER_PRODUCTS TABLE =====");
        System.out.println("Order ID: " + orderId);
        
        try (Connection conn = DatabaseConnection.getConnection()) {
            // First check if order has products
            String checkSql = "SELECT COUNT(*) FROM order_products WHERE order_id = ?";
            PreparedStatement checkStmt = conn.prepareStatement(checkSql);
            checkStmt.setInt(1, orderId);
            ResultSet checkRs = checkStmt.executeQuery();
            
            if (checkRs.next()) {
                int count = checkRs.getInt(1);
                System.out.println("Database shows " + count + " products for order ID " + orderId);
            }
            
            // Get all order_products entries for this order (each row is one product)
            String sql = "SELECT product_id, quantity, component_type FROM order_products WHERE order_id = ? ORDER BY product_id";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, orderId);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                int productId = rs.getInt("product_id");
                int quantity = rs.getInt("quantity");
                String componentType = rs.getString("component_type");
                
                System.out.println("Processing order_products entry - Product ID: " + productId + 
                                 ", Quantity: " + quantity + ", Component Type: " + componentType);
                
                // Load the actual product from the products table
                Product product = productDAO.getProductById(productId);
                if (product != null) {
                    // Set the quantity from the order
                    product.setItemCount(quantity);
                    orderProducts.add(product);
                    System.out.println("Successfully loaded product: " + product.getProductName() + 
                                     " (ID: " + productId + ", Qty: " + quantity + ")");
                } else {
                    System.out.println("WARNING: Product with ID " + productId + " not found in products table");
                }
            }
            
            rs.close();
            stmt.close();
            checkRs.close();
            checkStmt.close();
            
            if (orderProducts.isEmpty()) {
                System.out.println("No products found in order_products table for order ID: " + orderId);
            } else {
                System.out.println("Loaded " + orderProducts.size() + " products from order_products table");
            }
        } catch (SQLException e) {
            System.out.println("Error loading products from order_products table: " + e.getMessage());
            e.printStackTrace();
        }
          System.out.println("===== LOADING COMPLETE =====\n");
        return orderProducts;
    }
}