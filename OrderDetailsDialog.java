import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.sql.*;
import java.text.DecimalFormat;
import javax.swing.table.DefaultTableModel;

@SuppressWarnings("serial")
public class OrderDetailsDialog extends JDialog {

    private final int orderId;
    private final DecimalFormat df = new DecimalFormat("₹#,##0.00");

    public OrderDetailsDialog(JFrame parent, int orderId) {
        super(parent, "Order Details - #" + orderId, true); 
        this.orderId = orderId;
        
        setSize(800, 600);
        setLayout(new BorderLayout());
        getContentPane().setBackground(new Color(245, 245, 245));
        setLocationRelativeTo(parent); 
        
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(new EmptyBorder(20, 30, 20, 30));
        mainPanel.setBackground(new Color(245, 245, 245));
        
        loadAndDisplayDetails(mainPanel);

        JScrollPane scrollPane = new JScrollPane(mainPanel);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        add(scrollPane, BorderLayout.CENTER);

        setVisible(true);
    }

    private void loadAndDisplayDetails(JPanel mainPanel) {
        OrderSummary summary = loadOrderSummary();
        
        if (summary == null) {
            mainPanel.add(new JLabel("Could not load summary details for Order ID: " + orderId));
            return;
        }

        mainPanel.add(createSummaryPanel(summary));
        mainPanel.add(Box.createVerticalStrut(20));
        mainPanel.add(createShippingPanel(summary));
        mainPanel.add(Box.createVerticalStrut(20));

        mainPanel.add(createItemsPanel());
    }
    
    // --- Data Class (Simplified to match Orders table) ---

    private static class OrderSummary {
        String status;
        String paymentMethod;
        double totalAmount;
        String shippingName;
        String shippingState; // Only includes the state, not full address components
    }

    // --- Database Methods ---
    
    private OrderSummary loadOrderSummary() {
        // ⭐️ CORRECTED SQL: Only selects columns guaranteed to be in the Orders table ⭐️
        String sql = "SELECT total_amount, status, payment_method, shipping_name, shipping_state " +
                     "FROM Orders WHERE order_id = ?";
        OrderSummary summary = null;
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, orderId);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                summary = new OrderSummary();
                summary.totalAmount = rs.getDouble("total_amount");
                summary.status = rs.getString("status");
                summary.paymentMethod = rs.getString("payment_method");
                summary.shippingName = rs.getString("shipping_name");
                summary.shippingState = rs.getString("shipping_state");
                // The full address (street, city, zip) is likely NOT stored in Orders
            }
        } catch (SQLException ex) {
            System.err.println("Error loading order summary: " + ex.getMessage());
        }
        return summary;
    }
    
    // --- UI Methods ---

    private JPanel createSummaryPanel(OrderSummary summary) {
        JPanel panel = new JPanel(new GridLayout(3, 2, 10, 10));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createTitledBorder("Order Overview"));
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 150));
        
        // Total Paid
        panel.add(new JLabel("Total Paid:"));
        JLabel totalLabel = new JLabel(df.format(summary.totalAmount));
        totalLabel.setFont(new Font("Arial", Font.BOLD, 16));
        totalLabel.setForeground(new Color(255, 69, 0));
        panel.add(totalLabel);

        // Status
        panel.add(new JLabel("Status:"));
        JLabel statusLabel = new JLabel(summary.status);
        statusLabel.setFont(new Font("Arial", Font.BOLD, 16));
        statusLabel.setForeground(summary.status.contains("Paid") || summary.status.equals("Completed") ? new Color(34, 139, 34) : Color.ORANGE);
        panel.add(statusLabel);
        
        // Payment Method
        panel.add(new JLabel("Payment Method:"));
        panel.add(new JLabel(summary.paymentMethod));

        return panel;
    }

    private JPanel createShippingPanel(OrderSummary summary) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createTitledBorder("Shipping Details"));
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 150));

        JTextArea addressArea = new JTextArea();
        addressArea.setEditable(false);
        addressArea.setLineWrap(true);
        addressArea.setWrapStyleWord(true);
        addressArea.setFont(new Font("Arial", Font.PLAIN, 14));
        addressArea.setBackground(Color.WHITE);
        addressArea.setBorder(new EmptyBorder(10, 10, 10, 10));

        // NOTE: Since Orders only stores name and state, we simplify the display.
        // The full address components (street, city, zip) must be fetched from the Address table
        // if they are needed, but for now, we display what's available.
        String addressText = String.format("Recipient: %s\nShipping State: %s\n\n(Full address details not stored in Order record.)",
            summary.shippingName,
            summary.shippingState);
        
        addressArea.setText(addressText);
        panel.add(addressArea, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createItemsPanel() {
        JPanel container = new JPanel(new BorderLayout());
        container.setBackground(Color.WHITE);
        container.setBorder(BorderFactory.createTitledBorder("Items Purchased"));

        String[] columnNames = {"Product", "Quantity", "Price Per Unit", "Subtotal"};
        DefaultTableModel model = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        JTable itemsTable = new JTable(model);
        itemsTable.setFillsViewportHeight(true);
        itemsTable.setRowHeight(25);

        // ⭐️ CORRECTED SQL: Join OrderDetails (od) with Products (p) ⭐️
        String sql = "SELECT p.name, od.quantity, od.price_at_purchase " +
                     "FROM OrderDetails od JOIN Products p ON od.product_id = p.product_id " +
                     "WHERE od.order_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, orderId);
            ResultSet rs = ps.executeQuery();
            
            while (rs.next()) {
                String name = rs.getString("name");
                double unitPriceAtPurchase = rs.getDouble("price_at_purchase"); 
                int quantity = rs.getInt("quantity");
                
                // Calculate subtotal
                double finalSubtotal = unitPriceAtPurchase * quantity; 

                model.addRow(new Object[]{
                    name,
                    quantity,
                    df.format(unitPriceAtPurchase),
                    df.format(finalSubtotal)
                });
            }
        } catch (SQLException ex) {
            System.err.println("Error loading order items: " + ex.getMessage());
            model.addRow(new Object[]{"DB Error", "Failed to load items. Check OrderDetails table.", "", ""});
        }
        
        container.add(new JScrollPane(itemsTable), BorderLayout.CENTER);
        return container;
    }
}