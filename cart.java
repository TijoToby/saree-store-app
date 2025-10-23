

import javax.swing.JFrame;
import javax.swing.JLabel;
import java.awt.Dimension;

@SuppressWarnings("serial")
public class cart extends JFrame {

    public cart() {
        setTitle("SareeStore - Cart");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE); 
        setSize(new Dimension(800, 600));
        setLocationRelativeTo(null); 
        
        JLabel label = new JLabel("Cart Page (Content coming soon!)", JLabel.CENTER);
        add(label);
        
        setVisible(true);
    }
}
