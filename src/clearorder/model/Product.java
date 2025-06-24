package clearorder.model;

public class Product {
    private String productId; // Changed from int to String to support composite IDs
    private String productType;
    private String productName;
    private String manufacturerName;
    private double price;
    private String availability;
    private int itemCount;
    private String details;
    private String socketType;

    public Product(String productId, String productType, String productName, String manufacturerName, double price, String availability,
            int itemCount, String details) {
        this.productId = productId;
        this.productType = productType;
        this.productName = productName;
        this.manufacturerName = manufacturerName;
        this.price = price;
        this.availability = availability;
        this.itemCount = itemCount;
        this.details = details;
        this.socketType = null; // Default to null
    }
    
    // Constructor with int productId for backward compatibility
    public Product(int productId, String productType, String productName, String manufacturerName, double price, String availability,
            int itemCount, String details) {
        this.productId = String.valueOf(productId);
        this.productType = productType;
        this.productName = productName;
        this.manufacturerName = manufacturerName;
        this.price = price;
        this.availability = availability;
        this.itemCount = itemCount;
        this.details = details;
        this.socketType = null; // Default to null
    }
    
    // Constructor without product ID for backward compatibility
    public Product(String productType, String productName, String manufacturerName, double price, String availability,
            int itemCount, String details) {
        this.productId = "0"; // Default ID
        this.productType = productType;
        this.productName = productName;
        this.manufacturerName = manufacturerName;
        this.price = price;
        this.availability = availability;
        this.itemCount = itemCount;
        this.details = details;
        this.socketType = null; // Default to null
    }
    
    // Default constructor
    public Product() {
        this.productId = "0";
        this.productType = "";
        this.productName = "";
        this.manufacturerName = "";
        this.price = 0.0;
        this.availability = "";
        this.itemCount = 0;
        this.details = "";
        this.socketType = null;
    }

    public String getProductId() {
        return productId;
    }
    
    public int getProductIdAsInt() {
        try {
            return Integer.parseInt(productId);
        } catch (NumberFormatException e) {
            // If it's a composite ID (like "1,2,3"), return the first one
            if (productId != null && productId.contains(",")) {
                String firstId = productId.split(",")[0];
                try {
                    return Integer.parseInt(firstId.trim());
                } catch (NumberFormatException ex) {
                    return 0;
                }
            }
            return 0;
        }
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }
    
    public void setProductId(int productId) {
        this.productId = String.valueOf(productId);
    }

    public String getProductType() {
        return productType;
    }

    public void setProductType(String productType) {
        this.productType = productType;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getManufacturerName() {
        return manufacturerName;
    }

    public void setManufacturerName(String manufacturerName) {
        this.manufacturerName = manufacturerName;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public String getAvailability() {
        return availability;
    }

    public void setAvailability(String availability) {
        this.availability = availability;
    }

    public int getItemCount() {
        return itemCount;
    }

    public void setItemCount(int itemCount) {
        this.itemCount = itemCount;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public String getSocketType() {
        return socketType;
    }

    public void setSocketType(String socketType) {
        this.socketType = socketType;
    }

    @Override
    public String toString() {
        // Show product type and name for better display in JList
        return productType + ": " + productName + " (" + manufacturerName + ") - $" + price + " [" + availability + "]";
    }
}