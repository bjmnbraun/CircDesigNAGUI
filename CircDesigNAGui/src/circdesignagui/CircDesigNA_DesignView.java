/*
  Part of the CircDesigNA Project - http://cssb.utexas.edu/circdesigna
  
  Copyright (c) 2010-11 Ben Braun
  
  This library is free software; you can redistribute it and/or
  modify it under the terms of the GNU Lesser General Public
  License as published by the Free Software Foundation, version 2.1.

  This library is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  Lesser General Public License for more details.

  You should have received a copy of the GNU Lesser General
  Public License along with this library; if not, write to the
  Free Software Foundation, Inc., 59 Temple Place, Suite 330,
  Boston, MA  02111-1307  USA
*/
package circdesignagui;

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
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.TreeMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLayeredPane;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.OverlayLayout;
import javax.swing.SwingUtilities;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.plaf.basic.BasicBorders.MarginBorder;
import javax.swing.text.JTextComponent;

import circdesigna.BannedPatterns;
import circdesigna.CircDesigNA_SharedUtils;
import circdesigna.ZipExtractor;
import circdesigna.config.CircDesigNAConfig;
import circdesigna.exception.InvalidDNAMoleculeException;
import circdesigna.exception.InvalidDomainDefsException;
import circdesigna.impl.CodonCode;
import circdesignagui.MoleculePreview.UpdateSuccessfulException;

/**
 * Main gui class for the design view, where domain definitions, molecule definitions, and some
 * miscellaneous config options are input. Does not handle the window that pops up when a help button is pressed
 * or the one that pops up when design occurs. (see HelpButton and RunDesignerPanel, respectively)
 */
public class CircDesigNA_DesignView extends ThemedApplet implements ModalizableComponent, CaretListener{
	//input
	public CircDesigNA_DesignView(CircDesigNA_Context context){
		this.context = context;
		su = new ScaleUtils();
	}
	
	//output
	public static class Output {
		public String domainDefs;
		public String molDefs;
		public CircDesigNAConfig cfg;

		public Output(String domainDefs, String molDefs, CircDesigNAConfig circDesignConfig) {
			this.domainDefs = domainDefs;
			this.molDefs = molDefs;
			this.cfg = circDesignConfig;
		}
	}
	
