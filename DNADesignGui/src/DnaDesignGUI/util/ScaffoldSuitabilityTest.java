package DnaDesignGUI.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.Scanner;

import circdesigna.energy.CircDesigNAMCSFolder;

import edu.utexas.cssb.circdesigna.DomainSequence;

import DnaDesign.Config.CircDesigNAConfig;

/**
 * Tests partitions of a scaffold's reverse complement (length N each) against all length N regions of the
 * scaffold, aligned at multiples of N/2.
 * 
 * MFold is used for the 
 * 
 * A good scaffold should have specific interactions only between aligned subsets of its reverse complement,
 * but this is not sufficient. It should also not show a great deal of secondary structure formation.
 */
public class ScaffoldSuitabilityTest {
	public static void main(String[] args) throws IOException, InterruptedException{
		File dir = new File(System.getProperty("viennaFilesDir"));
		CircDesigNAConfig config = new CircDesigNAConfig();
		for(File q : dir.listFiles()){
			if (q.getName().endsWith(".vienna")){
				Scanner in = new Scanner(new FileInputStream(q));
				String mfe = in.nextLine();
				String seq = in.nextLine();
				if (seq.length()>8000){
					seq = seq.substring(0,8000);
				}
				String dotparens = in.nextLine();
				in.close();

				if (true){
					final File struct = new File(q.toString()+".struct");
					if (struct.exists()){
						System.err.println("Skipping "+struct);
					} else {
						System.setOut(new PrintStream(new FileOutputStream(struct)));

						//Secondary structure / Dimerization: don't revcomp 
						scaffoldSuitability(seq,13,config,false);

						System.out.close();
					}
				}
				if (true){
					final File suit = new File(q.toString()+".suit");
					if (suit.exists()){
						System.err.println("Skipping "+suit);
					} else {
						System.setOut(new PrintStream(new FileOutputStream(suit)));

						//Specificity of interaction. Revcomp!
						scaffoldSuitability(seq,13,config,true);

						System.out.close();
					}
				}
				if (false){
					System.setOut(new PrintStream(new FileOutputStream(new File(q.toString()+".comp"))));

					SequenceCompositionPlotter.plotSequence(config,seq,50,1);

					System.out.close();
				}
			}
		}
	}

	//Try with nupack instead?
	private static final String absPathToHybridMinMod = "\"C:\\Users\\Benjamin\\CLASSWORK\\002. UT UNDERGRADUATE GENERAL\\EllingtonLab\\AutoAmplifierDesign\\unafold\\hybrid-min.exe\" --NA=DNA ";

	private static void scaffoldSuitability(String seqT, int N, CircDesigNAConfig config, boolean doRevComp) throws IOException, InterruptedException {
		CircDesigNAMCSFolder fi = new CircDesigNAMCSFolder(config);
		
		int[][] domain = new int[N][];
		domain[0] = new int[N];
		domain[1] = new int[N*2];
		DomainSequence seq1 = new DomainSequence();
		DomainSequence seq2 = new DomainSequence();
		seq1.setDomains(0,null);
		seq2.setDomains(1,null);
		for(int y = 0; y + N - 1 < seqT.length(); y += N){
			if (doRevComp){
				for(int i = 0; i < N; i++){
					domain[0][i] = config.monomer.complement(config.monomer.decodeBaseChar(seqT.charAt(y+N-1-i)));
				}
			} else {
				for(int i = 0; i < N; i++){
					domain[0][i] = config.monomer.decodeBaseChar(seqT.charAt(y+i));
				}
			}
			
			File tmp1 = new File("unafoldtmp1.txt");
			PrintWriter out1 = new PrintWriter(tmp1);
			File tmp2 = new File("unafoldtmp2.txt");
			PrintWriter out2 = new PrintWriter(tmp2);
			for(int k = 0; k + 2*N - 1 < seqT.length(); k+= N){
				for(int i = 0; i < N*2; i++){
					domain[1][i] = config.monomer.decodeBaseChar(seqT.charAt(k+i));
				}
				
				//Get MFE
				{
					out1.println(">"+y);
					for(int q : domain[0]){
						out1.print(config.monomer.displayBase(q));
					}
					out1.println();
				}
				{
					out2.println(">"+k);
					for(int q : domain[1]){
						out2.print(config.monomer.displayBase(q));
					}
					out2.println();
				}
				//double score = fi.mfeHybridDeltaG_viaUnafold(seq1,seq2, domain, null);
				//System.out.printf("%d %d %f",y,k,score);
				//System.out.println();
			}
			out1.close();
			out2.close();
			
			
			Process p = Runtime.getRuntime().exec(absPathToHybridMinMod+"\""+tmp1.getAbsolutePath()+"\" \""+tmp2.getAbsolutePath()+"\"");
			Scanner in = new Scanner(p.getInputStream());
			while(in.hasNextLine()){
				{
					//Calculating for 0 and 0, t = 37
					String[] line = in.nextLine().split("\\s+|[,]");
					System.out.printf("%d %d ",new Integer(line[2]),new Integer(line[4]));
				}
				String[] line = in.nextLine().split("\\s+");
				int num = new Integer(line[0]);
				//39	dG = -17.5	dH = -100.9	0-0
				System.out.printf("%.2f",new Double(line[3]));
				for(int k = 0; k < num; k++){
					in.nextLine();
				}
				System.out.println();
			}
			p.waitFor();			
			p.getOutputStream().close();
			p.getInputStream().close();
		    p.getErrorStream().close();

		}
	}
}
