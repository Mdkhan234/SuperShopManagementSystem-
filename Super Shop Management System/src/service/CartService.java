package service;

import model.CartItem;
import model.Product;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

public class CartService {
    private static final String CART_FILE = "data/cart.dat";
    private static final String TRANSACTION_DIR = "data/transactions";
    private static final float VIP_DISCOUNT = 0.1f; // 10% discount for VIP customers

    private final ProductService productService;
    private List<CartItem> cartItems;
    private float discountRate = 0.0f;

    public CartService(ProductService productService) {
        this.productService = Objects.requireNonNull(productService, "ProductService cannot be null");
        this.cartItems = new ArrayList<>();
        ensureDataDirectoryExists();
        loadCartItems();
    }

    public synchronized void addToCart(int productId, int quantity) {
        try {
            validateQuantity(quantity);
            Product product = getValidProduct(productId);
            validateStock(product, quantity);

            Optional<CartItem> existingItem = findCartItem(productId);

            if (existingItem.isPresent()) {
                updateExistingItem(existingItem.get(), product, quantity);
            } else {
                addNewCartItem(product, quantity);
            }
        } catch (IllegalArgumentException | IllegalStateException e) {
            System.err.println("❌ Error: " + e.getMessage());
        }
    }

    public synchronized void updateCartItem(int productId, int newQuantity) {
        try {
            validateQuantity(newQuantity);
            Product product = getValidProduct(productId);

            if (newQuantity == 0) {
                removeCartItem(productId);
                return;
            }

            validateStock(product, newQuantity);

            findCartItem(productId).ifPresentOrElse(
                    item -> updateItemQuantity(item, newQuantity, product),
                    () -> System.out.println("❌ Product not found in cart.")
            );
        } catch (IllegalArgumentException | IllegalStateException e) {
            System.err.println("❌ Error: " + e.getMessage());
        }
    }

    public synchronized void removeCartItem(int productId) {
        boolean removed = cartItems.removeIf(item -> item.getProductId() == productId);
        if (removed) {
            if (saveCartItems()) {
                System.out.println("✅ Product removed from cart.");
            } else {
                System.out.println("❌ Failed to save cart changes.");
            }
        } else {
            System.out.println("❌ Product not found in cart.");
        }
    }

    public synchronized boolean checkout(boolean isVipCustomer) {
        if (cartItems.isEmpty()) {
            System.out.println("❌ Your cart is empty.");
            return false;
        }

        if (!validateCartItemsStock()) {
            return false;
        }

        this.discountRate = isVipCustomer ? VIP_DISCOUNT : 0.0f;

        if (!updateInventory()) {
            System.out.println("❌ Failed to update inventory.");
            return false;
        }

        String receipt = generateReceipt(isVipCustomer);
        saveTransaction(receipt);
        clearCart();

        System.out.println(receipt);
        return true;
    }

    public synchronized void viewCart() {
        if (cartItems.isEmpty()) {
            System.out.println("\n🛒 Your cart is empty.");
            return;
        }

        printCartDetails();
    }

    private Optional<CartItem> findCartItem(int productId) {
        return cartItems.stream()
                .filter(item -> item.getProductId() == productId)
                .findFirst();
    }

    private Product getValidProduct(int productId) {
        Product product = productService.getProductById(productId);
        if (product == null) {
            throw new IllegalArgumentException("Product with ID " + productId + " not found");
        }
        return product;
    }