	ScaleUtils su;
	private boolean started = false;
	public void start(){
		start(null);
	}
	public void start(Output initFromData){
		if(started) return;
		started = true;
		
		parseThemeColors();
		setBackground(Color.white);

		if (initFromData==null){
			DomainDef = new JTextArea("1\t8");
			MolDef = new JTextArea("A\t[1}");
			config = new CircDesigNAConfig();
		} else {
			DomainDef = new JTextArea(initFromData.domainDefs);
			MolDef = new JTextArea(initFromData.molDefs);
			config = initFromData.cfg;
		}
		
		//Gui:
		JPanel bottomPanel = new JPanel();
		bottomPanel.setBackground(Color.WHITE);
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

		editableTextArea(DomainDef);

		editableTextArea(MolDef);
		
		ErrorsOutText = new JTextArea();
		ErrorsOutText.setEditable(false);
		ErrorsOutText.setWrapStyleWord(true);
		ErrorsOutText.setLineWrap(true);
		ErrorsOutText.setBorder(BorderFactory.createEtchedBorder());
		reportError(null,null);
		
		String[][] ComponentHelp = new String[][]{
				{
					"Domain Definition",
					getHelpFile("Domain Definition")
				},
				{
					"Molecules Definition",
					getHelpFile("Molecules Definition")
				}
		};
		
		JTextArea[] ComponentStore = new JTextArea[2];
		ComponentStore[0] = DomainDef;
		ComponentStore[1] = MolDef;
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
			JPanel OperationsBox = new JPanel(){
				public Component add(Component comp) {
					if (comp instanceof JButton){
						ButtonSkin.process(CircDesigNA_DesignView.this,(JButton)comp);
					}
					return super.add(comp);
				}
			};
			OperationsBox.setOpaque(false);
			OperationsBox.setLayout(new GridLayout(0,1));
			JButton GoToDesigner = new JButton("Run"){
				{
					addActionListener(new ActionListener(){
						public void actionPerformed(ActionEvent f) {
							if (modalPanelIsOccupied()){
								return;
							}
							try {
								context.setDesignViewOutput(new Output(DomainDef.getText(), MolDef.getText(), config));
								context.openDesignOptionsView();
							} catch (Throwable e){
								System.err.println("Caught designer error: ");
								e.printStackTrace();
								System.err.println("//////////////////////");
								reportError(e.getMessage(), null);
							}
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
								JOptionPane.showMessageDialog(CircDesigNA_DesignView.this, new JTextArea("Snapshots saved to \n"+PreviewSeqs.getLastSnapshotPath()){
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
								try {
									String[] duplifiedSystem = CircDesigNA_SharedUtils.duplicateSystem(DomainDef.getText(),MolDef.getText());
									DomainDef.setText(duplifiedSystem[0]);
									MolDef.setText(duplifiedSystem[1]);
								} catch (Throwable f){
									reportError(f.getMessage(), null);
								}
							}
						}
					});
				}
			};
			
			JButton CyclicRotateSelected = new JButton("Rotate molecules"){
				{
					addActionListener(new ActionListener(){
						public void actionPerformed(ActionEvent e) {
							updatePreview();
							if (CurrentlyNoError){
								try {
									String rotatedSystem = CircDesigNA_SharedUtils.rotateMolecules(config, DomainDef.getText(), MolDef.getText());
									MolDef.setText(rotatedSystem);
								} catch (Throwable f){
									reportError(f.getMessage(), null);
								}
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
				ButtonSkin.process(CircDesigNA_DesignView.this,(JButton)q);
			}
			class RnaDnaToggleHandler {
				void handleToggle(int id){
					if (modalPanelIsOccupied()){
						return;
					}
					
					if (id==0){
						//Switch to RNA
						config.setMode(CircDesigNAConfig.RNA_MODE);
					} else {
						//Switch to DNA
						config.setMode(CircDesigNAConfig.DNA_MODE);
					}
					for(int k = 0; k < RnaDnaTogglA.length; k++){
						RnaDnaTogglA[k].setEnabled(k!=id);
					}
					RnaDnaToggle.invalidate();
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
							if (modalPanelIsOccupied()){
								return;
							}
							
							new ModifyCodonTablePanel(CircDesigNA_DesignView.this,monoSpaceFont);
						}
					});
				}
			};
			
			JButton ModifyBannedPatterns = new JButton("Add Banned Patterns"){
				{
					addActionListener(new ActionListener(){
						public void actionPerformed(ActionEvent e) {
							if (modalPanelIsOccupied()){
								return;
							}
							
							new ModifyBannedPatternsListPanel(CircDesigNA_DesignView.this, monoSpaceFont);
						}
					});
				}
			};
			
			OperationsBox.add(GoToDesigner);
			OperationsBox.add(DuplicateReaction);
			OperationsBox.add(CyclicRotateSelected);
			OperationsBox.add(ModifyCodonTable);
			OperationsBox.add(ModifyBannedPatterns);
			OperationsBox.add(RnaDnaToggle);
			OperationsBox.add(SavePreviewImage);
			
			JComponent holder = skinGroup(OperationsBox, "Run Designer");
			su.addPreferredSize(OperationsBox, 1f-fractionLeftPanel, 0, 0, 140);
			su.addPreferredSize(holder, 1f-fractionLeftPanel, 0, 0, 140);

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
			PreviewSeqs = new MoleculePreview(this, config);
			PreviewGraph = new DesignerVisualGraph();
			
			JPanel DomainDefWithHelp = new JPanel();
			DomainDefWithHelp.setOpaque(false);
			DomainDefWithHelp.setLayout(new BorderLayout());
			DomainDefWithHelp.add(PreviewSeqsProxy, BorderLayout.WEST);
			
			DomainDefWithHelp.add(new HelpButton("Preview Molecule", getHelpFile("Preview Molecule"),
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
		bottom.setLayout(new OverlayLayout(bottom));
		bottom.add(bottomPanel, JLayeredPane.DEFAULT_LAYER);
		
		JPanel overlay = new JPanel();
		overlay.setLayout(new FlowLayout(FlowLayout.CENTER));
		overlay.setOpaque(false);
		modalPanel = new JPanel(){
			public void validate(){
				fixAWT();
				super.validate();
			}
		};
		modalPanel.setOpaque(false);
		overlay.add(modalPanel);
		su.addPreferredSize(modalPanel, .9f,.9f); //Left half
		bottom.add(overlay, JLayeredPane.MODAL_LAYER);
		
		//su.addPreferredSize(bottom, 1f, 1f, 0, 0); //Whole screen.

		//Bottom is whole screen
		setLayout(new BorderLayout());
		add(PreviewSeqs);
		add(PreviewGraph);
		add(bottom);

		PreviewSeqs.init();
		PreviewSeqs.start();
		PreviewGraph.init();
		PreviewGraph.start();	
		
		//Initial display mode
		PreviewGraph.setVisible(false);
		
		SwingUtilities.invokeLater(new Runnable(){
			public void run(){
				invalidate();
				validate();
			}
		});
	}
	
	////////////////////////
	//Helper methods
	private TreeMap<String, String> getHelpFile_memo;
	private String getHelpFile(String string) {
		if (getHelpFile_memo==null){
			getHelpFile_memo = new TreeMap();
			ZipInputStream zis = ZipExtractor.getFile("helpfiles.zip");
			ZipEntry nextEntry;		
			try {
				while((nextEntry= zis.getNextEntry())!=null){
					ByteArrayOutputStream baos = ZipExtractor.readFully(zis);
					getHelpFile_memo.put(nextEntry.getName(),baos.toString());
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
			//System.out.println(getHelpFile_memo);
		}
		
		return getHelpFile_memo.get(string);
	}
	public boolean modalPanelIsOccupied(){
		if (getModalPanel()==null){ return false;}
		if (getModalPanel().getComponentCount() > 0){
			return true;
		}
		return false;
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
	private void fixAWT(){
		if(modalPanel!=null && PreviewSeqs!=null){
			if (modalPanel.getComponentCount()>0){
				//PreviewSeqs.setPreferredSize(new Dimension(0,0));
				PreviewSeqs.setVisible(false);
			} else {
				PreviewSeqs.setVisible(true);
			}
		}
	}
	public void invalidate(){
		//Good time as any.
		su.pushSizes(getWidth(), getHeight());
		fixAWT();
		for(Runnable q : modalScale){
			q.run();
		}
		super.invalidate();
	}

	private JTextArea DomainDef, MolDef, ErrorsOutText;
	private String Molecules_CLine="";
	private int Molecules_CLine_num;
	private MoleculePreview PreviewSeqs;
	private DesignerVisualGraph PreviewGraph;
	private JPanel PreviewSeqsProxy;
	private CircDesigNAConfig config;
	private CircDesigNA_Context context;
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
		return config.customCodonTable;
	}
	public void updateCodonTable(String text) {
		//Test it first
		new CodonCode(text, config);
		config.customCodonTable = text;
	}
	
	public String getCurrentBannedWordsList() {
		return config.bannedPatternsList;
	}
	public void updateBannedWordsList(String text){
		//Test it first
		new BannedPatterns(text, config);
		config.bannedPatternsList = text;
	}

	//Cursor update!
	public void caretUpdate(CaretEvent e) {
		//System.out.println(wise+" "+lines[countLine]);
		if (e.getSource()==MolDef){
			int wise = e.getDot();
			String[] lines = ((JTextComponent)e.getSource()).getText().split("\n");
			int countLine = 0, countLineTotal = 0;
			String q = "";
			while(countLine < lines.length){
				String newLine = lines[countLine];
				countLineTotal += newLine.length()+1;
				if (wise < countLineTotal){
					q = newLine;
					break;
				}
				countLine++;
			}
			if (q.trim().length()>0){
				Molecules_CLine_num = countLine;
				Molecules_CLine = q;
			}
		}
		updatePreview();
	}
	
	//Called when the caret updates (the user enters input text)
	private void updatePreview(){
		try {
			int numMolecules = MolDef.getLineCount();
			PreviewSeqs.setCurrentPreviewMolecule(Molecules_CLine_num, numMolecules, Molecules_CLine.trim(), DomainDef.getText(), true);
		} catch (InvalidDNAMoleculeException e){
			reportError(e.getMessage(), MolDef);
		} catch (InvalidDomainDefsException e){
			reportError(e.getMessage(), DomainDef);
		} catch (UpdateSuccessfulException e){
			reportError(null, null);
		} catch (Throwable e){
			reportError(e.getMessage(), null);
			e.printStackTrace();
		}
	}
	
//Error reporting
	//Handles the blinking that tells the user where an error came from.
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
	//Reports an error, blaming a certain component.
	private void reportError(String error, JComponent whichTarget){
		if (error==null){
			ErrorsOutText.setText("No problems here");
			CurrentlyNoError = true;
			blinker_whichBlink = null;
		} else {
			blinker_whichBlink = whichTarget;
			ErrorsOutText.setText(error);
			CurrentlyNoError = false;
		}
	}
}
