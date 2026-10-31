/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package ds.trys;

import javax.swing.JOptionPane;

/**
 *
 * @author Rudy Anonuevo
 */
public class Main {
    public static void main(String[] args) {
        String error = DBConnection.initializeDatabase();
        if (error != null) {
            JOptionPane.showMessageDialog(null, error, "Database Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        java.awt.EventQueue.invokeLater(() -> {
            new LoginFrame().setVisible(true);
        });
    }
}