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


import static circdesigna.GSFR.NA_COMPLEMENT_FLAGINV;

import java.awt.event.MouseEvent;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.TreeMap;

import processing.core.PApplet;
import processing.core.PFont;
import processing.core.PMatrix;
import circdesigna.CircDesigNAStyle;
import circdesigna.DomainDefinitions;
import circdesigna.DomainPolymerGraph;
import circdesigna.DomainStructureBNFTree;
import circdesigna.DomainStructureBNFTree.HairpinStem;
import circdesigna.DomainStructureBNFTree.ThreePFivePOpenJunc;
import circdesigna.config.CircDesigNAConfig;
import circdesigna.exception.InvalidDNAMoleculeException;
import circdesigna.exception.InvalidDomainDefsException;
import circdesigna.geometry.CircumscribedPolygonTool;
import circdesigna.geometry.CircumscribedPolygonTool.CircumscribedPolygon;

/**
 * An embedded PApplet for displaying molecule previews. A number of keystrokes
 * make this applet interactive. Mouse dragging also moves the screen. See the in-applet help file for
 * the molecules window for more information.
 * @author Benjamin
 */
public class MoleculePreview extends PApplet{
	private static final long INPUT_CLOCK_INT = (long) .3e9;
	public MoleculePreview(ThemedApplet mc, CircDesigNAConfig config) {
		this.config = config;
		this.mc = mc;
	}
	private ThemedApplet mc;
	private CircDesigNAConfig config;

