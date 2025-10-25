import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.sql.*;
import java.text.DecimalFormat;

@SuppressWarnings("serial")
public class PaymentPage extends JFrame {

    private final String username;
    private double subtotalPrice = 0.0; 

    // Fees and Rates
    private static final double PLATFORM_FEE = 15.00;
    private static final double DELIVERY_FEE = 100.00;
    private static final double TAX_RATE = 0.05;
    
    // --- Shipping Address Input Fields ---
    private final JTextField fullNameField = new JTextField(20);
    private final JTextField addressLineField = new JTextField(20);
    private final JTextField cityField = new JTextField(15);
    private final JComboBox<String> stateComboBox;
    private final JTextField zipCodeField = new JTextField(7);
    
    // States for the Dropdown (Example List)
    private static final String[] INDIAN_STATES = {
        "Andhra Pradesh", "Arunachal Pradesh", "Assam", "Bihar", "Chhattisgarh", 
        "Goa", "Gujarat", "Haryana", "Himachal Pradesh", "Jharkhand", 
        "Karnataka", "Kerala", "Madhya Pradesh", "Maharashtra", "Manipur", 
        "Meghalaya", "Mizoram", "Nagaland", "Odisha", "Punjab", "Rajasthan", 
        "Sikkim", "Tamil Nadu", "Telangana", "Tripura", "Uttar Pradesh", 
        "Uttarakhand", "West Bengal"
    };
    
    // UI Components for Summary
    private final JLabel subtotalLabel = new JLabel("₹0.00");
    private final JLabel feesLabel = new JLabel("₹" + String.format("%.2f", (PLATFORM_FEE + DELIVERY_FEE)));
    private final JLabel taxLabel = new JLabel("₹0.00");
    private final JLabel totalLabel = new JLabel("₹0.00");

    // UI Components for Payment
    private final JRadioButton cardRadio = new JRadioButton("Credit/Debit Card");
    private final JRadioButton codRadio = new JRadioButton("Cash On Delivery (COD)");
    private final JPanel paymentFormContainer = new JPanel(new BorderLayout());
    
    // Card Input Fields
    private final JTextField cardHolderNameField = new JTextField(15);
    private final JTextField cardNumberField = new JTextField(15);
    private final JTextField cvvField = new JTextField(5);
    private final JTextField expDateField = new JTextField(5);

    // Decimal formatter for consistent currency display
    private final DecimalFormat df = new DecimalFormat("₹#,##0.00");

    // CONSTRUCTOR
    public PaymentPage(String username, double subtotalPriceFromCart) {
        this.username = username;
        this.subtotalPrice = subtotalPriceFromCart;
        this.stateComboBox = new JComboBox<>(INDIAN_STATES);
        
        // **REMOVED: loadShippingAddress() - No address data in Users table**
        
        setTitle("SareeStore - Checkout & Payment");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        getContentPane().setBackground(new Color(245, 245, 245));
        setLayout(new BorderLayout());

        add(createTopBar(), BorderLayout.NORTH);
        
        JScrollPane scrollPane = new JScrollPane(createMainContentPanel());
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        add(scrollPane, BorderLayout.CENTER); 

        updateSummaryLabels(); 
        codRadio.setSelected(true); 
        updatePaymentForm(); 
        
        setVisible(true);
    }
    
    // --------------------------------------------------------------------------------
    // --- UI Layout Methods (Unchanged) ---
    // --------------------------------------------------------------------------------

