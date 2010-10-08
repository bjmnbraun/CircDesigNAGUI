package DnaDesignGUI;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.OverlayLayout;

import DnaDesign.DDSeqDesigner;

public class RunDesignerPanel {
	
	public RunDesignerPanel(ModalizableComponent mc, final DDSeqDesigner cDesign, Font monoSpaceFont, final DesignerVisualGraph showGraph){
		final JTextArea outputText = new JTextArea("No output. First begin the designer, and then press the button again to show an intermediate result.");
		final JButton resumeDesigner = new JButton("Resume Designer"){
			private boolean designerRunning = false, designerFinished1 = false;
			private void setDesignerRunning(double i) {
				setText("Designer Running (Click to get Intermediate Result.) Score: "+String.format("%.2f",i));
			}
			{
				addActionListener(new ActionListener(){
					public void actionPerformed(
							ActionEvent e) {
						if (!designerRunning){
							designerRunning = true;
							new Thread(){
								public void run(){
									cDesign.resume();
									while(!cDesign.isFinished() && cDesign.isRunning()){
										setDesignerRunning(cDesign.getBestScore());
										try {
											Thread.sleep(100);
										} catch (InterruptedException e) {
											e.printStackTrace();
										}
									}
									if (cDesign.isFinished()){
										if (cDesign.isEndConditionError()){
											setText("Designer Failed. See text output.");
										} else {
											setText("Designer Finished");
										}
										outputText.setText(cDesign.getResult());
										designerFinished1 = true;
										repaint();
									}
								}
							}.start();
						} else {
							//Get intermediate result.
							outputText.setText(cDesign.getResult());
						}
						repaint();
					}
				});
			}		
		};
		
		final JPanel openModalDialog = ModalUtils.openModalDialog(mc,monoSpaceFont, new Runnable(){;
		public void run(){
			cDesign.abort();
			showGraph.setVisible(false);
		}
		});
		
		JPanel DisplayTabs = new JPanel();
		DisplayTabs.setLayout(new GridLayout(0,3));
		JButton GoToVisualDisplay,GoToGraphDisplay,GoToOptions;
		final JButton[] allThree = new JButton[3];
		DisplayTabs.add(allThree[0] = GoToVisualDisplay = new JButton("Text Output"){
			{
				addActionListener(new ActionListener(){
					public void actionPerformed(ActionEvent e) {
						showGraph.setVisible(false);
						enableAllBut(allThree,0);
						openModalDialog.validate();
					}
				});
			}
		});
		DisplayTabs.add(allThree[1] = GoToGraphDisplay = new JButton("Graph View"){
			{
				addActionListener(new ActionListener(){
					public void actionPerformed(ActionEvent e) {
						showGraph.setVisible(true);
						enableAllBut(allThree,1);
						openModalDialog.validate();
					}
				});
			}
		});
		DisplayTabs.add(allThree[2] = GoToOptions = new JButton("Options..."){
			{
				addActionListener(new ActionListener(){
					public void actionPerformed(ActionEvent e) {
						showGraph.setVisible(false);
						enableAllBut(allThree,2);
						openModalDialog.validate();
					}
				});
			}
		});
		outputText.setEditable(false);
		
		openModalDialog.setLayout(new BorderLayout());
		JPanel buttons = new JPanel();
		buttons.setLayout(new GridLayout(2,0));
		buttons.add(resumeDesigner);
		buttons.add(DisplayTabs);
		openModalDialog.add(buttons,BorderLayout.NORTH);
		final JScrollPane holder = new JScrollPane(outputText);
		
		final JLayeredPane jlayers = new JLayeredPane();
		jlayers.setLayout(new OverlayLayout(jlayers));
		jlayers.add(holder,JLayeredPane.DEFAULT_LAYER);
		openModalDialog.add(jlayers, BorderLayout.CENTER);
		final ScaleUtils su = new ScaleUtils();
		//su.addPreferredSize(resumeDesigner, 0, 0, 200, 40);
		//su.addPreferredSize(ToggleVisualDisplay, 0, 0, 200, 40);
		su.addPreferredSize(buttons,1f,.1f,0,0);
		su.addPreferredSize(holder,1f,.9f, 0,0);
		//su.addPreferredSize(outputText, 1f, 1f, 0, -50);
		
		mc.addModalScale(new Runnable(){
			public void run() {
				int w = openModalDialog.getPreferredSize().width;
				int h = openModalDialog.getPreferredSize().height;
				su.pushSizes(w, h);

				Component model = holder;
				Point location = model.getLocation();
				Component nextParent = model.getParent();
				for(int i = 0; i < 4; i++){
					if (nextParent!=null){
						location.translate(nextParent.getLocation().x,nextParent.getLocation().y);
						nextParent = nextParent.getParent();
						//System.out.println(location);
					}
				}			
				showGraph.setBounds(location.x,location.y,model.getWidth(),model.getHeight());
				//showGraph.setLocation(location.x,location.y);
				//showGraph.setPreferredSize(new Dimension(holder.getWidth(),holder.getHeight()));
				openModalDialog.validate();
			}
		});
		showGraph.setDesigner(cDesign);

		//Begin in graph view
		for(ActionListener q : GoToGraphDisplay.getActionListeners()){
			q.actionPerformed(null);
		}
	}
	private static void enableAllBut(Component[] array, int but){
		for(int k = 0; k < array.length; k++){
			array[k].setEnabled(k!=but);
		}
	}
}
