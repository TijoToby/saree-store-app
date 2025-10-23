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

    // Assuming DBConnection.java exists and handles database connection
    // private static final long serialVersionUID = 1L; 

    private final String adminUsername;
    private JTable paymentTable;
    private DefaultTableModel tableModel;

    public AdminPaymentDetailsPage(String adminUsername) {
        this.adminUsername = adminUsername;

        setTitle("SareeStore Admin - Payment Details");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE); 
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        getContentPane().setBackground(new Color(245, 245, 245));
        setLayout(new BorderLayout());

        JPanel topBar = createTopBar();
        add(topBar, BorderLayout.NORTH);

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBorder(new EmptyBorder(20, 50, 20, 50));
        mainPanel.setBackground(new Color(245, 245, 245));
        
        // Setup table model
        String[] columnNames = {"Order ID", "Username", "Status", "Item Name", "Quantity", "Total Price (₹)"};
        tableModel = new DefaultTableModel(columnNames, 0);
        paymentTable = new JTable(tableModel);
        
        // Styling the table
        paymentTable.setFont(new Font("Arial", Font.PLAIN, 14));
        paymentTable.setRowHeight(25);
        paymentTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 14));
        
        JScrollPane scrollPane = new JScrollPane(paymentTable);
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        add(mainPanel, BorderLayout.CENTER);

        // Load data initially
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

        // --- Navigation and Refresh Buttons ---
        JPanel navButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 5));
        navButtons.setBackground(Color.WHITE);
        
        // Refresh Button
        JButton refreshBtn = new JButton("🔄 Refresh Data");
        styleButton(refreshBtn, new Color(25, 120, 190)); 
        
        refreshBtn.addActionListener(e -> {
            loadPaymentDetails(); // Re-run the data loading method
            JOptionPane.showMessageDialog(this, "Order history refreshed successfully!", "Refreshed", JOptionPane.INFORMATION_MESSAGE);
        });
        
        JButton backBtn = new JButton("<< Back to Admin Home");
        styleButton(backBtn, new Color(200, 200, 200));
        backBtn.addActionListener(e -> {
            // This assumes you have an AdminPage class for navigation
            new AdminPage(adminUsername); 
            dispose();
        });

        navButtons.add(refreshBtn);
        navButtons.add(backBtn);
        topBar.add(navButtons, BorderLayout.EAST);

        return topBar;
    }

    private void styleButton(JButton button, Color bgColor) {
        button.setFont(new Font("Arial", Font.BOLD, 16));
        button.setBackground(bgColor);
        
        // Set foreground color based on background for contrast
        Color fgColor = (bgColor.getRed() < 128 && bgColor.getGreen() < 128 && bgColor.getBlue() < 128) ? Color.WHITE : Color.BLACK;
        button.setForeground(fgColor);
        
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(10, 25, 10, 25));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    // --- Database Logic ---

    /**
     * Loads all orders (regardless of status) and their item details by using 
     * a LEFT JOIN to ensure all orders are listed, even if item details are missing.
     */
    public void loadPaymentDetails() { 
        tableModel.setRowCount(0); 

        // SQL: Use LEFT JOIN to guarantee every row from the 'orders' table is included.
        String sql = "SELECT o.order_id, o.username, o.status, o.total_amount, " +
                     "p.name AS item_name, c.quantity " +
                     "FROM orders o " +
                     "LEFT JOIN cart c ON o.order_id = c.order_id " +
                     "LEFT JOIN products p ON c.product_id = p.product_id " +
                     "ORDER BY o.order_date DESC, o.order_id DESC"; 

        int lastOrderId = -1; 

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            if (!rs.isBeforeFirst()) {
                JOptionPane.showMessageDialog(this, "No orders found.", "Information", JOptionPane.INFORMATION_MESSAGE);
                return;
            }

            while (rs.next()) {
                int currentOrderId = rs.getInt("order_id");
                
                // Get item details. These will be NULL if no matching Cart row exists (due to LEFT JOIN).
                String itemName = rs.getString("item_name");
                int quantity = rs.getInt("quantity");

                // Default values if no item data is found for the order ID
                String displayItemName = (itemName != null) ? itemName : "No Item Data Found (Review Cart Link)";
                int displayQuantity = (itemName != null) ? quantity : 0;
                
                String username = rs.getString("username");
                String status = rs.getString("status");
                double totalAmount = rs.getDouble("total_amount");

                String orderIdDisplay;
                String statusDisplay;
                String totalDisplay;

                if (currentOrderId != lastOrderId) {
                    // First item of a new, distinct Order - show all details
                    orderIdDisplay = "Order #" + currentOrderId;
                    statusDisplay = status;
                    totalDisplay = String.format("₹%.2f", totalAmount); 
                    lastOrderId = currentOrderId;
                } else {
                    // Subsequent item of the same Order - blank out redundant info
                    orderIdDisplay = "";
                    statusDisplay = "";
                    totalDisplay = ""; 
                    // Blank out username for subsequent rows for cleaner grouping
                    username = ""; 
                }

                // Add row to the table
                tableModel.addRow(new Object[]{
                    orderIdDisplay,
                    username,
                    statusDisplay,
                    displayItemName,
                    displayQuantity,
                    totalDisplay
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
