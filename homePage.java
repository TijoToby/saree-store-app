

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("serial")
public class homePage extends JFrame {

    private final String username;

    public homePage(String username) {
        this.username = username;

        setTitle("SareeStore - Home");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        getContentPane().setBackground(Color.WHITE);
        setLayout(new BorderLayout());

        JPanel topBar = createTopBar();
        add(topBar, BorderLayout.NORTH);

        JPanel mainPanel = new JPanel();
        mainPanel.setBackground(Color.WHITE);
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(new EmptyBorder(20, 50, 50, 50));

        JLabel welcomeLabel = new JLabel("Welcome, " + username + "!", SwingConstants.CENTER);
        welcomeLabel.setFont(new Font("Arial", Font.BOLD, 26));
        welcomeLabel.setForeground(new Color(140, 40, 80));
        welcomeLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        mainPanel.add(welcomeLabel);
        mainPanel.add(Box.createVerticalStrut(20));

        mainPanel.add(createCategoryPanel("New Collection", getProductsByCategory("New Collection")));
        mainPanel.add(createCategoryPanel("Cotton Sarees", getProductsByCategory("Cotton")));
        mainPanel.add(createCategoryPanel("Silk Sarees", getProductsByCategory("Silk")));
        mainPanel.add(createCategoryPanel("Banarasi Sarees", getProductsByCategory("Banarasi")));

        JScrollPane scrollPane = new JScrollPane(mainPanel);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        add(scrollPane, BorderLayout.CENTER);

        setVisible(true);
    }

    private JPanel createTopBar() {
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(Color.WHITE);
        topBar.setBorder(new EmptyBorder(15, 30, 15, 30));

        JLabel logo = new JLabel("SareeStore ");
        logo.setFont(new Font("Serif", Font.BOLD, 30));
        logo.setForeground(new Color(255, 105, 180));
        topBar.add(logo, BorderLayout.WEST);

        JPanel navButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 25, 5));
        navButtons.setBackground(Color.WHITE);
        String[] buttons = { "Home", "Cart", "Profile", "Logout" };
        for (String name : buttons) {
            JButton btn = new JButton(name);
            btn.setFont(new Font("Arial", Font.BOLD, 14));
            btn.setFocusPainted(false);
            btn.setBackground(Color.WHITE);
            btn.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200)));
            btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            navButtons.add(btn);

            btn.addActionListener(e -> {
                switch (name) {
                    case "Logout":
                        // new ui(); // Redirects to Login/UI
                        dispose();
                        break;
                    case "Cart":
                        new cartpage(username); 
                        dispose(); 
                        break;
                    case "Profile":
                        // ⭐️ FIX: Un-commented this line to open the profile page
                        new AccountPage(username); 
                        dispose(); 
                        break;
                    case "Home":
                        break;
                }
            });
        }
        topBar.add(navButtons, BorderLayout.EAST);
        return topBar;
    }

    private List<Product> getProductsByCategory(String category) {
        List<Product> list = new ArrayList<>();
        
        // --- Placeholder Data used for demonstration (Adjusted to use available images) ---
        if (category.equals("New Collection")) {
            list.add(new Product(101, "Royal Blue Banarasi Silk", 5499.00, category, "images/saree1.jpg"));
            list.add(new Product(102, "Blue Abstract Georgette", 2899.50, category, "images/saree4.jpg"));
        } else if (category.equals("Cotton")) {
            list.add(new Product(201, "Summer Floral Cotton Print", 1950.00, category, "images/saree2.jpg"));
            list.add(new Product(202, "Linen Cotton Casual White", 1200.00, category, "images/saree3.jpg"));
        } else if (category.equals("Silk")) {
            // Reusing an image
            list.add(new Product(301, "Kanjeevaram Wedding Silk", 8500.00, category, "images/saree5.jpg"));
        } else if (category.equals("Banarasi")) {
            // Reusing an image
            list.add(new Product(401, "Classic Maroon Banarasi", 7200.00, category, "images/saree6.jpg"));
        }
        
        /* * Uncomment and use this section when your database is fully populated 
         * with a 'Products' table.
        String sql = "SELECT product_id, name, price, category, image_path FROM Products WHERE category = ?";
        try (Connection conn = DBConnection.getConnection();
              PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, category);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Product p = new Product(
                    rs.getInt("product_id"),
                    rs.getString("name"),
                    rs.getDouble("price"),
                    rs.getString("category"),
                    rs.getString("image_path")
                );
                list.add(p);
            }
        } catch (SQLException ex) {
              System.err.println("Error fetching products: " + ex.getMessage());
        }
        */
        return list;
    }

    private JPanel createCategoryPanel(String title, List<Product> products) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setForeground(new Color(140, 40, 80));
        titleLabel.setBorder(new EmptyBorder(10, 10, 20, 10));
        panel.add(titleLabel, BorderLayout.NORTH);

        JPanel cardsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 30, 30));
        cardsPanel.setBackground(Color.WHITE);

        if (products.isEmpty()) {
            cardsPanel.add(new JLabel("No " + title + " available.", SwingConstants.CENTER));
        } else {
            for (Product p : products) {
                cardsPanel.add(createSareeCard(p));
            }
        }
        
        panel.add(cardsPanel, BorderLayout.CENTER);

        JPanel container = new JPanel();
        container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));
        container.add(panel);
        container.add(Box.createVerticalStrut(30));
        container.setBackground(Color.WHITE);
        return container;
    }

    private JPanel createSareeCard(Product product) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220), 2));
        card.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        card.setPreferredSize(new Dimension(270, 350)); 

        // Image loading block
        JLabel imageLabel;
        try {
            ImageIcon icon = new ImageIcon(product.getImagePath());
            Image img = icon.getImage().getScaledInstance(250, 250, Image.SCALE_SMOOTH);
            icon = new ImageIcon(img);
            imageLabel = new JLabel(icon);
        } catch (Exception e) {
            System.err.println("Error loading image for " + product.getName() + ": " + e.getMessage());
            imageLabel = new JLabel("[Image Error]", SwingConstants.CENTER);
            imageLabel.setPreferredSize(new Dimension(250, 250));
        }
        
        imageLabel.setHorizontalAlignment(SwingConstants.CENTER);
        card.add(imageLabel, BorderLayout.CENTER);

        // Details Panel for Name and Price
        JPanel detailsPanel = new JPanel(new BorderLayout());
        detailsPanel.setBorder(new EmptyBorder(5, 10, 5, 10));
        detailsPanel.setBackground(Color.WHITE);
        
        JLabel nameLabel = new JLabel(product.getName(), SwingConstants.LEFT);
        nameLabel.setFont(new Font("Arial", Font.BOLD, 14));
        nameLabel.setForeground(new Color(65, 105, 225));
        detailsPanel.add(nameLabel, BorderLayout.NORTH);
        
        JLabel priceLabel = new JLabel("₹" + String.format("%.2f", product.getPrice()), SwingConstants.RIGHT);
        priceLabel.setFont(new Font("Arial", Font.BOLD, 16));
        priceLabel.setForeground(new Color(255, 69, 0));
        detailsPanel.add(priceLabel, BorderLayout.SOUTH);
        
        card.add(detailsPanel, BorderLayout.SOUTH);

        card.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                // Correctly navigate to the ProductPage
                // Assuming the ProductPage class is available.
                new ProductPage(product, username); 
                dispose();
            }
        });
        return card;
    }
}
