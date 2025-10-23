import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.*;
import java.text.DecimalFormat;

@SuppressWarnings("serial")
public class AdminProductManagementPage extends JFrame {

    private final String adminUsername;
    private JTable orderTable;
    private DefaultTableModel tableModel;
    private final DecimalFormat df = new DecimalFormat("₹#,##0.00");

    // Define all possible order statuses (Must be declared before statusComboBox)
    private final String[] statuses = {"Processing", "Shipped", "Delivered", "Cancelled"};

    // Input fields for status update
    private final JTextField orderIdField = new JTextField(10);
    // ⭐️ FIX: Initialize the final field statusComboBox directly at declaration ⭐️
    private final JComboBox<String> statusComboBox = new JComboBox<>(statuses); 

    public AdminProductManagementPage(String adminUsername) {
        this.adminUsername = adminUsername;

        setTitle("SareeStore Admin - Order Management");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        getContentPane().setBackground(new Color(245, 245, 245));
        setLayout(new BorderLayout());

        add(createTopBar(), BorderLayout.NORTH);
        
        JPanel mainPanel = new JPanel(new BorderLayout(20, 0));
        mainPanel.setBorder(new EmptyBorder(20, 50, 20, 50));
        mainPanel.setBackground(new Color(245, 245, 245));
        
        mainPanel.add(createOrdersTablePanel(), BorderLayout.CENTER);
        mainPanel.add(createStatusUpdatePanel(), BorderLayout.EAST);
        
        add(mainPanel, BorderLayout.CENTER);
        
        loadOrders();
        setVisible(true);
    }

    private JPanel createTopBar() {
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(Color.WHITE);
        topBar.setBorder(new EmptyBorder(15, 30, 15, 30));

        JLabel logo = new JLabel("Order Management Dashboard");
        logo.setFont(new Font("Serif", Font.BOLD, 30));
        logo.setForeground(new Color(153, 50, 204));
        topBar.add(logo, BorderLayout.WEST);

        JButton backBtn = new JButton("<< Back to Admin Home");
        styleButton(backBtn, new Color(200, 200, 200));
        backBtn.addActionListener(e -> {
            new AdminPage(adminUsername); 
            dispose();
        });

        topBar.add(backBtn, BorderLayout.EAST);
        return topBar;
    }
    
    private void styleButton(JButton button, Color bgColor) {
        button.setFont(new Font("Arial", Font.BOLD, 14));
        button.setBackground(bgColor);
        button.setForeground(Color.BLACK);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }
    
    private JPanel createOrdersTablePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);

        String[] columnNames = {"Order ID", "User", "Date", "Total", "Status", "Payment Method"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Make all cells non-editable
            }
        };
        
        orderTable = new JTable(tableModel);
        orderTable.setFont(new Font("Arial", Font.PLAIN, 14));
        orderTable.setRowHeight(25);
        
        // Listener to populate the update fields when a row is clicked
        orderTable.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                int selectedRow = orderTable.getSelectedRow();
                if (selectedRow != -1) {
                    orderIdField.setText(tableModel.getValueAt(selectedRow, 0).toString());
                    String currentStatus = tableModel.getValueAt(selectedRow, 4).toString();
                    statusComboBox.setSelectedItem(currentStatus);
                }
            }
        });
        
        JScrollPane scrollPane = new JScrollPane(orderTable);
        panel.add(scrollPane, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createStatusUpdatePanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createTitledBorder("Update Order Status"));
        
        // Order ID Field (display only)
        JPanel idPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        idPanel.setBackground(Color.WHITE);
        idPanel.add(new JLabel("Order ID:"));
        orderIdField.setEditable(false);
        orderIdField.setFont(new Font("Arial", Font.BOLD, 14));
        idPanel.add(orderIdField);
        
        // Status Dropdown
        JPanel statusPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        statusPanel.setBackground(Color.WHITE);
        // ⭐️ IMPORTANT: Initialization removed here as it's done at declaration ⭐️
        statusPanel.add(new JLabel("New Status:"));
        statusPanel.add(statusComboBox);
        
        // Update Button
        JButton updateBtn = new JButton("Update Status");
        updateBtn.setFont(new Font("Arial", Font.BOLD, 16));
        updateBtn.setBackground(new Color(65, 105, 225));
        updateBtn.setForeground(Color.WHITE);
        updateBtn.addActionListener(e -> updateOrderStatus());

        panel.add(idPanel);
        panel.add(Box.createVerticalStrut(10));
        panel.add(statusPanel);
        panel.add(Box.createVerticalStrut(20));
        panel.add(updateBtn);
        panel.add(Box.createVerticalGlue()); 
        
        return panel;
    }

    // --- Database Logic ---

    private void loadOrders() {
        tableModel.setRowCount(0); // Clear existing data

        String sql = "SELECT order_id, username, order_date, total_amount, status, payment_method FROM Orders ORDER BY order_id DESC";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            
            while (rs.next()) {
                tableModel.addRow(new Object[]{
                    rs.getInt("order_id"),
                    rs.getString("username"),
                    rs.getDate("order_date"),
                    df.format(rs.getDouble("total_amount")),
                    rs.getString("status"),
                    rs.getString("payment_method")
                });
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error loading orders: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

    private void updateOrderStatus() {
        if (orderIdField.getText().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please select an order from the table first.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int orderId = Integer.parseInt(orderIdField.getText());
        String newStatus = (String) statusComboBox.getSelectedItem();

        String sql = "UPDATE Orders SET status = ? WHERE order_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, newStatus);
            pstmt.setInt(2, orderId);

            int rowsAffected = pstmt.executeUpdate();

            if (rowsAffected > 0) {
                JOptionPane.showMessageDialog(this, "Order #" + orderId + " status updated to '" + newStatus + "' successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                loadOrders(); // Refresh the table
                orderIdField.setText("");
                statusComboBox.setSelectedIndex(0);
            } else {
                JOptionPane.showMessageDialog(this, "Order ID not found or status already set.", "Update Failed", JOptionPane.ERROR_MESSAGE);
            }

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Database error during status update: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }
}
