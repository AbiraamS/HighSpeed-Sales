package clearorder.util;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import clearorder.model.Product;
import clearorder.dao.ProductDAO;

/**
 * Tool to diagnose database issues and fix common problems
 */
public class DatabaseDiagnostic {

    public static void main(String[] args) {
        System.out.println("=== Database Diagnostic Tool ===");
        
        // Step 1: Test database connection
        testConnection();
        
        // Step 2: List all tables
        listTables();
        
        // Step 3: Check products table
        checkProductsTable();
        
        // Step 4: Check configurations table
        checkConfigurationsTable();
        
        // Step 5: Fix missing product-configuration links
        fixProductConfigurations();
        
        System.out.println("\n=== Diagnostic complete ===");
    }
    
    private static void testConnection() {
        System.out.println("\n== Testing Database Connection ==");
        try (Connection conn = DatabaseConnection.getConnection()) {
            System.out.println("Connection successful!");
            System.out.println("URL: " + conn.getMetaData().getURL());
            System.out.println("Database: " + conn.getCatalog());
        } catch (Exception e) {
            System.err.println("Connection failed:");
            e.printStackTrace();
        }
    }
    
    private static void listTables() {
        System.out.println("\n== Listing all tables ==");
        try (Connection conn = DatabaseConnection.getConnection()) {
            DatabaseMetaData metaData = conn.getMetaData();
            ResultSet tables = metaData.getTables(null, null, "%", new String[] {"TABLE"});
            
            System.out.println("Tables found:");
            while (tables.next()) {
                String tableName = tables.getString("TABLE_NAME");
                System.out.println("- " + tableName);
                // List columns for each table
                ResultSet columns = metaData.getColumns(null, null, tableName, "%");
                while (columns.next()) {
                    String columnName = columns.getString("COLUMN_NAME");
                    String dataType = columns.getString("TYPE_NAME");
                    System.out.println("  • " + columnName + " (" + dataType + ")");
                }
                columns.close();
                
                // Show row count
                try (Statement stmt = conn.createStatement()) {
                    ResultSet countRs = stmt.executeQuery("SELECT COUNT(*) FROM " + tableName);
                    if (countRs.next()) {
                        System.out.println("  Total rows: " + countRs.getInt(1));
                    }
                } catch (Exception e) {
                    System.err.println("  Error getting row count: " + e.getMessage());
                }
            }
        } catch (SQLException e) {
            System.err.println("Error listing tables:");
            e.printStackTrace();
        }
    }
    
    private static void checkProductsTable() {
        System.out.println("\n== Checking products table ==");
        
        // List products using the DAO
        ProductDAO productDAO = new ProductDAO();
        List<Product> products = productDAO.getAllProducts();
        
        System.out.println("Products from ProductDAO: " + products.size());
        for (Product product : products) {
            System.out.println("- " + product.getProductName() + " (" + product.getManufacturerName() + ")");
        }
        
        // If no products found, create some
        if (products.isEmpty()) {
            System.out.println("No products found - creating sample products");
            createSampleProducts();
            
            // List again after creation
            products = productDAO.getAllProducts();
            System.out.println("Products after creation: " + products.size());
            for (Product product : products) {
                System.out.println("- " + product.getProductName() + " (" + product.getManufacturerName() + ")");
            }
        }
    }
    
    private static void checkConfigurationsTable() {
        System.out.println("\n== Checking pc_configurations table ==");
        
        try (Connection conn = DatabaseConnection.getConnection()) {
            // Check if table exists
            DatabaseMetaData metaData = conn.getMetaData();
            ResultSet tables = metaData.getTables(null, null, "pc_configurations", null);
            
            if (!tables.next()) {
                System.out.println("pc_configurations table does not exist - creating it");
                createConfigurationsTable();
            } else {
                System.out.println("pc_configurations table exists");
            }
            
            // List configurations
            try (Statement stmt = conn.createStatement()) {
                ResultSet rs = stmt.executeQuery("SELECT * FROM pc_configurations");
                
                System.out.println("Configurations found:");
                int count = 0;
                while (rs.next()) {
                    count++;
                    int configId = rs.getInt("configuration_id");
                    int orderId = rs.getInt("order_id");
                    int productId = rs.getInt("product_id");
                    double price = rs.getDouble("price");
                    
                    System.out.println(String.format(
                        "- Config #%d: Order=%d, Product=%d, Price=%.2f", 
                        configId, orderId, productId, price));
                }
                
                System.out.println("Total configurations: " + count);
                
                if (count == 0) {
                    System.out.println("No configurations found - this might be why no products appear in invoices");
                }
            }
        } catch (SQLException e) {
            System.err.println("Error checking configurations table:");
            e.printStackTrace();
        }
    }
    
