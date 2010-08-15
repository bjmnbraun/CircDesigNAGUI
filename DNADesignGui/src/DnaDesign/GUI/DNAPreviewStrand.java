package DnaDesign.GUI;

import static DnaDesign.DomainDesigner_ByRandomPartialMutations.DNA_SEQ_FLAGSINVERSE;

import java.awt.event.MouseEvent;
import java.awt.geom.Rectangle2D;
import java.util.Arrays;

import processing.core.PApplet;
import processing.core.PFont;
import processing.core.PMatrix;
import DnaDesign.DomainStructureData;
import DnaDesign.DomainStructureData.DomainStructure;
import DnaDesign.Exception.InvalidDNAMoleculeException;

/**
 * This is a Lite PApplet, which has no config file and just behaves as one would expect
 * @author Benjamin
 */
public class DNAPreviewStrand extends PApplet{
	public void setup(){
		String wP = null, hP = null;
		try {
			wP = getParameter("width");
			hP = getParameter("height");
		} catch(Throwable e){
			
		}
		int w = getPreferredSize().width, h = getPreferredSize().height;
		if (wP!=null && hP!=null){
			w = new Integer(wP);
			h = new Integer(hP);
		} 
		size(w,h,P3D);
		background(0);
		screen1 = new DnaDesignScreens$0_Screen();
	}
	public void draw(){
		if (getPreferredSize().width!=width){
			size(getPreferredSize().width,getPreferredSize().height,P3D);
		}
		//The world is your oyster, or you could refer to the page-system above.
		g.background(255);
		viewMatrix = g.getMatrix();
	};
	private PMatrix viewMatrix;
	public void viewport(Rectangle2D.Float previewArea) {
		viewport(previewArea.x,previewArea.y,previewArea.width,previewArea.height);
	}
	public void viewport(double x, double y, double w, double h) {
		g.setMatrix(viewMatrix);
		g.translate((float)x*g.width,(float)y*g.height);
		g.scale((float)w*g.width,(float)h*g.height);
	}
	/**
	 * Just the part that looks like "[3*.|2*.|1*.}
	 */
	public void setCurrentPreviewMolecule(String moleculeDescription, String domainDefs){
		needsCurrentMoleculeUpdate = (
				!currentMoleculeString.equals(moleculeDescription) ||
				!domainDefsBlock.equals(domainDefs));
		currentMoleculeString = moleculeDescription;
		domainDefsBlock = domainDefs;
		screen1.preview.updateMolecule();
	}
	public static class UpdateSuccessfulException extends RuntimeException{
	}
	private String currentMoleculeString = "";
	private String domainDefsBlock = "";
	private boolean needsCurrentMoleculeUpdate = true;
	private DnaDesignScreens$0_Screen screen1;
	//Branch code:
	public class DnaDesignScreens$0_Screen {
		/**
		 * What does this screen do?
		 * 1) Allow user to input "constraints" in a certain format, namely, domain sequences, with structural data as well.
		 * 2) Allow user to advance to "Run Designer" screen.
		 * 3) 
		 */
		public DnaDesignScreens$0_Screen(){
			registerDraw(this);
			registerMouseEvent(this);
			preview = new DnaSequencePreview();
			ff = createFont("Arial", 24);
		}
		private PFont ff;
		public class DnaSequencePreview {
			public void updateMolecule(){
				if (needsCurrentMoleculeUpdate){
					try {
						stopTheWorld[0] = 1;
						try {
							Thread.sleep(10);
						} catch (InterruptedException e1) {
							e1.printStackTrace();
						}
						DomainStructureData.readDomainDefs(domainDefsBlock, dsd);
						try {
							DomainStructureData.readStructure(currentMoleculeString, dsd);
						} catch (Throwable e){
							//e.printStackTrace();
							throw new InvalidDNAMoleculeException(e.getMessage(),0);
						}
						Arrays.fill(previewAreaDrag,0);
						needsCurrentMoleculeUpdate = false;
					} finally {
						stopTheWorld[0] = 0;
					}
					throw new UpdateSuccessfulException();
				}
			}
			private DomainStructureData dsd = new DomainStructureData();
			private int[] stopTheWorld = new int[]{0};
			public void draw(){
				if (dsd.structures==null){
					return;
				}
				if (stopTheWorld[0]!=0){
					return;
				}
				Arrays.fill(averagePositions,0);
				drawMolecule(false);
				averagePositions[2] = averagePositions[0] / averagePositions[2];
				averagePositions[3] = averagePositions[1] / averagePositions[3];
				pushMatrix();
				translate(-averagePositions[2]/width,-averagePositions[3]/height);
				translate(.5f,.5f);
				translate(previewAreaDrag[6], previewAreaDrag[7]);
				drawMolecule(true);
				popMatrix();
			}
			private float[] averagePositions = new float[4];
			private void drawMolecule(boolean actuallyDraw){
				pushMatrix();
				for(DomainStructure ds : dsd.structures){
					drawStructure(ds, actuallyDraw);
				}
				popMatrix();
			}
			private void drawStructure(DomainStructure ds, boolean trueDraw){
				float wiggleTheta = sin(frameCount/120f*(1+ds.random0*.3f)+ds.random0*TWO_PI)*.3f;
				if (ds instanceof DomainStructureData.SingleStranded){
					rotate(wiggleTheta);
					for(int p : ds.sequencePartsInvolved){
						int domain = dsd.domains[p];
						int seqLen = dsd.domainLengths[domain & DNA_SEQ_FLAGSINVERSE];
						for(int k = 0; k < seqLen; k++){
							if (k == (seqLen-1) / 2){
								markDomain(dsd.getDomainName(domain),trueDraw);
							}
							stroke(0);
							strokeWeight(2);
							fill(200,200,220);
							ellipseMode(CORNERS);
							float eW = .03f;
							if (trueDraw)	ellipse(-eW, -eW, eW*2, eW*2);
							translate(eW*2, 0);
						}
					}
				}
			}
			private void markDomain(String domainName, boolean trueDraw) {
				textFont(ff);
				if (!trueDraw){
					averagePositions[0] += modelX(0, 0, 0);
					averagePositions[1] += modelY(0, 0, 0);
					averagePositions[2] ++;
					averagePositions[3] ++;
					return;
				}
				pushMatrix();
				fill(0);
				translate(0,-.07f);
				scale(1f/200);
				textAlign(CENTER,BOTTOM);
				text(domainName, 0, 0);
				popMatrix();
			}
		}
		//Viewports broken for now.
		private Rectangle2D.Float previewArea = new Rectangle2D.Float(0,0,1,1);
		private DnaSequencePreview preview;
		public void cleanup() {
			unregisterDraw(this);
			unregisterMouseEvent(this);
		}
		//Mouse drag buffer for preview area
		private float[] previewAreaDrag = new float[8];
		public void mouseEvent(MouseEvent e){
			//MOUSE CONTROLS FOR PREVIEW AREA
			//Within preview area draggable?
			float gx = e.getX()/(float)g.width;
			float gy = e.getY()/(float)g.height;
			if (true /*|| isInRect(previewArea, gx, gy)*/){
				float ingx = (gx - previewArea.x) / previewArea.width;
				float ingy = (gy - previewArea.y) / previewArea.height;
				switch(e.getID()){
				case MouseEvent.MOUSE_PRESSED:
					//Dedicate old drag
					previewAreaDrag[0] += previewAreaDrag[2];
					previewAreaDrag[1] += previewAreaDrag[3];
					previewAreaDrag[2] = 0;
					previewAreaDrag[3] = 0;
					//Lock new position
					previewAreaDrag[4] = ingx;
					previewAreaDrag[5] = ingy;
					break;
				case MouseEvent.MOUSE_DRAGGED:
					previewAreaDrag[2] = ingx - previewAreaDrag[4];
					previewAreaDrag[3] = ingy - previewAreaDrag[5];
					break;
				}
				previewAreaDrag[6] = previewAreaDrag[0]+previewAreaDrag[2];
				previewAreaDrag[7] = previewAreaDrag[1]+previewAreaDrag[3];
			}
		}
		public void draw() {
			viewport(previewArea);
			stroke(0);
			noFill();
			drawGrid();
			
			//Draw current selected sequence?
			drawPreviewSequence();
			//Sequence select wheel (click or use arrow keys to slide up / down)
			
			viewport(0,0,1,1);
			stroke(0);
			noFill();
			//rect(previewArea.x, previewArea.y, previewArea.width, previewArea.height);
			
			//buttons.draw();
		}
		public void drawPreviewSequence(){
			pushMatrix();
			preview.draw();
			popMatrix();
		}
		public void drawGrid(){
			stroke(0,125,40);
			strokeWeight(1);
			float gridW = 2f;
			float gridH = 2f;
			int gridRes = (width / 20);
			pushMatrix();
			translate(previewAreaDrag[6]%gridW, previewAreaDrag[7]%gridH);
			translate(.5f, .5f);
			//Seemingly infinite grid - but don't modulus divide for actual rendering.
			translate(-gridW/2,-gridH/2);
			int xmin = -gridRes;
			int xmax = gridRes * 2;
			int ymin = -gridRes;
			int ymax = gridRes * 2;
			for(int x = xmin; x < xmax; x++){
				float gx = x * gridW / gridRes;
				float g1y = gridH * ymin / gridRes;
				float g2y = gridH * ymax / gridRes;
				line(gx,g1y,gx,g2y);
			}
			for(int y = ymin; y < ymax; y++){
				float gy = y * gridH / gridRes;
				float g1x = gridW * xmin / gridRes;
				float g2x = gridW * xmax / gridRes;
				line(g1x,gy,g2x,gy);
			}
			popMatrix();
		}
	}

