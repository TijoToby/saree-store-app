import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

@SuppressWarnings("serial")
public class ui extends JFrame {

    private final JTextField userField;
    private final JPasswordField passField;

    public ui() {
        setTitle("SareeStore - Login");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH);

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(new EmptyBorder(50, 150, 50, 150));
        mainPanel.setBackground(Color.WHITE);

        JLabel titleLabel = new JLabel(" SareeStore ", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Serif", Font.BOLD, 36));
        titleLabel.setForeground(new Color(140, 40, 80));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        mainPanel.add(titleLabel);

        JLabel tagline = new JLabel("Elegance in Every Weave", SwingConstants.CENTER);
        tagline.setFont(new Font("SansSerif", Font.ITALIC, 18));
        tagline.setForeground(new Color(120, 80, 120));
        tagline.setAlignmentX(Component.CENTER_ALIGNMENT);
        mainPanel.add(tagline);
        mainPanel.add(Box.createVerticalStrut(40));

        JLabel loginLabel = new JLabel("Login to Your Account", SwingConstants.CENTER);
        loginLabel.setFont(new Font("Arial", Font.BOLD, 26));
        loginLabel.setForeground(new Color(80, 60, 90));
        loginLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        mainPanel.add(loginLabel);
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

        JButton loginBtn = new JButton("Login");
        loginBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        loginBtn.setFont(new Font("Arial", Font.BOLD, 18));
        loginBtn.setBackground(new Color(153, 102, 255));
        loginBtn.setForeground(Color.WHITE);
        loginBtn.setFocusPainted(false);
        loginBtn.setOpaque(true);
        loginBtn.setBorderPainted(false);
        loginBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        JPanel btnPanel = new JPanel();
        btnPanel.setBackground(Color.WHITE);
        btnPanel.add(loginBtn);
        mainPanel.add(btnPanel);
        mainPanel.add(Box.createVerticalStrut(30));

        JButton signupLinkBtn = new JButton("Don't have an account? Sign up here");
        signupLinkBtn.setFont(new Font("Arial", Font.PLAIN, 16));
        signupLinkBtn.setBorderPainted(false);
        signupLinkBtn.setContentAreaFilled(false);
        signupLinkBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        signupLinkBtn.setForeground(new Color(90, 70, 130));
        signupLinkBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        mainPanel.add(signupLinkBtn);

        add(mainPanel);
        pack();
        setLocationRelativeTo(null);
        setVisible(true);

        loginBtn.addActionListener(e -> {
            String username = userField.getText().trim();
            String password = new String(passField.getPassword());

            if (username.isEmpty() || password.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please enter username and password.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (authenticateUser(username, password)) {
                JOptionPane.showMessageDialog(this, "Login Successful! Welcome, " + username + ".", "Success", JOptionPane.INFORMATION_MESSAGE);
                new homePage(username);
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, "Invalid Username or Password.", "Login Failed", JOptionPane.ERROR_MESSAGE);
            }
        });

        signupLinkBtn.addActionListener(e -> {
            new Signup();
            dispose();
        });
    }

    private boolean authenticateUser(String username, String password) {
        String sql = "SELECT password FROM users WHERE username = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                String stored = rs.getString("password");
                return stored.equals(password);
            }
            return false;
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Database error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(ui::new);
    }
}
