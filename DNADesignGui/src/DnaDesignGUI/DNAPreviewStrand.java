package DnaDesignGUI;


import static DnaDesign.DomainSequence.DNA_COMPLEMENT_FLAG;
import static DnaDesign.DomainSequence.DNA_SEQ_FLAGSINVERSE;

import java.awt.event.MouseEvent;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.TreeMap;

import processing.core.PApplet;
import processing.core.PFont;
import DnaDesign.DomainStructureData;
import DnaDesign.DomainStructureData.DomainStructure;
import DnaDesign.DomainStructureData.HairpinStem;
import DnaDesign.DomainStructureData.SingleStranded;
import DnaDesign.Exception.InvalidDNAMoleculeException;
import DnaDesign.Exception.InvalidDomainDefsException;

/**
 * This is a Lite PApplet, which has no config file and just behaves as one would expect
 * @author Benjamin
 */
public class DNAPreviewStrand extends PApplet{
	private static final long INPUT_CLOCK_INT = (long) .3e9;
	public DNAPreviewStrand(DnaDesignGUI_ThemedApplet mc) {
		this.mc = mc;
	}
	private DnaDesignGUI_ThemedApplet mc;
	private ScaleFactors<float[]> moleculeScales = new ScaleFactors<float[]>();
	private Object makeMolecularScale(){
		return new float[]{-1,-1};
	}
	private void invalidateMoleculeScale(Object entrygeneric) {
		float[] object = (float[])(entrygeneric);
		Arrays.fill(object,-1);
	}
	private class ScaleFactors<T> {
		public void invalidateAbove(int size){
			while(scaleFactors.lastKey() > size){
				scaleFactors.remove(scaleFactors.lastKey());
			}
		}
		public void setCurrentEntryAndInvalidate(int entry){
			currentEntry = entry;
			invalidateMoleculeScale(getCurrentEntry());
		}
		private int currentEntry;
		public T getCurrentEntry(){
			return getEntry(currentEntry);
		}
		public T getEntry(int id){
			T res = scaleFactors.get(id);
			if (res==null){
				res = (T) makeMolecularScale();
				scaleFactors.put(id,res);
			}
			return res;
		}
		private TreeMap<Integer, T> scaleFactors = new TreeMap();
		public Collection<T> getEntries() {
			return scaleFactors.values();
		}
	}
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
	};
	/**
	 * Just the part that looks like "[3*.|2*.|1*.}
	 * @param molecules_CLine_num 
	 */
	public void setCurrentPreviewMolecule(int molecules_CLine_num, int numMolecules, String moleculeDescription, String domainDefs){
		needsCurrentMoleculeUpdate = (
				!currentMoleculeString.equals(moleculeDescription) ||
				!domainDefsBlock.equals(domainDefs));
		String[] split = moleculeDescription.split("\\s+");
		if (split.length<2){
			throw new InvalidDNAMoleculeException("Correct molecule format: <name> <molecule>",0);
		}
		currentMoleculeName = split[0];
		currentMoleculeString = split[1];
		moleculeScales.setCurrentEntryAndInvalidate(molecules_CLine_num);
		moleculeScales.invalidateAbove(numMolecules);
		domainDefsBlock = domainDefs;
		screen1.preview.updateMolecule();
	}
	public void snapShot(){
		needsSnapshot = true;
	}
	public String getLastSnapshotPath() {
		return snapshotPath;
	}
	public boolean isTakingSnapshot() {
		return needsSnapshot;
	}
	public static class UpdateSuccessfulException extends RuntimeException{
	}
	public static class ConstraintColors {
		public static final float[] 
		                      G = new float []{0,128,0},
		                      A = new float []{0,0,255},
		                      T = new float []{125,125,0},
		                      C = new float []{112,24,27},
		                      D = new float []{100,0,140},
		                      H = new float []{24,27,120},
		                      P = new float []{100,100,140},
		                      Z = new float []{24,100,120},
		                      NONE = new float []{0,0,0},
		                      ERROR = new float []{255,0,0}
		; 
	}
	private String currentMoleculeString = "", currentMoleculeName="";
	private String domainDefsBlock = "";
	private boolean needsCurrentMoleculeUpdate = true, needsSnapshot = false;
	private String snapshotPath = null;
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
		private float moleculeScale = 1f, moleculeScaleMax = 1f, moleculeScale_override = -1;
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
						try {
							DomainStructureData.readDomainDefs(domainDefsBlock, dsd);
						} catch (Throwable e){
							throw new InvalidDomainDefsException(e.getMessage());
						}
						try {
							DomainStructureData.readStructure(currentMoleculeName, currentMoleculeString, dsd);
						} catch (Throwable e){
							//e.printStackTrace();
							throw new InvalidDNAMoleculeException(e.getMessage(),0);
						}
						//How many particles?
						moleculeNumSubStructures = 0;
						for(DomainStructure ds : dsd.structures){
							moleculeNumSubStructures += ds.countLabeledElements();
						}
						moleculeNumSubStructures*=3;
						hasInitialParticleConfiguration = false;
						Arrays.fill(previewAreaDrag,0);
						needsCurrentMoleculeUpdate = false;
						moleculeScale = 1f;
						moleculeScaleMax = 1f;
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
				if (moleculeNumSubStructures > particlepositions.length){
					particlepositions = new float[moleculeNumSubStructures][4];
				}
				if (keyPressed){
					if (keyEvent.getKeyChar() == '-'){
						moleculeScale *= (1-1f/60f);
					}
					if (keyEvent.getKeyChar() == '+'){
						moleculeScale *= (1+1f/60f);
					}
					if (keyEvent.getKeyChar() == 'd'){
						if (System.nanoTime()-drawLineStructure_clock>INPUT_CLOCK_INT){
							drawLineStructure_clock = System.nanoTime();
							drawLineStructure = !drawLineStructure;
						}
					}	
					/*
					if (keyEvent.getKeyChar() == 'w'){
						if (System.nanoTime()-dynamicWiggle_clock>INPUT_CLOCK_INT){
							dynamicWiggle_clock = System.nanoTime();
							dynamicWiggle = !dynamicWiggle;
						}
					}
					*/
				}
				particlepoints = 0;
				drawMolecule(false);
				Arrays.fill(averagePositions,0);
				//compute averages, rotation.
				for(int k = 0; k < particlepoints; k++){
					averagePositions[0] += particlepositions[k][0];
					averagePositions[1] += particlepositions[k][1];
				}
				float avgx = averagePositions[0] / particlepoints;
				float avgy = averagePositions[1] / particlepoints;
				float[] q = new float[4];
				float maxX = .1f;
				float maxY = .1f;
				for(int k = 0; k < particlepoints; k++){
					particlepositions[k][0] -= avgx;
					particlepositions[k][1] -= avgy;
					//Rotate in counterrotation:
					rotateVec(particlepositions[k],longestHairpin_counterrotation[0]);
					maxX = max(maxX,abs(particlepositions[k][0])*2*sqrt(2));
					maxY = max(maxY,abs(particlepositions[k][1])*2*sqrt(2));
					if (!hasInitialParticleConfiguration){
						//We will use the first draw as the "identity"
						particlepositions[k][2] = particlepositions[k][0];
						particlepositions[k][3] = particlepositions[k][1];
					}
					float onek0 = particlepositions[k][0];
					float onek1 = particlepositions[k][1];
					float twok0 = particlepositions[k][2];
					float twok1 = particlepositions[k][3];
					q[0]+=onek0*twok0;
					q[1]+=onek0*twok1;
					q[2]+=onek1*twok0;
					q[3]+=onek1*twok1;
				}
				float theta = atan2((q[1]-q[2]),(q[0]+q[3]))+longestHairpin_counterrotation[0];
				if (!hasInitialParticleConfiguration){
					moleculeScaleMax = 1f/(max(maxX,maxY)+.03f);
					//System.out.println(maxX);
					moleculeScale = moleculeScaleMax;
				}
				moleculeScales.getCurrentEntry()[0] = moleculeScale;
				pushMatrix();
				translate(.5f,.5f);
				translate(previewAreaDrag[6], previewAreaDrag[7]);
				if (moleculeScale_override>0){
					scale(moleculeScale_override);
				} else {
					scale(moleculeScale);
				}
				rotate(theta);
				translate(-avgx,-avgy);
				drawMolecule(true);
				popMatrix();
				hasInitialParticleConfiguration = true;
			}
			private int moleculeNumSubStructures = 0;
			private float[] averagePositions = new float[4];
			private float[][] particlepositions = new float[moleculeNumSubStructures][0];
			private int longestHairpin = -1;
			private float[] longestHairpin_counterrotation = new float[]{0,1};
			private boolean hasInitialParticleConfiguration = false;
			private int particlepoints = 0;
			private void drawMolecule(boolean actuallyDraw){
				pushMatrix_drawMolecule();
				longestHairpin = -1;
				longestHairpin_counterrotation = new float[]{0,1}; //Stays 0 unless a helix is found.
				try {
					boolean wasSS = true;
					for(DomainStructure ds : dsd.structures){
						drawStructure(ds, actuallyDraw, -1, 0, wasSS && !(ds instanceof HairpinStem));
						wasSS = ds instanceof SingleStranded;
					}
				} catch (Throwable e){
					//e.printStackTrace();
				} finally {
					popMatrix_drawMolecule_full();
				}
			}
			private void drawStructure(DomainStructure ds, boolean trueDraw, int hairpinSize, int shaftLength, boolean lastWasSS){
				if (shaftLength>0 && !(ds instanceof HairpinStem)){
					throw new RuntimeException("Assertion error: inShaft only valid for continuing hairpinstems");
				}
				//TODO: button to turn this on / off
				float wiggleTheta = !dynamicWiggle?PI/4:sin(frameCount/120f*(1+ds.random0*.3f)+ds.random0*TWO_PI)*.3f; 
				wiggleTheta = lastWasSS?0:wiggleTheta;
				float HairpinOpenAngle = HALF_PI-PI/8;
				float eW = .03f;
				float openingSize = 2f;
				//Additional space to put on the ring, due to the opening of the loop.
				float deltaTheta = 0;
				float ringAdd = openingSize/2;
				if (hairpinSize!=-1){
					deltaTheta = hairpinDeltaTheta(hairpinSize,ringAdd);
				}
				if (ds instanceof DomainStructureData.SingleStranded){
					if (hairpinSize==-1){ //Outer loop
						rotate(wiggleTheta);
					}
					for(int p : ds.sequencePartsInvolved){
						int domain = dsd.domains[p];
						int seqLen = dsd.domainLengths[domain & DNA_SEQ_FLAGSINVERSE];
						for(int k = 0; k < seqLen; k++){
							if (k == (seqLen-1) / 2){
								pushMatrix_drawMolecule();
								try {
									translate(seqLen%2==0?eW:0,0);
									markDomain(dsd.getDomainName(domain),seqLen/2f*eW,trueDraw);
								} finally {
									popMatrix_drawMolecule();
								}
							}
							if (trueDraw){
								drawBase(eW,domain,k);
							}
							translate(eW*2, 0);
							rotate(deltaTheta);
						}
					}
					//END OF METHOD
				} else if (ds instanceof DomainStructureData.HairpinStem){
					DomainStructureData.HairpinStem hs = (DomainStructureData.HairpinStem)ds;

					if (hairpinSize==-1){ //Outer loop
						if (shaftLength==0) //First stem on outside loop
							rotate(wiggleTheta);
					}
					if (shaftLength==0){
						translate(openingSize*eW/2,0); //Size of opening.
					}
					pushMatrix_drawMolecule();
					if (shaftLength==0){
						rotate(-HALF_PI);
					}

					//Draw the shaft.
					int domain = dsd.domains[ds.sequencePartsInvolved[0]];
					int domain2 = dsd.domains[ds.sequencePartsInvolved[1]];
					//They better be the same lengths...
					int seqLen = dsd.domainLengths[domain & DNA_SEQ_FLAGSINVERSE];

					
					//Do we begin a loop?
					boolean inShaft2 = false;
					int newShaftLength = shaftLength + seqLen;
					if (newShaftLength>longestHairpin){
						longestHairpin = newShaftLength;
						longestHairpin_counterrotation = getCounterRotation();
					}
					if (hs.subStructure.size()>0){
						DomainStructure domainStructure = hs.subStructure.get(0);
						inShaft2 = domainStructure instanceof HairpinStem;
						inShaft2 &= hs.subStructure.size()==1;
					}
										
					for(int k = 0; k < seqLen; k++){
						if (k == (seqLen-1) / 2){
							pushMatrix_drawMolecule();
							try {
								translate(seqLen%2==0?eW:0,0);
								translate(0,-eW*openingSize/2);
								markDomain(dsd.getDomainName(domain),seqLen/2f*eW,trueDraw);
								translate(0,eW*openingSize/2);
								rotate(PI);
								markDomain(dsd.getDomainName(domain2),seqLen/2f*eW,trueDraw);
							} finally {
								popMatrix_drawMolecule();
							}
						}
						if (trueDraw){
							translate(0,-eW*openingSize/2);
							drawBase(eW,domain,k);
							translate(0,eW*openingSize);
							drawBase(eW,domain2,seqLen-1-k);
							translate(0,-eW*openingSize/2);
						}
						translate(eW*2, 0);
					}
					if (!inShaft2){
						boolean isClosedLoop = hs.leftRightBreak==-1;
						if (isClosedLoop){
							//Loop!
							translate(0,-eW*openingSize/2);
							rotate(-HALF_PI);
							//Account for the opening
							rotate(hairpinDeltaTheta(hs.innerCurveCircumference,ringAdd));
							//Recurse through closed loop
							for(int k = 0; k < hs.subStructure.size(); k++){
								DomainStructure domainStructure = hs.subStructure.get(k);
								drawStructure(domainStructure,trueDraw,hs.innerCurveCircumference, 0, false);	
							}
						} else {
							//Broken loop. Render the right, then the left (stack)
							translate(0,-eW*openingSize/2);
							rotate(-HALF_PI);
							pushMatrix_drawMolecule();
							{
								rotate(PI);
								translate(eW*openingSize,0);
								rotate(-HairpinOpenAngle);
								int rightLoopSize = 0;
								//Recurse through right
								for(boolean getLengthPass : new boolean[]{true,false}){
									for(int k = hs.leftRightBreak+1; k < hs.subStructure.size(); k++){
										DomainStructure domainStructure = hs.subStructure.get(k);
										if (getLengthPass){
											rightLoopSize += DomainStructure.getOuterLevelSpace(domainStructure, dsd.domainLengths, dsd.domains);
										} else {
											//Use "lastwasSS" to get it oriented the right way on the way back
											drawStructure(domainStructure,trueDraw,-1, 0, true);
										}
									}
									if (getLengthPass){
										//Ok. Translate us WAY out there, and then come back.
										translate(eW*2*rightLoopSize,0);
										rotate(PI);
									}
								}
							}
							popMatrix_drawMolecule();
							//Recurse through left
							rotate(HairpinOpenAngle);
							for(int k = 0; k <= hs.leftRightBreak; k++){
								DomainStructure domainStructure = hs.subStructure.get(k);
								drawStructure(domainStructure,trueDraw,-1, 0, true);	
							}
						}
					} else {
						//Recurse up shaft
						for(int k = 0; k < hs.subStructure.size(); k++){
							DomainStructure domainStructure = hs.subStructure.get(k);
							drawStructure(domainStructure,trueDraw,hs.innerCurveCircumference, newShaftLength, false);	
						}
					}
					popMatrix_drawMolecule();
					translate(eW*openingSize/2,0); //Size of opening.
					rotate(deltaTheta);
					/*
					if (hairpinSize!=-1){
						rotate(TWO_PI/(hairpinSize+openingSize));
					}
					*/
					//END OF METHOD
				} else if (ds instanceof DomainStructureData.ThreePFivePOpenJunc){
					stroke(0);
					if (trueDraw){	
						line(-eW,0,eW,0);
						line(0,-eW,eW,0);
						line(0,eW,eW,0);
					}
					markDomain("",eW/2,trueDraw);
					//ew*4
					translate(eW*2, 0);
					rotate(deltaTheta);
					translate(eW*2, 0);
					rotate(deltaTheta);
				}
			}
			private float hairpinDeltaTheta(int hairpinSize, float ringAdd) {
				return TWO_PI/(hairpinSize+ringAdd);
			}
			private void drawBase(float eW, int domain, int k) {
				boolean isComp = (domain & DNA_COMPLEMENT_FLAG)!=0;
				/**
				 * Note: constraints 
				 */
				char constraint = dsd.getConstraint(domain).charAt(isComp?dsd.domainLengths[domain&DNA_SEQ_FLAGSINVERSE]-1-k:k);
				int sCol = color(0); //the Good color.
				switch(Character.toUpperCase(constraint)){
				case 'G':
					fillA(!isComp?ConstraintColors.G:ConstraintColors.C); break;
				case 'A':
					fillA(!isComp?ConstraintColors.A:ConstraintColors.T); break;
				case 'T':
					fillA(!isComp?ConstraintColors.T:ConstraintColors.A); break;
				case 'C':
					fillA(!isComp?ConstraintColors.C:ConstraintColors.G); break;
				case 'D':
					fillA(!isComp?ConstraintColors.H:ConstraintColors.D); break;
				case 'H':
					fillA(!isComp?ConstraintColors.D:ConstraintColors.H); break;
				case 'P':
					fillA(!isComp?ConstraintColors.H:ConstraintColors.D); break;
				case 'Z':
					fillA(!isComp?ConstraintColors.D:ConstraintColors.H); break;
				case '-':
					fillA(ConstraintColors.NONE); break;
				default:
					fillA(ConstraintColors.ERROR); break;
				}
				if (drawLineStructure){
					if (Character.isLowerCase(constraint)){
						line(0,eW/4,eW*2,eW/4);
						line(0,-eW/4,eW*2,-eW/4);
					} else {
						line(0,0,eW*2,0);
					}
				} else {
					stroke(0);
					line(0,0,eW*2,0);
					if (Character.isLowerCase(constraint)){
						stroke(sCol);
					} else {
						noStroke();
					}
					ellipseMode(CORNERS);
					ellipse(eW-eW*.9f,-eW*.9f,eW*2*.9f,eW*2*.9f);
				}
			}
			private boolean dynamicWiggle = false; private long dynamicWiggle_clock = System.nanoTime();
			private boolean drawLineStructure = true; private long drawLineStructure_clock = System.nanoTime();
			public void fillA(float[] color){
				if (drawLineStructure){
					stroke(color[0],color[1],color[2]);
				} else {
					fill(color[0],color[1],color[2]);
				}
			}
			private float[] sharedMarkDomain = new float[16];
			private void markDomain(String domainName, float spacing, boolean trueDraw) {
				textFont(ff);
				if (!trueDraw){
					for(int mult = -1; mult <= 1; mult++){
						particlepositions[particlepoints][0] = screenX(spacing*mult, 0)/width;
						particlepositions[particlepoints][1] = screenY(spacing*mult, 0)/height;
						particlepoints++;
					}
					return;
				}
				pushMatrix();
				fill(0);
				translate(0,-.1f);
				scale(1f/200);
				textAlign(CENTER,CENTER);
				//A bit of Linear algebra. Reverse engineer our current rotation and flip.
				final float[] counterRotation = getCounterRotation();
				scale(1,counterRotation[1]);
				rotate(counterRotation[0]);
				text(domainName, 0, 0);
				popMatrix();
			}
		}
		private float[] getCounterRotation(){
			float flipY = drawFlippedStack.size()%2==1?-1:1;
			float av1minav0x = screenX(1,0)-screenX(0,flipY);
			float av1minav0y = screenY(1,0)-screenY(0,flipY);
			return new float[]{-(atan2(av1minav0y,av1minav0x)+PI/4),flipY};
		}
		private void rotateVec(float[] vec2, float t){
			float nx = cos(t)*vec2[0]-sin(t)*vec2[1];
			float ny = sin(t)*vec2[0]+cos(t)*vec2[1];
			vec2[0] = nx;
			vec2[1] = ny;
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
			pushMatrix();
			scale(width,height);
			stroke(0);
			noFill();
			drawGrid();
			popMatrix();
			pushMatrix();
			strokeWeight(2);
			boolean isSnapshotting = false;
			moleculeScale_override = -1;
			if (needsSnapshot){
				isSnapshotting = true;
				File temp;
				try {
					temp = File.createTempFile("DNADesignPreview"+System.nanoTime(), ".pdf");
					beginRecord(PDF, snapshotPath = temp.getAbsolutePath());
				} catch (IOException e) {
					e.printStackTrace();
				}
				pushMatrix();
				float maxScale = 0, minScale = Float.MAX_VALUE; //greater than -1
				for(float[] k : moleculeScales.getEntries()){
					if (k[0] > 0){
						minScale = Math.min(maxScale,k[0]);
					}
					maxScale = Math.max(maxScale,k[0]);
				}
				if (maxScale!=0){ //This should always happen, but race conditions can screw up
					moleculeScale_override = minScale;
				}
				strokeWeight(2f/width);
				
			}
			pushMatrix();
			scale(width,height);
			//Draw current selected sequence?
			drawPreviewSequence();
			popMatrix();
			//Sequence select wheel (click or use arrow keys to slide up / down)
			if (isSnapshotting){
				popMatrix();
				if (true){
					fill(0);
					textFont(ff);
					textAlign(LEFT,TOP);
					text(currentMoleculeName,2,1);
				}
				endRecord();
				needsSnapshot = false;
			}
			popMatrix();
			resetMatrix();
		}
		public void drawPreviewSequence(){
			pushMatrix();
			preview.draw();
			popMatrix();
		}
		private int drawMoleculePush = 0;
		private LinkedList<Integer> drawFlippedStack = new LinkedList();
		private void pushMatrix_drawMolecule(){
			drawMoleculePush++;
			pushMatrix();
		}
		private void pushMatrix_drawFlippedMolecule(){
			pushMatrix_drawMolecule();
			drawFlippedStack.add(drawMoleculePush);
			rotate(PI);
			scale(1,-1);
		}
		private void popMatrix_drawMolecule(){
			drawMoleculePush--;
			if (!drawFlippedStack.isEmpty()){
				if (drawMoleculePush<drawFlippedStack.getLast()){
					drawFlippedStack.removeLast();
				}
			}
			popMatrix();
		}
		private void popMatrix_drawMolecule_full(){
			while(drawMoleculePush>0){
				popMatrix_drawMolecule();
			}
		}
		public void drawGrid(){
			stroke(mc.THEMECOL4.getRGB());
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
		if (needsSnapshot){
			ellipseMode(CORNER);
			super.ellipse(x,y,w,h);
			return;
		}
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
