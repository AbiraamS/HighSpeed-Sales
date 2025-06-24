package clearorder.util;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Enhanced debugging version of the order products saving logic
 * Use this to identify why 10-item orders are not being saved
 */
public class OrderProductsDebugSaver {
    
    /**
     * Debug version of saveProductsToOrderProductsTable with extensive logging
     */
    public static void debugSaveProducts(int orderId, java.util.List<clearorder.model.Product> selectedProducts) {
        System.out.println("\n===== DEBUG SAVE PRODUCTS TO ORDER_PRODUCTS TABLE =====");
        System.out.println("Order ID: " + orderId);
        System.out.println("Number of selected products: " + selectedProducts.size());
        
        try (Connection conn = clearorder.util.DatabaseConnection.getConnection()) {
            
            // Step 1: Check if order already exists
            System.out.println("\n--- Step 1: Checking existing products ---");
            String checkSql = "SELECT COUNT(*) FROM order_products WHERE order_id = ?";
            PreparedStatement checkStmt = conn.prepareStatement(checkSql);
            checkStmt.setInt(1, orderId);
            ResultSet checkRs = checkStmt.executeQuery();
            
            if (checkRs.next() && checkRs.getInt(1) > 0) {
                System.out.println("⚠ Order already has products. Deleting existing entries...");
                String deleteSql = "DELETE FROM order_products WHERE order_id = ?";
                PreparedStatement deleteStmt = conn.prepareStatement(deleteSql);
                deleteStmt.setInt(1, orderId);
                int deleted = deleteStmt.executeUpdate();
                System.out.println("✓ Deleted " + deleted + " existing product entries");
                deleteStmt.close();
            } else {
                System.out.println("✓ No existing products for this order");
            }
            checkRs.close();
            checkStmt.close();
            
            // Step 2: Process each product
            System.out.println("\n--- Step 2: Processing selected products ---");
            java.util.List<String> productIds = new java.util.ArrayList<>();
            int totalQuantity = 0;
            
            for (int i = 0; i < selectedProducts.size(); i++) {
                clearorder.model.Product product = selectedProducts.get(i);
                System.out.println("Processing product " + (i+1) + ": " + product.getProductName());
                
                String productIdStr = product.getProductId();
                int productId = 0;
                
                // Try to parse existing product ID
                try {
                    productId = Integer.parseInt(productIdStr);
                    System.out.println("  ✓ Using existing product ID: " + productId);
                } catch (NumberFormatException e) {
                    System.out.println("  ⚠ Product ID not numeric: " + productIdStr);
                    productId = findProductIdInDatabase(conn, product);
                }
                
                // Find product ID if needed
                if (productId <= 0) {
                    System.out.println("  → Searching database for product ID...");
                    productId = findProductIdInDatabase(conn, product);
                }
                
                if (productId > 0) {
                    productIds.add(String.valueOf(productId));
                    int qty = (product.getItemCount() > 0 ? product.getItemCount() : 1);
                    totalQuantity += qty;
                    System.out.println("  ✓ Added product ID: " + productId + " (qty: " + qty + ")");
                } else {
                    System.out.println("  ✗ ERROR: Could not find product ID for: " + product.getProductName());
                }
            }
            
            // Step 3: Check what we collected
            System.out.println("\n--- Step 3: Validation ---");
            System.out.println("Valid product IDs found: " + productIds.size());
            System.out.println("Total quantity: " + totalQuantity);
            
            if (productIds.isEmpty()) {
                System.out.println("✗ FATAL: No valid product IDs found, cannot save order");
                return;
            }
            
            // Step 4: Create comma-separated string
            String compositeProductId = String.join(",", productIds);
            System.out.println("Composite product ID: " + compositeProductId);
            System.out.println("String length: " + compositeProductId.length() + " characters");
            
            if (compositeProductId.length() > 255) {
                System.out.println("⚠ WARNING: String exceeds VARCHAR(255) limit!");
            }
            
            // Step 5: Database insertion
            System.out.println("\n--- Step 5: Database insertion ---");
            String sql = "INSERT INTO order_products (order_id, product_id, quantity) VALUES (?, ?, ?)";
            PreparedStatement stmt = conn.prepareStatement(sql);
            
            stmt.setInt(1, orderId);
            stmt.setString(2, compositeProductId);
            stmt.setInt(3, totalQuantity);
            
            System.out.println("Executing SQL: " + sql);
            System.out.println("Parameters: orderId=" + orderId + ", productId='" + compositeProductId + "', quantity=" + totalQuantity);
            
            try {
                int result = stmt.executeUpdate();
                
                if (result > 0) {
                    System.out.println("✓ SUCCESS: Insert returned " + result + " affected rows");
                } else {
                    System.out.println("✗ ERROR: Insert returned 0 affected rows");
                }
            } catch (SQLException e) {
                System.out.println("✗ SQL ERROR during insertion: " + e.getMessage());
                System.out.println("SQL State: " + e.getSQLState());
                System.out.println("Error Code: " + e.getErrorCode());
                throw e;
            }
            
            stmt.close();
            
            // Step 6: Verification
            System.out.println("\n--- Step 6: Verification ---");
            String verifySql = "SELECT product_id, quantity FROM order_products WHERE order_id = ?";
            PreparedStatement verifyStmt = conn.prepareStatement(verifySql);
            verifyStmt.setInt(1, orderId);
            ResultSet verifyRs = verifyStmt.executeQuery();
            
            if (verifyRs.next()) {
                String savedProductId = verifyRs.getString("product_id");
                int savedQuantity = verifyRs.getInt("quantity");
                System.out.println("✓ VERIFICATION SUCCESS:");
                System.out.println("  Saved Product ID: " + savedProductId);
                System.out.println("  Saved Quantity: " + savedQuantity);
                System.out.println("  Match: " + (compositeProductId.equals(savedProductId) ? "YES" : "NO"));
            } else {
                System.out.println("✗ VERIFICATION FAILED: No data found in order_products for order " + orderId);
            }
            
            verifyRs.close();
            verifyStmt.close();
            
        } catch (SQLException e) {
            System.out.println("✗ FATAL SQL ERROR: " + e.getMessage());
            e.printStackTrace();
        }
        
        System.out.println("===== DEBUG SAVE COMPLETE =====\n");
    }
    
    private static int findProductIdInDatabase(Connection conn, clearorder.model.Product product) throws SQLException {
        // Try exact match first
        String sql = "SELECT product_id FROM products WHERE product_name = ? AND manufacturer_name = ?";
        PreparedStatement stmt = conn.prepareStatement(sql);
        stmt.setString(1, product.getProductName());
        stmt.setString(2, product.getManufacturerName());
        ResultSet rs = stmt.executeQuery();
        
        if (rs.next()) {
            int id = rs.getInt("product_id");
            System.out.println("    ✓ Found exact match: ID " + id);
            rs.close();
            stmt.close();
            return id;
        }
        rs.close();
        stmt.close();
        
        // Try by name only
        sql = "SELECT product_id FROM products WHERE product_name = ? LIMIT 1";
        stmt = conn.prepareStatement(sql);
        stmt.setString(1, product.getProductName());
        rs = stmt.executeQuery();
        
        if (rs.next()) {
            int id = rs.getInt("product_id");
            System.out.println("    ✓ Found by name: ID " + id);
            rs.close();
            stmt.close();
            return id;
        }
        rs.close();
        stmt.close();
        
        System.out.println("    ✗ No match found in database");
        return 0;
    }
}
