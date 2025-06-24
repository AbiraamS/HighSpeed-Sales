package clearorder.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import clearorder.model.Invoice;
import clearorder.util.DatabaseConnection;

public class InvoiceDAO {
    private Connection connection;

    public InvoiceDAO() {
        this.connection = DatabaseConnection.getConnection();
    }

    public List<Invoice> getAllInvoices() {
        List<Invoice> invoices = new ArrayList<>();
        String sql = "SELECT invoice_id, order_id, total_price, invoice_date, status FROM invoices";

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Invoice invoice = createInvoiceFromResultSet(rs);
                invoices.add(invoice);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return invoices;
    }

    public Invoice getInvoiceById(long invoiceId) {
        String sql = "SELECT invoice_id, order_id, total_price, invoice_date, status FROM invoices WHERE invoice_id = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setLong(1, invoiceId);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return createInvoiceFromResultSet(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean addInvoice(Invoice invoice) {
        String sql = "INSERT INTO invoices (invoice_id, order_id, total_price, invoice_date, status) VALUES (?, ?, ?, ?, ?)";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setLong(1, invoice.getInvoiceId());
            pstmt.setInt(2, invoice.getOrderId());
            pstmt.setDouble(3, invoice.getTotalPrice());
            pstmt.setDate(4, invoice.getInvoiceDate());
            pstmt.setString(5, invoice.getStatus());
            
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateInvoice(Invoice invoice) {
        String sql = "UPDATE invoices SET order_id = ?, total_price = ?, invoice_date = ?, status = ? WHERE invoice_id = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, invoice.getOrderId());
            pstmt.setDouble(2, invoice.getTotalPrice());
            pstmt.setDate(3, invoice.getInvoiceDate());
            pstmt.setString(4, invoice.getStatus());
            pstmt.setLong(5, invoice.getInvoiceId());
            
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean deleteInvoice(long invoiceId) {
        String sql = "DELETE FROM invoices WHERE invoice_id = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setLong(1, invoiceId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<Invoice> getInvoicesByOrderId(int orderId) {
        List<Invoice> invoices = new ArrayList<>();
        String sql = "SELECT invoice_id, order_id, total_price, invoice_date, status FROM invoices WHERE order_id = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, orderId);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                Invoice invoice = createInvoiceFromResultSet(rs);
                invoices.add(invoice);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return invoices;
    }

    public Invoice getInvoiceByOrderId(int orderId) {
        String sql = "SELECT invoice_id, order_id, total_price, invoice_date, status FROM invoices WHERE order_id = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, orderId);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return createInvoiceFromResultSet(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // Helper method to create an Invoice object from a ResultSet
    private Invoice createInvoiceFromResultSet(ResultSet rs) throws SQLException {
        return new Invoice(
            rs.getLong("invoice_id"),
            rs.getInt("order_id"),
            rs.getDouble("total_price"),
            rs.getDate("invoice_date"),
            rs.getString("status")
        );
    }
}