package model;

public class PurchaseItem {
    private int productId;
    private String productName;
    private float unitPrice;
    private int quantity;
    private float itemTotal;

    public PurchaseItem(int productId, String productName, float unitPrice, int quantity) {
        this.productId = productId;
        this.productName = productName;
        this.unitPrice = unitPrice;
        this.quantity = quantity;
        this.itemTotal = unitPrice * quantity;
    }

    public int getProductId() {
        return productId;
    }

    public String getProductName() {
        return productName;
    }

    public float getUnitPrice() {
        return unitPrice;
    }

    public int getQuantity() {
        return quantity;
    }

    public float getItemTotal() {
        return itemTotal;
    }

    public String toCSV() {
        return String.join(",",
                String.valueOf(productId),
                productName,
                String.valueOf(unitPrice),
                String.valueOf(quantity),
                String.valueOf(itemTotal));
    }

    public static PurchaseItem fromCSV(String csv) {
        String[] parts = csv.split(",");
        if (parts.length != 5) {
            return null;
        }

        try {
            int productId = Integer.parseInt(parts[0]);
            String productName = parts[1];
            float unitPrice = Float.parseFloat(parts[2]);
            int quantity = Integer.parseInt(parts[3]);
            return new PurchaseItem(productId, productName, unitPrice, quantity);
        } catch (NumberFormatException e) {
            System.err.println("Error parsing PurchaseItem from CSV: " + e.getMessage());
            return null;
        }
    }

    @Override
    public String toString() {
        return String.format("%s (ID: %d) - %d x $%.2f = $%.2f",
                productName,
                productId,
                quantity,
                unitPrice,
                itemTotal);
    }

    public static class Builder {
        private int productId;
        private String productName;
        private float unitPrice;
        private int quantity;

        public Builder productId(int productId) {
            this.productId = productId;
            return this;
        }

        public Builder productName(String productName) {
            this.productName = productName;
            return this;
        }

        public Builder unitPrice(float unitPrice) {
            this.unitPrice = unitPrice;
            return this;
        }

        public Builder quantity(int quantity) {
            this.quantity = quantity;
            return this;
        }

        public PurchaseItem build() {
            return new PurchaseItem(productId, productName, unitPrice, quantity);
        }
    }
}