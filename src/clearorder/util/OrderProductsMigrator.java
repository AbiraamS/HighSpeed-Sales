package clearorder.util;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Utility to migrate order_products from comma-separated product_id format 
 * to individual rows per component
 */
public class OrderProductsMigrator {
    
    public static void main(String[] args) {
        System.out.println("=== ORDER_PRODUCTS TABLE MIGRATION ===");
        System.out.println("This will convert comma-separated product IDs to individual rows");
        
        try {
            migrateOrderProducts();
            System.out.println("✅ Migration completed successfully!");
        } catch (Exception e) {
            System.err.println("❌ Migration failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    public static void migrateOrderProducts() throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection()) {
            
            // Step 1: Create new table structure
            System.out.println("Creating new order_products table structure...");
            createNewOrderProductsTable(conn);
            
            // Step 2: Migrate existing data
            System.out.println("Migrating existing comma-separated data...");
            migrateExistingData(conn);
            
            // Step 3: Replace old table with new one
            System.out.println("Replacing old table with new structure...");
            replaceTable(conn);
            
            System.out.println("Migration completed successfully!");
        }
    }
      private static void createNewOrderProductsTable(Connection conn) throws SQLException {
        String createSql = "CREATE TABLE IF NOT EXISTS order_products_new (" +
                          "id INT AUTO_INCREMENT PRIMARY KEY, " +
                          "order_id INT NOT NULL, " +
                          "product_id INT NOT NULL, " +
                          "quantity INT DEFAULT 1, " +
                          "component_type VARCHAR(50), " +
                          "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                          "INDEX idx_order_id (order_id), " +
                          "INDEX idx_product_id (product_id)" +
                          ")";
        
        try (Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(createSql);
            System.out.println("✓ Created order_products_new table");
        }
    }
    
    private static void migrateExistingData(Connection conn) throws SQLException {
        // Get all existing order_products entries
        String selectSql = "SELECT order_id, product_id, quantity FROM order_products";
        String insertSql = "INSERT INTO order_products_new (order_id, product_id, quantity, component_type) VALUES (?, ?, ?, ?)";
        
        try (Statement selectStmt = conn.createStatement();
             PreparedStatement insertStmt = conn.prepareStatement(insertSql);
             ResultSet rs = selectStmt.executeQuery(selectSql)) {
            
            int totalMigrated = 0;
            
            while (rs.next()) {
                int orderId = rs.getInt("order_id");
                String productIdStr = rs.getString("product_id");
                int totalQuantity = rs.getInt("quantity");
                
                System.out.println("Processing order " + orderId + " with product_id: " + productIdStr);
                
                if (productIdStr != null && productIdStr.contains(",")) {
                    // Split comma-separated product IDs
                    String[] productIds = productIdStr.split(",");
                    
                    for (String productIdString : productIds) {
                        try {
                            int productId = Integer.parseInt(productIdString.trim());
                            
                            // Get component type from products table
                            String componentType = getComponentType(conn, productId);
                            
                            // Insert individual row
                            insertStmt.setInt(1, orderId);
                            insertStmt.setInt(2, productId);
                            insertStmt.setInt(3, 1); // Each component has quantity 1
                            insertStmt.setString(4, componentType);
                            insertStmt.executeUpdate();
                            
                            totalMigrated++;
                            System.out.println("  ✓ Added product " + productId + " (" + componentType + ")");
                            
                        } catch (NumberFormatException e) {
                            System.out.println("  ⚠ Skipped invalid product ID: " + productIdString);
                        }
                    }
                } else {
                    // Handle single product ID (not comma-separated)
                    try {
                        int productId = Integer.parseInt(productIdStr);
                        String componentType = getComponentType(conn, productId);
                        
                        insertStmt.setInt(1, orderId);
                        insertStmt.setInt(2, productId);
                        insertStmt.setInt(3, totalQuantity);
                        insertStmt.setString(4, componentType);
                        insertStmt.executeUpdate();
                        
                        totalMigrated++;
                        System.out.println("  ✓ Added single product " + productId + " (" + componentType + ")");
                    } catch (NumberFormatException e) {
                        System.out.println("  ⚠ Skipped invalid single product ID: " + productIdStr);
                    }
                }
            }
            
            System.out.println("✓ Migrated " + totalMigrated + " product entries");
        }
    }
    
    private static String getComponentType(Connection conn, int productId) {
        String sql = "SELECT product_type FROM products WHERE product_id = ?";
        
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, productId);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return rs.getString("product_type");
            }
        } catch (SQLException e) {
            System.out.println("Warning: Could not get component type for product " + productId);
        }
        
        return "Unknown";
    }
    
    private static void replaceTable(Connection conn) throws SQLException {
        try (Statement stmt = conn.createStatement()) {
            // Backup old table
            stmt.executeUpdate("DROP TABLE IF EXISTS order_products_backup");
            stmt.executeUpdate("RENAME TABLE order_products TO order_products_backup");
            
            // Replace with new table
            stmt.executeUpdate("RENAME TABLE order_products_new TO order_products");
            
            System.out.println("✓ Replaced old table (backup saved as order_products_backup)");
        }
    }
}
