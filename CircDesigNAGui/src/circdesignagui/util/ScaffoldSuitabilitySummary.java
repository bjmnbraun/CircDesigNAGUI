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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Scanner;


import circdesigna.DomainSequence;
import circdesigna.config.CircDesigNAConfig;
import circdesigna.energy.ConstraintsNAFoldingImpl;



public class ScaffoldSuitabilitySummary {
	public static void main(String[] args) throws FileNotFoundException{
		File dir = new File(System.getProperty("viennaFilesDir"));

		CircDesigNAConfig config = new CircDesigNAConfig();
		
		for(File q : dir.listFiles()){

			boolean printSeqs= false;
			
			
			if (q.getName().endsWith(".vienna.struct") || q.getName().endsWith(".vienna.suit")){
				boolean doRevComp = q.getName().endsWith(".suit");
				
				String seq = System.getProperty("viennaFilesDir")+"/"+q.getName().substring(0,q.getName().lastIndexOf('.'));
				{
					Scanner in = new Scanner(new File(seq));
					in.nextLine();
					seq = in.nextLine();
					in.nextLine();
					in.close();
				}
				System.out.print(q.getName()+": ");
				if (printSeqs){
					System.out.println();
				}
				Scanner in = new Scanner(new FileInputStream(q));
				int lastY = -1;
				double max = 0;
				int ct = -1; double sum = 0;

				int N = 13;
				int[][] domain = new int[N][];
				int[][] nullMark = new int[N][];
				domain[0] = new int[N];
				domain[1] = new int[N*2];
				nullMark[0] = new int[N];
				nullMark[1] = new int[N*2];
				DomainSequence seq1 = new DomainSequence();
				DomainSequence seq2 = new DomainSequence();
				seq1.setDomains(0,null);
				seq2.setDomains(1,null);
				ConstraintsNAFoldingImpl fl = new ConstraintsNAFoldingImpl(config);
				
				while(in.hasNextLine()){
					String[] line = in.nextLine().split("\\s+");
					int y = new Integer(line[0]);
					int x = new Integer(line[1]);
					if (y!=lastY){
						sum += max;
						lastY = y;
						ct++;
						max = 0;
					}
					double val = new Double(line[2]);
					if (Math.abs(x-y) > 13 && val < max){
						max = val;
						if (printSeqs){
							if (doRevComp){
								for(int i = 0; i < N; i++){
									domain[0][i] = config.monomer.complement(config.monomer.decodeBaseChar(seq.charAt(y+N-1-i)));
								}
							} else {
								for(int i = 0; i < N; i++){
									domain[0][i] = config.monomer.decodeBaseChar(seq.charAt(y+i));
								}
							}
							for(int i = 0; i < N*2; i++){
								domain[1][i] = config.monomer.decodeBaseChar(seq.charAt(x+i));
							}
							
							double scoreSelf = fl.mfe(seq1, seq2, domain, nullMark);
							
							System.out.println(y+" "+x+" "+seq.substring(y,y+N)+" "+seq.substring(x,x+N*2)+" "+val+" "+scoreSelf);
						}
					}
				}
				if(max < 0){
					sum += max;
					ct++;
				}
				in.close();
				System.out.printf("%.3f",sum / ct);
				System.out.println();
			}
		}
	}
}
