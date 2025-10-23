
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;

@SuppressWarnings("serial")
public class PaymentDetailsPage extends JFrame {

    private final JLabel totalSalesLabel = new JLabel("₹0.00");
    private final JLabel totalOrdersLabel = new JLabel("0");
    private final JLabel avgOrderValueLabel = new JLabel("₹0.00");
    
    // Hardcoded fees used for simulation (must match PaymentPage.java)
    private static final double PLATFORM_FEE = 15.00;
    private static final double DELIVERY_FEE = 100.00;
    private static final double TAX_RATE = 0.05; // 5% tax

    public PaymentDetailsPage(String adminUsername) {
        setTitle("SareeStore - Payment & Sales Dashboard");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE); 
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        getContentPane().setBackground(Color.WHITE);
        setLayout(new BorderLayout());

        // --- Header and Navigation ---
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(Color.WHITE);
        headerPanel.setBorder(new EmptyBorder(15, 30, 15, 30));

        JLabel titleLabel = new JLabel("Sales & Payment Reports", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Serif", Font.BOLD, 30));
        titleLabel.setForeground(new Color(65, 105, 225)); 
        headerPanel.add(titleLabel, BorderLayout.CENTER);
        
        JButton backBtn = new JButton("<< Back to Admin");
        styleButton(backBtn, new Color(100, 100, 100), 16);
        headerPanel.add(backBtn, BorderLayout.EAST);
        
        add(headerPanel, BorderLayout.NORTH);

        // --- Key Metrics Dashboard ---
        JPanel dashboardPanel = new JPanel(new GridLayout(1, 3, 50, 50));
        dashboardPanel.setBackground(new Color(245, 245, 245));
        dashboardPanel.setBorder(new EmptyBorder(80, 150, 80, 150));

        dashboardPanel.add(createMetricCard("Total Revenue (Estimated)", totalSalesLabel, new Color(34, 139, 34)));
        dashboardPanel.add(createMetricCard("Total Orders Placed", totalOrdersLabel, new Color(255, 69, 0)));
        dashboardPanel.add(createMetricCard("Average Order Value", avgOrderValueLabel, new Color(65, 105, 225)));

        add(dashboardPanel, BorderLayout.CENTER);
        
        // --- Action Listeners ---
        backBtn.addActionListener(e -> {
            new AdminPage(adminUsername);
            dispose();
        });

        // Load data on startup
        loadSalesData();

        setVisible(true);
    }
    
    /** Creates a visual card for a single sales metric. */
    private JPanel createMetricCard(String title, JLabel valueLabel, Color color) {
        JPanel card = new JPanel(new BorderLayout(0, 15));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(color, 3),
            new EmptyBorder(25, 25, 25, 25)
        ));

        JLabel titleLabel = new JLabel(title, SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        titleLabel.setForeground(Color.DARK_GRAY);
        card.add(titleLabel, BorderLayout.NORTH);

        valueLabel.setFont(new Font("Arial", Font.BOLD, 48));
        valueLabel.setForeground(color);
        valueLabel.setHorizontalAlignment(SwingConstants.CENTER);
        card.add(valueLabel, BorderLayout.CENTER);

        return card;
    }

    private void styleButton(JButton button, Color bgColor, int fontSize) {
        button.setFont(new Font("Arial", Font.BOLD, fontSize));
        button.setBackground(bgColor);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    /**
     * Loads and calculates sales data.
     * * NOTE: This assumes a simple model where every item in the 'Cart' table 
     * represents a confirmed order after payment is made, as a full 'Order' table 
     * was not explicitly created in the simplified flow.
     */
    private void loadSalesData() {
        // Query to get the count and sum of all items in the Cart table
        String sql = "SELECT COUNT(cart_item_id) AS total_items, SUM(price * quantity) AS subtotal " +
                     "FROM Cart"; 
        
        double subtotal = 0.0;
        int totalOrders = 0;
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            
            if (rs.next()) {
                // Fetch the total number of unique users who added items to the cart
                String ordersSql = "SELECT COUNT(DISTINCT username) AS total_orders FROM Cart";
                try (PreparedStatement ordersPstmt = conn.prepareStatement(ordersSql);
                     ResultSet ordersRs = ordersPstmt.executeQuery()) {
                    if (ordersRs.next()) {
                        totalOrders = ordersRs.getInt("total_orders");
                    }
                }
                
                subtotal = rs.getDouble("subtotal");
            }
            
            // --- Calculation ---
            double totalFees = totalOrders * (PLATFORM_FEE + DELIVERY_FEE);
            double taxAmount = subtotal * TAX_RATE;
            double finalRevenue = subtotal + totalFees + taxAmount;
            
            double avgOrderValue = (totalOrders > 0) ? finalRevenue / totalOrders : 0.0;

            // --- Update UI ---
            DecimalFormat df = new DecimalFormat("₹#,##0.00");
            
            totalSalesLabel.setText(df.format(finalRevenue));
            totalOrdersLabel.setText(String.valueOf(totalOrders));
            avgOrderValueLabel.setText(df.format(avgOrderValue));
            
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error loading sales data: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
            totalSalesLabel.setText("N/A");
            totalOrdersLabel.setText("N/A");
            avgOrderValueLabel.setText("N/A");
        }
    }
}
