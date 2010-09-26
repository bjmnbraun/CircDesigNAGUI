package DnaDesignGUI;

import java.applet.Applet;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLayeredPane;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.OverlayLayout;
import javax.swing.border.TitledBorder;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.plaf.basic.BasicBorders.MarginBorder;
import javax.swing.text.JTextComponent;

import DnaDesign.DDSeqDesigner;
import DnaDesign.DomainDesigner;
import DnaDesign.DomainDesigner_SharedUtils;
import DnaDesign.Exception.InvalidDNAMoleculeException;
import DnaDesign.Exception.InvalidDomainDefsException;
import DnaDesignGUI.DNAPreviewStrand.UpdateSuccessfulException;

public class DnaDesignGui extends Applet implements ModalizableComponent, CaretListener{
	public DnaDesignGui(){
		su = new ScaleUtils();
	}
	ScaleUtils su;
	private boolean started = false;
	public void start(){
		if(started) return;
		started = true;
		setBackground(Color.white);
		new Thread(){
			public void run(){
				runStartRoutine();
			}
		}.start();
	}
	private void runStartRoutine(){
		//Load up the bottompanel
		
		JPanel bottomPanel = new JPanel(){
			public boolean isOptimizedDrawingEnabled(){
				return false;
			}
		};
		bottomPanel.setOpaque(false);
		JPanel leftPanel = new JPanel();
		JPanel rightPanel = new JPanel();
		leftPanel.setLayout(new FlowLayout(FlowLayout.LEFT,0,0));
		rightPanel.setLayout(new FlowLayout(FlowLayout.LEFT,0,0));
		float fractionLeftPanel = .6f;
		su.addPreferredSize(leftPanel, fractionLeftPanel, 1f); //Left half
		su.addPreferredSize(rightPanel, 1f-fractionLeftPanel, 1f); //Right half
		
		bottomPanel.setLayout(new BorderLayout());
		bottomPanel.add(leftPanel, BorderLayout.WEST);
		bottomPanel.add(rightPanel, BorderLayout.EAST);
		
		//
		float leftPanelHeight = 0;
		int scW = 0;

		DomainDef = new JTextArea();
		editableTextArea(DomainDef);

		Molecules = new JTextArea();
		editableTextArea(Molecules);
		
		ErrorsOutText = new JTextArea();
		ErrorsOutText.setEditable(false);
		ErrorsOutText.setWrapStyleWord(true);
		ErrorsOutText.setLineWrap(true);
		ErrorsOutText.setBorder(BorderFactory.createEtchedBorder());
		reportError(null,null);
		
		String[][] ComponentHelp = new String[][]{
				{
					"Domain Definition",
					"The input of this box describes the DNA 'domains' which will be designed or used by the designer. Each line of this textual input describes a seperate domain in multiple fields: ID, Length, Sequence Constraints, Flags. <br><br>Example:<br>" +
					"1	8	[--------]	<br>" +
					"2	10	GRR[-----]	<br>" +
					"a	4	GACC	<br>" +
					"3	10	[GACTCCAG]	-iso(3)<br>" +
					"4	9	[AAAAAAAAA]	-p<br>" +
					"5	9	[AAAAAAAAA]	-pz(2)<br>" +
					"<br>" +
					"In this example, domain '1' is 8bp long and has no constraints. Domain '2' has some constraints imposed, where 'R' is a degenerate basepair (Google search for more information on this topic). Domains 'a' is locked, and will not be modified by the designer. Wrapping a portion of the constraint in square brackets ('[' and ']') flags that the given portion of the domain is mutable. Domain 3 is not locked, but the designer will initiall seed its sequence with the given code." +
					"Additionally, the -iso(3) flag means that at most 3 d/h bases will be introduced in this domain, where d/h are the 'ISOC' and 'ISOG' bases, respectively. By default, no such exotic bases are added. Domain 4 will be mutated to maintain its initial amino acid sequence (-p flag), and Domain 5 has a flag, -pz, which is similar to the -iso flag except for the 'p' and 'z' exotic bases."
				},
				{
					"Molecules",
					"The input of this box describes the DNA molecules which are present in the solution being designed. Each line of this textual input describes a seperate molecule, in a format of multiple \"domain(Complement?)|\" elements. The designer will implicitly recognize the constraints imposed by these definitions. That is, <br>" +
					"   1)\tParts of DNA molecules not specified as duplexed will be have secondary structure removed during design.<br>" +
					"   2)\tDNA molecules will be designed to minimize hybridization with one another (nonspecific interactions)<br>" +
					"<br>" +
					"Example:<br>" +
					"C1		[3*|2*|1*}<br>" +
					"H1A-loop		[1*|4*}<br>" +
					"H1A-tail		[5*|6*}"
				}
		};
		
		JTextArea[] ComponentStore = new JTextArea[2];
		ComponentStore[0] = DomainDef;
		ComponentStore[1] = Molecules;
		try {
			for(int compNum = 0; compNum < ComponentStore.length; compNum++){
				JScrollPane DomainDefholder = new JScrollPane(ComponentStore[compNum]);
				JPanel DomainDefWithHelp = new JPanel();
				DomainDefWithHelp.setOpaque(false);
				DomainDefWithHelp.setLayout(new BorderLayout());
				DomainDefWithHelp.add(DomainDefholder, BorderLayout.WEST);
				DomainDefWithHelp.add(new HelpButton(ComponentHelp[compNum][0],ComponentHelp[compNum][1],this,this), BorderLayout.CENTER);
				
				JComponent holder = skinGroup(DomainDefWithHelp, ComponentHelp[compNum][0]);
				leftPanelHeight+=.5f;
				su.addPreferredSize(DomainDefholder, fractionLeftPanel, .5f, -scW-9-HelpButton.dim.width, -32);
				su.addPreferredSize(holder, fractionLeftPanel, .5f, -scW, -4);
				leftPanel.add(holder);
			}
		} catch (Throwable e){
			e.printStackTrace();
		}
		{
			JPanel RunDesignerBox = new JPanel();
			RunDesignerBox.setOpaque(false);
			RunDesignerBox.setLayout(new GridLayout(0,1));
			JButton GoToDesigner = new JButton("Open Designer"){
				{
					addActionListener(new ActionListener(){
						public void actionPerformed(ActionEvent f) {
							try {
								createNewDesigner();
							} catch (Throwable e){
								System.err.println("Caught designer error: ");
								e.printStackTrace();
								System.err.println("//////////////////////");
								reportError(e.getMessage(), null);
								return;
							}
							new RunDesignerPanel(DnaDesignGui.this,cDesign,monoSpaceFont,PreviewGraph);
						}
					});
				}
			};
			
			final JButton SavePreviewImage = new JButton("Snapshot Preview (PDF)");
			SavePreviewImage.addActionListener(new ActionListener(){
				public void actionPerformed(ActionEvent e) {
					if (PreviewSeqs!=null){
						PreviewSeqs.snapShot();
						SavePreviewImage.setEnabled(false);
						new Thread(){
							public void run(){
								while(true){
									if (!PreviewSeqs.isTakingSnapshot()){
										break;
									}
									try {
										Thread.sleep(1);
									} catch (InterruptedException e) {
										e.printStackTrace();
									}
								}
								SavePreviewImage.setEnabled(true);
								JOptionPane.showMessageDialog(DnaDesignGui.this, new JTextArea("File saved to \n"+PreviewSeqs.getLastSnapshotPath()){
									{
										setEditable(false);
									}
								});
							}
						}.start();
					}
				}
			});
			JButton DuplicateReaction = new JButton("Duplicate Reaction System 1x"){
				{
					addActionListener(new ActionListener(){
						public void actionPerformed(ActionEvent e) {
							updatePreview();
							if (CurrentlyNoError){
								String[] duplifiedSystem = DomainDesigner_SharedUtils.duplicateSystem(DomainDef.getText(),Molecules.getText());
								DomainDef.setText(duplifiedSystem[0]);
								Molecules.setText(duplifiedSystem[1]);
							}
						}
					});
				}
			};
			
			RunDesignerBox.add(GoToDesigner);
			RunDesignerBox.add(DuplicateReaction);
			RunDesignerBox.add(SavePreviewImage);
			
			JComponent holder = skinGroup(RunDesignerBox, "Run Designer");
			su.addPreferredSize(RunDesignerBox, 1f-fractionLeftPanel, 0, 0, 100);
			su.addPreferredSize(holder, 1f-fractionLeftPanel, 0, 0, 100);

			rightPanel.add(holder);	
		}
		{
			JComponent holder = skinGroup(ErrorsOutText, "Errors");
			su.addPreferredSize(ErrorsOutText, 1f-fractionLeftPanel, 0, 0, 80);
			su.addPreferredSize(holder, 1f-fractionLeftPanel, 0, 0, 80);

			rightPanel.add(holder);	
		}

		{
			PreviewSeqsProxy = new JPanel(){
				public void setPreferredSize(Dimension d){
					super.setPreferredSize(d);

					//if (getLocation().x!=0){
						Point location = getLocation();
						Component nextParent = getParent();
						for(int i = 0; i < 3; i++){
							location.translate(nextParent.getLocation().x+1,nextParent.getLocation().y+1);
							nextParent = nextParent.getParent();
							//System.out.println(location);
						}
						PreviewSeqs.setLocation(location);
					//}
				}
				public void paintComponent(Graphics g){
					g.setColor(Color.black);
					g.fillRect(0,0,getWidth()-2,getHeight());
				}
				{
					setBackground(Color.white);
					setOpaque(true);
					setBorder(BorderFactory.createEmptyBorder(4,4,4,4));
				}
			};
			PreviewSeqs = new DNAPreviewStrand(){
				public void draw(){
					if(modalPanel!=null && PreviewSeqs!=null){
						if (modalPanel.getComponentCount()==0){
							PreviewSeqs.setPreferredSize(new Dimension(PreviewSeqsProxy.getPreferredSize().width-8,
									PreviewSeqsProxy.getPreferredSize().height-6));
						}
					}
					super.draw();
				}
			};
			PreviewGraph = new DesignerVisualGraph();
			
			JPanel DomainDefWithHelp = new JPanel();
			DomainDefWithHelp.setOpaque(false);
			DomainDefWithHelp.setLayout(new BorderLayout());
			DomainDefWithHelp.add(PreviewSeqsProxy, BorderLayout.WEST);
			DomainDefWithHelp.add(new HelpButton("Preview Molecule",
					"The base pairs in the visualization are colored according to constraints:<br>" +
					"<ul>" +
					"<li><font color="+toHexCol(DNAPreviewStrand.ConstraintColors.G)+">G</font> (with black outline - base immutable)</li>" +
					"<li><font color="+toHexCol(DNAPreviewStrand.ConstraintColors.A)+">A</font> (with black outline - base immutable)</li>" +
					"<li><font color="+toHexCol(DNAPreviewStrand.ConstraintColors.T)+">T</font> (with black outline - base immutable)</li>" +
					"<li><font color="+toHexCol(DNAPreviewStrand.ConstraintColors.C)+">C</font> (with black outline - base immutable)</li>" +
					"<li><font color="+toHexCol(DNAPreviewStrand.ConstraintColors.D)+">D (isoC)</font> (with black outline - base immutable)</li>" +
					"<li><font color="+toHexCol(DNAPreviewStrand.ConstraintColors.H)+">H (isoG)</font> (with black outline - base immutable)</li>" +
					"<li><font color="+toHexCol(DNAPreviewStrand.ConstraintColors.P)+">P</font> (with black outline - base immutable)</li>" +
					"<li><font color="+toHexCol(DNAPreviewStrand.ConstraintColors.Z)+">Z</font> (with black outline - base immutable)</li>" +
					"<li><font color="+toHexCol(DNAPreviewStrand.ConstraintColors.NONE)+">-</font> (unconstrained)</li>" +
					"</ul>",
				this,this), BorderLayout.CENTER);
			
			JComponent holder = skinGroup(DomainDefWithHelp, "Preview Molecule");
			su.addPreferredSize(PreviewSeqsProxy, 1f-fractionLeftPanel, 0, -10-HelpButton.dim.width, 0, 1f);
			su.addPreferredSize(DomainDefWithHelp, 1f-fractionLeftPanel, 0, 0, 0, 1f);
			su.addPreferredSize(holder, 1f-fractionLeftPanel, 0, 0, 4, 1f);
			rightPanel.add(holder);
		}
		//add(PreviewSeqs);
		//add(rightPanel,BorderLayout.EAST);
		
		su.addPreferredSize(leftPanel, fractionLeftPanel, leftPanelHeight); //Left half
		
		//bottomPanel.setBorder(BorderFactory.createEtchedBorder(Color.ORANGE, Color.GRAY));
		JLayeredPane bottom = new JLayeredPane();
		bottom.setOpaque(false);
		bottom.setLayout(new OverlayLayout(bottom));
		bottom.add(bottomPanel, JLayeredPane.DEFAULT_LAYER);
		
		JPanel overlay = new JPanel();
		overlay.setLayout(new FlowLayout(FlowLayout.CENTER));
		overlay.setOpaque(false);
		modalPanel = new JPanel(){
			public void validate(){
				super.validate();
				fixAWT();
			}
		};
		modalPanel.setOpaque(false);
		modalPanel.setLocation(new Point(30,30));
		overlay.add(modalPanel);
		su.addPreferredSize(modalPanel, .9f,.9f); //Left half
		bottom.add(overlay, JLayeredPane.MODAL_LAYER);
		
		su.addPreferredSize(bottom, 1f, 1f, 0, 0); //Whole screen.
		
		//Bottom is whole screen
		setLayout(new OverlayLayout(this));
		add(PreviewSeqs);
		add(PreviewGraph);
		add(bottom);

		PreviewSeqs.init();
		PreviewSeqs.start();
		
		PreviewGraph.init();
		PreviewGraph.start();

		PreviewGraph.setVisible(false);
		
		validate();
	}
	
	
	private static String toHexCol(float[] g) {
		return String.format("#%02x%02x%02x",(int)g[0],(int)g[1],(int)g[2]);
	}
	private void editableTextArea(JTextArea domainDef2) {
		domainDef2.setEditable(true);
		domainDef2.setTabSize(4);
		domainDef2.setFont(monoSpaceFont);
		domainDef2.addCaretListener(this);
	}
	private Font monoSpaceFont = Font.decode("Monospaced-12");
	private BufferedImage tmpImage = null;
	private Graphics createGraphics;
	public void paint(Graphics g){
		/*
		if (tmpImage==null || tmpImage.getWidth()!=getWidth()||tmpImage.getHeight()!=getHeight()){
			tmpImage = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration().createCompatibleImage(getWidth(), getHeight());
			createGraphics = tmpImage.getGraphics();
			super.paint(createGraphics);
		}
		g.drawImage(tmpImage, 0,0, null);
		super.paint(createGraphics);
		*/
		super.paint(g);
	}
	private void fixAWT(){
		su.pushSizes(getWidth(), getHeight());
		if(modalPanel!=null && PreviewSeqs!=null){
			if (modalPanel.getComponentCount()>0){
				PreviewSeqs.setPreferredSize(new Dimension(0,0));
			} else {
			}
		} 
	}
	public void invalidate(){
		//Good time as any.
		su.pushSizes(getWidth(), getHeight());
		for(Runnable q : modalScale){
			q.run();
		}
		super.invalidate();
	}
	private JComponent skinGroup(final Component inner, String string) {
		JComponent inner2 = new JPanel(){
			{
				setOpaque(false);
				setLayout(new BorderLayout());
				add(inner, BorderLayout.CENTER);
			}
		    protected void paintComponent(Graphics g) {
		        int width = getWidth();
		        int height = getHeight();

				((Graphics2D)g).setRenderingHint(RenderingHints.KEY_ANTIALIASING, // Anti-alias!
				        RenderingHints.VALUE_ANTIALIAS_ON);
		        // Paint a rounded rectangle in the background.
		        g.setColor(Color.white);
		        int round = 15;
		        int topY = 1;
		        g.fillRoundRect(1, topY, width-3, height-topY-1, round,round);
		        g.setColor(Color.black);
		        g.drawRoundRect(1, topY, width-3, height-topY-1, round,round);
		        super.paintComponent(g);
		    }
		};
		inner2.setBorder(BorderFactory.createTitledBorder(
				BorderFactory.createEmptyBorder(),
				string,
				TitledBorder.LEFT,
				TitledBorder.BELOW_TOP));
		return inner2;
	}
	private JTextArea DomainDef, Molecules, ErrorsOutText;
	private String DomainDef_CLine="", Molecules_CLine="";
	private DNAPreviewStrand PreviewSeqs;
	private DesignerVisualGraph PreviewGraph;
	private JPanel PreviewSeqsProxy;
	private DDSeqDesigner cDesign;
	private void createNewDesigner(){
		ArrayList<String> inputStrands = new ArrayList<String>();
		for(String q : Molecules.getText().split("\n")){
			String[] line = q.split("\\s+");
			if (line.length == 0 ){
				continue;
			}
			if (line.length != 2){
				throw new RuntimeException("Correct Molecule format: <name> <molecule> @ "+line[0]);
			}
			inputStrands.add(q);
		}
		cDesign = DomainDesigner.getDefaultDesigner(inputStrands,DomainDef.getText());
	}
	private JPanel modalPanel;
	private ArrayList<Runnable> modalScale = new ArrayList();
	public JPanel getModalPanel() {
		return modalPanel;
	}

