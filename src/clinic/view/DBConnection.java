/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package clinic.view;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import javax.swing.JOptionPane;
/**
 *
 * @author Administrator
 */
public class DBConnection {
   // Database Information
    private static final String URL =
            "jdbc:mysql://localhost:3306/clinic";

    private static final String USER = "root";

    private static final String PASSWORD = "@Choyisaac12";

    /**
     * Establishes MySQL database connection
     */
    public static Connection getConnection() {

        Connection conn = null;

        try {

            // Load JDBC Driver
            Class.forName("com.mysql.cj.jdbc.Driver");

            // Connect to MySQL
            conn = DriverManager.getConnection(URL, USER, PASSWORD);

            System.out.println("Database Connected!");

        } catch (ClassNotFoundException e) {

            JOptionPane.showMessageDialog(
                    null,
                    "MySQL JDBC Driver not found!",
                    "Driver Error",
                    JOptionPane.ERROR_MESSAGE);

        } catch (SQLException e) {

            JOptionPane.showMessageDialog(
                    null,
                    "Failed to connect to database!\n" + e.getMessage(),
                    "Connection Error",
                    JOptionPane.ERROR_MESSAGE);
        }

        return conn;
    }
    }

        

