package clearorder.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import clearorder.model.Order;
import clearorder.util.DatabaseConnection;

public class OrderDAO {
    public List<Order> getAllOrders() {
        List<Order> orders = new ArrayList<>();
        String sql = "SELECT order_id, order_number, customer_name, order_date, status, invoice_id FROM orders";
        
        Connection conn = DatabaseConnection.getConnection();
        if (conn == null) {
            System.out.println("Database not available - returning demo orders");
            // Return some demo orders
            orders.add(new Order(1, "ORD-001", "Demo Customer 1", new java.sql.Date(System.currentTimeMillis()), "In Bearbeitung", 1001L));
            orders.add(new Order(2, "ORD-002", "Demo Customer 2", new java.sql.Date(System.currentTimeMillis() - 86400000), "Abgeschlossen", 1002L));
            orders.add(new Order(3, "ORD-003", "Demo Customer 3", new java.sql.Date(System.currentTimeMillis() - 172800000), "Neue Bestellung", 1003L));
            return orders;
        }
        
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            // Debug: Print the SQL and count results
            System.out.println("[DEBUG] Executing SQL: " + sql);
            int rowCount = 0;
            
            while (rs.next()) {
                rowCount++;
                Order order = new Order(
                    rs.getInt("order_id"),
                    rs.getString("order_number"),
                    rs.getString("customer_name"),
                    rs.getDate("order_date"),
                    rs.getString("status"),
                    rs.getLong("invoice_id")
                );
                orders.add(order);
                
                // Debug: Print order details
                System.out.println("[DEBUG] Retrieved order: ID=" + order.getOrderId() + 
                                  ", Number=" + order.getOrderNumber() + 
                                  ", Customer=" + order.getCustomerName() +
                                  ", InvoiceID=" + order.getInvoiceId());
            }
            
            System.out.println("[DEBUG] Total orders retrieved: " + rowCount);
            System.out.println("[DEBUG] Orders list size: " + orders.size());
            
        } catch (SQLException e) {
            e.printStackTrace();
            System.err.println("Error in getAllOrders: " + e.getMessage());
        } finally {
            DatabaseConnection.closeConnection(conn);
        }
        return orders;
    }
    
    // No longer needed as invoice_id is in the orders table
    // private int getInvoiceIdForOrder(int orderId) {
    //     String sql = "SELECT invoice_id FROM invoices WHERE order_id = ? LIMIT 1";
    //     try (Connection conn = DatabaseConnection.getConnection();
    //          PreparedStatement pstmt = conn.prepareStatement(sql)) {
    //         pstmt.setInt(1, orderId);
    //         ResultSet rs = pstmt.executeQuery();
    //         if (rs.next()) {
    //             return rs.getInt("invoice_id");
    //         }
    //     } catch (SQLException e) {
    //         e.printStackTrace();
    //         System.err.println("Error getting invoice ID: " + e.getMessage());
    //     }
    //     return 0;
    // }

    public Order getOrderById(int orderId) {
        String sql = "SELECT order_id, order_number, customer_name, order_date, status, invoice_id FROM orders WHERE order_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, orderId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return new Order(
                    rs.getInt("order_id"),
                    rs.getString("order_number"),
                    rs.getString("customer_name"),
                    rs.getDate("order_date"),
                    rs.getString("status"),
                    rs.getLong("invoice_id")
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
            System.err.println("Error in getOrderById: " + e.getMessage());
        }
        return null;
    }

    public boolean addOrder(Order order) {
        String sql = "INSERT INTO orders (order_id, order_number, customer_name, order_date, status, invoice_id) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, order.getOrderId());
            pstmt.setString(2, order.getOrderNumber());
            pstmt.setString(3, order.getCustomerName());
            pstmt.setDate(4, order.getOrderDate());
            pstmt.setString(5, order.getStatus());
            pstmt.setLong(6, order.getInvoiceId());
            int rows = pstmt.executeUpdate();
            System.out.println("[DEBUG] addOrder: rows affected = " + rows);
            return rows > 0;
        } catch (SQLException e) {
            System.err.println("[ERROR] addOrder failed: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateOrder(Order order) {
        String sql = "UPDATE orders SET order_number = ?, customer_name = ?, order_date = ?, status = ?, invoice_id = ? WHERE order_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, order.getOrderNumber());
            pstmt.setString(2, order.getCustomerName());
            pstmt.setDate(3, order.getOrderDate());
            pstmt.setString(4, order.getStatus());
            pstmt.setLong(5, order.getInvoiceId());
            pstmt.setInt(6, order.getOrderId());
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean deleteOrder(int orderId) {
        String sql = "DELETE FROM orders WHERE order_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, orderId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<Order> getOrdersByCustomerName(String customerName) {
        List<Order> orders = new ArrayList<>();
        String sql = "SELECT order_id, order_number, customer_name, order_date, status, invoice_id FROM orders WHERE customer_name = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, customerName);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                Order order = new Order(
                    rs.getInt("order_id"),
                    rs.getString("order_number"),
                    rs.getString("customer_name"),
                    rs.getDate("order_date"),
                    rs.getString("status"),
                    rs.getLong("invoice_id")
                );
                orders.add(order);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            System.err.println("Error in getOrdersByCustomerName: " + e.getMessage());
        }
        return orders;
    }

    public int getNextOrderId() {
        String sql = "SELECT MAX(order_id) as max_id FROM orders";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                int maxId = rs.getInt("max_id");
                return maxId + 1;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        // If no orders exist or there was an error, start from 1
        return 1;
    }
}