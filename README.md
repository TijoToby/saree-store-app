#  SareeStore – Java Swing E-Commerce Application  

##  Project Overview  
SareeStore is a **desktop-based e-commerce application** built using **Java Swing** and **MySQL**.  
The project simulates an online saree shopping platform where users can register, log in, view saree products, add them to the cart, and proceed to checkout.  

The system also includes an optional **Admin Module** to manage products and orders.  
This project demonstrates the concepts of **GUI design, event handling, database connectivity (JDBC), and MVC architecture**.

---

##  Objective  
To design and develop a **Java-based desktop application** for saree shopping that provides:
- A **user-friendly graphical interface**
- **Database-backed** login and signup system
- **Product listing and cart functionality**
- **Checkout and order confirmation system**

---

## Key Features  

| Feature | Description |
|----------|-------------|
| **User Authentication** | Secure signup and login using MySQL database |
|  **Product Display** | View saree products by category and details |
|  **Cart Management** | Add, view, and remove items from the shopping cart |
|  **Checkout** | Simulated payment and order confirmation |
|  **Database Integration** | Uses MySQL database via JDBC |
|  **Interactive UI** | Elegant and responsive Java Swing design |
|  **Admin Module** *(Optional)* | Add, delete, or manage saree details and user orders |

---

## ⚙️ Technologies Used  

| Component | Technology |
|------------|-------------|
| Programming Language | Java (JDK 8 or above) |
| GUI Framework | Java Swing |
| Database | MySQL |
| Database Connectivity | JDBC |
| Architecture | MVC (Model-View-Controller) |
| IDE Used | IntelliJ IDEA / Eclipse / NetBeans |

---

##  Project Structure  

├── src/
│ ├── DBConnection.java → Handles MySQL database connection
│ ├── ui.java → Login Page
│ ├── Signup.java → Signup / Registration Page
│ ├── homePage.java → Main Homepage after login
│ ├── ProductPage.java → Product display with “Add to Cart”
│ ├── cartpage.java → Shopping cart page
│ ├── PaymentPage.java → Payment / Checkout page
│ ├── AccountPage.java → Customer profile page
│ ├── AdminPage.java → Admin dashboard (optional)
│ └── App.java → Main entry point of the project
└── /resources/
└── images/ → Product and UI images



 Learning Concepts Covered

✔ Java Swing GUI design
✔ Event handling and ActionListeners
✔ Database CRUD operations
✔ MVC pattern implementation
✔ Exception handling and validation
✔ Realistic UI flow and user experience


 Conclusion

The SareeStore application is a well-structured and interactive desktop-based e-commerce system.
It demonstrates how Java Swing can be used to build real-world applications that interact with databases, manage data efficiently, and provide an elegant user interface.
It serves as a strong example of combining GUI, database, and object-oriented programming concepts in one cohesive project.