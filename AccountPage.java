import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SuppressWarnings("serial")
public class AccountPage extends JFrame {

    private final String username;
    private JLabel currentAddressLabel;
    private JPanel orderHistoryContentPanel;
    private final DecimalFormat df = new DecimalFormat("₹#,##0.00");
    private final String adminUsername = ""; // Unused but kept as it was in the original code

    public AccountPage(String username) {
        super("KAYRA Saree House - My Account");
        this.username = username;
        // Removed redundant setTitle and JFrame setup as BaseFrame handles it
        // Note: Keeping setExtendedState here as it might override BaseFrame's setting
        setExtendedState(JFrame.MAXIMIZED_BOTH); 
        getContentPane().setBackground(new Color(245, 245, 245));
        setLayout(new BorderLayout());

        add(createTopBar(), BorderLayout.NORTH);

        JPanel mainPanel = new JPanel(new GridLayout(1, 2, 50, 0));
        mainPanel.setBorder(new EmptyBorder(30, 80, 50, 80));
        mainPanel.setBackground(new Color(245, 245, 245));
        
        mainPanel.add(createProfileManagementPanel());
        mainPanel.add(createOrderHistoryPanel());
        
        add(mainPanel, BorderLayout.CENTER);

        loadUserData();
        displayOrderHistory();

        setVisible(true);
    }
    
    // --- UI Creation Methods (Same as before) ---
    
    private JPanel createTopBar() {
        // ... (Code for createTopBar remains the same) ...
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(Color.WHITE);
        topBar.setBorder(new EmptyBorder(15, 30, 15, 30));

        JLabel logo = new JLabel("My Account");
        logo.setFont(new Font("Serif", Font.BOLD, 30));
        logo.setForeground(new Color(140, 40, 80));
        topBar.add(logo, BorderLayout.WEST);
        
        JButton backBtn = new JButton("<< Back to Store");
        backBtn.setFont(new Font("Arial", Font.BOLD, 14));
        backBtn.setBackground(new Color(200, 200, 200));
        backBtn.setForeground(Color.BLACK);
        backBtn.setFocusPainted(false);
        backBtn.addActionListener(e -> {
            // Assuming your home page class is named homePage
            // This is safe because homePage takes the username, not adminUsername.
            new homePage(username); 
            dispose();
        });

        topBar.add(backBtn, BorderLayout.EAST);
        return topBar;
    }
    
