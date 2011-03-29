package DnaDesignGUI.util;

import java.awt.geom.Rectangle2D;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

import javax.swing.JOptionPane;
import javax.swing.JTextArea;

import processing.core.PApplet;

public class ScaffoldSuitabilityGraph extends PApplet{
	public void setup(){
		size(640,640,P2D);
		Text Text;
		int i = 0;
		String[] names = new String[]{"M13based",/*"M13","CDNAonedomain","Random"*/};
		Texts = new Text[names.length*3];
		for(; i/3 < names.length;){
			Text = new Text();
			Text.name = names[i/3];
			Text.extension1 = ".vienna.suit";
			Text.extension2 = ".vienna.struct";
			Texts[i++]=Text;
			Text = new Text();
			Text.name = names[i/3];
			Text.extension1 = ".vienna.struct";
			Texts[i++]=Text;
			Text = new Text();
			Text.name = names[i/3];
			Text.extension1 = ".vienna.suit";
			Texts[i++]=Text;
		}
	}
	//If true, make pdfs of all files.
	private boolean snapshotPDF = true;
	
	private static class Text {
		String name;
		String extension1;
		String extension2;
	}
	private int cText = 0;
	private Text[] Texts;
	public void draw(){
		String snapshotPath = null;
		Text Text = Texts[cText];
		if (snapshotPDF){
			File temp;
			try {
				//temp = File.createTempFile("WangTile"+System.nanoTime(), ".pdf");
				temp = new File(System.getProperty("filedir")+"/"+Text.name+Text.extension1+"."+Text.extension2+".pdf");
				beginRecord(PDF, snapshotPath = temp.getAbsolutePath());
			} catch (Throwable e) {
				e.printStackTrace();
			}
		}
		
		background(255);
		//Rectangle2D.Float graphR = new Rectangle2D.Float(20,20,width-40,height-40);
		Rectangle2D.Float graphR = new Rectangle2D.Float(0,0,width,height);
		
		{pushMatrix();
		translate(graphR.x,graphR.y);
		scale(graphR.width/8000,graphR.height/8000);
		
		stroke(0);
		noFill();
		rect(-1,-1,8002,8002);
		colorMode(RGB,255);
		int blue = color(26,22,149);
		int red = color(170,16,45);
		int white = color(255,255,255);
		//colorMode(HSB,255);
		
		File in2 = new File(System.getProperty("filedir")+"/"+Text.name+Text.extension1);
		File in3 = new File(System.getProperty("filedir")+"/"+Text.name+Text.extension2);
		Scanner in, inAdd = null;
		int deltaY = 0;
		try {
			in = new Scanner(in2);
			int lastY = -1;
			big: while(in.hasNextLine()){
				String[] line = in.nextLine().split("\\s+");
				int newY = new Integer(line[0]);
				if (lastY!=newY){
					if (lastY != -1){
						deltaY = newY - lastY;
						break big;
					} else {
						lastY = newY;
					}
				}
			}
			in.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		try {
			in = new Scanner(in2);
			if (Text.extension2!=null){
				inAdd = new Scanner(in3);
			}
			noStroke();
			beginShape(QUAD_STRIP);
			int lastY = -1, lastX = -1;
			boolean firstTime = true;
			while(in.hasNextLine()){
				String[] line = in.nextLine().split("\\s+");
				String[] lineAdd = null;
				if (inAdd!=null){
					lineAdd = inAdd.nextLine().split("\\s+");
				}
				lastX = new Integer(line[1]);
				int y = new Integer(line[0]);
				if (y!=lastY){
					lastY = y;
					endShape();
					beginShape(QUAD_STRIP);
					System.out.println(lastY);
				}
				if (!firstTime){
					vertex(lastX, lastY);
					vertex(lastX, lastY+deltaY);
				}
				float lerp = new Float(line[2]);
				if (inAdd!=null){
					lerp += new Float(lineAdd[2]);
				}
				lerp = -lerp/14;
				int col;
				if (lerp < 1){
					col = lerpColor(blue, red, lerp);
				} else {
					col = lerpColor(red, white, lerp-1);
				}
				fill(col);
				vertex(lastX, lastY);
				vertex(lastX, lastY+deltaY);
				firstTime = false;
			}
			endShape();
			in.close();
			
			if (inAdd!=null){
				inAdd.close();
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		popMatrix();}

		if (snapshotPath!=null){
			endRecord();
			/*
			JOptionPane.showMessageDialog(this, new JTextArea("File saved to \n"+snapshotPath){
				{
					setEditable(false);
				}
			});
			*/
			snapshotPDF = false;
			cText++;
			snapshotPDF = true;
			loop();
		} else {
			noLoop();
		}

	}
}
