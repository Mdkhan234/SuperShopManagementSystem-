
import model.User;
import model.Product;
import service.AuthService;
import service.ProductService;
import service.CartService;
import service.PurchaseService;
import java.util.*;
import java.text.*;

public class Main {
    private static final Scanner scanner = new Scanner(System.in);
    private static final AuthService authService = new AuthService();
    private static final ProductService productService = new ProductService();
    private static final CartService cartService = new CartService(productService);
    private static final PurchaseService purchaseService = new PurchaseService(cartService);
    private static User currentUser;

    public static void main(String[] args) {
        Runtime.getRuntime().addShutdownHook(new Thread(Main::closeResources));
        displayWelcomeScreen();
        mainMenu();
    }

    private static void closeResources() {
        scanner.close();
        System.out.println("\nSystem resources cleaned up");
    }

    private static void displayWelcomeScreen() {
        System.out.println("\n=========================================");
        System.out.println("    SUPER SHOP MANAGEMENT SYSTEM v3.0    ");
        System.out.println("=========================================");
    }

    // ========== Main Menu ==========
    private static void mainMenu() {
        while (true) {
            System.out.println("\n=== MAIN MENU ===");
            System.out.println("1. Login");
            System.out.println("2. Register");
            System.out.println("3. Exit");
            System.out.print("Enter your choice: ");

            int choice = getIntInput(1, 3);

            switch (choice) {
                case 1 -> loginMenu();
                case 2 -> registerMenu();
                case 3 -> {
                    System.out.println("\nThank you for using our system. Goodbye!");
                    System.exit(0);
                }
            }
        }
    }

    // ========== Authentication Menus ==========
    private static void loginMenu() {
        while (true) {
            System.out.println("\n=== LOGIN ===");
            System.out.println("1. Admin Login");
            System.out.println("2. Customer Login");
            System.out.println("3. Back to Main Menu");
            System.out.print("Enter choice: ");

            int roleChoice = getIntInput(1, 3);
            if (roleChoice == 3) return;

            String mobile = getValidMobile();
            String password = getValidPassword();

            boolean isAdmin = (roleChoice == 1);
            currentUser = authService.login(mobile, password, isAdmin);

            if (currentUser != null) {
                System.out.println("\n✅ Login successful! Welcome, " + currentUser.getName() + "!");
                if (currentUser.isAdmin()) {
                    adminDashboard();
                } else {
                    customerDashboard();
                }
                return;
            } else {
                System.out.println("❌ Login failed. Invalid credentials or account not found.");
                System.out.print("Try again? (Y/N): ");
                if (!scanner.nextLine().equalsIgnoreCase("Y")) {
                    return;
                }
            }
        }
    }

    private static void registerMenu() {
        while (true) {
            System.out.println("\n=== REGISTER ===");
            System.out.println("1. Register as Customer");
            System.out.println("2. Register as Admin (requires key)");
            System.out.println("3. Back to Main Menu");
            System.out.print("Enter choice: ");

            int choice = getIntInput(1, 3);
            if (choice == 3) return;

            boolean isAdmin = (choice == 2);
            if (isAdmin) {
                System.out.print("Enter admin registration key: ");
                String secretKey = scanner.nextLine();
                if (!authService.validateAdminKey(secretKey)) {
                    System.out.println("❌ Invalid admin key!");
                    continue;
                }
            }

            String name = getValidName();
            String mobile = getValidMobile();
            String password = getValidPassword();
            int age = getValidAge();

            User newUser = new User(name, mobile, password, age, isAdmin);
            if (authService.registerUser(newUser)) {
                System.out.println("\n✅ Registration successful! You can now login.");
                return;
            } else {
                System.out.println("\n❌ Registration failed. Mobile may already be registered.");
                System.out.print("Try again? (Y/N): ");
                if (!scanner.nextLine().equalsIgnoreCase("Y")) {
                    return;
                }
            }
        }
    }

    // ========== Admin Dashboard ==========
    private static void adminDashboard() {
        while (true) {
            System.out.println("\n=== ADMIN DASHBOARD ===");
            System.out.println("1. Product Management");
            System.out.println("2. Sales Reports");
            System.out.println("3. User Management");
            System.out.println("4. View Profile");
            System.out.println("5. Logout");
            System.out.print("Enter choice: ");

            int choice = getIntInput(1, 5);

            switch (choice) {
                case 1 -> productManagementMenu();
                case 2 -> salesReportMenu();
                case 3 -> userManagementMenu();
                case 4 -> displayUserProfile();
                case 5 -> {
                    authService.logout();
                    currentUser = null;
                    return;
                }
            }
        }
    }

