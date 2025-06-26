package clearorder.util;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DatabaseValidator {
    
    public static void main(String[] args) {        validateUsersTable();
        validateOrdersTable();
        validateCustomersTable();
        validateInvoicesTable();
        validateProductsTable(); // Add products table validation
    }
    
    public static void validateUsersTable() {
        System.out.println("Validating users table...");
        
        try (Connection conn = DatabaseConnection.getConnection()) {
            // Check if users table exists
            boolean tableExists = false;
            try (ResultSet tables = conn.getMetaData().getTables(null, null, "users", null)) {
                if (tables.next()) {
                    tableExists = true;
                    System.out.println("✓ users table exists");
                } else {
                    System.out.println("✗ users table does not exist!");
                }
            }
            
            if (!tableExists) {
                System.out.println("  Creating users table...");
                try (PreparedStatement pstmt = conn.prepareStatement(
                        "CREATE TABLE users (" +
                        "user_id INT AUTO_INCREMENT PRIMARY KEY, " +
                        "employee_number VARCHAR(20) NOT NULL, " +
                        "employee_name VARCHAR(100) NOT NULL, " +
                        "employee_email VARCHAR(100) NOT NULL, " +
                        "department VARCHAR(50) NOT NULL, " +
                        "last_login_date TIMESTAMP NULL, " +
                        "notes TEXT NULL, " +
                        "password VARCHAR(100) NOT NULL, " +
                        "UNIQUE (employee_email), " +
                        "UNIQUE (employee_number)" +
                        ")")) {
                    pstmt.executeUpdate();
                    System.out.println("  ✓ users table created successfully");
                }
            }
            
            // Check column structure
            System.out.println("Checking users table columns...");
            boolean hasPasswordColumn = false;
            try (ResultSet columns = conn.getMetaData().getColumns(null, null, "users", null)) {
                while (columns.next()) {
                    String columnName = columns.getString("COLUMN_NAME");
                    System.out.println("  - " + columnName);
                    if (columnName.equalsIgnoreCase("password")) {
                        hasPasswordColumn = true;
                    }
                }
            }
            
            // Check if password column exists
            if (!hasPasswordColumn) {
                System.out.println("  Adding missing password column...");
                try (PreparedStatement pstmt = conn.prepareStatement(
                        "ALTER TABLE users ADD COLUMN password VARCHAR(100) NOT NULL DEFAULT 'changeme'")) {
                    pstmt.executeUpdate();
                    System.out.println("  ✓ password column added successfully");
                }
            }
            
            // Check for existing users
            System.out.println("Checking existing users...");
            try (PreparedStatement pstmt = conn.prepareStatement("SELECT COUNT(*) FROM users");
                 ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    int count = rs.getInt(1);
                    System.out.println("  Found " + count + " users in the table");
                }
            }
            
            // Test inserting a dummy user
            System.out.println("Testing user creation...");
            try (PreparedStatement pstmt = conn.prepareStatement(
                    "INSERT INTO users (employee_number, employee_name, employee_email, department, notes, password) " +
                    "VALUES ('TEST123', 'Test User', 'test@example.com', 'Test Department', 'Test notes', 'password123')")) {
                int result = pstmt.executeUpdate();
                System.out.println("  ✓ Test user inserted: " + (result > 0 ? "success" : "failed"));
                
                // Delete the test user
                try (PreparedStatement deleteStmt = conn.prepareStatement(
                        "DELETE FROM users WHERE employee_email = 'test@example.com'")) {
                    deleteStmt.executeUpdate();
                    System.out.println("  ✓ Test user deleted");
                }
            } catch (SQLException e) {
                System.out.println("  ✗ Test user insertion failed: " + e.getMessage());
                if (e.getMessage().contains("Duplicate entry")) {
                    System.out.println("    This is expected if test users already exist");
                } else {
                    throw e;
                }
            }
            
            System.out.println("Validation completed successfully");
            
        } catch (SQLException e) {
            System.err.println("Validation failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    public static void validateOrdersTable() {
        System.out.println("Validating orders table...");
        
        try (Connection conn = DatabaseConnection.getConnection()) {
            // Check if orders table exists
            boolean tableExists = false;
            try (ResultSet tables = conn.getMetaData().getTables(null, null, "orders", null)) {
                if (tables.next()) {
                    tableExists = true;
                    System.out.println("✓ orders table exists");
                } else {
                    System.out.println("✗ orders table does not exist!");
                }
            }
            
            if (!tableExists) {
                System.out.println("  Creating orders table...");
                try (PreparedStatement pstmt = conn.prepareStatement(
                        "CREATE TABLE orders (" +
                        "order_id INT PRIMARY KEY, " +
                        "order_number VARCHAR(20) NOT NULL, " +
                        "customer_name VARCHAR(100) NOT NULL, " +
                        "order_date DATE NOT NULL, " +
                        "status VARCHAR(50) NOT NULL, " +
                        "invoice_id BIGINT NULL" +
                        ")")) {
                    pstmt.executeUpdate();
                    System.out.println("  ✓ orders table created successfully");
                }
            }
            
            // Check if invoice_id column exists
            boolean hasInvoiceIdColumn = false;
            try (ResultSet columns = conn.getMetaData().getColumns(null, null, "orders", null)) {
                while (columns.next()) {
                    String columnName = columns.getString("COLUMN_NAME");
                    System.out.println("  - " + columnName);
                    if (columnName.equalsIgnoreCase("invoice_id")) {
                        hasInvoiceIdColumn = true;
                    }
                }
            }
            
            // Add invoice_id column if it doesn't exist
            if (!hasInvoiceIdColumn) {
                System.out.println("  Adding missing invoice_id column...");
                try (PreparedStatement pstmt = conn.prepareStatement(
                        "ALTER TABLE orders ADD COLUMN invoice_id BIGINT NULL")) {
                    pstmt.executeUpdate();
                    System.out.println("  ✓ invoice_id column added successfully");
                }
            }
            
            System.out.println("Orders table validation completed successfully");
            
        } catch (SQLException e) {
            System.err.println("Orders table validation failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    public static void validateCustomersTable() {
        System.out.println("Validating customers table...");
        try (Connection conn = DatabaseConnection.getConnection()) {
            // Check if customers table exists
            boolean tableExists = false;
            try (ResultSet tables = conn.getMetaData().getTables(null, null, "customers", null)) {
                if (tables.next()) {
                    tableExists = true;
                    System.out.println("✓ customers table exists");
                } else {
                    System.out.println("✗ customers table does not exist!");
                }
            }
            if (!tableExists) {
                System.out.println("  Creating customers table...");
                try (PreparedStatement pstmt = conn.prepareStatement(
                        "CREATE TABLE customers (" +
                        "customer_id INT AUTO_INCREMENT PRIMARY KEY, " +
                        "customer_name VARCHAR(100) NOT NULL, " +
                        "email VARCHAR(100) NOT NULL, " +
                        "address VARCHAR(255), " +
                        "phone VARCHAR(50) NOT NULL" +
                        ")")) {
                    pstmt.executeUpdate();
                    System.out.println("  ✓ customers table created successfully");
                }
            }
            // Check if address column exists
            boolean hasAddressColumn = false;
            try (ResultSet columns = conn.getMetaData().getColumns(null, null, "customers", null)) {
                while (columns.next()) {
                    String columnName = columns.getString("COLUMN_NAME");
                    if (columnName.equalsIgnoreCase("address")) {
                        hasAddressColumn = true;
                    }
                }
            }
            if (!hasAddressColumn) {
                System.out.println("  Adding missing address column...");
                try (PreparedStatement pstmt = conn.prepareStatement(
                        "ALTER TABLE customers ADD COLUMN address VARCHAR(255)")) {
                    pstmt.executeUpdate();
                    System.out.println("  ✓ address column added successfully");
                }
            }
            System.out.println("Customers table validation completed successfully");
        } catch (SQLException e) {
            System.err.println("Customers table validation failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    public static void validateInvoicesTable() {
        System.out.println("Validating invoices table...");
        
        try (Connection conn = DatabaseConnection.getConnection()) {
            // Check if invoices table exists
            boolean tableExists = false;
            try (ResultSet tables = conn.getMetaData().getTables(null, null, "invoices", null)) {
                if (tables.next()) {
                    tableExists = true;
                    System.out.println("✓ invoices table exists");
                } else {
                    System.out.println("✗ invoices table does not exist!");
                }
            }
            
            if (!tableExists) {
                System.out.println("  Creating invoices table...");
                try (PreparedStatement pstmt = conn.prepareStatement(
                        "CREATE TABLE invoices (" +
                        "invoice_id BIGINT PRIMARY KEY, " +
                        "order_id INT NOT NULL, " +
                        "total_price DOUBLE NOT NULL, " +
                        "invoice_date DATE NOT NULL, " +
                        "status VARCHAR(50) NOT NULL, " +
                        "FOREIGN KEY (order_id) REFERENCES orders(order_id)" +
                        ")")) {
                    pstmt.executeUpdate();
                    System.out.println("  ✓ invoices table created successfully");
                }
            }
        } catch (SQLException e) {
            System.err.println("Error validating invoices table: " + e.getMessage());
            e.printStackTrace();
        }    }
    
    // Add a method to validate the products table
    public static void validateProductsTable() {
        System.out.println("Validating products table...");
        
        try (Connection conn = DatabaseConnection.getConnection()) {
            // Check if products table exists
            boolean tableExists = false;
            try (ResultSet tables = conn.getMetaData().getTables(null, null, "products", null)) {
                if (tables.next()) {
                    tableExists = true;
                    System.out.println("✓ products table exists");
                } else {
                    System.out.println("✗ products table does not exist!");
                }
            }
            
            if (!tableExists) {                
                System.out.println("  Creating products table...");
                try (PreparedStatement pstmt = conn.prepareStatement(
                        "CREATE TABLE products (" +
                        "product_id INT AUTO_INCREMENT PRIMARY KEY, " +
                        "product_type VARCHAR(50) NOT NULL, " +
                        "product_name VARCHAR(100) NOT NULL, " +
                        "manufacturer_name VARCHAR(100) NOT NULL, " +
                        "price DOUBLE NOT NULL, " +
                        "availability VARCHAR(50) NOT NULL, " +
                        "item_count INT NOT NULL DEFAULT 1, " +
                        "details LONGTEXT NOT NULL" +
                        ")")) {
                    pstmt.executeUpdate();
                    System.out.println("  ✓ products table created successfully with compatible schema");
                    
                    // Add sample products
                    addSampleProducts(conn);
                }
            } else {
                // Check if product_id column exists
                boolean hasProductIdColumn = false;
                try (ResultSet columns = conn.getMetaData().getColumns(null, null, "products", "product_id")) {
                    if (columns.next()) {
                        hasProductIdColumn = true;
                        System.out.println("  ✓ product_id column exists");
                    }
                }
                
                // Add product_id column if it doesn't exist
                if (!hasProductIdColumn) {
                    System.out.println("  Adding missing product_id column...");
                    try {
                        // First, add the column
                        PreparedStatement addColumn = conn.prepareStatement(
                            "ALTER TABLE products ADD COLUMN product_id INT AUTO_INCREMENT PRIMARY KEY");
                        addColumn.executeUpdate();
                        System.out.println("  ✓ product_id column added successfully");
                    } catch (SQLException e) {
                        System.out.println("  Failed to add product_id column: " + e.getMessage());
                        
                        // If the error is about adding AUTO_INCREMENT, try without it
                        try {
                            PreparedStatement addColumn = conn.prepareStatement(
                                "ALTER TABLE products ADD COLUMN product_id INT");
                            addColumn.executeUpdate();
                            
                            // Update the column with sequential values
                            PreparedStatement updateValues = conn.prepareStatement(
                                "SET @counter = 0; UPDATE products SET product_id = (@counter:=@counter+1)");
                            updateValues.executeUpdate();
                            
                            // Try to add PRIMARY KEY and AUTO_INCREMENT
                            PreparedStatement modifyColumn = conn.prepareStatement(
                                "ALTER TABLE products MODIFY COLUMN product_id INT AUTO_INCREMENT PRIMARY KEY");
                            modifyColumn.executeUpdate();
                            
                            System.out.println("  ✓ product_id column added and configured successfully (alternative method)");
                        } catch (SQLException e2) {
                            System.out.println("  Failed to add product_id column (alternative method): " + e2.getMessage());
                        }
                    }
                }
                
                // Check if we have any products
                int productCount = 0;
                try (PreparedStatement pstmt = conn.prepareStatement("SELECT COUNT(*) FROM products");
                     ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        productCount = rs.getInt(1);
                    }
                }
                
                if (productCount == 0) {
                    System.out.println("  No products found. Adding sample products...");
                    addSampleProducts(conn);
                } else {
                    System.out.println("  Found " + productCount + " products in the database");
                }
            }
        } catch (SQLException e) {
            System.err.println("Error validating products table: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private static void addSampleProducts(Connection conn) {
        System.out.println("  Adding sample products...");
        
        // Sample PC components
        String[][] products = {
            {"CPU", "Ryzen 7 5800X", "AMD", "349.99", "In Stock", "10", "8 cores, 3.8GHz base, 4.7GHz boost"},
            {"CPU", "Core i9-12900K", "Intel", "499.99", "In Stock", "5", "16 cores (8P+8E), 3.2GHz base, 5.2GHz boost"},
            {"GPU", "Radeon RX 6800 XT", "AMD", "649.99", "In Stock", "3", "16GB GDDR6, 2250MHz, 128 compute units"},
            {"GPU", "GeForce RTX 3080", "NVIDIA", "699.99", "Limited", "2", "10GB GDDR6X, 8704 CUDA cores"},
            {"Motherboard", "ROG Strix X570-E", "ASUS", "299.99", "In Stock", "7", "AMD X570, ATX, PCIe 4.0, WiFi 6"},
            {"Motherboard", "Z690 Aorus Master", "Gigabyte", "399.99", "In Stock", "4", "Intel Z690, ATX, PCIe 5.0, DDR5"},
            {"RAM", "Trident Z Neo", "G.Skill", "169.99", "In Stock", "15", "32GB (2x16GB), DDR4-3600, CL16"},
            {"RAM", "Dominator Platinum", "Corsair", "229.99", "In Stock", "8", "32GB (2x16GB), DDR5-5200, CL38"},
            {"SSD", "970 EVO Plus", "Samsung", "149.99", "In Stock", "20", "1TB, NVMe, PCIe 3.0, 3500MB/s read"},
            {"SSD", "980 PRO", "Samsung", "229.99", "In Stock", "12", "2TB, NVMe, PCIe 4.0, 7000MB/s read"}
        };
        
        try {
            for (String[] product : products) {
                PreparedStatement pstmt = conn.prepareStatement(
                        "INSERT INTO products (product_type, product_name, manufacturer_name, price, availability, item_count, details) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?)");
                
                pstmt.setString(1, product[0]);  // product_type
                pstmt.setString(2, product[1]);  // product_name
                pstmt.setString(3, product[2]);  // manufacturer_name
                pstmt.setDouble(4, Double.parseDouble(product[3]));  // price
                pstmt.setString(5, product[4]);  // availability
                pstmt.setInt(6, Integer.parseInt(product[5]));  // item_count
                pstmt.setString(7, product[6]);  // details
                
                pstmt.executeUpdate();
                pstmt.close();
            }
            
            System.out.println("  ✓ Added " + products.length + " sample products");
        } catch (SQLException e) {
            System.err.println("Error adding sample products: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
