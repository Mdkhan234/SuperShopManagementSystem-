package service;

import model.Product;
import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

public class ProductService {
    private static final String PRODUCTS_FILE = "data/products.dat";
    private static final String CATEGORIES_FILE = "data/categories.dat";
    private static final int DEFAULT_CATEGORY_ID = 1;

    private List<Product> products;
    private Map<Integer, String> categories;

    public ProductService() {
        this.products = new ArrayList<>();
        this.categories = new HashMap<>();
        initializeData();
    }

    // ========== Initialization Methods ==========
    private void initializeData() {
        createDataDirectory();
        loadCategories();

        if (categories.isEmpty()) {
            createDefaultCategories();
        }

        loadProducts();
    }

    private void createDataDirectory() {
        File dataDir = new File("data");
        if (!dataDir.exists() && !dataDir.mkdirs()) {
            System.err.println("❌ Failed to create data directory");
        }
    }

    // ========== Category Management ==========
    @SuppressWarnings("unchecked")
    private void loadCategories() {
        File file = new File(CATEGORIES_FILE);
        if (!file.exists()) return;

        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
            categories = (Map<Integer, String>) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("❌ Error loading categories: " + e.getMessage());
        }
    }

    private void createDefaultCategories() {
        categories.putAll(Map.of(
                1, "Electronics",
                2, "Clothing",
                3, "Groceries",
                4, "Home Appliances"
        ));
        saveCategories();
    }

    public boolean addCategory(int id, String name) {
        if (id <= 0 || name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Invalid category data");
        }

        if (categories.containsKey(id)) {
            System.err.println("❌ Category ID already exists");
            return false;
        }
        categories.put(id, name.trim());
        return saveCategories();
    }

    private boolean saveCategories() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(CATEGORIES_FILE))) {
            oos.writeObject(categories);
            return true;
        } catch (IOException e) {
            System.err.println("❌ Error saving categories: " + e.getMessage());
            return false;
        }
    }

    // ========== Product CRUD Operations ==========
    public boolean insertProduct(Product product) {
        if (product == null) {
            throw new IllegalArgumentException("Product cannot be null");
        }

        if (!categories.containsKey(product.getCategoryId())) {
            product.setCategoryId(DEFAULT_CATEGORY_ID);
        }

        if (productExists(product.getId())) {
            System.err.println("❌ Product ID already exists");
            return false;
        }

        products.add(product);
        return saveProducts();
    }

    public boolean insertProduct(int categoryId, String name, float price, int quantity) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Product name cannot be empty");
        }
        if (price <= 0) {
            throw new IllegalArgumentException("Price must be positive");
        }
        if (quantity < 0) {
            throw new IllegalArgumentException("Quantity cannot be negative");
        }

        if (!categories.containsKey(categoryId)) {
            System.err.println("❌ Invalid category ID. Using default category.");
            categoryId = DEFAULT_CATEGORY_ID;
        }

        int newId = generateProductId();
        return insertProduct(new Product(newId, categoryId, name.trim(), price, quantity));
    }

    private int generateProductId() {
        return products.stream()
                .mapToInt(Product::getId)
                .max()
                .orElse(0) + 1;
    }

    public boolean updateProduct(Product updatedProduct) {
        if (updatedProduct == null) return false;

        for (int i = 0; i < products.size(); i++) {
            if (products.get(i).getId() == updatedProduct.getId()) {
                products.set(i, updatedProduct);
                return saveProducts();
            }
        }
        return false;
    }

    public boolean updateProductQuantity(int productId, int quantityChange) {
        Product product = getProductById(productId);
        if (product == null) return false;

        int newQuantity = product.getQuantity() + quantityChange;
        if (newQuantity < 0) {
            throw new IllegalArgumentException("Cannot reduce quantity below zero");
        }

        product.setQuantity(newQuantity);
        return saveProducts();
    }

    public boolean updateProductPrice(int productId, float newPrice) {
        if (newPrice <= 0) {
            throw new IllegalArgumentException("Price must be positive");
        }

        Product product = getProductById(productId);
        if (product == null) return false;

        product.setPrice(newPrice);
        return saveProducts();
    }

    public boolean deleteProduct(int productId) {
        boolean removed = products.removeIf(p -> p.getId() == productId);
        if (removed) {
            return saveProducts();
        }
        return false;
    }

    // ========== Product Search Methods ==========
    public Product getProductById(int id) {
        return products.stream()
                .filter(p -> p.getId() == id)
                .findFirst()
                .orElse(null);
    }

    public Product getProductByName(String name) {
        if (name == null || name.trim().isEmpty()) return null;

        return products.stream()
                .filter(p -> p.getName().equalsIgnoreCase(name.trim()))
                .findFirst()
                .orElse(null);
    }

    public List<Product> getProductsByCategory(int categoryId) {
        return products.stream()
                .filter(p -> p.getCategoryId() == categoryId)
                .collect(Collectors.toList());
    }

    public List<Product> getProductsByPriceRange(float minPrice, float maxPrice) {
        if (minPrice < 0 || maxPrice < 0 || maxPrice < minPrice) {
            throw new IllegalArgumentException("Invalid price range");
        }

        return products.stream()
                .filter(p -> p.getPrice() >= minPrice && p.getPrice() <= maxPrice)
                .collect(Collectors.toList());
    }

    public List<Product> getLowStockProducts(int threshold) {
        if (threshold < 0) {
            throw new IllegalArgumentException("Threshold cannot be negative");
        }

        return products.stream()
                .filter(p -> p.getQuantity() <= threshold)
                .collect(Collectors.toList());
    }

    // ========== Inventory Management ==========
    public void displayLowStockWarning(int threshold) {
        List<Product> lowStockItems = getLowStockProducts(threshold);

        if (lowStockItems.isEmpty()) {
            System.out.println("✅ No products below stock threshold");
            return;
        }

        System.out.println("\n⚠️ LOW STOCK ITEMS (Threshold: " + threshold + ")");
        System.out.println("----------------------------------------");
        System.out.printf("%-5s %-20s %-10s %-5s\n", "ID", "Name", "Price", "Qty");

        lowStockItems.forEach(p ->
                System.out.printf("%-5d %-20s %-10.2f %-5d\n",
                        p.getId(), p.getName(), p.getPrice(), p.getQuantity())
        );
    }

    // ========== Display Methods ==========
    public void displayAllProducts() {
        if (products.isEmpty()) {
            System.out.println("❌ No products available");
            return;
        }

        System.out.println("\n🛍️ ALL PRODUCTS");
        System.out.println("----------------------------------------------------------------");
        System.out.printf("%-5s %-20s %-15s %-10s %-8s %-10s\n",
                "ID", "Name", "Category", "Price", "Qty", "Status");

        products.forEach(p ->
                System.out.printf("%-5d %-20s %-15s %-10.2f %-8d %-10s\n",
                        p.getId(),
                        p.getName(),
                        getCategoryName(p.getCategoryId()),
                        p.getPrice(),
                        p.getQuantity(),
                        p.isInStock() ? "In Stock" : "Out of Stock")
        );
    }

    public void displayCategories() {
        System.out.println("\n📦 PRODUCT CATEGORIES");
        System.out.println("----------------------");
        categories.forEach((id, name) ->
                System.out.printf("%d. %s\n", id, name));
    }

    // ========== Data Persistence ==========
    @SuppressWarnings("unchecked")
    private void loadProducts() {
        File file = new File(PRODUCTS_FILE);
        if (!file.exists()) return;

        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
            products = (List<Product>) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("❌ Error loading products: " + e.getMessage());
            products = new ArrayList<>();
        }
    }

    private synchronized boolean saveProducts() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(PRODUCTS_FILE))) {
            oos.writeObject(products);
            return true;
        } catch (IOException e) {
            System.err.println("❌ Error saving products: " + e.getMessage());
            return false;
        }
    }

    // ========== Helper Methods ==========
    private boolean productExists(int productId) {
        return products.stream().anyMatch(p -> p.getId() == productId);
    }

    // ========== Getters ==========
    public List<Product> getAllProducts() {
        return new ArrayList<>(products);
    }

    public String getCategoryName(int categoryId) {
        return categories.getOrDefault(categoryId, "Uncategorized");
    }

    public Map<Integer, String> getAllCategories() {
        return new HashMap<>(categories);
    }
}