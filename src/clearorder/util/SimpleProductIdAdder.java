package clearorder.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A simple utility to add a product_id column to the products table.
 * This is a standalone version that doesn't depend on other classes in the project.
 */
public class SimpleProductIdAdder {
    // Database connection details
    private static final String URL = "jdbc:mysql://3.69.96.96:80/db1";
    private static final String USER = "db1";
    private static final String PASSWORD = "!db1.wip25?SS6";

    public static void main(String[] args) {
        System.out.println("=== ADDING PRODUCT_ID TO PRODUCTS TABLE ===");
        System.out.println("This tool will add a product_id column to your existing products table.");
        System.out.println("It will backup your data before making any changes.");
        
        try {
            // Load the MySQL database driver
            Class.forName("com.mysql.cj.jdbc.Driver");
            System.out.println("MySQL Driver loaded successfully");
            
            addProductIdColumn();
            System.out.println("\n✅ PRODUCT_ID COLUMN ADDED SUCCESSFULLY!");
            System.out.println("Your products now have unique IDs that can be used in the invoice preview.");
        } catch (ClassNotFoundException e) {
            System.err.println("ERROR: MySQL Driver not found. Make sure the MySQL jar is in your classpath.");
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("\n❌ ERROR: " + e.getMessage());
            e.printStackTrace();
            System.out.println("\nIf you see errors related to the product_id column already existing, that may mean the column is already there but not being used properly.");
        }
    }
    
    public static void addProductIdColumn() throws SQLException {
        Connection conn = null;
        try {
            conn = getConnection();
            
            // First, check if the column already exists
            boolean hasProductIdColumn = columnExists(conn, "products", "product_id");
            
            if (hasProductIdColumn) {
                System.out.println("product_id column already exists in the products table.");
                System.out.println("Checking if it's properly configured...");
                
                // Check if it's properly configured
                try {
                    Statement stmt = conn.createStatement();
                    ResultSet rs = stmt.executeQuery("SELECT product_id FROM products ORDER BY product_id LIMIT 1");
                    if (rs.next()) {
                        int firstId = rs.getInt("product_id");
                        System.out.println("First product ID: " + firstId);
                        if (firstId > 0) {
                            System.out.println("product_id column appears to be properly configured.");
                            rs.close();
                            stmt.close();
                            return;
                        }
                    }
                    rs.close();
                    stmt.close();
                    
                    System.out.println("product_id column exists but may not be properly configured. Will reconfigure it.");
                } catch (SQLException e) {
                    System.out.println("Error checking product_id values: " + e.getMessage());
                    System.out.println("Will attempt to reconfigure the column.");
                }
            } else {
                System.out.println("product_id column doesn't exist. Will create it.");
            }
            
            // Get a count of products
            int productCount = 0;
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM products")) {
                if (rs.next()) {
                    productCount = rs.getInt(1);
                }
            }
            System.out.println("Found " + productCount + " products in the table.");
            
            // Back up all products
            List<Map<String, Object>> products = new ArrayList<>();
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT * FROM products")) {
                
                int columnCount = rs.getMetaData().getColumnCount();
                while (rs.next()) {
                    Map<String, Object> product = new HashMap<>();
                    for (int i = 1; i <= columnCount; i++) {
                        String columnName = rs.getMetaData().getColumnName(i);
                        if (!columnName.equalsIgnoreCase("product_id")) { // Skip product_id if it exists
                            product.put(columnName, rs.getObject(i));
                        }
                    }
                    products.add(product);
                }
            }
            
            System.out.println("Backed up " + products.size() + " products.");
            
            // Create a temporary table with product_id
            System.out.println("Creating temporary table with product_id...");
            
