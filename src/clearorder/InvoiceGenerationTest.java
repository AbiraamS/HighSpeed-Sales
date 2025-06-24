package clearorder;

import java.util.List;
import clearorder.dao.CustomerDAO;
import clearorder.dao.InvoiceDAO;
import clearorder.dao.OrderDAO;
import clearorder.dao.ProductDAO;
import clearorder.model.Customer;
import clearorder.model.Invoice;
import clearorder.model.Order;
import clearorder.model.Product;
import clearorder.util.SimpleTextInvoiceGenerator;

/**
 * Test tool to generate an invoice for order 70 to verify the PDF/text generation
 */
public class InvoiceGenerationTest {
      public static void main(String[] args) {
        System.out.println("=== INVOICE GENERATION TEST ===");
        
        try {
            // Test with Order 70
            int orderId = 70;
            
            // Load data
            OrderDAO orderDAO = new OrderDAO();
            InvoiceDAO invoiceDAO = new InvoiceDAO();
            CustomerDAO customerDAO = new CustomerDAO();
            ProductDAO productDAO = new ProductDAO();
            
            Order order = orderDAO.getOrderById(orderId);
            if (order == null) {
                System.out.println("Error: Order " + orderId + " not found");
                return;
            }
            
            System.out.println("Order " + orderId + " found: " + order.getCustomerName());
            
            // Get or create invoice
            Invoice invoice = invoiceDAO.getInvoiceByOrderId(orderId);
            if (invoice == null) {
                System.out.println("No invoice found for order " + orderId + ", creating one...");
                invoice = new Invoice(1234567890L, orderId, 1500.0, 
                                    new java.sql.Date(System.currentTimeMillis()), "Test");
                invoiceDAO.addInvoice(invoice);
            }
            
            System.out.println("Invoice " + invoice.getInvoiceId() + " ready");
            
            // Get customer
            Customer customer = null;
            for (Customer c : customerDAO.getAllCustomers()) {
                if (c.getCustomerName().equals(order.getCustomerName())) {
                    customer = c;
                    break;
                }
            }
            
            if (customer == null) {
                customer = new Customer(1, order.getCustomerName(), "test@example.com", 
                                       "123-456-789", "Test Address", "");
                System.out.println("Created dummy customer for: " + order.getCustomerName());
            }
            
            // Load products from order_products table
            List<Product> products = loadProductsFromOrderProducts(orderId);
            
            if (products.isEmpty()) {
                System.out.println("No products found for order " + orderId);
                return;
            }
            
            System.out.println("Loaded " + products.size() + " products for order " + orderId);
            for (Product product : products) {
                System.out.println("  - " + product.getProductName() + " (Qty: " + product.getItemCount() + ")");
            }
            
            // Generate invoice
            String filename = "invoice_" + orderId + "_test.txt";
            boolean success = SimpleTextInvoiceGenerator.generateInvoice(order, invoice, customer, products, filename);
            
            if (success) {
                System.out.println("Invoice generated successfully: " + filename);
            } else {
                System.out.println("Failed to generate invoice");
            }
        } catch (Exception e) {
            System.err.println("Error in test: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private static List<Product> loadProductsFromOrderProducts(int orderId) {
        List<Product> orderProducts = new java.util.ArrayList<>();
        ProductDAO productDAO = new ProductDAO();
        
        try (java.sql.Connection conn = clearorder.util.DatabaseConnection.getConnection()) {
            String sql = "SELECT product_id, quantity FROM order_products WHERE order_id = ?";
            java.sql.PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, orderId);
            java.sql.ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                int productId = rs.getInt("product_id");
                int quantity = rs.getInt("quantity");
                
                Product product = productDAO.getProductById(productId);
                if (product != null) {
                    product.setItemCount(quantity);
                    orderProducts.add(product);
                    System.out.println("Loaded: " + product.getProductName() + " (ID: " + productId + ", Qty: " + quantity + ")");
                }
            }
        } catch (Exception e) {
            System.err.println("Error loading products: " + e.getMessage());
        }
        
        return orderProducts;
    }
}
