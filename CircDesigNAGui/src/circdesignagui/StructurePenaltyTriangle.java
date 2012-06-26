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

import java.awt.Point;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Arrays;

import processing.core.PApplet;
import processing.core.PFont;
import circdesigna.DomainSequence;
import circdesigna.CircDesigNA.ScorePenalty;
import circdesigna.config.CircDesigNAConfig;
import circdesigna.energy.OneMatrixNAFolding;
import circdesigna.impl.CircDesigNAImpl.MFEHybridNonlegalScore;
import circdesigna.impl.CircDesigNAImpl.SelfFoldNonlegalScore;
import circdesigna.impl.CircDesigNAImpl.SelfSimilarityScore;

/**
 * Helper applet for FoldingImplTestGui, displays a detailed graphic of a single objective subscore.
 * @deprecated
 */
public class StructurePenaltyTriangle extends PApplet{
	private ThemedApplet mc;
	private CircDesigNAConfig config;
	private DomainSequence[] curSeqs;
	private double[][] nussinovScores;
	private ArrayList<Point> traceback;
	private int[][] domain;
	private int[][] domain_markings;
	private ScorePenalty sp;
	private OneMatrixNAFolding fil;
	
	private double foldScore;
	public double getEvalScore(){
		return foldScore;
	}
	public void invalidate(){
		super.invalidate();
		loop();
	}
	
	public StructurePenaltyTriangle(ThemedApplet mc, CircDesigNAConfig config){
		this.mc = mc;
		this.config = config;
	}
	public void setPenalty(ScorePenalty sp, int[][] domain_sequences, int[][] nullMarkings, OneMatrixNAFolding fil){
		this.fil = fil;
		domain = domain_sequences;
		domain_markings = nullMarkings;
		this.sp = sp;
		throw new RuntimeException();
		/*
		if (sp instanceof MFEHybridNonlegalScore || sp instanceof SelfFoldNonlegalScore || sp instanceof SelfSimilarityScore){
			curSeqs = sp.getSeqs();
		} else {
			curSeqs = null;
		}
		evalTriangle();
		redraw();
		*/
	}
	private void evalTriangle() {
		if (curSeqs!=null){
			for(int[] row : domain_markings){
				Arrays.fill(row,0);
			}
		}
		double score = sp.evalScoreSub(domain, domain_markings);
		if (curSeqs!=null){
			int len1, len2;
			len2 = len1 = curSeqs[0].length(domain);
			if (sp instanceof MFEHybridNonlegalScore){
				len2 = curSeqs[1].length(domain);
				//score = fil.mfeHybridDeltaG_viaMatrix(curSeqs[0],curSeqs[1],domain,domain_markings);
			}
			if (sp instanceof SelfFoldNonlegalScore){
				//score = fil.mfeSSDeltaG(curSeqs[0],domain,domain_markings);
			}
			traceback = fil.getTraceback();
			nussinovScores = null;
			if (len1 < 1000 && len2 < 1000){
				nussinovScores = fil.getScoreMatrix(len1,len2);
			}
		}
		//int[][][] structureMatrix = fil.getMFEStructureMatrix();
		
		foldScore = score;
	}
	private PFont ff;
	public void setup(){
		ff = createFont("Arial", 20);
		size(100, 100, P2D);
	}

	private void fillByMarker(DomainSequence seq1, int y, int[][] domain2) {

		int cD = seq1.domainAt(y, domain) & DomainSequence.NA_COMPLEMENT_FLAGINV;
		int off = seq1.offsetInto(y, domain, true);
		if (domain_markings[cD][off]!=0){
			fill(255,0,0);
		} else {
			fill(0);
		}
	}
	public void draw(){
		noLoop();
		background(255);
		pushMatrix();
		stroke(mc.THEMECOL4.getRGB());
		scale(width,height);
		if (curSeqs!=null && curSeqs.length>0){
			DomainSequence seq1 = curSeqs[0], seq2 = curSeqs[0];
			if (curSeqs.length>1){
				seq2 = curSeqs[1];
			}
			int len1 = seq1.length(domain);
			int len2 = seq2.length(domain);
			scale((float)len2/Math.max(len2,len1),(float)len1/Math.max(len2,len1));
			Rectangle2D.Float area = new Rectangle2D.Float(1f/len2,1f/len1,1-1f/len2,1-1f/len1);
			//Draw axes.
			{
				pushMatrix();
				translate(0,area.y);
				fill(0);
				textFont(ff);
				textAlign(CENTER, CENTER);
				for(int y = 0; y < len1; y++){	
					pushMatrix();
					scale(1f/len2,1f/len1);
					translate(.5f,.5f);
					scale(10f/width,10f/height);
					fillByMarker(seq1,y,domain);
					text(""+config.monomer.displayBase(seq1.base(y, domain, config.monomer)),0,0);
					popMatrix();
					translate(0,area.height/len1);
				}
				popMatrix();

				pushMatrix();
				translate(area.x,0);
				textFont(ff);
				textAlign(CENTER, CENTER);
				for(int x = 0; x < len2; x++){
					pushMatrix();
					scale(1f/len2,1f/len1);
					translate(.5f,.5f);
					scale(10f/width,10f/height);
					fillByMarker(seq2,x,domain);
					text(""+config.monomer.displayBase(seq2.base(x, domain, config.monomer)),0,0);
					popMatrix();
					translate(area.width/len2,0);
				}
				popMatrix();
			}
			//Draw triangle
			try {
				translate(area.x,area.y);
				scale(area.width,area.height);
				scale(1f/len2,1f/len1);
				for(int y = 0; y < len1; y++){
					for(int x = 0; x < len2; x++){
						noStroke();
						float a;
						if (nussinovScores!=null){
							a = (float)-nussinovScores[y][x]*255 /(1+(float)foldScore);
						} else {
							a = 0;
						}
						fill(20,40,100,a);
						ellipseMode(CORNER);
						if (curSeqs.length==2 || y < x){
							rect(x,y,1,1);
						} else {
							fill(100,100,100);
							rect(x,y,1,1);
						}
					}
				}
				translate(.5f,.5f);
				
				stroke(0);
				strokeWeight(.5f);
				noFill();
				Point lastPoint = null;
				if (traceback!=null){
					for(Point t : traceback){
						if (t!=null && lastPoint!=null){
							line(lastPoint.y,lastPoint.x,t.y,t.x);
						}
						lastPoint = t;
					}
				}
			} catch (Throwable e){
				//
			}
		} else {
			fill((float)foldScore*10,0,0);
			rect(0,0,1,1);
		}
		popMatrix();
	}
}
