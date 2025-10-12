

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

@SuppressWarnings("serial")
public class AdminPaymentDetailsPage extends JFrame {

    private final String adminUsername;
    private JTable paymentTable;
    private DefaultTableModel tableModel;

    public AdminPaymentDetailsPage(String adminUsername) {
        this.adminUsername = adminUsername;

        setTitle("SareeStore Admin - Payment Details");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        getContentPane().setBackground(new Color(245, 245, 245));
        setLayout(new BorderLayout());

        JPanel topBar = createTopBar();
        add(topBar, BorderLayout.NORTH);

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBorder(new EmptyBorder(20, 50, 20, 50));
        mainPanel.setBackground(new Color(245, 245, 245));
        
        // Setup table model
        String[] columnNames = {"Order ID (Price)", "Username", "Status", "Item Name", "Quantity", "Total Price (₹)"};
        tableModel = new DefaultTableModel(columnNames, 0);
        paymentTable = new JTable(tableModel);
        
        // Styling the table
        paymentTable.setFont(new Font("Arial", Font.PLAIN, 14));
        paymentTable.setRowHeight(25);
        paymentTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 14));
        
        JScrollPane scrollPane = new JScrollPane(paymentTable);
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        add(mainPanel, BorderLayout.CENTER);

        loadPaymentDetails();

        setVisible(true);
    }
    
    // --- UI Creation Methods ---

    private JPanel createTopBar() {
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(Color.WHITE);
        topBar.setBorder(new EmptyBorder(15, 30, 15, 30));

        JLabel logo = new JLabel("Admin Dashboard - Order History");
        logo.setFont(new Font("Serif", Font.BOLD, 30));
        logo.setForeground(new Color(65, 105, 225));
        topBar.add(logo, BorderLayout.WEST);

        JButton backBtn = new JButton("<< Back to Admin Home");
        styleButton(backBtn, new Color(200, 200, 200));
        backBtn.addActionListener(e -> {
            // This assumes you have an AdminPage class for navigation
            new AdminPage(adminUsername); 
            dispose();
        });

        JPanel navButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 25, 5));
        navButtons.setBackground(Color.WHITE);
        navButtons.add(backBtn);
        topBar.add(navButtons, BorderLayout.EAST);

        return topBar;
    }

    private void styleButton(JButton button, Color bgColor) {
        button.setFont(new Font("Arial", Font.BOLD, 16));
        button.setBackground(bgColor);
        button.setForeground(Color.BLACK);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(10, 25, 10, 25));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    // --- Database Logic ---

    /**
     * Loads all finalized order details from the Cart table.
     * Orders are grouped by final_price to simulate distinct orders.
     */
    private void loadPaymentDetails() {
        // Clear existing data
        tableModel.setRowCount(0); 

        // SQL to fetch all items that belong to a finalized order (final_price is NOT NULL)
        String sql = "SELECT c.username, c.final_price, c.status, p.name, c.quantity " +
                     "FROM Cart c JOIN Products p ON c.product_id = p.product_id " +
                     "WHERE c.final_price IS NOT NULL " + 
                     "ORDER BY c.final_price DESC, c.username"; // Sort to group items by order/user

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            if (!rs.isBeforeFirst()) {
                JOptionPane.showMessageDialog(this, "No finalized orders found.", "Information", JOptionPane.INFORMATION_MESSAGE);
                return;
            }

            double lastFinalPrice = -1;
            String lastUsername = "";

            while (rs.next()) {
                String currentUsername = rs.getString("username");
                double currentFinalPrice = rs.getDouble("final_price");
                String status = rs.getString("status");
                String itemName = rs.getString("name");
                int quantity = rs.getInt("quantity");

                // Logic to visually group items belonging to the same single final transaction
                String orderIdDisplay;
                if (currentFinalPrice != lastFinalPrice || !currentUsername.equals(lastUsername)) {
                    // This is the first item of a new 'order'
                    orderIdDisplay = "Order #" + (int) currentFinalPrice; // Simple ID
                    lastFinalPrice = currentFinalPrice;
                    lastUsername = currentUsername;
                } else {
                    // Subsequent item of the same 'order'
                    orderIdDisplay = "";
                    status = ""; // Display status only on the first line
                    currentFinalPrice = 0.0; // Don't repeat total price
                }

                // Add row to the table
                tableModel.addRow(new Object[]{
                    orderIdDisplay,
                    currentUsername,
                    status,
                    itemName,
                    quantity,
                    currentFinalPrice > 0 ? String.format("%.2f", currentFinalPrice) : ""
                });
            }

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, 
                "Database Error loading order details: " + ex.getMessage(), 
                "DB Error", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }
}