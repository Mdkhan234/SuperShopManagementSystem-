package model;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;

public class User implements Serializable {
    private static final long serialVersionUID = 1L;
    private String name;
    private String mobile;
    private String password;
    private int age;
    private boolean isAdmin;
    private boolean isVip;  // Add this field
    private Date registrationDate;
    private Date lastLoginDate;
    private boolean isActive;


    public User(String name, String mobile, String password, int age, boolean isAdmin) {
        this(name, mobile, password, age, isAdmin, false, new Date(), null, true);
    }

    public User(String name, String mobile, String password, int age, boolean isAdmin,
                boolean isVip, Date registrationDate, Date lastLoginDate, boolean isActive) {
        setName(name);
        setMobile(mobile);
        setPassword(password);
        setAge(age);
        this.isAdmin = isAdmin;
        this.isVip = isVip;  // Initialize VIP status
        this.registrationDate = registrationDate;
        this.lastLoginDate = lastLoginDate;
        this.isActive = isActive;
    }

    public boolean isVip() {
        return isVip;
    }

    public void setVip(boolean vip) {
        isVip = vip;
    }

    public String getName() {
        return name;
    }

    public String getMobile() {
        return mobile;
    }

    public String getPassword() {
        return password;
    }

    public int getAge() {
        return age;
    }

    public boolean isAdmin() {
        return isAdmin;
    }

    public Date getRegistrationDate() {
        return registrationDate;
    }

    public Date getLastLoginDate() {
        return lastLoginDate;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setName(String name) {
        if (name != null && name.matches("[a-zA-Z ]{2,50}")) {
            this.name = name.trim();
        } else {
            throw new IllegalArgumentException("Invalid name format");
        }
    }

    public void setMobile(String mobile) {
        if (mobile != null && mobile.matches("01[3-9]\\d{8}")) {
            this.mobile = mobile;
        } else {
            throw new IllegalArgumentException("Invalid mobile number");
        }
    }

    public void setPassword(String password) {
        if (password != null && password.length() >= 8) {
            this.password = password;
        } else {
            throw new IllegalArgumentException("Password must be at least 8 characters");
        }
    }

    public void setAge(int age) {
        if (age >= 13 && age <= 120) {
            this.age = age;
        } else {
            throw new IllegalArgumentException("Age must be between 13 and 120");
        }
    }

    public void setAdmin(boolean isAdmin) {
        this.isAdmin = isAdmin;
    }

    public void setLastLoginDate(Date lastLoginDate) {
        this.lastLoginDate = lastLoginDate;
    }

    public void setActive(boolean isActive) {
        this.isActive = isActive;
    }

    public void updateLastLogin() {
        this.lastLoginDate = new Date();
    }

    public String getFormattedRegistrationDate() {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(registrationDate);
    }

    public String getFormattedLastLoginDate() {
        return lastLoginDate != null
                ? new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(lastLoginDate)
                : "Never logged in";
    }

    public String toCSV() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return String.join(",",
                name,
                mobile,
                password,
                String.valueOf(age),
                String.valueOf(isAdmin),
                String.valueOf(isVip),
                sdf.format(registrationDate),
                lastLoginDate != null ? sdf.format(lastLoginDate) : "null",
                String.valueOf(isActive)
        );
    }

    public static User fromCSV(String csvLine) {
        try {
            String[] parts = csvLine.split(",");
            if (parts.length != 9) {
                throw new IllegalArgumentException("Invalid CSV format for User");
            }

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            return new User(
                    parts[0],
                    parts[1],
                    parts[2],
                    Integer.parseInt(parts[3]),
                    Boolean.parseBoolean(parts[4]),
                    Boolean.parseBoolean(parts[5]),
                    sdf.parse(parts[6]),
                    parts[7].equals("null") ? null : sdf.parse(parts[7]),
                    Boolean.parseBoolean(parts[8])
            );
        } catch (Exception e) {
            System.err.println("Error parsing User from CSV: " + e.getMessage());
            return null;
        }
    }

    @Override
    public String toString() {
        return String.format("User[Name: %s, Mobile: %s, Age: %d, %s, Registered: %s, Last Login: %s, %s]",
                name,
                mobile,
                age,
                isAdmin ? "Admin" : "Customer",
                getFormattedRegistrationDate(),
                getFormattedLastLoginDate(),
                isActive ? "Active" : "Inactive");
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        User user = (User) obj;
        return mobile.equals(user.mobile);
    }

    @Override
    public int hashCode() {
        return mobile.hashCode();
    }

    public boolean verifyPassword(String inputPassword) {
        return this.password.equals(inputPassword);
    }

    public boolean canPerformAdminAction() {
        return isActive && isAdmin;
    }
}