package uCSelf;
import javax.swing.*;
import java.awt.*;

public class JScrollPaneWithPanel {
    public static void main(String[] args) {
        JFrame frame = new JFrame("Nested JPanel with JScrollPane");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        JPanel pane = new JPanel();
        pane.setLayout(new BorderLayout());
        JPanel left = new JPanel();
        left.setSize(300,330);
        left.add(new JLabel("Hallo"));
        left.setBorder(BorderFactory.createLineBorder(Color.black));
        // Inner panel with many components
        JPanel innerPanel = new JPanel();
        Dimension d = new Dimension(400, 800);
        innerPanel.setSize(d);
        innerPanel.setMinimumSize(d);
        innerPanel.setPreferredSize(d);
        innerPanel.setMaximumSize(d);
        innerPanel.setLayout(new GridLayout(50, 1)); // 50 rows
        for (int i = 1; i <= 50; i++) {
            innerPanel.add(new JLabel("Label " + i));
        }

        // Wrap inner panel in a scroll pane
        JScrollPane scrollPane = new JScrollPane(innerPanel,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setBorder(BorderFactory.createLineBorder(Color.black));
        // Outer panel (acts as a container)
        JPanel outerPanel = new JPanel(new BorderLayout());
        outerPanel.add(scrollPane, BorderLayout.CENTER);

        pane.add("West", left);
        pane.add("East", outerPanel);
        
        // Add outer panel to frame
        frame.add(pane);

        // Make frame smaller than inner panel so scrollbars appear
        frame.setSize(800, 300);
        frame.setVisible(true);
    }
}

