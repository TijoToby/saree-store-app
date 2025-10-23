import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.text.DecimalFormat;
import java.sql.*;
import java.util.Date; 

@SuppressWarnings("serial")
public class PaymentPage extends JFrame {

    private final String username;
    private double subtotalPrice = 0.0; 

    // Fees and Rates
    private static final double PLATFORM_FEE = 15.00;
    private static final double DELIVERY_FEE = 100.00;
    private static final double TAX_RATE = 0.05;

    // UI Components for Summary
    private final JLabel subtotalLabel = new JLabel("₹0.00");
    private final JLabel feesLabel = new JLabel("₹" + String.format("%.2f", (PLATFORM_FEE + DELIVERY_FEE)));
    private final JLabel taxLabel = new JLabel("₹0.00");
    private final JLabel totalLabel = new JLabel("₹0.00");

    // UI Components for Payment
    private final JRadioButton cardRadio = new JRadioButton("Credit/Debit Card");
    private final JRadioButton codRadio = new JRadioButton("Cash On Delivery (COD)");
    private final JPanel paymentFormContainer = new JPanel(new BorderLayout());

    private final JTextField cardNumberField = new JTextField(15);
    private final JTextField cvvField = new JTextField(5);
    private final JTextField expDateField = new JTextField(5);

    // CONSTRUCTOR
    public PaymentPage(String username, double subtotalPriceFromCart) {
        this.username = username;
        this.subtotalPrice = subtotalPriceFromCart;
        
        setTitle("SareeStore - Checkout & Payment");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        getContentPane().setBackground(new Color(245, 245, 245));
        setLayout(new BorderLayout());

        JPanel topBar = createTopBar();
        add(topBar, BorderLayout.NORTH);

        JPanel mainPanel = new JPanel(new GridLayout(1, 2, 80, 20));
        mainPanel.setBackground(new Color(245, 245, 245));
        mainPanel.setBorder(new EmptyBorder(40, 100, 50, 100));

        mainPanel.add(createPaymentOptionsPanel());
        mainPanel.add(createSummaryPanel());

        add(mainPanel, BorderLayout.CENTER);

        updateSummaryLabels(); 
        updatePaymentForm(); 
        
        setVisible(true);
    }
    
