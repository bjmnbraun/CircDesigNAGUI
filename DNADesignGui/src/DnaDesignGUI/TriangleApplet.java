package DnaDesignGUI;

import processing.core.PApplet;

public class TriangleApplet extends PApplet{
	private DnaDesignGUI_ThemedApplet mc;
	public TriangleApplet(DnaDesignGUI_ThemedApplet mc){
		this.mc = mc;
	}
	public void setup(){
		size(100, 100, P3D);
	}
	public void draw(){
		background(255);
		pushMatrix();
		stroke(mc.THEMECOL4.getRGB());
		noFill();
		scale(width,height);
		beginShape();
		vertex(0,0);
		vertex(1,0);
		vertex(0,1);
		endShape();
		popMatrix();
	}
}