    private static void userManagementMenu() {
        while (true) {
            System.out.println("\n=== USER MANAGEMENT ===");
            System.out.println("1. View All Users");
            System.out.println("2. Search User");
            System.out.println("3. Back to Admin Dashboard");
            System.out.print("Enter choice: ");

            int choice = getIntInput(1, 3);

            switch (choice) {
                case 1 -> authService.viewAllUsers();
                case 2 -> {
                    System.out.print("Enter mobile number to search: ");
                    String mobile = scanner.nextLine();
                    authService.viewUserDetails(mobile);
                }
                case 3 -> { return; }
            }
        }
    }

    // ========== Product Management ==========
    private static void productManagementMenu() {
        while (true) {
            System.out.println("\n=== PRODUCT MANAGEMENT ===");
            System.out.println("1. Add New Product");
            System.out.println("2. View All Products");
            System.out.println("3. Update Product");
            System.out.println("4. Delete Product");
            System.out.println("5. Check Low Stock");
            System.out.println("6. Back to Admin Dashboard");
            System.out.print("Enter choice: ");

            int choice = getIntInput(1, 6);

            switch (choice) {
                case 1 -> addProductMenu();
                case 2 -> productService.displayAllProducts();
                case 3 -> updateProductMenu();
                case 4 -> deleteProductMenu();
                case 5 -> checkLowStockMenu();
                case 6 -> { return; }
            }
        }
    }

    private static void addProductMenu() {
        System.out.println("\n=== ADD NEW PRODUCT ===");
        productService.displayCategories();

        System.out.print("Enter category ID: ");
        int categoryId = getIntInput(1, Integer.MAX_VALUE);

        System.out.print("Enter product name: ");
        String name = scanner.nextLine().trim();
        while (name.isEmpty()) {
            System.out.println("❌ Product name cannot be empty");
            System.out.print("Enter product name: ");
            name = scanner.nextLine().trim();
        }

        System.out.print("Enter price: ");
        float price = getFloatInput(0.01f, Float.MAX_VALUE);

        System.out.print("Enter initial quantity: ");
        int quantity = getIntInput(0, Integer.MAX_VALUE);

        if (productService.insertProduct(categoryId, name, price, quantity)) {
            System.out.println("✅ Product added successfully!");
        } else {
            System.out.println("❌ Failed to add product");
        }
    }

    private static void updateProductMenu() {
        System.out.println("\n=== UPDATE PRODUCT ===");
        productService.displayAllProducts();

        System.out.print("Enter product ID to update: ");
        int productId = getIntInput(1, Integer.MAX_VALUE);

        Product product = productService.getProductById(productId);
        if (product == null) {
            System.out.println("❌ Product not found");
            return;
        }

        System.out.println("\nCurrent Product Details:");
        displayProductDetails(product);

        System.out.println("\n1. Update Name");
        System.out.println("2. Update Category");
        System.out.println("3. Update Price");
        System.out.println("4. Update Quantity");
        System.out.println("5. Back to Product Management");
        System.out.print("Enter choice: ");

        int choice = getIntInput(1, 5);

        switch (choice) {
            case 1 -> {
                System.out.print("Enter new name: ");
                String newName = scanner.nextLine().trim();
                if (!newName.isEmpty()) {
                    product.setName(newName);
                    System.out.println("✅ Name updated successfully!");
                }
            }
            case 2 -> {
                productService.displayCategories();
                System.out.print("Enter new category ID: ");
                int newCategoryId = getIntInput(1, Integer.MAX_VALUE);
                product.setCategoryId(newCategoryId);
                System.out.println("✅ Category updated successfully!");
            }
            case 3 -> {
                System.out.print("Enter new price: ");
                float newPrice = getFloatInput(0.01f, Float.MAX_VALUE);
                product.setPrice(newPrice);
                System.out.println("✅ Price updated successfully!");
            }
            case 4 -> {
                System.out.print("Enter new quantity: ");
                int newQuantity = getIntInput(0, Integer.MAX_VALUE);
                product.setQuantity(newQuantity);
                System.out.println("✅ Quantity updated successfully!");
            }
            case 5 -> { return; }
        }

        productService.updateProduct(product);
    }

    private static void deleteProductMenu() {
        System.out.println("\n=== DELETE PRODUCT ===");
        productService.displayAllProducts();

        System.out.print("Enter product ID to delete: ");
        int productId = getIntInput(1, Integer.MAX_VALUE);

        System.out.print("Are you sure you want to delete this product? (Y/N): ");
        String confirmation = scanner.nextLine();
        if (confirmation.equalsIgnoreCase("Y")) {
            if (productService.deleteProduct(productId)) {
                System.out.println("✅ Product deleted successfully!");
            } else {
                System.out.println("❌ Failed to delete product");
            }
        }
    }

