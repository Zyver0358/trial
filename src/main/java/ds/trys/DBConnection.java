/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package ds.trys;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 *
 * @author Rudy Anonuevo
 */
public class DBConnection {
    private static final String SERVER_URL = "jdbc:mysql://localhost:3306/?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true";
    private static final String URL = "jdbc:mysql://localhost:3306/trys_db?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true";
    private static final String USER = "root";
    private static final String PASSWORD = "";

    public static Connection getConnection() throws SQLException {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new SQLException("MySQL JDBC driver not found. Add mysql-connector-j to the project libraries.");
        }
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    public static String initializeDatabase() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            try (Connection conn = DriverManager.getConnection(SERVER_URL, USER, PASSWORD);
                 Statement st = conn.createStatement()) {
                st.executeUpdate("CREATE DATABASE IF NOT EXISTS trys_db");
            }

            try (Connection conn = getConnection(); Statement st = conn.createStatement()) {
                st.executeUpdate("CREATE TABLE IF NOT EXISTS users ("
                        + "username VARCHAR(50) PRIMARY KEY, "
                        + "password VARCHAR(100) NOT NULL, "
                        + "role VARCHAR(20) NOT NULL)");

                st.executeUpdate("CREATE TABLE IF NOT EXISTS suppliers ("
                        + "supplier_id VARCHAR(30) PRIMARY KEY, "
                        + "name VARCHAR(100) NOT NULL, "
                        + "contact_info VARCHAR(150) NOT NULL)");

                st.executeUpdate("CREATE TABLE IF NOT EXISTS products ("
                        + "product_id INT PRIMARY KEY, "
                        + "name VARCHAR(100) NOT NULL, "
                        + "quantity INT NOT NULL, "
                        + "supplier_id VARCHAR(30) NOT NULL, "
                        + "price DOUBLE NOT NULL)");

                st.executeUpdate("CREATE TABLE IF NOT EXISTS transactions ("
                        + "transaction_id INT AUTO_INCREMENT PRIMARY KEY, "
                        + "product_id INT NOT NULL, "
                        + "type VARCHAR(10) NOT NULL, "
                        + "quantity INT NOT NULL, "
                        + "username VARCHAR(50) NOT NULL, "
                        + "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)");
            }
            return null;
        } catch (ClassNotFoundException e) {
            return "MySQL JDBC driver not found. Add mysql-connector-j to the project libraries.";
        } catch (SQLException e) {
            return "Database setup failed: " + e.getMessage();
        }
    }
}