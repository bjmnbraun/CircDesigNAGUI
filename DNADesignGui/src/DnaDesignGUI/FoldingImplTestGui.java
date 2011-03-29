package DnaDesignGUI;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Point;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.OverlayLayout;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import DnaDesign.AbstractDomainDesignTarget;
import DnaDesign.DesignIntermediateReporter;
import DnaDesign.DesignerOptions;
import DnaDesign.DomainDesigner;
import DnaDesign.DomainPolymerGraph;
import DnaDesign.DomainSequence;
import DnaDesign.DomainStructureData;
import DnaDesign.AbstractPolymer.DnaDefinition;
import DnaDesign.Config.CircDesigNAConfig;
import DnaDesign.DomainDesigner.ScorePenalty;
import DnaDesign.impl.DomainDesignerImpl;
import DnaDesign.impl.FoldingImpl;


public class FoldingImplTestGui extends DnaDesignGUI_ThemedApplet{
	ScaleUtils su;
	public FoldingImplTestGui(){
		su = new ScaleUtils();
	}
	boolean started = false;
	public void start(){
		if (started){
			return;
		}
		started = true;
		parseThemeColors();
		setBackground(Color.white);
		new Thread(){
			public void run(){
				runStartRoutine();
			}
		}.start();
	}
	private CircDesigNAConfig config;
	private FoldingImpl fil;
	private StructurePenaltyTriangle triangleApplet;
	private JPanel triangleAppletProxy;
	private JList possibleViews;
	private int[][] domain_sequences;
	
	private DefaultListModel possibleViews_model;
	private JTextArea domainDefs,errText,penaltyScore;
	private JTextField moleculeInput1, moleculeInput2;
	private String molAtextLatch = "", molBtextLatch = "", domainDefsTextLatch = "";


	private void updateViewTriangle() {
		PenaltyObject cur = (PenaltyObject)possibleViews.getSelectedValue();
		if (cur!=null && domain_sequences!=null){
			int[][] nullMarkings = new int[domain_sequences.length][];
			for(int k = 0; k < nullMarkings.length; k++){
				nullMarkings[k] = new int[domain_sequences[k].length];
			}
			triangleApplet.setPenalty(cur.sp, domain_sequences, nullMarkings, fil);
			double score = triangleApplet.getEvalScore();
			penaltyScore.setText(String.format("%.3f",score));
		}
	}
	
