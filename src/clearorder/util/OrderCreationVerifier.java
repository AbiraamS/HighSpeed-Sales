package clearorder.util;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Utility to verify order creation and product saving functionality
 */
public class OrderCreationVerifier {
    
    public static void main(String[] args) {
        System.out.println("=== Order Creation Verification Tool ===");
        
        if (args.length > 0) {
            try {
                int orderId = Integer.parseInt(args[0]);
                verifyOrder(orderId);
            } catch (NumberFormatException e) {
                System.err.println("Invalid order ID: " + args[0]);
            }
        } else {
            // Verify the most recent orders
            verifyRecentOrders(5);
        }
    }
    
    /**
     * Verify a specific order and its products
     */
    public static void verifyOrder(int orderId) {
        System.out.println("\n--- Verifying Order ID: " + orderId + " ---");
        
        try (Connection conn = DatabaseConnection.getConnection()) {
            if (conn == null) {
                System.err.println("❌ Cannot connect to database");
                return;
            }
            
            // Check if order exists
            String orderSql = "SELECT order_id, order_number, customer_name, status FROM orders WHERE order_id = ?";
            PreparedStatement orderStmt = conn.prepareStatement(orderSql);
            orderStmt.setInt(1, orderId);
            ResultSet orderRs = orderStmt.executeQuery();
            
            if (orderRs.next()) {
                String orderNumber = orderRs.getString("order_number");
                String customerName = orderRs.getString("customer_name");
                String status = orderRs.getString("status");
                
                System.out.println("✅ Order exists:");
                System.out.println("   Order Number: " + orderNumber);
                System.out.println("   Customer: " + customerName);
                System.out.println("   Status: " + status);
                
                // Check products in order_products table
                String productsSql = "SELECT COUNT(*) as product_count FROM order_products WHERE order_id = ?";
                PreparedStatement productsStmt = conn.prepareStatement(productsSql);
                productsStmt.setInt(1, orderId);
                ResultSet productsRs = productsStmt.executeQuery();
                
                if (productsRs.next()) {
                    int productCount = productsRs.getInt("product_count");
                    System.out.println("📦 Products in order_products table: " + productCount);
                    
                    if (productCount == 0) {
                        System.out.println("⚠️  WARNING: No products found in order_products table");
                        System.out.println("   This order will show as empty in invoice previews");
                        
                        // Check if there are products in pc_configurations instead
                        checkConfigurationProducts(conn, orderId);
                    } else {
                        System.out.println("✅ Order has products - should display correctly in invoice");
                        showOrderProducts(conn, orderId);
                    }
                } else {
                    System.err.println("❌ Could not query order_products table");
                }
                
                productsRs.close();
                productsStmt.close();
            } else {
                System.err.println("❌ Order " + orderId + " does not exist");
            }
            
            orderRs.close();
            orderStmt.close();
            
        } catch (SQLException e) {
            System.err.println("❌ Database error: " + e.getMessage());
        }
    }
    
    /**
     * Check if products are stored in pc_configurations table (legacy method)
     */
    private static void checkConfigurationProducts(Connection conn, int orderId) {
        try {
            String configSql = "SELECT COUNT(*) as config_count FROM pc_configurations WHERE order_id = ?";
            PreparedStatement configStmt = conn.prepareStatement(configSql);
            configStmt.setInt(1, orderId);
            ResultSet configRs = configStmt.executeQuery();
            
            if (configRs.next()) {
                int configCount = configRs.getInt("config_count");
                if (configCount > 0) {
                    System.out.println("📋 Found " + configCount + " entries in pc_configurations table");
                    System.out.println("   This appears to be a legacy order format");
                } else {
                    System.out.println("❌ No products found in pc_configurations table either");
                    System.out.println("   This order appears to be completely empty");
                }
            }
            
            configRs.close();
            configStmt.close();
        } catch (SQLException e) {
            System.err.println("Error checking pc_configurations: " + e.getMessage());
        }
    }
    
    /**
     * Show the actual products in an order
     */
    private static void showOrderProducts(Connection conn, int orderId) {
        try {
            String detailSql = "SELECT op.product_id, p.product_name, p.product_type, op.quantity " +
                              "FROM order_products op " +
                              "JOIN products p ON op.product_id = p.product_id " +
                              "WHERE op.order_id = ?";
            PreparedStatement detailStmt = conn.prepareStatement(detailSql);
            detailStmt.setInt(1, orderId);
            ResultSet detailRs = detailStmt.executeQuery();
            
            System.out.println("📋 Order Products:");
            while (detailRs.next()) {
                int productId = detailRs.getInt("product_id");
                String productName = detailRs.getString("product_name");
                String productType = detailRs.getString("product_type");
                int quantity = detailRs.getInt("quantity");
                
                System.out.println("   - " + productName + " (" + productType + ") [ID: " + productId + ", Qty: " + quantity + "]");
            }
            
            detailRs.close();
            detailStmt.close();
        } catch (SQLException e) {
            System.err.println("Error showing order products: " + e.getMessage());
        }
    }
    
    /**
     * Verify the most recent orders
     */
    public static void verifyRecentOrders(int count) {
        System.out.println("\n--- Verifying " + count + " Most Recent Orders ---");
        
        try (Connection conn = DatabaseConnection.getConnection()) {
            if (conn == null) {
                System.err.println("❌ Cannot connect to database");
                return;
            }
            
            String sql = "SELECT order_id FROM orders ORDER BY order_id DESC LIMIT ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, count);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                int orderId = rs.getInt("order_id");
                verifyOrder(orderId);
            }
            
            rs.close();
            stmt.close();
            
        } catch (SQLException e) {
            System.err.println("❌ Database error: " + e.getMessage());
        }
    }
}
