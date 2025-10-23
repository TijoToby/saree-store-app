import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

@SuppressWarnings("serial")
public class AdminPage extends JFrame {

    private final String adminUsername;

    public AdminPage(String adminUsername) {
        this.adminUsername = adminUsername;
        
        setTitle("SareeStore - Admin Dashboard");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH); // Fullscreen
        getContentPane().setBackground(new Color(245, 245, 245));
        setLayout(new BorderLayout());

        // --- Top Bar ---
        JPanel topBar = createTopBar();
        add(topBar, BorderLayout.NORTH);

        // --- Main Content (Buttons) ---\
        JPanel mainContent = new JPanel(new GridBagLayout());
        mainContent.setBackground(new Color(245, 245, 245));
        
        JPanel buttonPanel = createAdminPanel();
        mainContent.add(buttonPanel);
        
        add(mainContent, BorderLayout.CENTER);
        
        setVisible(true);
    }
    
    // --- UI Creation Methods ---

    private JPanel createTopBar() {
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(Color.WHITE);
        topBar.setBorder(new EmptyBorder(15, 30, 15, 30));

        JLabel logo = new JLabel("Admin: " + adminUsername);
        logo.setFont(new Font("Serif", Font.BOLD, 30));
        logo.setForeground(new Color(65, 105, 225));
        topBar.add(logo, BorderLayout.WEST);

        JButton logoutBtn = new JButton("Logout");
        styleButton(logoutBtn, new Color(220, 20, 60)); // Crimson red
        logoutBtn.addActionListener(e -> {
            // Assuming 'ui' is your main login/welcome page
            new ui(); 
            dispose();
        });

        JPanel navButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 25, 5));
        navButtons.setBackground(Color.WHITE);
        navButtons.add(logoutBtn);
        topBar.add(navButtons, BorderLayout.EAST);

        return topBar;
    }
    
    // AdminPage.java - CORRECTED createAdminPanel()

private JPanel createAdminPanel() {
    // ⭐️ IMPORTANT CHANGE: Change GridLayout from (3, 1) to (4, 1) to fit the new button ⭐️
    JPanel panel = new JPanel(new GridLayout(4, 1, 30, 30)); 
    panel.setBorder(new EmptyBorder(50, 100, 50, 100));
    panel.setBackground(new Color(245, 245, 245));

    // 1. Manage Products Button (Correct: Launches AdminProductManagementPage)
    JButton manageProductsBtn = new JButton("Manage Products (Add/Edit/Delete)");
    styleAdminButton(manageProductsBtn, new Color(34, 139, 34)); // Forest green
    manageProductsBtn.addActionListener(e -> {
        // Assuming AdminProductManagementPage is the main product view
        new ManageProductsPage (adminUsername); 
        dispose();
    });
    panel.add(manageProductsBtn);
    
    // 2. View Order & Payment History Button
    JButton paymentDetailsBtn = new JButton("View Sales & Payment History");
    styleAdminButton(paymentDetailsBtn, new Color(255, 140, 0)); // Dark orange
    paymentDetailsBtn.addActionListener(e -> {
        new AdminPaymentDetailsPage(adminUsername);
        dispose();
    });
    panel.add(paymentDetailsBtn);

    // ⭐️ 3. FIX: Manage Customer Orders Button (Must launch AdminOrderManagementPage) ⭐️
    JButton manageOrdersBtn = new JButton("Manage Customer Orders (Update Status)");
    styleAdminButton(manageOrdersBtn, new Color(153, 50, 204)); // Purple
    manageOrdersBtn.addActionListener(e -> {
        // 🚀 CORRECTED: Launch the order management class 
        new AdminProductManagementPage(adminUsername); // <-- ASSUMING THIS IS THE CORRECT CLASS NAME
        dispose();
    });
    panel.add(manageOrdersBtn);

    // 4. View Customer Accounts
    JButton customerAccountsBtn = new JButton("Manage Customer Accounts");
    styleAdminButton(customerAccountsBtn, new Color(100, 149, 237)); // Cornflower blue
    customerAccountsBtn.addActionListener(e -> {
        JOptionPane.showMessageDialog(this, "Customer management feature coming soon!", "Feature Alert", JOptionPane.INFORMATION_MESSAGE);
    });
    panel.add(customerAccountsBtn);

    return panel;
}

    private void styleButton(JButton button, Color bgColor) {
        button.setFont(new Font("Arial", Font.BOLD, 16));
        button.setBackground(bgColor);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(10, 25, 10, 25));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }
    
    private void styleAdminButton(JButton button, Color bgColor) {
        button.setFont(new Font("Arial", Font.BOLD, 22));
        button.setBackground(bgColor);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(25, 50, 25, 50));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }
    
    // --- Main Method for Testing (Optional) ---
    /*
    public static void main(String[] args) {
        final String TEST_ADMIN_USERNAME = "admin_test"; 
        
        SwingUtilities.invokeLater(() -> {
            new AdminPage(TEST_ADMIN_USERNAME);
        });
    }
    */
}