	public void addModalScale(Runnable runnable) {
		modalScale.add(runnable);
		runnable.run();
	}


	public void removeAllModalScale() {
		modalScale.clear();
	}


	public void caretUpdate(CaretEvent e) {
		int wise = e.getDot();
		String[] lines = ((JTextComponent)e.getSource()).getText().split("\n");
		int countLine = 0, countLineTotal = 0;
		for(String q : lines){
			countLineTotal += q.length()+1;
			if (wise <= countLineTotal){
				break;
			}
			countLine++;
		}
		//System.out.println(wise+" "+lines[countLine]);
		if (e.getSource()==DomainDef){
			DomainDef_CLine = lines[countLine];
		} else {
			Molecules_CLine = lines[countLine];
		}
		updatePreview();
	}
	private void updatePreview(){
		try {
			String[] cline = Molecules_CLine.trim().split("\\s+");
			String subCline = "";
			if (cline.length == 0 || cline[0].length()==0){
				return;
			}
			if (cline.length >= 2){
				subCline = cline[1];
			}
			PreviewSeqs.setCurrentPreviewMolecule(subCline, DomainDef.getText());
		} catch (InvalidDNAMoleculeException e){
			reportError(e.getMessage(), Molecules);
		} catch (InvalidDomainDefsException e){
			reportError(e.getMessage(), DomainDef);
		} catch (UpdateSuccessfulException e){
			reportError(null, null);
		} catch (Throwable e){
			reportError(e.getMessage(), null);
			e.printStackTrace();
		}
	}
	private JComponent blinker_whichBlink;
	private Thread blinkerThread = new Thread(){
		public void run(){
			int BLINK_INTERVAL = 300, MAX_BLINK = 10;
			JComponent currentBlink = blinker_whichBlink;
			int blink = 0;
			while(true){
				int BLINK_PART = 10;
				loop: for(int k = 0; k < BLINK_INTERVAL/BLINK_PART; k++){
					if (blinker_whichBlink!=currentBlink){
						blink = 0;
						if (currentBlink!=null){
							setBlink(currentBlink,blink%2==0);
						}
						currentBlink = blinker_whichBlink;
						break loop;
					}
					try {
						Thread.sleep(BLINK_PART);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				if (currentBlink==null){
					continue;
				}
				setBlink(currentBlink,(++blink)%2==0);
				if (blink > MAX_BLINK){
					blinker_whichBlink = null;
				}
			}
		}
		private void setBlink(JComponent onBlink, boolean blink){
			if (blink){
				onBlink.setBorder(new MarginBorder());
			} else {
				onBlink.setBorder(BorderFactory.createEtchedBorder(new Color(100,100,100),new Color(240,240,255)));
			}
		}
	};{
		blinkerThread.start();
	}
	private boolean CurrentlyNoError = true;
	private void reportError(String error, JComponent whichTarget){
		blinker_whichBlink = whichTarget;
		if (error==null){
			ErrorsOutText.setText("No problems here");
			CurrentlyNoError = true;
		} else {
			ErrorsOutText.setText(error);
			CurrentlyNoError = false;
		}
	}
}
