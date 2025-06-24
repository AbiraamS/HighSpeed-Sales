package clearorder.util;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.List;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;

import clearorder.model.Customer;
import clearorder.model.Invoice;
import clearorder.model.Order;
import clearorder.model.Product;

/**
 * Simple PDF generator using Apache PDFBox
 */
public class SimplePdfGenerator {

    /**
     * Generate a PDF invoice for an order with the specified products
     * 
     * @param order       The order information
     * @param invoice     The invoice information
     * @param customer    The customer information
     * @param products    The list of ordered products
     * @param filePath    The path where to save the PDF file
     * @return true if the PDF was generated successfully, false otherwise
     */
    public static boolean generateInvoice(Order order, Invoice invoice, Customer customer, 
                                         List<Product> products, String filePath) {
        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage();
            document.addPage(page);
            
            try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {                // Set up fonts
                PDType1Font titleFont = PDType1Font.HELVETICA_BOLD;
                PDType1Font headerFont = PDType1Font.HELVETICA_BOLD;
                PDType1Font normalFont = PDType1Font.HELVETICA;
                
                // Title
                contentStream.beginText();
                contentStream.setFont(titleFont, 18);
                contentStream.newLineAtOffset(250, 750);
                contentStream.showText("RECHNUNG");
                contentStream.endText();
                
                // Invoice details
                SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");
                drawText(contentStream, "Rechnungsnummer: " + invoice.getInvoiceId(), headerFont, 12, 50, 700);
                drawText(contentStream, "Bestellnummer: " + order.getOrderId(), headerFont, 12, 50, 680);
                drawText(contentStream, "Datum: " + dateFormat.format(invoice.getInvoiceDate()), headerFont, 12, 50, 660);
                drawText(contentStream, "Status: " + order.getStatus(), headerFont, 12, 50, 640);
                
                // Company info
                drawText(contentStream, "HighSpeed Stuttgart", headerFont, 12, 50, 600);
                drawText(contentStream, "Königstraße 45", normalFont, 10, 50, 585);
                drawText(contentStream, "70173 Stuttgart", normalFont, 10, 50, 570);
                drawText(contentStream, "Tel: +49 711 123456", normalFont, 10, 50, 555);
                drawText(contentStream, "Email: info@highspeed-stuttgart.de", normalFont, 10, 50, 540);
                
                // Customer info
                drawText(contentStream, "Rechnungsempfänger:", headerFont, 12, 300, 600);
                drawText(contentStream, customer.getCustomerName(), normalFont, 10, 300, 585);
                drawText(contentStream, customer.getAddress(), normalFont, 10, 300, 570);
                drawText(contentStream, "Tel: " + customer.getPhone(), normalFont, 10, 300, 555);
                drawText(contentStream, "Email: " + customer.getEmail(), normalFont, 10, 300, 540);
                
                // Products header
                drawText(contentStream, "Bestellte Artikel:", headerFont, 12, 50, 500);
                
                // Table headers
                contentStream.setFont(headerFont, 10);
                contentStream.beginText();
                contentStream.newLineAtOffset(50, 480);
                contentStream.showText("Artikelname");
                contentStream.endText();
                
                contentStream.beginText();
                contentStream.newLineAtOffset(250, 480);
                contentStream.showText("Hersteller");
                contentStream.endText();
                
                contentStream.beginText();
                contentStream.newLineAtOffset(350, 480);
                contentStream.showText("Spezifikationen");
                contentStream.endText();
                
                contentStream.beginText();
                contentStream.newLineAtOffset(500, 480);
                contentStream.showText("Preis");
                contentStream.endText();
                
                // Table content
                int y = 460;
                double total = 0;
                
                for (int i = 0; i < products.size(); i++) {
                    Product product = products.get(i);
                    double price = product.getPrice();
                    
                    contentStream.setFont(normalFont, 9);
                    
                    contentStream.beginText();
                    contentStream.newLineAtOffset(50, y);
                    contentStream.showText(limitText(product.getProductName(), 25));
                    contentStream.endText();
                    
                    contentStream.beginText();
                    contentStream.newLineAtOffset(250, y);
                    contentStream.showText(limitText(product.getManufacturerName(), 15));
                    contentStream.endText();
                    
                    contentStream.beginText();
                    contentStream.newLineAtOffset(350, y);
                    contentStream.showText(limitText(product.getDetails(), 20));
                    contentStream.endText();
                    
                    contentStream.beginText();
                    contentStream.newLineAtOffset(500, y);
                    contentStream.showText(String.format("%.2f €", price));
                    contentStream.endText();
                    
                    y -= 15;
                    total += price;
                    
                    // Prevent overflowing the page
                    if (y < 100) break;
                }
                
                // Total
                contentStream.setFont(headerFont, 11);
                contentStream.beginText();
                contentStream.newLineAtOffset(400, y - 20);
                contentStream.showText("Gesamtsumme: " + String.format("%.2f €", total));
                contentStream.endText();
                
                // Payment info
                y -= 60;
                drawText(contentStream, "Zahlungsinformationen:", headerFont, 11, 50, y);
                drawText(contentStream, "Bitte überweisen Sie den fälligen Betrag innerhalb von 14 Tagen auf folgendes Konto:", normalFont, 9, 50, y - 15);
                drawText(contentStream, "Bank: Sparkasse Stuttgart", normalFont, 9, 50, y - 30);
                drawText(contentStream, "IBAN: DE12 3456 7890 1234 5678 90", normalFont, 9, 50, y - 45);
                drawText(contentStream, "BIC: SPKRDE21XXX", normalFont, 9, 50, y - 60);
                drawText(contentStream, "Verwendungszweck: Rechnungsnummer " + invoice.getInvoiceId(), normalFont, 9, 50, y - 75);
                
                // Footer
                drawText(contentStream, "Vielen Dank für Ihren Einkauf bei HighSpeed Stuttgart!", normalFont, 10, 50, 100);
            }
            
            document.save(filePath);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    private static void drawText(PDPageContentStream contentStream, String text, PDType1Font font, int fontSize, 
                                float x, float y) throws IOException {
        contentStream.beginText();
        contentStream.setFont(font, fontSize);
        contentStream.newLineAtOffset(x, y);
        contentStream.showText(text);
        contentStream.endText();
    }
    
    private static String limitText(String text, int maxLength) {
        if (text == null) return "";
        return text.length() > maxLength ? text.substring(0, maxLength - 3) + "..." : text;
    }
}