    private JPanel createMainContentPanel() {
        JPanel wrapperPanel = new JPanel(new BorderLayout());
        wrapperPanel.setBackground(new Color(245, 245, 245));
        wrapperPanel.setBorder(new EmptyBorder(40, 100, 50, 100));
        
        JPanel gridPanel = new JPanel(new GridLayout(1, 2, 80, 20));
        gridPanel.setBackground(new Color(245, 245, 245));

        JPanel leftPanel = new JPanel();
        leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));
        leftPanel.setBackground(new Color(245, 245, 245));
        
        JPanel addressPanel = createAddressPanel();
        addressPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, addressPanel.getPreferredSize().height));
        addressPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        leftPanel.add(addressPanel);
        
        leftPanel.add(Box.createVerticalStrut(20));
        
        JPanel paymentOptionsPanel = createPaymentOptionsPanel();
        paymentOptionsPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, paymentOptionsPanel.getPreferredSize().height));
        paymentOptionsPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        leftPanel.add(paymentOptionsPanel);

        gridPanel.add(leftPanel);
        gridPanel.add(createSummaryPanel()); 

        wrapperPanel.add(gridPanel, BorderLayout.NORTH); 

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBackground(new Color(245, 245, 245));
        JButton confirmBtn = new JButton("Place Order");
        styleButton(confirmBtn, new Color(34, 139, 34));
        confirmBtn.addActionListener(e -> processPayment(codRadio.isSelected()));
        buttonPanel.add(confirmBtn);
        
        wrapperPanel.add(buttonPanel, BorderLayout.SOUTH);

        return wrapperPanel;
    }
    
    private JPanel createAddressPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(Color.GRAY), "Shipping Address",
            TitledBorder.LEFT, TitledBorder.TOP, new Font("Arial", Font.BOLD, 18)
        ));
        
        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(Color.WHITE);
        form.setBorder(new EmptyBorder(10, 20, 10, 20));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0; 
        
        int y = 0;
        
        y = addInputField(form, gbc, "Full Name:", fullNameField, y);
        y = addInputField(form, gbc, "Address Line:", addressLineField, y);
        y = addInputField(form, gbc, "City:", cityField, y);
        
        JPanel locPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        locPanel.setBackground(Color.WHITE);
        
        locPanel.add(createSmallLabel("State:"));
        stateComboBox.setFont(new Font("Arial", Font.PLAIN, 16));
        stateComboBox.setPreferredSize(new Dimension(150, 30));
        stateComboBox.setSelectedIndex(-1); // Start with no selection
        locPanel.add(stateComboBox);
        
        locPanel.add(Box.createHorizontalStrut(20));
        
        locPanel.add(createSmallLabel("Zip Code:"));
        zipCodeField.setFont(new Font("Arial", Font.PLAIN, 16));
        zipCodeField.setPreferredSize(new Dimension(70, 30));
        locPanel.add(zipCodeField);
        
        gbc.gridx = 0; gbc.gridy = y; gbc.gridwidth = 2;
        form.add(new JSeparator(SwingConstants.HORIZONTAL), gbc);
        
        y++;
        gbc.gridx = 0; gbc.gridy = y; gbc.gridwidth = 2; gbc.anchor = GridBagConstraints.WEST;
        form.add(locPanel, gbc);
        
        panel.add(form, BorderLayout.NORTH);
        return panel;
    }
    
    private int addInputField(JPanel panel, GridBagConstraints gbc, String labelText, JTextField field, int y) {
        gbc.gridx = 0; gbc.gridy = y; gbc.gridwidth = 1; gbc.anchor = GridBagConstraints.WEST;
        JLabel lbl = new JLabel(labelText);
        lbl.setFont(new Font("Arial", Font.BOLD, 16));
        panel.add(lbl, gbc);

        gbc.gridx = 1; gbc.gridy = y;
        field.setFont(new Font("Arial", Font.PLAIN, 16));
        field.setPreferredSize(new Dimension(field.getPreferredSize().width, 30)); 
        panel.add(field, gbc);
        
        return y + 1;
    }
    
    private JLabel createSmallLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("Arial", Font.BOLD, 16));
        return lbl;
    }

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

        paymentFormContainer.setBackground(Color.WHITE);
        panel.add(paymentFormContainer, BorderLayout.CENTER);
        
        cardRadio.addActionListener(e -> updatePaymentForm());
        codRadio.addActionListener(e -> updatePaymentForm());

        return panel;
    }

    private void updatePaymentForm() {
        paymentFormContainer.removeAll();
        
        if (codRadio.isSelected()) {
            String totalText = totalLabel.getText();
            paymentFormContainer.add(createCODForm(totalText), BorderLayout.CENTER);
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

        cardNumberField.setText("");
        cardHolderNameField.setText("");
        cvvField.setText("");
        expDateField.setText("");

        addRow(form, gbc, "Card Number:", cardNumberField, 0);
        addRow(form, gbc, "Card Holder Name:", cardHolderNameField, 1);
        
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

        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 2; gbc.weighty = 1.0;
        form.add(Box.createVerticalStrut(10), gbc);

        return form;
    }
    
    private JPanel createCODForm(String totalAmountText) {
        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(Color.WHITE);
        form.setBorder(new EmptyBorder(50, 20, 50, 20));
        
        JLabel warning = new JLabel("<html><div style='text-align: center;'><b>Cash on Delivery Selected.</b><br>Please have the exact amount (" + totalAmountText + ") ready at the time of delivery.</div></html>", SwingConstants.CENTER);
        warning.setFont(new Font("Arial", Font.PLAIN, 16));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(20, 10, 20, 10);
        gbc.gridx = 0; gbc.gridy = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        form.add(warning, gbc);
        
        gbc.gridy = 1; gbc.weighty = 1.0;
        form.add(Box.createVerticalStrut(10), gbc);
        
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
             // Assuming cartpage constructor takes username
             new cartpage(username); 
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

        JPanel details = new JPanel(new GridLayout(5, 2, 10, 15));
        details.setBackground(Color.WHITE);
        details.setBorder(new EmptyBorder(30, 30, 30, 30));

        details.add(createSummaryLabel("Subtotal:"));
        subtotalLabel.setFont(new Font("Arial", Font.PLAIN, 18));
        details.add(subtotalLabel);

        details.add(createSummaryLabel("Platform + Delivery Fees:"));
        feesLabel.setFont(new Font("Arial", Font.PLAIN, 18));
        details.add(feesLabel);

        details.add(createSummaryLabel("Tax (" + (int)(TAX_RATE * 100) + "%):"));
        taxLabel.setFont(new Font("Arial", Font.PLAIN, 18));
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

    // --------------------------------------------------------------------------------
    // --- Data & Logic Methods ---
    // --------------------------------------------------------------------------------

    /**
     * **REMOVED** - This method is no longer needed since the Users table does not hold address info.
     */
    /* private void loadShippingAddress() {
        // ... (removed logic) ...
    }
    */

    private void updateSummaryLabels() {
        double tax = subtotalPrice * TAX_RATE;
        double fees = PLATFORM_FEE + DELIVERY_FEE;
        double total = subtotalPrice + tax + fees;

        subtotalLabel.setText(df.format(subtotalPrice));
        taxLabel.setText(df.format(tax));
        totalLabel.setText(df.format(total));
    }

    private void processPayment(boolean isCOD) {
        // 1. VALIDATE AND CAPTURE ADDRESS FROM TEXT FIELDS & COMBO BOX
        String name = fullNameField.getText().trim();
        String address = addressLineField.getText().trim();
        String city = cityField.getText().trim();
        String state = (String) stateComboBox.getSelectedItem(); 
        String zip = zipCodeField.getText().trim();

        if (name.isEmpty() || address.isEmpty() || city.isEmpty() || state == null || state.isEmpty() || zip.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill in all shipping address fields and select a state.", "Address Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String paymentMethod = isCOD ? "Cash on Delivery" : "Card";
        String orderStatus = isCOD ? "Pending" : "Paid"; 
        
        if (!isCOD && cardRadio.isSelected()) {
            // Card Validation
            String cnum = cardNumberField.getText().trim();
            String cvv = cvvField.getText().trim();
            String exp = expDateField.getText().trim();

            if (cnum.length() != 16 || cvv.length() != 3 || exp.length() != 5) {
                JOptionPane.showMessageDialog(this, "Please enter valid card details (16-digit Card Number, 3-digit CVV, MM/YY Expiry).", "Payment Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
        }
        
        // Calculate final total
        double tax = subtotalPrice * TAX_RATE;
        double finalTotal = subtotalPrice + PLATFORM_FEE + DELIVERY_FEE + tax;

        // 2. Create the order record using the CAPTURED address
        int orderId = createOrderRecord(finalTotal, paymentMethod, orderStatus, name, state, city, address, zip);

        if (orderId > 0) {
            // 3. Move cart items to the orderitems table (CRITICAL TRANSACTIONAL FIX)
            if (moveCartItemsToOrder(orderId)) {
                 JOptionPane.showMessageDialog(this, 
                     "Order #" + orderId + " placed successfully!\nTotal: " + totalLabel.getText() + 
                     (isCOD ? "\nStatus: Pending Cash On Delivery." : "\nStatus: Payment successful."), 
                     "Order Confirmed", JOptionPane.INFORMATION_MESSAGE);
                 
                 // **REMOVED: saveShippingAddress() - No saving back to Users table**

            } else {
                 JOptionPane.showMessageDialog(this, 
                     "Order creation failed to finalize (items not recorded). Order #" + orderId + " will be marked as 'Failed'.\nPlease contact support.", 
                     "Critical Data Error", JOptionPane.ERROR_MESSAGE);
                 
                 try (Connection conn = DBConnection.getConnection();
                      PreparedStatement ps = conn.prepareStatement("UPDATE orders SET status = 'Failed - Data Error' WHERE order_id = ?")) {
                     ps.setInt(1, orderId);
                     ps.executeUpdate();
                 } catch (SQLException ex) {
                     System.err.println("Failed to clean up half-order: " + ex.getMessage());
                 }
                return; 
            }
        } else {
             JOptionPane.showMessageDialog(this, "Failed to create Order record. Database error.", "Order Error", JOptionPane.ERROR_MESSAGE);
             return;
        }

        // Navigation after successful order placement
        // **FIX: Navigates back to HomePage**
        // You MUST ensure your HomePage class exists and takes a String username in its constructor.
        new homePage(username); 
        dispose();
    }

    private int createOrderRecord(double totalPrice, String paymentMethod, String orderStatus, 
                                  String name, String state, String city, String address, String zip) {
        
        // This query inserts the address directly into the orders table, using the shipping_* columns
        String sql = "INSERT INTO orders (username, total_amount, payment_method, status, shipping_name, shipping_state, shipping_city, shipping_address, shipping_zip, order_date) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, NOW())";
        int orderId = -1;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            pstmt.setString(1, username);
            pstmt.setDouble(2, totalPrice);
            pstmt.setString(3, paymentMethod);
            pstmt.setString(4, orderStatus);
            
            // Shipping Details (using input parameters)
            pstmt.setString(5, name);
            pstmt.setString(6, state); 
            pstmt.setString(7, city);
            pstmt.setString(8, address);
            pstmt.setString(9, zip);
            
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
            JOptionPane.showMessageDialog(this, "SQL Error creating order: " + ex.getMessage(), "DB Error", JOptionPane.ERROR_MESSAGE);
        }
        return orderId;
    }
    
    /**
     * **REMOVED** - This method is no longer needed since the Users table does not have address columns.
     */
    /*
    private void saveShippingAddress(String name, String address, String city, String state, String zip) {
        // ... (removed logic) ...
    }
    */

    private boolean moveCartItemsToOrder(int orderId) {
        Connection conn = null;
        boolean success = false;
        
        String selectSql = "SELECT c.product_id, c.quantity, p.price " +
                              "FROM cart c JOIN Products p ON c.product_id = p.product_id " +
                              "WHERE c.username = ?"; 
        
        String insertSql = "INSERT INTO orderitems (order_id, product_id, quantity, price_at_purchase) VALUES (?, ?, ?, ?)";
        
        String deleteSql = "DELETE FROM cart WHERE username = ?";

        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false); 

            try (PreparedStatement selectPstmt = conn.prepareStatement(selectSql)) {
                selectPstmt.setString(1, username);
                try (ResultSet rs = selectPstmt.executeQuery();
                     PreparedStatement insertPstmt = conn.prepareStatement(insertSql)) {

                    while (rs.next()) {
                        int productId = rs.getInt("product_id");
                        int quantity = rs.getInt("quantity");
                        double price = rs.getDouble("price");

                        insertPstmt.setInt(1, orderId);
                        insertPstmt.setInt(2, productId);
                        insertPstmt.setInt(3, quantity);
                        insertPstmt.setDouble(4, price);
                        insertPstmt.addBatch(); 
                    }
                    insertPstmt.executeBatch(); 
                }
            }

            try (PreparedStatement deletePstmt = conn.prepareStatement(deleteSql)) {
                deletePstmt.setString(1, username);
                deletePstmt.executeUpdate();
                success = true;
            }
            
            conn.commit(); 
            
        } catch (SQLException ex) {
            try {
                if (conn != null) {
                    conn.rollback(); 
                    System.err.println("Transaction rolled back due to error.");
                }
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
    
    // ---------------------------------
    // --- Helper Methods (Unchanged) ---
    // ---------------------------------

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
