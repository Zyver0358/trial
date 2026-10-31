/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package ds.trys;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

/**
 *
 * @author Rudy Anonuevo
 */
public class InventoryManager extends JFrame implements ActionListener {

    public static final int LOW_STOCK_THRESHOLD = 5;
    public static final int RESTOCK_TARGET = 20;

    private LinkedList<Product> productList;
    private Product root;
    private HashMap<String, Supplier> supplierMap;
    private String currentUserRole;
    private String currentUsername;
    private ArrayList<JFrame> childWindows = new ArrayList<>();

    private DefaultTableModel tableModel;
    private JTable table;
    private JScrollPane scrollPane;
    private JTextField txtFPID, txtFName, txtFQuantity, txtFSID, txtFPrice;
    private JButton btnAdd, btnUpdate, btnDelete, btnSearch, btnVReport, btnVSupplier, btnStock, btnUsers, btnRefresh;
    private JLabel lbHeader, lbIM, lbPDT, lbPID, lbName, lbQuantity, lbSID, lbPrice, lbSReport, lbReport;

    public InventoryManager(String role, String username) {
        this.currentUserRole = role;
        this.currentUsername = username;
        productList = new LinkedList<>();
        root = null;
        supplierMap = new HashMap<>();

        String[] columns = {"Product ID", "Name", "Quantity", "Supplier ID", "Price"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        String titleExtra = isAdmin() ? " [Admin - Full Control]" : " [Staff - View, Search & Stock Transactions]";
        setTitle("Inventory Management System" + titleExtra);
        setSize(900, 410);
        setLayout(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        table = new JTable(tableModel);
        scrollPane = new JScrollPane(table);
        scrollPane.setBounds(440, 70, 430, 190);
        add(scrollPane);

        txtFPID = new JTextField();
        txtFPID.setBounds(110, 70, 270, 30);
        add(txtFPID);

        txtFName = new JTextField();
        txtFName.setBounds(110, 110, 270, 30);
        add(txtFName);

        txtFQuantity = new JTextField();
        txtFQuantity.setBounds(110, 150, 270, 30);
        add(txtFQuantity);

        txtFSID = new JTextField();
        txtFSID.setBounds(110, 190, 270, 30);
        add(txtFSID);

        txtFPrice = new JTextField();
        txtFPrice.setBounds(110, 230, 270, 30);
        add(txtFPrice);

        lbHeader = new JLabel("Inventory Management System");
        lbHeader.setBounds(360, 0, 220, 30);
        add(lbHeader);

        lbIM = new JLabel("Inventory Manager");
        lbIM.setBounds(135, 35, 120, 30);
        add(lbIM);

        lbPDT = new JLabel("Product Data Table");
        lbPDT.setBounds(600, 35, 120, 30);
        add(lbPDT);

        lbPID = new JLabel("Product ID:");
        lbPID.setBounds(10, 70, 100, 30);
        add(lbPID);

        lbName = new JLabel("Name:");
        lbName.setBounds(10, 110, 100, 30);
        add(lbName);

        lbQuantity = new JLabel("Quantity:");
        lbQuantity.setBounds(10, 150, 100, 30);
        add(lbQuantity);

        lbSID = new JLabel("Supplier ID:");
        lbSID.setBounds(10, 190, 100, 30);
        add(lbSID);

        lbPrice = new JLabel("Price:");
        lbPrice.setBounds(10, 230, 100, 30);
        add(lbPrice);

        lbSReport = new JLabel("Stock Report:");
        lbSReport.setBounds(440, 265, 100, 30);
        add(lbSReport);

        lbReport = new JLabel(" [No data]");
        lbReport.setBounds(530, 265, 120, 30);
        add(lbReport);

        btnAdd = new JButton("ADD");
        btnAdd.setBounds(10, 280, 85, 30);
        add(btnAdd);

        btnUpdate = new JButton("UPDATE");
        btnUpdate.setBounds(105, 280, 85, 30);
        add(btnUpdate);

        btnDelete = new JButton("DELETE");
        btnDelete.setBounds(200, 280, 85, 30);
        add(btnDelete);

        btnSearch = new JButton("SEARCH");
        btnSearch.setBounds(295, 280, 85, 30);
        add(btnSearch);

        btnStock = new JButton("Stock-In / Stock-Out");
        btnStock.setBounds(10, 325, 170, 30);
        add(btnStock);

        btnUsers = new JButton("Manage Users");
        btnUsers.setBounds(190, 325, 130, 30);
        add(btnUsers);

        btnVReport = new JButton("View Report");
        btnVReport.setBounds(440, 325, 130, 30);
        add(btnVReport);

        btnVSupplier = new JButton("View Supplier");
        btnVSupplier.setBounds(580, 325, 130, 30);
        add(btnVSupplier);

        btnRefresh = new JButton("Refresh");
        btnRefresh.setBounds(720, 325, 100, 30);
        add(btnRefresh);

        btnAdd.addActionListener(this);
        btnUpdate.addActionListener(this);
        btnDelete.addActionListener(this);
        btnSearch.addActionListener(this);
        btnStock.addActionListener(this);
        btnUsers.addActionListener(this);
        btnVReport.addActionListener(this);
        btnVSupplier.addActionListener(this);
        btnRefresh.addActionListener(this);

        if (!isAdmin()) {
            btnAdd.setEnabled(false);
            btnUpdate.setEnabled(false);
            btnDelete.setEnabled(false);
            btnUsers.setVisible(false);
        }

        loadDataFromDatabase();
    }

    boolean isAdmin() {
        return "Admin".equalsIgnoreCase(currentUserRole);
    }

    public LinkedList<Product> getProducts() {
        return productList;
    }

    public Collection<Supplier> getSuppliers() {
        return supplierMap.values();
    }

    public String getCurrentUsername() {
        return currentUsername;
    }

    private void openChild(JFrame frame) {
        childWindows.add(frame);
        frame.setVisible(true);
    }

    @Override
    public void dispose() {
        for (JFrame f : childWindows) {
            f.dispose();
        }
        super.dispose();
    }

    public void loadDataFromDatabase() {
        productList.clear();
        supplierMap.clear();
        root = null;

        try (Connection conn = DBConnection.getConnection()) {
            String supSql = "SELECT * FROM suppliers ORDER BY supplier_id";
            try (PreparedStatement pstmt = conn.prepareStatement(supSql);
                 ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    String id = rs.getString("supplier_id");
                    String name = rs.getString("name");
                    String contact = rs.getString("contact_info");
                    supplierMap.put(id, new Supplier(id, name, contact));
                }
            }

            String prodSql = "SELECT * FROM products ORDER BY product_id";
            try (PreparedStatement pstmt = conn.prepareStatement(prodSql);
                 ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    int id = rs.getInt("product_id");
                    String name = rs.getString("name");
                    int qty = rs.getInt("quantity");
                    String supId = rs.getString("supplier_id");
                    double price = rs.getDouble("price");

                    Product p = new Product(id, name, qty, supId, price);
                    productList.add(p);
                    root = insertBST(root, p);
                }
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Database Load Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
        refreshTable();
    }

    private void refreshTable() {
        tableModel.setRowCount(0);
        for (Product p : productList) {
            tableModel.addRow(new Object[]{p.getId(), p.getName(), p.getQuantity(), p.getSupplier(), p.getPrice()});
        }
        updateStockLabelStatus();
    }

    private void updateStockLabelStatus() {
        if (productList.isEmpty()) {
            lbReport.setText(" [No data]");
        } else if (hasLowStock(LOW_STOCK_THRESHOLD)) {
            lbReport.setText(" [low-stock]");
        } else {
            lbReport.setText(" [Fully Stocked]");
        }
    }

    private void clearProductFields() {
        txtFPID.setText("");
        txtFName.setText("");
        txtFQuantity.setText("");
        txtFSID.setText("");
        txtFPrice.setText("");
    }

    private void validateProduct(String name, int qty, String supplierId, double price) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Product name cannot be empty.");
        }
        if (supplierId == null || supplierId.trim().isEmpty()) {
            throw new IllegalArgumentException("Supplier ID cannot be empty.");
        }
        if (qty < 0) {
            throw new IllegalArgumentException("Quantity cannot be negative.");
        }
        if (price < 0) {
            throw new IllegalArgumentException("Price cannot be negative.");
        }
    }