	private void runStartRoutine() {
		//Logic
		config = new CircDesigNAConfig();
		
		
		//Gui
		
		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new BorderLayout());
		JPanel leftPanel = skinPanel(new JPanel());
		final CaretListener cl = new CaretListener(){
			public void caretUpdate(CaretEvent e) {
				updatepossibleViews();
			}
		};
		float leftProportion = .5f;
		{
			domainDefs = new JTextArea("");
			JScrollPane domainDefsScroll = new JScrollPane(domainDefs);
			moleculeInput1 = new JTextField("");
			moleculeInput2 = new JTextField("");
			leftPanel.setLayout(new FlowLayout());
			final JComponent domainDefsHolder = skinGroup(domainDefsScroll, "Domain Definition");
			leftPanel.add(domainDefsHolder);
			final JComponent molIn1Holder = skinGroup(moleculeInput1, "Molecule A Input");
			leftPanel.add(molIn1Holder);
			final JComponent molIn2Holder = skinGroup(moleculeInput2, "Molecule B Input");
			leftPanel.add(molIn2Holder);
			su.addPreferredSize(leftPanel, leftProportion, 1f);
			int texHeight = 50;
			possibleViews = new JList();
			possibleViews.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			//possibleViews.setBorder(BorderFactory.createEtchedBorder());
			possibleViews.addListSelectionListener(new ListSelectionListener(){
				public void valueChanged(ListSelectionEvent e) {
					updateViewTriangle();
				}
			});
			JScrollPane possibleViewScroll = new JScrollPane(possibleViews);
			possibleViews_model = new DefaultListModel();
			JComponent possibleViewsInHolder = skinGroup(possibleViewScroll, "Computed Constraints");
			leftPanel.add(possibleViewsInHolder);
			float sumH = 0f;
			int sumHD = 0;
			su.addPreferredSize(domainDefsHolder, leftProportion, .3f, 0, -18); sumH += .3f; 
			su.addPreferredSize(domainDefsScroll, leftProportion, .3f, -9, -18);
			su.addPreferredSize(molIn1Holder, leftProportion, 0, 0, texHeight); sumHD+= texHeight;
			su.addPreferredSize(molIn2Holder, leftProportion, 0, 0, texHeight); sumHD+= texHeight;
			su.addPreferredSize(moleculeInput1, leftProportion, 0, -9, texHeight*2/3);
			su.addPreferredSize(moleculeInput2, leftProportion, 0, -9, texHeight*2/3);
			su.addPreferredSize(possibleViewScroll, leftProportion, 1f-sumH, -9, -sumHD - 18);
			su.addPreferredSize(possibleViewsInHolder, leftProportion, 1f-sumH, 0, -sumHD - 18);
			updatepossibleViews();
			moleculeInput1.addCaretListener(cl);
			moleculeInput2.addCaretListener(cl);
			domainDefs.addCaretListener(cl);
		}
		JPanel rightPanel = skinPanel(new JPanel());
		{
			errText = new JTextArea("");
			errText.setEditable(false);
			errText.setBorder(BorderFactory.createEtchedBorder());
			JComponent errTexH = skinGroup(errText, "Errors");
			
			penaltyScore = new JTextArea("");
			penaltyScore.setEditable(false);
			penaltyScore.setBorder(BorderFactory.createEtchedBorder());
			JComponent penaltyScoreH = skinGroup(penaltyScore, "Penalty Evaluation");
			
			triangleAppletProxy = skinPanel(new JPanel(){
				public void setPreferredSize(Dimension dim){
					super.setPreferredSize(dim);
					Point loc = getLocation();
					loc.translate(8, 8);
					Container parent = getParent();
					for(int k = 0; k < 3; k++){
						loc.translate(parent.getLocation().x,parent.getLocation().y);
						parent = parent.getParent();
					}
					triangleApplet.setLocation(loc);
				}
			});
			triangleApplet = new StructurePenaltyTriangle(this,config){
				public void draw(){
					triangleApplet.setSize(new Dimension(triangleAppletProxy.getPreferredSize().width -16, triangleAppletProxy.getPreferredSize().height -16));
					super.draw();
				}
			};
			triangleAppletProxy.setBorder(BorderFactory.createLineBorder(Color.black, 8));
			
			JComponent triangleproxyGroup = skinGroup(triangleAppletProxy, "Structure analysis, displaying MFE");
			rightPanel.add(errTexH);
			rightPanel.add(penaltyScoreH);
			rightPanel.add(triangleproxyGroup);
			su.addPreferredSize(errText, 1f-leftProportion, .2f, -9, -9);
			su.addPreferredSize(errTexH, 1f-leftProportion, .2f);
			su.addPreferredSize(penaltyScore, 1f-leftProportion, .1f, -9, -9);
			su.addPreferredSize(penaltyScoreH, 1f-leftProportion, .1f);
			su.addPreferredSize(triangleAppletProxy, 1f-leftProportion, 0, -8, 0, 1f);
			su.addPreferredSize(triangleproxyGroup, 1f-leftProportion, 0, 0, 18, 1f);
			su.addPreferredSize(rightPanel, 1-leftProportion, 1f);	
		}
		mainPanel.add(leftPanel, BorderLayout.WEST);
		mainPanel.add(rightPanel, BorderLayout.CENTER);
		su.addPreferredSize(mainPanel, 1f, 1f);
		setLayout(new OverlayLayout(this));
		add(triangleApplet);
		add(mainPanel);
		triangleApplet.init();
		triangleApplet.start();
		validate();
	}
	private void updatepossibleViews() {
		boolean changed = !(molAtextLatch.equals(moleculeInput1.getText()) && molBtextLatch.equals(moleculeInput2.getText()) && domainDefs.getText().equals(domainDefsTextLatch));  
		if (changed){
			molAtextLatch = moleculeInput1.getText();
			molBtextLatch = moleculeInput2.getText();
			domainDefsTextLatch = domainDefs.getText();
			//Build new model
			Collection<PenaltyObject> penalties = getPenalties();
			possibleViews_model.clear();
			for(PenaltyObject q : penalties){
				possibleViews_model.addElement(q);
			}
			possibleViews.setModel(possibleViews_model);
		}
	}
	private Collection<PenaltyObject> getPenalties() {
		ArrayList<PenaltyObject> penalties = new ArrayList();
		try {
			fil = new FoldingImpl(config);
			DomainDesignerImpl ddi = new DomainDesignerImpl(fil,config);
			DomainStructureData dsd = new DomainStructureData(config);
			DomainStructureData.readDomainDefs(domainDefs.getText(), dsd);
			DomainPolymerGraph dsg = new DomainPolymerGraph(dsd);
			AbstractDomainDesignTarget target = new AbstractDomainDesignTarget(dsd,config);
			for(int whichMolecule = 0; whichMolecule <= 1; whichMolecule++){
				if (whichMolecule==0){
					target.addTargetStructure("Molecule A", last(moleculeInput1.getText().trim().split("\\s+")));
				} else {
					target.addTargetStructure("Molecule B", last(moleculeInput2.getText().trim().split("\\s+")));
				}
			}
			
			DesignIntermediateReporter dir = new DesignIntermediateReporter();
			
			//Make domain array
			int[] domainLengths = dsd.domainLengths;
			int[][] domain = new int[domainLengths.length][];
			for(int i = 0; i < domain.length; i++){
				domain[i] = new int[domainLengths[i]];
				String constraint = dsd.getConstraint(i);
				for(int j = 0; j < domain[i].length; j++){
					domain[i][j] = config.monomer.decodeConstraintChar(constraint.charAt(j));
					if (config.monomer.noFlags(domain[i][j])==DnaDefinition.NOBASE){
						throw new RuntimeException("Invalid sequence given for "+dsd.getDomainName(i));
					}
				}
			}

			List<ScorePenalty> listPenalties = ddi.listPenalties(target, dir, domain, DesignerOptions.getDefaultOptions(), dsd);
			for(ScorePenalty sp : listPenalties){
				penalties.add(new PenaltyObject(sp, dsd));	
			}
			
			domain_sequences = domain;
			errText.setText("No problems here");
		} catch (Throwable e){
			errText.setText(e.getMessage());
		}
		return penalties;
	}
	private <T> T last(T[] split) {
		return split[split.length-1];
	}
	private class PenaltyObject {
		public String myString;
		private ScorePenalty sp;
		public PenaltyObject(ScorePenalty sp, DomainStructureData dsd){
			myString = sp.toString(dsd);
			this.sp = sp;
		}
		public String toString(){
			return myString;
		}
	}
	public void invalidate(){
		//Good time as any.
		int w = getWidth();
		int h = getHeight();
		float ar = 550f/480f;
		w = (int) Math.min(w,h*ar);
		su.pushSizes(w,h);
		super.invalidate();
	}
	public void addModalScale(Runnable runnable) {
	}
	public JPanel getModalPanel() {
		return null;
	}
	public void removeAllModalScale() {
	}
}
