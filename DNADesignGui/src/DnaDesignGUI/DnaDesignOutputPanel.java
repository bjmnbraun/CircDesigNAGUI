package DnaDesignGUI;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import edu.utexas.cssb.circdesigna.SequenceDesigner;
import edu.utexas.cssb.circdesigna.SequenceDesigner.AlternativeResult;

public class DnaDesignOutputPanel extends JPanel{
	private JTextArea textArea;
	private SequenceDesigner design;
	private boolean setupResults = false;
	private JComboBox resultSelector;

	public DnaDesignOutputPanel(SequenceDesigner design){
		this.design = design;
		textArea = new JTextArea("No output. First press \"Begin Designer\", and then press the button again to show an intermediate result.");
		textArea.setEditable(false);
		JScrollPane showText = new JScrollPane(textArea);
		setLayout(new BorderLayout());
		
		add(showText, BorderLayout.CENTER);
		
		resultSelector = new JComboBox();
		resultSelector.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				updateResult();
			}
		});
		resultSelector.setMaximumRowCount(5);
		Box resultSelectorPane = Box.createHorizontalBox();
		resultSelectorPane.add(resultSelector);
		resultSelectorPane.add(Box.createHorizontalGlue());
		add(resultSelectorPane,BorderLayout.NORTH);
	}
	
	public void updateResult(){
		AlternativeResult[] alternativeResults = design.getAlternativeResults();
		if (!setupResults){
			if (alternativeResults!=null){
				resultSelector.removeAllItems();
				for(AlternativeResult q : alternativeResults){
					resultSelector.addItem(q.getDescription());
				}
				resultSelector.setSelectedIndex(0);
				setupResults = true;
			}
		}
		if (setupResults){
			textArea.setText(design.getResult(alternativeResults[resultSelector.getSelectedIndex()]));
		}
	}
}
