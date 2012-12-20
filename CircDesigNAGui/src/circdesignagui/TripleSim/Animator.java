package circdesignagui.TripleSim;

import java.awt.Color;
import java.awt.Dimension;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Scanner;

import javax.swing.JPanel;

import circdesigna.DomainDefinitions;
import circdesigna.DomainPolymerGraph;
import circdesigna.config.CircDesigNAConfig;
import circdesignagui.MoleculePreview;
import circdesignagui.ThemedApplet;

public class Animator extends MoleculePreview{
	public static class Themer extends ThemedApplet{
		public void addModalScale(Runnable runnable) {
		}
		public JPanel getModalPanel() {
			return null;
		}
		public void removeAllModalScale() {
		}
		public boolean modalPanelIsOccupied() {
			// TODO Auto-generated method stub
			return false;
		}
	}
	
	public static Animation readAnimation(CircDesigNAConfig cfg, String text, String domainDefs){
		if (text.startsWith("Attach") || text.startsWith("Detach")){
			return new AttachDetach(text, domainDefs);
		}
		if (text.startsWith("Branch Migration")){
			return new BranchMigrationAnimation(cfg, text, domainDefs);
		}
		throw new RuntimeException("Unrecognized animation: "+text);
	}
	public static abstract class Animation{
		public Animation(String text, String domainDefs){
			this.text = text;
			this.domainDefs = domainDefs;
			molecule = "A "+text.substring(text.indexOf('['));
		}	
		public String text;
		public String molecule;
		public String domainDefs;
		public int frames;
		public DnaDesignScreens$0_Screen.DnaSequencePreview renderer;
		public abstract float progress();
		
		public void work(DomainPolymerGraph dpg) {
		}
	}
	public static class BranchMigrationAnimation extends Animation {
		public BranchMigrationAnimation(CircDesigNAConfig cfg, String text, String domainDefs) {
			super(text, domainDefs);
			
			String[] parS = text.substring(0,text.indexOf('[')).trim().split("\\s+");
			Scanner in = new Scanner(text);
			in.next();
			in.next();
			int numDirection = in.nextInt();
			String clockDirection = in.next();
			ArrayList<int[]> bmPairs = new ArrayList(); //positions to displace
			while(in.hasNext()){
				String word = in.next();
				if (word.equals("END")){
					break;
				}
				bmPairs.add(new int[]{new Integer(word), in.nextInt(), in.nextInt()}); 
			}
			
			String mol = in.nextLine();
			DomainDefinitions dd = new DomainDefinitions(cfg);
			DomainDefinitions.readDomainDefs(domainDefs, dd);
			DomainPolymerGraph dpg = new DomainPolymerGraph(dd);
			DomainPolymerGraph.readStructure("A "+mol, dpg);
			
			if (dpg.getDomain(dpg.length()-1) != -1){
				throw new RuntimeException("I don't know how to animate molecules that don't end in a 5' end");
			}
			
			N = 0;
			dispPairs = new ArrayList(); //domain indices of decrease, increase pairs. 
			
			//Sum over all bases being displaced
			for(int i = 0; i < bmPairs.size(); i++){
				int[] disp = bmPairs.get(i);
				int id = dpg.getDomain(disp[0]);
				int len = dd.getDomainLength(id);
				N += len;
			}
			for(int i = 0; i <= 0; i++){
				//Add decrease, increase pair
				domainDefs += "dec"+i+" "+N+"\n";
				domainDefs += "inc"+i+" "+0+"\n";
				dispPairs.add(new String[]{"dec"+i,"inc"+i});
			}
			
			//Rewrite mol to actually use them
			mol = "A [";
			for(int i = 0; i < dpg.length(); i++){
				int d = dpg.getDomain(i);
				mol += dpg.getStyle(i).toString();
				if (d >= 0){
					boolean found = false;
					int pair = dpg.getDomainPair(i);
					String punct = (pair < i) ? ") " : "( ";
					for(int j = 0; j < bmPairs.size(); j++){
						int[] bm = bmPairs.get(j);
						if (bm[0] == i){
							if (j == 0){
								if (clockDirection.equals("CCW")){
									mol += "dec"+j+" inc"+j+punct;
								} else {
									mol += "inc"+j+punct+"dec"+j+" ";
								}
							}	
							found = true;
						}
						if (bm[1] == i){
							String punct2 = (bm[1] < bm[2]) ? "( " : ") ";
							if (j == 0){
								if (clockDirection.equals("CCW")){
									mol += "inc"+j+"*"+punct+"dec"+j+"*"+punct2;
								} else {
									mol += "dec"+j+"*"+punct2+"inc"+j+"*"+punct;
								}
							}	
							found = true;
						}
						if (bm[2] == i){
							String punct2 = (bm[1] < bm[2]) ? ") " : "( ";
							if (j == 0){
								if (clockDirection.equals("CCW")){
									mol += "dec"+j+punct2+"inc"+j+" ";
								} else {
									mol += "inc"+j+" "+"dec"+j+punct2;
								}
							}
							found = true;
						}
					}
					if (!found){
						if (pair == -1){
							mol += dd.getDomainName(d) + " ";
						} else {
							mol += dd.getDomainName(d) + punct;
						}
					}
				} else {
					mol+="}";
					//5' end
					if (i + 1 < dpg.length()){
						mol+=" [ ";
					}
				}
			}
			
			//Override default assignment and domain definitions
			molecule = mol;
			this.domainDefs = domainDefs;
			System.out.println(molecule);
			System.out.println(domainDefs);
		}
		
