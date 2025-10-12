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
    // subtotalPrice is now set by the constructor from the CartPage
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

    // ⭐️ CORRECTED CONSTRUCTOR: Accepts the subtotal price from cartpage ⭐️
    public PaymentPage(String username, double subtotalPriceFromCart) {
        this.username = username;
        this.subtotalPrice = subtotalPriceFromCart; // Set the class variable immediately
        
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

        // Calculate and display totals using the passed subtotal
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
            // Ensure cartpage constructor only takes username if you removed the total from its constructor
            // but based on your corrected cartpage, it only takes username.
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

    // loadCartSubtotal() method has been removed as it is now redundant.

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

        int orderId = createOrderRecord(finalTotal, paymentMethod, orderStatus);

        if (orderId > 0) {
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

        new homePage(username); 
        dispose();
    }

    private int createOrderRecord(double totalPrice, String paymentMethod, String orderStatus) {
        String sql = "INSERT INTO Orders (username, total_amount, payment_method, status, order_date) VALUES (?, ?, ?, ?, ?)";
        int orderId = -1;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            pstmt.setString(1, username);
            pstmt.setDouble(2, totalPrice);
            pstmt.setString(3, paymentMethod);
            pstmt.setString(4, orderStatus);
            pstmt.setTimestamp(5, new java.sql.Timestamp(new Date().getTime()));
            
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

    private boolean moveCartItemsToOrder(int orderId) {
        // SQL to link all current cart items to the new order_id and record the final price
        String mysqlSql = "UPDATE Cart c JOIN Products p ON c.product_id = p.product_id " +
                         "SET c.order_id = ?, c.final_price = p.price * c.quantity " +
                         "WHERE c.username = ? AND c.order_id IS NULL";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(mysqlSql)) {
            
            pstmt.setInt(1, orderId);
            pstmt.setString(2, username);
            
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
            
        } catch (SQLException ex) {
            System.err.println("Error moving cart items to order: " + ex.getMessage());
            ex.printStackTrace();
            return false;
        }
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