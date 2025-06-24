package clearorder.util;

import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.List;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.Image;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

import clearorder.dao.CustomerDAO;
import clearorder.dao.InvoiceDAO;
import clearorder.dao.OrderDAO;
import clearorder.dao.PCConfigurationDAO;
import clearorder.dao.ProductDAO;
import clearorder.model.Customer;
import clearorder.model.Invoice;
import clearorder.model.Order;
import clearorder.model.PCConfiguration;
import clearorder.model.Product;

public class InvoicePdfGenerator {
    
    // Define fonts
    private static final Font TITLE_FONT = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 20, BaseColor.BLACK);
    private static final Font HEADER_FONT = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, BaseColor.BLACK);
    private static final Font SUBHEADER_FONT = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, BaseColor.BLACK);
    private static final Font NORMAL_FONT = FontFactory.getFont(FontFactory.HELVETICA, 10, BaseColor.BLACK);
    
    /**
     * Generate an invoice PDF file for an order
     * 
     * @param order       The order to generate an invoice for
     * @param invoice     The invoice associated with the order
     * @param customer    The customer who placed the order
     * @param products    The list of products in the order
     * @param filePath    The path where to save the PDF file
     */
    public static void generateInvoicePdf(Order order, Invoice invoice, Customer customer, 
                                        List<PCConfiguration> configs, List<Product> products, String filePath) 
                                        throws DocumentException, IOException {
        
        Document document = new Document();
        PdfWriter.getInstance(document, new FileOutputStream(filePath));
        document.open();
          // Add company logo (using clearorder's logo)
        try {
            java.io.File logoFile = null;
            try {
                // First, try to load from resources
                java.net.URL logoUrl = InvoicePdfGenerator.class.getClassLoader().getResource("clearorder/clearorder-neueLogo.png");
                if (logoUrl != null) {
                    Image logo = Image.getInstance(logoUrl);
                    logo.scaleToFit(150, 150);
                    logo.setAlignment(Element.ALIGN_LEFT);
                    document.add(logo);
                } else {
                    // Try using a direct file path as fallback
                    java.io.File projectDir = new java.io.File(System.getProperty("user.dir"));
                    logoFile = new java.io.File(projectDir, "src/clearorder/clearorder-neueLogo.png");
                    if (logoFile.exists()) {
                        Image logo = Image.getInstance(logoFile.getAbsolutePath());
                        logo.scaleToFit(150, 150);
                        logo.setAlignment(Element.ALIGN_LEFT);
                        document.add(logo);
                    } else {
                        System.err.println("Logo file not found at: " + logoFile.getAbsolutePath());
                    }
                }
            } catch (Exception e) {
                System.err.println("Could not load logo image: " + e.getMessage());
                // Continue without logo
            }
        } catch (Exception e) {
            // If logo can't be loaded, just continue without it
            System.err.println("Error processing logo image: " + e.getMessage());
        }
        
        // Add title
        Paragraph title = new Paragraph("RECHNUNG", TITLE_FONT);
        title.setAlignment(Element.ALIGN_CENTER);
        title.setSpacingBefore(20);
        title.setSpacingAfter(20);
        document.add(title);
        
        // Add invoice details
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");
        document.add(new Paragraph("Rechnungsnummer: " + invoice.getInvoiceId(), HEADER_FONT));
        document.add(new Paragraph("Bestellnummer: " + order.getOrderId(), HEADER_FONT));
        document.add(new Paragraph("Datum: " + dateFormat.format(invoice.getInvoiceDate()), HEADER_FONT));
        document.add(new Paragraph("Status: " + order.getStatus(), HEADER_FONT));
        document.add(new Paragraph(" "));  // Empty line for spacing
        
        // Add company info
        PdfPTable companyInfoTable = new PdfPTable(2);
        companyInfoTable.setWidthPercentage(100);
        
        PdfPCell cell = new PdfPCell(new Paragraph("HighSpeed Stuttgart", HEADER_FONT));
        cell.setBorder(Rectangle.NO_BORDER);
        companyInfoTable.addCell(cell);
        
        cell = new PdfPCell(new Paragraph("Rechnungsempfänger:", HEADER_FONT));
        cell.setBorder(Rectangle.NO_BORDER);
        companyInfoTable.addCell(cell);
        
        cell = new PdfPCell(new Paragraph(
            "Königstraße 45\n" +
            "70173 Stuttgart\n" +
            "Tel: +49 711 123456\n" +
            "Email: info@highspeed-stuttgart.de\n" +
            "USt-IdNr: DE123456789", NORMAL_FONT));
        cell.setBorder(Rectangle.NO_BORDER);
        companyInfoTable.addCell(cell);
        
        cell = new PdfPCell(new Paragraph(
            customer.getCustomerName() + "\n" +
            customer.getAddress() + "\n" +
            "Tel: " + customer.getPhone() + "\n" +
            "Email: " + customer.getEmail(), NORMAL_FONT));
        cell.setBorder(Rectangle.NO_BORDER);
        companyInfoTable.addCell(cell);
        
        document.add(companyInfoTable);
        document.add(new Paragraph(" "));  // Empty line for spacing
        
        // Add products table
        document.add(new Paragraph("Bestellte Artikel:", HEADER_FONT));
        document.add(new Paragraph(" "));  // Empty line for spacing
        
        PdfPTable productsTable = new PdfPTable(new float[] { 2, 6, 1, 2, 3 });
        productsTable.setWidthPercentage(100);
        
        // Add table header
        addTableHeader(productsTable);
          // Add table rows
        double totalPrice = 0;
        int itemNumber = 1;
        
        // Use products directly if configs is empty (when loaded from order_products table)
        if (configs.isEmpty() && !products.isEmpty()) {
            System.out.println("PDF Generator: Using products directly from order_products table");
            for (Product product : products) {
                int quantity = product.getItemCount(); // This contains the quantity from order_products
                double price = product.getPrice();
                addProductRow(productsTable, itemNumber++, product, quantity, price);
                totalPrice += price * quantity;
                System.out.println("PDF Generator: Added product row: " + product.getProductName() + " (Qty: " + quantity + ", Price: " + price + ")");
            }
        } else {
            // Fallback to old configuration method
            System.out.println("PDF Generator: Using configurations (fallback method)");
            for (PCConfiguration config : configs) {
                // Find corresponding product
                Product product = findProductById(products, config.getProductId());
                if (product != null) {
                    addProductRow(productsTable, itemNumber++, product, config.getItemCount(), config.getPrice());
                    totalPrice += config.getPrice() * config.getItemCount();
                }
            }
        }
        
        // Add total row
        PdfPCell totalLabelCell = new PdfPCell(new Phrase("Gesamtsumme (inkl. MwSt.)", HEADER_FONT));
        totalLabelCell.setColspan(4);
        totalLabelCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        totalLabelCell.setBorder(Rectangle.TOP);
        productsTable.addCell(totalLabelCell);
        
        PdfPCell totalValueCell = new PdfPCell(new Phrase(String.format("%.2f €", totalPrice), HEADER_FONT));
        totalValueCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        totalValueCell.setBorder(Rectangle.TOP);
        productsTable.addCell(totalValueCell);
        
        document.add(productsTable);
        
        // Add payment info
        document.add(new Paragraph(" "));
        document.add(new Paragraph("Zahlungsinformationen:", HEADER_FONT));
        document.add(new Paragraph("Bitte überweisen Sie den fälligen Betrag innerhalb von 14 Tagen auf folgendes Konto:", NORMAL_FONT));
        document.add(new Paragraph("Bank: Sparkasse Stuttgart", NORMAL_FONT));
        document.add(new Paragraph("IBAN: DE12 3456 7890 1234 5678 90", NORMAL_FONT));
        document.add(new Paragraph("BIC: SPKRDE21XXX", NORMAL_FONT));
        document.add(new Paragraph("Verwendungszweck: Rechnungsnummer " + invoice.getInvoiceId(), NORMAL_FONT));
        
        // Add footer with thank you note
        document.add(new Paragraph(" "));
        document.add(new Paragraph(" "));
        document.add(new Paragraph("Vielen Dank für Ihren Einkauf bei HighSpeed Stuttgart!", NORMAL_FONT));
        document.add(new Paragraph("Bei Fragen zu Ihrer Rechnung kontaktieren Sie uns gerne unter info@highspeed-stuttgart.de", NORMAL_FONT));
        
        document.close();
    }
    
    private static void addTableHeader(PdfPTable table) {
        String[] headers = new String[] { "Nr.", "Artikel", "Anzahl", "Preis", "Gesamt" };
        
        for (String header : headers) {
            PdfPCell cell = new PdfPCell(new Phrase(header, SUBHEADER_FONT));
            cell.setBackgroundColor(BaseColor.LIGHT_GRAY);
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setPadding(5);
            table.addCell(cell);
        }
    }
    
    private static void addProductRow(PdfPTable table, int number, Product product, int quantity, double price) {
        PdfPCell cell;
        
        // Number column
        cell = new PdfPCell(new Phrase(String.valueOf(number), NORMAL_FONT));
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        table.addCell(cell);
          // Product name column
        String description = product.getProductName();
        if (product.getManufacturerName() != null && !product.getManufacturerName().isEmpty()) {
            description += "\n" + product.getManufacturerName();
        }
        if (product.getDetails() != null && !product.getDetails().isEmpty()) {
            description += "\n" + product.getDetails();
        }
        cell = new PdfPCell(new Phrase(description, NORMAL_FONT));
        cell.setHorizontalAlignment(Element.ALIGN_LEFT);
        table.addCell(cell);
        
        // Quantity column
        cell = new PdfPCell(new Phrase(String.valueOf(quantity), NORMAL_FONT));
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        table.addCell(cell);
        
        // Price column
        cell = new PdfPCell(new Phrase(String.format("%.2f €", price), NORMAL_FONT));
        cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        table.addCell(cell);
        
        // Total column
        double total = price * quantity;
        cell = new PdfPCell(new Phrase(String.format("%.2f €", total), NORMAL_FONT));
        cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        table.addCell(cell);
    }    private static Product findProductById(List<Product> products, int productId) {
        // Debug logging
        System.out.println("PDF Generator: Finding product with ID: " + productId + " from " + products.size() + " products");
        
        // If productId is within valid range, return the product at that index
        if (productId >= 0 && productId < products.size()) {
            return products.get(productId);
        }
        
        // If we can't use index-based lookup, try to find by matching attributes
        // This is a fallback mechanism
        for (Product product : products) {
            // If there's another way to match products with configurations, use it here
            if (products.indexOf(product) == productId) {
                return product;
            }
        }
        
        // If nothing found and productId is valid, just return first product as placeholder
        if (productId >= 0 && !products.isEmpty()) {
            System.out.println("PDF Generator: Using placeholder product for ID: " + productId);
            return products.get(0);
        }
        
        return null;
    }
    
    /**
     * Generate an invoice PDF for an order
     * 
     * @param orderId   The ID of the order to generate an invoice for
     * @param filePath  The path to save the PDF file
     * @return          True if the PDF was generated successfully, false otherwise
     */
    public static boolean generateInvoiceForOrder(int orderId, String filePath) {
        try {
            OrderDAO orderDAO = new OrderDAO();
            InvoiceDAO invoiceDAO = new InvoiceDAO();
            CustomerDAO customerDAO = new CustomerDAO();
            PCConfigurationDAO pcConfigurationDAO = new PCConfigurationDAO();
            ProductDAO productDAO = new ProductDAO();
            
            // Get order details
            Order order = orderDAO.getOrderById(orderId);
            if (order == null) {
                System.err.println("Order not found: " + orderId);
                return false;
            }
            
            // Get invoice details
            Invoice invoice = invoiceDAO.getInvoiceByOrderId(orderId);
            if (invoice == null) {
                System.err.println("Invoice not found for order: " + orderId);
                return false;
            }
            
            // Get customer details
            Customer customer = null;
            for (Customer c : customerDAO.getAllCustomers()) {
                if (c.getCustomerName().equals(order.getCustomerName())) {
                    customer = c;
                    break;
                }
            }
            if (customer == null) {
                System.err.println("Customer not found: " + order.getCustomerName());
                return false;
            }
              // Get PC configurations (products in order) - UPDATED TO USE order_products table
            List<PCConfiguration> configurations = pcConfigurationDAO.getConfigurationsByOrderId(orderId);
            
            // Load products directly from order_products table (the correct approach)
            List<Product> products = loadProductsFromOrderProductsTable(orderId);
            
            if (products == null || products.isEmpty()) {
                System.err.println("No products found in order_products table for order: " + orderId);
                // Fallback to old configuration method if order_products is empty
                if (configurations == null || configurations.isEmpty()) {
                    System.err.println("No configurations found either for order: " + orderId);
                    return false;
                }
                // Use old method as fallback
                products = productDAO.getAllProducts();
                if (products == null || products.isEmpty()) {
                    System.err.println("No products found in database");
                    return false;
                }
            } else {
                System.out.println("Successfully loaded " + products.size() + " products from order_products table");
                // If we have products from order_products, we don't need configurations
                configurations = new java.util.ArrayList<>();
            }
            
            // Generate PDF
            generateInvoicePdf(order, invoice, customer, configurations, products, filePath);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Load products for an order from the order_products table
     * @param orderId The order ID to load products for
     * @return List of products in the order with correct quantities
     */
    private static List<Product> loadProductsFromOrderProductsTable(int orderId) {
        List<Product> orderProducts = new java.util.ArrayList<>();
        ProductDAO productDAO = new ProductDAO();
        
        System.out.println("Loading products from order_products table for order: " + orderId);
        
        try (java.sql.Connection conn = clearorder.util.DatabaseConnection.getConnection()) {
            String sql = "SELECT product_id, quantity FROM order_products WHERE order_id = ? ORDER BY product_id";
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
                    System.out.println("Loaded for PDF: " + product.getProductName() + " (Qty: " + quantity + ")");
                } else {
                    System.out.println("WARNING: Product with ID " + productId + " not found in products table");
                }
            }
            
            rs.close();
            stmt.close();
        } catch (Exception e) {
            System.err.println("Error loading products from order_products table: " + e.getMessage());
            e.printStackTrace();
        }
        
        return orderProducts;
    }
}