		public float progress(){
			float fast = frames / 2f / N;
			if (viewLocal){
				return fast/5;
			} 
			return fast;
		}
		
		private ArrayList<String[]> dispPairs = new ArrayList();
		//Size of migration domain
		int N;
		public String reduce, increase;
		
		int lastPhase = -1;
		public void work(DomainPolymerGraph dpg) {
			DomainDefinitions ddef = dpg.getDomainDefs();
			
			int phase = (int)(progress() * N * (N+1)/((float)N));
			
			if (phase > lastPhase){
				for(String[] q : dispPairs){
					int q0 = ddef.getDomainFromName(q[0]);
					int q1 = ddef.getDomainFromName(q[1]);
					int currentLength = ddef.domainLengths[q0];
					if (currentLength > 0){
						ddef.domainLengths[q0]--;
						ddef.domainLengths[q1]++;
						break;
					}
				}
				lastPhase = phase;
			}
			renderer.parseMolecule();
			
			/*
			for(DomainStructure s : dsg.listStructures()){
				s.handleSubConformation(ddef.domainLengths, dsg.domains);
			}
			dsg.buildOuterCurve();
			*/
		}
	}
	public static class AttachDetach extends Animation{
		public AttachDetach(String text, String domainDefs) {
			super(text, domainDefs);
			Scanner in = new Scanner(text);
			in.next();
			par = new ArrayList();
			while(true){
				String next = in.next();
				if (next.equals("END")){
					break;
				}
				par.add(new int[]{new Integer(next), in.nextInt()});
			}
			if (text.startsWith("Detach")){
				detach = true;
			}
		}
		public float progress(){
			float fast = frames / 10f;
			if (viewLocal){
				return fast/5;
			} 
			return fast;
		}
		boolean detach = false;
		private float amplitude = 20;
		private ArrayList<int[]> par;
		public float phase(){
			if (detach){
				return 1-progress();
			} else {
				return progress();
			}
		}
		
		/*
		public boolean work_r(DomainStructure s){
			boolean found = false;
			int oneBreak = 0;
			for(DomainStructure u : s.subStructure){
				found |= work_r(u);
			}
			if (found){
				for(DomainStructure u : s.subStructure){
					if (u instanceof ThreePFivePOpenJunc){
						ThreePFivePOpenJunc t = (ThreePFivePOpenJunc)u;
						t.size = amplitude/(1+exp(phase()*5))+1.2f;
					}
				}
			}
			if (s instanceof HairpinStem){
				HairpinStem hs = (HairpinStem)s;
				if (s.sequencePartsInvolved[0]==par[0] && s.sequencePartsInvolved[1]==par[1]){
					//hs.hydrogenBondStrength = phase();
					//hs.openingSize = amplitude/(1+exp(phase()*5))+2;
					for(DomainStructure q : hs.subStructure){
						if (q instanceof ThreePFivePOpenJunc){
							ThreePFivePOpenJunc t = (ThreePFivePOpenJunc)q;
							t.size = amplitude/(1+exp(phase()*5))+1.2f;
						}
					}
					return true;
				}
			}
			return false;
		}
		*/
		
