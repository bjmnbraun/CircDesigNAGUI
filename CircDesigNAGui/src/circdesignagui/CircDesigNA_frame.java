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

import javax.swing.JFrame;

/**
 * For running the web applet in a local window (i.e., not in a web browser)
 * Pass in the width and height of the window you want to the applet to run in.
 * 
 * You may also want to specify the theme colors, as the webpage does, with VM arguments
 * -Dthemecol0=FF0000 -Dthemecol1=0020D0 etc.
 * 
 * @see Color.decode
 */
public class CircDesigNA_frame extends JFrame{
	public static void main(String[] args){
		int w = 720;
		int h = 480;
		if (args.length >= 2){
			w = new Integer(args[0]);
			h = new Integer(args[1]);
		}
		new CircDesigNA_frame(w,h);
	}
	public CircDesigNA_frame(int w, int h){
		setSize(w,h);
		addComponents();
	}
	private void addComponents() {
		setLayout(new BorderLayout());
		//Emulate applet behavior in a frame
		final CircDesigNA_Context comp = new CircDesigNA_Context(){
			public String getParameter(String key){
				return System.getProperty(key);
			}
		};
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		add(comp);
		comp.init();
		setVisible(true);
		comp.start();
	}
}
