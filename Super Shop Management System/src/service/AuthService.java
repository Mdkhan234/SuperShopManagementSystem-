package service;

import model.User;
import java.util.ArrayList;
import java.util.List;
import java.io.*;

public class AuthService {
    private static final String ADMIN_KEY = "admin123";
    private List<User> users;
    private static final String USERS_FILE = "data/users.dat";
    private static final String CART_FILE = "data/cart.dat";

    public AuthService() {
        users = new ArrayList<>();
        loadUsers();
    }
    public void viewUserDetails(String mobile) {
        User user = users.stream()
                .filter(u -> u.getMobile().equals(mobile))
                .findFirst()
                .orElse(null);

        if (user != null) {
            System.out.println("\n=== USER DETAILS ===");
            System.out.println("Name: " + user.getName());
            System.out.println("Mobile: " + user.getMobile());
            System.out.println("Age: " + user.getAge());
            System.out.println("Type: " + (user.isAdmin() ? "Admin" : "Customer"));
            System.out.println("Status: " + (user.isActive() ? "Active" : "Inactive"));
            System.out.println("Registered: " + user.getFormattedRegistrationDate());
            System.out.println("Last Login: " + user.getFormattedLastLoginDate());
        } else {
            System.out.println("❌ User not found with mobile: " + mobile);
        }
    }

    public User login(String mobile, String password, boolean isAdmin) {
        for (User user : users) {
            if (user.getMobile().equals(mobile) &&
                    user.verifyPassword(password) &&
                    user.isAdmin() == isAdmin) {
                user.updateLastLogin();
                saveUsers();
                return user;
            }
        }
        return null;
    }

    public void logout() {
        saveUsers();
    }

    public boolean registerUser(User newUser) {
        if (isMobileRegistered(newUser.getMobile())) {
            return false;
        }
        users.add(newUser);
        saveUsers();
        return true;
    }

    public boolean validateAdminKey(String inputKey) {
        return ADMIN_KEY.equals(inputKey.trim());
    }

    public void viewAllUsers() {
        System.out.println("\n=== ALL REGISTERED USERS ===");
        System.out.printf("%-15s %-20s %-10s %-10s %-15s\n",
                "Mobile", "Name", "Age", "Type", "Last Login");
        System.out.println("------------------------------------------------------------");

        for (User user : users) {
            System.out.printf("%-15s %-20s %-10d %-10s %-15s\n",
                    user.getMobile(),
                    user.getName(),
                    user.getAge(),
                    user.isAdmin() ? "Admin" : "Customer",
                    user.getFormattedLastLoginDate());
        }
    }

    public boolean updateUserProfile(String mobile, String newName, String newPassword) {
        for (User user : users) {
            if (user.getMobile().equals(mobile)) {
                if (newName != null && !newName.trim().isEmpty()) {
                    user.setName(newName.trim());
                }
                if (newPassword != null && newPassword.length() >= 8) {
                    user.setPassword(newPassword);
                }
                saveUsers();
                return true;
            }
        }
        return false;
    }

    private boolean isMobileRegistered(String mobile) {
        return users.stream().anyMatch(u -> u.getMobile().equals(mobile));
    }

    private void saveUsers() {
        try (ObjectOutputStream oos = new ObjectOutputStream(
                new FileOutputStream(USERS_FILE))) {
            oos.writeObject(users);
        } catch (IOException e) {
            System.err.println("Error saving users: " + e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    private void loadUsers() {
        File file = new File(USERS_FILE);
        if (file.exists()) {
            try (ObjectInputStream ois = new ObjectInputStream(
                    new FileInputStream(file))) {
                users = (List<User>) ois.readObject();
            } catch (IOException | ClassNotFoundException e) {
                System.err.println("Error loading users: " + e.getMessage());
                users = new ArrayList<>();
            }
        }
    }

    public boolean upgradeToAdmin(String mobile, String adminKey) {
        if (!validateAdminKey(adminKey)) return false;

        for (User user : users) {
            if (user.getMobile().equals(mobile)) {
                user.setAdmin(true);
                saveUsers();
                return true;
            }
        }
        return false;
    }
}