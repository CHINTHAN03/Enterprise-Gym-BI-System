# Enterprise Gym Management & BI Dashboard

A professional-grade Java Swing application designed for gym administrators to manage member lifecycles and visualize business health through real-time financial analytics.

<img width="1837" height="971" alt="proj_screenshot" src="https://github.com/user-attachments/assets/851c1b5e-b946-4837-8380-14c09e6f7610" />


##  Key Features

* **Real-time BI Dashboard:** Live visualization of revenue trends using JFreeChart.
* **Member Lifecycle Management:** Full CRUD (Create, Read, Update, Delete) operations with status tracking (Active/Expired).
* **One-Click Check-in System:** Instantly update member attendance and log visit timestamps.
* **Financial Analytics:** Automatic revenue calculation grouped by membership plans (Annual, Quarterly, Monthly).
* **Data Portability:** Integrated CSV Export utility for offline reporting and accounting.
* **Modern UI/UX:** Built with the **FlatLaf** Look and Feel for a sleek, dark-mode enterprise aesthetic.

##  Technical Architecture

The project follows a modular **MVC (Model-View-Controller)** and **DAO (Data Access Object)** pattern to ensure scalability and clean separation of concerns.



### System Flow:
1.  **Presentation Layer:** `AdminDashboard.java` handles user events and UI rendering.
2.  **Logic Layer:** `MemberDAO.java` serves as the engine, translating Java objects into secure SQL queries.
3.  **Data Layer:** `Member.java` acts as the DTO (Data Transfer Object) for structured data handling.
4.  **Infrastructure:** `DatabaseConnection.java` manages the JDBC pool to a MySQL server.

## 🛠️ Tech Stack

* **Language:** Java 17+
* **UI Framework:** Java Swing + FlatLaf (Modern Look & Feel)
* **Database:** MySQL 8.0
* **Data Visualization:** JFreeChart
* **Build Tool:** Maven
* **Database Connectivity:** JDBC (Java Database Connectivity)

## 📂 Project Structure

```text
src/main/java/com/gym/
├── core/
│   ├── DatabaseConnection.java  # JDBC Singleton Connection
│   └── CSVExporter.java         # File I/O Utility
├── dao/
│   └── MemberDAO.java           # CRUD & SQL Business Logic
├── model/
│   └── Member.java              # POJO Data Model
├── view/
│   ├── AdminDashboard.java      # Main BI Interface
│   └── AddMemberDialog.java     # Input Modal
└── Main.java                    # Application Entry Point
