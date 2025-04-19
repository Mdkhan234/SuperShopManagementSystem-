package model;

import java.util.Date;
import java.util.List;
import java.util.ArrayList;
import java.text.SimpleDateFormat;

public class Purchase {
    private String purchaseId;
    private String customerMobile;
    private Date purchaseDate;
    private List<PurchaseItem> items;
    private float subtotal;
    private float discount;
    private float total;
    private String paymentMethod;
    private String status;

    public Purchase(String customerMobile, List<PurchaseItem> items, float subtotal,
                    float discount, String paymentMethod) {
        this.purchaseId = generatePurchaseId();
        this.customerMobile = customerMobile;
        this.purchaseDate = new Date();
        this.items = new ArrayList<>(items);
        this.subtotal = subtotal;
        this.discount = discount;
        this.total = subtotal - discount;
        this.paymentMethod = paymentMethod;
        this.status = "Completed";
    }

    private String generatePurchaseId() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
        return "PUR-" + sdf.format(new Date()) + "-" + (int)(Math.random() * 1000);
    }


    public String getPurchaseId() {
        return purchaseId;
    }

    public String getCustomerMobile() {
        return customerMobile;
    }

    public Date getPurchaseDate() {
        return purchaseDate;
    }

    public List<PurchaseItem> getItems() {
        return new ArrayList<>(items);
    }

    public float getSubtotal() {
        return subtotal;
    }

    public float getDiscount() {
        return discount;
    }

    public float getTotal() {
        return total;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public String getFormattedDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return sdf.format(purchaseDate);
    }

    public String toCSV() {
        StringBuilder sb = new StringBuilder();
        sb.append(purchaseId).append(",")
                .append(customerMobile).append(",")
                .append(getFormattedDate()).append(",")
                .append(subtotal).append(",")
                .append(discount).append(",")
                .append(total).append(",")
                .append(paymentMethod).append(",")
                .append(status);

        for (PurchaseItem item : items) {
            sb.append(";").append(item.toCSV());
        }

        return sb.toString();
    }

    public static Purchase fromCSV(String csv) {
        String[] parts = csv.split(",", 8);
        if (parts.length < 8) return null;

        try {
            String purchaseId = parts[0];
            String customerMobile = parts[1];
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date purchaseDate = sdf.parse(parts[2]);
            float subtotal = Float.parseFloat(parts[3]);
            float discount = Float.parseFloat(parts[4]);
            float total = Float.parseFloat(parts[5]);
            String paymentMethod = parts[6];
            String status = parts[7];

            List<PurchaseItem> items = new ArrayList<>();
            if (parts.length > 8) {
                String[] itemParts = parts[8].split(";");
                for (String itemStr : itemParts) {
                    if (!itemStr.isEmpty()) {
                        PurchaseItem item = PurchaseItem.fromCSV(itemStr);
                        if (item != null) items.add(item);
                    }
                }
            }

            Purchase purchase = new Purchase(customerMobile, items, subtotal, discount, paymentMethod);
            purchase.purchaseId = purchaseId;
            purchase.purchaseDate = purchaseDate;
            purchase.status = status;
            return purchase;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public String toString() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        StringBuilder sb = new StringBuilder();
        sb.append("Purchase ID: ").append(purchaseId).append("\n")
                .append("Date: ").append(sdf.format(purchaseDate)).append("\n")
                .append("Customer: ").append(customerMobile).append("\n")
                .append("Items:\n");

        for (PurchaseItem item : items) {
            sb.append("  - ").append(item.toString()).append("\n");
        }

        sb.append(String.format("Subtotal: $%.2f\n", subtotal))
                .append(String.format("Discount: $%.2f\n", discount))
                .append(String.format("Total: $%.2f\n", total))
                .append("Payment Method: ").append(paymentMethod).append("\n")
                .append("Status: ").append(status);

        return sb.toString();
    }
}