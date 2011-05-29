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

import java.applet.Applet;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;

/**
 * Handles the customizable color-scheme. This is controlled by the running webpage.
 */
public abstract class ThemedApplet extends Applet implements ModalizableComponent{
	public Color THEMECOL0,THEMECOL1,THEMECOL2,THEMECOL3,THEMECOL4;
	public void parseThemeColors(){
		THEMECOL0 = parseColorFromParam("themecol0");
		THEMECOL1 = parseColorFromParam("themecol1");
		THEMECOL2 = parseColorFromParam("themecol2");
		THEMECOL3 = parseColorFromParam("themecol3");
		THEMECOL4 = parseColorFromParam("themecol4");
	}

	public JPanel skinPanel(JPanel panel) {
		panel.setBackground(getBackground());
		return panel;
	}
	public JComponent skinGroup(final Component inner, String string) {
		JComponent inner2 = new JPanel(){
			{
				setOpaque(false);
				setLayout(new BorderLayout());
				add(inner, BorderLayout.CENTER);
			}
		    protected void paintComponent(Graphics g) {
		        int width = getWidth();
		        int height = getHeight();

				((Graphics2D)g).setRenderingHint(RenderingHints.KEY_ANTIALIASING, // Anti-alias!
				        RenderingHints.VALUE_ANTIALIAS_ON);
		        // Paint a rounded rectangle in the background.
		        g.setColor(Color.white);
		        int round = 15;
		        int topY = 1;
		        g.fillRoundRect(1, topY, width-3, height-topY-1, round,round);
		        g.setColor(THEMECOL4);
		        g.drawRoundRect(1, topY, width-3, height-topY-1, round,round);
		        super.paintComponent(g);
		    }
		};
		inner2.setBorder(BorderFactory.createTitledBorder(
				BorderFactory.createEmptyBorder(),
				string,
				TitledBorder.LEFT,
				TitledBorder.BELOW_TOP));
		return inner2;
	}

	private Color parseColorFromParam(String string) {
		try {
			string = getParameter(string);
		} catch (Throwable e){
			//e.printStackTrace();
			return Color.black;
		}
		if (string==null){
			return Color.black;
		}
		return Color.decode(string);
	}
}
