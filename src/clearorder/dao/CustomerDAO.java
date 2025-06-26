package clearorder.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import clearorder.model.Customer;
import clearorder.util.DatabaseConnection;

public class CustomerDAO {
    public List<Customer> getAllCustomers() {
        List<Customer> customers = new ArrayList<>();
        String sql = "SELECT customer_id, customer_name, customer_email, address, customer_telephone_number, customer_signature FROM customers";
        
        Connection conn = DatabaseConnection.getConnection();
        if (conn == null) {
            System.out.println("Database not available - returning demo customers");
            // Return some demo customers
            customers.add(new Customer(1, "Demo Customer 1", "demo1@example.com", "123-456-7890", "123 Demo Street", "Demo Signature 1"));
            customers.add(new Customer(2, "Demo Customer 2", "demo2@example.com", "098-765-4321", "456 Demo Avenue", "Demo Signature 2"));
            customers.add(new Customer(3, "Demo Customer 3", "demo3@example.com", "555-123-4567", "789 Demo Boulevard", "Demo Signature 3"));
            return customers;
        }
        
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Customer customer = new Customer(
                    rs.getInt("customer_id"),
                    rs.getString("customer_name"),
                    rs.getString("customer_email"),
                    rs.getString("customer_telephone_number"),
                    rs.getString("address"),
                    rs.getString("customer_signature")
                );
                customers.add(customer);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DatabaseConnection.closeConnection(conn);
        }
        System.out.println("[DEBUG] Customers fetched: " + customers.size());
        return customers;
    }

    public Customer getCustomerById(int customerId) {
        String sql = "SELECT customer_id, customer_name, customer_email, address, customer_telephone_number, customer_signature FROM customers WHERE customer_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, customerId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return new Customer(
                    rs.getInt("customer_id"),
                    rs.getString("customer_name"),
                    rs.getString("customer_email"),
                    rs.getString("customer_telephone_number"),
                    rs.getString("address"),
                    rs.getString("customer_signature")
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean addCustomer(Customer customer) {
        String sql = "INSERT INTO customers (customer_id, customer_name, customer_email, address, customer_telephone_number, customer_signature) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, customer.getCustomerId());
            pstmt.setString(2, customer.getCustomerName());
            pstmt.setString(3, customer.getEmail());
            pstmt.setString(4, customer.getAddress());
            pstmt.setString(5, customer.getPhone());
            pstmt.setString(6, customer.getSignature());
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateCustomer(Customer customer) {
        String sql = "UPDATE customers SET customer_name = ?, customer_email = ?, address = ?, customer_telephone_number = ?, customer_signature = ? WHERE customer_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, customer.getCustomerName());
            pstmt.setString(2, customer.getEmail());
            pstmt.setString(3, customer.getAddress());
            pstmt.setString(4, customer.getPhone());
            pstmt.setString(5, customer.getSignature());
            pstmt.setInt(6, customer.getCustomerId());
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean deleteCustomer(int customerId) {
        String sql = "DELETE FROM customers WHERE customer_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, customerId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}