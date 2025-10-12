import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet; // Added for the new query
import java.sql.SQLException;
import java.sql.Types; 
import java.time.LocalDateTime;

@SuppressWarnings("serial")
public class FeedbackPage extends JFrame {

    private final String username;
    private final int orderId;
    private final AccountPage parentPage; 
    private JComboBox<Integer> ratingComboBox;
    private JTextArea commentArea;

    public FeedbackPage(String username, int orderId, AccountPage parentPage) {
        this.username = username;
        this.orderId = orderId;
        this.parentPage = parentPage;

        setTitle("Leave Feedback for Order #" + orderId);
        setSize(500, 450); 
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        getContentPane().setBackground(Color.WHITE);
        setLayout(new BorderLayout());

        add(createMainPanel(), BorderLayout.CENTER);
        add(createSubmitButton(), BorderLayout.SOUTH);

        setVisible(true);
    }

    private JPanel createMainPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(new EmptyBorder(20, 20, 10, 20));
        panel.setBackground(Color.WHITE);

        // Title
        JLabel titleLabel = new JLabel("Review Order #" + orderId, SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        titleLabel.setForeground(new Color(140, 40, 80));
        panel.add(titleLabel, BorderLayout.NORTH);

        // Input Fields Panel (Rating)
        JPanel ratingPanel = new JPanel(new GridLayout(1, 2, 10, 10)); // Used 'ratingPanel'
        ratingPanel.setBackground(Color.WHITE);
        
        // FIX: Used 'ratingPanel' here
        ratingPanel.add(new JLabel("Rating (1-5):")); 
        Integer[] ratings = {5, 4, 3, 2, 1};
        ratingComboBox = new JComboBox<>(ratings);
        ratingPanel.add(ratingComboBox);
        
        // Comment Area
        commentArea = new JTextArea(6, 20); 
        commentArea.setLineWrap(true);
        commentArea.setWrapStyleWord(true);
        JScrollPane commentScrollPane = new JScrollPane(commentArea);
        commentScrollPane.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));

        JPanel commentWrapper = new JPanel(new BorderLayout());
        commentWrapper.setBackground(Color.WHITE);
        commentWrapper.setBorder(new EmptyBorder(20, 0, 0, 0)); 
        commentWrapper.add(new JLabel("Comment:"), BorderLayout.NORTH);
        commentWrapper.add(commentScrollPane, BorderLayout.CENTER);

        JPanel centerContainer = new JPanel(new BorderLayout(0, 10));
        centerContainer.setBackground(Color.WHITE);
        centerContainer.add(ratingPanel, BorderLayout.NORTH);
        centerContainer.add(commentWrapper, BorderLayout.CENTER);
        
        panel.add(centerContainer, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createSubmitButton() {
        // ... (Submit Button setup is unchanged)
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(0, 20, 20, 20));

        JButton submitBtn = new JButton("Submit Feedback");
        submitBtn.setFont(new Font("Arial", Font.BOLD, 16));
        submitBtn.setBackground(new Color(34, 139, 34)); 
        submitBtn.setForeground(Color.WHITE);
        submitBtn.setFocusPainted(false);
        submitBtn.addActionListener(e -> submitFeedback());
        
        panel.add(submitBtn);
        return panel;
    }

    // Inside FeedbackPage.java

private void submitFeedback() {
    int rating = (Integer) ratingComboBox.getSelectedItem(); // Corresponds to 'rate'
    String comment = commentArea.getText().trim();
    
    int productId = -1; 
    
    // 1. Find the Product ID associated with this Order 
    // This is mandatory because 'product_id' cannot be NULL in your table.
    String findProductSql = "SELECT product_id FROM OrderItems WHERE order_id = ? LIMIT 1";

    try (Connection conn = DBConnection.getConnection()) {
        
        // --- Step 1: Find Product ID ---
        try (PreparedStatement findPs = conn.prepareStatement(findProductSql)) {
            findPs.setInt(1, orderId);
            try (ResultSet rs = findPs.executeQuery()) {
                if (rs.next()) {
                    productId = rs.getInt("product_id");
                }
            }
        }

        if (productId == -1) {
             JOptionPane.showMessageDialog(this,
                "Error: No product found linked to Order #" + orderId + ". Cannot save feedback.",
                "Data Missing", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        // --- Step 2: INSERT Feedback ---
        // ⭐️ Final SQL Statement with 8 non-PK columns ⭐️
        String sql = "INSERT INTO Feedback (product_id, username, rate, quality, details, submission_date, order_id, comment) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?)"; // 8 placeholders
        
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            // 1. Collected/Found Data
            pstmt.setInt(1, productId);         // product_id (from OrderItems)
            pstmt.setString(2, username);       // username
            pstmt.setInt(3, rating);            // rate
            
            // 2. Uncollected Data (Set to NULL)
            pstmt.setNull(4, Types.VARCHAR);    // quality (NULL)
            pstmt.setNull(5, Types.VARCHAR);    // details (NULL)
            
            // 3. Collected Data
            pstmt.setObject(6, LocalDateTime.now()); // submission_date
            pstmt.setInt(7, orderId);           // order_id
            pstmt.setString(8, comment);        // comment
            
            pstmt.executeUpdate();
            
            JOptionPane.showMessageDialog(this,
                "Thank you for your feedback! Your review has been saved.",
                "Success", JOptionPane.INFORMATION_MESSAGE);
            
            if (parentPage != null) {
                parentPage.displayOrderHistory(); 
            }
            
            dispose(); 
        }

    } catch (SQLException ex) {
        JOptionPane.showMessageDialog(this,
            "Error submitting feedback. Details: " + ex.getMessage(),
            "Database Error", JOptionPane.ERROR_MESSAGE);
        ex.printStackTrace();
    }
}
}