    private static void checkLowStockMenu() {
        System.out.print("\nEnter low stock threshold: ");
        int threshold = getIntInput(1, Integer.MAX_VALUE);
        productService.displayLowStockWarning(threshold);
    }

    // ========== Sales Reports ==========
    private static void salesReportMenu() {
        while (true) {
            System.out.println("\n=== SALES REPORTS ===");
            System.out.println("1. Today's Sales");
            System.out.println("2. Yesterday's Sales");
            System.out.println("3. This Week's Sales");
            System.out.println("4. Custom Date Range");
            System.out.println("5. Back to Admin Dashboard");
            System.out.print("Enter choice: ");

            int choice = getIntInput(1, 5);

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            Calendar cal = Calendar.getInstance();
            Date today = cal.getTime();

            try {
                switch (choice) {
                    case 1 -> {
                        purchaseService.generateSalesReport(today, today);
                    }
                    case 2 -> {
                        cal.add(Calendar.DATE, -1);
                        Date yesterday = cal.getTime();
                        purchaseService.generateSalesReport(yesterday, yesterday);
                    }
                    case 3 -> {
                        cal.add(Calendar.DATE, -6);
                        Date weekStart = cal.getTime();
                        purchaseService.generateSalesReport(weekStart, today);
                    }
                    case 4 -> {
                        Date[] dateRange = getDateRangeFromUser();
                        purchaseService.generateSalesReport(dateRange[0], dateRange[1]);
                    }
                    case 5 -> { return; }
                }
            } catch (Exception e) {
                System.out.println("❌ Error generating sales report: " + e.getMessage());
            }
        }
    }

    // In Main.java, update getDateRangeFromUser():
    private static Date[] getDateRangeFromUser() throws ParseException {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Date from = null;
        Date to = null;

        while (from == null) {
            System.out.print("Enter start date (yyyy-MM-dd): ");
            try {
                from = sdf.parse(scanner.nextLine());
                // Ensure date isn't in the future
                if (from.after(new Date())) {
                    System.out.println("❌ Start date cannot be in the future.");
                    from = null;
                }
            } catch (ParseException e) {
                System.out.println("❌ Invalid date format. Please try again.");
            }
        }

        while (to == null) {
            System.out.print("Enter end date (yyyy-MM-dd): ");
            try {
                to = sdf.parse(scanner.nextLine());
                if (to.before(from)) {
                    System.out.println("❌ End date must be after start date.");
                    to = null;
                } else if (to.after(new Date())) {
                    System.out.println("❌ End date cannot be in the future.");
                    to = null;
                }
            } catch (ParseException e) {
                System.out.println("❌ Invalid date format. Please try again.");
            }
        }

        return new Date[]{from, to};
    }

    // ========== Customer Dashboard ==========
    private static void customerDashboard() {
        while (true) {
            System.out.println("\n=== CUSTOMER DASHBOARD ===");
            System.out.println("1. Browse Products");
            System.out.println("2. My Cart");
            System.out.println("3. Purchase History");
            System.out.println("4. My Profile");
            System.out.println("5. Logout");
            System.out.print("Enter choice: ");

            int choice = getIntInput(1, 5);

            switch (choice) {
                case 1 -> browseProductsMenu();
                case 2 -> cartMenu();
                case 3 -> purchaseService.viewPurchaseHistory(currentUser.getMobile());
                case 4 -> profileMenu();
                case 5 -> {
                    currentUser = null;
                    return;
                }
            }
        }
    }

    private static void browseProductsMenu() {
        while (true) {
            System.out.println("\n=== BROWSE PRODUCTS ===");
            System.out.println("1. View All Products");
            System.out.println("2. Search by Name");
            System.out.println("3. Search by Category");
            System.out.println("4. Search by Price Range");
            System.out.println("5. Back to Customer Dashboard");
            System.out.print("Enter choice: ");

            int choice = getIntInput(1, 5);

            switch (choice) {
                case 1 -> productService.displayAllProducts();
                case 2 -> {
                    System.out.print("Enter product name: ");
                    String name = scanner.nextLine();
                    Product product = productService.getProductByName(name);
                    displayProductDetails(product);
                }
                case 3 -> {
                    productService.displayCategories();
                    System.out.print("Enter category ID: ");
                    int categoryId = getIntInput(1, Integer.MAX_VALUE);
                    List<Product> products = productService.getProductsByCategory(categoryId);
                    displayProductList(products);
                }
                case 4 -> {
                    System.out.print("Enter minimum price: ");
                    float min = getFloatInput(0, Float.MAX_VALUE);
                    System.out.print("Enter maximum price: ");
                    float max = getFloatInput(min, Float.MAX_VALUE);
                    List<Product> products = productService.getProductsByPriceRange(min, max);
                    displayProductList(products);
                }
                case 5 -> { return; }
            }
        }
    }

