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

import java.awt.Component;
import java.awt.Dimension;
import java.util.ArrayList;

/**
 * Utilities for creating scalable layouts.
 */
public class ScaleUtils {
	private ArrayList<Component> preferredSizesKey = new ArrayList();;
	private ArrayList<float[]> preferredSizes = new ArrayList();
	public void addPreferredSize(Component listen, float x, float y){
		addPreferredSize(listen, x, y, -1);
	}
	public void pushSizes(int width, int height) {
		for(int k = 0; k < preferredSizes.size(); k++){
			float[] val = preferredSizes.get(k);
			int xWise = (int)(val[0]*width+val[2]);
			int yWise = (int)(val[1]*height+val[3]);
			if (val[4]>0){
				yWise = (int) (xWise / val[4] + val[3]);
			}
			Dimension neu = new Dimension(xWise,yWise);
			preferredSizesKey.get(k).setPreferredSize(neu);
		}
	}
	public void addPreferredSize(Component listen, float x, float y, float ar) {
		addPreferredSize(listen, x, y, 0, 0, ar);
	}
	public void addPreferredSize(Component listen, float x, float y, int xoff, int yoff) {
		addPreferredSize(listen, x, y, xoff,yoff,-1);
	}
	public void addPreferredSize(Component listen, float x, float y, int xoff,
			int yoff, float ar) {
		preferredSizesKey.add(listen);
		preferredSizes.add(new float[]{x,y, xoff, yoff, ar});
	}
}

