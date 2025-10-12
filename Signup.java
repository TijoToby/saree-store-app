

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

@SuppressWarnings("serial")
public class Signup extends JFrame {

    private final JTextField userField;
    private final JPasswordField passField;
    private final JPasswordField confirmField;

    public Signup() {
        setTitle("SareeStore - Sign Up");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        
        // ... (UI setup code) ...
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(new EmptyBorder(50, 150, 50, 150));
        mainPanel.setBackground(Color.WHITE);

        JLabel storeLabel = new JLabel(" SareeStore ", SwingConstants.CENTER);
        storeLabel.setFont(new Font("Serif", Font.BOLD, 36));
        storeLabel.setForeground(new Color(140, 40, 80));
        storeLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        mainPanel.add(storeLabel);

        JLabel tagline = new JLabel("Grace in Every Drape", SwingConstants.CENTER);
        tagline.setFont(new Font("SansSerif", Font.ITALIC, 18));
        tagline.setForeground(new Color(120, 80, 120));
        tagline.setAlignmentX(Component.CENTER_ALIGNMENT);
        mainPanel.add(tagline);
        mainPanel.add(Box.createVerticalStrut(40));

        JLabel titleLabel = new JLabel("Create Your Account", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 26));
        titleLabel.setForeground(new Color(80, 60, 90));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        mainPanel.add(titleLabel);
        mainPanel.add(Box.createVerticalStrut(30));

        JPanel userPanel = new JPanel(new BorderLayout(8, 8));
        userPanel.setBackground(Color.WHITE);
        JLabel userLabel = new JLabel("Username:");
        userLabel.setFont(new Font("Arial", Font.BOLD, 18));
        userField = new JTextField();
        userField.setPreferredSize(new Dimension(400, 35));
        userField.setBackground(new Color(245, 245, 245));
        userPanel.add(userLabel, BorderLayout.WEST);
        userPanel.add(userField, BorderLayout.CENTER);
        mainPanel.add(userPanel);
        mainPanel.add(Box.createVerticalStrut(20));

        JPanel passPanel = new JPanel(new BorderLayout(8, 8));
        passPanel.setBackground(Color.WHITE);
        JLabel passLabel = new JLabel("Password:");
        passLabel.setFont(new Font("Arial", Font.BOLD, 18));
        passField = new JPasswordField();
        passField.setPreferredSize(new Dimension(400, 35));
        passField.setBackground(new Color(245, 245, 245));
        passPanel.add(passLabel, BorderLayout.WEST);
        passPanel.add(passField, BorderLayout.CENTER);
        mainPanel.add(passPanel);
        mainPanel.add(Box.createVerticalStrut(20));

        JPanel confirmPanel = new JPanel(new BorderLayout(8, 8));
        confirmPanel.setBackground(Color.WHITE);
        JLabel confirmLabel = new JLabel("Confirm Password:");
        confirmLabel.setFont(new Font("Arial", Font.BOLD, 18));
        confirmField = new JPasswordField();
        confirmField.setPreferredSize(new Dimension(400, 35));
        confirmField.setBackground(new Color(245, 245, 245));
        confirmPanel.add(confirmLabel, BorderLayout.WEST);
        confirmPanel.add(confirmField, BorderLayout.CENTER);
        mainPanel.add(confirmPanel);
        mainPanel.add(Box.createVerticalStrut(30));

        JButton signUpBtn = new JButton("Sign Up");
        signUpBtn.setFont(new Font("Arial", Font.BOLD, 18));
        signUpBtn.setBackground(new Color(153, 102, 255));
        signUpBtn.setForeground(Color.WHITE);
        signUpBtn.setFocusPainted(false);
        signUpBtn.setOpaque(true);
        signUpBtn.setBorderPainted(false);
        signUpBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        JPanel btnPanel = new JPanel();
        btnPanel.setBackground(Color.WHITE);
        btnPanel.add(signUpBtn);
        mainPanel.add(btnPanel);
        mainPanel.add(Box.createVerticalStrut(20));

        JButton backToLoginBtn = new JButton("Already have an account? Back to Login");
        backToLoginBtn.setFont(new Font("Arial", Font.PLAIN, 16));
        backToLoginBtn.setBorderPainted(false);
        backToLoginBtn.setContentAreaFilled(false);
        backToLoginBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        backToLoginBtn.setForeground(new Color(90, 70, 130));
        backToLoginBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        mainPanel.add(backToLoginBtn);

        add(mainPanel);
        pack();
        setLocationRelativeTo(null); 
        setVisible(true);

        // --- Action Listeners ---
        signUpBtn.addActionListener(e -> {
            String username = userField.getText().trim();
            String password = new String(passField.getPassword());
            String confirm = new String(confirmField.getPassword());
            
            if (username.isEmpty() || password.isEmpty() || confirm.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please fill all fields.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            } 
            if (!password.equals(confirm)) {
                JOptionPane.showMessageDialog(this, "Passwords do not match.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            } 
            
            if (registerUser(username, password)) {
                JOptionPane.showMessageDialog(this, "Welcome, " + username + "! Account created successfully 🎉", "Success", JOptionPane.INFORMATION_MESSAGE);
                new homePage(username); 
                dispose();
            }
        });

        backToLoginBtn.addActionListener(e -> {
            new ui(); 
            dispose();
        });
    }

    /** Registers a new user into the 'users' table. */
    private boolean registerUser(String username, String password) {
        String sql = "INSERT INTO users (username, password) VALUES (?, ?)";
        
        try (Connection conn = DBConnection.getConnection()) {
            
            if (isUsernameTaken(conn, username)) {
                 return false;
            }

            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, username);
                pstmt.setString(2, password);
                
                int rowsAffected = pstmt.executeUpdate();
                return rowsAffected > 0;
            }
            
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "A database error occurred during registration: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
            return false;
        }
    }
    
    /** Helper method to check if the username already exists. */
    private boolean isUsernameTaken(Connection conn, String username) throws SQLException {
        String checkSql = "SELECT COUNT(*) FROM users WHERE username = ?";
        try (PreparedStatement checkPstmt = conn.prepareStatement(checkSql)) {
            checkPstmt.setString(1, username);
            try (ResultSet rs = checkPstmt.executeQuery()) {
                if (rs.next() && rs.getInt(1) > 0) {
                    JOptionPane.showMessageDialog(this, "Username '" + username + "' is already taken. Please choose another.", "Registration Failed", JOptionPane.ERROR_MESSAGE);
                    return true;
                }
                return false;
            }
        }
    }
}