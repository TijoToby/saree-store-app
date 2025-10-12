import javax.swing.SwingUtilities;

public class AdminLauncher {
    
    // ⚠️ IMPORTANT: Change this to a valid username 
    // that exists in your Users or Admin database table.
    private static final String TEST_ADMIN_USERNAME = "admin_test"; 

    public static void main(String[] args) {
        
        // Ensure that GUI creation and manipulation occur on the Event Dispatch Thread (EDT)
        SwingUtilities.invokeLater(() -> {
            try {
                // Initialize the AdminPage
                new AdminPage(TEST_ADMIN_USERNAME);
            } catch (Exception e) {
                System.err.println("Failed to launch AdminPage: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }
}