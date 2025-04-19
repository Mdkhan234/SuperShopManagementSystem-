package service;

import model.Purchase;
import model.Product;
import model.CartItem;
import model.PurchaseItem;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

public class PurchaseService {
    private static final String PURCHASES_FILE = "data/purchases.dat";
    private static final String TRANSACTIONS_DIR = "data/transactions";
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");
    private static final SimpleDateFormat DATETIME_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    private final CartService cartService;
    private final ProductService productService;
    private List<Purchase> purchases;

    public PurchaseService(CartService cartService) {
        this.cartService = Objects.requireNonNull(cartService, "CartService cannot be null");
        this.productService = cartService.getProductService();
        this.purchases = new ArrayList<>();
        ensureDirectoriesExist();
        loadPurchases();
    }

    public boolean checkout(String customerMobile, boolean isVipCustomer) {
        if (cartService.isEmpty()) {
            System.out.println("❌ Cannot checkout empty cart");
            return false;
        }

        // Validate stock before processing
        for (CartItem item : cartService.getCartItems()) {
            Product product = productService.getProductById(item.getProductId());
            if (product == null || product.getQuantity() < item.getQuantity()) {
                System.out.printf("❌ Insufficient stock for %s (Available: %d, Requested: %d)\n",
                        product != null ? product.getName() : "Product",
                        product != null ? product.getQuantity() : 0,
                        item.getQuantity());
                return false;
            }
        }

        try {
            // Process checkout with cart service
            boolean checkoutSuccess = cartService.checkout(isVipCustomer);
            if (!checkoutSuccess) {
                return false;
            }

            // Create purchase record
            Purchase purchase = new Purchase(
                    customerMobile,
                    convertCartItemsToPurchaseItems(cartService.getCartItems()),
                    cartService.calculateSubtotal(),
                    cartService.calculateDiscount(),
                    "Cash" // Default payment method
            );

            // Save the purchase
            purchases.add(purchase);
            savePurchases();
            saveTransactionReceipt(purchase);

            System.out.println("✅ Checkout successful! Transaction ID: " + purchase.getPurchaseId());
            printReceipt(purchase);
            return true;
        } catch (Exception e) {
            System.out.println("❌ Checkout failed: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    private List<PurchaseItem> convertCartItemsToPurchaseItems(List<CartItem> cartItems) {
        return cartItems.stream()
                .map(item -> new PurchaseItem(
                        item.getProductId(),
                        item.getName(),
                        item.getPrice(),
                        item.getQuantity()
                ))
                .collect(Collectors.toList());
    }

    public void viewPurchaseHistory(String customerMobile) {
        List<Purchase> customerPurchases = purchases.stream()
                .filter(p -> p.getCustomerMobile().equals(customerMobile))
                .sorted(Comparator.comparing(Purchase::getPurchaseDate).reversed())
                .collect(Collectors.toList());

        if (customerPurchases.isEmpty()) {
            System.out.println("❌ No purchase history found");
            return;
        }

        System.out.println("\n🛒 PURCHASE HISTORY FOR: " + customerMobile);
        System.out.println("══════════════════════════════════════");

        customerPurchases.forEach(purchase -> {
            System.out.println("\nTransaction ID: " + purchase.getPurchaseId());
            System.out.println("Date: " + DATETIME_FORMAT.format(purchase.getPurchaseDate()));
            System.out.printf("Total: %.2f\n", purchase.getTotal());
            System.out.println("Items:");

            purchase.getItems().forEach(item -> {
                System.out.printf("- %-20s (ID: %d) %3d x %-6.2f = %-8.2f\n",
                        item.getProductName(),
                        item.getProductId(),
                        item.getQuantity(),
                        item.getUnitPrice(),
                        item.getItemTotal());
            });
        });
    }

    public void generateSalesReport(Date startDate, Date endDate) {
        List<Purchase> filteredPurchases = purchases.stream()
                .filter(p -> !p.getPurchaseDate().before(startDate) &&
                        !p.getPurchaseDate().after(endDate))
                .sorted(Comparator.comparing(Purchase::getPurchaseDate))
                .collect(Collectors.toList());

        if (filteredPurchases.isEmpty()) {
            System.out.println("❌ No sales found for the selected period");
            return;
        }

        double totalRevenue = filteredPurchases.stream()
                .mapToDouble(Purchase::getTotal)
                .sum();
        int totalItemsSold = filteredPurchases.stream()
                .flatMap(p -> p.getItems().stream())
                .mapToInt(PurchaseItem::getQuantity)
                .sum();

        System.out.println("\n════════════ SALES REPORT ════════════");
        System.out.printf("Period: %s to %s\n",
                DATE_FORMAT.format(startDate), DATE_FORMAT.format(endDate));
        System.out.printf("Total Transactions: %d\n", filteredPurchases.size());
        System.out.printf("Total Items Sold: %d\n", totalItemsSold);
        System.out.printf("Total Revenue: %.2f\n", totalRevenue);
        System.out.println("══════════════════════════════════════");

        // Product-wise sales breakdown
        Map<Integer, PurchaseItem> productSales = new HashMap<>();
        filteredPurchases.forEach(purchase ->
                purchase.getItems().forEach(item -> {
                    if (productSales.containsKey(item.getProductId())) {
                        PurchaseItem existing = productSales.get(item.getProductId());
                        productSales.put(item.getProductId(),
                                new PurchaseItem(
                                        item.getProductId(),
                                        item.getProductName(),
                                        item.getUnitPrice(),
                                        existing.getQuantity() + item.getQuantity()
                                ));
                    } else {
                        productSales.put(item.getProductId(), item);
                    }
                })
        );

        System.out.println("\nProduct-wise Sales:");
        System.out.printf("%-5s %-25s %10s %12s\n", "ID", "Name", "Qty Sold", "Revenue");
        System.out.println("------------------------------------------------");

        productSales.values().stream()
                .sorted((a, b) -> Integer.compare(b.getQuantity(), a.getQuantity()))
                .forEach(item -> {
                    double revenue = item.getQuantity() * item.getUnitPrice();
                    System.out.printf("%-5d %-25s %10d %12.2f\n",
                            item.getProductId(),
                            item.getProductName(),
                            item.getQuantity(),
                            revenue);
                });
    }

    private void printReceipt(Purchase purchase) {
        System.out.println("\n══════════════ RECEIPT ══════════════");
        System.out.println("Transaction ID: " + purchase.getPurchaseId());
        System.out.println("Date: " + DATETIME_FORMAT.format(purchase.getPurchaseDate()));
        System.out.println("Customer: " + purchase.getCustomerMobile());
        System.out.println("\nItems Purchased:");
        System.out.printf("%-5s %-25s %10s %8s %12s\n",
                "ID", "Name", "Price", "Qty", "Subtotal");
        System.out.println("------------------------------------------------");

        purchase.getItems().forEach(item -> {
            System.out.printf("%-5d %-25s %10.2f %8d %12.2f\n",
                    item.getProductId(),
                    item.getProductName(),
                    item.getUnitPrice(),
                    item.getQuantity(),
                    item.getItemTotal());
        });

        System.out.println("\n------------------------------------------------");
        System.out.printf("%40s: %12.2f\n", "Subtotal", purchase.getSubtotal());
        if (purchase.getDiscount() > 0) {
            System.out.printf("%40s: %12.2f\n", "Discount", purchase.getDiscount());
        }
        System.out.printf("%40s: %12.2f\n", "TOTAL", purchase.getTotal());
        System.out.println("══════════════════════════════════════\n");
    }

    // ========== Data Persistence ==========
    @SuppressWarnings("unchecked")
    private void loadPurchases() {
        File file = new File(PURCHASES_FILE);
        if (!file.exists()) return;

        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
            purchases = (List<Purchase>) ois.readObject();
        } catch (Exception e) {
            System.err.println("❌ Error loading purchases: " + e.getMessage());
            purchases = new ArrayList<>();
        }
    }

    private synchronized void savePurchases() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(PURCHASES_FILE))) {
            oos.writeObject(purchases);
        } catch (IOException e) {
            System.err.println("❌ Error saving purchases: " + e.getMessage());
        }
    }

    private void saveTransactionReceipt(Purchase purchase) {
        String filename = TRANSACTIONS_DIR + "/receipt_" + purchase.getPurchaseId() + ".txt";
        try (PrintWriter writer = new PrintWriter(filename)) {
            writer.println("══════════════ RECEIPT ══════════════");
            writer.println("Transaction ID: " + purchase.getPurchaseId());
            writer.println("Date: " + DATETIME_FORMAT.format(purchase.getPurchaseDate()));
            writer.println("Customer: " + purchase.getCustomerMobile());
            writer.println("\nItems Purchased:");
            writer.printf("%-5s %-25s %10s %8s %12s\n",
                    "ID", "Name", "Price", "Qty", "Subtotal");
            writer.println("------------------------------------------------");

            purchase.getItems().forEach(item -> {
                writer.printf("%-5d %-25s %10.2f %8d %12.2f\n",
                        item.getProductId(),
                        item.getProductName(),
                        item.getUnitPrice(),
                        item.getQuantity(),
                        item.getItemTotal());
            });

            writer.println("\n------------------------------------------------");
            writer.printf("%40s: %12.2f\n", "Subtotal", purchase.getSubtotal());
            if (purchase.getDiscount() > 0) {
                writer.printf("%40s: %12.2f\n", "Discount", purchase.getDiscount());
            }
            writer.printf("%40s: %12.2f\n", "TOTAL", purchase.getTotal());
            writer.println("══════════════════════════════════════");
        } catch (FileNotFoundException e) {
            System.err.println("❌ Error saving transaction receipt: " + e.getMessage());
        }
    }

    private void ensureDirectoriesExist() {
        new File(TRANSACTIONS_DIR).mkdirs();
        new File("data").mkdirs();
    }
}