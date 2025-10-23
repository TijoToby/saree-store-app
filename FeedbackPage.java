import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet; 
import java.sql.SQLException;
import java.sql.Types; 
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Vector; // Used for JComboBox items

@SuppressWarnings("serial")
public class FeedbackPage extends JFrame {

    private final String username;
    private final int orderId;
    private final AccountPage parentPage; 
    private final Map<Integer, String> itemsToReview; // NEW: Map of Product ID -> Product Name
    
    private JComboBox<Integer> ratingComboBox;
    private JComboBox<String> itemComboBox; // NEW: Item selector
    private JTextArea commentArea;

    // ⭐️ UPDATED CONSTRUCTOR SIGNATURE ⭐️
    public FeedbackPage(String username, int orderId, Map<Integer, String> items, AccountPage parentPage) {
        this.username = username;
        this.orderId = orderId;
        this.itemsToReview = items; // Store the items passed from AccountPage
        this.parentPage = parentPage;

        setTitle("Leave Feedback for Order #" + orderId);
        setSize(500, 500); // Increased size slightly to accommodate the new field
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

        // Input Fields Container
        JPanel inputContainer = new JPanel();
        inputContainer.setLayout(new BoxLayout(inputContainer, BoxLayout.Y_AXIS));
        inputContainer.setBackground(Color.WHITE);
        inputContainer.setBorder(new EmptyBorder(10, 0, 0, 0));

        // ⭐️ NEW: Item Selection Panel ⭐️
        JPanel itemPanel = new JPanel(new GridLayout(1, 2, 10, 10));
        itemPanel.setBackground(Color.WHITE);
        
        Vector<String> itemNames = new Vector<>(itemsToReview.values());
        itemComboBox = new JComboBox<>(itemNames);
        
        itemPanel.add(new JLabel("Review Item:")); 
        itemPanel.add(itemComboBox);
        inputContainer.add(itemPanel);
        inputContainer.add(Box.createVerticalStrut(15)); // Spacer
        
        // Rating Panel
        JPanel ratingPanel = new JPanel(new GridLayout(1, 2, 10, 10));
        ratingPanel.setBackground(Color.WHITE);
        ratingPanel.add(new JLabel("Rating (1-5):")); 
        Integer[] ratings = {5, 4, 3, 2, 1};
        ratingComboBox = new JComboBox<>(ratings);
        ratingPanel.add(ratingComboBox);
        inputContainer.add(ratingPanel);
        inputContainer.add(Box.createVerticalStrut(15)); // Spacer
        
        // Comment Area
        commentArea = new JTextArea(6, 20); 
        commentArea.setLineWrap(true);
        commentArea.setWrapStyleWord(true);
        JScrollPane commentScrollPane = new JScrollPane(commentArea);
        commentScrollPane.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));

        JPanel commentWrapper = new JPanel(new BorderLayout());
        commentWrapper.setBackground(Color.WHITE);
        commentWrapper.add(new JLabel("Comment:"), BorderLayout.NORTH);
        commentWrapper.add(commentScrollPane, BorderLayout.CENTER);
        
        inputContainer.add(commentWrapper);

        panel.add(inputContainer, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createSubmitButton() {
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

    private void submitFeedback() {
        int rating = (Integer) ratingComboBox.getSelectedItem();
        String comment = commentArea.getText().trim();
        String selectedItemName = (String) itemComboBox.getSelectedItem();
        
        // 1. Find the Product ID based on the selected item name
        int productId = -1;
        for (Entry<Integer, String> entry : itemsToReview.entrySet()) {
            if (entry.getValue().equals(selectedItemName)) {
                productId = entry.getKey();
                break;
            }
        }
        
        if (productId == -1) {
            JOptionPane.showMessageDialog(this,
                "Internal Error: Could not resolve Product ID for the selected item.",
                "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        // 2. Insert Feedback into the Database
        // We use the collected productId instead of querying the DB again.
        String sql = "INSERT INTO Feedback (product_id, username, rate, quality, details, submission_date, order_id, comment) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            // 1. Collected/Found Data
            pstmt.setInt(1, productId);// product_id (from selected item)
            pstmt.setString(2, username);// username
            pstmt.setInt(3, rating);// rate
            
            // 2. Uncollected Data (Set to NULL)
            pstmt.setNull(4, Types.VARCHAR);// quality (NULL)
            pstmt.setNull(5, Types.VARCHAR);// details (NULL)
            
            // 3. Collected Data
            pstmt.setObject(6, LocalDateTime.now()); // submission_date
            pstmt.setInt(7, orderId);// order_id
            pstmt.setString(8, comment);// comment
            
            pstmt.executeUpdate();
            
            JOptionPane.showMessageDialog(this,
                "Thank you for your feedback on '" + selectedItemName + "'! Your review has been saved.",
                "Success", JOptionPane.INFORMATION_MESSAGE);
            
            // Tell the parent AccountPage to refresh the order list
            if (parentPage != null) {
                parentPage.displayOrderHistory(); 
                parentPage.setVisible(true); // Ensure AccountPage is visible again
            }
            
            dispose(); 
            
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this,
                "Error submitting feedback. Details: " + ex.getMessage(),
                "Database Error", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }
}
