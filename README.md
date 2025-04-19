Project Description: Super Shop Management System  
The Super Shop Management System is a Java-based desktop application that enables efficient management of a retail store’s core operations. It is built using Object-Oriented Programming principles in Java and is designed to streamline inventory management, user authentication, product purchasing, and reporting.
This system supports two main user roles:
Admin – Full control over the system (product, stock, purchase report, etc.)
Customer – Can browse products, add to cart, and make purchases

Admin Key 
To access the admin dashboard, use the following credentials during login:
Username: admin  
Password: admin123

Key Functionalities
User Registration and Login (with role-based access)
Admin Panel: 
1.Add, Update, Delete Products
2.View Product List by Category
3.Monitor Stock Quantity
4.View All Purchase History

Customer Panel:
1.View Product List
2.Add Items to Cart
3.Checkout and Generate Bill
4.View Purchase History

Data Persistence using File Handling or optionally SQLite
Technical Stack
Language: Java
IDE: IntelliJ IDEA / NetBeans / Eclipse
Concepts Used: Object-Oriented Programming, File Handling
Optional GUI: Java Swing / JavaFX
Optional DB: SQLite / MySQL

Project Structure Overview

src/
 ├── model/
 │   ├── Product.java
 │   ├── User.java
 │   ├── CartItem.java
 │   ├── Purchase.java
 │   └── PurchaseItem.java
 ├── service/
 │   ├── AuthService.java
 │   ├── ProductService.java
 │   ├── CartService.java
 │   └── PurchaseService.java
 └── Main.java


Getting Started
Clone or download the repository.
Open the project in your Java IDE.
Run Main.java.

Choose:
Login as Admin (username: admin, password: admin123)
Register as Customer and shop
Start managing your super shop.

