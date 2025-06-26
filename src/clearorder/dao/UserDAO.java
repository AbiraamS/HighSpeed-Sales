package clearorder.dao;

import clearorder.model.User;
import clearorder.util.DatabaseConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UserDAO {
    
    public User findByEmailOrEmployeeNumber(String emailOrNumber) {
        String sql = "SELECT user_id, employee_number, employee_name, employee_email, department, last_login_date, notes FROM users WHERE employee_email = ? OR employee_number = ?";
        
        Connection conn = DatabaseConnection.getConnection();
        if (conn == null) {
            // Return a demo user when database is not available
            System.out.println("Database not available - using demo user");
            return new User(1, 123, "Demo User", "demo@example.com", "IT", "2025-06-20", "Demo user for testing");
        }
        
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, emailOrNumber);
            
            // Try to parse the employee number as an integer if possible
            try {
                int empNumber = Integer.parseInt(emailOrNumber);
                pstmt.setInt(2, empNumber);
            } catch (NumberFormatException e) {
                // If not a number, set a value that won't match any employee number
                pstmt.setInt(2, -1);
            }
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return new User(
                        rs.getInt("user_id"),
                        rs.getInt("employee_number"),
                        rs.getString("employee_name"),
                        rs.getString("employee_email"),
                        rs.getString("department"),
                        rs.getString("last_login_date"),
                        rs.getString("notes")
                    );
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DatabaseConnection.closeConnection(conn);
        }
        return null;
    }
    
    public boolean validateUser(String emailOrNumber, String password) {
        String sql = "SELECT password FROM users WHERE (employee_email = ? OR employee_number = ?) AND password = ?";
        
        Connection conn = DatabaseConnection.getConnection();
        if (conn == null) {
            // For demo purposes, accept any non-empty password
            System.out.println("Database not available - using demo authentication");
            return password != null && !password.trim().isEmpty();
        }
        
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, emailOrNumber);
            
            // Try to parse the employee number as an integer if possible
            try {
                int empNumber = Integer.parseInt(emailOrNumber);
                pstmt.setInt(2, empNumber);
            } catch (NumberFormatException e) {
                // If not a number, set a value that won't match any employee number
                pstmt.setInt(2, -1);
            }
            
            pstmt.setString(3, password);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DatabaseConnection.closeConnection(conn);
        }
        return false;
    }
    
    public void updateLastLoginDate(int userId) {
        String sql = "UPDATE users SET last_login_date = CURRENT_TIMESTAMP WHERE user_id = ?";
        
        Connection conn = DatabaseConnection.getConnection();
        if (conn == null) {
            System.out.println("Database not available - skipping login date update");
            return;
        }
        
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, userId);
            pstmt.executeUpdate();
            
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DatabaseConnection.closeConnection(conn);
        }
    }

    public java.util.List<User> getAllUsers() {
        java.util.List<User> users = new java.util.ArrayList<>();
        String sql = "SELECT user_id, employee_number, employee_name, employee_email, department, last_login_date, notes FROM users";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                users.add(new User(
                    rs.getInt("user_id"),
                    rs.getInt("employee_number"),
                    rs.getString("employee_name"),
                    rs.getString("employee_email"),
                    rs.getString("department"),
                    rs.getString("last_login_date"),
                    rs.getString("notes")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return users;
    }
    
    /**
     * Creates a new user in the database
     * 
     * @param fullName The full name of the employee
     * @param isAdmin Whether the user is an admin or not
     * @param department The department the user belongs to
     * @param notes Any notes about the user
     * @return true if user was created successfully, false otherwise
     */
    public boolean createUser(String fullName, boolean isAdmin, String department, String notes) {
        try {
            // First get the next user ID from the database
            int nextUserId = getNextUserId();
            
            // Create email and password based on user type
            String emailPrefix = isAdmin ? "a" : "m";
            String passwordPrefix = isAdmin ? "admin" : "mitarbeiter";
            
            String email = emailPrefix + nextUserId + "@highspeed.de";
            String password = passwordPrefix + nextUserId;
            // Use the same value for employee_number as the user_id
            int employeeNumber = nextUserId;
            
            System.out.println("[DEBUG] Creating new user: " + fullName);
            System.out.println("[DEBUG] Next User ID: " + nextUserId);
            System.out.println("[DEBUG] Email: " + email);
            System.out.println("[DEBUG] Employee Number: " + employeeNumber);
            System.out.println("[DEBUG] Department: " + department);
            
            // Check if users table has the right structure
            ensureUsersTableExists();
            
            // Now create the user
            String sql = "INSERT INTO users (user_id, employee_number, employee_name, employee_email, department, notes, password) " +
                         "VALUES (?, ?, ?, ?, ?, ?, ?)";
            
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                
                pstmt.setInt(1, employeeNumber); // Set user_id to same as employee_number
                pstmt.setInt(2, employeeNumber);
                pstmt.setString(3, fullName);
                pstmt.setString(4, email);
                pstmt.setString(5, department);
                pstmt.setString(6, notes);
                pstmt.setString(7, password);
                
                System.out.println("[DEBUG] Executing SQL: " + sql);
                int affectedRows = pstmt.executeUpdate();
                System.out.println("[DEBUG] Affected rows: " + affectedRows);
                return affectedRows > 0;
            }
        } catch (Exception e) {
            System.err.println("[ERROR] Failed to create user: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    // Ensure users table has the right structure
    private void ensureUsersTableExists() {
        try (Connection conn = DatabaseConnection.getConnection()) {
            // Check if users table exists
            boolean tableExists = false;
            try (ResultSet tables = conn.getMetaData().getTables(null, null, "users", null)) {
                tableExists = tables.next();
            }
            
            // Create users table if it doesn't exist
            if (!tableExists) {
                try (java.sql.Statement stmt = conn.createStatement()) {
                    String createTableSql = "CREATE TABLE users (" +
                        "user_id INT PRIMARY KEY, " + // Removed AUTO_INCREMENT
                        "employee_number INT NOT NULL, " +
                        "employee_name VARCHAR(100) NOT NULL, " +
                        "employee_email VARCHAR(100) NOT NULL, " +
                        "department VARCHAR(50) NOT NULL, " +
                        "last_login_date TIMESTAMP NULL, " +
                        "notes TEXT NULL, " +
                        "password VARCHAR(100) NOT NULL, " +
                        "UNIQUE (employee_email), " +
                        "UNIQUE (employee_number)" +
                        ")";
                    stmt.executeUpdate(createTableSql);
                    System.out.println("[DEBUG] Created users table");
                }
            } else {
                // Check if password column exists
                boolean hasPasswordColumn = false;
                try (ResultSet columns = conn.getMetaData().getColumns(null, null, "users", "password")) {
                    hasPasswordColumn = columns.next();
                }
                
                if (!hasPasswordColumn) {
                    try (java.sql.Statement stmt = conn.createStatement()) {
                        stmt.executeUpdate("ALTER TABLE users ADD COLUMN password VARCHAR(100) NOT NULL DEFAULT 'changeme'");
                        System.out.println("[DEBUG] Added password column to users table");
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("[ERROR] Failed to ensure users table exists: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Gets the next user number for generating email and password
     * 
     * @param isAdmin Whether to get the next admin number or normal user number
     * @return The next number to use
     */
    public int getNextUserNumber(boolean isAdmin) {
        String emailPattern = isAdmin ? "a%@highspeed.de" : "m%@highspeed.de";
        String sql = "SELECT employee_email FROM users WHERE employee_email LIKE ? ORDER BY employee_email DESC LIMIT 1";
        
        System.out.println("[DEBUG] Getting next user number for " + (isAdmin ? "admin" : "normal user"));
        System.out.println("[DEBUG] Using email pattern: " + emailPattern);
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, emailPattern);
            System.out.println("[DEBUG] Executing SQL: " + sql);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    String email = rs.getString("employee_email");
                    System.out.println("[DEBUG] Found email: " + email);
                    
                    // Extract the number from the email (format: a1@highspeed.de or m5@highspeed.de)
                    Pattern pattern = Pattern.compile(isAdmin ? "a(\\d+)@" : "m(\\d+)@");
                    Matcher matcher = pattern.matcher(email);
                    
                    if (matcher.find()) {
                        int currentNumber = Integer.parseInt(matcher.group(1));
                        int nextNumber = currentNumber + 1;
                        System.out.println("[DEBUG] Current number: " + currentNumber + ", Next number: " + nextNumber);
                        return nextNumber;
                    } else {
                        System.out.println("[DEBUG] Could not extract number from email: " + email);
                    }
                } else {
                    System.out.println("[DEBUG] No email found matching pattern: " + emailPattern);
                }
            }
            
            // If no user found, return 1 for admin or 6 for normal users
            // (assuming a1, m5 are the default starting points)
            int defaultNumber = isAdmin ? 2 : 6;
            System.out.println("[DEBUG] Using default next number: " + defaultNumber);
            return defaultNumber;
            
        } catch (SQLException e) {
            System.err.println("[ERROR] Database error in getNextUserNumber: " + e.getMessage());
            e.printStackTrace();
            
            // Default fallback
            int defaultNumber = isAdmin ? 2 : 6;
            System.out.println("[DEBUG] Using fallback default number after error: " + defaultNumber);
            return defaultNumber;
        }
    }
    
    /**
     * Gets the next user ID by finding the maximum value between user_id and employee_number
     * This ensures we don't have conflicts with existing auto-incremented IDs
     * 
     * @return The next user ID to use
     */
    public int getNextUserId() {
        String sql = "SELECT GREATEST(COALESCE(MAX(user_id), 0), COALESCE(MAX(employee_number), 0)) as max_id FROM users";
        
        System.out.println("[DEBUG] Getting next user ID");
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            System.out.println("[DEBUG] Executing SQL: " + sql);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    int maxId = rs.getInt("max_id");
                    int nextId = maxId + 1;
                    System.out.println("[DEBUG] Current max ID: " + maxId + ", Next ID: " + nextId);
                    return nextId > 0 ? nextId : 1; // Return 1 if table is empty
                }
            }
            
            // If no user found, return 1
            System.out.println("[DEBUG] No users in table, returning ID: 1");
            return 1;
            
        } catch (SQLException e) {
            System.err.println("[ERROR] Database error in getNextUserId: " + e.getMessage());
            e.printStackTrace();
            
            // Default fallback
            System.out.println("[DEBUG] Error occurred, returning default ID: 1");
            return 1;
        }
    }
    
    /**
     * Updates an existing user in the database
     * 
     * @param user The user object with updated information
     * @return true if user was updated successfully, false otherwise
     */
    public boolean updateUser(User user) {
        String sql = "UPDATE users SET employee_name = ?, department = ?, notes = ? WHERE user_id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, user.getEmployeeName());
            pstmt.setString(2, user.getDepartment());
            pstmt.setString(3, user.getNotes());
            pstmt.setInt(4, user.getUserId());
            
            System.out.println("[DEBUG] Updating user: " + user.getEmployeeName());
            System.out.println("[DEBUG] User ID: " + user.getUserId());
            System.out.println("[DEBUG] Department: " + user.getDepartment());
            
            int affectedRows = pstmt.executeUpdate();
            System.out.println("[DEBUG] Affected rows: " + affectedRows);
            return affectedRows > 0;
            
        } catch (SQLException e) {
            System.err.println("[ERROR] Failed to update user: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Gets a user by their ID
     * 
     * @param userId The ID of the user to retrieve
     * @return The User object if found, null otherwise
     */
    public User getUserById(int userId) {
        String sql = "SELECT user_id, employee_number, employee_name, employee_email, department, last_login_date, notes FROM users WHERE user_id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, userId);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return new User(
                        rs.getInt("user_id"),
                        rs.getInt("employee_number"),
                        rs.getString("employee_name"),
                        rs.getString("employee_email"),
                        rs.getString("department"),
                        rs.getString("last_login_date"),
                        rs.getString("notes")
                    );
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
}