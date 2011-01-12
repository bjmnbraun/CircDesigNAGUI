package DnaDesignGUI;

import java.awt.Point;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Arrays;

import processing.core.PApplet;
import processing.core.PFont;
import DnaDesign.DnaDefinition;
import DnaDesign.DomainSequence;
import DnaDesign.DomainDesigner.ScorePenalty;
import DnaDesign.impl.FoldingImpl;
import DnaDesign.impl.DomainDesignerImpl.CrossInteraction;
import DnaDesign.impl.DomainDesignerImpl.SelfFold;

public class StructurePenaltyTriangle extends PApplet{
	private DnaDesignGUI_ThemedApplet mc;
	private DomainSequence[] curSeqs;
	private double[][] nussinovScores;
	private ArrayList<Point> traceback;
	private int[][] domain;
	private int[][] domain_markings;
	private ScorePenalty sp;
	private FoldingImpl fil;
	
	private double foldScore;
	
	public void invalidate(){
		super.invalidate();
		loop();
	}
	
	public StructurePenaltyTriangle(DnaDesignGUI_ThemedApplet mc){
		this.mc = mc;
		fil = new FoldingImpl();
	}
	public void setPenalty(ScorePenalty sp, int[][] domain_sequences, int[][] nullMarkings){
		if (sp instanceof CrossInteraction || sp instanceof SelfFold){
			curSeqs = sp.getSeqs();
			domain = domain_sequences;
			domain_markings = nullMarkings;
			this.sp = sp;
			evalTriangle();
		} else {
			curSeqs = null;
		}
		redraw();
	}
	private void evalTriangle() {
		for(int[] row : domain_markings){
			Arrays.fill(row,0);
		}
		double score = 0;
		int len1, len2;
		len2 = len1 = curSeqs[0].length(domain);
		if (sp instanceof CrossInteraction){
			len2 = curSeqs[1].length(domain);
			score = fil.pairscore_viaMatrix(curSeqs[0],curSeqs[1],domain,domain_markings);
		}
		if (sp instanceof SelfFold){
			score = fil.foldSingleStranded(curSeqs[0],domain,domain_markings);
		}
		traceback = fil.getTraceback();
		nussinovScores = fil.getNussinovMatrixScore(len1,len2);
		//int[][][] structureMatrix = fil.getMFEStructureMatrix();
		
		foldScore = score;
	}
	private PFont ff;
	public void setup(){
		ff = createFont("Arial", 20);
		size(100, 100, P3D);
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
				textAlign(CENTER,CENTER);
				for(int y = 0; y < len1; y++){
					pushMatrix();
					scale(1f/len2,1f/len1);
					translate(.5f,.5f);
					scale(10f/width,10f/height);
					text(""+DnaDefinition.displayBase(seq1.base(y, domain)),0,0);
					popMatrix();
					translate(0,area.height/len1);
				}
				popMatrix();

				pushMatrix();
				translate(area.x,0);
				textFont(ff);
				textAlign(CENTER,CENTER);
				for(int x = 0; x < len2; x++){
					pushMatrix();
					scale(1f/len2,1f/len1);
					translate(.5f,.5f);
					scale(10f/width,10f/height);
					text(""+DnaDefinition.displayBase(seq2.base(x, domain)),0,0);
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
						fill(20,40,100,(float)nussinovScores[y][x]*255 /(1+(float)foldScore));
						ellipseMode(CORNER);
						if (curSeqs.length==2 || y < x){
							ellipse(x,y,1,1);
						}
					}
				}
				translate(.5f,.5f);
				
				stroke(0);
				strokeWeight(4);
				Point lastPoint = null;
				for(Point t : traceback){
					if (lastPoint!=null){
						line(lastPoint.y,lastPoint.x,t.y,t.x);
					}
					lastPoint = t;
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
