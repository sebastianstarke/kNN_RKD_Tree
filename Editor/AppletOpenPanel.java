package Points2Map;

import java.net.*;
import java.util.*;
import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;


public class AppletOpenPanel extends JPanel {

    public JComboBox selectionList;

    public AppletOpenPanel(URL[] files) {

        Vector filenames = new Vector();
        int numFiles = files.length;
        String filePath = null;
        String fileOnly = null;
        int slashIndex = -1;
        for (int i = 0;i < numFiles;i++ ) {
            filePath = files[i].getFile();
            slashIndex = filePath.lastIndexOf('/');
            if (slashIndex != -1) {
                fileOnly = filePath.substring(slashIndex + 1);
            } else {
                fileOnly = filePath;
            }
            filenames.add(fileOnly);
        }
        selectionList = new JComboBox(filenames);
        selectionList.setSelectedIndex(0);

        this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        JPanel labelPanel = new JPanel();
        labelPanel.add(new JLabel("Select an OFF file to load."));
        this.add(labelPanel);

        JPanel comboPanel = new JPanel();
        comboPanel.add(selectionList);
        this.add(comboPanel);
    }

    public int getSelectedIndex() {
        return selectionList.getSelectedIndex();
    }
}