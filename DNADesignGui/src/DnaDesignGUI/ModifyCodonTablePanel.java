package DnaDesignGUI;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

public class ModifyCodonTablePanel {
	public ModifyCodonTablePanel(final DnaDesignGui mc, Font monoSpaceFont) {
		final JPanel openModalDialog = ModalUtils.openModalDialog(mc,monoSpaceFont, new Runnable(){
			public void run() {
				//Closing action
				
			}
		});
		
		final JTextArea currentCodons = new JTextArea();
		currentCodons.setFont(monoSpaceFont);
		currentCodons.setText(mc.getCurrentCodonTable());
		
		JScrollPane currentCodonsPane = new JScrollPane(currentCodons);
		
		JButton action = new JButton("Update Codons"){
			{
				addActionListener(new ActionListener(){
					public void actionPerformed(ActionEvent e) {
						try {
							mc.updateCodonTable(currentCodons.getText());
						} catch (Throwable f){
							setText("Error: "+f.getMessage());
						}
					}
				});
			}
		};
		openModalDialog.setLayout(new BorderLayout());
		openModalDialog.add(currentCodonsPane,BorderLayout.NORTH);
		openModalDialog.add(action,BorderLayout.CENTER);
		
		final ScaleUtils su = new ScaleUtils();
		su.addPreferredSize(currentCodonsPane, 1f, 1f, 0, -50);
		su.addPreferredSize(action, .5f, 0, 0, 50);
		
		mc.addModalScale(new Runnable(){
			public void run() {
				su.pushSizes(openModalDialog.getPreferredSize().width,
						openModalDialog.getPreferredSize().height);
				openModalDialog.validate();
			}
		});
		
	}
}
