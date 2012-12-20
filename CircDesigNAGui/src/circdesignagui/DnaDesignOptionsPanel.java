package circdesignagui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import circdesigna.CircDesigNAOptions;
import circdesigna.SequenceDesigner;
import circdesigna.SequenceDesigner.SeqDesignerOption;

public class DnaDesignOptionsPanel extends JPanel{
	public DnaDesignOptionsPanel(SequenceDesigner cDesign){
		setLayout(new BorderLayout());

		Box showOptionsBox = Box.createVerticalBox();
		
		if (cDesign.isRunning()){
			JLabel lab = new JLabel("<html><u>Options cannot be changed while the designer is running.</u></html>");
			lab.setHorizontalAlignment(SwingConstants.CENTER);
			Box horiz = Box.createHorizontalBox();
			horiz.add(lab);
			horiz.add(Box.createHorizontalGlue());
			showOptionsBox.add(horiz);
		}
		
		CircDesigNAOptions options = cDesign.getOptions();
		for(final SeqDesignerOption option : options.options){
			JLabel label = new JLabel();
			label.setText("<html>"+option.getDescription()+"</html>");
			if (option instanceof SeqDesignerOption.Boolean){
				//Add a new toggle
				final JCheckBox toggle = new JCheckBox();
				toggle.setOpaque(false);
				Box horiz = Box.createHorizontalBox();
				horiz.add(toggle);
				horiz.add(label);
				horiz.add(Box.createHorizontalGlue());
				showOptionsBox.add(horiz);
				label.setLabelFor(toggle);
				final SeqDesignerOption.Boolean bOption = (SeqDesignerOption.Boolean) option;
				toggle.setSelected(bOption.getState());
				toggle.addActionListener(new ActionListener(){
					public void actionPerformed(ActionEvent e) {
						bOption.toggle();
						toggle.setSelected(bOption.getState());
					}
				});

				toggle.setEnabled(!cDesign.isRunning());
			} else if (option instanceof SeqDesignerOption.Double || option instanceof SeqDesignerOption.Integer || option instanceof SeqDesignerOption.Str){
				final SeqDesignerOption.Double dOption;
				final SeqDesignerOption.Integer iOption;
				final SeqDesignerOption.Str sOption;
				//Add a value field
				final JTextField jta = new JTextField(12);
				if (option instanceof SeqDesignerOption.Double){
					dOption = (SeqDesignerOption.Double) option;
					jta.setText(dOption.getState()+"");
					iOption = null;
					sOption = null;
				} else if (option instanceof SeqDesignerOption.Integer){
					iOption = (SeqDesignerOption.Integer) option;
					jta.setText(iOption.getState()+"");
					dOption = null;
					sOption = null;
				} else /*if (option instanceof SeqDesignerOption.String)*/{
					sOption = (SeqDesignerOption.Str) option;
					jta.setText(sOption.getState()+"");
					dOption = null;
					iOption = null;
				}
				jta.setMaximumSize(new Dimension(150,23));
				jta.setMinimumSize(new Dimension(150,23));
				final String defaultDedicateText = "Set";
				final JButton dedicate = new JButton(defaultDedicateText);

				dedicate.setPreferredSize(new Dimension(70,23));
				dedicate.addActionListener(new ActionListener(){
					public void actionPerformed(ActionEvent f) {
						try {
							if (dOption!=null){
								dOption.setState(new Double(jta.getText()));
							} else if (iOption!=null){
								iOption.setState(new Integer(jta.getText()));
							} else if (sOption!=null){
								sOption.setState(jta.getText());
							}
						} catch (Throwable e){
							dedicate.setText("ERR.");
							new Thread(){
								public void run(){
									try {
										Thread.sleep(500);
									} catch (InterruptedException e) {
										e.printStackTrace();
									}
									dedicate.setText(defaultDedicateText);
								}
							}.start();
						}
					}
				});

				dedicate.setEnabled(!cDesign.isRunning());
				jta.setEditable(!cDesign.isRunning());

				Box horiz = Box.createHorizontalBox();
				horiz.add(jta);
				horiz.add(dedicate);
				horiz.add(label);
				horiz.add(Box.createHorizontalGlue());
				showOptionsBox.add(horiz);
			}
		}
		
		//showOptionsBox.add(Box.createVerticalGlue());
		add(showOptionsBox);

		validate();
	}
}
