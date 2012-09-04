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
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.OverlayLayout;

import circdesigna.CircDesigNAOptions;
import circdesigna.SequenceDesigner;
import circdesigna.SequenceDesigner.SeqDesignerOption;



/**
 * The panel displayed when the designer is actively working on a design problem.
 * @deprecated
 */
public class RunDesignerPanel {
	private ThemedApplet mc;
	public JButton skinButton(JButton jb){
		ButtonSkin.process(mc, jb);
		return jb;
	}
	public RunDesignerPanel(ThemedApplet mc, final SequenceDesigner cDesign, Font monoSpaceFont, final DesignerVisualGraph showGraph){
		this.mc = mc;
		
		final DnaDesignOutputPanel showText = new DnaDesignOutputPanel(cDesign);
		
		final JButton actionOnRunningDesigner = new JButton();
		
		final DnaDesignOptionsPanel showOptions = new DnaDesignOptionsPanel(cDesign);
		showOptions.setBackground(mc.THEMECOL1);
		
		
		final JButton resumeDesigner = skinButton(new JButton("Resume Designer"){
			private boolean designerRunning = false, designerFinished1 = false;
			private void setDesignerRunning(double i) {
				String append = "Designer Running (Click to get Intermediate Result.)";
				if(cDesign.getCurrentIteration()>0){
					append += "Score: "+String.format("%.2f",i)+" Iteration: "+cDesign.getCurrentIteration();
				}
				setText(append);
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
									for(ActionListener al : actionOnRunningDesigner.getListeners(ActionListener.class)){
										al.actionPerformed(null);
									}
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
											setText("Designer halted. See text output.");
										} else {
											setText("Designer Finished");
										}
										showText.fetchResults();
										designerFinished1 = true;
										repaint();
									}
								}
							}.start();
						} else {
							//Get intermediate result.
							showText.fetchResults();
						}
						repaint();
					}
				});
			}		
		});
		
		final JPanel openModalDialog = ModalUtils.openModalDialog(mc,monoSpaceFont, new Runnable(){;
			public void run(){
				//Run on closing
				cDesign.abort();
				showGraph.setVisible(false);
			}
		});
		
		JPanel DisplayTabs = new JPanel();
		DisplayTabs.setLayout(new GridLayout(0,3));
		JButton GoToText,GoToGraphDisplay,GoToOptions;
		final JButton[] allThree = new JButton[3];
		DisplayTabs.add(allThree[0] = GoToText = skinButton(new JButton("Text Output"){
			{
				addActionListener(new ActionListener(){
					public void actionPerformed(ActionEvent e) {
						showGraph.setVisible(false);
						showText.setVisible(true);
						showOptions.setVisible(false);
						enableAllBut(allThree,0);
						openModalDialog.invalidate();
						for(ActionListener al : actionOnRunningDesigner.getListeners(ActionListener.class)){
							al.actionPerformed(null);
						}
					}
				});
			}
		}));
		DisplayTabs.add(allThree[1] = GoToGraphDisplay = skinButton(new JButton("Graph View"){
			{
				addActionListener(new ActionListener(){
					public void actionPerformed(ActionEvent e) {
						showGraph.setVisible(true);
						showOptions.setVisible(false);
						showText.setVisible(false);
						enableAllBut(allThree,1);
						openModalDialog.invalidate();
						for(ActionListener al : actionOnRunningDesigner.getListeners(ActionListener.class)){
							al.actionPerformed(null);
						}
					}
				});
			}
		}));
		DisplayTabs.add(allThree[2] = GoToOptions = skinButton(new JButton("Options..."){
			{
				addActionListener(new ActionListener(){
					public void actionPerformed(ActionEvent e) {
						showGraph.setVisible(false);
						showOptions.setVisible(true);
						showText.setVisible(false);
						enableAllBut(allThree,2);
						openModalDialog.invalidate();
						for(ActionListener al : actionOnRunningDesigner.getListeners(ActionListener.class)){
							al.actionPerformed(null);
						}
					}
				});
			}
		}));
		
		openModalDialog.setLayout(new BorderLayout());
		JPanel buttons = new JPanel();
		buttons.setLayout(new GridLayout(2,0));
		buttons.add(resumeDesigner);
		buttons.add(DisplayTabs);
		openModalDialog.add(buttons,BorderLayout.NORTH);
		
		final JLayeredPane jlayers = new JLayeredPane();
		jlayers.setLayout(new OverlayLayout(jlayers));
		jlayers.add(showText,JLayeredPane.DEFAULT_LAYER);
		jlayers.add(showOptions,JLayeredPane.MODAL_LAYER);
		openModalDialog.add(jlayers, BorderLayout.CENTER);
		final ScaleUtils su = new ScaleUtils();
		//su.addPreferredSize(resumeDesigner, 0, 0, 200, 40);
		//su.addPreferredSize(ToggleVisualDisplay, 0, 0, 200, 40);
		su.addPreferredSize(buttons,1f,.1f,0,0);
		su.addPreferredSize(showText,1f,.9f, 0,0);
		//su.addPreferredSize(outputText, 1f, 1f, 0, -50);
		
		mc.addModalScale(new Runnable(){
			public void run() {
				int w = openModalDialog.getPreferredSize().width;
				int h = openModalDialog.getPreferredSize().height;
				su.pushSizes(w, h);

				Component model = showText;
				Point location = model.getLocation();
				Component nextParent = model.getParent();
				for(int i = 0; i < 4; i++){
					if (nextParent!=null){
						location.translate(nextParent.getLocation().x,nextParent.getLocation().y);
						nextParent = nextParent.getParent();
						//System.out.println(location);
					}
				}			
				Rectangle newBounds = new Rectangle(location.x,location.y,model.getPreferredSize().width,model.getPreferredSize().height);
				/*
				if (showGraph.getBounds().equals(newBounds)){
					
				} else {
					showGraph.setBounds(newBounds);
				}
				*/
				//Prevent a rare, but severe, AWT event feedback loop.
				if (stackOverFlowHack[0]){
					return;
				}
				stackOverFlowHack[0] = true;
				if (!(showGraph.getLocation().equals(location))){
					showGraph.setLocation(location.x,location.y);
				}
				Dimension newSize = new Dimension(model.getPreferredSize().width-1,model.getPreferredSize().height-8);
				if (!(showGraph.getPreferredSize().equals(newSize))){
					showGraph.setPreferredSize(newSize);
				}
				stackOverFlowHack[0] = false;
			}
			private boolean[] stackOverFlowHack = new boolean[]{false};
		});
		showGraph.setDesigner(cDesign);
		
		//Begin in options
		for(ActionListener q : GoToText.getActionListeners()){
			q.actionPerformed(null);
		}
		for(ActionListener q : GoToGraphDisplay.getActionListeners()){
			q.actionPerformed(null);
		}
		for(ActionListener q : GoToOptions.getActionListeners()){
			q.actionPerformed(null);
		}
	}
	private static void enableAllBut(Component[] array, int but){
		for(int k = 0; k < array.length; k++){
			array[k].setEnabled(k!=but);
		}
	}
}