    private void validateQuantity(int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }
    }

    private void validateStock(Product product, int quantity) {
        if (quantity > product.getQuantity()) {
            throw new IllegalStateException("Not enough stock for " + product.getName() +
                    ". Available: " + product.getQuantity());
        }
    }

    private void updateExistingItem(CartItem item, Product product, int additionalQuantity) {
        int newQuantity = item.getQuantity() + additionalQuantity;
        if (newQuantity > product.getQuantity()) {
            System.out.println("❌ Cannot add more than available stock.");
            return;
        }
        item.setQuantity(newQuantity);
        if (saveCartItems()) {
            System.out.println("✅ Updated in cart: " + product.getName() + " (Qty: " + newQuantity + ")");
        }
    }

    private void addNewCartItem(Product product, int quantity) {
        cartItems.add(new CartItem(
                product.getId(),
                product.getName(),
                product.getPrice(),
                quantity
        ));
        if (saveCartItems()) {
            System.out.println("✅ Added to cart: " + product.getName() + " (Qty: " + quantity + ")");
        }
    }

    private void updateItemQuantity(CartItem item, int newQuantity, Product product) {
        item.setQuantity(newQuantity);
        if (saveCartItems()) {
            System.out.println("✅ Updated: " + product.getName() + " (New Qty: " + newQuantity + ")");
        }
    }

    private boolean validateCartItemsStock() {
        for (CartItem item : cartItems) {
            Product product = productService.getProductById(item.getProductId());
            if (product == null || product.getQuantity() < item.getQuantity()) {
                System.out.println("❌ " + item.getName() + " is no longer available in sufficient quantity.");
                return false;
            }
        }
        return true;
    }

    private boolean updateInventory() {
        for (CartItem item : cartItems) {
            if (!productService.updateProductQuantity(item.getProductId(), -item.getQuantity())) {
                System.out.println("❌ Failed to update inventory for: " + item.getName());
                return false;
            }
        }
        return true;
    }

    public float calculateSubtotal() {
        return (float) cartItems.stream()
                .mapToDouble(item -> item.getPrice() * item.getQuantity())
                .sum();
    }

    public float calculateDiscount() {
        return calculateSubtotal() * discountRate;
    }

    public float calculateTotal() {
        return calculateSubtotal() - calculateDiscount();
    }

    private void printCartDetails() {
        float subtotal = calculateSubtotal();
        float discount = calculateDiscount();
        float total = calculateTotal();

        System.out.println("\n🛒 YOUR SHOPPING CART");
        System.out.println("------------------------------------------------------------");
        System.out.printf("%-5s %-25s %10s %8s %12s\n", "ID", "PRODUCT", "PRICE", "QTY", "SUBTOTAL");
        System.out.println("------------------------------------------------------------");

        cartItems.forEach(item -> System.out.printf("%-5d %-25s %10.2f %8d %12.2f\n",
                item.getProductId(),
                item.getName(),
                item.getPrice(),
                item.getQuantity(),
                item.getPrice() * item.getQuantity()));

        System.out.println("------------------------------------------------------------");
        System.out.printf("%40s: %12.2f\n", "Subtotal", subtotal);
        if (discount > 0) {
            System.out.printf("%40s: %12.2f (%.0f%%)\n", "Discount", discount, discountRate * 100);
        }
        System.out.printf("%40s: %12.2f\n", "TOTAL", total);
        System.out.println("------------------------------------------------------------");
    }

    private String generateReceipt(boolean isVipCustomer) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        float subtotal = calculateSubtotal();
        float discount = calculateDiscount();
        float total = subtotal - discount;

        StringBuilder receipt = new StringBuilder();
        receipt.append("\n════════════════ RECEIPT ════════════════\n");
        receipt.append("Date: ").append(sdf.format(new Date())).append("\n");
        receipt.append("VIP Customer: ").append(isVipCustomer ? "Yes" : "No").append("\n\n");
        receipt.append(String.format("%-5s %-25s %10s %8s %12s\n", "ID", "ITEM", "PRICE", "QTY", "TOTAL"));
        receipt.append("------------------------------------------------\n");

        cartItems.forEach(item -> receipt.append(String.format("%-5d %-25s %10.2f %8d %12.2f\n",
                item.getProductId(),
                item.getName(),
                item.getPrice(),
                item.getQuantity(),
                item.getPrice() * item.getQuantity())));

        receipt.append("\n------------------------------------------------\n");
        receipt.append(String.format("%40s: %12.2f\n", "Subtotal", subtotal));
        if (discount > 0) {
            receipt.append(String.format("%40s: %12.2f (%.0f%%)\n", "Discount", discount, discountRate * 100));
        }
        receipt.append(String.format("%40s: %12.2f\n", "Total", total));
        receipt.append("\n══════════ THANK YOU FOR SHOPPING! ══════════\n");

        return receipt.toString();
    }

    private void ensureDataDirectoryExists() {
        new File(TRANSACTION_DIR).mkdirs();
        new File("data").mkdirs();
    }

    @SuppressWarnings("unchecked")
    private void loadCartItems() {
        File file = new File(CART_FILE);
        if (!file.exists()) return;

        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
            cartItems = (List<CartItem>) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("❌ Error loading cart: " + e.getMessage());
            cartItems = new ArrayList<>();
        }
    }

    private synchronized boolean saveCartItems() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(CART_FILE))) {
            oos.writeObject(cartItems);
            return true;
        } catch (IOException e) {
            System.err.println("❌ Error saving cart: " + e.getMessage());
            return false;
        }
    }

    private void saveTransaction(String receipt) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss");
        String filename = TRANSACTION_DIR + "/receipt_" + sdf.format(new Date()) + ".txt";

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filename))) {
            writer.write(receipt);
        } catch (IOException e) {
            System.err.println("❌ Error saving transaction receipt: " + e.getMessage());
        }
    }

    public List<CartItem> getCartItems() {
        return new ArrayList<>(cartItems);
    }

    public boolean isEmpty() {
        return cartItems.isEmpty();
    }

    public synchronized void clearCart() {
        cartItems.clear();
        saveCartItems();
    }

    public ProductService getProductService() {
        return productService;
    }
}