    private static void cartMenu() {
        while (true) {
            System.out.println("\n=== MY CART ===");
            System.out.println("1. View Cart");
            System.out.println("2. Add Item");
            System.out.println("3. Update Quantity");
            System.out.println("4. Remove Item");
            System.out.println("5. Checkout");
            System.out.println("6. Back to Customer Dashboard");
            System.out.print("Enter choice: ");

            int choice = getIntInput(1, 6);

            switch (choice) {
                case 1 -> cartService.viewCart();
                case 2 -> addToCartMenu();
                case 3 -> updateCartItemMenu();
                case 4 -> removeCartItemMenu();
                case 5 -> checkoutMenu();
                case 6 -> { return; }
            }
        }
    }

    private static void addToCartMenu() {
        productService.displayAllProducts();
        System.out.print("Enter product ID to add: ");
        int productId = getIntInput(1, Integer.MAX_VALUE);

        Product product = productService.getProductById(productId);
        if (product == null) {
            System.out.println("❌ Product not found");
            return;
        }

        System.out.print("Enter quantity: ");
        int quantity = getIntInput(1, product.getQuantity());

        cartService.addToCart(productId, quantity);
    }

    private static void updateCartItemMenu() {
        cartService.viewCart();
        if (cartService.isEmpty()) return;

        System.out.print("Enter product ID to update: ");
        int productId = getIntInput(1, Integer.MAX_VALUE);

        Product product = productService.getProductById(productId);
        if (product == null) {
            System.out.println("❌ Product not found");
            return;
        }

        System.out.print("Enter new quantity: ");
        int quantity = getIntInput(0, product.getQuantity());

        cartService.updateCartItem(productId, quantity);
    }

    private static void removeCartItemMenu() {
        cartService.viewCart();
        if (cartService.isEmpty()) return;

        System.out.print("Enter product ID to remove: ");
        int productId = getIntInput(1, Integer.MAX_VALUE);

        System.out.print("Are you sure? (Y/N): ");
        String confirm = scanner.nextLine();
        if (confirm.equalsIgnoreCase("Y")) {
            cartService.removeCartItem(productId);
        }
    }

    private static void checkoutMenu() {
        if (cartService.isEmpty()) {
            System.out.println("❌ Your cart is empty.");
            return;
        }

        cartService.viewCart();
        System.out.print("Confirm checkout? (Y/N): ");
        String confirm = scanner.nextLine();

        if (confirm.equalsIgnoreCase("Y")) {
            boolean isVip = currentUser.isVip(); // Assuming User class has isVip() method
            if (purchaseService.checkout(currentUser.getMobile(), isVip)) {
                System.out.println("✅ Checkout completed successfully!");
            } else {
                System.out.println("❌ Checkout failed. Please try again.");
            }
        }
    }

    private static void profileMenu() {
        while (true) {
            System.out.println("\n=== MY PROFILE ===");
            System.out.println("1. View Profile");
            System.out.println("2. Update Name");
            System.out.println("3. Change Password");
            System.out.println("4. Back to Customer Dashboard");
            System.out.print("Enter choice: ");

            int choice = getIntInput(1, 4);

            switch (choice) {
                case 1 -> displayUserProfile();
                case 2 -> updateNameMenu();
                case 3 -> changePasswordMenu();
                case 4 -> { return; }
            }
        }
    }

    private static void updateNameMenu() {
        System.out.print("Enter new name: ");
        String newName = scanner.nextLine().trim();
        if (newName.isEmpty()) {
            System.out.println("❌ Name cannot be empty");
            return;
        }

        if (authService.updateUserProfile(currentUser.getMobile(), newName, null)) {
            currentUser.setName(newName);
            System.out.println("✅ Name updated successfully!");
        } else {
            System.out.println("❌ Failed to update name.");
        }
    }

