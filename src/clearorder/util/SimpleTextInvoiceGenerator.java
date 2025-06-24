package clearorder.util;

import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.List;

import clearorder.model.Customer;
import clearorder.model.Invoice;
import clearorder.model.Order;
import clearorder.model.Product;

/**
 * Simple text-based invoice generator that works without external dependencies
 */
public class SimpleTextInvoiceGenerator {

    /**
     * Generate a text-based invoice for an order with the specified products
     * 
     * @param order       The order information
     * @param invoice     The invoice information
     * @param customer    The customer information
     * @param products    The list of ordered products
     * @param filePath    The path where to save the text file (will be changed to .txt)
     * @return true if the invoice was generated successfully, false otherwise
     */
    public static boolean generateInvoice(Order order, Invoice invoice, Customer customer, 
                                         List<Product> products, String filePath) {
        
        // Change file extension to .txt
        if (filePath.toLowerCase().endsWith(".pdf")) {
            filePath = filePath.substring(0, filePath.length() - 4) + ".txt";
        }
        
        try (FileWriter writer = new FileWriter(filePath)) {
            // Header
            writer.write("=".repeat(60) + "\n");
            writer.write("                    RECHNUNG\n");
            writer.write("=".repeat(60) + "\n\n");
            
            // Invoice details
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");
            writer.write("Rechnungsnummer: " + invoice.getInvoiceId() + "\n");
            writer.write("Bestellnummer: " + order.getOrderId() + "\n");
            writer.write("Datum: " + dateFormat.format(invoice.getInvoiceDate()) + "\n");
            writer.write("Status: " + order.getStatus() + "\n\n");
            
            // Company info
            writer.write("HighSpeed Stuttgart\n");
            writer.write("Königstraße 45\n");
            writer.write("70173 Stuttgart\n");
            writer.write("Tel: +49 711 123456\n");
            writer.write("Email: info@highspeed-stuttgart.de\n");
            writer.write("USt-IdNr: DE123456789\n\n");
            
            // Customer info
            writer.write("Rechnungsempfänger:\n");
            writer.write(customer.getCustomerName() + "\n");
            if (customer.getAddress() != null && !customer.getAddress().isEmpty()) {
                writer.write(customer.getAddress() + "\n");
            }
            writer.write("Tel: " + customer.getPhone() + "\n");
            writer.write("Email: " + customer.getEmail() + "\n\n");
            
            // Products header
            writer.write("Bestellte Artikel:\n");
            writer.write("-".repeat(60) + "\n");
            writer.write(String.format("%-3s %-30s %-4s %-10s %-10s\n", 
                        "Nr.", "Artikel", "Anz", "Preis", "Gesamt"));
            writer.write("-".repeat(60) + "\n");
            
            // Products
            double total = 0;
            for (int i = 0; i < products.size(); i++) {
                Product product = products.get(i);
                double itemTotal = product.getPrice() * product.getItemCount();
                total += itemTotal;
                
                writer.write(String.format("%-3d %-30s %-4d %8.2f € %8.2f €\n", 
                           i + 1,
                           limitText(product.getProductName(), 30),
                           product.getItemCount(),
                           product.getPrice(),
                           itemTotal));
                
                // Add manufacturer and details if available
                if (product.getManufacturerName() != null && !product.getManufacturerName().isEmpty()) {
                    writer.write("    " + product.getManufacturerName() + "\n");
                }
                if (product.getDetails() != null && !product.getDetails().isEmpty()) {
                    writer.write("    " + limitText(product.getDetails(), 55) + "\n");
                }
                writer.write("\n");
            }
            
            writer.write("-".repeat(60) + "\n");
            writer.write(String.format("%50s %8.2f €\n", "Gesamtsumme (inkl. MwSt.):", total));
            writer.write("=".repeat(60) + "\n\n");
            
            // Payment info
            writer.write("Zahlungsinformationen:\n");
            writer.write("Bitte überweisen Sie den fälligen Betrag innerhalb von 14 Tagen auf folgendes Konto:\n\n");
            writer.write("Bank: Sparkasse Stuttgart\n");
            writer.write("IBAN: DE12 3456 7890 1234 5678 90\n");
            writer.write("BIC: SPKRDE21XXX\n");
            writer.write("Verwendungszweck: Rechnungsnummer " + invoice.getInvoiceId() + "\n\n");
            
            // Footer
            writer.write("Vielen Dank für Ihren Einkauf bei HighSpeed Stuttgart!\n");
            writer.write("Bei Fragen zu Ihrer Rechnung kontaktieren Sie uns gerne unter info@highspeed-stuttgart.de\n");
            
            System.out.println("Text invoice generated successfully: " + filePath);
            return true;
            
        } catch (IOException e) {
            System.err.println("Error generating text invoice: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    private static String limitText(String text, int maxLength) {
        if (text == null) return "";
        if (text.length() <= maxLength) return text;
        return text.substring(0, maxLength - 3) + "...";
    }
}
