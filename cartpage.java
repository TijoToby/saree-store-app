import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.sql.*;
import java.text.DecimalFormat;

@SuppressWarnings("serial")
public class cartpage extends JFrame {

    private final String username;
    private final DecimalFormat df = new DecimalFormat("₹#,##0.00");
    private JPanel itemsPanel;
    private JLabel totalLabel;
    private double grandTotal = 0.0;

    public cartpage(String username) {
        this.username = username;

        setTitle("SareeStore - My Cart");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        getContentPane().setBackground(Color.WHITE);
        setLayout(new BorderLayout());

        add(createTopBar(), BorderLayout.NORTH);
        add(createMainCartPanel(), BorderLayout.CENTER);
        add(createFooterPanel(), BorderLayout.SOUTH);

        loadCartItems();

        setVisible(true);
    }

    private JPanel createTopBar() {
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(Color.WHITE);
        topBar.setBorder(new EmptyBorder(15, 30, 15, 30));

        JLabel logo = new JLabel("Shopping Cart");
        logo.setFont(new Font("Serif", Font.BOLD, 30));
        logo.setForeground(new Color(153, 50, 204));
        topBar.add(logo, BorderLayout.WEST);

        JButton backBtn = new JButton("<< Continue Shopping");
        backBtn.setFont(new Font("Arial", Font.BOLD, 14));
        backBtn.setFocusPainted(false);
        backBtn.setBackground(new Color(240, 240, 240));
        backBtn.addActionListener(e -> {
            new homePage(username);
            dispose();
        });

        JPanel nav = new JPanel(new FlowLayout(FlowLayout.RIGHT, 25, 5));
        nav.setBackground(Color.WHITE);
        nav.add(backBtn);
        topBar.add(nav, BorderLayout.EAST);

        return topBar;
    }

