package model;

import java.io.Serializable;

public class CartItem implements Serializable {
    private static final long serialVersionUID = 1L;  // Added for serialization

    private final int productId;  // Made final since ID shouldn't change
    private String name;
    private float price;
    private int quantity;

    public CartItem(int productId, String name, float price, int quantity) {
        if (productId <= 0) {
            throw new IllegalArgumentException("Product ID must be positive");
        }
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Product name cannot be null or empty");
        }
        if (price <= 0) {
            throw new IllegalArgumentException("Price must be positive");
        }
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }

        this.productId = productId;
        this.name = name.trim();
        this.price = price;
        this.quantity = quantity;
    }

    // Getters
    public int getProductId() {
        return productId;
    }

    public String getName() {
        return name;
    }

    public float getPrice() {
        return price;
    }

    public int getQuantity() {
        return quantity;
    }

    // Setters with validation
    public void setName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Product name cannot be null or empty");
        }
        this.name = name.trim();
    }

    public void setPrice(float price) {
        if (price <= 0) {
            throw new IllegalArgumentException("Price must be positive");
        }
        this.price = price;
    }

    public void setQuantity(int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }
        this.quantity = quantity;
    }

    // CSV Conversion
    public String toCSV() {
        return String.format("%d,%s,%.2f,%d",
                productId,
                name.replace(",", "\\,"),  // Escape commas in name
                price,
                quantity);
    }

    public static CartItem fromCSV(String csvLine) {
        try {
            // Handle escaped commas in name
            String[] parts = csvLine.split("(?<!\\\\),");
            if (parts.length != 4) {
                throw new IllegalArgumentException("Invalid CSV format for CartItem");
            }

            int productId = Integer.parseInt(parts[0]);
            String name = parts[1].replace("\\,", ",");  // Unescape commas
            float price = Float.parseFloat(parts[2]);
            int quantity = Integer.parseInt(parts[3]);

            return new CartItem(productId, name, price, quantity);
        } catch (Exception e) {
            System.err.println("Error parsing CartItem from CSV: " + e.getMessage());
            return null;
        }
    }

    // Business logic methods
    public float getSubtotal() {
        return price * quantity;
    }

    public void increaseQuantity(int amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Amount must be positive");
        }
        this.quantity += amount;
    }

    public void decreaseQuantity(int amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Amount must be positive");
        }
        if (this.quantity < amount) {
            throw new IllegalArgumentException("Insufficient quantity to decrease");
        }
        this.quantity -= amount;
    }

    @Override
    public String toString() {
        return String.format("CartItem[ID: %d, Name: %s, Price: %.2f, Qty: %d, Subtotal: %.2f]",
                productId, name, price, quantity, getSubtotal());
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        CartItem cartItem = (CartItem) obj;
        return productId == cartItem.productId;
    }

    @Override
    public int hashCode() {
        return productId;
    }
}