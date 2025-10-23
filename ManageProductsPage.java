import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("serial")
public class ManageProductsPage extends JFrame {

    private final JTable productTable;
    private final DefaultTableModel tableModel;
    private final String adminUsername; // Keep track of admin user
    private List<Integer> productIds = new ArrayList<>(); // Stores PKs corresponding to table rows

    public ManageProductsPage(String adminUsername) {
        this.adminUsername = adminUsername;
        
        setTitle("SareeStore - Manage Products");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE); 
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        getContentPane().setBackground(Color.WHITE);
        setLayout(new BorderLayout());

        // --- Header ---
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(Color.WHITE);
        headerPanel.setBorder(new EmptyBorder(15, 30, 15, 30));

        JLabel titleLabel = new JLabel("Product Inventory Management", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Serif", Font.BOLD, 30));
        titleLabel.setForeground(new Color(140, 40, 80));
        headerPanel.add(titleLabel, BorderLayout.CENTER);
        
        // --- Navigation Buttons Panel (East) ---
        JPanel navPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        navPanel.setBackground(Color.WHITE);
        
        // ⭐️ NEW: Add Product Button ⭐️
        JButton addProductBtn = new JButton("+ Add New Product");
        styleButton(addProductBtn, new Color(34, 139, 34), 16); // Green
        navPanel.add(addProductBtn);
        
        JButton backBtn = new JButton("<< Back to Admin");
        styleButton(backBtn, new Color(100, 100, 100), 16);
        navPanel.add(backBtn);
        
        headerPanel.add(navPanel, BorderLayout.EAST);
        
        add(headerPanel, BorderLayout.NORTH);

        // --- Table Setup ---
        String[] columnNames = {"ID", "Name", "Category", "Price (₹)", "Stock", "Image Path", "Edit", "Delete"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                // Only allow editing in columns 6 (Edit) and 7 (Delete)
                return column >= 6; 
            }
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                // Helps render the buttons correctly (though we use mouse listeners)
                if (columnIndex == 6 || columnIndex == 7) return JButton.class;
                return super.getColumnClass(columnIndex);
            }
        };
        
        productTable = new JTable(tableModel);
        productTable.setRowHeight(30);
        productTable.setFont(new Font("Arial", Font.PLAIN, 14));
        
        // Set column widths for better display
        productTable.getColumnModel().getColumn(0).setPreferredWidth(50); // ID
        productTable.getColumnModel().getColumn(3).setPreferredWidth(100); // Price
        productTable.getColumnModel().getColumn(4).setPreferredWidth(70); // Stock
        productTable.getColumnModel().getColumn(6).setPreferredWidth(50); // Edit
        productTable.getColumnModel().getColumn(7).setPreferredWidth(50); // Delete
        
        // Add table to scroll pane
        JScrollPane scrollPane = new JScrollPane(productTable);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(20, 50, 50, 50));
        add(scrollPane, BorderLayout.CENTER);
        
        // --- Action Listeners ---
        addProductBtn.addActionListener(e -> {
            new AddProductPage(adminUsername, this); // Pass 'this' reference to reload table later
            // dispose(); // Keep this window open if Admin wants to add multiple products
        });
        
        backBtn.addActionListener(e -> {
            new AdminPage(adminUsername);
            dispose();
        });
        
        // Handle Clicks on Edit/Delete Columns
        productTable.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                int col = productTable.columnAtPoint(evt.getPoint());
                int row = productTable.rowAtPoint(evt.getPoint());
                
                if (row >= 0 && row < productIds.size()) {
                    int productId = productIds.get(row);
                    if (col == 6) { // Edit column
                        openEditDialog(productId, row);
                    } else if (col == 7) { // Delete column
                        deleteProduct(productId, row);
                    }
                }
            }
        });

        // Load data on startup
        loadProducts();
        
        setVisible(true);
    }
    
    // --- Logic Methods ---

    /** Fetches all products from the Products table and populates the JTable. */
    public void loadProducts() {
        tableModel.setRowCount(0);
        productIds.clear();
        
        String sql = "SELECT product_id, name, category, price, stock_quantity, image_path FROM Products ORDER BY product_id";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            
            while (rs.next()) {
                int id = rs.getInt("product_id");
                
                // Store the ID corresponding to the row
                productIds.add(id);

                tableModel.addRow(new Object[]{
                    id, 
                    rs.getString("name"), 
                    rs.getString("category"),
                    String.format("%.2f", rs.getDouble("price")), 
                    rs.getInt("stock_quantity"),
                    rs.getString("image_path"),
                    "Edit", // Button simulation
                    "Delete" // Button simulation
                });
            }
            
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error loading products: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }
    
    /** Opens a dialog box to allow the admin to edit product details. */
    private void openEditDialog(int productId, int rowIndex) {
        String name = (String) tableModel.getValueAt(rowIndex, 1);
        String category = (String) tableModel.getValueAt(rowIndex, 2);
        // Remove '₹' and parse price string to a clean number string for the field
        String price = ((String) tableModel.getValueAt(rowIndex, 3)).replace("₹", ""); 
        String stock = String.valueOf(tableModel.getValueAt(rowIndex, 4));
        String imagePath = (String) tableModel.getValueAt(rowIndex, 5);
        
        // ... (rest of the openEditDialog logic remains the same) ...
        
        // Use JTextFields for input
        JTextField nameField = new JTextField(name);
        JTextField priceField = new JTextField(price);
        JTextField stockField = new JTextField(stock);
        JTextField imagePathField = new JTextField(imagePath);
        
        // Use JComboBox for category selection (reuse categories from AddProductPage)
        String[] categories = {"New Collection", "Cotton Sarees", "Silk Sarees", "Banarasi Sarees", "Other"};
        JComboBox<String> categoryComboBox = new JComboBox<>(categories);
        categoryComboBox.setSelectedItem(category);
        
        // Layout for the dialog
        JPanel panel = new JPanel(new GridLayout(0, 2, 5, 5));
        panel.add(new JLabel("Name:"));
        panel.add(nameField);
        panel.add(new JLabel("Category:"));
        panel.add(categoryComboBox);
        panel.add(new JLabel("Price (₹):"));
        panel.add(priceField);
        panel.add(new JLabel("Stock Quantity:"));
        panel.add(stockField);
        panel.add(new JLabel("Image Path:"));
        panel.add(imagePathField);

        int result = JOptionPane.showConfirmDialog(this, panel, 
            "Edit Product ID: " + productId, JOptionPane.OK_CANCEL_OPTION);
        
        if (result == JOptionPane.OK_OPTION) {
            try {
                String newName = nameField.getText().trim();
                String newCategory = (String) categoryComboBox.getSelectedItem();
                double newPrice = Double.parseDouble(priceField.getText().trim());
                int newStock = Integer.parseInt(stockField.getText().trim());
                String newImagePath = imagePathField.getText().trim();

                updateProduct(productId, newName, newCategory, newPrice, newStock, newImagePath);
                
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "Price and Stock must be valid numbers.", "Input Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /** Updates the product details in the database. */
    private void updateProduct(int productId, String name, String category, double price, int stock, String imagePath) {
        String sql = "UPDATE Products SET name=?, category=?, price=?, stock_quantity=?, image_path=? WHERE product_id=?";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            // ... (parameters remain correct) ...
            pstmt.setString(1, name);
            pstmt.setString(2, category);
            pstmt.setDouble(3, price);
            pstmt.setInt(4, stock);
            pstmt.setString(5, imagePath);
            pstmt.setInt(6, productId);
            
            int rowsAffected = pstmt.executeUpdate();
            
            if (rowsAffected > 0) {
                JOptionPane.showMessageDialog(this, "Product updated successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                loadProducts(); // Reload the table to show updated data
            }
            
        } catch (SQLException ex) {
            // ... (error handling remains correct) ...
            JOptionPane.showMessageDialog(this, "Error updating product: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }
    
    /** Deletes a product from the database. */
    private void deleteProduct(int productId, int rowIndex) {
        int dialogResult = JOptionPane.showConfirmDialog(this, 
            "Are you sure you want to permanently delete Product ID " + productId + "?", 
            "Confirm Delete", JOptionPane.YES_NO_OPTION);
        
        if (dialogResult == JOptionPane.YES_OPTION) {
            String sql = "DELETE FROM Products WHERE product_id=?";
            
            try (Connection conn = DBConnection.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                
                // ... (logic remains correct) ...
                pstmt.setInt(1, productId);
                int rowsAffected = pstmt.executeUpdate();
                
                if (rowsAffected > 0) {
                    JOptionPane.showMessageDialog(this, "Product deleted successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                    loadProducts(); // Reload the table
                } else {
                    JOptionPane.showMessageDialog(this, "Product not found.", "Error", JOptionPane.ERROR_MESSAGE);
                }
                
            } catch (SQLException ex) {
                // Handle FK constraint errors (e.g., if product is in a cart or order)
                JOptionPane.showMessageDialog(this, "Error deleting product. Check if it's linked to an Order or Cart: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace();
            }
        }
    }

    private void styleButton(JButton button, Color bgColor, int fontSize) {
        button.setFont(new Font("Arial", Font.BOLD, fontSize));
        button.setBackground(bgColor);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }
}
