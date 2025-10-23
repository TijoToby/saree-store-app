import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

@SuppressWarnings("serial")
public class AddProductPage extends JFrame {

    // UI Input Fields
    private final JTextField idField = new JTextField(15);
    private final JTextField nameField = new JTextField(15);
    private final JTextArea detailsArea = new JTextArea(3, 15);
    private final JTextField priceField = new JTextField(15);
    private final JTextField imagePathField = new JTextField(15);
    private final JTextField stockField = new JTextField(15);
    private final JComboBox<String> categoryComboBox;
    private final String adminUsername;
    
    // ⭐️ NEW: Reference to the main product list page ⭐️
    private final ManageProductsPage manageProductsPage; 

    // Modified Constructor to accept the calling page
    public AddProductPage(String adminUsername, ManageProductsPage callerPage) { 
        super("SareeStore - Add New Product"); 
        this.adminUsername = adminUsername;
        this.manageProductsPage = callerPage; // Store the reference
        
        setTitle("SareeStore - Add New Product");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE); 
        setSize(800, 650);
        setLocationRelativeTo(null);
        getContentPane().setBackground(Color.WHITE);
        setLayout(new BorderLayout());

        // --- Header ---
        JLabel titleLabel = new JLabel("Add New Saree to Inventory", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Serif", Font.BOLD, 30));
        titleLabel.setForeground(new Color(140, 40, 80));
        titleLabel.setBorder(new EmptyBorder(20, 0, 20, 0));
        add(titleLabel, BorderLayout.NORTH);

        // --- Main Form Panel ---
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(Color.WHITE);
        formPanel.setBorder(new EmptyBorder(30, 50, 50, 50));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        // Product Categories (must match your database categories)
        String[] categories = {"New Collection", "Cotton Sarees", "Silk Sarees", "Banarasi Sarees", "Other"};
        categoryComboBox = new JComboBox<>(categories);
        categoryComboBox.setFont(new Font("Arial", Font.PLAIN, 16));

        idField.setEditable(false); 
        idField.setText("AUTO GENERATED");
        
        // Row 1: Product ID (Placeholder)
        addRow(formPanel, gbc, "Product ID:", idField, 0);
        
        // Row 2: Product Name
        addRow(formPanel, gbc, "Product Name:", nameField, 1);
        
        // Row 3: Price
        addRow(formPanel, gbc, "Price:", priceField, 2);
        
        // Row 4: Stock Quantity
        addRow(formPanel, gbc, "Stock Quantity:", stockField, 3);
        
        // Row 5: Category (ComboBox)
        addRow(formPanel, gbc, "Category:", categoryComboBox, 4);

        // Row 6: Product Image Path
        addRow(formPanel, gbc, "Product Image Path (e.g., images/new.jpg):", imagePathField, 5);

        // Row 7: Product Details (TextArea)
        gbc.gridx = 0;
        gbc.gridy = 6;
        gbc.gridwidth = 1;
        JLabel detailsLabel = new JLabel("Product Details:");
        detailsLabel.setFont(new Font("Arial", Font.BOLD, 16));
        formPanel.add(detailsLabel, gbc);

        gbc.gridx = 1;
        gbc.gridy = 6;
        gbc.gridwidth = 1;
        detailsArea.setLineWrap(true);
        detailsArea.setWrapStyleWord(true);
        JScrollPane scrollPane = new JScrollPane(detailsArea);
        scrollPane.setPreferredSize(new Dimension(200, 80)); 
        formPanel.add(scrollPane, gbc);

        // Row 8: Action Buttons
        JButton addBtn = new JButton("Add Product");
        JButton backBtn = new JButton("Back to Admin"); // Changed from "Back to Admin" to "Back to List" for better flow
        
        styleButton(addBtn, new Color(34, 139, 34)); // Green
        styleButton(backBtn, new Color(100, 100, 100)); // Gray

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
        buttonPanel.setBackground(Color.WHITE);
        buttonPanel.add(addBtn);
        buttonPanel.add(backBtn);
        
        gbc.gridx = 0;
        gbc.gridy = 7;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(30, 0, 0, 0);
        formPanel.add(buttonPanel, gbc);

        add(formPanel, BorderLayout.CENTER);
        setVisible(true);
        
        // --- Action Listeners ---
        addBtn.addActionListener(e -> addProduct());
        backBtn.addActionListener(e -> {
            // New AdminPage(adminUsername); // Use this if you want to go all the way back
            dispose(); // Just close this window, leaving the ManageProductsPage open
        });
    }
    
    // Helper methods (addRow and styleButton are correct)
    private void addRow(JPanel panel, GridBagConstraints gbc, String labelText, JComponent component, int y) {
        // ... (implementation is correct) ...
        gbc.gridx = 0;
        gbc.gridy = y;
        gbc.gridwidth = 1;
        JLabel label = new JLabel(labelText);
        label.setFont(new Font("Arial", Font.BOLD, 16));
        panel.add(label, gbc);
        
        gbc.gridx = 1;
        gbc.gridy = y;
        gbc.gridwidth = 1;
        component.setFont(new Font("Arial", Font.PLAIN, 16));
        panel.add(component, gbc);
    }
    
    private void styleButton(JButton button, Color bgColor) {
        // ... (implementation is correct) ...
        button.setFont(new Font("Arial", Font.BOLD, 18));
        button.setBackground(bgColor);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(12, 30, 12, 30));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    /** Inserts the new product details into the 'Products' table. */
    private void addProduct() {
        String name = nameField.getText().trim();
        String details = detailsArea.getText().trim();
        String priceText = priceField.getText().trim();
        String stockText = stockField.getText().trim();
        String imagePath = imagePathField.getText().trim();
        String category = (String) categoryComboBox.getSelectedItem();
        
        if (name.isEmpty() || priceText.isEmpty() || stockText.isEmpty() || imagePath.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill in all required fields (Name, Price, Stock, Image Path).", "Missing Input", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            double price = Double.parseDouble(priceText);
            int stock = Integer.parseInt(stockText);
            
            String sql = "INSERT INTO Products (name, description, price, category, image_path, stock_quantity) " +
                          "VALUES (?, ?, ?, ?, ?, ?)";
            
            try (Connection conn = DBConnection.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                
                pstmt.setString(1, name);
                pstmt.setString(2, details);
                pstmt.setDouble(3, price);
                pstmt.setString(4, category);
                pstmt.setString(5, imagePath);
                pstmt.setInt(6, stock);
                
                int rowsAffected = pstmt.executeUpdate();
                
                if (rowsAffected > 0) {
                    JOptionPane.showMessageDialog(this, "Product '" + name + "' added successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                    
                    // ⭐️ NEW: Refresh the main product list after successful addition ⭐️
                    if (manageProductsPage != null) {
                        manageProductsPage.loadProducts();
                    }
                    
                    // Clear fields after success
                    nameField.setText("");
                    detailsArea.setText("");
                    priceField.setText("");
                    stockField.setText("");
                    imagePathField.setText("");
                    categoryComboBox.setSelectedIndex(0);
                }
                
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Database Error adding product: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace();
            }
            
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Price and Stock Quantity must be valid numbers.", "Invalid Input", JOptionPane.ERROR_MESSAGE);
        }
    }
}