    private static void fixProductConfigurations() {
        System.out.println("\n== Fixing Product-Configuration links ==");
        
        try (Connection conn = DatabaseConnection.getConnection()) {
            // Step 1: Get all orders
            try (Statement ordersStmt = conn.createStatement()) {
                ResultSet orders = ordersStmt.executeQuery("SELECT order_id FROM orders");
                
                while (orders.next()) {
                    int orderId = orders.getInt("order_id");
                    System.out.println("Checking order #" + orderId);
                    
                    // Step 2: Check if this order has configurations
                    try (Statement configStmt = conn.createStatement()) {
                        ResultSet configs = configStmt.executeQuery(
                            "SELECT COUNT(*) FROM pc_configurations WHERE order_id = " + orderId);
                        
                        if (configs.next() && configs.getInt(1) == 0) {
                            System.out.println("  No configurations found for order #" + orderId + " - adding some");
                            
                            // Add configurations for this order using the first 2 products
                            try (Statement prodStmt = conn.createStatement()) {
                                ResultSet products = prodStmt.executeQuery("SELECT product_id FROM products LIMIT 2");
                                
                                int configCount = 0;
                                while (products.next()) {
                                    int productId = products.getInt("product_id");
                                    configCount++;
                                    
                                    // Add configuration
                                    try (Statement insertStmt = conn.createStatement()) {
                                        String sql = String.format(
                                            "INSERT INTO pc_configurations " +
                                            "(configuration_id, order_id, product_id, price, item_count, " +
                                            "estimated_delivery_date, compatibility) VALUES " +
                                            "(%d, %d, %d, 299.99, 1, CURRENT_DATE(), 'Compatible')",
                                            (orderId * 100) + configCount,
                                            orderId,
                                            productId
                                        );
                                        
                                        insertStmt.executeUpdate(sql);
                                        System.out.println("    Added config with product #" + productId);
                                    } catch (Exception e) {
                                        System.err.println("    Error adding configuration: " + e.getMessage());
                                    }
                                }
                                
                                if (configCount == 0) {
                                    System.out.println("    No products found to add to configurations");
                                }
                            }
                        } else {
                            System.out.println("  Order #" + orderId + " already has configurations");
                        }
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fixing product-configuration links:");
            e.printStackTrace();
        }
    }
    
    private static void createSampleProducts() {
        System.out.println("Creating sample products...");
        
        ProductDAO productDAO = new ProductDAO();
        
        // Sample PC components
        Product[] sampleProducts = {
            new Product("CPU", "Ryzen 7 5800X", "AMD", 349.99, "In Stock", 10, "8 cores, 3.8GHz base, 4.7GHz boost"),
            new Product("CPU", "Core i9-12900K", "Intel", 499.99, "In Stock", 5, "16 cores (8P+8E), 3.2GHz base, 5.2GHz boost"),
            new Product("GPU", "Radeon RX 6800 XT", "AMD", 649.99, "In Stock", 3, "16GB GDDR6, 2250MHz, 128 compute units"),
            new Product("GPU", "GeForce RTX 3080", "NVIDIA", 699.99, "In Stock", 2, "10GB GDDR6X, 8704 CUDA cores"),
            new Product("Motherboard", "ROG Strix X570-E", "ASUS", 299.99, "In Stock", 7, "AMD X570, ATX, PCIe 4.0, WiFi 6")
        };
        
        for (Product product : sampleProducts) {
            boolean success = productDAO.addProduct(product);
            System.out.println("Added product " + product.getProductName() + ": " + (success ? "Success" : "Failed"));
        }
    }
    
    private static void createConfigurationsTable() {
        System.out.println("Creating pc_configurations table...");
        
        try (Connection conn = DatabaseConnection.getConnection()) {
            try (Statement stmt = conn.createStatement()) {
                String sql = "CREATE TABLE pc_configurations (" +
                             "configuration_id INT PRIMARY KEY, " +
                             "order_id INT NOT NULL, " +
                             "product_id INT NOT NULL, " +
                             "price DOUBLE NOT NULL, " +
                             "item_count INT NOT NULL DEFAULT 1, " +
                             "estimated_delivery_date DATE NULL, " +
                             "compatibility VARCHAR(50) NOT NULL)";
                
                stmt.executeUpdate(sql);
                System.out.println("Table created successfully");
            }
        } catch (SQLException e) {
            System.err.println("Error creating table:");
            e.printStackTrace();
        }
    }
}
