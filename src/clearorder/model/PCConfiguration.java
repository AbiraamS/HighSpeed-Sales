package clearorder.model;

import java.util.Date;

public class PCConfiguration {
    private int configurationId;
    private int orderId;
    private int productId;
    private int itemCount;
    private Date estimatedDeliveryDate;
    private String compatibility;
    private double price;

    public PCConfiguration(int configurationId, int orderId, int productId, int itemCount, 
                         Date estimatedDeliveryDate, String compatibility, double price) {
        this.configurationId = configurationId;
        this.orderId = orderId;
        this.productId = productId;
        this.itemCount = itemCount;
        this.estimatedDeliveryDate = estimatedDeliveryDate;
        this.compatibility = compatibility;
        this.price = price;
    }

    // Getters and Setters
    public int getConfigurationId() { return configurationId; }
    public void setConfigurationId(int configurationId) { this.configurationId = configurationId; }
    
    public int getOrderId() { return orderId; }
    public void setOrderId(int orderId) { this.orderId = orderId; }
    
    public int getProductId() { return productId; }
    public void setProductId(int productId) { this.productId = productId; }
    
    public int getItemCount() { return itemCount; }
    public void setItemCount(int itemCount) { this.itemCount = itemCount; }
    
    public Date getEstimatedDeliveryDate() { return estimatedDeliveryDate; }
    public void setEstimatedDeliveryDate(Date estimatedDeliveryDate) { this.estimatedDeliveryDate = estimatedDeliveryDate; }
    
    public String getCompatibility() { return compatibility; }
    public void setCompatibility(String compatibility) { this.compatibility = compatibility; }
    
    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }
} 