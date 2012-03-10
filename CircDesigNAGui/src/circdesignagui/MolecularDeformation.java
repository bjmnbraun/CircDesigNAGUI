package circdesignagui;

import processing.core.PApplet;
import processing.core.PConstants;
import processing.core.PMatrix3D;

public class MolecularDeformation implements PConstants{
	private PApplet g;
	public MolecularDeformation(PApplet parent){
		g = parent;
	}
	private int state;
	private double rotation;
	private float pitchandroll = PI / 16;
	private boolean enabled = true;
	public void setEnabled(boolean enabled){
		this.enabled = enabled;
	}
	public boolean isNullTransform(){
		return rotation==0;
	}
	public void reset(){
		state = 0;
	}
	public void twist(double theta){
		rotation = Math.max(0,theta+rotation);
	}
	public void run(int ticks){
		if (!enabled){
			return;
		}
		if (ticks < 0){
			state += ticks;
		}
		/*
		if (state == 0){
			g.rotateZ((float)(rotation*Math.signum(ticks)));
		}
		*/
		float newtheta = (float)(rotation - state * pitchandroll); 
		if (newtheta <= 0){
			//Do nothing.
		} else {
			g.rotateX(pitchandroll*ticks/(1+PApplet.exp(-newtheta)));
			g.rotateY(pitchandroll*ticks/(1+PApplet.exp(-newtheta)));
		}
		if (ticks > 0){
			state += ticks;
		}
	}
	public void unrotate() {
		if (!enabled){
			return;
		}
		
		float scale = PApplet.sqrt(PApplet.sq(g.modelX(1,0,0)-g.modelX(0,0,0))+PApplet.sq(g.modelY(1,0,0)-g.modelY(0,0,0))+PApplet.sq(g.modelZ(1,0,0)-g.modelZ(0,0,0)));
		//Remove all deformation, but keep scaling and position.
		PMatrix3D m = (PMatrix3D)g.getMatrix();
		m.set(1,0,0,m.m03,0,1,0,m.m13,0,0,1,m.m23,m.m30,m.m31,m.m32,m.m33);
		g.setMatrix(m);
		g.scale(scale);
	}
}
