package circdesignagui.TripleSim;

import java.awt.Dimension;
import java.util.LinkedList;
import java.util.Scanner;

import javax.swing.JPanel;

import circdesigna.DomainDefinitions;
import circdesigna.DomainPolymerGraph;
import circdesigna.DomainStructureBNFTree;
import circdesigna.DomainStructureBNFTree.DomainStructure;
import circdesigna.DomainStructureBNFTree.HairpinStem;
import circdesigna.DomainStructureBNFTree.ThreePFivePOpenJunc;
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
	
	public static Animation readAnimation(String text, String domainDefs){
		if (text.startsWith("Attach") || text.startsWith("Detach")){
			return new AttachDetach(text, domainDefs);
		}
		if (text.startsWith("Branch Migration")){
			return new BranchMigrationAnimation(text, domainDefs);
		}
		return new Animation(text, domainDefs);
	}
	public static class Animation{
		public Animation(String text, String domainDefs){
			this.text = text;
			this.domainDefs = domainDefs;
			molecule = "A "+text.substring(text.indexOf('['));
		}	
		public String text;
		public String molecule;
		public String domainDefs;
		public int frames;
		public float progress(){
			return frames / 100.f;
		}
		public void work(DomainStructureBNFTree dsg) {
		}
	}
	public static class BranchMigrationAnimation extends Animation {
		public BranchMigrationAnimation(String text, String domainDefs) {
			super(text, domainDefs);
			
			String[] parS = text.substring(0,text.indexOf('[')).trim().split("\\s+");
			N = new Integer(parS[2]);
			reduce = parS[3];
			increase = parS[4];
			this.domainDefs+= reduce+" "+N+"\n";
			this.domainDefs+= increase+" "+0+"\n";
		}
		//Size of migration domain
		int N;
		public String reduce, increase;
		public void work(DomainStructureBNFTree dsg) {
			DomainDefinitions ddef = dsg.getDomainDefs();
			
			int phase = (int)(progress() * N * (N+1)/((float)N));
			
			ddef.domainLengths[ddef.getDomainFromName(increase)] = phase;
			ddef.domainLengths[ddef.getDomainFromName(reduce)] = N - phase;

			for(DomainStructure s : dsg.listStructures()){
				s.handleSubConformation(ddef.domainLengths, dsg.domains);
			}
			dsg.buildOuterCurve();
		}
	}
	public static class AttachDetach extends Animation{
		public AttachDetach(String text, String domainDefs) {
			super(text, domainDefs);
			String[] parS = text.substring(0,text.indexOf('[')).split("\\s+");
			par = new int[]{new Integer(parS[1]),new Integer(parS[2])};
			if (parS[0].startsWith("Detach")){
				detach = true;
			}
		}
		boolean detach = false;
		private float amplitude = 20;
		private int[] par;
		public float phase(){
			if (detach){
				return 1-progress();
			} else {
				return progress();
			}
		}
		
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
					hs.hydrogenBondStrength = phase();
					hs.openingSize = amplitude/(1+exp(phase()*5))+2;
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
		
		public void work(DomainStructureBNFTree dsg) {
			float phase = progress();
			if (detach){
				phase = 1-phase;
			}
			DomainDefinitions ddef = dsg.getDomainDefs();
			//for(DomainStructure s : dsg.listStructures()){
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
		} 
	}
	
	private LinkedList<Animation> toAnimate = new LinkedList();
	public String snapshotDirectory = "";
	
	public Animator() {
		super(new Themer(), new CircDesigNAConfig());
		setPreferredSize(new Dimension(480,480));
		Scanner in = new Scanner(System.in);
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
				Animation neu = readAnimation(line, domainDefs);
				toAnimate.add(neu);
			} catch (Throwable e){
				e.printStackTrace();
			}
		}
	}
	
	public void draw(){
		DnaDesignScreens$0_Screen.DnaSequencePreview g = getRenderer();
		g.drawGrid = false;
		g.rotateToLongestHairpin = false;
		g.dynamicWiggle = false;
		g.uniformScale = true;
		if (frameCount <= 1){	
			try {
				setCurrentPreviewMolecule(0, 2, "A [A}", "A 150");
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
			return;
		}
		if (current.frames==0){
			try {
				setCurrentPreviewMolecule(1, 2, current.molecule, current.domainDefs);
			} catch (UpdateSuccessfulException e){
			} 
		}
		
		current.work(g.dsg);
		current.frames++;
		snapShot(snapshotDirectory+"/"+frameCount+".pdf");
		super.draw();
		System.out.println(getLastSnapshotPath());
	}
}