    private JPanel createProfileManagementPanel() {
        // ... (Code for createProfileManagementPanel remains the same) ...
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(140, 40, 80)),
            "Profile Details", TitledBorder.LEFT, TitledBorder.TOP,
            new Font("Arial", Font.BOLD, 18), new Color(140, 40, 80)
        ));
        panel.add(Box.createVerticalStrut(10));

        JLabel userLabel = new JLabel("Username: " + username);
        userLabel.setFont(new Font("Arial", Font.BOLD, 16));
        userLabel.setBorder(new EmptyBorder(10, 10, 10, 10));
        userLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(userLabel);
        
        currentAddressLabel = new JLabel("Address: Loading...");
        currentAddressLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        currentAddressLabel.setBorder(new EmptyBorder(10, 10, 20, 10));
        currentAddressLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(currentAddressLabel);

        JButton updateAddressBtn = new JButton("Update Shipping Address");
        updateAddressBtn.setFont(new Font("Arial", Font.BOLD, 14));
        updateAddressBtn.setBackground(new Color(65, 105, 225));
        updateAddressBtn.setForeground(Color.WHITE);
        updateAddressBtn.setFocusPainted(false);
        updateAddressBtn.setBorder(new EmptyBorder(10, 20, 10, 20));
        updateAddressBtn.addActionListener(e -> {
            JOptionPane.showMessageDialog(this, "Address update feature coming soon!", "Feature Alert", JOptionPane.INFORMATION_MESSAGE);
        });
        updateAddressBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(updateAddressBtn);
        panel.add(Box.createVerticalGlue()); 

        return panel;
    }
    
    private JPanel createOrderHistoryPanel() {
        // ... (Code for createOrderHistoryPanel remains the same) ...
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(140, 40, 80)),
            "Order History", TitledBorder.LEFT, TitledBorder.TOP,
            new Font("Arial", Font.BOLD, 18), new Color(140, 40, 80)
        ));

        orderHistoryContentPanel = new JPanel();
        orderHistoryContentPanel.setLayout(new BoxLayout(orderHistoryContentPanel, BoxLayout.Y_AXIS));
        orderHistoryContentPanel.setBackground(Color.WHITE);
        orderHistoryContentPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        
        JScrollPane scrollPane = new JScrollPane(orderHistoryContentPanel);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }

    // --- Data Loading Methods (Same as before) ---

    private void loadUserData() {
        // ... (Code for loadUserData remains the same) ...
        String sql = "SELECT address FROM users WHERE username = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                String address = rs.getString("address");
                currentAddressLabel.setText("Address: " + (address == null || address.isEmpty() ? "Not Set" : address));
            }

        } catch (SQLException ex) {
            currentAddressLabel.setText("Address: Database Error");
            System.err.println("Error loading user data: " + ex.getMessage());
        }
    }
    
    public void displayOrderHistory() {
        // ... (Code for displayOrderHistory remains the same) ...
        orderHistoryContentPanel.removeAll();
        orderHistoryContentPanel.setLayout(new BoxLayout(orderHistoryContentPanel, BoxLayout.Y_AXIS));

        Map<Integer, Boolean> feedbackStatusMap = checkExistingFeedback(); 

        String sql = "SELECT order_id, order_date, total_amount, status FROM orders WHERE username = ? ORDER BY order_id DESC";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();

            boolean ordersFound = false;
            
            while (rs.next()) {
                ordersFound = true;
                int orderId = rs.getInt("order_id");
                String date = rs.getString("order_date");
                double total = rs.getDouble("total_amount");
                String status = rs.getString("status");
                
                boolean hasFeedback = feedbackStatusMap.getOrDefault(orderId, false);

                JPanel orderRow = createOrderRow(orderId, date, total, status, hasFeedback);
                orderHistoryContentPanel.add(orderRow);
                orderHistoryContentPanel.add(Box.createVerticalStrut(15)); 
            }

            if (!ordersFound) {
                JLabel noOrdersLabel = new JLabel("You have no past orders.", SwingConstants.CENTER);
                noOrdersLabel.setFont(new Font("Arial", Font.ITALIC, 16));
                noOrdersLabel.setBorder(new EmptyBorder(30, 0, 30, 0));
                orderHistoryContentPanel.add(noOrdersLabel);
            }

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error loading order history: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
        
        orderHistoryContentPanel.revalidate();
        orderHistoryContentPanel.repaint();
    }


    /**
     * Creates a single JPanel row for an order, including the Feedback button logic.
     */
    private JPanel createOrderRow(int orderId, String date, double total, String status, boolean hasFeedback) {
        JPanel row = new JPanel(new BorderLayout(20, 0));
        row.setBackground(new Color(250, 250, 250));
        row.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(220, 220, 220)),
            new EmptyBorder(10, 15, 10, 15)
        ));

        // --- Left Panel (ID and Date) ---
        JPanel idDatePanel = new JPanel(new GridLayout(2, 1));
        idDatePanel.setBackground(new Color(250, 250, 250));
        JLabel idLabel = new JLabel("Order #" + orderId);
        idLabel.setFont(new Font("Arial", Font.BOLD, 16));
        JLabel dateLabel = new JLabel("Date: " + date.split(" ")[0]); 
        idDatePanel.add(idLabel);
        idDatePanel.add(dateLabel);
        row.add(idDatePanel, BorderLayout.WEST);

        // --- Center Panel (Details and Status) ---
        JPanel centerPanel = new JPanel(new GridLayout(2, 1));
        centerPanel.setBackground(new Color(250, 250, 250));
        
        // Items Label is now fetching the actual item names!
        JLabel itemsLabel = new JLabel("Items: " + getOrderSummary(orderId)); 
        
        // Status Label styling
        JLabel statusLabel = new JLabel("Status: " + status);
        statusLabel.setFont(new Font("Arial", Font.BOLD, 14));
        
        if (status.equals("Delivered")) {
            statusLabel.setForeground(new Color(34, 139, 34)); // Green
        } else if (status.equals("Cancelled")) {
            statusLabel.setForeground(Color.RED);
        } else {
            statusLabel.setForeground(new Color(255, 140, 0)); // Orange
        }

        centerPanel.add(itemsLabel);
        centerPanel.add(statusLabel);
        row.add(centerPanel, BorderLayout.CENTER);

        // --- Right Panel (Total and Action Button) ---
        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        rightPanel.setBackground(new Color(250, 250, 250));
        
        JLabel totalLabel = new JLabel(df.format(total));
        totalLabel.setFont(new Font("Arial", Font.BOLD, 18));
        rightPanel.add(totalLabel);

        // ⭐️ Feedback Button Logic (UPDATED) ⭐️
        if (status.equals("Delivered")) {
            if (!hasFeedback) {
                JButton feedbackBtn = new JButton("Leave Feedback");
                feedbackBtn.setBackground(new Color(65, 105, 225)); // Royal Blue
                feedbackBtn.setForeground(Color.WHITE);
                feedbackBtn.setFocusPainted(false);
                feedbackBtn.addActionListener(e -> {
                    // Fetch the list of items to pass to the FeedbackPage
                    Map<Integer, String> items = getOrderItemsForFeedback(orderId);
                    
                    if (items.isEmpty()) {
                         JOptionPane.showMessageDialog(this, 
                             "Error: No items found in Order #" + orderId + ". Cannot leave feedback.", 
                             "Data Missing", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                    
                    // Launch the FeedbackPage, passing the map of items for review
                    new FeedbackPage(username, orderId, items, this); 
                    // You might want to keep the account page open or refresh it,
                    // but following your original dispose logic:
                    dispose(); 
                });
                rightPanel.add(feedbackBtn);
            } else {
                JLabel reviewedLabel = new JLabel("Reviewed ✔️");
                reviewedLabel.setForeground(new Color(34, 139, 34));
                reviewedLabel.setFont(new Font("Arial", Font.ITALIC, 14));
                rightPanel.add(reviewedLabel);
            }
        }
        
        row.add(rightPanel, BorderLayout.EAST);
        return row;
    }

    /**
     * Retrieves a summary of items in the order from the OrderItems table. (This already works)
     */
   private String getOrderSummary(int orderId) {
        // SQL query now selects oi.product_id
        String itemSql = "SELECT oi.quantity, oi.product_id, p.name FROM orderitems oi " +
                         "JOIN products p ON oi.product_id = p.product_id " +
                         "WHERE oi.order_id = ?";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(itemSql)) {
            
            pstmt.setInt(1, orderId);
            ResultSet rs = pstmt.executeQuery();
            
            if (!rs.isBeforeFirst()) {
                // If this is returned, it confirms the orderitems table has no entry for this order ID.
                return "[Details unavailable]"; 
            }
            
            StringBuilder summary = new StringBuilder();
            while (rs.next()) {
                // ⭐️ UPDATED: Include Product ID in the display ⭐️
                summary.append(rs.getString("name"))
                       .append(" (ID: ").append(rs.getInt("product_id")) // Added Product ID
                       .append(", x").append(rs.getInt("quantity")).append("), ");
            }
            // Remove the trailing comma and space
            return summary.substring(0, summary.length() - 2); 
        } catch (SQLException e) {
            System.err.println("Error getting order summary for ID " + orderId + ": " + e.getMessage());
            return "[Error loading details]";
        }
    }
    /**
     * ⭐️ NEW METHOD ⭐️
     * Retrieves the Product ID and Name for all items in an order. 
     * This is passed to the FeedbackPage to let the user select/confirm which item to review.
     */
    private Map<Integer, String> getOrderItemsForFeedback(int orderId) {
        Map<Integer, String> items = new HashMap<>();
        // Query to get the product ID and name for all items in the order
        String sql = "SELECT oi.product_id, p.name " +
                     "FROM orderitems oi " +
                     "JOIN products p ON oi.product_id = p.product_id " +
                     "WHERE oi.order_id = ?";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, orderId);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                items.put(rs.getInt("product_id"), rs.getString("name"));
            }
        } catch (SQLException e) {
            System.err.println("Error fetching review items for Order ID " + orderId + ": " + e.getMessage());
            // An empty map will trigger the error message in createOrderRow
        }
        return items;
    }


    /**
     * Checks the Feedback table to see which orders the user has already reviewed.
     */
    private Map<Integer, Boolean> checkExistingFeedback() {
        // ... (Code for checkExistingFeedback remains the same) ...
        Map<Integer, Boolean> status = new HashMap<>();
        // NOTE: This assumes your Feedback table links a review to an ORDER_ID.
        // If you change the Feedback table to link to PRODUCT_ID, you'll need to update this query.
        String sql = "SELECT order_id FROM feedback WHERE username = ?";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                status.put(rs.getInt("order_id"), true);
            }
        } catch (SQLException ex) {
            System.err.println("Error checking existing feedback: " + ex.getMessage());
        }
        return status;
    }
}