    private static void changePasswordMenu() {
        System.out.print("Enter current password: ");
        String currentPass = scanner.nextLine();
        if (!currentUser.getPassword().equals(currentPass)) {
            System.out.println("❌ Incorrect current password.");
            return;
        }

        System.out.print("Enter new password: ");
        String newPass = scanner.nextLine();
        System.out.print("Confirm new password: ");
        String confirmPass = scanner.nextLine();

        if (!newPass.equals(confirmPass)) {
            System.out.println("❌ Passwords don't match.");
            return;
        }

        if (authService.updateUserProfile(currentUser.getMobile(), null, newPass)) {
            currentUser.setPassword(newPass);
            System.out.println("✅ Password updated successfully!");
        } else {
            System.out.println("❌ Failed to update password.");
        }
    }

    // ========== Display Methods ==========
    private static void displayUserProfile() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        System.out.println("\n=== PROFILE DETAILS ===");
        System.out.println("Name: " + currentUser.getName());
        System.out.println("Mobile: " + currentUser.getMobile());
        System.out.println("Age: " + currentUser.getAge());
        System.out.println("Account Type: " + (currentUser.isAdmin() ? "Admin" : "Customer"));
        System.out.println("Registration Date: " + sdf.format(currentUser.getRegistrationDate()));
        System.out.println("Last Login: " + (currentUser.getLastLoginDate() != null ?
                sdf.format(currentUser.getLastLoginDate()) : "Never logged in"));
    }

    private static void displayProductDetails(Product product) {
        if (product == null) {
            System.out.println("❌ Product not found.");
            return;
        }

        System.out.println("\n=== PRODUCT DETAILS ===");
        System.out.println("ID: " + product.getId());
        System.out.println("Name: " + product.getName());
        System.out.println("Category: " + productService.getCategoryName(product.getCategoryId()));
        System.out.printf("Price: $%.2f\n", product.getPrice());
        System.out.println("Quantity: " + product.getQuantity());
        System.out.println("Status: " + (product.isInStock() ? "In Stock" : "Out of Stock"));
    }

    private static void displayProductList(List<Product> products) {
        if (products == null || products.isEmpty()) {
            System.out.println("❌ No products found.");
            return;
        }

        System.out.println("\n=== PRODUCT LIST ===");
        System.out.printf("%-5s %-20s %-15s %-10s %-8s\n", "ID", "Name", "Category", "Price", "Qty");
        System.out.println("------------------------------------------------");
        for (Product p : products) {
            System.out.printf("%-5d %-20s %-15s $%-9.2f %-8d\n",
                    p.getId(),
                    p.getName(),
                    productService.getCategoryName(p.getCategoryId()),
                    p.getPrice(),
                    p.getQuantity());
        }
    }

    // ========== Input Validation Methods ==========
    private static String getValidName() {
        while (true) {
            System.out.print("Enter full name (2-50 characters): ");
            String name = scanner.nextLine().trim();
            if (name.length() >= 2 && name.length() <= 50) {
                return name;
            }
            System.out.println("❌ Invalid name. Must be 2-50 characters.");
        }
    }

    private static String getValidMobile() {
        while (true) {
            System.out.print("Enter mobile (11 digits starting with 01): ");
            String mobile = scanner.nextLine().trim();
            if (mobile.matches("01[3-9]\\d{8}")) {
                return mobile;
            }
            System.out.println("❌ Invalid mobile. Must be 11 digits starting with 01[3-9].");
        }
    }

    private static String getValidPassword() {
        while (true) {
            System.out.print("Enter password (min 8 characters): ");
            String password = scanner.nextLine().trim();
            if (password.length() >= 8) {
                return password;
            }
            System.out.println("❌ Password must be at least 8 characters.");
        }
    }

    private static int getValidAge() {
        while (true) {
            System.out.print("Enter age (13-120): ");
            try {
                int age = Integer.parseInt(scanner.nextLine());
                if (age >= 13 && age <= 120) {
                    return age;
                }
                System.out.println("❌ Age must be between 13 and 120.");
            } catch (NumberFormatException e) {
                System.out.println("❌ Please enter a valid number.");
            }
        }
    }

    private static int getIntInput(int min, int max) {
        while (true) {
            try {
                int input = Integer.parseInt(scanner.nextLine());
                if (input >= min && input <= max) {
                    return input;
                }
                System.out.printf("❌ Please enter a number between %d and %d: ", min, max);
            } catch (NumberFormatException e) {
                System.out.print("❌ Please enter a valid number: ");
            }
        }
    }

    private static float getFloatInput(float min, float max) {
        while (true) {
            try {
                float input = Float.parseFloat(scanner.nextLine());
                if (input >= min && input <= max) {
                    return input;
                }
                System.out.printf("❌ Please enter a number between %.2f and %.2f: ", min, max);
            } catch (NumberFormatException e) {
                System.out.print("❌ Please enter a valid number: ");
            }
        }
    }
}