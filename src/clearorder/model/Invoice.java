package clearorder.model;

import java.sql.Date;

public class Invoice {    private long invoiceId;
    private int orderId;
    private double totalPrice;
    private Date invoiceDate;
    private String status;

    public Invoice() {
        // Default constructor
    }    public Invoice(long invoiceId, int orderId, double totalPrice, Date invoiceDate, String status) {
        this.invoiceId = invoiceId;
        this.orderId = orderId;
        this.totalPrice = totalPrice;
        this.invoiceDate = invoiceDate;
        this.status = status;
    }    public long getInvoiceId() {
        return invoiceId;
    }

    public void setInvoiceId(long invoiceId) {
        this.invoiceId = invoiceId;
    }

    public int getOrderId() {
        return orderId;
    }

    public void setOrderId(int orderId) {
        this.orderId = orderId;
    }

    public double getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(double totalPrice) {
        this.totalPrice = totalPrice;
    }

    public Date getInvoiceDate() {
        return invoiceDate;
    }

    public void setInvoiceDate(Date invoiceDate) {
        this.invoiceDate = invoiceDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}