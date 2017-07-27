package Points2Map;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;

public class ViewParameterPanel extends JPanel {

    private JTextField atX;
    private JTextField atY;
    private JTextField atZ;

    private JTextField fromX;
    private JTextField fromY;
    private JTextField fromZ;

    private JTextField upX;
    private JTextField upY;
    private JTextField upZ;

    private JTextField viewAngle;

    public ViewParameterPanel(Vector3D at, Vector3D from, Vector3D up, double viewAngle) {
    	
        this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        atX = new JTextField(getStringValue(at.p[0]), 10);
        atY = new JTextField(getStringValue(at.p[1]), 10);
        atZ = new JTextField(getStringValue(at.p[2]), 10);

        fromX = new JTextField(getStringValue(from.p[0]), 10);
        fromY = new JTextField(getStringValue(from.p[1]), 10);
        fromZ = new JTextField(getStringValue(from.p[2]), 10);

        upX = new JTextField(getStringValue(up.p[0]), 10);
        upY = new JTextField(getStringValue(up.p[1]), 10);
        upZ = new JTextField(getStringValue(up.p[2]), 10);

        this.viewAngle = new JTextField(Double.toString(viewAngle), 5);

        JPanel vecPanel;
        JPanel compPanel;

        vecPanel = new JPanel();
        vecPanel.setLayout(new GridLayout(0, 1));
        vecPanel.setBorder(BorderFactory.createTitledBorder(
                           BorderFactory.createBevelBorder(BevelBorder.LOWERED),
                           "At Point")
                          );

        compPanel = new JPanel();
        compPanel.add(new JLabel("X:"));
        compPanel.add(atX);
        vecPanel.add(compPanel);

        compPanel = new JPanel();
        compPanel.add(new JLabel("Y:"));
        compPanel.add(atY);
        vecPanel.add(compPanel);

        compPanel = new JPanel();
        compPanel.add(new JLabel("Z:"));
        compPanel.add(atZ);
        vecPanel.add(compPanel);
        add(vecPanel);

        vecPanel = new JPanel();
        vecPanel.setLayout(new GridLayout(0, 1));
        vecPanel.setBorder(BorderFactory.createTitledBorder(
                           BorderFactory.createBevelBorder(BevelBorder.LOWERED),
                           "From Point")
                          );

        compPanel = new JPanel();
        compPanel.add(new JLabel("X:"));
        compPanel.add(fromX);
        vecPanel.add(compPanel);

        compPanel = new JPanel();
        compPanel.add(new JLabel("Y:"));
        compPanel.add(fromY);
        vecPanel.add(compPanel);

        compPanel = new JPanel();
        compPanel.add(new JLabel("Z:"));
        compPanel.add(fromZ);
        vecPanel.add(compPanel);
        add(vecPanel);

        vecPanel = new JPanel();
        vecPanel.setLayout(new GridLayout(0, 1));
        vecPanel.setBorder(BorderFactory.createTitledBorder(
                           BorderFactory.createBevelBorder(BevelBorder.LOWERED),
                           "Up Vector")
                          );

        compPanel = new JPanel();
        compPanel.add(new JLabel("X:"));
        compPanel.add(upX);
        vecPanel.add(compPanel);

        compPanel = new JPanel();
        compPanel.add(new JLabel("Y:"));
        compPanel.add(upY);
        vecPanel.add(compPanel);

        compPanel = new JPanel();
        compPanel.add(new JLabel("Z:"));
        compPanel.add(upZ);
        vecPanel.add(compPanel);
        add(vecPanel);

        vecPanel = new JPanel();
        vecPanel.setLayout(new GridLayout(0, 1));
        vecPanel.setBorder(BorderFactory.createTitledBorder(
                           BorderFactory.createBevelBorder(BevelBorder.LOWERED),
                           "View Angle")
                          );
        compPanel = new JPanel();
        compPanel.add(this.viewAngle);
        vecPanel.add(compPanel);
        add(vecPanel);
    }

    public double getAngle() {
        return getDoubleValue(viewAngle);
    }

    public Vector3D getAt() {
        return new Vector3D(getDoubleValue(atX),
                              getDoubleValue(atY),
                              getDoubleValue(atZ));
    }

    public Vector3D getFrom() {
        return new Vector3D(getDoubleValue(fromX),
                              getDoubleValue(fromY),
                              getDoubleValue(fromZ));
    }

    public Vector3D getUp() {
        return new Vector3D(getDoubleValue(upX),
                              getDoubleValue(upY),
                              getDoubleValue(upZ));
    }

    private double getDoubleValue(JTextField inputBox) {

        double rval = 0;
        try {
            rval = Double.parseDouble(inputBox.getText());
        } catch (NumberFormatException e) {
            rval = 0;
        }
        return rval;
    }

    private String getStringValue(double number) {
        String rval = Double.toString(number);
        if (rval.length() > 8) {
            rval = rval.substring(0, 8);
        }
        return rval;
    }
}