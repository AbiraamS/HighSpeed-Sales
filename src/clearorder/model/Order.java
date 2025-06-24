package clearorder.model;

import java.sql.Date;

public class Order {
    private int orderId;
    private String orderNumber;
    private String customerName;
    private Date orderDate;
    private String status;
    private long invoiceId;

    public Order() {
        // Default constructor
    }

    public Order(int orderId, String orderNumber, String customerName, Date orderDate, String status, long invoiceId) {
        this.orderId = orderId;
        this.orderNumber = orderNumber;
        this.customerName = customerName;
        this.orderDate = orderDate;
        this.status = status;
        this.invoiceId = invoiceId;
    }

    public int getOrderId() {
        return orderId;
    }

    public void setOrderId(int orderId) {
        this.orderId = orderId;
    }

    public String getOrderNumber() {
        return orderNumber;
    }

    public void setOrderNumber(String orderNumber) {
        this.orderNumber = orderNumber;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public Date getOrderDate() {
        return orderDate;
    }

    public void setOrderDate(Date orderDate) {
        this.orderDate = orderDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }    public long getInvoiceId() {
        return invoiceId;
    }
    
    public void setInvoiceId(long invoiceId) {
        this.invoiceId = invoiceId;
    }
}