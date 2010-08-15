package DnaDesign.GUI;

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
import java.lang.reflect.Field;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLayeredPane;
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
import DnaDesign.DomainDesigner_5_RandomDesigner2;
import DnaDesign.Exception.InvalidDNAMoleculeException;
import DnaDesign.GUI.DNAPreviewStrand.UpdateSuccessfulException;

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
		ErrorsOutText.setBorder(BorderFactory.createEtchedBorder());
		reportError(null,null);
		
		Field[] ComponentStore = null;
		String[][] ComponentHelp = new String[][]{
				{
					"Domain Definition",
					"The input of this box describes the DNA 'domains' which will be designed or used by the designer. Each line of this textual input describes a seperate domain in 4 fields: ID, Length, Sequence Constraints, and a flag for the presence of the Reverse Complement. \n\nExample:\n" +
					"1	8	[--------]	*Y\n" +
					"2	10	[GRR-----]	*Y\n" +
					"a	4	GACC	*N\n" +
					"3	10	GACTCCAG	*Y\n" +
					"\n" +
					"In this example, domain '1' is 8bp long and has no constraints. Domain '2' has some constraints imposed, where 'R' is a degenerate basepair (Google search for more information on this topic). Domains 'a' and '3' are locked, and will not be modified by the designer."
				},
				{
					"Molecules",
					"The input of this box describes the DNA molecules which are present in the solution being designed. Each line of this textual input describes a seperate molecule, in a format of multiple \"domain(Complement?)|\" elements. The designer will implicitly recognize the constraints imposed by these definitions. That is, \n" +
					"   1)\tParts of DNA molecules not specified as duplexed will be have secondary structure removed during design.\n" +
					"   2)\tDNA molecules will be designed to not hybridize with one another (nonspecific interactions) unless they contain at least one complementary domain.\n" +
					"\n" +
					"Example:\n" +
					"C1		[3*|2*|1*}\n" +
					"H1A-loop		[1*|4*}\n" +
					"H1A-tail		[5*|6*}"
				}
		};
		try {
			ComponentStore = new Field[]{
					getClass().getDeclaredField("DomainDef"),
					getClass().getDeclaredField("Molecules"),
			};
		} catch (Throwable e){
			e.printStackTrace();
		}
		try {
			for(int compNum = 0; compNum < ComponentStore.length; compNum++){
				Field compThis = ComponentStore[compNum];
				JScrollPane DomainDefholder = new JScrollPane((Component)compThis.get(this));
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
								//e.printStackTrace();
								reportError(e.getMessage(), null);
								return;
							}
							new RunDesignerPanel(DnaDesignGui.this,cDesign,monoSpaceFont);
						}
					});
				}
			};
			
			JButton SavePreviewImage = new JButton("Snapshot Preview");
			RunDesignerBox.add(GoToDesigner);
			RunDesignerBox.add(SavePreviewImage);
			
			JComponent holder = skinGroup(RunDesignerBox, "Run Designer");
			su.addPreferredSize(RunDesignerBox, 1f-fractionLeftPanel, 0, 0, 70);
			su.addPreferredSize(holder, 1f-fractionLeftPanel, 0, 0, 70);

			rightPanel.add(holder);	
		}
		{
			JComponent holder = skinGroup(ErrorsOutText, "Errors");
			su.addPreferredSize(ErrorsOutText, 1f-fractionLeftPanel, 0, 0, 80);
			su.addPreferredSize(holder, 1f-fractionLeftPanel, 0, 0, 80);

			rightPanel.add(holder);	
		}

		{
			JPanel PreviewSeqsProxy = new JPanel(){
				public void setPreferredSize(Dimension d){
					PreviewSeqs.setPreferredSize(new Dimension(d.width-3,d.height-3));
					super.setPreferredSize(d);

					if (getLocation().x!=0){
						Point location = getLocation();
						Component nextParent = getParent();
						for(int i = 0; i < 2; i++){
							location.translate(nextParent.getLocation().x+1,nextParent.getLocation().y+1);
							nextParent = nextParent.getParent();
						}
						PreviewSeqs.setLocation(location);
					}
				}
				{
					setBackground(Color.black);
					setOpaque(true);
					setBorder(BorderFactory.createEmptyBorder(1,1,1,1));
				}
			};
			PreviewSeqs = new DNAPreviewStrand();

			JComponent holder = skinGroup(PreviewSeqsProxy, "Preview Molecule");
			su.addPreferredSize(PreviewSeqsProxy, 1f-fractionLeftPanel, 0, -8, 0, 1f);
			su.addPreferredSize(holder, 1f-fractionLeftPanel, 0, 0, 21, 1f);
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
		add(bottom);

		PreviewSeqs.init();
		PreviewSeqs.start();

		validate();
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
	private DDSeqDesigner cDesign;
	private void createNewDesigner(){
		ArrayList<String> inputStrands = new ArrayList<String>();
		for(String q : Molecules.getText().split(System.getProperty("line.separator"))){
			String[] line = q.split("\\s+");
			if (line.length == 0 ){
				continue;
			}
			if (line.length == 1){
				inputStrands.add(line[0]);
			} else {
				inputStrands.add(line[1]);
			}
		}
		cDesign = DomainDesigner_5_RandomDesigner2.makeDesigner(inputStrands,DomainDef.getText());
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
		} catch (UpdateSuccessfulException e){
			reportError(null, null);
		} catch (Throwable e){
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
	private void reportError(String error, JComponent whichTarget){
		blinker_whichBlink = whichTarget;
		if (error==null){
			ErrorsOutText.setText("No problems here");
		} else {
			ErrorsOutText.setText(error);
		}
	}
}
