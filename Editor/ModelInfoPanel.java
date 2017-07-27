package Points2Map;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;

public class ModelInfoPanel extends JPanel {

    public ModelInfoPanel(Vector3D min, Vector3D max, Vector3D mean, int numVerts) {
        this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        JPanel offPanel = new JPanel();
        offPanel.setLayout(new GridLayout(0, 1));
        offPanel.add(new JLabel("Number of Vertices:   " + numVerts));
        offPanel.add(new JLabel("Bounding Box Minimum:   " + min));
        offPanel.add(new JLabel("Bounding Box Maximum:   " + max));
        offPanel.add(new JLabel("Center of Gravity:   " + mean));
        offPanel.setBorder(BorderFactory.createTitledBorder(
                           BorderFactory.createEtchedBorder(),
                           "Current Pointcloud")
                          );
        this.add(offPanel);
    }
}