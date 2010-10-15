package DnaDesignGUI;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Point;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.OverlayLayout;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;


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
	private TriangleApplet triangleApplet;
	private JPanel triangleAppletProxy;
	private JList possibleViews;
	private DefaultListModel possibleViews_model;
	private JTextArea domainDefs;
	private JTextField moleculeInput1, moleculeInput2;
	private String molAtextLatch = "", molBtextLatch = "";

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
			final JComponent domainDefsHolder = skinGroup(domainDefsScroll, "Domain Sequences");
			leftPanel.add(domainDefsHolder);
			final JComponent molIn1Holder = skinGroup(moleculeInput1, "Molecule A Input");
			leftPanel.add(molIn1Holder);
			final JComponent molIn2Holder = skinGroup(moleculeInput2, "Molecule B Input");
			leftPanel.add(molIn2Holder);
			su.addPreferredSize(leftPanel, leftProportion, 1f);
			int texHeight = 50;
			possibleViews = new JList();
			//possibleViews.setBorder(BorderFactory.createEtchedBorder());
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
		}
		JPanel rightPanel = skinPanel(new JPanel());
		{
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
			triangleApplet = new TriangleApplet(this){
				public void draw(){
					triangleApplet.setSize(new Dimension(triangleAppletProxy.getPreferredSize().width -16, triangleAppletProxy.getPreferredSize().height -16));
					super.draw();
				}
			};
			triangleAppletProxy.setBorder(BorderFactory.createLineBorder(Color.black, 8));
			
			JComponent triangleproxyGroup = skinGroup(triangleAppletProxy, "Longest Common Substring Triangle");
			rightPanel.add(triangleproxyGroup);
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
		boolean changed = !(molAtextLatch.equals(moleculeInput1.getText()) && molBtextLatch.equals(moleculeInput2.getText()));  
		if (changed){
			molAtextLatch = moleculeInput1.getText();
			molBtextLatch = moleculeInput2.getText();
			//Build new model
			possibleViews_model.clear();
			possibleViews_model.add(0, molAtextLatch);
			possibleViews.setModel(possibleViews_model);
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
