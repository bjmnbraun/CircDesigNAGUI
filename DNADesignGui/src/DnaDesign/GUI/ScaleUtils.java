package DnaDesign.GUI;

import java.awt.Component;
import java.awt.Dimension;
import java.util.ArrayList;

/**
 * Utils for creating scalable layouts.
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
			preferredSizesKey.get(k).setPreferredSize(new Dimension(xWise,yWise));
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