    // --- UI Creation Methods ---
    private JPanel createPaymentOptionsPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 20));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(Color.GRAY), "Select Payment Method",
            TitledBorder.LEFT, TitledBorder.TOP, new Font("Arial", Font.BOLD, 18)
        ));

        JPanel radioPanel = new JPanel(new GridLayout(2, 1, 0, 10));
        radioPanel.setBorder(new EmptyBorder(10, 20, 10, 20));
        radioPanel.setBackground(Color.WHITE);
        
        ButtonGroup group = new ButtonGroup();
        group.add(cardRadio);
        group.add(codRadio);
        
        cardRadio.setFont(new Font("Arial", Font.BOLD, 16));
        codRadio.setFont(new Font("Arial", Font.BOLD, 16));
        cardRadio.setBackground(Color.WHITE);
        codRadio.setBackground(Color.WHITE);
        
        radioPanel.add(cardRadio);
        radioPanel.add(codRadio);
        panel.add(radioPanel, BorderLayout.NORTH);

        panel.add(paymentFormContainer, BorderLayout.CENTER);
        
        cardRadio.setSelected(true);
        updatePaymentForm(); 

        cardRadio.addActionListener(e -> updatePaymentForm());
        codRadio.addActionListener(e -> updatePaymentForm());

        return panel;
    }

    private void updatePaymentForm() {
        paymentFormContainer.removeAll();
        
        if (codRadio.isSelected()) {
            paymentFormContainer.add(createCODForm(), BorderLayout.CENTER);
        } else if (cardRadio.isSelected()) {
            paymentFormContainer.add(createCardForm(), BorderLayout.CENTER);
        }
        
        paymentFormContainer.revalidate();
        paymentFormContainer.repaint();
    }
    
    private JPanel createCardForm() {
        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(Color.WHITE);
        form.setBorder(new EmptyBorder(20, 20, 20, 20));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        addRow(form, gbc, "Card Number:", cardNumberField, 0);
        addRow(form, gbc, "Card Holder Name:", new JTextField(15), 1);
        
        JPanel smallFields = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        smallFields.setBackground(Color.WHITE);
        smallFields.add(new JLabel("CVV:"));
        smallFields.add(cvvField);
        smallFields.add(new JLabel("Exp Date (MM/YY):"));
        smallFields.add(expDateField);
        
        JLabel cvvExpLabel = new JLabel("Security Codes:");
        cvvExpLabel.setFont(new Font("Arial", Font.BOLD, 16));
        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 1;
        form.add(cvvExpLabel, gbc);
        gbc.gridx = 1; gbc.gridy = 2;
        form.add(smallFields, gbc);

        JButton payBtn = new JButton("Place Order & Pay");
        styleButton(payBtn, new Color(34, 139, 34));
        payBtn.addActionListener(e -> processPayment(false));

        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 2;
        gbc.insets = new Insets(40, 10, 10, 10);
        form.add(payBtn, gbc);

        return form;
    }
    
    private JPanel createCODForm() {
        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(Color.WHITE);
        form.setBorder(new EmptyBorder(50, 20, 50, 20));
        
        JLabel warning = new JLabel("<html><b>Cash on Delivery Selected.</b><br>Please have the exact amount (" + totalLabel.getText() + ") ready at the time of delivery.</html>", SwingConstants.CENTER);
        warning.setFont(new Font("Arial", Font.PLAIN, 16));
        
        JButton confirmBtn = new JButton("Confirm COD Order");
        styleButton(confirmBtn, new Color(255, 69, 0));
        confirmBtn.addActionListener(e -> processPayment(true));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(20, 10, 20, 10);
        gbc.gridx = 0; gbc.gridy = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        form.add(warning, gbc);
        
        gbc.gridy = 1;
        gbc.insets = new Insets(40, 10, 10, 10);
        form.add(confirmBtn, gbc);
        
        return form;
    }

    private JPanel createTopBar() {
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(Color.WHITE);
        topBar.setBorder(new EmptyBorder(15, 30, 15, 30));

        JLabel logo = new JLabel("SareeStore Checkout");
        logo.setFont(new Font("Serif", Font.BOLD, 30));
        logo.setForeground(new Color(255, 105, 180));
        topBar.add(logo, BorderLayout.WEST);

        JButton backBtn = new JButton("<< Back to Cart");
        styleButton(backBtn, new Color(200, 200, 200));
        backBtn.addActionListener(e -> {
            // new cartpage(username); // Uncomment if cartpage exists
            dispose();
        });

        JPanel nav = new JPanel(new FlowLayout(FlowLayout.RIGHT, 25, 5));
        nav.setBackground(Color.WHITE);
        nav.add(backBtn);
        topBar.add(nav, BorderLayout.EAST);

        return topBar;
    }
    
    private JPanel createSummaryPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(Color.GRAY),
            "Order Summary",
            TitledBorder.LEFT,
            TitledBorder.TOP,
            new Font("Arial", Font.BOLD, 18)
        ));

        JPanel details = new JPanel(new GridLayout(4, 2, 10, 15));
        details.setBackground(Color.WHITE);
        details.setBorder(new EmptyBorder(30, 30, 30, 30));

        details.add(createSummaryLabel("Subtotal:"));
        details.add(subtotalLabel);

        details.add(createSummaryLabel("Platform + Delivery Fees:"));
        details.add(feesLabel);

        details.add(createSummaryLabel("Tax (" + (int)(TAX_RATE * 100) + "%):"));
        details.add(taxLabel);

        details.add(new JSeparator(SwingConstants.HORIZONTAL));
        details.add(new JSeparator(SwingConstants.HORIZONTAL));

        JLabel grand = createSummaryLabel("GRAND TOTAL:");
        grand.setFont(new Font("Arial", Font.BOLD, 22));
        details.add(grand);
        totalLabel.setFont(new Font("Arial", Font.BOLD, 22));
        totalLabel.setForeground(new Color(255, 69, 0));
        details.add(totalLabel);

        panel.add(details, BorderLayout.NORTH);

        return panel;
    }

    // --- Data Calculation ---

    private void updateSummaryLabels() {
        DecimalFormat df = new DecimalFormat("₹#,##0.00");

        double tax = subtotalPrice * TAX_RATE;
        double fees = PLATFORM_FEE + DELIVERY_FEE;
        double total = subtotalPrice + tax + fees;

        subtotalLabel.setText(df.format(subtotalPrice));
        taxLabel.setText(df.format(tax));
        totalLabel.setText(df.format(total));
    }

    // --- Order Finalization Logic ---

    private void processPayment(boolean isCOD) {
        String paymentMethod = isCOD ? "Cash on Delivery" : "Card";
        String orderStatus = isCOD ? "Pending" : "Paid";
        
        if (!isCOD) {
            String cnum = cardNumberField.getText().trim();
            String cvv = cvvField.getText().trim();
            String exp = expDateField.getText().trim();

            if (cnum.length() != 16 || cvv.length() != 3 || exp.length() != 5) {
                JOptionPane.showMessageDialog(this, "Please enter valid card details (16 digits, 3-digit CVV, MM/YY).", "Payment Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
        }
        
        double tax = subtotalPrice * TAX_RATE;
        double finalTotal = subtotalPrice + PLATFORM_FEE + DELIVERY_FEE + tax;

        // 1. Create the order record
        int orderId = createOrderRecord(finalTotal, paymentMethod, orderStatus);

        if (orderId > 0) {
            // 2. Move cart items to the orderitems table
            if (moveCartItemsToOrder(orderId)) {
                 JOptionPane.showMessageDialog(this, 
                    "Order #" + orderId + " placed successfully!\nTotal: " + totalLabel.getText() + 
                    (isCOD ? "\nStatus: Pending Cash On Delivery." : "\nStatus: Payment successful."), 
                    "Order Confirmed", JOptionPane.INFORMATION_MESSAGE);
            } else {
                 JOptionPane.showMessageDialog(this, "Order created but failed to link cart items. Contact support.", "Critical Error", JOptionPane.ERROR_MESSAGE);
            }
        } else {
             JOptionPane.showMessageDialog(this, "Failed to create Order record. Database error.", "Order Error", JOptionPane.ERROR_MESSAGE);
             return;
        }

        // ⭐️ FIX for Navigation: Launch homePage before disposing of the current frame. ⭐️
        new homePage(username); 
        dispose();
    }

// ⭐️ FIX for: Field 'shipping_name', 'shipping_state', etc. doesn't have a default value
private int createOrderRecord(double totalPrice, String paymentMethod, String orderStatus) {
// SQL includes all required shipping fields with placeholders
    String sql = "INSERT INTO orders (username, total_amount, payment_method, status, shipping_name, shipping_state, shipping_city, shipping_address, shipping_zip, order_date) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, NOW())";
        int orderId = -1;
        
        // Placeholder Data (Since the UI doesn't collect it)
        final String PLACEHOLDER_STATE = "Maharashtra";
        final String PLACEHOLDER_CITY = "Mumbai";
        final String PLACEHOLDER_ADDRESS = "Default Pickup Address - Check User Profile";
        final String PLACEHOLDER_ZIP = "400001"; 
        final String PLACEHOLDER_NAME = "Self Pickup / " + username; 

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            // Basic Order Details (1-4)
            pstmt.setString(1, username);
            pstmt.setDouble(2, totalPrice);
            pstmt.setString(3, paymentMethod);
            pstmt.setString(4, orderStatus);
            
            // Shipping Details (5-9) 
            pstmt.setString(5, PLACEHOLDER_NAME);
            pstmt.setString(6, PLACEHOLDER_STATE); 
            pstmt.setString(7, PLACEHOLDER_CITY);
            pstmt.setString(8, PLACEHOLDER_ADDRESS);
            pstmt.setString(9, PLACEHOLDER_ZIP);
            
            // NOW() automatically handles the 10th parameter (order_date)

            int affectedRows = pstmt.executeUpdate();
            
            if (affectedRows > 0) {
                try (ResultSet rs = pstmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        orderId = rs.getInt(1); 
                    }
                }
            }
        } catch (SQLException ex) {
            System.err.println("Error creating order record: " + ex.getMessage());
            ex.printStackTrace();
        }
        return orderId;
    }

    /**
    * Transactional logic to move cart items to orderitems and clear the cart.
    */
    private boolean moveCartItemsToOrder(int orderId) {
        Connection conn = null;
        boolean success = false;
        
        // Step A: SELECT the cart items before deleting them
        String selectSql = "SELECT c.product_id, c.quantity, p.price " +
                              "FROM cart c JOIN Products p ON c.product_id = p.product_id " +
                              "WHERE c.username = ? AND c.order_id IS NULL";
        
        // Step B: INSERT one item at a time into orderitems
        String insertSql = "INSERT INTO orderitems (order_id, product_id, quantity, price_at_purchase) VALUES (?, ?, ?, ?)";
        
        // Step C: DELETE items from the Cart
        String deleteSql = "DELETE FROM cart WHERE username = ? AND order_id IS NULL";

        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false); 

            // 1. Fetch all items from the cart and insert into orderitems
            try (PreparedStatement selectPstmt = conn.prepareStatement(selectSql)) {
                selectPstmt.setString(1, username);
                try (ResultSet rs = selectPstmt.executeQuery();
                     PreparedStatement insertPstmt = conn.prepareStatement(insertSql)) {

                    while (rs.next()) {
                        int productId = rs.getInt("product_id");
                        int quantity = rs.getInt("quantity");
                        double price = rs.getDouble("price");

                        // Insert item into permanent orderitems table
                        insertPstmt.setInt(1, orderId);
                        insertPstmt.setInt(2, productId);
                        insertPstmt.setInt(3, quantity);
                        insertPstmt.setDouble(4, price);
                        insertPstmt.addBatch();
                    }
                    insertPstmt.executeBatch(); // Execute all inserts at once
                }
            }

            // 2. Clear the temporary Cart records
            try (PreparedStatement deletePstmt = conn.prepareStatement(deleteSql)) {
                deletePstmt.setString(1, username);
                deletePstmt.executeUpdate();
                success = true;
            }
            
            conn.commit(); 
            
        } catch (SQLException ex) {
            // Rollback and report error
            try {
                if (conn != null) conn.rollback();
            } catch (SQLException rollbackEx) { }
            System.err.println("Transaction failed: " + ex.getMessage());
            ex.printStackTrace();
            success = false;
        } finally {
            try {
                if (conn != null) conn.setAutoCommit(true);
                if (conn != null) conn.close(); 
            } catch (SQLException finalEx) { }
        }
        return success;
    }
    
    // --- Helper Methods ---
    private void addRow(JPanel panel, GridBagConstraints gbc, String labelText, JComponent comp, int y) {
        gbc.gridx = 0; gbc.gridy = y; gbc.gridwidth = 1;
        JLabel lbl = new JLabel(labelText);
        lbl.setFont(new Font("Arial", Font.BOLD, 16));
        panel.add(lbl, gbc);

        gbc.gridx = 1; gbc.gridy = y;
        comp.setFont(new Font("Arial", Font.PLAIN, 16));
        panel.add(comp, gbc);
    }

    private JLabel createSummaryLabel(String text) {
        JLabel lbl = new JLabel(text, SwingConstants.LEFT);
        lbl.setFont(new Font("Arial", Font.PLAIN, 18));
        return lbl;
    }

    private void styleButton(JButton button, Color bg) {
        button.setFont(new Font("Arial", Font.BOLD, 18));
        button.setBackground(bg);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(12, 30, 12, 30));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }
}
