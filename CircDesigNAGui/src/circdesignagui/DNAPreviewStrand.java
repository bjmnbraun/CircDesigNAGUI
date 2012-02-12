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


import static circdesigna.DomainSequence.NA_COMPLEMENT_FLAG;
import static circdesigna.DomainSequence.NA_COMPLEMENT_FLAGINV;

import java.awt.event.MouseEvent;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.TreeMap;

import processing.core.PApplet;
import processing.core.PFont;
import processing.core.PMatrix;
import circdesigna.DomainDefinitions;
import circdesigna.DomainPolymerGraph;
import circdesigna.DomainStructureBNFTree;
import circdesigna.DomainStructureBNFTree.DomainStructure;
import circdesigna.DomainStructureBNFTree.HairpinStem;
import circdesigna.DomainStructureBNFTree.SingleStranded;
import circdesigna.DomainStructureBNFTree.ThreePFivePOpenJunc;
import circdesigna.config.CircDesigNAConfig;
import circdesigna.exception.InvalidDNAMoleculeException;
import circdesigna.exception.InvalidDomainDefsException;
import circdesignagui.math.CircumscribedPolygonTool.CircumscribedPolygon;

/**
 * An embedded PApplet for displaying molecule previews. A number of keystrokes
 * make this applet interactive. Mouse dragging also moves the screen. See the in-applet help file for
 * the molecules window for more information.
 * @author Benjamin
 */
