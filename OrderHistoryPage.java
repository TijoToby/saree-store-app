import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.sql.*;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import javax.swing.table.DefaultTableModel;

@SuppressWarnings("serial")
public class OrderHistoryPage extends JFrame {

    private final String username;
    private final DecimalFormat df = new DecimalFormat("₹#,##0.00");
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MMM-yyyy");
    
    private JTable orderTable;
    private DefaultTableModel tableModel;

    public OrderHistoryPage(String username) {
        this.username = username;

        setTitle("KAYRA Saree House - My Order History");
        // Use DISPOSE_ON_CLOSE to manage resources better than EXIT_ON_CLOSE
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE); 
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        getContentPane().setBackground(new Color(245, 245, 245));
        setLayout(new BorderLayout());

        try {
            // Assuming kayra_icon.png exists in the project root
            Image iconImage = Toolkit.getDefaultToolkit().getImage("kayra_icon.png");
            this.setIconImage(iconImage);
        } catch (Exception ignored) {} 

        add(createTopBar(), BorderLayout.NORTH);
        add(createMainPanel(), BorderLayout.CENTER);
        
        loadOrderData(); 

        setVisible(true);
    }

    private JPanel createTopBar() {
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(Color.WHITE);
        topBar.setBorder(new EmptyBorder(15, 30, 15, 30));

        JLabel title = new JLabel("Order History for " + username);
        title.setFont(new Font("Serif", Font.BOLD, 30));
        title.setForeground(new Color(0, 100, 102));
        topBar.add(title, BorderLayout.WEST);

        JButton backBtn = new JButton("<< Back to Home");
        backBtn.setFont(new Font("Arial", Font.BOLD, 14));
        backBtn.setFocusPainted(false);
        backBtn.setBackground(new Color(240, 240, 240));
        // NOTE: Replace 'homePage' with the actual name of your customer home class if different
        backBtn.addActionListener(e -> { 
            // Example: new CustomerHomePage(username);
            // new homePage(username); 
            dispose();
        });
        
        JPanel nav = new JPanel(new FlowLayout(FlowLayout.RIGHT, 25, 5));
        nav.setBackground(Color.WHITE);
        nav.add(backBtn);
        topBar.add(nav, BorderLayout.EAST);

        return topBar;
    }

    private JPanel createMainPanel() {
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBorder(new EmptyBorder(20, 100, 20, 100));
        mainPanel.setBackground(new Color(245, 245, 245));

        String[] columnNames = {"Order ID", "Date", "Total Amount", "Status", "Items"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; 
            }
        };

        orderTable = new JTable(tableModel);
        orderTable.setFont(new Font("Arial", Font.PLAIN, 14));
        orderTable.setRowHeight(30);
        orderTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 16));
        orderTable.getTableHeader().setBackground(new Color(200, 200, 200));
        
        orderTable.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                if (evt.getClickCount() == 2) {
                    int row = orderTable.getSelectedRow();
                    if (row != -1 && tableModel.getRowCount() > 0 && !tableModel.getValueAt(row, 0).equals("N/A")) {
                        int orderId = (Integer) orderTable.getValueAt(row, 0); 
                        showOrderDetails(orderId); 
                    }
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(orderTable);
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        
        return mainPanel;
    }

    private void loadOrderData() {
        tableModel.setRowCount(0); 

        // ⭐️ CORRECTED SQL: Joins Orders (o) with OrderDetails (od) ⭐️
        String sql = "SELECT o.order_id, o.order_date, o.total_amount, o.status, " +
                     "COUNT(od.detail_id) AS item_count " + 
                     "FROM Orders o " +
                     "LEFT JOIN OrderDetails od ON o.order_id = od.order_id " + // Use OrderDetails for historical items
                     "WHERE o.username = ? " +
                     "GROUP BY o.order_id, o.order_date, o.total_amount, o.status " + 
                     "ORDER BY o.order_date DESC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, username);
            ResultSet rs = ps.executeQuery();

            if (!rs.isBeforeFirst()) {
                tableModel.addRow(new Object[]{"N/A", "N/A", "N/A", "No orders placed yet.", "N/A"});
            } else {
                while (rs.next()) {
                    int id = rs.getInt("order_id");
                    Timestamp date = rs.getTimestamp("order_date");
                    double total = rs.getDouble("total_amount");
                    String status = rs.getString("status");
                    int itemCount = rs.getInt("item_count"); 

                    tableModel.addRow(new Object[]{
                        id,
                        date != null ? dateFormat.format(date) : "N/A",
                        df.format(total),
                        status,
                        itemCount + " Items"
                    });
                }
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Database Error loading orders: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }
    
    private void showOrderDetails(int orderId) {
        // This makes the dialog visible
        new OrderDetailsDialog(this, orderId).setVisible(true); 
    }
}