	public void ellipse(float x, float y, float w, float h){
	    float radiusH = w / 2;
	    float radiusV = h / 2;

	    float centerX = x + radiusH;
	    float centerY = y + radiusV;

//	    float sx1 = screenX(x, y);
//	    float sy1 = screenY(x, y);
//	    float sx2 = screenX(x+w, y+h);
//	    float sy2 = screenY(x+w, y+h);

	    // returning to pre-1.0 version of algorithm because of problems
	    int wpix = (int) (width*w);
	    int hpix = (int) (height*h);
	    int rough = (int)(4+Math.sqrt(wpix+hpix)*3);
	    int accuracy = PApplet.constrain(rough, 6, 100);
	    
	    float inc = TWO_PI / accuracy;
	      
	    if (g.fill) {
	      // returning to pre-1.0 version of algorithm because of problems
//	      int rough = (int)(4+Math.sqrt(w+h)*3);
//	      int rough = (int) (TWO_PI * PApplet.dist(sx1, sy1, sx2, sy2) / 20);
//	      int accuracy = PApplet.constrain(rough, 6, 100);

	      float val = 0;

	      boolean strokeSaved = g.stroke;
	      g.stroke = false;
	      boolean smoothSaved = g.smooth;
	      if (g.smooth && g.stroke) {
	    	  g.smooth = false;
	      }

	      beginShape(TRIANGLE_FAN);
	      normal(0, 0, 1);
	      vertex(centerX, centerY);
	      for (int i = 0; i < accuracy; i++) {
	        vertex(centerX + cos(val) * radiusH,
	               centerY + sin(val) * radiusV);
	        val = (val + inc);
	      }
	      // back to the beginning
	        vertex(centerX + cos(0) * radiusH,
		               centerY + sin(0) * radiusV);
	      endShape();

	      g.stroke = strokeSaved;
	      g.smooth = smoothSaved;
	    }

	    if (g.stroke) {
//	      int rough = (int) (TWO_PI * PApplet.dist(sx1, sy1, sx2, sy2) / 8);
//	      int accuracy = PApplet.constrain(rough, 6, 100);

	      float val = 0;

	      boolean savedFill = g.fill;
	      g.fill = false;

	      val = 0;
	      beginShape();
	      for (int i = 0; i < accuracy; i++) {
	        vertex(centerX + cos(val) * radiusH,
	               centerY + sin(val) * radiusV);
	        val = (val + inc);
	      }
	      endShape(CLOSE);

	      g.fill = savedFill;
	    }
	}
}
