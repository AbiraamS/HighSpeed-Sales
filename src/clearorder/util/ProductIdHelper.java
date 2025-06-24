package clearorder.util;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import clearorder.model.Product;

/**
 * Helper class for dealing with the new string-based product IDs in the order_products table.
 * This supports both legacy integer product IDs and the new comma-separated string format.
 */
public class ProductIdHelper {
    
    /**
     * Parse a product ID string which might be a single integer or a comma-separated list
     * @param productIdStr The product ID string
     * @return List of individual product IDs as integers
     */
    public static List<Integer> parseProductIdString(String productIdStr) {
        List<Integer> productIds = new ArrayList<>();
        
        if (productIdStr == null || productIdStr.isEmpty()) {
            return productIds;
        }
        
        // If it contains commas, split and parse each part
        if (productIdStr.contains(",")) {
            String[] parts = productIdStr.split(",");
            for (String part : parts) {
                try {
                    int id = Integer.parseInt(part.trim());
                    productIds.add(id);
                } catch (NumberFormatException e) {
                    System.err.println("Invalid product ID part: " + part);
                    // Skip invalid parts
                }
            }
        } else {
            // Try to parse as a single integer
            try {
                int id = Integer.parseInt(productIdStr.trim());
                productIds.add(id);
            } catch (NumberFormatException e) {
                System.err.println("Invalid product ID: " + productIdStr);
                // Return empty list if invalid
            }
        }
        
        return productIds;
    }
    
    /**
     * Load a product from the database by its ID
     * @param conn Database connection
     * @param productId Product ID
     * @param quantity Quantity for the product
     * @return The loaded product or null if not found
     */
    public static Product loadProductById(Connection conn, int productId, int quantity) throws SQLException {
        String sql = "SELECT * FROM products WHERE product_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, productId);
            ResultSet rs = stmt.executeQuery();
                    
            if (rs.next()) {
                String productName = rs.getString("product_name");
                String productType = rs.getString("product_type");
                String manufacturer = rs.getString("manufacturer_name");
                double price = rs.getDouble("price");
                String availability = rs.getString("availability");
                String details = rs.getString("details");
                
                return new Product(
                    String.valueOf(productId),
                    productType,
                    productName,
                    manufacturer,
                    price,
                    availability,
                    quantity,
                    details
                );
            }
        }
        return null;
    }
    
    /**
     * Loads all products from a comma-separated product ID string
     * @param conn Database connection
     * @param productIdStr Comma-separated product IDs
     * @param quantity Quantity for each product
     * @return List of loaded products
     */
    public static List<Product> loadProductsFromIdString(Connection conn, String productIdStr, int quantity) 
            throws SQLException {
        List<Product> products = new ArrayList<>();
        List<Integer> productIds = parseProductIdString(productIdStr);
        
        for (int productId : productIds) {
            Product product = loadProductById(conn, productId, quantity);
            if (product != null) {
                products.add(product);
                System.out.println("Loaded product: " + product.getProductName() + 
                                 " (ID: " + product.getProductId() + ", Qty: " + quantity + ")");
            }
        }
        
        return products;
    }
}
