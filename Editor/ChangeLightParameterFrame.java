package Points2Map;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;

public class ChangeLightParameterFrame extends JFrame {

	private JSlider  specS, shadS, reflS, ambiS, dirlS, noisS;
	private Checkbox specC, shadC, reflC, ambiC, dirlC, noisC;

    public ChangeLightParameterFrame(final ViewPanel vp) {
	
		super("Light Parameters");
		
		double spec=vp.specular;
		double shad=vp.shadow;
		double refl=vp.mirror;
		double ambi=vp.ambient;
		double dirl=vp.brightness;
		double nois=vp.noiselevel;
		boolean specOn=vp.SPECULAR;
		boolean shadOn=vp.CONTOUR;
		boolean reflOn=vp.REFLECTIVE;
		boolean ambiOn=vp.AMBIENT;
		boolean dirlOn=vp.BRIGHTNESS;
		boolean noisOn=vp.NOISE;

        this.getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));

		specS = new JSlider(JSlider.HORIZONTAL,0,300,(int)(spec*100));
		specC = new Checkbox("On", null, specOn);
		
		shadS = new JSlider(JSlider.HORIZONTAL,0,300,(int)(shad*100));
		shadC = new Checkbox("On", null, shadOn);
		
		reflS = new JSlider(JSlider.HORIZONTAL,0,300,(int)(refl*300));
		reflC = new Checkbox("On", null, reflOn);
		
		ambiS = new JSlider(JSlider.HORIZONTAL,0,300,(int)(ambi*50));
		ambiC = new Checkbox("On", null, ambiOn);
		
		dirlS = new JSlider(JSlider.HORIZONTAL,0,300,(int)(dirl*100));
		dirlC = new Checkbox("On", null, dirlOn);

		noisS = new JSlider(JSlider.HORIZONTAL,0,300,(int)(nois*1000));
		noisC = new Checkbox("On", null, noisOn);

        JPanel specPanel=new JPanel(); specPanel.setLayout(new GridLayout(0, 1));
		specPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED),"Specularity"));
		specPanel.add(specC);
		specPanel.add(specS);
		this.getContentPane().add(specPanel);

        JPanel shadPanel=new JPanel(); shadPanel.setLayout(new GridLayout(0, 1));
		shadPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED),"Contour Shadow"));
		shadPanel.add(shadC);
		shadPanel.add(shadS);
		this.getContentPane().add(shadPanel);

        JPanel reflPanel=new JPanel(); reflPanel.setLayout(new GridLayout(0, 1));
		reflPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED),"Reflectivity"));
		reflPanel.add(reflC);
		reflPanel.add(reflS);
		this.getContentPane().add(reflPanel);

        JPanel ambiPanel=new JPanel(); ambiPanel.setLayout(new GridLayout(0, 1));
		ambiPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED),"Ambient Light"));
		ambiPanel.add(ambiC);
		ambiPanel.add(ambiS);
		this.getContentPane().add(ambiPanel);

        JPanel dirlPanel=new JPanel(); dirlPanel.setLayout(new GridLayout(0, 1));
		dirlPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED),"Directed Light"));
		dirlPanel.add(dirlC);
		dirlPanel.add(dirlS);
		this.getContentPane().add(dirlPanel);

        JPanel noisPanel=new JPanel(); noisPanel.setLayout(new GridLayout(0, 1));
		noisPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED),"Surface Roughness"));
		noisPanel.add(noisC);
		noisPanel.add(noisS);
		this.getContentPane().add(noisPanel);

		specS.addChangeListener(new ChangeListener() {@Override public void stateChanged(ChangeEvent e) {double v=((JSlider) e.getSource()).getValue();vp.specular=v/100;vp.fullPaint();}});
		shadS.addChangeListener(new ChangeListener() {@Override public void stateChanged(ChangeEvent e) {double v=((JSlider) e.getSource()).getValue();vp.shadow=v/100;vp.fullPaint();}});
		reflS.addChangeListener(new ChangeListener() {@Override public void stateChanged(ChangeEvent e) {double v=((JSlider) e.getSource()).getValue();vp.mirror=v/300;vp.fullPaint();}});
		ambiS.addChangeListener(new ChangeListener() {@Override public void stateChanged(ChangeEvent e) {double v=((JSlider) e.getSource()).getValue();vp.ambient=v/50;vp.fullPaint();}});
		dirlS.addChangeListener(new ChangeListener() {@Override public void stateChanged(ChangeEvent e) {double v=((JSlider) e.getSource()).getValue();vp.brightness=v/100;vp.fullPaint();}});
		noisS.addChangeListener(new ChangeListener() {@Override public void stateChanged(ChangeEvent e) {double v=((JSlider) e.getSource()).getValue();vp.noiselevel=v/1000;vp.fullPaint();}});
		
		specC.addItemListener(new ItemListener() {@Override public void itemStateChanged(ItemEvent e) {vp.SPECULAR=((Checkbox) e.getSource()).getState();vp.fullPaint();}});
		shadC.addItemListener(new ItemListener() {@Override public void itemStateChanged(ItemEvent e) {vp.CONTOUR=((Checkbox) e.getSource()).getState();vp.fullPaint();}});
		reflC.addItemListener(new ItemListener() {@Override public void itemStateChanged(ItemEvent e) {vp.REFLECTIVE=((Checkbox) e.getSource()).getState();vp.fullPaint();}});
		ambiC.addItemListener(new ItemListener() {@Override public void itemStateChanged(ItemEvent e) {vp.AMBIENT=((Checkbox) e.getSource()).getState();vp.fullPaint();}});
		dirlC.addItemListener(new ItemListener() {@Override public void itemStateChanged(ItemEvent e) {vp.BRIGHTNESS=((Checkbox) e.getSource()).getState();vp.fullPaint();}});
		noisC.addItemListener(new ItemListener() {@Override public void itemStateChanged(ItemEvent e) {vp.NOISE=((Checkbox) e.getSource()).getState();vp.fullPaint();}});

		this.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        this.addWindowListener(new WindowAdapter() {
                                   public void windowClosing(WindowEvent e) {
                                       System.exit(0);
                                   }
                               });
        this.pack();
		this.setVisible(true);
	}
	
	public void resetParameters() {
	}
}