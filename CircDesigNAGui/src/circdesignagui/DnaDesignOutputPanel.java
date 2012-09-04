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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import circdesigna.SequenceDesigner;
import circdesigna.SequenceDesigner.AlternativeResult;

public class DnaDesignOutputPanel extends JPanel{
	private JTextArea textArea;
	private SequenceDesigner design;
	private JScrollPane showText;
	private boolean setupResults = false;
	private JComboBox resultSelector;
	private AlternativeResult[] alternativeResults;
	
	public DnaDesignOutputPanel(SequenceDesigner design){
		this.design = design;
		textArea = new JTextArea("No output. Press \"Get Results\" to show an intermediate result.");
		textArea.setEditable(false);
		showText = new JScrollPane(textArea);
		setLayout(new BorderLayout());
		
		add(showText, BorderLayout.CENTER);
		
		resultSelector = new JComboBox();
		resultSelector.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				updateSelection();
			}
		});
		resultSelector.setMaximumRowCount(5);
		
		JButton updateResultsButton = new JButton("Get results");
		updateResultsButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				fetchResults();
			}
		});
		
		Box resultSelectorPane = Box.createHorizontalBox();
		resultSelectorPane.add(updateResultsButton);
		resultSelectorPane.add(Box.createHorizontalGlue());
		resultSelectorPane.add(resultSelector);
		add(resultSelectorPane,BorderLayout.NORTH);
	}
	
	private void updateSelection(){
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
		} else {
			textArea.setText(design.getResult());
		}
		textArea.setCaretPosition(0); //scroll to top of text
	}

	public void fetchResults() {
		AlternativeResult[] alternativeResults = design.getAlternativeResults();
		if (this.alternativeResults!=alternativeResults){
			//Changed? Update the text.
			this.alternativeResults = alternativeResults;
			updateSelection();
		}
	}
}
