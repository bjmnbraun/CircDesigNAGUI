package circdesignagui.TripleSim;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Scanner;

import circdesigna.DomainPolymerGraph;
import circdesigna.TripleSim.Associate;
import circdesigna.TripleSim.Attachment;
import circdesigna.TripleSim.BranchMigration;
import circdesigna.TripleSim.Disassociate;
import circdesigna.TripleSim.PulseEvents;
import circdesigna.TripleSim.TripleSim;
import circdesigna.TripleSim.TripleSimWidestPathGraph;
import circdesigna.TripleSim.ReactionGraph3X.Graph;
import circdesigna.TripleSim.ReactionGraph3X.GraphNode;
import circdesigna.config.CircDesigNAConfig;
import circdesigna.test.MoleculeToTreeConversion;

public class TripleSimTest {
	public static void main(String[] args) throws FileNotFoundException{
		CircDesigNAConfig config = new CircDesigNAConfig();
		Scanner in = new Scanner(System.in);
		
		//Machine run for no more than 3600 seconds from initial conditions.
		double MachineRuntime = 7200; 
		//A possible product which never occurs in concentrations greater than this
		//over the machine runtime is not "visited," in the sense that the possible reactions
		//stemming from such a product are not investigated and added to the graph.
		//This achieves the effect that in the resulting graph, any possible product that occurs at
		//concentrations above this value in the ACTUAL machine during an interval of MachineRuntime is in
		//the graph. Proof: For any species Y in the actual (complete) reaction graph which achieves a concentration
		//of X at some point in a simulation over some time interval, there must be a reaction pathway P from the initial species
		//to Y where every intermediate of P has concentration X at some point in the simulation.
		double IgnorePriority = 1e-13; 
		
		double simulationAccuracy = 1e-8;
		
		GraphNode inputMolecule = null;
		
		Graph g = new Graph(config);
		{
			ArrayList<DomainPolymerGraph> inputStructure = (ArrayList)MoleculeToTreeConversion.getInputStructure(-1, MoleculeToTreeConversion.GRAPH, in);
			for(DomainPolymerGraph d : inputStructure){
				GraphNode init = g.addSpecies(d);
				double x = 20e-9;
				if (d.moleculeName.equals("INPUT")){
					inputMolecule = init;
					init.initialConc = x/2;
					g.events.add(new PulseEvents(init, 3600, init.initialConc));
				} else if (d.moleculeName.startsWith("G")){
					init.initialConc = x*2;
				} else if (d.moleculeName.startsWith("T")){
					init.initialConc = x;
				} else {
					init.initialConc = 8*x;
				}
				System.out.printf("%s %.3e",d.moleculeName,init.initialConc);
				System.out.println();
				//Matcher m = Pattern.compile(".*\\((.*)\\)").matcher(d.moleculeName);
				//if (m.find()){
				//	init.initialConc = 1e-8;//new Double(m.group(1));
				//}
			}
		}
		//Base file outputs off of this file...
		System.out.println("Please enter the location of the template SBML file: ");
		String baseXML = in.nextLine();
		
		Associate associate = new Associate(config);
		Disassociate disassociate = new Disassociate(config);
		Attachment attachment = new Attachment(config);
		BranchMigration bm = new BranchMigration(config);
		TripleSim simulator = new TripleSim();

		long nanoTime = System.nanoTime();
		int unproductiveIterations = 0;
		big: while(!g.unvisited.isEmpty()){
			{
				GraphNode A = g.unvisited.peek();
				if (A.priority < IgnorePriority){
					long now = System.nanoTime();
					System.out.print("Update took ");
					double simulatedTime = simulator.updatePriorities(g,simulationAccuracy,MachineRuntime,null,IgnorePriority);
					double UpdateTime = (System.nanoTime()-now)/1e9;
					System.out.println(UpdateTime+" seconds. System size: "+g.size()+". Simulated up to time: "+simulatedTime);
					//simulator.printLastConcentrations(g);
					if (UpdateTime > 60){
						dumpGraph(baseXML,g);
						System.out.println("ENTER ABORT TO STOP, CONTINUE TO CONTINUE:");
						while(in.hasNextLine()){
							String line = in.nextLine();
							if (line.equals("CONTINUE")){
								break;
							}
							if (line.equals("ABORT")){
								break big;
							}
						}
					}
				}

				if (A.priority < IgnorePriority){
					unproductiveIterations++;
					if (unproductiveIterations > 3){
						System.out.printf("Congratulations. Your system has a finite number of %.3e-ignorable species over %.2e seconds of running for these initial conditions!",IgnorePriority,MachineRuntime);
						System.out.println();
						break;
					}
				} else {
					unproductiveIterations = 0;
				}
			}
			
			GraphNode A = g.unvisited.poll();
			if (A.visited){
				throw new RuntimeException("Visiting node twice.");
			}
			//Visiting a species triggers output.
			System.out.println(A.index+" "+A);
			//A is MONOMOLECULAR
			g.visit(A);
			//Intramolecular stuff
			associate.associate(g, A);
			disassociate.disassociate(g, A); //Covers detachment.
			bm.branchMigrate(g, A);
			
			if (A.stable){
				//Intermolecular (bimolecular): Register attachments only when both nodes have been visited
				for(GraphNode B : g.allVisited){
					if (B.stable){
						if (A.structure.getStrandRotations().size() + B.structure.getStrandRotations().size() <= 5){
							attachment.attach(g,g.getDocking(A,B));
						}
					}
				}
			}
			
			//Time bound?
			//if (System.nanoTime()-nanoTime > 10e9){
			//	break;
			//}
		}

		System.out.printf("Size of reaction graph: %d nodes and %d (one-way) reactions",g.size(),g.countOneWayReactions());
		System.out.println();

		dumpGraph(baseXML,g);

		while(true){
			System.out.println("Simulating pulsed system. Please enter in a pulse interval (seconds): ");
			double value = 300;
			try {
				value = new Double(in.nextLine());
			} catch (Throwable e){
				continue;
			}
			g.events.get(0).schedule[0].t = value;
			
			System.out.println("Simulating pulsed system. Please enter in a pulse concentration (initial): "+inputMolecule.initialConc);
			value = inputMolecule.initialConc;
			try {
				value = new Double(in.nextLine());
			} catch (Throwable e){
				continue;
			}
			g.events.get(0).schedule[0].amount = value;
			
			System.out.println("Simulating pulsed system. Please enter in a machine runtime (initial): "+MachineRuntime);
			value = MachineRuntime;
			try {
				value = new Double(in.nextLine());
			} catch (Throwable e){
				continue;
			}
			MachineRuntime = value;
			
			System.out.println("Dumping simulation");
			//Show the final system as a simulation! Exciting!
			{
				PrintWriter out = new PrintWriter(new FileOutputStream(new File(baseXML+".sim")));
				simulator.updatePriorities(g,simulationAccuracy,MachineRuntime,out,Double.MAX_VALUE);
				out.close();
			}
		}
	}

	private static void dumpGraph(String baseXML, Graph g) {
		System.out.println("Dumping sbml");
		//Output SBML of the reaction graph. 
		TripleSimSBML sbml = new TripleSimSBML(baseXML, g);
		sbml.write(baseXML+".out.xml");

		System.out.println("Dumping Widest Path Graph");
		TripleSimWidestPathGraph widest = new TripleSimWidestPathGraph(g);
		widest.write(baseXML+".out.path");

		System.out.println("Dumping graph");
		//Show the reaction graph! Also Exciting!
		TripleSimGraphVis graph = new TripleSimGraphVis();
		{
			PrintWriter out;
			try {
				out = new PrintWriter(new FileOutputStream(new File(baseXML+".s")));
				graph.write(g, out);
				out.close();
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}
