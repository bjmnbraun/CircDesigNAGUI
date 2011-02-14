package DnaDesignGUI;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Scanner;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLayeredPane;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.OverlayLayout;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.plaf.basic.BasicBorders.MarginBorder;
import javax.swing.text.JTextComponent;

import DnaDesign.DDSeqDesigner;
import DnaDesign.DomainDesigner;
import DnaDesign.DomainDesigner_SharedUtils;
import DnaDesign.Config.CircDesigNAConfig;
import DnaDesign.Exception.InvalidDNAMoleculeException;
import DnaDesign.Exception.InvalidDomainDefsException;
import DnaDesign.impl.CodonCode;
import DnaDesignGUI.DNAPreviewStrand.UpdateSuccessfulException;

public class DnaDesignGui extends DnaDesignGUI_ThemedApplet implements ModalizableComponent, CaretListener{
	public DnaDesignGui(){
		su = new ScaleUtils();
		/*
		PlasticLookAndFeel.setPlasticTheme(new DesertRed());
		try {
			UIManager.setLookAndFeel(new PlasticXPLookAndFeel());
		} catch (Exception e) {}
		*/
	}
	ScaleUtils su;
	private boolean started = false;
	public void start(){
		if(started) return;
		parseThemeColors();
		setBackground(Color.white);
		started = true;
		new Thread(){
			public void run(){
				runStartRoutine();
			}
		}.start();
	}
	private void runStartRoutine(){
		//Logic:
		CircDesignConfig = new CircDesigNAConfig();
		
		//Gui:
		
		JPanel bottomPanel = new JPanel(){
			public boolean isOptimizedDrawingEnabled(){
				return false;
			}
		};
		bottomPanel.setOpaque(false);
		JPanel leftPanel = skinPanel(new JPanel());
		JPanel rightPanel = skinPanel(new JPanel());
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
					"The input of this box provides the DNA 'words', or domains, which will be designed. Each line of this textual input describes a seperate domain in the syntax:<br><br>" +
					"&#60;ID&#62; &#60;Length&#62; {Sequence Initialization} {-Option1{(arguments)}}<br><br>" +
					"Where <ul><li>ID is an alphanumeric, unique identifier of the domain, </li><li>Length is the domain's length,  </li><li>Sequence Initialization defines" +
					" the initial state of the domain. Base-specific constraints can be imposed here, using these special codes (for RNA, replace 'T' with 'U')." +
					"<table border=\"1\">" +
					"<tr><th>Codes</th><th>Possible Bases</th></tr>" +
					"<tr><td>N -</td><td> no constraints</td></tr>" +
					"<tr><td>R</td><td> A, G</td></tr>" +
					"<tr><td>Y</td><td> C, G</td></tr>" +
					"<tr><td>M</td><td> A, C</td></tr>" +
					"<tr><td>K</td><td> G, T</td></tr>" +
					"<tr><td>S</td><td> C, G</td></tr>" +
					"<tr><td>W</td><td> A, T</td></tr>" +
					"<tr><td>V</td><td> A, C, G</td></tr>" +
					"<tr><td>H</td><td> A, C, T</td></tr>" +
					"<tr><td>B</td><td> C, G, T</td></tr>" +
					"<tr><td>D</td><td> A, G, T</td></tr>" +
					"</table>" +
					"</li><li>The current available options are:" +
					"<ul><li>-seq(Bases, Min, Max): The domain must contain at least <i>Min</i> and at most <i>Max</i> of the <i>Bases</i> bases. Bases can be a single base," +
					" or it can be multiple bases by separating each base with the '+' sign. <i>Min</i> and <i>Max</i> are in units of bases, but can be input as integer percentages of the" +
					"entire domain by adding the % character (see below for examples). To remove either the <i>Min</i> or <i>Max</i> constraint, set them to a negative value.</li></ul>" +
					"</li></ul>" +
					"&#60;bracket&#62;'ed elements are Required fields, and {bracket}'ed elements are Optional. If Sequence Initialization is provided, then the Length field is ignored.<br><br>" +
					"Enclosing part of the initial sequence string in square brackets ([]) indicates a region that will be mutated. Outside of square brackets, the sequence is immutable. An exception" +
					"is the '-' character, which always indicates a mutable base " +
					"<br><br><u>Example:</u><br>" +
					"1	8	<br>" +
					"2	10	GAA[-----]	<br>" +
					"2a	10	[GRR-----]	<br>" +
					"a	4	GACC	<br>" +
					"3	10	[GACTCCAG]	-seq(A,-1,-1,T,-1,3,G,1,-1,C,2,4)<br>" +
					"4	9	[AAAAAAAAA]	-p<br>" +
					"5  8  -seq(P,-1,-1,Z,-1,-1)<br>"+
					"6  23  -seq(G+C,45%,55%)"+	
					"<br><br>" +
					"Translates to:<br>" +
					"Domain '1' is 8bp long and has no constraints. " +
					"Domain '2' has 3 locked bases (GAA), and the rest is immutable. Domain '2a' constrains its second and third bases to be As or Gs, the rest of its bases are unconstrained, and the first base will be initialized to G." +
					"Domain 'a' is locked, and will not be modified by the designer. " +
					"<br>Wrapping a portion of the constraint in square brackets ('[' and ']') flags that the given portion of the domain is mutable. " +
					"Domain 3 is not locked, but the designer will initially work with the given sequence." +
					"Additionally, Domain 3 has composition based constraints imposed, with the -seq option, where <br>" +
					"<ul>" +
					"<li>Any number of A's are allowed (-1 means no lower bound, -1 means no upper bound)</li>"+
					"<li>Number of T's &#60;= 3 (-1 means no lower bound, 3 is upper bound)</li>"+
					"<li>Number of G's &#62;= 1 (1 is lower bound, -1 means no upper bound)</li>"+
					"<li>2 &#60;= Number of C's &#60;= 4 (2 lower bound, 4 upper bound)</li>"+
					"</ul>"+
					"Additionally, the -seq option also allows one to experiment with exotic bases (P,Z). By default, these bases are set to lower bound 0, upper bound 0 (so they are disallowed). " +
					"In Domain 5, the -seq arguments specify that any number of P and Z are allowed, overriding this default. Domain 6 specifies that the number of G's plus the number of C's will be between" +
					" 45% and 55% of the domain's length" +
					"\n<br>"
				},
				{
					"Molecules",
					"The input of this box provides the DNA molecules which are present in the solution being designed. Each line of this textual input describes a seperate molecule, by listing its component nucleic acid strands." +
					"<ul><li>Each strand is an ordered list of domain ID's, separated by the pipe character '|'</li>" +
					"<li>Each domain ID is optionally followed by the <i>reverse complement</i> operator, the asterisk '*'</li>" +
					"<li>Each domain is marked by a hybridization flag, which is either a '.', a '(', or a ')', which is a Dot-Parenthesis" +
					"representation of the secondary structure formed by the molecule. '.' (no hybridization) is default.</li>" +
					"</ul> The designer will interpret the Dot-Parenthesis structures, and produce its net optimization function from it." +
					"The sequences, subject to domain constraints, will then be chosen so as to minimize this function.<br>" +
					"<br>" +
					"Example:<br>" +
					"C1		[3*|2*|1}<br>" +
					"H1A-loop		[1*|4*}<br>" +
					"H1A-tail		[1*(|5*|6*|4|1)}" +
					"C1_H1A	[3*|2*|(1}[1*)|4*}"
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
				su.addPreferredSize(holder, fractionLeftPanel, .5f, -scW, -4);
				su.addPreferredSize(DomainDefholder, fractionLeftPanel, .5f, -scW-9-HelpButton.dim.width, -32);
				leftPanel.add(holder);
			}
		} catch (Throwable e){
			e.printStackTrace();
		}
		{
			JPanel RunDesignerBox = new JPanel(){
				public Component add(Component comp) {
					if (comp instanceof JButton){
						ButtonSkin.process(DnaDesignGui.this,(JButton)comp);;
					}
					return super.add(comp);
				}
			};
			RunDesignerBox.setOpaque(false);
			RunDesignerBox.setLayout(new GridLayout(0,1));
			JButton GoToDesigner = new JButton("Open Designer"){
				{
					addActionListener(new ActionListener(){
						public void actionPerformed(ActionEvent f) {
							if (getModalPanel()==null){ return;}
							if (getModalPanel().getComponentCount() > 0){
								return;
							}
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
		
			final JPanel RnaDnaToggle = new JPanel();
			JButton RnaButton = new JButton("RNA Design");
			JButton DnaButton = new JButton("DNA Design");
			final JButton[] RnaDnaTogglA = new JButton[]{RnaButton,DnaButton};
			for(JButton q : RnaDnaTogglA){
				ButtonSkin.process(DnaDesignGui.this,(JButton)q);
			}
			class RnaDnaToggleHandler {
				void handleToggle(int id){
					if (modalPanelOpen()){
						return;
					}
					
					if (id==0){
						//Switch to RNA
						CircDesignConfig.setMode(CircDesigNAConfig.RNA_MODE);
					} else {
						//Switch to DNA
						CircDesignConfig.setMode(CircDesigNAConfig.DNA_MODE);
					}
					for(int k = 0; k < RnaDnaTogglA.length; k++){
						RnaDnaTogglA[k].setEnabled(k!=id);
					}
					RnaDnaToggle.validate();
				}
			}
			final RnaDnaToggleHandler RnaDnaToggleHandle = new RnaDnaToggleHandler();
			RnaDnaToggleHandle.handleToggle(1);
			RnaButton.addActionListener(new ActionListener(){
				public void actionPerformed(ActionEvent e) {
					RnaDnaToggleHandle.handleToggle(0);
				}
			});
			DnaButton.addActionListener(new ActionListener(){
				public void actionPerformed(ActionEvent e) {
					RnaDnaToggleHandle.handleToggle(1);
				}
			});
			RnaDnaToggle.setLayout(new GridLayout(1,2));
			RnaDnaToggle.add(RnaButton);
			RnaDnaToggle.add(DnaButton);
			
			JButton ModifyCodonTable = new JButton("Modify Codon Table"){
				{
					addActionListener(new ActionListener(){
						public void actionPerformed(ActionEvent e) {
							if (modalPanelOpen()){
								return;
							}
							
							new ModifyCodonTablePanel(DnaDesignGui.this,monoSpaceFont);
						}
					});
				}
			};
			
			RunDesignerBox.add(GoToDesigner);
			RunDesignerBox.add(DuplicateReaction);
			RunDesignerBox.add(ModifyCodonTable);
			RunDesignerBox.add(RnaDnaToggle);
			RunDesignerBox.add(SavePreviewImage);
			
			JComponent holder = skinGroup(RunDesignerBox, "Run Designer");
			su.addPreferredSize(RunDesignerBox, 1f-fractionLeftPanel, 0, 0, 120);
			su.addPreferredSize(holder, 1f-fractionLeftPanel, 0, 0, 120);

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
						

						if(modalPanel!=null && PreviewSeqs!=null){
							if (modalPanel.getComponentCount()==0){
								PreviewSeqs.setPreferredSize(new Dimension(PreviewSeqsProxy.getPreferredSize().width-8,
										PreviewSeqsProxy.getPreferredSize().height-6));
							}
						}
				}
				public void paintComponent(Graphics g){
					g.setColor(Color.black);
					g.fillRect(0,0,getWidth()-2,getHeight());
				}
				{
					setOpaque(true);
					setBorder(BorderFactory.createEmptyBorder(4,4,4,4));
				}
			};
			PreviewSeqs = new DNAPreviewStrand(this, CircDesignConfig){
				public void draw(){
					super.draw();
				}
			};
			PreviewGraph = new DesignerVisualGraph();
			
			JPanel DomainDefWithHelp = new JPanel();
			DomainDefWithHelp.setOpaque(false);
			DomainDefWithHelp.setLayout(new BorderLayout());
			DomainDefWithHelp.add(PreviewSeqsProxy, BorderLayout.WEST);
			String immcond = "Double line";
			DomainDefWithHelp.add(new HelpButton("Preview Molecule",
					"The base pairs in the visualization are colored according to constraints:<br>" +
					"<ul>" +
					"<li><font color="+toHexCol(DNAPreviewStrand.ConstraintColors.G)+">G</font> ("+immcond+" - base immutable)</li>" +
					"<li><font color="+toHexCol(DNAPreviewStrand.ConstraintColors.A)+">A</font> ("+immcond+" - base immutable)</li>" +
					"<li><font color="+toHexCol(DNAPreviewStrand.ConstraintColors.T)+">T</font> ("+immcond+" - base immutable)</li>" +
					"<li><font color="+toHexCol(DNAPreviewStrand.ConstraintColors.C)+">C</font> ("+immcond+" - base immutable)</li>" +
					//"<li><font color="+toHexCol(DNAPreviewStrand.ConstraintColors.D)+">D (isoC)</font> ("+immcond+" - base immutable)</li>" +
					//"<li><font color="+toHexCol(DNAPreviewStrand.ConstraintColors.H)+">H (isoG)</font> ("+immcond+" - base immutable)</li>" +
					"<li><font color="+toHexCol(DNAPreviewStrand.ConstraintColors.P)+">P</font> ("+immcond+" - base immutable)</li>" +
					"<li><font color="+toHexCol(DNAPreviewStrand.ConstraintColors.Z)+">Z</font> ("+immcond+" - base immutable)</li>" +
					"<li><font color="+toHexCol(DNAPreviewStrand.ConstraintColors.NONE)+">-, N</font> (unconstrained)</li>" +
					"<li><font color="+toHexCol(DNAPreviewStrand.ConstraintColors.R)+">R</font> (see Domain Defs helpfile for specifics)</li>" +
					"<li><font color="+toHexCol(DNAPreviewStrand.ConstraintColors.Y)+">Y</font> (see Domain Defs helpfile for specifics)</li>" +
					"<li><font color="+toHexCol(DNAPreviewStrand.ConstraintColors.M)+">M</font> (see Domain Defs helpfile for specifics)</li>" +
					"<li><font color="+toHexCol(DNAPreviewStrand.ConstraintColors.K)+">K</font> (see Domain Defs helpfile for specifics)</li>" +
					"<li><font color="+toHexCol(DNAPreviewStrand.ConstraintColors.S)+">S</font> (see Domain Defs helpfile for specifics)</li>" +
					"<li><font color="+toHexCol(DNAPreviewStrand.ConstraintColors.W)+">W</font> (see Domain Defs helpfile for specifics)</li>" +
					"<li><font color="+toHexCol(DNAPreviewStrand.ConstraintColors.V)+">V</font> (see Domain Defs helpfile for specifics)</li>" +
					"<li><font color="+toHexCol(DNAPreviewStrand.ConstraintColors.H)+">H</font> (see Domain Defs helpfile for specifics)</li>" +
					"<li><font color="+toHexCol(DNAPreviewStrand.ConstraintColors.B)+">B</font> (see Domain Defs helpfile for specifics)</li>" +
					"<li><font color="+toHexCol(DNAPreviewStrand.ConstraintColors.D)+">D</font> (see Domain Defs helpfile for specifics)</li>" +
					"</ul>" +
					"<br>" +
					"Hotkeys:<br>" +
					"<ul>" +
					"<li>Shift\"+\" - Zoom In" +
					"<li>\"-\" - Zoom Out" +
					"<li>\"d\" - Toggle line or bubble display" +
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
		overlay.add(modalPanel);
		su.addPreferredSize(modalPanel, .9f,.9f); //Left half
		bottom.add(overlay, JLayeredPane.MODAL_LAYER);
		
		su.addPreferredSize(bottom, 1f, 1f, 0, 0); //Whole screen.
		
		//Bottom is whole screen
		setLayout(new OverlayLayout(this));
		add(PreviewSeqs);
		add(PreviewGraph);
		add(bottom);

		PreviewGraph.setVisible(false);
		PreviewSeqs.setVisible(true);
		
		validate();
	}
	public boolean modalPanelOpen(){
		if (getModalPanel()==null){ return false;}
		if (getModalPanel().getComponentCount() > 0){
			return true;
		}
		return false;
	}
	private static String toHexCol(float[] g) {
		return String.format("#%02x%02x%02x",(int)g[0],(int)g[1],(int)g[2]);
	}
	private void editableTextArea(JTextArea domainDef2) {
		domainDef2.setEditable(true);
		domainDef2.setTabSize(4);
		domainDef2.setFont(monoSpaceFont);
		domainDef2.addCaretListener(this);
		domainDef2.setWrapStyleWord(false);
		domainDef2.setLineWrap(false);
		domainDef2.setBackground(Color.WHITE);
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
				//PreviewSeqs.setPreferredSize(new Dimension(0,0));
				PreviewSeqs.setVisible(false);
			} else {
				PreviewSeqs.setVisible(true);
			}
		} 
	}
	private boolean[] invalidate_thread = new boolean[]{true};
	public void invalidate(){
			//Good time as any.
			//su.pushSizes(getWidth(), getHeight());
			fixAWT();
			for(Runnable q : modalScale){
				q.run();
			}
		super.invalidate();
	}

	private JTextArea DomainDef, Molecules, ErrorsOutText;
	private String DomainDef_CLine="", Molecules_CLine="";
	private int Molecules_CLine_num;
	private DNAPreviewStrand PreviewSeqs;
	private DesignerVisualGraph PreviewGraph;
	private JPanel PreviewSeqsProxy;
	private DDSeqDesigner cDesign;
	private CircDesigNAConfig CircDesignConfig;
	private void createNewDesigner(){
		cDesign = DomainDesigner.getDefaultDesigner(Molecules.getText(),DomainDef.getText(),CircDesignConfig);
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

	public String getCurrentCodonTable() {
		return CircDesignConfig.customCodonTable;
	}
	public void updateCodonTable(String text) {
		//Test it first
		new CodonCode(text, CircDesignConfig);
		CircDesignConfig.customCodonTable = text;
	}

	public void caretUpdate(CaretEvent e) {
		int wise = e.getDot();
		Scanner lines = new Scanner(((JTextComponent)e.getSource()).getText());
		int countLine = 0, countLineTotal = 0;
		String q = "";
		while(lines.hasNextLine()){
			q = lines.nextLine();
			countLineTotal += q.length()+1;
			if (wise < countLineTotal){
				break;
			}
			countLine++;
		}
		//System.out.println(wise+" "+lines[countLine]);
		if (e.getSource()==DomainDef){
			DomainDef_CLine = q;
		} else {
			Molecules_CLine_num = countLine;
			Molecules_CLine = q;
		}
		updatePreview();
	}
	private void updatePreview(){
		try {
			int numMolecules = Molecules.getLineCount();
			PreviewSeqs.setCurrentPreviewMolecule(Molecules_CLine_num, numMolecules, Molecules_CLine.trim(), DomainDef.getText());
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
				onBlink.setBorder(BorderFactory.createEtchedBorder(THEMECOL0,THEMECOL1));
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