    private JPanel createMainCartPanel() {
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(Color.WHITE);
        mainPanel.setBorder(new EmptyBorder(20, 100, 20, 100));

        itemsPanel = new JPanel();
        itemsPanel.setLayout(new BoxLayout(itemsPanel, BoxLayout.Y_AXIS));
        itemsPanel.setBackground(Color.WHITE);

        JScrollPane scrollPane = new JScrollPane(itemsPanel);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200)));
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        mainPanel.add(scrollPane, BorderLayout.CENTER);
        return mainPanel;
    }

    private JPanel createFooterPanel() {
        JPanel footerPanel = new JPanel(new BorderLayout());
        footerPanel.setBackground(new Color(230, 230, 230));
        footerPanel.setBorder(new EmptyBorder(20, 100, 20, 100));

        JPanel totalPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        totalPanel.setBackground(new Color(230, 230, 230));
        JLabel totalText = new JLabel("GRAND TOTAL: ");
        totalText.setFont(new Font("Arial", Font.BOLD, 22));
        totalLabel = new JLabel(df.format(0.00));
        totalLabel.setFont(new Font("Arial", Font.BOLD, 22));
        totalLabel.setForeground(new Color(255, 69, 0));
        totalPanel.add(totalText);
        totalPanel.add(totalLabel);

        JButton checkoutBtn = new JButton("Proceed to Checkout >>");
        checkoutBtn.setFont(new Font("Arial", Font.BOLD, 20));
        checkoutBtn.setBackground(new Color(60, 179, 113));
        checkoutBtn.setForeground(Color.WHITE);
        checkoutBtn.setFocusPainted(false);
        checkoutBtn.setBorder(BorderFactory.createEmptyBorder(10, 25, 10, 25));

        checkoutBtn.addActionListener(e -> {
            if (grandTotal > 0) {
                // ⭐️ FIX APPLIED HERE: Uncommented and completed the navigation line ⭐️
                // Assuming PaymentPage is the next screen and requires username and total
                new PaymentPage(username, grandTotal); 
                
                dispose();
            } else {
                JOptionPane.showMessageDialog(this,
                    "Your cart is empty. Add some sarees first!",
                    "Empty Cart", JOptionPane.WARNING_MESSAGE);
            }
        });

        footerPanel.add(totalPanel, BorderLayout.WEST);
        footerPanel.add(checkoutBtn, BorderLayout.EAST);
        return footerPanel;
    }

    private void loadCartItems() {
        itemsPanel.removeAll();
        grandTotal = 0.0;

        String sql = "SELECT c.cart_item_id, c.quantity, p.name, p.price " +
                     "FROM Cart c JOIN Products p ON c.product_id = p.product_id " +
                     "WHERE c.username = ? AND c.order_id IS NULL";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, username);
            ResultSet rs = ps.executeQuery();

            if (!rs.isBeforeFirst()) {
                JLabel emptyLabel = new JLabel("Your shopping cart is empty.", SwingConstants.CENTER);
                emptyLabel.setFont(new Font("Arial", Font.ITALIC, 18));
                itemsPanel.add(Box.createVerticalStrut(50));
                emptyLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
                itemsPanel.add(emptyLabel);
                itemsPanel.add(Box.createVerticalGlue());
            } else {
                itemsPanel.add(createHeaderRow());
                itemsPanel.add(Box.createVerticalStrut(10));

                while (rs.next()) {
                    int cartItemId = rs.getInt("cart_item_id");
                    String name = rs.getString("name");
                    double price = rs.getDouble("price");
                    int quantity = rs.getInt("quantity");

                    double subtotal = price * quantity;
                    grandTotal += subtotal;

                    itemsPanel.add(createCartItemRow(cartItemId, name, price, quantity, subtotal));
                    itemsPanel.add(Box.createVerticalStrut(5));
                }
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this,
                "Database Error loading cart: " + ex.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }

        totalLabel.setText(df.format(grandTotal));
        itemsPanel.revalidate();
        itemsPanel.repaint();
    }

    private JPanel createHeaderRow() {
        JPanel header = new JPanel(new GridLayout(1, 5, 10, 0));
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.GRAY));
        header.setBackground(new Color(240, 240, 240));
        header.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));

        String[] titles = {"Item", "Price", "Quantity", "Subtotal", "Actions"};
        for (String title : titles) {
            JLabel label = new JLabel(title, SwingConstants.CENTER);
            label.setFont(new Font("Arial", Font.BOLD, 16));
            label.setBorder(new EmptyBorder(5, 0, 5, 0));
            header.add(label);
        }
        return header;
    }

    private JPanel createCartItemRow(int cartItemId, String name, double price, int quantity, double subtotal) {
        JPanel row = new JPanel(new GridLayout(1, 5, 10, 0));
        row.setBackground(Color.WHITE);
        row.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(220, 220, 220)));
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));

        JLabel nameLabel = new JLabel(name);
        nameLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        nameLabel.setBorder(new EmptyBorder(0, 10, 0, 0));

        JLabel priceLabel = new JLabel(df.format(price), SwingConstants.CENTER);
        priceLabel.setFont(new Font("Arial", Font.PLAIN, 14));

        JLabel qtyLabel = new JLabel(String.valueOf(quantity), SwingConstants.CENTER);
        qtyLabel.setFont(new Font("Arial", Font.PLAIN, 14));

        JLabel subtotalLabel = new JLabel(df.format(subtotal), SwingConstants.CENTER);
        subtotalLabel.setFont(new Font("Arial", Font.BOLD, 14));
        subtotalLabel.setForeground(new Color(255, 69, 0));

        JButton removeBtn = new JButton("Remove");
        removeBtn.setBackground(new Color(255, 100, 100));
        removeBtn.setForeground(Color.WHITE);
        removeBtn.setFocusPainted(false);
        removeBtn.addActionListener(e -> removeItem(cartItemId));

        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        actionPanel.setBackground(Color.WHITE);
        actionPanel.add(removeBtn);

        row.add(nameLabel);
        row.add(priceLabel);
        row.add(qtyLabel);
        row.add(subtotalLabel);
        row.add(actionPanel);

        return row;
    }

    private void removeItem(int cartItemId) {
        int confirm = JOptionPane.showConfirmDialog(this,
            "Are you sure you want to remove this item from your cart?",
            "Confirm Removal",
            JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            String sql = "DELETE FROM Cart WHERE cart_item_id = ?";
            try (Connection conn = DBConnection.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {

                ps.setInt(1, cartItemId);
                int rowsAffected = ps.executeUpdate();

                if (rowsAffected > 0) {
                    JOptionPane.showMessageDialog(this, "Item successfully removed.", "Removed", JOptionPane.INFORMATION_MESSAGE);
                    loadCartItems();
                }
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this,
                    "Error removing item: " + ex.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace();
            }
        }
    }
}