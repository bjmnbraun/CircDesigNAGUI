package circdesignagui.util;

import java.util.Scanner;

import circdesigna.config.CircDesigNAConfig;

public class CompositionCalc {
	public static void main(String[] args){
		StringBuffer dna = new StringBuffer();
		System.out.println("Enter Dna / Rna:");
		Scanner in = new Scanner(System.in); 
		while(in.hasNextLine()){
			String line = in.nextLine();
			if (line.equals("END")){
				break;
			}
			dna.append(line);
		}
		CircDesigNAConfig config = new CircDesigNAConfig();
		String d = dna.toString();
		int[] ct = new int[config.monomer.getMonomers().length];
		for(int k = 0; k < d.length(); k++){
			ct[config.monomer.getNormalBaseFromZero(config.monomer.decodeBaseChar(dna.charAt(k)))]++;
		}
		int total = 0;
		for(int q : ct) total += q;
		for(int i = 0; i < ct.length; i++){
			System.out.println("%"+config.monomer.displayBase(i+1)+"="+ct[i]/(double)total);
		}
	}
}
