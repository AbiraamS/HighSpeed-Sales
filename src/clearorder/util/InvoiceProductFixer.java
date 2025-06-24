package clearorder.util;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import clearorder.dao.ProductDAO;
import clearorder.model.Product;

/**
 * Database diagnostic and repair tool for product management.
 * This class will:
 * 1. Check if the products table has a product_id column
 * 2. Add product_id if it doesn't exist
 * 3. Ensure sample products exist for testing
 * 4. Provide test data for order creation and invoice preview
 */
public class InvoiceProductFixer {    public static void main(String[] args) {
        System.out.println("Running product database setup...");
        
        // 1. Fix the database schema (add product_id column)
        fixProductsTable();
        
        // 2. Add sample products with proper IDs if none exist
        ensureSampleProductsExist();
        
        System.out.println("Setup completed. Products table is ready for order creation and invoice generation.");
        System.out.println("To test: Create a new order, then preview the invoice.");
    }
    
    private static void fixProductsTable() {
        System.out.println("Checking products table schema...");
        
        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            
            // Check if product_id column exists
            ResultSet columns = conn.getMetaData().getColumns(null, null, "products", "product_id");
            boolean hasProductIdColumn = false;
            if (columns.next()) {
                hasProductIdColumn = true;
                System.out.println("✓ product_id column exists");
            } else {
                System.out.println("✗ product_id column does not exist, will add it");
            }
            columns.close();
            
            if (!hasProductIdColumn) {                // Use a direct approach to add product_id column since DatabaseFixer might not be compiled yet
                addProductIdColumnDirectly(conn);
            }
            
        } catch (SQLException e) {
            System.err.println("Error checking products table: " + e.getMessage());
            e.printStackTrace();
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    
    private static void ensureSampleProductsExist() {
        System.out.println("Ensuring sample products exist...");
        
        ProductDAO productDAO = new ProductDAO();
        
        // Check if products exist
        if (productDAO.getAllProducts().isEmpty()) {
            System.out.println("No products found, adding sample products");
            
            // Add sample PC components
            Product[] sampleProducts = {
                new Product("CPU", "Ryzen 7 5800X", "AMD", 349.99, "In Stock", 10, "8 cores, 3.8GHz base, 4.7GHz boost"),
                new Product("CPU", "Core i9-12900K", "Intel", 499.99, "In Stock", 5, "16 cores (8P+8E), 3.2GHz base, 5.2GHz boost"),
                new Product("GPU", "Radeon RX 6800 XT", "AMD", 649.99, "In Stock", 3, "16GB GDDR6, 2250MHz, 128 compute units"),
                new Product("GPU", "GeForce RTX 3080", "NVIDIA", 699.99, "In Stock", 2, "10GB GDDR6X, 8704 CUDA cores"),
                new Product("Motherboard", "ROG Strix X570-E", "ASUS", 299.99, "In Stock", 7, "AMD X570, ATX, PCIe 4.0, WiFi 6"),
                new Product("Motherboard", "Z690 Aorus Master", "Gigabyte", 399.99, "In Stock", 4, "Intel Z690, ATX, PCIe 5.0, DDR5"),
                new Product("RAM", "Trident Z Neo", "G.Skill", 169.99, "In Stock", 15, "32GB (2x16GB), DDR4-3600, CL16"),
                new Product("RAM", "Dominator Platinum", "Corsair", 229.99, "In Stock", 8, "32GB (2x16GB), DDR5-5200, CL38"),
                new Product("SSD", "970 EVO Plus", "Samsung", 149.99, "In Stock", 20, "1TB, NVMe, PCIe 3.0, 3500MB/s read"),
                new Product("SSD", "980 PRO", "Samsung", 229.99, "In Stock", 12, "2TB, NVMe, PCIe 4.0, 7000MB/s read")
            };
            
            for (Product product : sampleProducts) {
                try {
                    productDAO.addProduct(product);
                    System.out.println("Added product: " + product.getProductName() + " with ID: " + product.getProductId());
                } catch (Exception e) {
                    System.err.println("Error adding product " + product.getProductName() + ": " + e.getMessage());
                }
            }        } else {
            System.out.println("Products already exist in the database");
        }
    }

    /**
     * Add the product_id column to the products table directly
     */
    private static void addProductIdColumnDirectly(Connection conn) throws SQLException {
        System.out.println("Directly adding product_id column to products table...");
        
        // First, try to backup the existing data
        List<Map<String, Object>> products = new ArrayList<>();
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT * FROM products");
        
        while (rs.next()) {
            Map<String, Object> product = new HashMap<>();
            product.put("product_type", rs.getString("product_type"));
            product.put("product_name", rs.getString("product_name"));
            product.put("manufacturer_name", rs.getString("manufacturer_name"));
            product.put("price", rs.getDouble("price"));
            product.put("availability", rs.getString("availability"));
            product.put("item_count", rs.getInt("item_count"));
            product.put("details", rs.getString("details"));
            products.add(product);
        }
        
        System.out.println("✓ Backed up " + products.size() + " products");
        rs.close();
        stmt.close();
        
        // Drop the existing table
        Statement dropStmt = conn.createStatement();
        dropStmt.executeUpdate("DROP TABLE products");
        System.out.println("✓ Dropped existing products table");
        dropStmt.close();
        
        // Create new table with product_id
        Statement createStmt = conn.createStatement();
        createStmt.executeUpdate(
            "CREATE TABLE products (" +
            "product_id INT AUTO_INCREMENT PRIMARY KEY, " +
            "product_type VARCHAR(50) NOT NULL, " +
            "product_name VARCHAR(100) NOT NULL, " +
            "manufacturer_name VARCHAR(100) NOT NULL, " +
            "price DOUBLE NOT NULL, " +
            "availability VARCHAR(50) NOT NULL, " +
            "item_count INT NOT NULL DEFAULT 1, " +
            "details LONGTEXT NOT NULL" +
            ")"
        );
        System.out.println("✓ Created new products table with product_id column");
        createStmt.close();
        
        // Restore the data
        for (Map<String, Object> product : products) {
            PreparedStatement pstmt = conn.prepareStatement(
                "INSERT INTO products (product_type, product_name, manufacturer_name, price, availability, item_count, details) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)");
            
            pstmt.setString(1, (String) product.get("product_type"));
            pstmt.setString(2, (String) product.get("product_name"));
            pstmt.setString(3, (String) product.get("manufacturer_name"));
            pstmt.setDouble(4, (Double) product.get("price"));
            pstmt.setString(5, (String) product.get("availability"));
            pstmt.setInt(6, (Integer) product.get("item_count"));
            pstmt.setString(7, (String) product.get("details"));
            
            pstmt.executeUpdate();
            pstmt.close();
        }
          System.out.println("✓ Restored " + products.size() + " products to the new table");
    }
}