	private ScaleFactors<float[]> moleculeScales = new ScaleFactors<float[]>();
	private Object makeMolecularScale(){
		return new float[]{-1,-1};
	}
	public CircDesigNAConfig getConfig() {
		return config;
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
	public void setCurrentPreviewMolecule(int molecules_CLine_num, int numMolecules, String moleculeDescription, String domainDefs, boolean harsh){
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
		screen1.preview.updateMolecule(harsh);
	}
	public void snapShot(String snapshotDir){
		needsSnapshot = true;
		this.snapshotDir = snapshotDir;
	}
	public void snapShot(){
		snapShot(snapshotDir);
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
	private String snapshotDir = null, snapshotPath = null;
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
				ff = createFont("Arial", 16);
			}
			private PFont ff;
			private float moleculeScale = 1f, moleculeScaleMax = 1f, moleculeScale_override = -1;
			public class DnaSequencePreview {
				public void updateMolecule(boolean harsh){
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
							parseMolecule();
							//How many particles?
							/*
						moleculeNumSubStructures = 0;
						for(DomainStructure ds : dsg.structures){
							moleculeNumSubStructures += ds.countLabeledElements();
						}
						moleculeNumSubStructures*=3;
							 */
							if (harsh){
								hasInitialParticleConfiguration = false;
								Arrays.fill(previewAreaDrag,0);
								moleculeScale = 1f;
								moleculeScaleMax = 1f;
							}
							needsCurrentMoleculeUpdate = false;
						} finally {
							stopTheWorld[0] = 0;
						}
						throw new UpdateSuccessfulException();
					}
				}
				public void parseMolecule() {
					int oldWorld = stopTheWorld[0];
					if (oldWorld == 0){
						stopTheWorld[0] = 1;
					}
					try {
						DomainStructureBNFTree.readStructure(currentMoleculeString, dsg);
						DomainPolymerGraph.readStructure(currentMoleculeString, dpg);
					} catch (Throwable e){
						//e.printStackTrace();
						//dsg.structures = null;
						throw new InvalidDNAMoleculeException(e.getMessage(),0);
					} finally {
						if (oldWorld == 0){
							stopTheWorld[0] = 0;
						}
					}
				}
				private DomainDefinitions dsd = new DomainDefinitions(config);
				private HashMap<Integer, float[]> connectorMap = new HashMap(); //For drawing pseudoknotted structures.
				public DomainStructureBNFTree dsg = new DomainStructureBNFTree(dsd);
				public DomainPolymerGraph dpg = new DomainPolymerGraph(dsd);
				private float[] renderPolymerGraph_LengthCache = null; //For storing partial indexes of domains
				private int[] stopTheWorld = new int[]{0};
				public void draw(){
					try {
						/*
				if (dsg.structures==null){
					return;
				}
						 */
						if (stopTheWorld[0]!=0){
							return;
						}
						particlepositions = new float[10000][4];
						/*
				if (moleculeNumSubStructures > particlepositions.length){
					particlepositions = new float[moleculeNumSubStructures][4];
				}
						 */
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
										drawDomainNames = true;
									}
								}
							}
							if (keyEvent.getKeyChar() == 'd'){
								if (System.nanoTime()-keyPressedClock>INPUT_CLOCK_INT){
									keyPressedClock = System.nanoTime();

									drawDomainPositionsAsLabels = !drawDomainPositionsAsLabels;
								}
							}	
							if (keyEvent.getKeyChar() == 'a'){
								if (System.nanoTime()-keyPressedClock>INPUT_CLOCK_INT){
									keyPressedClock = System.nanoTime();

									drawDomainNames = !drawDomainNames;
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
							if (keyEvent.getKeyChar() == 'd'){
								if (System.nanoTime()-keyPressedClock>INPUT_CLOCK_INT){
									keyPressedClock = System.nanoTime();

									drawDomainPositionsAsLabels = !drawDomainPositionsAsLabels;
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
						drawForeground(false);
						if (!hasInitialParticleConfiguration){
							Arrays.fill(averagePositions,0);
							//compute averages, rotation.
							for(int k = 0; k < particlepoints; k++){
								averagePositions[0] += particlepositions[k][0];
								averagePositions[1] += particlepositions[k][1];
							}
							averagePositions[0] /= particlepoints;
							averagePositions[1] /= particlepoints;
						}
						float avgx = averagePositions[0];
						float avgy = averagePositions[1];
						float[] q = new float[4];
						float maxX = 0;
						float maxY = 0;
						for(int k = 0; k < particlepoints; k++){
							particlepositions[k][0] -= avgx;
							particlepositions[k][1] -= avgy;
							//Rotate in counterrotation:
							rotateVec(particlepositions[k],longestHairpin_counterrotation[0]);
							maxX = max(maxX,abs(particlepositions[k][0])*2);
							maxY = max(maxY,abs(particlepositions[k][1])*2);
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
							moleculeScaleMax = .9f/(max(maxX,maxY));
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
						translate(-avgx,-avgy);
						if (counterRotate){
							rotate(theta);
						}
						drawForeground(true);
						popMatrix();
						hasInitialParticleConfiguration = true;

					}catch (Throwable e){
						e.printStackTrace();
						stop();
					}
				}
				//private int moleculeNumSubStructures = 0;
				private float[] averagePositions = new float[4];
				private float[][] particlepositions = new float[0][0];
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
					return new MolecularDeformation(MoleculePreview.this);
				}
				private void drawForeground(boolean actuallyDraw){
					pushMatrix_drawMolecule();
					try {
						if (drawPolymerGraph){
							drawPolymerGraph(actuallyDraw);
						} else {
							drawMolecule(actuallyDraw);
							/*
							if (dsg.outerCurve!=null){
								dsg.outerCurve.resetCounter();
								dsg.outerCurve.next();
							}
							sampleDeform.reset();
							for(DomainStructure ds : dsg.structures){
								drawStructure(ds, actuallyDraw, dsg.outerCurve, 0, true, sampleDeform);
							}
							*/
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
									translate(0, -eW);
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
									drawDomainLabel(i, dsd.getDomainName(id),0,actuallyDraw, null);
								}
								popMatrix_drawMolecule();
							}	
						}
					}
					popMatrix_drawMolecule();
				}

				final float eW = .03f;
				final float icD = .03f;
				private void drawMolecule(boolean trueDraw) {
					MolecularDeformation deform = sampleDeform;
					int pivot = -1;
					for(int i = 0; i < dpg.length(); i++){
						if (dpg.getDomainPair(i) >= 0){
							pivot = i;
						}
					}
					if (pivot == -1){
						//Single stranded structure.
						if (dpg.length()>0){
							drawMoleculeSS(0, dpg.length()-1, trueDraw, deform);
						}
					} else {
						pushMatrix_drawMolecule();
						drawMoleculeStem(pivot, trueDraw, deform);
						popMatrix_drawMolecule();
						rotate(PI);
						drawMoleculeLoop(dpg.getDomainPair(pivot), trueDraw, deform);
					}
				}
				private void drawMoleculeSS(int iBegin, int iFinal, boolean trueDraw, MolecularDeformation deform) {
					int N = dpg.length();
					float openingSize = HairpinStem.openingSize*eW*2;
					boolean lastWasHelix = false;
					for(int i = iBegin; ; i=(i+1)%N){
						if (dpg.getDomainPair(i) != -1){
							int j = dpg.getDomainPair(i);
							int domainI = dpg.getDomain(i);
							int domainJ = dpg.getDomain(j);
							CircDesigNAStyle stylei = dpg.getStyle(i);
							if (trueDraw){
								drawBase(0, stylei);
							}
							CircDesigNAStyle stylej = dpg.getStyle(j);
							int next = (j+1)%N;
							if (dpg.getDomain(next)==-1){
								//We have nothing to our right, so give ourselves more berth and just continue on ahead
								rotate(HALF_PI);
							} else {
								if (lastWasHelix) {
									rotate(PI/4); //Dangerous! If this happens too many times the helices start overlapping...
								}
							}
							pushMatrix_drawMolecule();
							translate(openingSize/2, 0);
							rotate(-HALF_PI);
							translate(0, -openingSize/2);
							if (trueDraw){
								drawBase(eW, stylei);
							}
							translate(0, openingSize);
							if (trueDraw){
								drawBase(eW, stylej);
							}
							translate(0, -openingSize/2);
							translate(eW, 0);
							drawMoleculeStem(i, trueDraw, deform); //draw stem
							popMatrix_drawMolecule();
							translate(openingSize, 0);
							i = j;
							lastWasHelix = true;
						} else {
							//unpaired single stranded straight
							int domain = dpg.getDomain(i);
							CircDesigNAStyle stylei = dpg.getStyle(i);
							if (trueDraw){
								drawBase(0, stylei);
							}
							if (domain == -1){
								int last = (i-1+N)%N;
								if (dpg.getDomainPair(last)!=-1){
									//Attach three prime end to last stem
									rotate(HALF_PI);
								}
								if (trueDraw){
									draw3PEnd();
								}
							} else {
								//At the start, too, if conNumber is maximal
								if (stylei.conNumber == Integer.MAX_VALUE){
									handleConnector(i, trueDraw, deform);
								}
								
								int seqLen = dsd.getDomainLength(domain);
								for(int k = 0; k < seqLen; k++){
									if (k == seqLen / 2 && stylei.conNumber == 0){ //Don't draw domain labels for connectors
										pushMatrix_drawMolecule();
										translate(seqLen%2==1?eW:0,0);
										drawDomainLabel(i, dsd.getDomainName(domain),(seqLen+1)/2f*eW*2, trueDraw, deform);
										popMatrix_drawMolecule();
									}
									if (trueDraw){
										drawBase(eW*2, stylei);
									}
									translate(eW*2, 0);
								}
								//At the end of the 
								handleConnector(i, trueDraw, deform);
							}
							lastWasHelix = false;
						}
						if (i == iFinal){
							break;
						}
					}
				}
				private void handleConnector(int i, boolean trueDraw, MolecularDeformation deform) {
					int domain = dpg.getDomain(i);
					CircDesigNAStyle stylei = dpg.getStyle(i);
					int seqLen = dsd.getDomainLength(domain);
					if (stylei.conNumber > 0 && seqLen > 1){ //the seqLen>1 thing is weird.
						if (connectorMap.containsKey(stylei.conNumber)){
							pushMatrix_drawMolecule();
							//The second should connect at the bottom, so translate our position up halfwidth
							translate(0,openingSize/2+eW/2);
							
							float[] cPos = new float[]{screenX(0, 0), screenY(0, 0), i};
							//Draw the stem connector
							float[] nPos = connectorMap.remove(stylei.conNumber);
							int j = (int)nPos[2];
							float[] dPos = new float[]{cPos[0] - nPos[0], cPos[1] - nPos[1]};
							rotateToIdentity();
							float cScl = getCounterScale();
							translate(-dPos[0]*cScl, -dPos[1]*cScl);
							float len = mag(dPos[0], dPos[1])*cScl;
							rotate(atan2(dPos[1], dPos[0]));
							//Custom gap, so that gap*(seqLen-1) = the correct length
							drawStem_(i, j, len/(seqLen-1), trueDraw, deform);
							popMatrix_drawMolecule();
						} else {
							pushMatrix_drawMolecule();
							translate(0,-openingSize/2-eW/2);
							float[] cPos = new float[]{screenX(0, 0), screenY(0, 0), i};
							connectorMap.put(stylei.conNumber, cPos);
							popMatrix_drawMolecule();
						}
					}
				}
				private float openingSize = HairpinStem.openingSize*eW*2;
				private void drawStem_(int i, int j, float gap, boolean trueDraw, MolecularDeformation deform) {
					int domain = dpg.getDomain(i);
					int domain2 = dpg.getDomain(j);
					CircDesigNAStyle stylei = dpg.getStyle(i).duplicate();
					CircDesigNAStyle stylej = dpg.getStyle(j).duplicate();

					int seqLen = dsd.getDomainLength(domain);
					if (seqLen == 0){
						throw new RuntimeException("Do not call drawMoleculeStem on a zero length stem!");
					}

					if (stylei.conNumber == 0){
						pushMatrix_drawMolecule();
						try {
							translate(gap*(seqLen-1)/2f, 0);
							translate(0,-openingSize/2);
							drawDomainLabel(i, dsd.getDomainName(domain),seqLen/2f*eW*2, trueDraw, deform);
							translate(0,openingSize);
							rotate(PI);
							drawDomainLabel(j, dsd.getDomainName(domain2),seqLen/2f*eW*2, trueDraw, deform);
						} finally {
							popMatrix_drawMolecule();
						}
					}

					
					if (stylei.conNumber > 0){
						//Get rid of the connumbers
						stylei.conNumber = 0;
						stylej.conNumber = 0;
						translate(-eW*1.5f,-openingSize/2);
						if(trueDraw){
							drawBase(eW*3 + gap*(seqLen-1),stylei);
						}
						translate(0,openingSize);
						if(trueDraw){
							drawBase(eW*3 + gap*(seqLen-1),stylej);
						}
						translate(eW*1.5f,-openingSize/2);	
					} else {
						//Get rid of the connumbers
						stylei.conNumber = 0;
						stylej.conNumber = 0;
						translate(0,-openingSize/2);
						if(trueDraw){
							drawBase(gap*(seqLen-1),stylei);
						}
						translate(0,openingSize);
						if(trueDraw){
							drawBase(gap*(seqLen-1),stylej);
						}
						translate(0,-openingSize/2);
					}
				
					int col = color(0,0,0, min(stylei.color.getAlpha(), stylej.color.getAlpha()));
					stroke(col);
					fill(col);
					for(int k = 0; k < seqLen; k++){
						//hydrogen bonding
						if (trueDraw){
							ellipseMode(CENTER);
							ellipse(0, 0, icD, icD);
						}
						if (k != seqLen - 1){
							translate(gap, 0);
						}
					}
				}
				private void drawMoleculeStem(int i, boolean trueDraw, MolecularDeformation deform) {
					int j = dpg.getDomainPair(i);
					drawStem_(i, j, eW*2, trueDraw, deform);
					//Interior.
					drawMoleculeLoop(i, trueDraw, deform);
				}
				private void drawMoleculeLoop(int i, boolean trueDraw, MolecularDeformation deform) {
					int j = dpg.getDomainPair(i);
					//Finish off the last stem
					float openingSize = HairpinStem.openingSize*eW*2;
					int domain = dpg.getDomain(i);
					CircDesigNAStyle stylei = dpg.getStyle(i);
					int domain2 = dpg.getDomain(j);
					CircDesigNAStyle stylej = dpg.getStyle(j);
					translate(0,-openingSize/2);
					if(trueDraw){
						drawBase(eW,stylei);
					}
					translate(0,openingSize);
					if(trueDraw){
						drawBase(eW,stylej);
					}
					translate(0,-openingSize/2);
					translate(eW, 0);
					if (trueDraw){
						drawBase(0, stylei);
					}

					//Iterate over the loop interior, compute the circumference
					int threePrimeEnds = 0;
					int nick = -1;
					int unpairedBasesInLeft = 0;
					int unpairedBasesInRight = 0;
					float circumOnRight = 0;
					boolean hasAdjacentStems = false;
					int N = dpg.length();
					ArrayList<Float> loopElements = new ArrayList();
					loopElements.add(HairpinStem.openingSize*2*eW);
					for(int k = (i+1)%N; k != j; k = (k+1)%N){
						if (dpg.getDomain(k) == -1){
							loopElements.add(-1f*ThreePFivePOpenJunc.size*2*eW);
							threePrimeEnds++;
							nick = k;
							if (threePrimeEnds >= 2){
								throw new RuntimeException("Disconnected Structure!");
							}
						} else {
							int pairk = dpg.getDomainPair(k);
							if (pairk == -1){
								int domainLen = dsd.getDomainLength(dpg.getDomain(k));
								loopElements.add(-1f*domainLen*2*eW);
								if (threePrimeEnds == 0){
									unpairedBasesInLeft += domainLen;
								} else {
									unpairedBasesInRight += domainLen;
									circumOnRight += domainLen * 2 * eW;
								}
							} else {
								int last = (k-1+N)%N;
								if (last != i){
									if (dpg.getDomainPair(last) != -1){
										//Two stems in a row on the interior of this loop - can't draw these using drawMoleculeSS.
										hasAdjacentStems = true;
									}
								}

								loopElements.add(HairpinStem.openingSize*2*eW);
								if (threePrimeEnds == 0){
								} else {
									circumOnRight += HairpinStem.openingSize*2*eW;
								}
								k = pairk;
							}
						}
					}

					boolean isAwkwardSS = hasAdjacentStems && (unpairedBasesInLeft + unpairedBasesInRight > 0);

					if (threePrimeEnds > 0){
						boolean leftBeginsWithSS = true;
						if (dpg.getDomainPair((i+1)%N)!=-1){
							leftBeginsWithSS = false;
						}
						boolean leftBeginsWithStem = !leftBeginsWithSS;

						boolean rightBeginsWithSS = true;
						if (dpg.getDomainPair((j-1+N)%N)!=-1){
							rightBeginsWithSS = false;
						}
						boolean rightBeginsWithStem = !rightBeginsWithSS;

						//Special cases for nicked loops
						boolean leftIsSS = false;
						boolean leftIsEmpty = true;
						{
							for(int f = (i+1)%N; ; f = (f+1)%N){
								if (dpg.getDomain(f)==-1){
									leftIsSS = true;
									break;//Good end.
								} else {
									leftIsEmpty = false;
									if (dpg.getDomainPair(f)!=-1){
										break; //bad end. left has a stem.
									}
								}
							}
						}
						boolean rightIsSS = false;
						boolean rightIsEmpty = true;
						{
							for(int f = (j-1+N)%N; ; f = (f-1+N)%N){
								if (dpg.getDomain(f)==-1){
									rightIsSS = true;
									break;//Good end.
								} else {
									rightIsEmpty = false;
									if (dpg.getDomainPair(f)!=-1){
										break; //bad end. right has a stem.
									}
								}
							}
						}

						boolean leftIs3pEnd = dpg.getDomain((i+1)%N) == -1;
						boolean leftIsStemAndSS = false;
						boolean leftAfterStemIsEmpty = true;
						{
							int d = (i+1)%N;
							if (dpg.getDomain(d)!=-1){
								int e = dpg.getDomainPair(d);
								if (e != -1){
									for(int f = (e+1)%N; ; f = (f+1)%N){
										if (dpg.getDomain(f)==-1){
											leftIsStemAndSS = true;
											break;//Good end.
										} else {
											leftAfterStemIsEmpty = false;
											if (dpg.getDomainPair(f)!=-1){
												break; //bad end. There are stems after the first.
											}
										}
									}
								}
							}
						}
						boolean rightIsStemAndSS = false;
						boolean rightAfterStemIsEmpty = true;
						{
							int e = (j-1+N)%N;
							if (dpg.getDomain(e)!=-1){
								int d = dpg.getDomainPair(e);
								if (d != -1){
									for(int f = (d-1+N)%N; ; f = (f-1+N)%N){
										if (dpg.getDomain(f)==-1){
											rightIsStemAndSS = true;
											break;//Good end.
										} else {
											rightAfterStemIsEmpty = false;
											if (dpg.getDomainPair(f)!=-1){
												break; //bad end. There are stems after the first.
											}
										}
									}
								}
							}
						}

						if (leftIs3pEnd && rightBeginsWithSS && !isAwkwardSS){
							//draw left straight out, right at 90 degrees
							translate(0, -openingSize / 2);
							if (trueDraw){
								draw3PEnd();
							}
							translate(0, openingSize);
							rotate(HALF_PI);
							translate(circumOnRight, 0);
							rotate(PI);
							if (dpg.getDomain((j-1+N)%N) != -1){
								drawMoleculeSS((nick+1)%N, (j-1+N)%N, trueDraw, deform);
							}
							return;
						}
						if (leftBeginsWithStem && rightBeginsWithSS){
							pushMatrix_drawMolecule();
							translate(eW, 0);
							int d = (i+1)%N;
							CircDesigNAStyle styled = dpg.getStyle(d);
							//Draw stem straight out
							drawMoleculeStem(d, trueDraw, deform);
							popMatrix_drawMolecule();
							pushMatrix_drawMolecule();
							translate(0, -openingSize/2);
							if (trueDraw){
								drawBase(eW, styled);
							}
							translate(0, openingSize);
							translate(eW, 0);
							int e = dpg.getDomainPair(d);
							if (leftIsStemAndSS){
								if (unpairedBasesInLeft > 0){
									rotate(3*PI/4);
								} else {
									rotate(HALF_PI); //To make the 3prime end work out correctly
								}
							} else {
								rotate(HALF_PI); //For complex stem-subsequents, give more berth
							}
							drawMoleculeSS((e+1)%N, nick, trueDraw, deform);
							popMatrix_drawMolecule();
							translate(0, openingSize/2);
							if (rightIsSS){
								rotate(PI/4);
							} else {
								rotate(HALF_PI); //If the right is complex, give it more space
							}
							translate(circumOnRight, 0);
							rotate(PI);
							if (dpg.getDomain((j-1+N)%N) != -1){
								drawMoleculeSS((nick+1)%N, (j-1+N)%N, trueDraw, deform);
							}
							return;
						}
						if (rightIsStemAndSS && leftBeginsWithSS){
							//draw the stem straight out, the subsequent single stranded is angled at 45 degrees, coming back in this direction
							int e = (j-1+N)%N;
							int d = dpg.getDomainPair(e);
							CircDesigNAStyle stylee = dpg.getStyle(e);

							pushMatrix_drawMolecule();
							translate(eW, 0);
							drawMoleculeStem(d, trueDraw, deform);
							popMatrix_drawMolecule();
							pushMatrix_drawMolecule();
							translate(0, openingSize/2);
							if (trueDraw){
								drawBase(eW, stylee);
							}
							translate(0, -openingSize);
							translate(eW, 0);
							rotate(-3*PI/4);
							translate(unpairedBasesInRight*2*eW, 0);
							rotate(PI);
							//draw the right
							if (dpg.getDomain((d-1+N)%N) != -1){
								drawMoleculeSS((nick+1)%N, (d-1+N)%N, trueDraw, deform);
							}
							popMatrix_drawMolecule();
							translate(0, -openingSize/2);
							if (leftIsSS){
								if (unpairedBasesInLeft > 0){
									rotate(-PI/4);
								}
							} else {
								rotate(-HALF_PI); //If the left is complex, give it more space
							}
							drawMoleculeSS((i+1)%N, nick, trueDraw, deform);
							return;
						}	

						if (leftIsSS && rightIsSS){
							pushMatrix_drawMolecule();
							translate(0, -openingSize/2);
							if (unpairedBasesInLeft > 0){
								rotate(-PI/4);
							}
							drawMoleculeSS((i+1)%N, nick, trueDraw, deform);
							popMatrix_drawMolecule();
							translate(0, openingSize/2);
							rotate(PI/4);
							translate(unpairedBasesInRight*2*eW, 0);
							rotate(PI);
							if (dpg.getDomain((j-1+N)%N) != -1){
								drawMoleculeSS((nick+1)%N, (j-1+N)%N, trueDraw, deform);
							}
							return;
						}

					}
					//If this is reached, then use the circumference value to draw the loop.
					//This method can draw all possible loops.

					/*
				if (trueDraw){
					ellipseMode(CENTER);
					ellipse(0,0, icD*4, icD*4);
				}
					 */
					CircumscribedPolygon cp = new CircumscribedPolygon();
					cp.S = new float[loopElements.size()];
					for(int k = 0; k < loopElements.size(); k++){
						cp.S[k] = loopElements.get(k);
					}
					CircumscribedPolygonTool.solvePolygonProblem(cp);
					drawMoleculeLoopOnCircle(cp, i, trueDraw, deform);
				}
				private void drawMoleculeLoopOnCircle(CircumscribedPolygon cp, int i, boolean trueDraw, MolecularDeformation deform) {
					cp.resetCounter();
					float radius = cp.R;
					float openingSize = HairpinStem.openingSize*eW*2;
					float hairpinOpeningAdjust = radius - sqrt(radius * radius - sq(openingSize/2));
					int N = dpg.length();
					int j = dpg.getDomainPair(i);

					translate(radius-hairpinOpeningAdjust, 0);
					rotate(PI);
					rotate(cp.nextArc() / 2); //Half of the initial hairpin.
					//Draw the parts of the loop
					for(int k = (i+1)%N; k != j; k = (k+1)%N){
						float arc = cp.nextArc();
						if (dpg.getDomain(k) == -1){ //3-prime end
							int last = (k-1+N)%N;
							if (dpg.getDomain(last) != -1 && dpg.getDomainPair(last) != -1){
								//Attach ourselves to the last hairpin
								pushMatrix_drawMolecule();
								translate(radius, 0);
								rotate(PI);
								if(trueDraw){
									draw3PEnd();
								}
								popMatrix_drawMolecule();
							} else {
								pushMatrix_drawMolecule();
								translate(radius, 0);
								rotate(HALF_PI);
								if(trueDraw){
									draw3PEnd();
								}
								popMatrix_drawMolecule();
							}
						} else {
							int pairk = dpg.getDomainPair(k);
							CircDesigNAStyle stylek = dpg.getStyle(k);
							if (pairk == -1){ //Unpaired
								int domainK = dpg.getDomain(k);
								pushMatrix_drawMolecule();
								rotate(arc/2);
								translate(radius, 0);
								rotate(HALF_PI);
								drawDomainLabel(k, dsd.getDomainName(domainK), radius, trueDraw, deform);
								popMatrix_drawMolecule();
								if(trueDraw){
									int domainLen = dsd.getDomainLength(dpg.getDomain(k));
									//TODO: color the domain correctly
									noFill();
									ellipseMode(CENTER);
									noFill();
									drawBase(0, stylek);
									if (stylek.conNumber == 0){
										arc(0, 0, 2*radius, 2*radius, 0, arc);
									}
								}
								pushMatrix_drawMolecule();
								rotate(arc);
								translate(radius, 0);
								handleConnector(k, trueDraw, deform);
								popMatrix_drawMolecule();
							} else {
								pushMatrix_drawMolecule();
								rotate(arc/2);
								translate(radius-hairpinOpeningAdjust, 0);
								int domainK = dpg.getDomain(k);
								int domainKPair = dpg.getDomain(dpg.getDomainPair(k));
								CircDesigNAStyle stylekpair = dpg.getStyle(dpg.getDomainPair(k));
								translate(0, -openingSize/2);
								if (trueDraw){
									drawBase(eW, stylek);
								}
								translate(0, openingSize);
								if (trueDraw){
									drawBase(eW, stylekpair);
								}
								translate(0, -openingSize/2);
								translate(eW, 0);
								drawMoleculeStem(k, trueDraw, deform);
								popMatrix_drawMolecule();
								k = pairk;
							}
						}
						rotate(arc);
					}
				}
				private void draw3PEnd() {
					fill(0);
					line(0,0,-eW*2,-eW*2);
				}
				private void drawBase(float len, CircDesigNAStyle style) {
					if (drawDomainNameOnLine){
						return;
					}
					if (style.conNumber > 0){
						return;
					}
					//int col = lerpColor(g.backgroundColor, color(style.color.getRed(), style.color.getGreen(), style.color.getBlue()), style.color.getAlpha()/255.f);
					stroke(style.color.getRed(), style.color.getGreen(), style.color.getBlue(), style.color.getAlpha());
					//stroke(col);
					//TODO: bold etcetera
					if (len > 0){
						line(0,0,len,0);
					}
				}
				/*
			private void drawBase(float len, int domain, int k) {
				if (drawDomainNameOnLine){
					return;
				}

				boolean isComp = (domain & NA_COMPLEMENT_FLAG)!=0;
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
				if (Character.isLowerCase(constraint)){
					line(0,eW/4,len,eW/4);
					line(0,-eW/4,len,-eW/4);
				} else {
					line(0,0,len,0);
				}
			}
				 */

				public boolean dynamicWiggle = true; private long dynamicWiggle_clock = System.nanoTime();
				public boolean drawLineStructure = false;
				public boolean drawDomainNameOnLine = false; //Don't draw lines to represent bases, and draw the domain name in-line
				public boolean drawDomainNames = true;
				public boolean drawPolymerGraph = false;
				public boolean drawDomainPositionsAsLabels = false; //Instead of drawing domain names, draw numbers 0 through dpg.length()
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
				private void drawDomainLabel(int position, String domainName, float spacing, boolean trueDraw, MolecularDeformation deform) {
					if (drawDomainPositionsAsLabels){
						domainName = ""+position;
					}
					
					pushMatrix_drawMolecule();
					if (drawDomainNameOnLine){ //just draw them inline.
					} else {
						//Translate off of the line (y-axis) a bit.
						translate(0,-.2f);
					}
					fill(0);

					textFont(ff);
					if (!trueDraw){
						for(int mult = -1; mult <= 1; mult++){
							pushMatrix_drawMolecule();
							translate(spacing * mult, 0);
							particlepositions[particlepoints][0] = screenX(0,0)/width;
							particlepositions[particlepoints][1] = screenY(0,0)/height;
							particlepoints++;
							popMatrix_drawMolecule();
						}
					}

					if (deform!=null && !deform.isNullTransform()){
						deform.unrotate();
					}

					if (trueDraw){
						//translate(eW, 0);

						scale(1/100f);
						//A bit of Linear algebra. Reverse engineer our current rotation and flip.
						rotateToIdentity();
						if (!domainName.endsWith("*")){
							domainName += " ";
						}
						if (drawDomainNames){
							textAlign(CENTER,CENTER);
							text(domainName, 0, 0);
						}
					}
					popMatrix_drawMolecule();
				}
				private void rotateToIdentity() {
					final float[] counterRotation = getCounterRotation();
					scale(1,counterRotation[1]);
					rotate(counterRotation[0]);
				}
				private float getCounterScale(){
					float dSx = screenX(1,0)-screenX(0,1);
					float dSy = screenY(1,0)-screenY(0,1);
					//Distance should be sqrt (2), if the transformation is purely planar.
					return sqrt(2/(dSx*dSx + dSy*dSy));
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
				String currentMoleculeName = (currentMoleculeString+" .").split("\\s+")[0];
				pushMatrix();
				scale(width,height);
				stroke(0);
				noFill();
				if (preview.drawGrid){
					drawGrid();				
				}
				popMatrix();
				pushMatrix();
				float strokeWidth;
				if (preview.drawPolymerGraph){
					strokeWidth = 2;
				} else {
					strokeWidth = 4;
				}
				strokeWeight(strokeWidth);
				boolean isSnapshotting = false;
				moleculeScale_override = -1;
				if (needsSnapshot){
					isSnapshotting = true;
					File temp;
					try {
						if(snapshotDir==null){
							String pathToTmp = File.createTempFile("DNADesignPreview"+System.nanoTime(), ".tmp").getPath();
							int len = pathToTmp.length();
							File pathToTmpDir = new File(pathToTmp.substring(0,len-4));
							pathToTmpDir.mkdir();

							snapshotDir = pathToTmpDir.getAbsolutePath();
						}
						String sanitizedMolname = currentMoleculeName.replaceAll("[\\\\\\/:\\*\\?\"\'<>|]", "_");
						snapshotPath = snapshotDir+"/"+sanitizedMolname+".pdf";
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
				}
				if (needsSnapshot){
					strokeWeight(strokeWidth/192);
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
				while(drawMoleculePush > 0){
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

		/*
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
		 */
}