    private boolean supplierExists(Connection conn, String supplierId) throws SQLException {
        String sql = "SELECT supplier_id FROM suppliers WHERE supplier_id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, supplierId);
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next();
            }
        }
    }

    public void addProduct(Product p) {
        validateProduct(p.getName(), p.getQuantity(), p.getSupplier(), p.getPrice());

        try (Connection conn = DBConnection.getConnection()) {
            if (!supplierExists(conn, p.getSupplier())) {
                throw new IllegalArgumentException("Supplier ID '" + p.getSupplier() + "' does not exist. Add the supplier first.");
            }

            String sql = "INSERT INTO products (product_id, name, quantity, supplier_id, price) VALUES (?, ?, ?, ?, ?)";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, p.getId());
            pstmt.setString(2, p.getName());
            pstmt.setInt(3, p.getQuantity());
            pstmt.setString(4, p.getSupplier());
            pstmt.setDouble(5, p.getPrice());
            pstmt.executeUpdate();
        } catch (SQLIntegrityConstraintViolationException e) {
            throw new IllegalArgumentException("Product ID " + p.getId() + " already exists.");
        } catch (SQLException e) {
            throw new RuntimeException("Database error during insert: " + e.getMessage());
        }
        loadDataFromDatabase();
    }

    private Product insertBST(Product current, Product p) {
        if (current == null) return p;
        if (p.getPrice() < current.getPrice()) {
            current.left = insertBST(current.left, p);
        } else {
            current.right = insertBST(current.right, p);
        }
        return current;
    }

    public boolean updateProduct(int id, String name, int qty, String supplier, double price) {
        validateProduct(name, qty, supplier, price);
        int affected;

        try (Connection conn = DBConnection.getConnection()) {
            if (!supplierExists(conn, supplier)) {
                throw new IllegalArgumentException("Supplier ID '" + supplier + "' does not exist. Add the supplier first.");
            }

            String sql = "UPDATE products SET name = ?, quantity = ?, supplier_id = ?, price = ? WHERE product_id = ?";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, name);
            pstmt.setInt(2, qty);
            pstmt.setString(3, supplier);
            pstmt.setDouble(4, price);
            pstmt.setInt(5, id);
            affected = pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Database update error: " + e.getMessage());
        }
        loadDataFromDatabase();
        return affected > 0;
    }

    public boolean deleteProduct(int id) {
        int affected;
        try (Connection conn = DBConnection.getConnection()) {
            String sql = "DELETE FROM products WHERE product_id = ?";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, id);
            affected = pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Database delete error: " + e.getMessage());
        }
        loadDataFromDatabase();
        return affected > 0;
    }

    public Product searchById(int id) {
        for (Product p : productList) {
            if (p.getId() == id) return p;
        }
        return null;
    }

    public Product searchByName(String name) {
        for (Product p : productList) {
            if (p.getName().equalsIgnoreCase(name)) return p;
        }
        return null;
    }

    public Product searchBySupplier(String supplierId) {
        for (Product p : productList) {
            if (p.getSupplier().equalsIgnoreCase(supplierId)) return p;
        }
        return null;
    }

    public Product searchByPrice(double price) {
        Product current = root;
        while (current != null) {
            if (Double.compare(price, current.getPrice()) == 0) return current;
            if (price < current.getPrice()) {
                current = current.left;
            } else {
                current = current.right;
            }
        }
        return null;
    }

    public void addSupplier(Supplier s) {
        if (s.getId().trim().isEmpty() || s.getName().trim().isEmpty() || s.getContactInfo().trim().isEmpty()) {
            throw new IllegalArgumentException("Supplier ID, name and contact info cannot be empty.");
        }
        try (Connection conn = DBConnection.getConnection()) {
            String sql = "INSERT INTO suppliers (supplier_id, name, contact_info) VALUES (?, ?, ?)";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, s.getId());
            pstmt.setString(2, s.getName());
            pstmt.setString(3, s.getContactInfo());
            pstmt.executeUpdate();
        } catch (SQLIntegrityConstraintViolationException e) {
            throw new IllegalArgumentException("Supplier ID '" + s.getId() + "' already exists.");
        } catch (SQLException e) {
            throw new RuntimeException("Database error adding supplier: " + e.getMessage());
        }
        loadDataFromDatabase();
    }

    public boolean updateSupplier(String id, String name, String contact) {
        int affected;
        try (Connection conn = DBConnection.getConnection()) {
            String sql = "UPDATE suppliers SET name = ?, contact_info = ? WHERE supplier_id = ?";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, name);
            pstmt.setString(2, contact);
            pstmt.setString(3, id);
            affected = pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Database error updating supplier: " + e.getMessage());
        }
        loadDataFromDatabase();
        return affected > 0;
    }

    public boolean deleteSupplier(String id) {
        int affected;
        try (Connection conn = DBConnection.getConnection()) {
            PreparedStatement check = conn.prepareStatement("SELECT COUNT(*) FROM products WHERE supplier_id = ?");
            check.setString(1, id);
            ResultSet rs = check.executeQuery();
            rs.next();
            if (rs.getInt(1) > 0) {
                throw new IllegalArgumentException("Cannot delete this supplier because products still use it.");
            }

            PreparedStatement pstmt = conn.prepareStatement("DELETE FROM suppliers WHERE supplier_id = ?");
            pstmt.setString(1, id);
            affected = pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Database error deleting supplier: " + e.getMessage());
        }
        loadDataFromDatabase();
        return affected > 0;
    }

    private String supplierNameOf(String supplierId) {
        Supplier s = supplierMap.get(supplierId);
        return (s == null) ? "N/A" : s.getName();
    }

    public void recordTransaction(int productId, String type, int qty) {
        if (qty <= 0) {
            throw new IllegalArgumentException("Transaction quantity must be greater than zero.");
        }

        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                int affected;
                if ("IN".equals(type)) {
                    PreparedStatement pstmt = conn.prepareStatement("UPDATE products SET quantity = quantity + ? WHERE product_id = ?");
                    pstmt.setInt(1, qty);
                    pstmt.setInt(2, productId);
                    affected = pstmt.executeUpdate();
                } else {
                    PreparedStatement pstmt = conn.prepareStatement("UPDATE products SET quantity = quantity - ? WHERE product_id = ? AND quantity >= ?");
                    pstmt.setInt(1, qty);
                    pstmt.setInt(2, productId);
                    pstmt.setInt(3, qty);
                    affected = pstmt.executeUpdate();
                }

                if (affected == 0) {
                    PreparedStatement pstmt = conn.prepareStatement("SELECT quantity FROM products WHERE product_id = ?");
                    pstmt.setInt(1, productId);
                    ResultSet rs = pstmt.executeQuery();
                    if (!rs.next()) {
                        throw new IllegalArgumentException("Product ID " + productId + " was not found.");
                    }
                    throw new IllegalArgumentException("Not enough stock. Available quantity: " + rs.getInt("quantity"));
                }

                PreparedStatement ins = conn.prepareStatement("INSERT INTO transactions (product_id, type, quantity, username) VALUES (?, ?, ?, ?)");
                ins.setInt(1, productId);
                ins.setString(2, type);
                ins.setInt(3, qty);
                ins.setString(4, currentUsername);
                ins.executeUpdate();
                conn.commit();
            } catch (SQLException | RuntimeException ex) {
                conn.rollback();
                throw ex;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Database error during transaction: " + e.getMessage());
        }
        loadDataFromDatabase();
    }

    public boolean hasLowStock(int threshold) {
        for (Product p : productList) {
            if (p.getQuantity() <= threshold) return true;
        }
        return false;
    }

    public String generateLowStockReport(int threshold) {
        StringBuilder report = new StringBuilder("=== LOW STOCK REPORT (Qty <= " + threshold + ") ===\n\n");
        boolean found = false;
        for (Product p : productList) {
            if (p.getQuantity() <= threshold) {
                report.append("ID: ").append(p.getId())
                      .append(" | Name: ").append(p.getName())
                      .append(" | Qty: ").append(p.getQuantity())
                      .append(" | Supplier: ").append(p.getSupplier())
                      .append(" (").append(supplierNameOf(p.getSupplier())).append(")")
                      .append(" | Price: PHP ").append(String.format("%.2f", p.getPrice()))
                      .append("\n");
                found = true;
            }
        }
        return found ? report.toString() : "All products are fully stocked (Quantity > " + threshold + ").";
    }

    public String generateRestockReport(int threshold, int targetLevel) {
        if (!hasLowStock(threshold)) {
            return "No products need restocking (Quantity > " + threshold + ").";
        }
        StringBuilder report = new StringBuilder("=== RESTOCK REPORT (Qty <= " + threshold
                + ", restock up to " + targetLevel + ") ===\n\n");
        double totalCost = restockInOrder(root, report, threshold, targetLevel);
        report.append("\nTotal Estimated Cost: PHP ").append(String.format("%.2f", totalCost));
        return report.toString();
    }

    private double restockInOrder(Product node, StringBuilder report, int threshold, int targetLevel) {
        if (node == null) return 0;

        double total = restockInOrder(node.left, report, threshold, targetLevel);

        if (node.getQuantity() <= threshold) {
            int toOrder = targetLevel - node.getQuantity();
            double cost = toOrder * node.getPrice();
            total += cost;
            report.append("ID: ").append(node.getId())
                  .append(" | Name: ").append(node.getName())
                  .append(" | Qty: ").append(node.getQuantity())
                  .append(" | Order: ").append(toOrder)
                  .append(" | Supplier: ").append(node.getSupplier())
                  .append(" (").append(supplierNameOf(node.getSupplier())).append(")")
                  .append(" | Est. Cost: PHP ").append(String.format("%.2f", cost))
                  .append("\n");
        }

        total += restockInOrder(node.right, report, threshold, targetLevel);
        return total;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == btnAdd) {
            if (!isAdmin()) return;
            String PID = txtFPID.getText().trim();
            String Name = txtFName.getText().trim();
            String Quanty = txtFQuantity.getText().trim();
            String SID = txtFSID.getText().trim();
            String Price = txtFPrice.getText().trim();

            if (!PID.isEmpty() && !Name.isEmpty() && !Quanty.isEmpty() && !SID.isEmpty() && !Price.isEmpty()) {
                try {
                    int id = Integer.parseInt(PID);
                    int qty = Integer.parseInt(Quanty);
                    double price = Double.parseDouble(Price);
                    addProduct(new Product(id, Name, qty, SID, price));

                    clearProductFields();
                    JOptionPane.showMessageDialog(this, "Product successfully added and saved to database!");
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(this, "Product ID and Quantity must be whole numbers, Price must be a number", "Input Error", JOptionPane.ERROR_MESSAGE);
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, ex.getMessage(), "Input Error", JOptionPane.ERROR_MESSAGE);
                }
            } else {
                JOptionPane.showMessageDialog(this, "Please fill in all product details first", "ERROR Message", JOptionPane.ERROR_MESSAGE);
            }
        } else if (e.getSource() == btnUpdate) {
            if (!isAdmin()) return;
            int rowSelected = table.getSelectedRow();
            if (rowSelected != -1) {
                String PID = txtFPID.getText().trim();
                String Name = txtFName.getText().trim();
                String Quanty = txtFQuantity.getText().trim();
                String SID = txtFSID.getText().trim();
                String Price = txtFPrice.getText().trim();

                if (!PID.isEmpty() && !Name.isEmpty() && !Quanty.isEmpty() && !SID.isEmpty() && !Price.isEmpty()) {
                    try {
                        int selectedId = Integer.parseInt(tableModel.getValueAt(rowSelected, 0).toString());
                        int id = Integer.parseInt(PID);
                        int qty = Integer.parseInt(Quanty);
                        double price = Double.parseDouble(Price);
                        if (id != selectedId) {
                            JOptionPane.showMessageDialog(this, "Product ID cannot be changed. Use the ID of the selected row", "Input Error", JOptionPane.ERROR_MESSAGE);
                        } else if (updateProduct(id, Name, qty, SID, price)) {
                            clearProductFields();
                            JOptionPane.showMessageDialog(this, "Product Info has been updated in the database");
                        } else {
                            JOptionPane.showMessageDialog(this, "Product no longer exists (it may have been deleted by another user).", "Update Failed", JOptionPane.ERROR_MESSAGE);
                        }
                    } catch (NumberFormatException ex) {
                        JOptionPane.showMessageDialog(this, "Product ID and Quantity must be whole numbers, Price must be a number", "Input Error", JOptionPane.ERROR_MESSAGE);
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(this, ex.getMessage(), "Input Error", JOptionPane.ERROR_MESSAGE);
                    }
                } else {
                    JOptionPane.showMessageDialog(this, "Please fill in all product details first", "ERROR Message", JOptionPane.ERROR_MESSAGE);
                }
            } else {
                JOptionPane.showMessageDialog(this, "Please select a product first before updating", "ERROR Message", JOptionPane.ERROR_MESSAGE);
            }
        } else if (e.getSource() == btnDelete) {
            if (!isAdmin()) return;
            int indexSelected = table.getSelectedRow();
            if (indexSelected != -1) {
                int confirm = JOptionPane.showConfirmDialog(this, "Delete the selected product?", "Confirm Delete", JOptionPane.YES_NO_OPTION);
                if (confirm == JOptionPane.YES_OPTION) {
                    try {
                        int id = Integer.parseInt(tableModel.getValueAt(indexSelected, 0).toString());
                        if (deleteProduct(id)) {
                            JOptionPane.showMessageDialog(this, "Product Info has been deleted from the database");
                        } else {
                            JOptionPane.showMessageDialog(this, "Product no longer exists (it may have been deleted by another user).", "Delete Failed", JOptionPane.ERROR_MESSAGE);
                        }
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            } else {
                JOptionPane.showMessageDialog(this, "Please select a product first before deleting", "ERROR Message", JOptionPane.ERROR_MESSAGE);
            }
        } else if (e.getSource() == btnSearch) {
            String PID = txtFPID.getText().trim();
            String Name = txtFName.getText().trim();
            String SID = txtFSID.getText().trim();
            String Price = txtFPrice.getText().trim();

            if (!PID.isEmpty() || !Name.isEmpty() || !SID.isEmpty() || !Price.isEmpty()) {
                try {
                    Product found = null;
                    if (!PID.isEmpty()) {
                        found = searchById(Integer.parseInt(PID));
                        txtFPID.setText("");
                    } else if (!Name.isEmpty()) {
                        found = searchByName(Name);
                        txtFName.setText("");
                    } else if (!SID.isEmpty()) {
                        found = searchBySupplier(SID);
                        txtFSID.setText("");
                    } else if (!Price.isEmpty()) {
                        found = searchByPrice(Double.parseDouble(Price));
                        txtFPrice.setText("");
                    }

                    if (found != null) {
                        for (int i = 0; i < tableModel.getRowCount(); i++) {
                            if (tableModel.getValueAt(i, 0).toString().equals(String.valueOf(found.getId()))) {
                                table.setRowSelectionInterval(i, i);
                                table.scrollRectToVisible(table.getCellRect(i, 0, true));
                                break;
                            }
                        }
                        JOptionPane.showMessageDialog(this, "Match found!");
                    } else {
                        table.clearSelection();
                        JOptionPane.showMessageDialog(this, "No matching product found!", "Input Error", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(this, "Product ID must be a whole number and Price must be a number", "Input Error", JOptionPane.ERROR_MESSAGE);
                }
            } else {
                JOptionPane.showMessageDialog(this, "Enter a Product ID, Name, Supplier ID, or Price to search", "ERROR Message", JOptionPane.ERROR_MESSAGE);
            }
        } else if (e.getSource() == btnStock) {
            openChild(new StockFrame(this));
        } else if (e.getSource() == btnUsers) {
            if (!isAdmin()) return;
            openChild(new UserFrame(this));
        } else if (e.getSource() == btnRefresh) {
            loadDataFromDatabase();
        } else if (e.getSource() == btnVReport) {
            loadDataFromDatabase();
            if (!productList.isEmpty()) {
                openChild(new ReportFrame(this));
            } else {
                JOptionPane.showMessageDialog(this, "Please add a product first", "ERROR Message", JOptionPane.ERROR_MESSAGE);
            }
        } else if (e.getSource() == btnVSupplier) {
            openChild(new SupplierFrame(this));
        }
    }
}