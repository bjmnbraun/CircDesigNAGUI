package circdesignagui.util;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Scanner;

public class ScaffoldShortformSplitter {
	public static void main(String[] args) throws IOException{
		File dir = new File(System.getProperty("viennaFilesDir"));
		File shortform = new File(System.getProperty("sfoldInput"));
		Scanner in = new Scanner(shortform);
		while(in.hasNextLine()){
			String[] line = in.nextLine().split("\\s+",2);
			int len = line[1].trim().length();
			PrintWriter o = new PrintWriter(new FileWriter(new File(dir.toString()+"/"+line[0]+"_"+len+".vienna")));
			o.println(len);
			o.println(line[1]);
			o.println(".");
			o.close();
		}
	}
}