            try (Statement stmt = conn.createStatement()) {
                // Drop temp table if it exists
                try {
                    stmt.executeUpdate("DROP TABLE IF EXISTS products_temp");
                } catch (SQLException e) {
                    // Ignore errors when dropping
                }
                
                // Get the create statement for the current table
                String createStatement = "";
                try (ResultSet rs = stmt.executeQuery("SHOW CREATE TABLE products")) {
                    if (rs.next()) {
                        createStatement = rs.getString(2);
                    }
                } catch (SQLException e) {
                    // MySQL-specific command failed, try a more generic approach
                    System.out.println("Could not get CREATE TABLE statement, using a generic one instead.");
                    createStatement = 
                        "CREATE TABLE products_temp (" +
                        "product_id INT AUTO_INCREMENT PRIMARY KEY, " +
                        "product_type VARCHAR(50) NOT NULL, " +
                        "product_name VARCHAR(100) NOT NULL, " +
                        "manufacturer_name VARCHAR(100) NOT NULL, " +
                        "price DOUBLE NOT NULL, " +
                        "availability VARCHAR(50) NOT NULL, " +
                        "item_count INT NOT NULL DEFAULT 1, " +
                        "details LONGTEXT NOT NULL" +
                        ")";
                }
                
                // Modify create statement for temp table with product_id
                if (!createStatement.isEmpty()) {
                    createStatement = createStatement.replace("CREATE TABLE products", "CREATE TABLE products_temp");
                    
                    // Check if the statement already has a product_id
                    if (!createStatement.contains("product_id")) {
                        createStatement = createStatement.replace("(", "(product_id INT AUTO_INCREMENT PRIMARY KEY, ");
                    }
                    
                    System.out.println("Creating temporary table with: " + createStatement);
                    stmt.executeUpdate(createStatement);
                } else {
                    // Use a generic create statement
                    stmt.executeUpdate(
                        "CREATE TABLE products_temp (" +
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
                }
            } catch (SQLException e) {
                System.out.println("Error creating temporary table: " + e.getMessage());
                
                // Try a simpler approach
                try (Statement stmt = conn.createStatement()) {
                    stmt.executeUpdate(
                        "CREATE TABLE products_temp (" +
                        "product_id INT AUTO_INCREMENT PRIMARY KEY, " +
                        "product_type VARCHAR(50) NOT NULL, " +
                        "product_name VARCHAR(100) NOT NULL, " +
                        "manufacturer_name VARCHAR(100) NOT NULL, " +
                        "price DOUBLE NOT NULL, " +
                        "availability VARCHAR(50) NOT NULL, " +
                        "item_count INT NOT NULL DEFAULT 1, " +
                        "details TEXT NOT NULL" +
                        ")"
                    );
                }
            }
            
            // Insert all products into the temporary table
            System.out.println("Inserting products into temporary table...");
            for (Map<String, Object> product : products) {
                StringBuilder columns = new StringBuilder();
                StringBuilder placeholders = new StringBuilder();
                List<Object> values = new ArrayList<>();
                
                for (Map.Entry<String, Object> entry : product.entrySet()) {
                    if (columns.length() > 0) {
                        columns.append(", ");
                        placeholders.append(", ");
                    }
                    columns.append(entry.getKey());
                    placeholders.append("?");
                    values.add(entry.getValue());
                }
                
                String sql = "INSERT INTO products_temp (" + columns + ") VALUES (" + placeholders + ")";
                try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                    for (int i = 0; i < values.size(); i++) {
                        pstmt.setObject(i + 1, values.get(i));
                    }
                    pstmt.executeUpdate();
                }
            }
            
            // Rename tables
            System.out.println("Renaming tables...");
            try (Statement stmt = conn.createStatement()) {
                stmt.executeUpdate("DROP TABLE IF EXISTS products_backup");
                
                // First, backup the original table
                stmt.executeUpdate("CREATE TABLE products_backup AS SELECT * FROM products");
                System.out.println("Original products table backed up to products_backup");
                
                // Then drop the original and rename the temp
                stmt.executeUpdate("DROP TABLE products");
                stmt.executeUpdate("RENAME TABLE products_temp TO products");
            } catch (SQLException e) {
                System.out.println("Error in table operations: " + e.getMessage());
                
                // Try an alternative approach for databases that don't support RENAME TABLE
                try (Statement stmt = conn.createStatement()) {
                    stmt.executeUpdate("DROP TABLE products");
                    stmt.executeUpdate("ALTER TABLE products_temp RENAME TO products");
                } catch (SQLException e2) {
                    // Try another approach
                    try (Statement stmt = conn.createStatement()) {
                        stmt.executeUpdate("DROP TABLE products");
                        stmt.executeUpdate("CREATE TABLE products AS SELECT * FROM products_temp");
                        stmt.executeUpdate("DROP TABLE products_temp");
                    }
                }
            }
            
            // Verify the new table
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM products")) {
                if (rs.next()) {
                    int count = rs.getInt(1);
                    System.out.println("New products table has " + count + " products.");
                    if (count != productCount) {
                        System.out.println("WARNING: Product count mismatch. Expected " + productCount + " but found " + count);
                    }
                }
            }
            
            // Verify product_id column
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT MIN(product_id), MAX(product_id) FROM products")) {
                if (rs.next()) {
                    int minId = rs.getInt(1);
                    int maxId = rs.getInt(2);
                    System.out.println("Product IDs range from " + minId + " to " + maxId);
                }
            }
            
            System.out.println("product_id column has been successfully added to the products table.");
            
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
    
    private static boolean columnExists(Connection conn, String tableName, String columnName) throws SQLException {
        ResultSet rs = conn.getMetaData().getColumns(null, null, tableName, columnName);
        boolean exists = rs.next();
        rs.close();
        return exists;
    }
    
    private static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}
