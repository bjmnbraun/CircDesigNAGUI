package DnaDesignGUI.util;

import static DnaDesignGUI.util.DesignMultipleTimes.readToEnd;

import java.io.File;
import java.io.IOException;
import java.util.Scanner;

import DnaDesign.DomainStructureData;
import DnaDesign.Config.CircDesigNAConfig;

public class ExtractScoresFromRun {
	public static void main(String[] args) throws IOException{
		Scanner in = new Scanner(System.in);
		String file = in.nextLine();

		String Domains = readToEnd(in);
		String Molecules = readToEnd(in);
		
		DomainStructureData dsd = new DomainStructureData(new CircDesigNAConfig());
		dsd.readDomainDefs(Domains, dsd);
		
		DesignMultipleTimes.RunEvaluation(new File(file), Molecules, dsd, -1, false);
	}
}
