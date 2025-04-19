package model;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Product implements Serializable {
    private static final long serialVersionUID = 1L;
    private final int id;
    private int categoryId;
    private String name;
    private float price;
    private int quantity;
    private final Date dateAdded;
    private boolean inStock;

    public Product(int id, int categoryId, String name, float price, int quantity, Date dateAdded) {
        validateInputs(id, categoryId, name, price, quantity);

        this.id = id;
        this.categoryId = categoryId;
        this.name = name.trim();
        this.price = price;
        this.quantity = quantity;
        this.dateAdded = new Date(dateAdded.getTime());
        this.inStock = quantity > 0;
    }

    public Product(int id, int categoryId, String name, float price, int quantity) {
        this(id, categoryId, name, price, quantity, new Date());
    }

    private void validateInputs(int id, int categoryId, String name, float price, int quantity) {
        if (id <= 0) {
            throw new IllegalArgumentException("Product ID must be positive");
        }
        if (categoryId <= 0) {
            throw new IllegalArgumentException("Category ID must be positive");
        }
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Product name cannot be null or empty");
        }
        if (price <= 0) {
            throw new IllegalArgumentException("Price must be positive");
        }
        if (quantity < 0) {
            throw new IllegalArgumentException("Quantity cannot be negative");
        }
    }

    public int getId() {
        return id;
    }

    public int getProductId() {
        return id;
    }

    public int getCategoryId() {
        return categoryId;
    }

    public String getCategory() {
        return String.valueOf(categoryId);
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

    public int getStockQuantity() {
        return quantity; // Alias for getQuantity()
    }

    public Date getDateAdded() {
        return new Date(dateAdded.getTime());
    }

    public boolean isInStock() {
        return inStock;
    }

    public boolean isAvailable() {
        return inStock; // Alias for isInStock()
    }

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
        if (quantity < 0) {
            throw new IllegalArgumentException("Quantity cannot be negative");
        }
        this.quantity = quantity;
        this.inStock = quantity > 0;
    }

    public void setCategoryId(int categoryId) {
        if (categoryId <= 0) {
            throw new IllegalArgumentException("Category ID must be positive");
        }
        this.categoryId = categoryId;
    }

    public void increaseQuantity(int amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Increase amount must be positive");
        }
        this.quantity += amount;
        this.inStock = true;
    }

    public void decreaseQuantity(int amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Decrease amount must be positive");
        }
        if (this.quantity < amount) {
            throw new IllegalArgumentException("Insufficient quantity available");
        }
        this.quantity -= amount;
        this.inStock = this.quantity > 0;
    }

    public String toCSV() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        return String.join(",",
                String.valueOf(id),
                String.valueOf(categoryId),
                escapeSpecialCharacters(name),
                String.format("%.2f", price),
                String.valueOf(quantity),
                sdf.format(dateAdded)
        );
    }

    public static Product fromCSV(String csvLine) {
        try {
            String[] parts = csvLine.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", -1);
            if (parts.length != 6) {
                throw new IllegalArgumentException("Invalid CSV format for Product");
            }

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            return new Product(
                    Integer.parseInt(parts[0]),
                    Integer.parseInt(parts[1]),
                    unescapeSpecialCharacters(parts[2]),
                    Float.parseFloat(parts[3]),
                    Integer.parseInt(parts[4]),
                    sdf.parse(parts[5])
            );
        } catch (Exception e) {
            throw new IllegalArgumentException("Error parsing Product from CSV: " + e.getMessage(), e);
        }
    }

    private static String escapeSpecialCharacters(String data) {
        String escapedData = data.replace("\\", "\\\\");
        escapedData = escapedData.replace("\"", "\\\"");
        escapedData = escapedData.replace(",", "\\,");
        return "\"" + escapedData + "\"";
    }

    private static String unescapeSpecialCharacters(String data) {
        if (data.startsWith("\"") && data.endsWith("\"")) {
            data = data.substring(1, data.length() - 1);
        }
        return data.replace("\\,", ",")
                .replace("\\\"", "\"")
                .replace("\\\\", "\\");
    }

    @Override
    public String toString() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        return String.format("Product [ID: %d, Category: %d, Name: %s, Price: %s, Qty: %d, Added: %s, Status: %s]",
                id,
                categoryId,
                name,
                getFormattedPrice(),
                quantity,
                sdf.format(dateAdded),
                inStock ? "In Stock" : "Out of Stock");
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Product product = (Product) obj;
        return id == product.id;
    }

    @Override
    public int hashCode() {
        return id;
    }

    public boolean isLowStock(int threshold) {
        if (threshold < 0) {
            throw new IllegalArgumentException("Stock threshold cannot be negative");
        }
        return inStock && quantity <= threshold;
    }

    public String getFormattedPrice() {
        return String.format("$%.2f", price);
    }

    public String getFormattedDateAdded() {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(dateAdded);
    }
}