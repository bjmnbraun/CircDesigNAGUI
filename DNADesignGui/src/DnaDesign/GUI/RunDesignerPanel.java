package DnaDesign.GUI;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import DnaDesign.DDSeqDesigner;

public class RunDesignerPanel {
	public RunDesignerPanel(ModalizableComponent mc, final DDSeqDesigner cDesign, Font monoSpaceFont){
		final JTextArea outputText = new JTextArea("New Designer Created.");
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
		mc.removeAllModalScale();
		outputText.setEditable(false);
		final JPanel openModalDialog = ModalUtils.openModalDialog(mc,monoSpaceFont, new Runnable(){;
			public void run(){
				cDesign.abort();
			}
		});
		openModalDialog.setLayout(new BorderLayout());
		openModalDialog.add(resumeDesigner, BorderLayout.NORTH);
		JScrollPane holder = new JScrollPane(outputText);
		openModalDialog.add(holder, BorderLayout.CENTER);
		final ScaleUtils su = new ScaleUtils();
		su.addPreferredSize(resumeDesigner, 0, 0, 200, 40);
		su.addPreferredSize(holder, 1f, 1f, 0, -50);
		su.addPreferredSize(outputText, 1f, 1f, 0, -50);
		mc.addModalScale(new Runnable(){
			public void run() {
				su.pushSizes(openModalDialog.getPreferredSize().width, openModalDialog.getPreferredSize().height);
				openModalDialog.validate();
			}
		});
	}
}
