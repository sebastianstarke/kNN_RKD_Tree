package Points2Map;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;

public class ChangeColorParameterFrame extends JFrame implements ActionListener {

	private JRadioButton[] r=new JRadioButton[17];
	private JRadioButton[] g=new JRadioButton[17];
	private JRadioButton[] b=new JRadioButton[17];
	
	private ViewPanel vp;

    public ChangeColorParameterFrame(final ViewPanel vp) {
	
		super("Surface Color");
		
		this.vp=vp;
		
		ButtonGroup rG=new ButtonGroup();
		ButtonGroup gG=new ButtonGroup();
		ButtonGroup bG=new ButtonGroup();

		for(int i=0;i<17;i++){r[i]=new JRadioButton("",false);rG.add(r[i]);r[i].addActionListener(this);}
		for(int i=0;i<17;i++){g[i]=new JRadioButton("",false);gG.add(g[i]);g[i].addActionListener(this);}
		for(int i=0;i<17;i++){b[i]=new JRadioButton("",false);bG.add(b[i]);b[i].addActionListener(this);}
		r[vp.colRSelection].setSelected(true);
		g[vp.colGSelection].setSelected(true);
		b[vp.colBSelection].setSelected(true);
		
        this.getContentPane().setLayout(new GridLayout(0,18));

		this.getContentPane().add(new JLabel(""));
		this.getContentPane().add(new JLabel("X"));
		this.getContentPane().add(new JLabel("Y"));
		this.getContentPane().add(new JLabel("Z"));
		this.getContentPane().add(new JLabel("Nx"));
		this.getContentPane().add(new JLabel("Ny"));
		this.getContentPane().add(new JLabel("Nz"));
		this.getContentPane().add(new JLabel("0.0"));
		this.getContentPane().add(new JLabel("0.1"));
		this.getContentPane().add(new JLabel("0.2"));
		this.getContentPane().add(new JLabel("0.3"));
		this.getContentPane().add(new JLabel("0.4"));
		this.getContentPane().add(new JLabel("0.5"));
		this.getContentPane().add(new JLabel("0.6"));
		this.getContentPane().add(new JLabel("0.7"));
		this.getContentPane().add(new JLabel("0.8"));
		this.getContentPane().add(new JLabel("0.9"));
		this.getContentPane().add(new JLabel("1.0"));
		this.getContentPane().add(new JLabel("R"));
		for(int i=0;i<17;i++)this.getContentPane().add(r[i]);
		this.getContentPane().add(new JLabel("G"));
		for(int i=0;i<17;i++)this.getContentPane().add(g[i]);
		this.getContentPane().add(new JLabel("B"));
		for(int i=0;i<17;i++)this.getContentPane().add(b[i]);

		this.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        this.addWindowListener(new WindowAdapter() {
                                   public void windowClosing(WindowEvent e) {
                                       System.exit(0);
                                   }
                               });
        this.pack();
		this.setVisible(true);
	}
	
	public void actionPerformed(ActionEvent event)  
	{  
		for(int i=0;i<17;i++){
			if(r[i].isSelected())vp.colRSelection=i;
			if(g[i].isSelected())vp.colGSelection=i;
			if(b[i].isSelected())vp.colBSelection=i;
			vp.fullPaint();
		}
	}
	
	public void resetParameters() {
	}
}