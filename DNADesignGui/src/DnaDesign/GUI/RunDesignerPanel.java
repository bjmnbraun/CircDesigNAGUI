package DnaDesign.GUI;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import DnaDesign.DDSeqDesigner;

public class RunDesignerPanel {
	public RunDesignerPanel(ModalizableComponent mc, final DDSeqDesigner cDesign, Font monoSpaceFont, final DesignerVisualGraph showGraph){
		final JTextArea outputText = new JTextArea("No output. First begin the designer, and then press the button again to show an intermediate result.");
		final JButton resumeDesigner = new JButton("Resume Designer"){
			private boolean designerRunning = false, designerFinished1 = false;
			{
				addActionListener(new ActionListener(){
					public void actionPerformed(
							ActionEvent e) {
						try{
							if (!designerRunning){
								designerRunning = true;
								new Thread(){
									public void run(){
										cDesign.resume();
										while(!cDesign.isFinished() && cDesign.isRunning()){
											try {
												Thread.sleep(100);
											} catch (InterruptedException e) {
												e.printStackTrace();
											}
										}
										if (cDesign.isFinished()){
											setText("Designer Finished");
											outputText.setText(cDesign.getResult());
											designerRunning = false;
											designerFinished1 = true;
											repaint();
										}
									}
								}.start();
							} else {
								outputText.setText(cDesign.getResult());
							}
						} finally {
							if (designerRunning){
								setText("Designer Running (Click to get Intermediate Result)");
							} else {
								if (designerFinished1){
									setText("Designer Finished (Output is Below)");
								} else {
									setText("Resume Designer");
								}
							}
							repaint();
						}
					}
				});
			}
		};
		JButton ToggleVisualDisplay = new JButton(){
			private boolean textToggle = false;
			private void toggleText(){
				textToggle = !textToggle;
				setText(textToggle?"View Designer Status as Text":"View Designer Status Graphically");
			}
			{
				toggleText();
				addActionListener(new ActionListener(){
					public void actionPerformed(ActionEvent e) {
						toggleText();
						showGraph.setVisible(textToggle);
						//openModalDialog.validate();
					}
				});
			}
		};
		mc.removeAllModalScale();
		outputText.setEditable(false);
		final JPanel openModalDialog = ModalUtils.openModalDialog(mc,monoSpaceFont, new Runnable(){;
			public void run(){
				cDesign.abort();
				showGraph.setVisible(false);
			}
		});
		openModalDialog.setLayout(new BorderLayout());
		JPanel buttons = new JPanel();
		buttons.setLayout(new GridLayout(2,0));
		buttons.add(resumeDesigner);
		buttons.add(ToggleVisualDisplay);
		openModalDialog.add(buttons,BorderLayout.NORTH);
		final JScrollPane holder = new JScrollPane(outputText);
		
		openModalDialog.add(holder, BorderLayout.CENTER);
		final ScaleUtils su = new ScaleUtils();
		//su.addPreferredSize(resumeDesigner, 0, 0, 200, 40);
		//su.addPreferredSize(ToggleVisualDisplay, 0, 0, 200, 40);
		su.addPreferredSize(buttons, 0, 0, 200, 80);
		su.addPreferredSize(holder, 1f, 1f, 0, -50);
		//su.addPreferredSize(outputText, 1f, 1f, 0, -50);
		
		mc.addModalScale(new Runnable(){
			public void run() {
				int w = openModalDialog.getPreferredSize().width;
				int h = openModalDialog.getPreferredSize().height;
				su.pushSizes(w, h);

				Point location = holder.getLocation();
				Component nextParent = holder.getParent();
				for(int i = 0; i < 3; i++){
					if (nextParent!=null){
						location.translate(nextParent.getLocation().x,nextParent.getLocation().y);
						nextParent = nextParent.getParent();
						//System.out.println(location);
					}
				}			
				showGraph.setBounds(location.x,location.y,holder.getWidth(),holder.getHeight());
				//showGraph.setLocation(location.x,location.y);
				//showGraph.setPreferredSize(new Dimension(holder.getWidth(),holder.getHeight()));
				openModalDialog.validate();
			}
		});
		showGraph.setDesigner(cDesign);
		showGraph.setVisible(true);
	}
}