		public void work(DomainPolymerGraph dpg) {
			float phase = progress();
			if (detach){
				phase = 1-phase;
			}
			//for(DomainStructure s : dsg.listStructures()){
			/*
			boolean found = false;
			for(DomainStructure s : dsg.structures){
				found |= work_r(s);
			}
			if (found){
				for(DomainStructure u : dsg.structures){
					if (u instanceof ThreePFivePOpenJunc){
						ThreePFivePOpenJunc t = (ThreePFivePOpenJunc)u;
						t.size = amplitude/(1+exp(phase()*5))+1.2f;
					}
				}
			}

			//System.out.println(Arrays.toString(dsg.structures));
			for(DomainStructure s : dsg.listStructures()){
				s.handleSubConformation(ddef.domainLengths, dsg.domains);
			}
			dsg.buildOuterCurve();
			*/
			
			for(int[] p : par){
				int loc = p[0];
				//Extend all the way out until the end of the strand in both directions.
				for(int dir : new int[]{-1,1}){
					int nx = loc;
					while(true){
						if (nx < 0 || nx >= dpg.length()){
							break;
						}
						Color old = dpg.getStyle(nx).color;
						dpg.getStyle(nx).color = new Color(old.getRed(), old.getGreen(), old.getBlue(), (int)(phase * 255));
						System.out.println(dpg.getStyle(nx).color.getAlpha());
						if (dpg.getDomain(nx) == -1){
							break; //Hit the end of a strand
						}
						nx += dir;
					}
				}
			}
		} 
	}
	
	private LinkedList<Animation> toAnimate = new LinkedList();
	public String snapshotDirectory = "";
	private boolean firstAnimation = true;
	public static boolean viewLocal = false; //set to true if viewing locally
	
	public Animator() {
		super(new Themer(), new CircDesigNAConfig());
		setPreferredSize(new Dimension(1024,1024));
	}
	public void init(){
		Scanner in;
		if (getParameter("infile")!= null){
			try {
				in = new Scanner(new File(getParameter("infile")));
			} catch (FileNotFoundException e) {
				e.printStackTrace();
				exit();
				in = null;
			}
		} else {
			in = new Scanner(System.in);
		}
		System.out.println("Please enter the directory in which to store the animation:");
		snapshotDirectory = in.nextLine();
		in.nextLine();
		System.out.println("Please enter in the animation commands:");
		String domainDefs = "";
		while(in.hasNextLine()){
			String line = in.nextLine();
			if (line.equals("END")){
				break;
			}
			domainDefs += line+"\n";
		}
		while(in.hasNextLine()){
			String line = in.nextLine();
			if (line.equals("END")){
				break;
			}
			try {
				Animation neu = readAnimation(getConfig(), line, domainDefs);
				toAnimate.add(neu);
			} catch (Throwable e){
				e.printStackTrace();
			}
		}
		super.init();
	}

	public void draw(){
		String pdf = getLastSnapshotPath();
		if (pdf != null){
			pdf2png(pdf, snapshotDirectory+"/"+frameCount+".png");
			System.out.println(frameCount);
		}
		
		DnaDesignScreens$0_Screen.DnaSequencePreview g = getRenderer();
		g.drawGrid = false;
		g.rotateToLongestHairpin = false;
		g.dynamicWiggle = false;
		g.drawDomainNames = false;
		if (frameCount <= 1){	
			try {
				setCurrentPreviewMolecule(0, 2, "A [A}", "A 150", true);
			} catch (UpdateSuccessfulException e){
			} 
			return;
		}
		
		Animation current = toAnimate.peek();
		while (current != null && current.progress() >= 1){
			System.out.println("Completed: "+current);
			toAnimate.poll();
			current = toAnimate.peek();
		}
		if (current==null){
			noLoop();
			return;
		}
		current.renderer = g;
		
		if (current.frames==0){
			try {
				setCurrentPreviewMolecule(1, 2, current.molecule, current.domainDefs, firstAnimation);
			} catch (UpdateSuccessfulException e){
			} 
			firstAnimation = false;
		}
		
		current.work(g.dpg);
		current.frames++;
		snapShot(snapshotDirectory+"/"+frameCount+".pdf");
		//viewLocal = true;
		super.draw();
	}

	private void pdf2png(String pdf, String png) {
		String[] cmd = {
				/*
				    "cmd", 
				    "/c", 
				    "echo %PATH%"
				 */
				"gswin32c", 
				//"-q", 
				"-dSAFER -dBATCH -dNOPAUSE", 
				"-sDEVICE=png16m",
				//"-r200",
				"-dTextAlphaBits=4",
				"-dGraphicsAlphaBits=4",
				"-o",
				png, 
				pdf, 
		};
		try {
			Process p = Runtime.getRuntime().exec(cmd);
			if (false){
				BufferedReader bri = new BufferedReader
						(new InputStreamReader(p.getInputStream()));
				BufferedReader bre = new BufferedReader
						(new InputStreamReader(p.getErrorStream()));
				String line;
				while ( (line = bri.readLine ()) != null) {
					System.out.println(line);
				}
				bri.close();
				while ( (line = bre.readLine ()) != null) {
					System.out.println(line);
				}
				bre.close();
				p.waitFor();
				exit();
			}
		} 
		catch (Throwable e) {
			e.printStackTrace();
			exit();
		}
	}
}
