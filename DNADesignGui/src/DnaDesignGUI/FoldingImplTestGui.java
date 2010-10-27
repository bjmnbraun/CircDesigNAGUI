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

import DnaDesign.DesignIntermediateReporter;
import DnaDesign.DnaDefinition;
import DnaDesign.DomainDesigner;
import DnaDesign.DomainDesigner_SharedUtils;
import DnaDesign.DomainSequence;
import DnaDesign.DomainStructureData;
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
	private StructurePenaltyTriangle triangleApplet;
	private JPanel triangleAppletProxy;
	private JList possibleViews;
	private int[][] domain_sequences;
	
	private DefaultListModel possibleViews_model;
	private JTextArea domainDefs,errText;
	private JTextField moleculeInput1, moleculeInput2;
	private String molAtextLatch = "", molBtextLatch = "", domainDefsTextLatch = "";


	private void updateViewTriangle() {
		PenaltyObject cur = (PenaltyObject)possibleViews.getSelectedValue();
		if (cur!=null && domain_sequences!=null){
			int[][] nullMarkings = new int[domain_sequences.length][];
			for(int k = 0; k < nullMarkings.length; k++){
				nullMarkings[k] = new int[domain_sequences[k].length];
			}
			triangleApplet.setPenalty(cur.sp, domain_sequences, nullMarkings);
		}
	}
	
	private void runStartRoutine() {
		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new BorderLayout());
		JPanel leftPanel = skinPanel(new JPanel());
		final CaretListener cl = new CaretListener(){
			public void caretUpdate(CaretEvent e) {
				updatepossibleViews();
			}
		};
		float leftProportion = .4f;
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
			triangleApplet = new StructurePenaltyTriangle(this){
				public void draw(){
					triangleApplet.setSize(new Dimension(triangleAppletProxy.getPreferredSize().width -16, triangleAppletProxy.getPreferredSize().height -16));
					super.draw();
				}
			};
			triangleAppletProxy.setBorder(BorderFactory.createLineBorder(Color.black, 8));
			
			JComponent triangleproxyGroup = skinGroup(triangleAppletProxy, "Structure analysis, with MFE labeled");
			rightPanel.add(errTexH);
			rightPanel.add(triangleproxyGroup);
			su.addPreferredSize(errText, 1f-leftProportion, .2f, -9, -9);
			su.addPreferredSize(errTexH, 1f-leftProportion, .2f);
			su.addPreferredSize(triangleAppletProxy, 1f-leftProportion, 0, -8, 0, 1f);
			su.addPreferredSize(triangleproxyGroup, 1f-leftProportion, 0, 0, 18, 1f);
			su.addPreferredSize(rightPanel, 1-leftProportion, 1f);	
		}
		mainPanel.add(leftPanel, BorderLayout.WEST);
		mainPanel.add(rightPanel, BorderLayout.EAST);
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
			DomainDesignerImpl ddi = new DomainDesignerImpl(new FoldingImpl());
			DomainStructureData dsd = new DomainStructureData();
			DomainStructureData.readDomainDefs(domainDefs.getText(), dsd);
			ArrayList<DomainSequence> SingleStrandedRegions = new ArrayList();
			ArrayList<DomainSequence> HairpinInnards = new ArrayList();
			ArrayList<DomainSequence[]> HairpinOpenings = new ArrayList();
			for(int whichMolecule = 0; whichMolecule <= 1; whichMolecule++){
				if (whichMolecule==0){
					DomainStructureData.readStructure("Molecule A", last(moleculeInput1.getText().trim().split("\\s+")), dsd);
				} else {
					DomainStructureData.readStructure("Molecule B", last(moleculeInput2.getText().trim().split("\\s+")), dsd);
				}
				DomainDesigner_SharedUtils.utilSingleStrandedFinder(dsd, SingleStrandedRegions);
				DomainDesigner_SharedUtils.utilHairpinInternalsFinder(dsd, HairpinInnards);
				DomainDesigner_SharedUtils.utilHairpinClosingFinder(dsd, HairpinOpenings);
			}
			DomainDesigner_SharedUtils.utilRemoveDuplicateSequences(SingleStrandedRegions);
			DomainDesigner_SharedUtils.utilRemoveDuplicateSequences(HairpinInnards);
			DesignIntermediateReporter dir = new DesignIntermediateReporter();
			
			List<ScorePenalty> listPenalties = ddi.listPenalties(SingleStrandedRegions, HairpinInnards, HairpinOpenings, dir);
			for(ScorePenalty sp : listPenalties){
				penalties.add(new PenaltyObject(sp, dsd));	
			}
			//Make domain array
			int[] domainLengths = dsd.domainLengths;
			int[][] domain = new int[domainLengths.length][];
			for(int i = 0; i < domain.length; i++){
				domain[i] = new int[domainLengths[i]];
				String constraint = dsd.getConstraint(i);
				for(int j = 0; j < domain[i].length; j++){
					domain[i][j] = DomainDesigner.decodeConstraintChar(constraint.charAt(j));
					if (DnaDefinition.noFlags(domain[i][j])==DnaDefinition.NOBASE){
						throw new RuntimeException("Invalid sequence given for "+dsd.getDomainName(i));
					}
				}
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
			DomainSequence[] seqs = sp.getSeqs();
			myString = sp.getClass().getSimpleName();
			if (seqs.length>0){
				myString += " : ";
				for(int k = 0; k < seqs.length; k++){
					myString += seqs[k].toString(dsd);
					if (k + 1 < seqs.length){
						myString += " vs ";
					}
				}
			} else {
				myString += " (On all molecules)";
			}
			this.sp = sp;
		}
		public String toString(){
			return myString;
		}
	}
	public void invalidate(){
		//Good time as any.
		su.pushSizes(getWidth(), getHeight());
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
