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
package circdesignagui.util;

import java.util.Arrays;
import java.util.Scanner;

import circdesigna.config.CircDesigNAConfig;


public class SequenceCompositionPlotter {
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
		config.setMode(CircDesigNAConfig.RNA_MODE);
		plotSequence(config,dna.toString(),50,1);
	}

	public static void plotSequence(CircDesigNAConfig config, String dna, int windowSize, int stepSize) {
		int[] ct = new int[config.monomer.getMonomers().length];
		for(int i = 0; i + windowSize - 1 < dna.length(); i+=stepSize){
			Arrays.fill(ct,0);
			for(int k = i; k < dna.length() && k < i+windowSize; k++){
				ct[config.monomer.getNormalBaseFromZero(config.monomer.decodeBaseChar(dna.charAt(k)))]++;
			}
			System.out.print(i+" ");
			for(int r = 0; r < ct.length; r++){
				System.out.print(config.monomer.displayBase(r+1)+" "+ct[r]+" ");
			}
			System.out.println();
		}
	}
}
