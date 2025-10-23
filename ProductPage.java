

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.sql.ResultSet;

@SuppressWarnings("serial")
public class ProductPage extends JFrame {

    private final Product product;
    private final String username;
    private final DecimalFormat df = new DecimalFormat("₹#,##0.00");

    public ProductPage(Product product, String username) {
        this.product = product;
        this.username = username;

        setTitle("SareeStore - " + product.getName());
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        getContentPane().setBackground(Color.WHITE);
        setLayout(new BorderLayout());

        add(createTopBar(), BorderLayout.NORTH);
        add(createMainContentPanel(), BorderLayout.CENTER);

        setVisible(true);
    }

    private JPanel createTopBar() {
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(Color.WHITE);
        topBar.setBorder(new EmptyBorder(15, 30, 15, 30));

        JLabel logo = new JLabel("Product Details");
        logo.setFont(new Font("Serif", Font.BOLD, 30));
        logo.setForeground(new Color(153, 50, 204));
        topBar.add(logo, BorderLayout.WEST);

        JButton backBtn = new JButton("<< Back to Home");
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

    private JPanel createMainContentPanel() {
        JPanel panel = new JPanel(new GridLayout(1, 2, 50, 0));
        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(40, 100, 50, 100));

        panel.add(createImagePanel());
        panel.add(createDetailsPanel());

        return panel;
    }

    private JPanel createImagePanel() {
        JPanel imagePanel = new JPanel(new BorderLayout());
        imagePanel.setBackground(Color.WHITE);
        imagePanel.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220), 1));

        JLabel imageLabel;
        try {
            // Note: The imagePath must be valid for this to work
            ImageIcon icon = new ImageIcon(product.getImagePath()); 
            Image img = icon.getImage().getScaledInstance(500, 500, Image.SCALE_SMOOTH);
            icon = new ImageIcon(img);
            imageLabel = new JLabel(icon);
        } catch (Exception e) {
            imageLabel = new JLabel("Image not available: " + product.getImagePath(), SwingConstants.CENTER);
            imageLabel.setFont(new Font("Arial", Font.ITALIC, 16));
            imageLabel.setForeground(Color.RED);
        }

        imageLabel.setHorizontalAlignment(SwingConstants.CENTER);
        imagePanel.add(imageLabel, BorderLayout.CENTER);
        return imagePanel;
    }

    private JPanel createDetailsPanel() {
        JPanel detailsPanel = new JPanel();
        detailsPanel.setLayout(new BoxLayout(detailsPanel, BoxLayout.Y_AXIS));
        detailsPanel.setBackground(Color.WHITE);

        JLabel nameLabel = new JLabel(product.getName());
        nameLabel.setFont(new Font("Serif", Font.BOLD, 36));
        nameLabel.setForeground(new Color(140, 40, 80));
        detailsPanel.add(nameLabel);
        detailsPanel.add(Box.createVerticalStrut(15));

        JLabel categoryLabel = new JLabel("Category: " + product.getCategory());
        categoryLabel.setFont(new Font("Arial", Font.PLAIN, 18));
        categoryLabel.setForeground(Color.GRAY);
        detailsPanel.add(categoryLabel);
        detailsPanel.add(Box.createVerticalStrut(10));

        JLabel priceLabel = new JLabel(df.format(product.getPrice()));
        priceLabel.setFont(new Font("Arial", Font.BOLD, 48));
        priceLabel.setForeground(new Color(255, 69, 0));
        detailsPanel.add(priceLabel);
        detailsPanel.add(Box.createVerticalStrut(30));

        JTextArea description = new JTextArea(
            "This exquisite saree features a blend of traditional weaving techniques and modern prints. " +
            "Perfect for weddings, festivals, or special occasions. Fabric quality ensures comfort and a beautiful drape."
        );
        description.setFont(new Font("Arial", Font.PLAIN, 16));
        description.setLineWrap(true);
        description.setWrapStyleWord(true);
        description.setEditable(false);
        description.setBackground(Color.WHITE);
        detailsPanel.add(description);
        detailsPanel.add(Box.createVerticalStrut(40));

        JPanel qtyPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        qtyPanel.setBackground(Color.WHITE);
        JLabel qtyLabel = new JLabel("Quantity:");
        qtyLabel.setFont(new Font("Arial", Font.BOLD, 18));
        qtyPanel.add(qtyLabel);

        SpinnerModel model = new SpinnerNumberModel(1, 1, 10, 1);
        JSpinner qtySpinner = new JSpinner(model);
        qtySpinner.setFont(new Font("Arial", Font.PLAIN, 18));
        qtySpinner.setPreferredSize(new Dimension(80, 30));
        qtyPanel.add(qtySpinner);
        detailsPanel.add(qtyPanel);
        detailsPanel.add(Box.createVerticalStrut(40));

        JButton addToCartBtn = new JButton("Add to Cart");
        addToCartBtn.setFont(new Font("Arial", Font.BOLD, 22));
        addToCartBtn.setBackground(new Color(65, 105, 225));
        addToCartBtn.setForeground(Color.WHITE);
        addToCartBtn.setFocusPainted(false);
        addToCartBtn.setBorder(BorderFactory.createEmptyBorder(15, 40, 15, 40));
        addToCartBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        addToCartBtn.addActionListener(e -> {
            int quantity = (Integer) qtySpinner.getValue();
            addItemToCart(quantity);
        });

        detailsPanel.add(addToCartBtn);

        return detailsPanel;
    }

    private void addItemToCart(int quantity) {
        String checkSql = "SELECT cart_item_id, quantity FROM Cart WHERE username = ? AND product_id = ? AND order_id IS NULL";
        String updateSql = "UPDATE Cart SET quantity = quantity + ?, product_name = ?, price = ? WHERE cart_item_id = ?";
        String insertSql = "INSERT INTO Cart (username, product_id, quantity, product_name, price) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection()) {
            try (PreparedStatement checkPs = conn.prepareStatement(checkSql)) {
                checkPs.setString(1, username);
                checkPs.setInt(2, product.getId());
                ResultSet rs = checkPs.executeQuery();

                if (rs.next()) {
                    int cartItemId = rs.getInt("cart_item_id");
                    try (PreparedStatement updatePs = conn.prepareStatement(updateSql)) {
                        updatePs.setInt(1, quantity);
                        updatePs.setString(2, product.getName());
                        updatePs.setDouble(3, product.getPrice());
                        updatePs.setInt(4, cartItemId);
                        updatePs.executeUpdate();
                        JOptionPane.showMessageDialog(this,
                            quantity + " more of " + product.getName() + " added to your cart!",
                            "Cart Updated", JOptionPane.INFORMATION_MESSAGE);
                    }
                } else {
                    try (PreparedStatement insertPs = conn.prepareStatement(insertSql)) {
                        insertPs.setString(1, username);
                        insertPs.setInt(2, product.getId());
                        insertPs.setInt(3, quantity);
                        insertPs.setString(4, product.getName());
                        insertPs.setDouble(5, product.getPrice());
                        insertPs.executeUpdate();
                        JOptionPane.showMessageDialog(this,
                            quantity + " x " + product.getName() + " successfully added to cart!",
                            "Added to Cart", JOptionPane.INFORMATION_MESSAGE);
                    }
                }
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this,
                "Error adding item to cart: " + ex.getMessage(),
                "Database Error", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }
}
// ⭐️ Removed the extra closing brace that was here ⭐️
