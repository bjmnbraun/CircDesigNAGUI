package DnaDesignGUI;

import java.awt.geom.Rectangle2D;
import java.io.File;
import java.io.IOException;

import javax.swing.JOptionPane;
import javax.swing.JTextArea;

import processing.core.PApplet;
import processing.core.PFont;
import DnaDesign.DDSeqDesigner;
import DnaDesign.DesignIntermediateReporter;

public class DesignerVisualGraph extends PApplet{
	private DDSeqDesigner design;
	public void setup(){
		size(100,100,P3D);
		ff = createFont("Arial", 24);
	}
	private PFont ff;
	private String snapshotPath = "";
	private float strokeScale = 1f;
	private boolean thicknessIsScore = false, needsSnapshot = false;
	public void draw(){
		if (!isVisible()){
			return;
		}
		textFont(ff);
		background(255,255,255);
		drawGui();
		if (dir==null){
			return;
		}
		if (!dir.currentMatrixSynchronized){
			return;
		}
		boolean snapshotting = false;
		strokeScale = 1f;
		if (needsSnapshot){
			snapshotting = true;
			try {
				File temp = File.createTempFile("DNADesignGraph"+System.nanoTime(), ".pdf");
				beginRecord(PDF, snapshotPath = temp.getAbsolutePath());
				strokeScale = 1/100f;
			} catch (IOException e) {
				e.printStackTrace();
				snapshotting = false;
			}
		}
		textFont(ff);
		strokeWeight(strokeScale);
		drawReal();
		if (snapshotting){
			needsSnapshot = false;
			endRecord();
			System.out.println("Done.");
			JOptionPane.showMessageDialog(DesignerVisualGraph.this, new JTextArea("File saved to \n"+snapshotPath){
				{
					setEditable(false);
				}
			});
		}
	}
	private void drawGui() {
		Rectangle2D.Float toggleLineDisplay = new Rectangle2D.Float(.01f,.01f,.3f,.06f);
		int buttonY = 0;
		
		for(Runnable setupButton : new Runnable[]{
				new Runnable(){
					public void run(){
						thicknessIsScore=!thicknessIsScore;
					}
				},
				new Runnable(){
					public void run() {
						System.out.println("Snapshotting");
						needsSnapshot = true;
					}					
				},
		}){
			String string;
			switch(buttonY){
			case 0:
				string = thicknessIsScore?"View via Colorscale":"View via Thickness";
				break;
			case 1: default:
				string = "Render PDF";
				break;
			}
			pushMatrix();{
				translate(toggleLineDisplay.x*width,(toggleLineDisplay.y+toggleLineDisplay.height*buttonY)*height);
				scale(toggleLineDisplay.width*width,toggleLineDisplay.height*height);
				pushMatrix();{
					noFill();
					stroke(0);
					strokeWeight(1*strokeScale);
					rect(0,0,1,1);
				}popMatrix();
				fill(0,0,255);
				textAlign(LEFT,TOP);
				translate(.02f,.1f);
				scale(1f/30);
				scale(height/(float)width*toggleLineDisplay.height/(float)toggleLineDisplay.width,1);
				text(string,0,0);
				if (mousePressed(toggleLineDisplay,toggleLineDisplay.height*buttonY)&(System.nanoTime()-lastToggle>.3e9)){
					lastToggle = System.nanoTime();
					setupButton.run();
				}
			}popMatrix();
			buttonY++;
		}
		}
		private long lastToggle = System.nanoTime();
		private boolean mousePressed(Rectangle2D.Float m, float yoffset){
			return mousePressed&&m.contains(mouseX/(float)width,(mouseY)/(float)height-yoffset);
		}
		public void drawReal(){
			float[][] values =  dir.currentMatrix;
			if (values==null) return;
			fill(0);
			stroke(0);
			int numNodes = values.length;
			float shorterDim = min(width,height);
			scale(shorterDim,shorterDim);
			ellipseMode(CORNER);
			pushMatrix();
			translate(width/shorterDim/2,height/shorterDim/2);
			scale(.35f); //radius of circle
			float nodeSize = .05f;
			float trad = TWO_PI/numNodes;
			float maxValue = 0;
			for(int k = 0; k < numNodes; k++){
				for(int tk = 0; tk < numNodes; tk++){
					maxValue = Math.max(maxValue,values[k][tk]);
				}
			}
			for(int k = 0; k < numNodes; k++){
				position[0] = cos(k*trad);
				position[1] = sin(k*trad);
				pushMatrix();{

					moveTo(position,0);
					ellipse(-nodeSize,-nodeSize,nodeSize*2,nodeSize*2);

					float circum = .6f;
					pushMatrix();{
						rotate(k*trad);
						//Arrow to self:
						int interval = 20;
						rotate(-HALF_PI);
						if (scoreStrokeSettings(k,k,maxValue,values)){
							for(int c = 0; c < interval-2; c++){
								line(0,0,circum/interval,0);
								translate(circum/interval,0);
								rotate(TWO_PI/interval);
							}
							line(0,0,-.03f,-.03f);
							line(0,0,-.03f,.03f);
						}
					}popMatrix();

					pushMatrix();{
						float outTheta = k*trad-1f;
						rotate(outTheta);
						float textSize= circum/TWO_PI;
						float outSize = .2f;
						//line(0,.1f,0,outSize-textSize);
						translate(0,outSize);
						rotate(-outTheta);
						fill(0,0,0,40);
						//ellipse(-textSize,-textSize,textSize*2,textSize*2);
						fill(0);
						scale(1f/160);
						int leftAlign = CENTER, topAlign = CENTER;
						if (position[0] < 0){
							leftAlign = RIGHT;
						} else if (position[0] > 0){
							leftAlign = LEFT;
						}
						if (position[1] < 0){
							topAlign = BOTTOM;
						} else if (position[1] > 0){
							topAlign = TOP;
						}
						textAlign(leftAlign,topAlign);
						text(dir.getMolecule(k),0,0);
					}popMatrix();

				}popMatrix();

				for(int tk = 0; tk < numNodes; tk++){
					if (tk==k) continue;
					if (scoreStrokeSettings(tk,k,maxValue,values)){
						position[2] = cos(tk*trad);
						position[3] = sin(tk*trad);
						line(position[0],position[1],position[2],position[3]);
					}
				}
				strokeWeight(1*strokeScale);
			}
			popMatrix();
		}
		float ENDTHRESHOLD = 0;
		private boolean scoreStrokeSettings(int tk, int k, float maxValue, float[][] values) {
			float value = values[tk][k];
			if (thicknessIsScore){
				if (value > ENDTHRESHOLD){
					stroke(0,0,0);
					value /= maxValue;
					strokeWeight((value+1)*strokeScale);
					return true;
				}
				return false;
			} else{
				strokeWeight(2*strokeScale);
				if (value > ENDTHRESHOLD){
					value /= maxValue;
					stroke(lerpColor(leastColor,mostColor,value));
				} else {
					value /= maxValue;
					stroke(40,255,40);
				}
				return true;
			}
		}
		private int mostColor = color(255,0,0);
		private int leastColor = color(0,0,0);
		private void moveTo(float[] position2, int i) {
			translate(position2[i],position2[i+1]);
		}
		private float[] position = new float[4];
		private DesignIntermediateReporter dir;
		public void setDesigner(DDSeqDesigner design) {
			this.design = design;
			dir = design.getDir();
			background(255);
		}
}