public class DNAPreviewStrand extends PApplet{
	private static final long INPUT_CLOCK_INT = (long) .3e9;
	public DNAPreviewStrand(ThemedApplet mc, CircDesigNAConfig config) {
		this.config = config;
		this.mc = mc;
	}
	private ThemedApplet mc;
	private CircDesigNAConfig config;
	private ScaleFactors<float[]> moleculeScales = new ScaleFactors<float[]>();
	private Object makeMolecularScale(){
		return new float[]{-1,-1};
	}
	private void invalidateMoleculeScale(Object entrygeneric) {
		float[] object = (float[])(entrygeneric);
		Arrays.fill(object,-1);
	}
	//For scaling mkultiple molecule previews when generating PDFs
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
	public void setVisible(boolean b){
		if (b && !start){
			init();
			start();	
			start = true;
		}
		super.setVisible(b);
	}
	private boolean start = false;
	public void setup(){
		int w = getPreferredSize().width, h = getPreferredSize().height;
		size(w,h,P3D);
		background(0);
		screen1 = new DnaDesignScreens$0_Screen();
	}
	public void draw(){
		if (getPreferredSize().width!=width){
			size(getPreferredSize().width,getPreferredSize().height,P3D);
		}
		g.background(255);
	};
	/**
	 * With a name, so valid input looks like "Name [3*.|2*.|1*.}"
	 * @param molecules_CLine_num 
	 */
	public void setCurrentPreviewMolecule(int molecules_CLine_num, int numMolecules, String moleculeDescription, String domainDefs){
		needsCurrentMoleculeUpdate = (
				!currentMoleculeString.equals(moleculeDescription) ||
				!domainDefsBlock.equals(domainDefs));
		String[] split = moleculeDescription.split("\\s+",2);
		if (split.length<2){
			throw new InvalidDNAMoleculeException("Correct molecule format: <name> <molecule>",0);
		}
		currentMoleculeString = moleculeDescription;
		moleculeScales.setCurrentEntryAndInvalidate(molecules_CLine_num);
		moleculeScales.invalidateAbove(numMolecules);
		domainDefsBlock = domainDefs;
		screen1.preview.updateMolecule();
	}
	public void snapShot(String snapshotPath){
		needsSnapshot = true;
		this.snapshotPath = snapshotPath;
	}
	public void snapShot(){
		snapShot(null);
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
		                      G = new float []{0,100,0},
		                      A = new float []{0,0,255},
		                      T = new float []{125,125,0},
		                      C = new float []{112,24,27},
		                      P = new float []{100,100,140},
		                      Z = new float []{24,100,120},
		                      ERROR = new float []{255,0,0},
		                      I = new float []{200,0,140},
		                      L = new float []{200,27,120},
		                      R = new float []{10,150,50},
		                      Y = R,
		                      M = Y,
		                      K = M,
		                      S = K,
		                      W = S,
		                      V = W,
		                      H = V,
		                      B = H,
		                      D = B,
		                      NONE = new float []{0,0,0}
		; 
	}
	private String currentMoleculeString = "";
	private String domainDefsBlock = "";
	private boolean needsCurrentMoleculeUpdate = true, needsSnapshot = false;
	private String snapshotPath = null;
	public DnaDesignScreens$0_Screen screen1;
	public DnaDesignScreens$0_Screen.DnaSequencePreview getRenderer(){
		return screen1.preview;
	}
	//Branch code:
	public class DnaDesignScreens$0_Screen {
		/**
		 * What does this screen do?
		 * 1) Allow user to input "constraints" in a certain format, namely, domain sequences, with structural data as well.
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
							DomainDefinitions.readDomainDefs(domainDefsBlock, dsd);
						} catch (Throwable e){
							throw new InvalidDomainDefsException(e.getMessage());
						}
						try {
							DomainStructureBNFTree.readStructure(currentMoleculeString, dsg);
							DomainPolymerGraph.readStructure(currentMoleculeString, dpg);
						} catch (Throwable e){
							//e.printStackTrace();
							dsg.structures = null;
							throw new InvalidDNAMoleculeException(e.getMessage(),0);
						}
						//How many particles?
						moleculeNumSubStructures = 0;
						for(DomainStructure ds : dsg.structures){
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
			private DomainDefinitions dsd = new DomainDefinitions(config);
			public DomainStructureBNFTree dsg = new DomainStructureBNFTree(dsd);
			public DomainPolymerGraph dpg = new DomainPolymerGraph(dsd);
			private float[] renderPolymerGraph_LengthCache = null; //For storing partial indexes of domains
			private int[] stopTheWorld = new int[]{0};
			public void draw(){
				if (dsg.structures==null){
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
					if (keyEvent.getKeyChar() == 'q'){
						if (System.nanoTime()-keyPressedClock>INPUT_CLOCK_INT){
							keyPressedClock = System.nanoTime();
							
							drawDomainNameOnLine = !drawDomainNameOnLine;
							if (drawDomainNameOnLine){
								showDomainNames = true;
							}
						}
					}
					if (keyEvent.getKeyChar() == 'd'){
						if (System.nanoTime()-keyPressedClock>INPUT_CLOCK_INT){
							keyPressedClock = System.nanoTime();
							
							drawLineStructure = !drawLineStructure;
						}
					}	
					if (keyEvent.getKeyChar() == 'a'){
						if (System.nanoTime()-keyPressedClock>INPUT_CLOCK_INT){
							keyPressedClock = System.nanoTime();
							
							showDomainNames = !showDomainNames;
						}
					}
					if (keyEvent.getKeyChar() == 'w'){
						if (System.nanoTime()-keyPressedClock>INPUT_CLOCK_INT){
							keyPressedClock = System.nanoTime();
							
							dynamicWiggle = !dynamicWiggle;
							//Invalidate:
							hasInitialParticleConfiguration = false;
						}
					}
					if (keyEvent.getKeyChar() == 'p'){
						if (System.nanoTime()-keyPressedClock>INPUT_CLOCK_INT){
							keyPressedClock = System.nanoTime();
							
							drawPolymerGraph = !drawPolymerGraph;
							//Invalidate:
							hasInitialParticleConfiguration = false;
						}
					}
					if (keyEvent.getKeyChar() == 'g'){
						if (System.nanoTime()-keyPressedClock>INPUT_CLOCK_INT){
							keyPressedClock = System.nanoTime();

							drawGrid = !drawGrid;
						}
					}
					if (keyEvent.getKeyChar() == '('){
						sampleDeform.twist(PI/16);
					}
					if (keyEvent.getKeyChar() == ')'){
						sampleDeform.twist(-PI/16);
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
				longestHairpin = -1;
				longestHairpin_counterrotation = new float[]{0,1}; //Stays 0 unless a helix is found.
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
				if (counterRotate){
					rotate(theta);
				}
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
			
			public void setDeformation(MolecularDeformation md){
				if (md==null){
					this.sampleDeform.setEnabled(false);
				} else {
					this.sampleDeform = md;
				}
			}
			private MolecularDeformation sampleDeform = getDefaultDeformation();
			public MolecularDeformation getDefaultDeformation() {
				return new MolecularDeformation(DNAPreviewStrand.this);
			}
			private void drawMolecule(boolean actuallyDraw){
				pushMatrix_drawMolecule();
				try {
					if (drawPolymerGraph){
						drawPolymerGraph(actuallyDraw);
					} else {
						boolean wasSS = true;
						if (dsg.outerCurve!=null){
							dsg.outerCurve.resetCounter();
							dsg.outerCurve.next();
						}
						sampleDeform.reset();
						for(DomainStructure ds : dsg.structures){
							drawStructure(ds, actuallyDraw, dsg.outerCurve, 0, wasSS, true, sampleDeform);
							wasSS = !(ds instanceof HairpinStem);
						}
					}
				} catch (Throwable e){
					//e.printStackTrace();
				} finally {
					popMatrix_drawMolecule_full();
				}
			}
			private void drawPolymerGraph(boolean actuallyDraw) {
				int pairColor = color(54,150,153);
				
				if (!actuallyDraw){
					particlepositions[particlepoints][0] = screenX(-1, -1)/width;
					particlepositions[particlepoints][1] = screenY(-1, -1)/height;
					particlepoints++;

					particlepositions[particlepoints][0] = screenX(1, 1)/width;
					particlepositions[particlepoints][1] = screenY(1, 1)/height;
					particlepoints++;
				}

				if (renderPolymerGraph_LengthCache==null || renderPolymerGraph_LengthCache.length < dpg.length()){
					renderPolymerGraph_LengthCache = new float[dpg.length()];
				}
				float renderLength = -1;
				float add5pGapR = 0;
				for(boolean add5pGap : new boolean[]{false,true}){
					renderLength = 1;
					for(int i = 0; i < dpg.length(); i++){
						renderPolymerGraph_LengthCache[i] = renderLength;
						int id = dpg.getDomain(i);
						if (id == -1){
							renderLength += add5pGapR;
						} else {
							renderLength += dsd.domainLengths[id & NA_COMPLEMENT_FLAGINV];
						}
					}
					
					//First time, this value is not created. Second, it is valid.
					if (!add5pGap){
						add5pGapR = renderLength / 20;
					}
				}
				float dTheta = TWO_PI/renderLength;
				pushMatrix_drawMolecule();
				rotate(PI); //Start with the entry point at stage left, because people read from left to right.
				ellipseMode(CORNER);
				for(int i = 0; i < dpg.length(); i++){
					int id = dpg.getDomain(i);
					float numBases = 0;
					if (id == -1){
						numBases= 1;
					} else {
						numBases= dsd.domainLengths[id & NA_COMPLEMENT_FLAGINV];
					}
					for(int k = 0; k < numBases; k++){
						float cDLength = renderPolymerGraph_LengthCache[i]+k;
						float radS = dTheta*cDLength;
						float x1 = cos(radS);
						float y1 = sin(radS);
						stroke(0);
						noFill();
						if (id==-1){
							if (actuallyDraw){
								pushMatrix_drawMolecule();
								translate(x1,y1);
								rotate(radS+HALF_PI);
								draw3PEnd();
								popMatrix_drawMolecule();
							}
						} else {
							float radE = dTheta*(cDLength+1);
							if (actuallyDraw){
								arc(-1,-1,2,2,radS,radE);
							}
							//Paired?
							int pair = dpg.getDomainPair(i);
							if (pair!=-1){
								stroke(pairColor);
								if (actuallyDraw){
									int lenPair = dsd.domainLengths[dpg.getDomain(pair)&NA_COMPLEMENT_FLAGINV];
									float radP = dTheta*(renderPolymerGraph_LengthCache[pair]+(lenPair-1-k));
									float x2 = cos(radP);
									float y2 = sin(radP);
									line(x1,y1,x2,y2);
								}
							}
							pushMatrix_drawMolecule();
							translate(x1,y1);
							rotate(radS+HALF_PI);
							if (actuallyDraw){
								line(0,-eW,0,0);
							}
							if (k == (int)((numBases - 1) / 2)){
								markDomain(dsd.getDomainName(id),0,actuallyDraw, null);
							}
							popMatrix_drawMolecule();
						}	
					}
				}
				popMatrix_drawMolecule();
			}
			final float eW = .03f;
			private void drawStructure(DomainStructure ds, boolean trueDraw, CircumscribedPolygon cp, int shaftLength, boolean lastWasSS, boolean allowWiggle, MolecularDeformation deform){
				//System.out.println(ds+" "+lastWasSS);
				if (shaftLength>0 && !(ds instanceof HairpinStem)){
					throw new RuntimeException("Assertion error: inShaft only valid for continuing hairpinstems");
				}
				
				float wiggleTheta = PI/4; 
				wiggleTheta = lastWasSS?0:wiggleTheta;
				if (dynamicWiggle && allowWiggle){
					wiggleTheta += sin(frameCount/120f*(1+ds.random0*.3f)+ds.random0*TWO_PI)*.3f;
				}
				float HairpinOpenAngle = wiggleTheta;

				if (ds instanceof ThreePFivePOpenJunc){
					stroke(0);
					if (trueDraw){	
						draw3PEnd();
					}
					markDomain("",eW/2,trueDraw, deform);
				}
				
				if (!lastWasSS){
					popMatrix_drawMolecule();
				}
				
				if (ds instanceof ThreePFivePOpenJunc){
					translate(eW*2*((ThreePFivePOpenJunc)ds).size, 0);
					if (cp!=null) rotate(cp.next());
				}
				
				if (ds instanceof SingleStranded){
					if (cp==null){ //Outer loop
						rotate(wiggleTheta);
					}
					for(int p : ds.sequencePartsInvolved){
						int domain = dsg.domains[p];
						int seqLen = dsd.domainLengths[domain & NA_COMPLEMENT_FLAGINV];
						for(int k = 0; k < seqLen; k++){
							if (k == (seqLen-1) / 2){
								pushMatrix_drawMolecule();
								try {
									translate(seqLen%2==0?eW:0,0);
									markDomain(dsd.getDomainName(domain),seqLen/2f*eW,trueDraw, deform);
								} finally {
									popMatrix_drawMolecule();
								}
							}
							if (trueDraw){
								drawBase(eW,domain,k);
							}
							translate(eW*2, 0);
							if (cp!=null){
								rotate(cp.next());	
							} else {
								if (deform!=null) deform.run(1);
							}
						}
					}
					//END OF METHOD
				} else if (ds instanceof HairpinStem){
					HairpinStem hs = (HairpinStem)ds;
					float openingSize = hs.openingSize*eW*2;
					
					//Draw the shaft.
					int domain = dsg.domains[ds.sequencePartsInvolved[0]];
					int domain2 = dsg.domains[ds.sequencePartsInvolved[1]];
					//They better be the same lengths...
					int seqLen = dsd.domainLengths[domain & NA_COMPLEMENT_FLAGINV];
					int newShaftLength = shaftLength + seqLen;
					
					//Position shaft
					if (shaftLength==0){
						if (cp==null){ //Outer loop
							rotate(wiggleTheta);
						}
						translate(openingSize,0); //Size of opening.
						float deltaTheta = 0;
						if (cp!=null) deltaTheta = cp.next();
						rotate(deltaTheta);
						pushMatrix_drawMolecule();
						rotate(-deltaTheta);
						rotate(HALF_PI);
						pushMatrix_drawMolecule();
						rotate(-HALF_PI);
						translate(-openingSize/2,0); //Size of opening.
						rotate(-HALF_PI);
					} 
					
					if (seqLen > 0){
						if (longestHairpin == -1){
							if (deform.isNullTransform() && rotateToLongestHairpin){
								longestHairpin_counterrotation = getCounterRotation();	
							}
						}
						longestHairpin = Math.max(newShaftLength,longestHairpin);
					}
					
					//Draw shaft
					for(int k = 0; k < seqLen; k++){
						if (k == (seqLen - 1) / 2){
							pushMatrix_drawMolecule();
							try {
								translate(seqLen%2==0?eW:0,0);
								translate(0,-openingSize/2);
								markDomain(dsd.getDomainName(domain),seqLen/2f*eW,trueDraw, deform);
								translate(0,openingSize);
								translate(eW*2,0);
								rotate(PI);
								markDomain(dsd.getDomainName(domain2),seqLen/2f*eW,trueDraw, deform);
							} finally {
								popMatrix_drawMolecule();
							}
						}
						if (trueDraw){
							translate(0,-openingSize/2);
							deform.run(1);
							drawBase(eW,domain,k);
							deform.run(-1);
							translate(0,openingSize);
							deform.run(1);
							drawBase(eW,domain2,seqLen-1-k);
							deform.run(-1);
							translate(0,-openingSize/2);
							
							//hydrogen bonding
							stroke(lerpColor(color(100,100,100),color(255,255,255),1-hs.hydrogenBondStrength));
							deform.run(1);
							translate(eW,0);
							line(0,-openingSize/4,0,openingSize/4);	
							translate(-eW,0);
							deform.run(-1);
							//Back to backbone color.
							stroke(0);
						}
						deform.run(1);
						translate(eW*2, 0);
					}
					
					//Do we have a loop? (i.e., we are the end of a stem)
					if (hs.innerCurve!=null){
						boolean displayBrokenLoop = (hs.leftRightBreak==0 || hs.leftRightBreak==hs.subStructure.size()-1) 
						&& !(hs.subStructure.get(0) instanceof HairpinStem || hs.subStructure.get(hs.subStructure.size()-1) instanceof HairpinStem);
						
						if (!displayBrokenLoop){
							//Loop!
							translate(0,-openingSize/2);
							hs.innerCurve.resetCounter();
							float openingTheta = -HALF_PI+hs.innerCurve.next();
							rotate(openingTheta);
							pushMatrix_drawMolecule();
							rotate(-openingTheta);
							//Recurse through closed loop
							boolean wasSS = false;
							for(int k = 0; k < hs.subStructure.size(); k++){
								DomainStructure domainStructure = hs.subStructure.get(k);
								drawStructure(domainStructure,trueDraw,hs.innerCurve, 0, wasSS, true, deform);	
								wasSS = !(domainStructure instanceof HairpinStem);
							}
							if (!wasSS){
								popMatrix_drawMolecule();
							}
						} else {
							//Broken loop. Render the left, then the right (stack)
							translate(0,-openingSize/2);
							rotate(-HALF_PI);
							pushMatrix_drawMolecule();
							if (hs.leftRightBreak==0){
								
							} else {
								//Recurse through left
								rotate(HairpinOpenAngle);
							}
							pushMatrix_drawMolecule();
							rotate(HALF_PI);
							//The left hand side can curve.
							boolean lastWasSS2 = false;
							for(int k = 0; k <= hs.leftRightBreak; k++){
								DomainStructure domainStructure = hs.subStructure.get(k);
								//if(!(domainStructure instanceof HairpinStem)){
								//	lastWasSS2 = true;
								//}
								drawStructure(domainStructure,trueDraw,null, 0, lastWasSS2, true, deform);
								lastWasSS2 = !(domainStructure instanceof HairpinStem);
							}
							if (!lastWasSS2){
								popMatrix_drawMolecule();
							}
							popMatrix_drawMolecule();
							pushMatrix_drawMolecule();
							{
								//Recurse through right
								rotate(PI);
								translate(openingSize,0);
								//rotate(-HairpinOpenAngle);
								rotate(-HALF_PI);
								float rightLoopSize = 0;
								boolean wasSS = true;
								for(boolean getLengthPass : new boolean[]{true,false}){
									List<Float> holder = new ArrayList();
									for(int k = hs.leftRightBreak+1; k < hs.subStructure.size(); k++){
										DomainStructure domainStructure = hs.subStructure.get(k);
										if (getLengthPass){
											DomainStructure.getOuterLevelSpace(holder, domainStructure, dsd.domainLengths, dsg.domains);
										} else {
											//Use "lastwasSS" to get it oriented the right way on the way back
											//Can't allow wiggling, unfortunately.
											drawStructure(domainStructure,trueDraw,null, 0, wasSS, false, deform);	
											wasSS = !(domainStructure instanceof HairpinStem);
										}
									}
									if (getLengthPass){
										rightLoopSize = 0;
										for(Float k : holder){
											rightLoopSize += k;
										}
										//Ok. Translate us WAY out there, and then come back.
										translate(eW*2*rightLoopSize,0);
										rotate(PI);
									}
								}
								if (!wasSS){
									popMatrix_drawMolecule();
								}
							}
							popMatrix_drawMolecule();
						}
					} else {
						//Recurse up shaft
						for(int k = 0; k < hs.subStructure.size(); k++){
							DomainStructure domainStructure = hs.subStructure.get(k);
							drawStructure(domainStructure,trueDraw,null, newShaftLength, true, true, deform);	
						}
					}

					if (shaftLength==0){
						popMatrix_drawMolecule();
					}

					/*
					if (hairpinSize!=-1){
						rotate(TWO_PI/(hairpinSize+openingSize));
					}
					*/
					//END OF METHOD
				}
			}
			private void draw3PEnd() {
				fill(0);
				line(0,0,eW/2,0);
				beginShape();
				vertex(eW/2,eW/2);
				vertex(eW*1.5f,0);
				vertex(eW/2,-eW/2);
				endShape();
			}
			private void drawBase(float eW, int domain, int k) {
				if (drawDomainNameOnLine){
					return;
				}
				
				boolean isComp = (domain & NA_COMPLEMENT_FLAG)!=0;
				/**
				 * Note: constraints 
				 */
				char constraint = '-';
				
				constraint = dsd.getConstraint(domain).charAt(isComp?dsd.domainLengths[domain&NA_COMPLEMENT_FLAGINV]-1-k:k);
				
				int sCol = color(0); //the Good color.
				switch(Character.toUpperCase(constraint)){
				case 'G':
					fillA(!isComp?ConstraintColors.G:ConstraintColors.C); break;
				case 'A':
					fillA(!isComp?ConstraintColors.A:ConstraintColors.T); break;
				case 'T': case 'U':
					fillA(!isComp?ConstraintColors.T:ConstraintColors.A); break;
				case 'C':
					fillA(!isComp?ConstraintColors.C:ConstraintColors.G); break;
				case 'P':
					fillA(!isComp?ConstraintColors.H:ConstraintColors.D); break;
				case 'Z':
					fillA(!isComp?ConstraintColors.D:ConstraintColors.H); break;
				case '-': case 'N':
					fillA(ConstraintColors.NONE); break;
				case 'R': case 'Y': case 'M': case 'K': case 'S': case 'W': case 'V': case 'H': case 'B': case 'D':
					fillA(ConstraintColors.R); break;
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
			public boolean dynamicWiggle = true; private long dynamicWiggle_clock = System.nanoTime();
			public boolean drawLineStructure = true;
			public boolean drawDomainNameOnLine = false; //Don't draw lines to represent bases, and draw the domain name in-line
			public boolean showDomainNames = true;
			public boolean drawPolymerGraph = false;
			public boolean drawGrid = true;
			public boolean uniformScale = false;
			public boolean counterRotate = true;
			public boolean rotateToLongestHairpin = true;
			private long keyPressedClock = System.nanoTime();
			public void fillA(float[] color){
				if (drawLineStructure){
					stroke(color[0],color[1],color[2]);
				} else {
					fill(color[0],color[1],color[2]);
				}
			}
			private void markDomain(String domainName, float spacing, boolean trueDraw, MolecularDeformation deform) {
				textFont(ff);
				float scx = screenX(0,0);
				float scy = screenY(0,0);
				if (!trueDraw){
					for(int mult = -1; mult <= 1; mult++){
						particlepositions[particlepoints][0] = (scx+spacing*mult)/width;
						particlepositions[particlepoints][1] = (scy+spacing*mult)/height;
						particlepoints++;
					}
					return;
				}
				if (!showDomainNames){
					return;
				}
				pushMatrix();
				fill(0);
				translate(eW,0);
				if (drawDomainNameOnLine){ //just draw them inline.
				} else {
					//Translate off of the line (y-axis) a bit.
					translate(0,-.1f);
				}
				
				if (deform!=null){
					deform.unrotate();
				}
					
				scale(1/200f);
				//A bit of Linear algebra. Reverse engineer our current rotation and flip.
				final float[] counterRotation = getCounterRotation();
				scale(1,counterRotation[1]);
				rotate(counterRotation[0]);
				if (!domainName.endsWith("*")){
					domainName += " ";
				}
				textAlign(CENTER,CENTER);
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
		
		private Rectangle2D.Float previewArea = new Rectangle2D.Float(0,0,1,1);
		public DnaSequencePreview preview;
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
			if (preview.drawGrid){
				drawGrid();				
			}
			popMatrix();
			pushMatrix();
			strokeWeight(2);
			boolean isSnapshotting = false;
			moleculeScale_override = -1;
			if (needsSnapshot){
				isSnapshotting = true;
				File temp;
				try {
					if(snapshotPath==null){
						temp = File.createTempFile("DNADesignPreview"+System.nanoTime(), ".pdf");
						snapshotPath = temp.getAbsolutePath();
					}
					beginRecord(PDF, snapshotPath);
				} catch (IOException e) {
					e.printStackTrace();
				}
				preview.setDeformation(null);
				pushMatrix();
			}
			if (needsSnapshot || preview.uniformScale){
				float maxScale = 0, minScale = Float.MAX_VALUE; //greater than -1
				for(float[] k : moleculeScales.getEntries()){
					if (k[0] > 0){
						minScale = Math.min(minScale,k[0]);
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

			if (isSnapshotting){
				popMatrix();
				if (true){
					fill(0);
					textFont(ff);
					textAlign(LEFT,TOP);
					String currentMoleculeName = currentMoleculeString.split("\\s+")[0];
					text(currentMoleculeName,2,1);
				}
				endRecord();
				needsSnapshot = false;
				preview.setDeformation(preview.getDefaultDeformation());
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
		private LinkedList<PMatrix> infMatrixStack = new LinkedList();
		private void pushMatrix_drawMolecule(){
			//infMatrixStack.push(g.getMatrix());
			pushMatrix();
			drawMoleculePush++;
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
			//g.setMatrix(infMatrixStack.pop());
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
