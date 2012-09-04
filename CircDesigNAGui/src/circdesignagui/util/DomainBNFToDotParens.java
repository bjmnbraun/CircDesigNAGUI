package circdesignagui.util;

import java.util.Scanner;

import circdesigna.DomainDefinitions;
import circdesigna.config.CircDesigNAConfig;



public class DomainBNFToDotParens {
	public static void main(String[] args){
		Scanner in = new Scanner(System.in);
		String lineEnd = "\n";
		StringBuffer DomainDefs = new StringBuffer();
		StringBuffer Molecule = new StringBuffer();
		while(in.hasNextLine()){
			String line = in.nextLine();
			if (line.startsWith("[")){
				Molecule.append(line);
				break;
			} else {
				DomainDefs.append(line);
				DomainDefs.append(lineEnd);
			}
		}
		CircDesigNAConfig config = new CircDesigNAConfig();
		DomainDefinitions dsd = new DomainDefinitions(config);
		DomainDefinitions.readDomainDefs(DomainDefs.toString(), dsd);
		for(String q : Molecule.toString().split("(\\||}|\\[)+")){
			if (q.length()>0){
				StringBuffer namePart = new StringBuffer();
				boolean upParen = false;
				boolean downParen = false;

				for(char d : q.toCharArray()){
					if (d == '('){
						upParen = true;
					} else 
						if (d == ')'){
							downParen = true;
						} else
							if (d == '.' || d == '*'){

							} else {
								namePart.append(d);
							}
				}
				//System.out.println(namePart);
				final Integer i = dsd.getDomainFromName(namePart.toString());
				//System.out.println(i);
				int num = dsd.domainLengths[i];
				for(int k = 0; k < num; k++){
					if (upParen){
						System.out.print("(");
					} else if (downParen){
						System.out.print(")");
					} else {
						System.out.print(".");
					}
				}
			}
		}
	}
}
