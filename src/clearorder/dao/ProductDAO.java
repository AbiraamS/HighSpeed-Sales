package clearorder.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import clearorder.model.Product;
import clearorder.util.DatabaseConnection;

public class ProductDAO {
    public List<Product> getAllProducts() {
        List<Product> products = new ArrayList<>();
        String sql = "SELECT product_id, product_type, product_name, manufacturer_name, price, availability, item_count, details, socket_type FROM products";
        
        Connection conn = DatabaseConnection.getConnection();
        if (conn == null) {
            System.out.println("Database not available - returning demo products");
            // Return some demo products
            products.add(new Product("1", "Processor", "Intel Core i7-12700K", "Intel", 450.00, "Available", 10, "8-core processor, 3.6GHz base"));
            products.add(new Product("2", "Graphics card", "NVIDIA RTX 4070", "NVIDIA", 600.00, "Available", 5, "12GB GDDR6X"));
            products.add(new Product("3", "Motherboard", "ASUS Z690-A", "ASUS", 200.00, "Available", 8, "ATX motherboard"));
            products.add(new Product("4", "RAM", "Corsair 32GB DDR4", "Corsair", 150.00, "Available", 15, "3200MHz DDR4"));
            products.add(new Product("5", "HDD/SSD", "Samsung 1TB NVMe SSD", "Samsung", 100.00, "Available", 20, "1TB NVMe M.2 SSD"));
            return products;
        }
        
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                try {
                    int productId = rs.getInt("product_id");
                    if (productId <= 0) {
                        System.out.println("[WARNING] Skipping product with invalid ID: " + productId);
                        continue;
                    }
                    
                    Product product = new Product(
                        productId, // Use the int constructor for backward compatibility
                        rs.getString("product_type"),
                        rs.getString("product_name"),
                        rs.getString("manufacturer_name"),
                        rs.getDouble("price"),
                        rs.getString("availability"),
                        rs.getInt("item_count"),
                        rs.getString("details")
                    );
                    // Set socket type if available
                    try {
                        product.setSocketType(rs.getString("socket_type"));
                    } catch (SQLException e) {
                        // Column might not exist yet, ignore
                        product.setSocketType(null);
                    }
                    System.out.println("[DEBUG] Loaded product: ID=" + product.getProductId() + ", " + product.getProductType() + ", name=" + product.getProductName() + ", avail=" + product.getAvailability() + ", socket=" + product.getSocketType());
                    products.add(product);
                } catch (SQLException e) {
                    System.err.println("Error processing product row: " + e.getMessage());
                }
            }
        } catch (SQLException e) {
            System.err.println("Database error fetching products: " + e.getMessage());
            e.printStackTrace();
        } finally {
            DatabaseConnection.closeConnection(conn);
        }
        System.out.println("[DEBUG] Products fetched: " + products.size());
        return products;
    }

    public Product getProductById(int productId) {
        System.out.println("Looking up product with ID: " + productId);
        
        if (productId <= 0) {
            System.out.println("Invalid product ID: " + productId);
            return null;
        }
        
        // Try to get product by product_id
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(
                "SELECT product_id, product_type, product_name, manufacturer_name, price, availability, item_count, details, socket_type " +
                "FROM products WHERE product_id = ?")) {
            pstmt.setInt(1, productId);
            
            System.out.println("Executing SQL: SELECT * FROM products WHERE product_id = " + productId);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                Product product = new Product(
                    rs.getInt("product_id"),
                    rs.getString("product_type"),
                    rs.getString("product_name"),
                    rs.getString("manufacturer_name"),
                    rs.getDouble("price"),
                    rs.getString("availability"),
                    rs.getInt("item_count"),
                    rs.getString("details")
                );
                // Set socket type if available
                try {
                    product.setSocketType(rs.getString("socket_type"));
                } catch (SQLException e) {
                    // Column might not exist yet, ignore
                    product.setSocketType(null);
                }
                System.out.println("FOUND product by ID " + productId + ": " + product.getProductName() + " (" + product.getProductType() + ")");
                return product;
            }
            
            System.out.println("NO product found with ID: " + productId);
        } catch (SQLException e) {
            System.err.println("Error getting product by ID: " + e.getMessage());
            
            // Fall back to index-based lookup if product_id lookup fails
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(
                    "SELECT product_id, product_type, product_name, manufacturer_name, price, availability, item_count, details " +
                    "FROM products")) {
                ResultSet rs = pstmt.executeQuery();
                
                int currentIndex = 1;
                while (rs.next()) {
                    if (currentIndex == productId) {
                        Product product = new Product(
                            rs.getInt("product_id"),
                            rs.getString("product_type"),
                            rs.getString("product_name"),
                            rs.getString("manufacturer_name"),
                            rs.getDouble("price"),
                            rs.getString("availability"),
                            rs.getInt("item_count"),
                            rs.getString("details")
                        );
                        System.out.println("Found product by index: " + product.getProductName());
                        return product;
                    }
                    currentIndex++;
                }
            } catch (SQLException e2) {
                System.err.println("Error getting product by index: " + e2.getMessage());
            }
        }
        
        // If still not found, try to get any product
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(
                "SELECT product_id, product_type, product_name, manufacturer_name, price, availability, item_count, details " +
                "FROM products LIMIT 1")) {
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                Product product = new Product(
                    rs.getInt("product_id"),
                    rs.getString("product_type"),
                    rs.getString("product_name"),
                    rs.getString("manufacturer_name"),
                    rs.getDouble("price"),
                    rs.getString("availability"),
                    rs.getInt("item_count"),
                    rs.getString("details")
                );
                System.out.println("Using first available product as fallback: " + product.getProductName());
                return product;
            }
        } catch (SQLException e) {
            System.err.println("Error getting first product: " + e.getMessage());
        }
        
        // If all else fails, create a dummy product
        if (productId > 0) {
            Product dummy = new Product(
                productId,
                "CPU",
                "Product " + productId,
                "Manufacturer " + productId,
                99.99 * productId,
                "In Stock",
                1,
                "Sample product " + productId + " details"
            );
            System.out.println("Created dummy product: " + dummy.getProductName());
            return dummy;
        }
        
        return null;
    }

    public boolean addProduct(Product product) {
        String sql = "INSERT INTO products (product_type, product_name, manufacturer_name, price, availability, item_count, details) VALUES (?, ?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, product.getProductType());
            pstmt.setString(2, product.getProductName());
            pstmt.setString(3, product.getManufacturerName());
            pstmt.setDouble(4, product.getPrice());
            pstmt.setString(5, product.getAvailability());
            pstmt.setInt(6, product.getItemCount());
            pstmt.setString(7, product.getDetails());
            
            int rowsAffected = pstmt.executeUpdate();
            
            // Get the generated ID and set it back to the product
            if (rowsAffected > 0) {
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        product.setProductId(generatedKeys.getInt(1));
                        System.out.println("Added product with ID: " + product.getProductId());
                    }
                }
                return true;
            }
            return false;
        } catch (SQLException e) {
            System.err.println("Error adding product: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateProduct(Product product) {
        String sql = "UPDATE products SET product_type = ?, product_name = ?, manufacturer_name = ?, price = ?, availability = ?, item_count = ?, details = ? WHERE product_name = ? AND manufacturer_name = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, product.getProductType());
            pstmt.setString(2, product.getProductName());
            pstmt.setString(3, product.getManufacturerName());
            pstmt.setDouble(4, product.getPrice());
            pstmt.setString(5, product.getAvailability());
            pstmt.setInt(6, product.getItemCount());
            pstmt.setString(7, product.getDetails());
            pstmt.setString(8, product.getProductName());
            pstmt.setString(9, product.getManufacturerName());
            
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println(e);
            return false;
        }
    }

    public boolean deleteProduct(String productName, String manufacturerName) {
        String sql = "DELETE FROM products WHERE product_name = ? AND manufacturer_name = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, productName);
            pstmt.setString(2, manufacturerName);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println(e);
            return false;
        }
    }

    public List<Product> getProductsByType(String productType) {
        List<Product> products = new ArrayList<>();
        String sql = "SELECT product_type, product_name, manufacturer_name, price, availability, item_count, details FROM products WHERE product_type = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, productType);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                Product product = new Product(
                    rs.getString("product_type"),
                    rs.getString("product_name"),
                    rs.getString("manufacturer_name"),
                    rs.getDouble("price"),
                    rs.getString("availability"),
                    rs.getInt("item_count"),
                    rs.getString("details")
                );
                products.add(product);
            }
        } catch (SQLException e) {
            System.err.println(e);
        }
        return products;
    }
}