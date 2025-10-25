import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

@SuppressWarnings("serial")
public class AdminPaymentDetailsPage extends JFrame {

    private final String adminUsername;
    private JTable paymentTable;
    private DefaultTableModel tableModel;

    public AdminPaymentDetailsPage(String adminUsername) {
        this.adminUsername = adminUsername;

        setTitle("SareeStore Admin - Order History");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE); 
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        getContentPane().setBackground(new Color(245, 245, 245));
        setLayout(new BorderLayout());

        JPanel topBar = createTopBar();
        add(topBar, BorderLayout.NORTH);

        // --- Main Table Panel Setup ---
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBorder(new EmptyBorder(20, 50, 20, 50));
        mainPanel.setBackground(new Color(245, 245, 245));
        
        // FIX: Column names changed to reflect a single-line summary
        String[] columnNames = {"Order ID", "Username", "Status", "Item Summary", "Total Price (₹)"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Table should be read-only
            }
        };
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
    
    // UI Creation Methods (Same as before)
    private JPanel createTopBar() {
        // ... (Code for createTopBar remains the same)
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(Color.WHITE);
        topBar.setBorder(new EmptyBorder(15, 30, 15, 30));

        JLabel logo = new JLabel("Admin Dashboard - Order History");
        logo.setFont(new Font("Serif", Font.BOLD, 30));
        logo.setForeground(new Color(65, 105, 225));
        topBar.add(logo, BorderLayout.WEST);

        JPanel navButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 5));
        navButtons.setBackground(Color.WHITE);
        
        JButton refreshBtn = new JButton("🔄 Refresh Data");
        styleButton(refreshBtn, new Color(25, 120, 190)); 
        refreshBtn.addActionListener(e -> {
            loadPaymentDetails();
            JOptionPane.showMessageDialog(this, "Order history refreshed successfully!", "Refreshed", JOptionPane.INFORMATION_MESSAGE);
        });
        
        JButton backBtn = new JButton("<< Back to Admin Home");
        styleButton(backBtn, new Color(200, 200, 200));
        backBtn.addActionListener(e -> {
            // Assuming AdminPage exists
             // new AdminPage(adminUsername).setVisible(true); 
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
        Color fgColor = (bgColor.getRed() < 128 && bgColor.getGreen() < 128 && bgColor.getBlue() < 128) ? Color.WHITE : Color.BLACK;
        button.setForeground(fgColor);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(10, 25, 10, 25));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }


    // ---------------------------------
    // --- Database Logic (FINAL CORRECTED) ---
    // ---------------------------------

    /**
     * FIX: Loads all orders, aggregates item quantities, and then combines all items 
     * into a single 'Item Summary' string per order ID.
     */
    public void loadPaymentDetails() { 
        tableModel.setRowCount(0); 
        
        // Map to hold aggregated data: Key=OrderID, Value=OrderSummaryData
        // TreeMap keeps the orders sorted (optional, but good practice for display)
        Map<Integer, OrderSummaryData> orderMap = new TreeMap<>((a, b) -> b.compareTo(a)); // Descending order

        // SQL: Gets all item line data for ALL orders, but uses aggregation to prevent 
        // duplicate line items from cart logic errors (e.g., Order #27).
        String sql = "SELECT o.order_id, o.username, o.status, o.total_amount, " +
                     "p.name AS item_name, " +
                     "SUM(oi.quantity) AS TotalQuantity " + // Aggregated Quantity
                     "FROM orders o " +
                     "LEFT JOIN orderitems oi ON o.order_id = oi.order_id " +
                     "LEFT JOIN products p ON oi.product_id = p.product_id " +
                     "GROUP BY o.order_id, o.username, o.status, o.total_amount, p.name " + // Group by all non-aggregated columns
                     "ORDER BY o.order_id DESC, p.name ASC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            if (!rs.isBeforeFirst()) {
                JOptionPane.showMessageDialog(this, "No orders found.", "Information", JOptionPane.INFORMATION_MESSAGE);
                return;
            }

            // 1. ITERATE OVER RESULTS AND AGGREGATE ITEMS BY ORDER ID
            while (rs.next()) {
                int orderId = rs.getInt("order_id");
                String itemName = rs.getString("item_name");
                int totalQuantity = rs.getInt("TotalQuantity"); 
                
                OrderSummaryData data = orderMap.get(orderId);
                
                if (data == null) {
                    // New Order ID found - initialize OrderSummaryData
                    data = new OrderSummaryData(
                        orderId,
                        rs.getString("username"),
                        rs.getString("status"),
                        rs.getDouble("total_amount")
                    );
                    orderMap.put(orderId, data);
                }
                
                // Add the item to the order's item list
                if (itemName != null) {
                    data.addItem(itemName, totalQuantity);
                }
            }
            
            // 2. ITERATE OVER THE AGGREGATED MAP AND POPULATE THE TABLE
            for (OrderSummaryData data : orderMap.values()) {
                tableModel.addRow(new Object[]{
                    "Order #" + data.orderId,
                    data.username,
                    data.status,
                    data.getItemSummaryString(), // FIX: This is the single-line summary
                    String.format("₹%.2f", data.totalAmount)
                });
            }

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, 
                "Database Error loading order details. Check DBConnection and table structure: " + ex.getMessage(), 
                "DB Error", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }
    
    // ---------------------------------
    // --- Helper Class for Aggregation ---
    // ---------------------------------
    
    /**
     * Helper class to hold and aggregate all item data for a single order, 
     * allowing it to be condensed into a single summary string.
     */
    private static class OrderSummaryData {
        public final int orderId;
        public final String username;
        public final String status;
        public final double totalAmount;
        // Map to store item name and its aggregated quantity
        private final Map<String, Integer> items = new HashMap<>();

        public OrderSummaryData(int orderId, String username, String status, double totalAmount) {
            this.orderId = orderId;
            this.username = username;
            this.status = status;
            this.totalAmount = totalAmount;
        }
        
        public void addItem(String itemName, int quantity) {
            // This method should not be needed if SQL aggregation works, but acts as a safeguard
            items.put(itemName, items.getOrDefault(itemName, 0) + quantity); 
        }

        public String getItemSummaryString() {
            if (items.isEmpty()) {
                return "No Item Data Found";
            }
            
            StringBuilder sb = new StringBuilder();
            boolean first = true;
            for (Map.Entry<String, Integer> entry : items.entrySet()) {
                if (!first) {
                    sb.append(", ");
                }
                // Format: Item Name (xQuantity)
                sb.append(entry.getKey()).append(" (x").append(entry.getValue()).append(")");
                first = false;
            }
            return sb.toString();
        }
    }
    
    /**
     * Placeholder for DBConnection. Replace this with your actual database connection utility.
     */
    private static class DBConnection {
        public static Connection getConnection() throws SQLException {
            // Replace with your actual connection details
            throw new UnsupportedOperationException("DBConnection.getConnection() not implemented. Provide your actual implementation.");
        }
    }
}
