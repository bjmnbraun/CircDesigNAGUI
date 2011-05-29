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

import java.util.LinkedList;
import java.util.Scanner;

public class DotParensToFakeDesignInput {
	public static void main(String[] args){
		Scanner in = new Scanner(System.in);
		String line = in.nextLine();
		LinkedList<Integer> opens = new LinkedList();
		LinkedList<Numeric> intlist = new LinkedList();
		int[] pairs = new int[line.length()];
		for(int i = 0; i < line.length(); i++){
			switch(line.charAt(i)){
			case '.':
				pairs[i] = -1;
				break;
			case '(':
				opens.push(i);
				break;
			case ')':
				int got = opens.pop();
				pairs[got] = i;
				pairs[i] = got;
				break;
			}
		}
		if (opens.size()>0){
			throw new RuntimeException("Overflow buffer");
		}
		for(int i = 0; i < line.length();){
			for(char got : new char[]{'.', '(', ')'}){
				if (i >= line.length()){
					break;
				}
				int startPair = pairs[i];
				int len = 0;
				switch(got){
				case '.':
					for(; i < line.length() && line.charAt(i)=='.'; i++){
						len++;
					}
					if (len > 0)
						intlist.add(new Period(len));
					break;
				case '(':
					for(; i < line.length() && line.charAt(i)=='(' && pairs[i]==startPair-len; i++){
						len++;
					}
					if (len > 0)
						intlist.add(new Open(len));
					break;
				case ')':
					for(; i < line.length() && line.charAt(i)==')' && pairs[i]==startPair-len; i++){
						len++;
					}
					if (len > 0)
						intlist.add(new Close(len));
					break;
				}
			}
		}
		int max = 0;
		for(Numeric q : intlist){
			max = Math.max(max,q.getI());
		}
		System.out.println("Domains:");
		for(int k = 1; k <= max; k++){
			System.out.println(k+" "+k);
		}
		System.out.println("Molecules:");
		System.out.print("A ");
		System.out.print("[");
		for(Numeric q : intlist){
			System.out.print(q.getI()+q.getChar()+"|");
		}
		System.out.println("}");
	}
	public interface Numeric {
		public int getI();
		public String getChar();
	}
	public static class Open implements Numeric{
		public Open(int i){
			this.i = i;
		}
		public int i;
		public int getI() {
			return i;
		}
		public String getChar() {
			return "(";
		}
		
	}
	public static class Close implements Numeric{
		public Close(int i){
			this.i = i;
		}
		public int i;
		public int getI() {
			return i;
		}
		public String getChar() {
			return ")";
		}
	}
	public static class Period implements Numeric{
		public Period(int i){
			this.i = i;
		}
		public int i;
		public int getI() {
			return i;
		}
		public String getChar() {
			return ".";
		}
	}
}
