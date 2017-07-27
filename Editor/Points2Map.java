package Points2Map;


import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.PlainDocument;
import javax.swing.text.html.HTMLDocument.HTMLReader.SpecialAction;
import javax.swing.text.AttributeSet;

import java.io.*;
import java.net.*;
import java.awt.*;
import java.text.NumberFormat;
import java.util.*;
import java.awt.event.*;
import javax.imageio.*;

import java.io.FileWriter; 
import java.io.IOException; 
import java.io.PrintWriter; 

public class Points2Map extends JFrame implements ActionListener {

    private Mesh mesh;
    private final JFileChooser fc;
    private JFileChooser exportFC;
    private File currentFile;
    private ViewPanel vp;
	private JTextArea status;
	private OutputStream out;
    private boolean inApplet;
    private URL[] appletURLs;
	private ChangeLightParameterFrame clpp;
	private ChangeColorParameterFrame ccpp;

    public Points2Map(boolean inApplet) {
        super("Points2Map");

        this.inApplet = inApplet;
        appletURLs = null;

        if (!inApplet) {
            fc = new JFileChooser();
        } else {
            fc = null;
        } 
        exportFC = null;
        currentFile = null;
        mesh = new Mesh();

        JMenuBar menubar = new JMenuBar();
        JMenu menu = new JMenu("File");
        menu.setMnemonic(KeyEvent.VK_F);
        JMenuItem menuItem;

        menuItem = new JMenuItem("Open...");
        menuItem.setActionCommand("OPEN");
        menuItem.setMnemonic(KeyEvent.VK_O);
        menuItem.addActionListener(this);
        menu.add(menuItem);

        menu.addSeparator();

        if (!inApplet) {
            menuItem = new JMenuItem("Save");
            menuItem.setActionCommand("SAVE");
            menuItem.setMnemonic(KeyEvent.VK_S);
            menuItem.addActionListener(this);
            menu.add(menuItem);

            menuItem = new JMenuItem("Save As...");
            menuItem.setActionCommand("SAVEAS");
            menuItem.setMnemonic(KeyEvent.VK_A);
            menuItem.addActionListener(this);
            menu.add(menuItem);

            menuItem = new JMenuItem("Extract Ground...");
            menuItem.setActionCommand("GROUND");
            menuItem.setMnemonic(KeyEvent.VK_G);
            menuItem.addActionListener(this);
            menu.add(menuItem);

            menuItem = new JMenuItem("Reduce to 5cm x 5cm Grid Resolution...");
            menuItem.setActionCommand("RESOLUTION");
            menuItem.setMnemonic(KeyEvent.VK_R);
            menuItem.addActionListener(this);
            menu.add(menuItem);

            menuItem = new JMenuItem("Save Map...");
            menuItem.setActionCommand("MAP");
            menuItem.setMnemonic(KeyEvent.VK_M);
            menuItem.addActionListener(this);
            menu.add(menuItem);

            menu.addSeparator();
        }

        menuItem = new JMenuItem("Exit");
        menuItem.setActionCommand("EXIT");
        menuItem.setMnemonic(KeyEvent.VK_X);
        menuItem.addActionListener(this);
        menu.add(menuItem);

        menubar.add(menu);

        menu = new JMenu("View");
        menu.setMnemonic(KeyEvent.VK_V);

        menuItem = new JMenuItem("View Model Information...");
        menuItem.setActionCommand("V_MODELINFO");
        menuItem.setMnemonic(KeyEvent.VK_I);
        menuItem.addActionListener(this);
        menu.add(menuItem);

        menu.addSeparator();

        menuItem = new JMenuItem("Set View Parameters...");
        menuItem.setActionCommand("V_SETVIEW");
        menuItem.setMnemonic(KeyEvent.VK_P);
        menuItem.addActionListener(this);
        menu.add(menuItem);

        menuItem = new JMenuItem("Set to Default View");
        menuItem.setActionCommand("V_SETDEFAULT");
        menuItem.setMnemonic(KeyEvent.VK_V);
        menuItem.addActionListener(this);
        menu.add(menuItem);

        menu.addSeparator();

        menuItem = new JMenuItem("Set Light Parameters...");
        menuItem.setActionCommand("V_SETLIGHT");
        menuItem.setMnemonic(KeyEvent.VK_L);
        menuItem.addActionListener(this);
        menu.add(menuItem);

        menu.addSeparator();

        menuItem = new JMenuItem("Set Color Parameters...");
        menuItem.setActionCommand("V_SETCOLOR");
        menuItem.setMnemonic(KeyEvent.VK_C);
        menuItem.addActionListener(this);
        menu.add(menuItem);

        menuItem = new JMenuItem("Change Color Mode...");
        menuItem.setActionCommand("V_COLMODE");
        menuItem.setMnemonic(KeyEvent.VK_C);
        menuItem.addActionListener(this);
        menu.add(menuItem);

        menu.addSeparator();

        menuItem = new JMenuItem("Toggle Backface Lighting");
        menuItem.setActionCommand("BACKLIGHT");
        menuItem.setMnemonic(KeyEvent.VK_B);
        menuItem.addActionListener(this);
        menu.add(menuItem);

		menuItem = new JMenuItem("Invert Normal Directions");
        menuItem.setActionCommand("INVERT");
        menuItem.setMnemonic(KeyEvent.VK_I);
        menuItem.addActionListener(this);
        menu.add(menuItem);

        menu.addSeparator();

		menuItem = new JMenuItem("Additional Normal Guess");
        menuItem.setActionCommand("GUESS");
        menuItem.setMnemonic(KeyEvent.VK_G);
        menuItem.addActionListener(this);
        menu.add(menuItem);

		menuItem = new JMenuItem("Compute Normals");
        menuItem.setActionCommand("COMPUTE");
        menuItem.setMnemonic(KeyEvent.VK_N);
        menuItem.addActionListener(this);
        menu.add(menuItem);

        menu.addSeparator();
        
		menuItem = new JMenuItem("Automatic kNN-Guess");
        menuItem.setActionCommand("kNNa");
        menuItem.setMnemonic(KeyEvent.VK_1);
        menuItem.addActionListener(this);
        menu.add(menuItem);
        
		menuItem = new JMenuItem("Manual kNN-Guess");
        menuItem.setActionCommand("kNNm");
        menuItem.setMnemonic(KeyEvent.VK_2);
        menuItem.addActionListener(this);
        menu.add(menuItem);
        
		menuItem = new JMenuItem("Additional kNN-Guess");
        menuItem.setActionCommand("kNN+");
        menuItem.setMnemonic(KeyEvent.VK_3);
        menuItem.addActionListener(this);
        menu.add(menuItem);
        
        /*
		menuItem = new JMenuItem("Generate kNN point cloud");
        menuItem.setActionCommand("kNNpc");
        menuItem.setMnemonic(KeyEvent.VK_4);
        menuItem.addActionListener(this);
        menu.add(menuItem);

        menu.addSeparator();
        
		menuItem = new JMenuItem("Add noise to vertices");
        menuItem.setActionCommand("noise");
        menuItem.setMnemonic(KeyEvent.VK_4);
        menuItem.addActionListener(this);
        menu.add(menuItem);

		menuItem = new JMenuItem("Computation Time");
        menuItem.setActionCommand("CT");
        menuItem.setMnemonic(KeyEvent.VK_N);
        menuItem.addActionListener(this);
        menu.add(menuItem);
        
		menuItem = new JMenuItem("Linear Search (MaxHeap)");
        menuItem.setActionCommand("LSMH");
        menuItem.setMnemonic(KeyEvent.VK_N);
        menuItem.addActionListener(this);
        menu.add(menuItem);
        
		menuItem = new JMenuItem("Linear Search (QuickSort)");
        menuItem.setActionCommand("LSQS");
        menuItem.setMnemonic(KeyEvent.VK_N);
        menuItem.addActionListener(this);
        menu.add(menuItem);
        */
        
        menu.addSeparator();

		menuItem = new JMenuItem("Close Holes");
        menuItem.setActionCommand("HOLES");
        menuItem.setMnemonic(KeyEvent.VK_H);
        menuItem.addActionListener(this);
        menu.add(menuItem);

		menuItem = new JMenuItem("Grow Dots");
        menuItem.setActionCommand("GROW");
        menuItem.setMnemonic(KeyEvent.VK_D);
        menuItem.addActionListener(this);
        menu.add(menuItem);

        menubar.add(menu);

        // menu = new JMenu("Control Mode");
        // menu.setMnemonic(KeyEvent.VK_C);

        // bg3 = new ButtonGroup();

        // rbmenuItem = new JRadioButtonMenuItem("Viewing Mode", true);defaultrb3=rbmenuItem;
        // rbmenuItem.setActionCommand("MODE_VIEW");
        // rbmenuItem.setMnemonic(KeyEvent.VK_V);
        // rbmenuItem.addActionListener(this);
        // bg3.add(rbmenuItem);
        // menu.add(rbmenuItem);

        // rbmenuItem = new JRadioButtonMenuItem("Lighting Mode");
        // rbmenuItem.setActionCommand("MODE_LIGHT");
        // rbmenuItem.setMnemonic(KeyEvent.VK_L);
        // rbmenuItem.addActionListener(this);
        // bg3.add(rbmenuItem);
        // menu.add(rbmenuItem);

        // rbmenuItem = new JRadioButtonMenuItem("Coloring Mode");
        // rbmenuItem.setActionCommand("MODE_COLOR");
        // rbmenuItem.setMnemonic(KeyEvent.VK_C);
        // rbmenuItem.addActionListener(this);
        // bg3.add(rbmenuItem);
        // menu.add(rbmenuItem);

        // rbmenuItem = new JRadioButtonMenuItem("Cut Plane Mode");
        // rbmenuItem.setActionCommand("MODE_PLANE");
        // rbmenuItem.setMnemonic(KeyEvent.VK_P);
        // rbmenuItem.addActionListener(this);
        // bg3.add(rbmenuItem);
        // menu.add(rbmenuItem);

        // rbmenuItem = new JRadioButtonMenuItem("Editing Mode");
        // rbmenuItem.setActionCommand("MODE_EDIT");
        // rbmenuItem.setMnemonic(KeyEvent.VK_E);
        // rbmenuItem.addActionListener(this);
        // bg3.add(rbmenuItem);
        // menu.add(rbmenuItem);

        // menubar.add(menu);

        this.setJMenuBar(menubar);

        vp = new ViewPanel();
        vp.setMesh(mesh);

        this.getContentPane().add(vp, BorderLayout.CENTER);

		status = new JTextArea("", 15, 15);
		//textArea2.setPreferredSize(new Dimension(100, 100));
		JScrollPane scrollPane = new JScrollPane(status, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		status.setLineWrap(true);
		status.setBackground(Color.black);
		status.setCaretColor(Color.white);
		status.setForeground(Color.gray);
		status.setFont(new Font("Monospaced", Font.BOLD, 12));
		this.getContentPane().add(scrollPane, BorderLayout.PAGE_END);
		
        this.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        this.addWindowListener(new WindowAdapter() {
                                   public void windowClosing(WindowEvent e) {
                                       exitHandler();
                                   }
                               });
        this.pack();

		
		out = new OutputStream() {
			@Override
			public void write(int b) throws IOException {
			  updateTextArea(String.valueOf((char) b));
			}
		 
			@Override
			public void write(byte[] b, int off, int len) throws IOException {
			  updateTextArea(new String(b, off, len));
			}
		 
			@Override
			public void write(byte[] b) throws IOException {
			  write(b, 0, b.length);
			}
		};
	 
		System.setOut(new PrintStream(out, true));

	}

	private void updateTextArea(final String text) {
		SwingUtilities.invokeLater(new Runnable() { public void run() {status.append(text);}});
	}
 
    public static void main(String[] args) {

        Points2Map mainFrame = new Points2Map(false);
        mainFrame.setVisible(true);
		if(args.length>0) {
			String fileName=args[0];
			System.out.println("Loading File "+fileName);
			mainFrame.currentFile=new File(fileName);
			Mesh m=mainFrame.readFile(mainFrame.currentFile);
			if(m!=null) {
				mainFrame.mesh=m;
				mainFrame.vp.setMesh(mainFrame.mesh);
				mainFrame.setTitle("Points2Map - " + fileName);
			} else {
				JOptionPane.showMessageDialog(mainFrame,
											"The specified file was not able to be loaded. Please check the format.",
											"Unable to Load File1",
											JOptionPane.ERROR_MESSAGE);
			}
		}
    }

    public void setAppletURLList(URL[] list) {
        appletURLs = list;
    }

    public void actionPerformed(ActionEvent e) {

        String action = e.getActionCommand();
        
        if (action.equals("EXIT")) {
            exitHandler();
        } else if (action.equals("OPEN")) {
			(new Thread(){ public void run(){
				openFile();
			}}).start();
        } else if (action.equals("SAVE")) {
			(new Thread(){ public void run(){
				saveFile();
			}}).start();
        } else if (action.equals("SAVEAS")) {
			(new Thread(){ public void run(){
				saveFileAs();
			}}).start();
        } else if (action.equals("GROUND")) {
			(new Thread(){ public void run(){
				mesh.extractGround(0.0625);
				mesh.extractGround(0.125);
				mesh.extractGround(0.5);
				mesh.extractGround(1);
				mesh.extractGround(2);
				mesh.extractGround(4);
				mesh.extractGround(8);
				mesh.extractGround(16);
				mesh.extractGround(32);
				System.out.println("Start sorting...");
				double timer=System.currentTimeMillis();
				// mesh.kdtreesort(0,mesh.getNumVertices(),1,mesh.getMin(),mesh.getMax());
				mesh.kdtreesort(0,mesh.getNumVertices(),mesh.getMin(),mesh.getMax());
				System.out.println("Finished sorting in "+(System.currentTimeMillis()-timer)+"msec.");
				vp.fullPaint();
			}}).start();
        } else if (action.equals("RESOLUTION")) {
			(new Thread(){ public void run(){
				mesh.reduceResolution(20);
				System.out.println("Start sorting...");
				double timer=System.currentTimeMillis();
				// mesh.kdtreesort(0,mesh.getNumVertices(),1,mesh.getMin(),mesh.getMax());
				mesh.kdtreesort(0,mesh.getNumVertices(),mesh.getMin(),mesh.getMax());
				System.out.println("Finished sorting in "+(System.currentTimeMillis()-timer)+"msec.");
			}}).start();
		} else if (action.equals("MAP")) {
			(new Thread(){ public void run(){
				generatemap();
			}}).start();
        } else if (action.equals("V_MODELINFO")) {
            showInfo();
        } else if (action.equals("V_SETDEFAULT")) {
            vp.setDefaultPerspective();
        } else if (action.equals("V_SETVIEW")) {
            changeView();
        } else if (action.equals("V_SETLIGHT")) {
            changeLight();
        } else if (action.equals("V_SETCOLOR")) {
            changeColor();
        } else if (action.equals("V_COLMODE")) {
            vp.changeColMode();
        } else if (action.equals("BACKLIGHT")) {
            vp.BACKLIGHT=!vp.BACKLIGHT;
			vp.fullPaint();
        } else if (action.equals("INVERT")) {
            vp.INVERT=!vp.INVERT;
			vp.fullPaint();
        } else if (action.equals("GUESS")) {
			(new Thread(){ public void run(){
				mesh.guessNormals(6);
				vp.fullPaintWithNewIndexBuffer();
			}}).start();
        } else if (action.equals("COMPUTE")) {
			(new Thread(){ public void run(){
				mesh.computeNormals(30);
				vp.fullPaintWithNewIndexBuffer();
			}}).start();
        } else if (action.equals("kNNa")) {
			(new Thread(){ public void run(){
	        	input_kNNa();
			}}).start();
        } else if (action.equals("kNNm")) {
			(new Thread(){ public void run(){
	        	input_kNNm();
			}}).start();
        } else if (action.equals("kNN+")) {
			(new Thread(){ public void run(){
	        	input_kNNplus();
			}}).start();
        } else if (action.equals("kNNpc")) {
			(new Thread(){ public void run(){
	        	kNNpointcloud();
			}}).start();
        } else if (action.equals("noise")) {
			(new Thread(){ public void run(){
	        	addNoise();
			}}).start();
        } else if (action.equals("CT")) {
			(new Thread(){ public void run(){
	        	input_computeCT();
			}}).start();
        } else if (action.equals("LSMH")) {
			(new Thread(){ public void run(){
				input_computeLCTMH();
			}}).start();
        } else if (action.equals("LSQS")) {
			(new Thread(){ public void run(){
				input_computeLCTQS();
			}}).start();
        } else if (action.equals("HOLES")) {
			(new Thread(){ public void run(){
				vp.fillHoles();
				vp.fullPaint();
			}}).start();
        } else if (action.equals("GROW")) {
			(new Thread(){ public void run(){
				vp.growDots();
				vp.fullPaint();
			}}).start();
		}
    }

    public void exitHandler() {
        if (inApplet) {
            this.setVisible(false);
        } else {
            System.exit(0);
        }
    }

    private void openFile() {
		String fileName="";
		Mesh m=null;
        if (!inApplet) {
            int fcValue = fc.showOpenDialog(this);
            if (fcValue == JFileChooser.APPROVE_OPTION) {
                currentFile = fc.getSelectedFile();
				m=readFile(currentFile);
				fileName=currentFile.getName();
            }
        } else {
            if (appletURLs != null && appletURLs.length > 0) {
                AppletOpenPanel raop = new AppletOpenPanel(appletURLs);
                int option = JOptionPane.showConfirmDialog(this,
                                      raop,
                                      "Choose an OFF File to Open",
                                      JOptionPane.OK_CANCEL_OPTION,
                                      JOptionPane.PLAIN_MESSAGE
                                      );
                if (option == JOptionPane.OK_OPTION) {
                    int index = raop.getSelectedIndex();
					m=readFile(appletURLs[index]);
					fileName=appletURLs[index].getFile();
				}
            }
        }
		if(m!=null) {
			mesh=m;
			vp.setMesh(mesh);
			this.setTitle("Points2Map - " + fileName);
		} else {
			JOptionPane.showMessageDialog(this,
										  "The specified file was not able to be loaded. Please check the format.",
										  "Unable to Load File1",
										  JOptionPane.ERROR_MESSAGE);
		}
    }

	public Mesh readFile(File currentFile) {
        BufferedReader in = null;
		Mesh ret=null;
		try {
			in = new BufferedReader(new FileReader(currentFile),65536);
			ret=Mesh.read(in,currentFile.length());
            in.close();
		} catch (IOException e) {
			JOptionPane.showMessageDialog(this,
										   "The specified file was not able to be loaded. Please check the format.",
										   "Unable to Load File2",
										   JOptionPane.ERROR_MESSAGE);
			in = null;
		}
		return ret;
	}
	
	public Mesh readFile(URL url) {
        BufferedReader in = null;
		Mesh ret=null;
		try {
			in = new BufferedReader(new InputStreamReader(url.openStream()),65536);
			File f;
			try {
			  f = new File(url.toURI());
			} catch(URISyntaxException e) {
			  f = new File(url.getPath());
			}
			ret=Mesh.read(in,f.length());
            in.close();
		} catch (IOException e) {
			JOptionPane.showMessageDialog(this,
									   "The specified file was not able to be loaded. Please check the format.",
									   "Unable to Load File3",
									   JOptionPane.ERROR_MESSAGE);
			in = null;
		}
		return ret;
	}

    private void saveFile() {

        if (currentFile != null) {
            try {
                PrintWriter out = new PrintWriter(new FileWriter(currentFile));
                if (mesh.write(out)) {
                    JOptionPane.showMessageDialog(this,
                                                  "The file has been saved.",
                                                  "File Saved",
                                                  JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(this,
                                                  "An error occured while trying to save the current file.",
                                                  "Unable to Save File",
                                                  JOptionPane.ERROR_MESSAGE);
                }
                out.close();
            } catch (IOException e) {
                JOptionPane.showMessageDialog(this,
                                              "An error occured while trying to save the current file.",
                                              "Unable to Save File",
                                              JOptionPane.ERROR_MESSAGE);
            }
        } else {
            saveFileAs();
        }

    }

    private void saveFileAs() {

        if (!inApplet) {
            int fcValue = fc.showSaveDialog(this);

            if (fcValue == JFileChooser.APPROVE_OPTION) {
                 boolean draw = true;
                 File newFile = fc.getSelectedFile();
                 if (newFile.exists()) {
                     draw = JOptionPane.OK_OPTION == 
                            JOptionPane.showConfirmDialog(this,
                                                    "Overwrite existing " + newFile.getName() + "?",
                                                    "Overwrite Existing File?",
                                                    JOptionPane.WARNING_MESSAGE);
                 }
                 if (draw) {
                     currentFile = newFile;
                     this.setTitle("Points2Map - " + currentFile.getName());
                     saveFile();
                 }
            }
        }
    }

    private void generatemap() {
		if (!inApplet) {

            if (exportFC == null) {
                exportFC = new JFileChooser(fc.getCurrentDirectory());
            }

            int fcValue = exportFC.showSaveDialog(this);

            if (fcValue == JFileChooser.APPROVE_OPTION) {
				boolean draw = true;
				File exportFile = exportFC.getSelectedFile();

				// If exportFile.exists(), prompt for overwrite.
				if (exportFile.exists()) {
					draw =  JOptionPane.OK_OPTION == 
							JOptionPane.showConfirmDialog(this,
													"Overwrite existing " + exportFile.getName() + "?",
													"Overwrite Existing File?",
													JOptionPane.WARNING_MESSAGE);
				}
				String separator = System.getProperty("file.separator");
				String s=exportFile.getName();

				int lastSeparatorIndex = s.lastIndexOf(separator);
				if (lastSeparatorIndex > -1) {
					s = s.substring(lastSeparatorIndex + 1);
				}

				int extensionIndex = s.lastIndexOf(".");
				if (extensionIndex == -1)
					s="";
				else 
					s=s.substring(extensionIndex+1).toLowerCase();
				if (draw) {
					if (exportFile != null) {
						try {
							ImageIO.write(vp.getMap(),s,exportFile);
						} catch (IOException e) {
							JOptionPane.showMessageDialog(this,
														  "An error occured while trying to export the screenshot.",
														  "Unable to export screenshot",
														  JOptionPane.ERROR_MESSAGE);
						}
					} else {
						JOptionPane.showMessageDialog(this,
														  "An error occured while trying to export the screenshot.",
														  "Unable to export screenshot",
													  JOptionPane.ERROR_MESSAGE);
					}
				}
			}
        }
		System.out.println("Finished Saving Map");
    }

    private void showInfo() {
        JOptionPane.showMessageDialog(this,
                                      new ModelInfoPanel(mesh.getMin(),mesh.getMax(),mesh.getMean(),
                                                        mesh.getNumVertices()),
														"Current Model Information",
														JOptionPane.PLAIN_MESSAGE
														);
    }

    private void changeView() {
        Perspective perspec = vp.getPerspective();
        ViewParameterPanel rvpp = new ViewParameterPanel(perspec.getAt(),
                                                                 perspec.getFrom(),
                                                                 perspec.getUp(),
                                                                 perspec.getAngle()
                                                                );

        int option = JOptionPane.showConfirmDialog(this,
                                      rvpp,
                                      "Viewing Space Parameters",
                                      JOptionPane.OK_CANCEL_OPTION,
                                      JOptionPane.PLAIN_MESSAGE
                                      );
        if (option == JOptionPane.OK_OPTION) {
            perspec.setAt(rvpp.getAt());
            perspec.setFrom(rvpp.getFrom());
            perspec.setUp(rvpp.getUp());
            perspec.setAngle(rvpp.getAngle());
            vp.fullPaint();
        }
    }
    
	private Document numericDocumentWithMaxLength(final int length){
	      
	      Document document = new PlainDocument(){
	         @Override
	         public void insertString(int offset, String s, AttributeSet attr)
	               throws BadLocationException {
	            if(getLength() + s.length() > length)
	               return;
	            if(!s.matches("^[\\d]*$"))
	               return;
	            
	            
	            super.insertString(offset, s, attr);
	         }
	      };
	      return document;
	      
	   }

	/* Computes the approximate computational cost of a kNN-guess */
	public double approximateComputationalCost(double iterations, double boxsize)
	{
		return 0.0782364 + 0.590347*boxsize - 0.0012149*Math.pow(boxsize,2) + 
				6.42518*Math.pow(10,-7)*Math.pow(boxsize,3) + 42.5351*iterations + 
				0.797729*boxsize*iterations + 0.0000150386*Math.pow(boxsize,2)*iterations + 
				5.01395*Math.pow(10,-8)*Math.pow(boxsize,3)*iterations - 0.340649*Math.pow(iterations,2) + 
				0.0248862*boxsize*Math.pow(iterations,2) - 
				0.0000190781*Math.pow(boxsize,2)*Math.pow(iterations,2) - 
				1.27436*Math.pow(10,-8)*Math.pow(boxsize,3)*Math.pow(iterations,2) + 0.000599169*Math.pow(iterations,3) - 
				0.00133975*boxsize*Math.pow(iterations,3) + 
				1.21904*Math.pow(10,-6)*Math.pow(boxsize,2)*Math.pow(iterations,3) + 
				8.53249*Math.pow(10,-10)*Math.pow(boxsize,3)*Math.pow(iterations,3);
	}
    
    /* Initializes the input for an automatically parametrization of a kNN-Guess */
    private void input_kNNa() {
    	JPanel tempPanel;
    	Box box = new Box(BoxLayout.Y_AXIS);
    	
    	if(mesh.getTotalNumVertices() == 0)
    	{
    		tempPanel = new JPanel();
            tempPanel.add(new JLabel("No mesh found."));
            box.add(tempPanel);
            JOptionPane.showConfirmDialog(this,
            		box,
            		"Automatic kNN-Guess",
                    JOptionPane.DEFAULT_OPTION,
                    JOptionPane.PLAIN_MESSAGE
                    );
    	}
    	else
    	{
    		JTextField inputNeighbors = new JTextField(5);
	    	inputNeighbors.setDocument(numericDocumentWithMaxLength(7));
	    	
	    	JCheckBox meanEvaluation = new JCheckBox("mean value");
	    	JCheckBox operationData = new JCheckBox("internal operation data");
	    	
	        tempPanel = new JPanel();
	    	tempPanel.add(new JLabel("Neighbors    "));
	    	tempPanel.add(inputNeighbors);
	    	box.add(tempPanel);
	    	box.add(new JLabel("                         "));
	    	
	    	JRadioButton twentyPercent = new JRadioButton("~20%");
	    	JRadioButton fourtyPercent = new JRadioButton("~40%");
	    	JRadioButton sixtyPercent = new JRadioButton("~60%");
	    	JRadioButton eightyPercent = new JRadioButton("~80%");
	    	JRadioButton ninetyPercent = new JRadioButton("~90%");
	    	JRadioButton ninetyFivePercent = new JRadioButton("~95%");
	    	JRadioButton aboveNinetyFivePercent = new JRadioButton(">95%");
	    	JRadioButton aboveNinetyNinePercent = new JRadioButton(">99%");
	    	ninetyPercent.setSelected(true);
	    	
	    	ButtonGroup buttongroup = new ButtonGroup();
	    	buttongroup.add(twentyPercent);
	    	buttongroup.add(fourtyPercent);
	    	buttongroup.add(sixtyPercent);
	    	buttongroup.add(eightyPercent);
	    	buttongroup.add(ninetyPercent);
	    	buttongroup.add(ninetyFivePercent);
	    	buttongroup.add(aboveNinetyFivePercent);
	    	buttongroup.add(aboveNinetyNinePercent);
	    	
	    	box.add(new JLabel("Precision of kNN-Guess"));
	    	box.add(twentyPercent);
	    	box.add(fourtyPercent);
	    	box.add(sixtyPercent);
	    	box.add(eightyPercent);
	    	box.add(ninetyPercent);
	    	box.add(ninetyFivePercent);
	    	box.add(aboveNinetyFivePercent);
	    	box.add(aboveNinetyNinePercent);
	    	box.add(new JLabel("_________________________"));
	    	box.add(new JLabel("                         "));
	    	box.add(new JLabel("Evaluation"));
	    	box.add(meanEvaluation);
	    	box.add(operationData);
	    	box.add(new JLabel("      (returns no computation time)"));
	    	box.add(new JLabel("                         "));
	    	
	        int option = JOptionPane.showConfirmDialog(this,
	        		box,
	                "Automatic kNN-Guess",
	                JOptionPane.OK_CANCEL_OPTION,
	                JOptionPane.PLAIN_MESSAGE
	                );
	        
	        if (option == JOptionPane.OK_OPTION) {
	            if(inputNeighbors.getText().length()!=0)
	            {	
	            	mesh.kNearestNeighbors=null;
	            	int neighbors = Integer.parseInt(inputNeighbors.getText());
	            	int iterations = 0;
	            	int boxsize = 0;
	            	
	            	int boxsizeTemp = 0;   	
	            	double lowestapproximateComputationalCost = 0.0;
	            	
	            	if(twentyPercent.isSelected())
	            	{
	            		iterations = 1;
	            		boxsize = (int)Math.round(1.5567375 + neighbors*0.33213125);
	            		lowestapproximateComputationalCost = approximateComputationalCost(iterations, boxsize);
	            		if(boxsize<3){boxsize=3;}
	            		
	            		boxsizeTemp = (int)Math.round(1.0217375 + neighbors*0.18191725);
	            		if(boxsizeTemp<3){boxsizeTemp=3;}
	            		if(approximateComputationalCost(2,boxsizeTemp)<=lowestapproximateComputationalCost)
	            		{
	            			iterations = 2;
	            			boxsize = boxsizeTemp;
	            			lowestapproximateComputationalCost = approximateComputationalCost(iterations, boxsize);
	            		}
	            		
	            		boxsizeTemp = (int)Math.round(0.797865 + neighbors*0.1395115);
	            		if(boxsizeTemp<3){boxsizeTemp=3;}
	            		if(approximateComputationalCost(3,boxsizeTemp)<=lowestapproximateComputationalCost)
	            		{
	            			iterations = 3;
	            			boxsize = boxsizeTemp;
	            			lowestapproximateComputationalCost = approximateComputationalCost(iterations, boxsize);
	            		}
	            		
	            		boxsizeTemp = (int)Math.round(0.71542 + neighbors*0.115025);
	            		if(boxsizeTemp<3){boxsizeTemp=3;}
	            		if(approximateComputationalCost(4,boxsizeTemp)<=lowestapproximateComputationalCost)
	            		{
	            			iterations = 4;
	            			boxsize = boxsizeTemp;
	            			lowestapproximateComputationalCost = approximateComputationalCost(iterations, boxsize);
	            		}
	            		
	            		boxsizeTemp = (int)Math.round(0.659745 + neighbors*0.1001385);
	            		if(boxsizeTemp<3){boxsizeTemp=3;}
	            		if(approximateComputationalCost(5,boxsizeTemp)<=lowestapproximateComputationalCost)
	            		{
	            			iterations = 5;
	            			boxsize = boxsizeTemp;
	            			lowestapproximateComputationalCost = approximateComputationalCost(iterations, boxsize);
	            		}
	            		
	            		boxsizeTemp = (int)Math.round(0.6550875 + neighbors*0.08857425);
	            		if(boxsizeTemp<3){boxsizeTemp=3;}
	            		if(approximateComputationalCost(6,boxsizeTemp)<=lowestapproximateComputationalCost)
	            		{
	            			iterations = 6;
	            			boxsize = boxsizeTemp;
	            			lowestapproximateComputationalCost = approximateComputationalCost(iterations, boxsize);
	            		}
	            		
	            		boxsizeTemp = (int)Math.round(0.646 + neighbors*0.080538);
	            		if(boxsizeTemp<3){boxsizeTemp=3;}
	            		if(approximateComputationalCost(7,boxsizeTemp)<=lowestapproximateComputationalCost)
	            		{
	            			iterations = 7;
	            			boxsize = boxsizeTemp;
	            			lowestapproximateComputationalCost = approximateComputationalCost(iterations, boxsize);
	            		}
	            		
	            		boxsizeTemp = (int)Math.round(0.642035 + neighbors*0.0748785);
	            		if(boxsizeTemp<3){boxsizeTemp=3;}
	            		if(approximateComputationalCost(8,boxsizeTemp)<=lowestapproximateComputationalCost)
	            		{
	            			iterations = 8;
	            			boxsize = boxsizeTemp;
	            			lowestapproximateComputationalCost = approximateComputationalCost(iterations, boxsize);
	            		}
	            		
	            		boxsizeTemp = (int)Math.round(0.640505 + neighbors*0.0701775);
	            		if(boxsizeTemp<3){boxsizeTemp=3;}
	            		if(approximateComputationalCost(9,boxsizeTemp)<=lowestapproximateComputationalCost)
	            		{
	            			iterations = 9;
	            			boxsize = boxsizeTemp;
	            			lowestapproximateComputationalCost = approximateComputationalCost(iterations, boxsize);
	            		}
	            		
	            		boxsizeTemp = (int)Math.round(0.26662 + neighbors*0.0723484);
	            		if(boxsizeTemp<3){boxsizeTemp=3;}
	            		if(approximateComputationalCost(10,boxsizeTemp)<=lowestapproximateComputationalCost)
	            		{
	            			iterations = 10;
	            			boxsize = boxsizeTemp;
	            			lowestapproximateComputationalCost = approximateComputationalCost(iterations, boxsize);
	            		}
	            	}
	            	
	            	else if(fourtyPercent.isSelected())
	            	{
	            		iterations = 1;
	            		boxsize = (int)Math.round(3.084125 + neighbors*1.0070675);
	            		lowestapproximateComputationalCost = approximateComputationalCost(iterations, boxsize);

	            		boxsizeTemp = (int)Math.round(1.7755625 + neighbors*0.44615475);
	            		if(boxsizeTemp<3){boxsizeTemp=3;}
	            		if(approximateComputationalCost(2,boxsizeTemp)<=lowestapproximateComputationalCost)
	            		{
	            			iterations = 2;
	            			boxsize = boxsizeTemp;
	            			lowestapproximateComputationalCost = approximateComputationalCost(iterations, boxsize);
	            		}
	            		
	            		boxsizeTemp = (int)Math.round(1.18185 + neighbors*0.320411);
	            		if(boxsizeTemp<3){boxsizeTemp=3;}
	            		if(approximateComputationalCost(3,boxsizeTemp)<=lowestapproximateComputationalCost)
	            		{
	            			iterations = 3;
	            			boxsize = boxsizeTemp;
	            			lowestapproximateComputationalCost = approximateComputationalCost(iterations, boxsize);
	            		}
	            		
	            		boxsizeTemp = (int)Math.round(1.176775 + neighbors*0.2554565);
	            		if(boxsizeTemp<3){boxsizeTemp=3;}
	            		if(approximateComputationalCost(4,boxsizeTemp)<=lowestapproximateComputationalCost)
	            		{
	            			iterations = 4;
	            			boxsize = boxsizeTemp;
	            			lowestapproximateComputationalCost = approximateComputationalCost(iterations, boxsize);
	            		}
	            		
	            		boxsizeTemp = (int)Math.round(1.0918 + neighbors*0.223046);
	            		if(boxsizeTemp<3){boxsizeTemp=3;}
	            		if(approximateComputationalCost(5,boxsizeTemp)<=lowestapproximateComputationalCost)
	            		{
	            			iterations = 5;
	            			boxsize = boxsizeTemp;
	            			lowestapproximateComputationalCost = approximateComputationalCost(iterations, boxsize);
	            		}
	            		
	            		boxsizeTemp = (int)Math.round(1.1331 + neighbors*0.194856);
	            		if(boxsizeTemp<3){boxsizeTemp=3;}
	            		if(approximateComputationalCost(6,boxsizeTemp)<=lowestapproximateComputationalCost)
	            		{
	            			iterations = 6;
	            			boxsize = boxsizeTemp;
	            			lowestapproximateComputationalCost = approximateComputationalCost(iterations, boxsize);
	            		}
	            		
	            		boxsizeTemp = (int)Math.round(1.1482925 + neighbors*0.17544675);
	            		if(boxsizeTemp<3){boxsizeTemp=3;}
	            		if(approximateComputationalCost(7,boxsizeTemp)<=lowestapproximateComputationalCost)
	            		{
	            			iterations = 7;
	            			boxsize = boxsizeTemp;
	            			lowestapproximateComputationalCost = approximateComputationalCost(iterations, boxsize);
	            		}
	            		
	            		boxsizeTemp = (int)Math.round(1.13011 + neighbors*0.165153);
	            		if(boxsizeTemp<3){boxsizeTemp=3;}
	            		if(approximateComputationalCost(8,boxsizeTemp)<=lowestapproximateComputationalCost)
	            		{
	            			iterations = 8;
	            			boxsize = boxsizeTemp;
	            			lowestapproximateComputationalCost = approximateComputationalCost(iterations, boxsize);
	            		}
	            		
	            		boxsizeTemp = (int)Math.round(1.140895 + neighbors*0.1543665);
	            		if(boxsizeTemp<3){boxsizeTemp=3;}
	            		if(approximateComputationalCost(9,boxsizeTemp)<=lowestapproximateComputationalCost)
	            		{
	            			iterations = 9;
	            			boxsize = boxsizeTemp;
	            			lowestapproximateComputationalCost = approximateComputationalCost(iterations, boxsize);
	            		}
	            		
	            		boxsizeTemp = (int)Math.round(0.5798 + neighbors*0.154361);
	            		if(boxsizeTemp<3){boxsizeTemp=3;}
	            		if(approximateComputationalCost(10,boxsizeTemp)<=lowestapproximateComputationalCost)
	            		{
	            			iterations = 10;
	            			boxsize = boxsizeTemp;
	            			lowestapproximateComputationalCost = approximateComputationalCost(iterations, boxsize);
	            		}
	            	}
	            	
	            	else if(sixtyPercent.isSelected())
	            	{
	            		iterations = 2;
	            		boxsize = (int)Math.round(2.174175 + neighbors*1.0258225);
	            		lowestapproximateComputationalCost = approximateComputationalCost(iterations, boxsize);
	            		
	            		boxsizeTemp = (int)Math.round(1.810275 + neighbors*0.6418425);
	            		if(boxsizeTemp<3){boxsizeTemp=3;}
	            		if(approximateComputationalCost(3,boxsizeTemp)<=lowestapproximateComputationalCost)
	            		{
	            			iterations = 3;
	            			boxsize = boxsizeTemp;
	            			lowestapproximateComputationalCost = approximateComputationalCost(iterations, boxsize);
	            		}
	            		
	            		boxsizeTemp = (int)Math.round(1.59595 + neighbors*0.482627);
	            		if(boxsizeTemp<3){boxsizeTemp=3;}
	            		if(approximateComputationalCost(4,boxsizeTemp)<=lowestapproximateComputationalCost)
	            		{
	            			iterations = 4;
	            			boxsize = boxsizeTemp;
	            			lowestapproximateComputationalCost = approximateComputationalCost(iterations, boxsize);
	            		}
	            		
	            		boxsizeTemp = (int)Math.round(0.7909375 + neighbors*0.41924525);
	            		if(boxsizeTemp<3){boxsizeTemp=3;}
	            		if(approximateComputationalCost(5,boxsizeTemp)<=lowestapproximateComputationalCost)
	            		{
	            			iterations = 5;
	            			boxsize = boxsizeTemp;
	            			lowestapproximateComputationalCost = approximateComputationalCost(iterations, boxsize);
	            		}
	            		
	            		boxsizeTemp = (int)Math.round(0.92965 + neighbors*0.369285);
	            		if(boxsizeTemp<3){boxsizeTemp=3;}
	            		if(approximateComputationalCost(6,boxsizeTemp)<=lowestapproximateComputationalCost)
	            		{
	            			iterations = 6;
	            			boxsize = boxsizeTemp;
	            			lowestapproximateComputationalCost = approximateComputationalCost(iterations, boxsize);
	            		}
	            		
	            		boxsizeTemp = (int)Math.round(1.0441 + neighbors*0.331004);
	            		if(boxsizeTemp<3){boxsizeTemp=3;}
	            		if(approximateComputationalCost(7,boxsizeTemp)<=lowestapproximateComputationalCost)
	            		{
	            			iterations = 7;
	            			boxsize = boxsizeTemp;
	            			lowestapproximateComputationalCost = approximateComputationalCost(iterations, boxsize);
	            		}
	            		
	            		boxsizeTemp = (int)Math.round(1.16795 + neighbors*0.300451);
	            		if(boxsizeTemp<3){boxsizeTemp=3;}
	            		if(approximateComputationalCost(8,boxsizeTemp)<=lowestapproximateComputationalCost)
	            		{
	            			iterations = 8;
	            			boxsize = boxsizeTemp;
	            			lowestapproximateComputationalCost = approximateComputationalCost(iterations, boxsize);
	            		}
	            		
	            		boxsizeTemp = (int)Math.round(1.2372625 + neighbors*0.27895875);
	            		if(boxsizeTemp<3){boxsizeTemp=3;}
	            		if(approximateComputationalCost(9,boxsizeTemp)<=lowestapproximateComputationalCost)
	            		{
	            			iterations = 9;
	            			boxsize = boxsizeTemp;
	            			lowestapproximateComputationalCost = approximateComputationalCost(iterations, boxsize);
	            		}
	            		
	            		boxsizeTemp = (int)Math.round(0.807475 + neighbors*0.2681705);
	            		if(boxsizeTemp<3){boxsizeTemp=3;}
	            		if(approximateComputationalCost(10,boxsizeTemp)<=lowestapproximateComputationalCost)
	            		{
	            			iterations = 10;
	            			boxsize = boxsizeTemp;
	            			lowestapproximateComputationalCost = approximateComputationalCost(iterations, boxsize);
	            		}
	            	}
	            	
	            	else if(eightyPercent.isSelected())
	            	{
	            		iterations = 2;
	            		boxsize = (int)Math.round(3.39675 + neighbors*2.910325);
	            		lowestapproximateComputationalCost = approximateComputationalCost(iterations, boxsize);
	            		
	            		boxsizeTemp = (int)Math.round(2.662 + neighbors*1.44676);
	            		if(boxsizeTemp<3){boxsizeTemp=3;}
	            		if(approximateComputationalCost(3,boxsizeTemp)<=lowestapproximateComputationalCost)
	            		{
	            			iterations = 3;
	            			boxsize = boxsizeTemp;
	            			lowestapproximateComputationalCost = approximateComputationalCost(iterations, boxsize);
	            		}
	            		
	            		boxsizeTemp = (int)Math.round(1.779325 + neighbors*1.0387375);
	            		if(boxsizeTemp<3){boxsizeTemp=3;}
	            		if(approximateComputationalCost(4,boxsizeTemp)<=lowestapproximateComputationalCost)
	            		{
	            			iterations = 4;
	            			boxsize = boxsizeTemp;
	            			lowestapproximateComputationalCost = approximateComputationalCost(iterations, boxsize);
	            		}
	            		
	            		boxsizeTemp = (int)Math.round(1.2308375 + neighbors*0.83953325);
	            		if(boxsizeTemp<3){boxsizeTemp=3;}
	            		if(approximateComputationalCost(5,boxsizeTemp)<=lowestapproximateComputationalCost)
	            		{
	            			iterations = 5;
	            			boxsize = boxsizeTemp;
	            			lowestapproximateComputationalCost = approximateComputationalCost(iterations, boxsize);
	            		}
	            		
	            		boxsizeTemp = (int)Math.round(1.8256 + neighbors*0.687538);
	            		if(boxsizeTemp<3){boxsizeTemp=3;}
	            		if(approximateComputationalCost(6,boxsizeTemp)<=lowestapproximateComputationalCost)
	            		{
	            			iterations = 6;
	            			boxsize = boxsizeTemp;
	            			lowestapproximateComputationalCost = approximateComputationalCost(iterations, boxsize);
	            		}
	            		
	            		boxsizeTemp = (int)Math.round(1.5985 + neighbors*0.621876);
	            		if(boxsizeTemp<3){boxsizeTemp=3;}
	            		if(approximateComputationalCost(7,boxsizeTemp)<=lowestapproximateComputationalCost)
	            		{
	            			iterations = 7;
	            			boxsize = boxsizeTemp;
	            			lowestapproximateComputationalCost = approximateComputationalCost(iterations, boxsize);
	            		}
	            		
	            		boxsizeTemp = (int)Math.round(1.725075 + neighbors*0.5440705);
	            		if(boxsizeTemp<3){boxsizeTemp=3;}
	            		if(approximateComputationalCost(8,boxsizeTemp)<=lowestapproximateComputationalCost)
	            		{
	            			iterations = 8;
	            			boxsize = boxsizeTemp;
	            			lowestapproximateComputationalCost = approximateComputationalCost(iterations, boxsize);
	            		}
	            		
	            		boxsizeTemp = (int)Math.round(1.527075 + neighbors*0.5000705);
	            		if(boxsizeTemp<3){boxsizeTemp=3;}
	            		if(approximateComputationalCost(9,boxsizeTemp)<=lowestapproximateComputationalCost)
	            		{
	            			iterations = 9;
	            			boxsize = boxsizeTemp;
	            			lowestapproximateComputationalCost = approximateComputationalCost(iterations, boxsize);
	            		}
	            		
	            		boxsizeTemp = (int)Math.round(0.9414875 + neighbors*0.47504625);
	            		if(boxsizeTemp<3){boxsizeTemp=3;}
	            		if(approximateComputationalCost(10,boxsizeTemp)<=lowestapproximateComputationalCost)
	            		{
	            			iterations = 10;
	            			boxsize = boxsizeTemp;
	            			lowestapproximateComputationalCost = approximateComputationalCost(iterations, boxsize);
	            		}
	            	}
	            	
	            	else if(ninetyPercent.isSelected())
	            	{
	            		iterations = 3;
	            		boxsize = (int)Math.round(1.957125 + neighbors*2.9153975);
	            		lowestapproximateComputationalCost = approximateComputationalCost(iterations, boxsize);
	            		
	            		boxsizeTemp = (int)Math.round(1.703575 + neighbors*1.8681025);
	            		if(boxsizeTemp<3){boxsizeTemp=3;}
	            		if(approximateComputationalCost(4,boxsizeTemp)<=lowestapproximateComputationalCost)
	            		{
	            			iterations = 4;
	            			boxsize = boxsizeTemp;
	            			lowestapproximateComputationalCost = approximateComputationalCost(iterations, boxsize);
	            		}
	            		
	            		boxsizeTemp = (int)Math.round(1.995975 + neighbors*1.3925125);
	            		if(boxsizeTemp<3){boxsizeTemp=3;}
	            		if(approximateComputationalCost(5,boxsizeTemp)<=lowestapproximateComputationalCost)
	            		{
	            			iterations = 5;
	            			boxsize = boxsizeTemp;
	            			lowestapproximateComputationalCost = approximateComputationalCost(iterations, boxsize);
	            		}
	            		
	            		boxsizeTemp = (int)Math.round(1.2115 + neighbors*1.20077);
	            		if(boxsizeTemp<3){boxsizeTemp=3;}
	            		if(approximateComputationalCost(6,boxsizeTemp)<=lowestapproximateComputationalCost)
	            		{
	            			iterations = 6;
	            			boxsize = boxsizeTemp;
	            			lowestapproximateComputationalCost = approximateComputationalCost(iterations, boxsize);
	            		}
	            		
	            		boxsizeTemp = (int)Math.round(1.0376 + neighbors*1.01693);
	            		if(boxsizeTemp<3){boxsizeTemp=3;}
	            		if(approximateComputationalCost(7,boxsizeTemp)<=lowestapproximateComputationalCost)
	            		{
	            			iterations = 7;
	            			boxsize = boxsizeTemp;
	            			lowestapproximateComputationalCost = approximateComputationalCost(iterations, boxsize);
	            		}
	            		
	            		boxsizeTemp = (int)Math.round(0.6119625 + neighbors*0.92276075);
	            		if(boxsizeTemp<3){boxsizeTemp=3;}
	            		if(approximateComputationalCost(8,boxsizeTemp)<=lowestapproximateComputationalCost)
	            		{
	            			iterations = 8;
	            			boxsize = boxsizeTemp;
	            			lowestapproximateComputationalCost = approximateComputationalCost(iterations, boxsize);
	            		}
	            		
	            		boxsizeTemp = (int)Math.round(1.22655 + neighbors*0.807901);
	            		if(boxsizeTemp<3){boxsizeTemp=3;}
	            		if(approximateComputationalCost(9,boxsizeTemp)<=lowestapproximateComputationalCost)
	            		{
	            			iterations = 9;
	            			boxsize = boxsizeTemp;
	            			lowestapproximateComputationalCost = approximateComputationalCost(iterations, boxsize);
	            		}
	            		
	            		boxsizeTemp = (int)Math.round(1.50685 + neighbors*0.698435);
	            		if(boxsizeTemp<3){boxsizeTemp=3;}
	            		if(approximateComputationalCost(10,boxsizeTemp)<=lowestapproximateComputationalCost)
	            		{
	            			iterations = 10;
	            			boxsize = boxsizeTemp;
	            			lowestapproximateComputationalCost = approximateComputationalCost(iterations, boxsize);
	            		}
	            	}
	            	
	            	else if(ninetyFivePercent.isSelected())
	            	{
	            		iterations = 4;
	            		boxsize = (int)Math.round(4.642875 + neighbors*3.1071425);
	            		lowestapproximateComputationalCost = approximateComputationalCost(iterations, boxsize);
	            		
	            		boxsizeTemp = (int)Math.round(-0.019 + neighbors*2.43372);
	            		if(boxsizeTemp<3){boxsizeTemp=3;}
	            		if(approximateComputationalCost(5,boxsizeTemp)<=lowestapproximateComputationalCost)
	            		{
	            			iterations = 5;
	            			boxsize = boxsizeTemp;
	            			lowestapproximateComputationalCost = approximateComputationalCost(iterations, boxsize);
	            		}
	            		
	            		boxsizeTemp = (int)Math.round(1.270575 + neighbors*1.8015125);
	            		if(boxsizeTemp<3){boxsizeTemp=3;}
	            		if(approximateComputationalCost(6,boxsizeTemp)<=lowestapproximateComputationalCost)
	            		{
	            			iterations = 6;
	            			boxsize = boxsizeTemp;
	            			lowestapproximateComputationalCost = approximateComputationalCost(iterations, boxsize);
	            		}
	            		
	            		boxsizeTemp = (int)Math.round(1.059925 + neighbors*1.5051175);
	            		if(boxsizeTemp<3){boxsizeTemp=3;}
	            		if(approximateComputationalCost(7,boxsizeTemp)<=lowestapproximateComputationalCost)
	            		{
	            			iterations = 7;
	            			boxsize = boxsizeTemp;
	            			lowestapproximateComputationalCost = approximateComputationalCost(iterations, boxsize);
	            		}
	            		
	            		boxsizeTemp = (int)Math.round(0.574425 + neighbors*1.3739275);
	            		if(boxsizeTemp<3){boxsizeTemp=3;}
	            		if(approximateComputationalCost(8,boxsizeTemp)<=lowestapproximateComputationalCost)
	            		{
	            			iterations = 8;
	            			boxsize = boxsizeTemp;
	            			lowestapproximateComputationalCost = approximateComputationalCost(iterations, boxsize);
	            		}
	            		
	            		boxsizeTemp = (int)Math.round(0.34725 + neighbors*1.243055);
	            		if(boxsizeTemp<3){boxsizeTemp=3;}
	            		if(approximateComputationalCost(9,boxsizeTemp)<=lowestapproximateComputationalCost)
	            		{
	            			iterations = 9;
	            			boxsize = boxsizeTemp;
	            			lowestapproximateComputationalCost = approximateComputationalCost(iterations, boxsize);
	            		}
	            		
	            		boxsizeTemp = (int)Math.round(0.938825 + neighbors*1.0431375);
	            		if(boxsizeTemp<3){boxsizeTemp=3;}
	            		if(approximateComputationalCost(10,boxsizeTemp)<=lowestapproximateComputationalCost)
	            		{
	            			iterations = 10;
	            			boxsize = boxsizeTemp;
	            			lowestapproximateComputationalCost = approximateComputationalCost(iterations, boxsize);
	            		}
	            	}
	            	
	            	else if(aboveNinetyFivePercent.isSelected())
	            	{
	            		iterations = 5;
	            		boxsize = (int)Math.ceil(0.3435 + neighbors*3.60851);
	            		lowestapproximateComputationalCost = approximateComputationalCost(iterations, boxsize);
	            		
	            		boxsizeTemp = (int)Math.ceil(0.71425 + neighbors*2.785715);
	            		if(boxsizeTemp<3){boxsizeTemp=3;}
	            		if(approximateComputationalCost(6,boxsizeTemp)<=lowestapproximateComputationalCost)
	            		{
	            			iterations = 6;
	            			boxsize = boxsizeTemp;
	            			lowestapproximateComputationalCost = approximateComputationalCost(iterations, boxsize);
	            		}
	            		
	            		boxsizeTemp = (int)Math.ceil(-0.58775 + neighbors*2.280995);
	            		if(boxsizeTemp<3){boxsizeTemp=3;}
	            		if(approximateComputationalCost(7,boxsizeTemp)<=lowestapproximateComputationalCost)
	            		{
	            			iterations = 7;
	            			boxsize = boxsizeTemp;
	            			lowestapproximateComputationalCost = approximateComputationalCost(iterations, boxsize);
	            		}
	            		
	            		boxsizeTemp = (int)Math.ceil(-0.4762 + neighbors*1.97619);
	            		if(boxsizeTemp<3){boxsizeTemp=3;}
	            		if(approximateComputationalCost(8,boxsizeTemp)<=lowestapproximateComputationalCost)
	            		{
	            			iterations = 8;
	            			boxsize = boxsizeTemp;
	            			lowestapproximateComputationalCost = approximateComputationalCost(iterations, boxsize);
	            		}
	            		
	            		boxsizeTemp = (int)Math.ceil(-0.44645 + neighbors*1.723215);
	            		if(boxsizeTemp<3){boxsizeTemp=3;}
	            		if(approximateComputationalCost(9,boxsizeTemp)<=lowestapproximateComputationalCost)
	            		{
	            			iterations = 9;
	            			boxsize = boxsizeTemp;
	            			lowestapproximateComputationalCost = approximateComputationalCost(iterations, boxsize);
	            		}
	            		
	            		boxsizeTemp = (int)Math.ceil(1.00605 + neighbors*1.399395);
	            		if(boxsizeTemp<3){boxsizeTemp=3;}
	            		if(approximateComputationalCost(10,boxsizeTemp)<=lowestapproximateComputationalCost)
	            		{
	            			iterations = 10;
	            			boxsize = boxsizeTemp;
	            			lowestapproximateComputationalCost = approximateComputationalCost(iterations, boxsize);
	            		}
	            	}
	            	
	            	else if(aboveNinetyNinePercent.isSelected())
	            	{
	            		iterations = 6;
	            		boxsize = (int)Math.ceil(3.33333 + neighbors*6.1666667);
	            		lowestapproximateComputationalCost = approximateComputationalCost(iterations, boxsize);
	            		
	            		boxsizeTemp = (int)Math.ceil(3.125 + neighbors*4.6875);
	            		if(boxsizeTemp<3){boxsizeTemp=3;}
	            		if(approximateComputationalCost(7,boxsizeTemp)<=lowestapproximateComputationalCost)
	            		{
	            			iterations = 7;
	            			boxsize = boxsizeTemp;
	            			lowestapproximateComputationalCost = approximateComputationalCost(iterations, boxsize);
	            		}
	            		
	            		boxsizeTemp = (int)Math.ceil(2.08325 + neighbors*3.791675);
	            		if(boxsizeTemp<3){boxsizeTemp=3;}
	            		if(approximateComputationalCost(8,boxsizeTemp)<=lowestapproximateComputationalCost)
	            		{
	            			iterations = 8;
	            			boxsize = boxsizeTemp;
	            			lowestapproximateComputationalCost = approximateComputationalCost(iterations, boxsize);
	            		}
	            		
	            		boxsizeTemp = (int)Math.ceil(-0.000125 + neighbors*3.3333425);
	            		if(boxsizeTemp<3){boxsizeTemp=3;}
	            		if(approximateComputationalCost(9,boxsizeTemp)<=lowestapproximateComputationalCost)
	            		{
	            			iterations = 9;
	            			boxsize = boxsizeTemp;
	            			lowestapproximateComputationalCost = approximateComputationalCost(iterations, boxsize);
	            		}
	            		
	            		boxsizeTemp = (int)Math.ceil(-2.916625 + neighbors*2.9583325);
	            		if(boxsizeTemp<3){boxsizeTemp=3;}
	            		if(approximateComputationalCost(10,boxsizeTemp)<=lowestapproximateComputationalCost)
	            		{
	            			iterations = 10;
	            			boxsize = boxsizeTemp;
	            			lowestapproximateComputationalCost = approximateComputationalCost(iterations, boxsize);
	            		}
	            	}
	            	
	            	int radius = 0;
            		if(meanEvaluation.isSelected() == true)
            		{
    	            	boolean radiusValid = false;
            			int radiusMax = mesh.getTotalNumVertices()/2;
    	            	while(radiusValid == false)
    	            	{
			            	box = new Box(BoxLayout.Y_AXIS);
			        		tempPanel = new JPanel();
			                tempPanel.add(new JLabel("Radius for evaluation of mean data:"));
			            	box.add(tempPanel);
			        		tempPanel = new JPanel();
			                tempPanel.add(new JLabel("(maximum value is "+String.valueOf(radiusMax)+")"));
			                box.add(tempPanel);
			                tempPanel = new JPanel();
			            	JTextField inputRadius = new JTextField(5);
			            	inputRadius.setDocument(numericDocumentWithMaxLength(7));
			            	inputRadius.setText("0");
			            	tempPanel.add(inputRadius);
			            	box.add(tempPanel);
			                JOptionPane.showConfirmDialog(this,
			                		box,
			                		"Automatic kNN-Guess",
			                        JOptionPane.DEFAULT_OPTION,
			                        JOptionPane.PLAIN_MESSAGE
			                        );
			                radius = Integer.parseInt(inputRadius.getText());
			                
			                if(radius <= radiusMax)
			                {
			                	radiusValid = true;
			                }
    	            	}
            		}
	            	
	            	if(operationData.isSelected() == true)
	            	{
	            		mesh.guessNeighborsWithEvaluation(iterations, boxsize, neighbors);
	            		mesh.evaluateKNN(mesh.getTotalNumVertices()/2-radius,mesh.getTotalNumVertices()/2+radius,neighbors,iterations,boxsize,meanEvaluation.isSelected(),true);
	            	}
	            	else
	            	{
	            		mesh.guessNeighbors(iterations, boxsize, neighbors);
	            		if(meanEvaluation.isSelected() == true)
	            		{
	            			mesh.evaluateKNN(mesh.getTotalNumVertices()/2-radius,mesh.getTotalNumVertices()/2+radius,neighbors,iterations,boxsize,meanEvaluation.isSelected(),false);
	            		}
	            	}
	            }
        	}
        }
    }
    
    /* Initializes the input for a manually parametrization of a kNN-Guess */
    private void input_kNNm() {
    	JPanel tempPanel;
    	Box box = new Box(BoxLayout.Y_AXIS);
    	
    	if(mesh.getTotalNumVertices() == 0)
    	{
    		tempPanel = new JPanel();
            tempPanel.add(new JLabel("No mesh found."));
            box.add(tempPanel);
            JOptionPane.showConfirmDialog(this,
            		box,
                    "Manual kNN-Guess",
                    JOptionPane.DEFAULT_OPTION,
                    JOptionPane.PLAIN_MESSAGE
                    );
    	}
    	else
    	{
    	JTextField inputNeighbors,inputIterations,inputBoxsize;
    	
    	inputNeighbors = new JTextField(5);
    	inputNeighbors.setDocument(numericDocumentWithMaxLength(7));
    	inputIterations = new JTextField(5);
    	inputIterations.setDocument(numericDocumentWithMaxLength(7));
    	inputBoxsize = new JTextField(5);
    	inputBoxsize.setDocument(numericDocumentWithMaxLength(7));
    	
    	JCheckBox meanEvaluation = new JCheckBox("mean value");
    	JCheckBox operationData = new JCheckBox("internal operation data");

        tempPanel = new JPanel();
    	tempPanel.add(new JLabel("Neighbors    "));
    	tempPanel.add(inputNeighbors);
    	box.add(tempPanel);
        tempPanel = new JPanel();
    	tempPanel.add(new JLabel("Iterations     "));
    	tempPanel.add(inputIterations);
    	box.add(tempPanel);
        tempPanel = new JPanel();
    	tempPanel.add(new JLabel("Boxsize        "));
    	tempPanel.add(inputBoxsize);
    	box.add(tempPanel);
    	box.add(new JLabel("_________________________"));
    	box.add(new JLabel("                         "));
    	box.add(new JLabel("Evaluation"));
    	box.add(meanEvaluation);
    	box.add(operationData);
    	box.add(new JLabel("      (returns no computation time)"));
    	box.add(new JLabel("                         "));
    	
        int option = JOptionPane.showConfirmDialog(this,
        		box,
                "Manual kNN-Guess",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE
                );
        
        if (option == JOptionPane.OK_OPTION) {
            if(inputNeighbors.getText().length()!=0 && inputIterations.getText().length()!=0 && inputBoxsize.getText().length()!=0)
            {	
            	mesh.kNearestNeighbors=null;
                int neighbors = Integer.parseInt(inputNeighbors.getText());
            	int iterations = Integer.parseInt(inputIterations.getText());
            	int boxsize = Integer.parseInt(inputBoxsize.getText());
            	
            	int radius = 0;
        		if(meanEvaluation.isSelected() == true)
        		{
	            	boolean radiusValid = false;
        			int radiusMax = mesh.getTotalNumVertices()/2;
	            	while(radiusValid == false)
	            	{
		            	box = new Box(BoxLayout.Y_AXIS);
		        		tempPanel = new JPanel();
		                tempPanel.add(new JLabel("Radius for evaluation of mean data:"));
		            	box.add(tempPanel);
		        		tempPanel = new JPanel();
		                tempPanel.add(new JLabel("(maximum value is "+String.valueOf(radiusMax)+")"));
		                box.add(tempPanel);
		                tempPanel = new JPanel();
		            	JTextField inputRadius = new JTextField(5);
		            	inputRadius.setDocument(numericDocumentWithMaxLength(7));
		            	inputRadius.setText("0");
		            	tempPanel.add(inputRadius);
		            	box.add(tempPanel);
		                JOptionPane.showConfirmDialog(this,
		                		box,
		                		"Automatic kNN-Guess",
		                        JOptionPane.DEFAULT_OPTION,
		                        JOptionPane.PLAIN_MESSAGE
		                        );
		                radius = Integer.parseInt(inputRadius.getText());
		                
		                if(radius <= radiusMax)
		                {
		                	radiusValid = true;
		                }
	            	}
        		}
            	
            	if(operationData.isSelected() == true)
            	{
            		mesh.guessNeighborsWithEvaluation(iterations, boxsize, neighbors);
            		mesh.evaluateKNN(mesh.getTotalNumVertices()/2-radius,mesh.getTotalNumVertices()/2+radius,neighbors,iterations,boxsize,meanEvaluation.isSelected(),true);
            	}
            	else
            	{
            		mesh.guessNeighbors(iterations, boxsize, neighbors);
            		if(meanEvaluation.isSelected() == true)
            		{
            			mesh.evaluateKNN(mesh.getTotalNumVertices()/2-radius,mesh.getTotalNumVertices()/2+radius,neighbors,iterations,boxsize,meanEvaluation.isSelected(),false);
            		}
            	}
            }
           	}
        }
    }
    
    /* Initializes the input for an additional kNN-Guess */
    private void input_kNNplus() {
    	JPanel tempPanel;
    	Box box = new Box(BoxLayout.Y_AXIS);
    	
    	if(mesh.kNearestNeighbors == null)
    	{
    		tempPanel = new JPanel();
            tempPanel.add(new JLabel("No previous guess performed."));
            box.add(tempPanel);
            JOptionPane.showConfirmDialog(this,
            		box,
                    "Additional kNN-Guess",
                    JOptionPane.DEFAULT_OPTION,
                    JOptionPane.PLAIN_MESSAGE
                    );
    	}
    	else
    	{
    	JTextField inputNeighbors,inputIterations,inputBoxsize;
    
    	inputNeighbors = new JTextField(String.valueOf(mesh.kNearestNeighbors[0].heap_max_neighbors),5);
    	inputIterations = new JTextField(5);
    	inputIterations.setDocument(numericDocumentWithMaxLength(7));
    	inputBoxsize = new JTextField(5);
    	inputBoxsize.setDocument(numericDocumentWithMaxLength(7));
    	
    	JCheckBox meanEvaluation = new JCheckBox("mean value");
    	JCheckBox operationData = new JCheckBox("internal operation data");
    	
        tempPanel = new JPanel();
        tempPanel.add(new JLabel("Neighbors    "));
        inputNeighbors.setEditable(false);
        tempPanel.add(inputNeighbors);
    	box.add(tempPanel);
        tempPanel = new JPanel();
    	tempPanel.add(new JLabel("Iterations     "));
    	tempPanel.add(inputIterations);
    	box.add(tempPanel);
        tempPanel = new JPanel();
    	tempPanel.add(new JLabel("Boxsize        "));
    	tempPanel.add(inputBoxsize);
    	box.add(tempPanel);
    	box.add(new JLabel("_________________________"));
    	box.add(new JLabel("                         "));
    	box.add(new JLabel("Evaluation"));
    	box.add(meanEvaluation);
    	box.add(operationData);
    	box.add(new JLabel("      (returns no computation time)"));
    	box.add(new JLabel("                         "));
    	
        int option = JOptionPane.showConfirmDialog(this,
        		box,
                "Additional kNN-Guess",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE
                );
        
        if (option == JOptionPane.OK_OPTION) {
            if(inputIterations.getText().length()!=0 && inputBoxsize.getText().length()!=0)
            {	
            	int neighbors = mesh.kNearestNeighbors[0].heap_max_neighbors;
            	int iterations = Integer.parseInt(inputIterations.getText());
            	int boxsize = Integer.parseInt(inputBoxsize.getText());
            	
            	int radius = 0;
        		if(meanEvaluation.isSelected() == true)
        		{
	            	boolean radiusValid = false;
        			int radiusMax = mesh.getTotalNumVertices()/2;
	            	while(radiusValid == false)
	            	{
		            	box = new Box(BoxLayout.Y_AXIS);
		        		tempPanel = new JPanel();
		                tempPanel.add(new JLabel("Radius for evaluation of mean data:"));
		            	box.add(tempPanel);
		        		tempPanel = new JPanel();
		                tempPanel.add(new JLabel("(maximum value is "+String.valueOf(radiusMax)+")"));
		                box.add(tempPanel);
		                tempPanel = new JPanel();
		            	JTextField inputRadius = new JTextField(5);
		            	inputRadius.setDocument(numericDocumentWithMaxLength(7));
		            	inputRadius.setText("0");
		            	tempPanel.add(inputRadius);
		            	box.add(tempPanel);
		                JOptionPane.showConfirmDialog(this,
		                		box,
		                		"Automatic kNN-Guess",
		                        JOptionPane.DEFAULT_OPTION,
		                        JOptionPane.PLAIN_MESSAGE
		                        );
		                radius = Integer.parseInt(inputRadius.getText());
		                
		                if(radius <= radiusMax)
		                {
		                	radiusValid = true;
		                }
	            	}
        		}
            	
            	if(operationData.isSelected() == true)
            	{
            		mesh.guessNeighborsWithEvaluation(iterations, boxsize, neighbors);
            		mesh.evaluateKNN(mesh.getTotalNumVertices()/2-radius,mesh.getTotalNumVertices()/2+radius,neighbors,iterations,boxsize,meanEvaluation.isSelected(),true);
            	}
            	else
            	{
            		mesh.guessNeighbors(iterations, boxsize, neighbors);
            		if(meanEvaluation.isSelected() == true)
            		{
            			mesh.evaluateKNN(mesh.getTotalNumVertices()/2-radius,mesh.getTotalNumVertices()/2+radius,neighbors,iterations,boxsize,meanEvaluation.isSelected(),false);
            		}
            	}
            }
        }
    	}
    }
    
    public void kNNpointcloud() {
    	JPanel tempPanel;
    	Box box = new Box(BoxLayout.Y_AXIS);
    	
    	if(mesh.kNearestNeighbors == null)
    	{
    		tempPanel = new JPanel();
            tempPanel.add(new JLabel("No previous guess performed."));
            box.add(tempPanel);
            JOptionPane.showConfirmDialog(this,
            		box,
                    "Generate kNN point cloud",
                    JOptionPane.DEFAULT_OPTION,
                    JOptionPane.PLAIN_MESSAGE
                    );
    	}
    	else
    	{
        	JRadioButton generateByMean = new JRadioButton("mean value 100%");
        	box.add(generateByMean);
        	JRadioButton generateByFilledMaxHeaps = new JRadioButton("all max-heaps filled");
        	box.add(generateByFilledMaxHeaps);
	    	ButtonGroup buttongroup = new ButtonGroup();
	    	buttongroup.add(generateByMean);
	    	buttongroup.add(generateByFilledMaxHeaps);
            int option = JOptionPane.showConfirmDialog(this,
            		box,
                    "Generate kNN point cloud",
                    JOptionPane.OK_CANCEL_OPTION,
                    JOptionPane.PLAIN_MESSAGE
                    );
            
            if (option == JOptionPane.OK_OPTION) {
            	
		        System.out.println("Generating kNN point cloud...");
		        long timer = System.currentTimeMillis();
		        
		        kNNevaluation evaluation = new kNNevaluation();
		        
		        int totalNumVertices = mesh.getTotalNumVertices();
		    	int correctVertices = 0;
		
		        double[] x = new double[totalNumVertices];
		        double[] y = new double[totalNumVertices];
		        double[] z = new double[totalNumVertices];
		
		        for(int i=0; i<totalNumVertices; i++) {
		        	
		        	
					if(((i+1) % (totalNumVertices/100)) == 0)
					{
						System.out.println((i+1)/(totalNumVertices/100)+" / 100 - " + (System.currentTimeMillis()-timer) + "msec");
					}
					
					
					if(generateByMean.isSelected())
					{
				        if(evaluation.kNNmeanValueOfOnePoint(mesh.kNearestNeighbors[i].heap_max_neighbors, mesh.kNearestNeighbors[i], mesh.vertices[i], mesh.vertices) == 1.0) {
				        	x[correctVertices] = mesh.vertices[i].p[0];
				        	y[correctVertices] = mesh.vertices[i].p[1];
				        	z[correctVertices] = mesh.vertices[i].p[2];
				        	correctVertices++;
				        }
					}
			        
					if(generateByFilledMaxHeaps.isSelected())
					{
				        if(mesh.kNearestNeighbors[i].heap_current_neighbors == mesh.kNearestNeighbors[i].heap_max_neighbors) {
				        	x[correctVertices] = mesh.vertices[i].p[0];
				        	y[correctVertices] = mesh.vertices[i].p[1];
				        	z[correctVertices] = mesh.vertices[i].p[2];
				        	correctVertices++;
				        }
					}
		        }
		    	
		    	try{
			        PrintWriter pWriter = new PrintWriter(new FileWriter("C:\\Users\\LetsPlayHideAndSeek\\Documents\\kNN-point-cloud.off")); 
			        pWriter.println("OFF");
			        pWriter.println(correctVertices + " " + "0 0");
			        for(int i=0; i<correctVertices; i++) {
			        	pWriter.println(x[i] + " " + y[i] + " " + z[i]);	
			        }
		        	pWriter.flush(); 
			        pWriter.close();
		    	}
		    	catch(IOException ioe){
		    		ioe.printStackTrace();
		    	}
		    	
		        System.out.println("Generated point cloud! ~"+(System.currentTimeMillis()-timer+"msec"));
            }
        }
    }
    
    private void addNoise() {
    	
    	
    	JPanel tempPanel;
    	Box box = new Box(BoxLayout.Y_AXIS);
    	
    	if(mesh.getTotalNumVertices() == 0)
    	{
    		tempPanel = new JPanel();
            tempPanel.add(new JLabel("No mesh found."));
            box.add(tempPanel);
            JOptionPane.showConfirmDialog(this,
            		box,
                    "Add Noise",
                    JOptionPane.DEFAULT_OPTION,
                    JOptionPane.PLAIN_MESSAGE
                    );
    	}
    	else
    	{
    	JTextField inputNoise = new JTextField(5);
        tempPanel = new JPanel();
    	tempPanel.add(new JLabel("Noise    "));
    	tempPanel.add(inputNoise);
    	box.add(tempPanel);
    	
        int option = JOptionPane.showConfirmDialog(this,
        		box,
                "Add Noise",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE
                );
        
        if (option == JOptionPane.OK_OPTION) {
        	double noise = Double.parseDouble(inputNoise.getText());
        	mesh.addNoiseToVertices(noise);
        }
    	}
    	
        
    	vp.fullPaint();
    }
    
    private void input_computeCT() {
    	
    	JPanel tempPanel;
    	Box box = new Box(BoxLayout.Y_AXIS);
    	
    	if(mesh.getTotalNumVertices() == 0)
    	{
    		tempPanel = new JPanel();
            tempPanel.add(new JLabel("No mesh found."));
            box.add(tempPanel);
            JOptionPane.showConfirmDialog(this,
            		box,
                    "Compute CT",
                    JOptionPane.DEFAULT_OPTION,
                    JOptionPane.PLAIN_MESSAGE
                    );
    	}
    	else
    	{
    	JTextField inputNeighbors,inputIterations,inputBoxsize;
    	
    	inputNeighbors = new JTextField(5);
    	inputNeighbors.setDocument(numericDocumentWithMaxLength((int)Math.log10(mesh.getTotalNumVertices())+1));
    	inputIterations = new JTextField(5);
    	inputIterations.setDocument(numericDocumentWithMaxLength(7));
    	inputBoxsize = new JTextField(5);
    	inputBoxsize.setDocument(numericDocumentWithMaxLength(7));
    	
        tempPanel = new JPanel();
    	tempPanel.add(new JLabel("Neighbors    "));
    	tempPanel.add(inputNeighbors);
    	box.add(tempPanel);
        tempPanel = new JPanel();
    	tempPanel.add(new JLabel("Iterations     "));
    	tempPanel.add(inputIterations);
    	box.add(tempPanel);
        tempPanel = new JPanel();
    	tempPanel.add(new JLabel("Boxsize        "));
    	tempPanel.add(inputBoxsize);
    	box.add(tempPanel);
    	
        int option = JOptionPane.showConfirmDialog(this,
        		box,
                "Computation Time",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE
                );
        
        if (option == JOptionPane.OK_OPTION) {
            int neighbors = Integer.parseInt(inputNeighbors.getText());
        	int iterations = Integer.parseInt(inputIterations.getText());
        	int boxsize = Integer.parseInt(inputBoxsize.getText());
        	
        	mesh.computeComputationTime(neighbors, iterations, boxsize, 25);
        }
        }
    	
    }
    
 private void input_computeLCTMH() {
    	
    	JPanel tempPanel;
    	Box box = new Box(BoxLayout.Y_AXIS);
    	
    	if(mesh.getTotalNumVertices() == 0)
    	{
    		tempPanel = new JPanel();
            tempPanel.add(new JLabel("No mesh found."));
            box.add(tempPanel);
            JOptionPane.showConfirmDialog(this,
            		box,
                    "Manual kNN-Guess",
                    JOptionPane.DEFAULT_OPTION,
                    JOptionPane.PLAIN_MESSAGE
                    );
    	}
    	else
    	{
    	JTextField inputNeighbors;
    	
    	inputNeighbors = new JTextField(5);
    	inputNeighbors.setDocument(numericDocumentWithMaxLength((int)Math.log10(mesh.getTotalNumVertices())+1));
    	
        tempPanel = new JPanel();
    	tempPanel.add(new JLabel("Neighbors    "));
    	tempPanel.add(inputNeighbors);
    	box.add(tempPanel);
    	
        int option = JOptionPane.showConfirmDialog(this,
        		box,
                "Compute LCT using MaxHeap",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE
                );
        
        if (option == JOptionPane.OK_OPTION) {
            int neighbors = Integer.parseInt(inputNeighbors.getText());
        	
        	mesh.kNN_LinearSearchViaMaxHeap(neighbors);
        }
        }
    	
    }
 
 private void input_computeLCTQS() {
 	
 	JPanel tempPanel;
 	Box box = new Box(BoxLayout.Y_AXIS);
 	
 	if(mesh.getTotalNumVertices() == 0)
 	{
 		tempPanel = new JPanel();
         tempPanel.add(new JLabel("No mesh found."));
         box.add(tempPanel);
         JOptionPane.showConfirmDialog(this,
         		box,
                 "Manual kNN-Guess",
                 JOptionPane.DEFAULT_OPTION,
                 JOptionPane.PLAIN_MESSAGE
                 );
 	}
 	else
 	{
 	JTextField inputNeighbors;
 	
 	inputNeighbors = new JTextField(5);
 	inputNeighbors.setDocument(numericDocumentWithMaxLength((int)Math.log10(mesh.getTotalNumVertices())+1));
 	
     tempPanel = new JPanel();
 	tempPanel.add(new JLabel("Neighbors    "));
 	tempPanel.add(inputNeighbors);
 	box.add(tempPanel);
 	
     int option = JOptionPane.showConfirmDialog(this,
     		box,
             "Compute LCT using QuickSort",
             JOptionPane.OK_CANCEL_OPTION,
             JOptionPane.PLAIN_MESSAGE
             );
     
     if (option == JOptionPane.OK_OPTION) {
         int neighbors = Integer.parseInt(inputNeighbors.getText());
     	
     	mesh.kNN_LinearSearchViaQuickSort(neighbors);
     }
     }
 	
 }

    private void changeLight() {
		if(clpp==null)
			clpp = new ChangeLightParameterFrame(vp);
    }
    private void changeColor() {
		if(ccpp==null)
			ccpp = new ChangeColorParameterFrame(vp);
    }
}