package circdesignagui.TripleSim;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.PriorityQueue;
import java.util.Scanner;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.Map.Entry;

import circdesigna.TripleSim.ReactionGraph3X;
import circdesigna.TripleSim.ReactionGraph3X.BimolecularNode;
import circdesigna.TripleSim.ReactionGraph3X.GraphEdge;
import circdesigna.TripleSim.ReactionGraph3X.GraphNode;
import circdesigna.config.CircDesigNAConfig;

public class WidestPath {
	private static class StructureComparator implements Comparator<GraphNode>{
		public int compare(GraphNode o1, GraphNode o2) {
			int a1 = new Integer(o1.structureString);
			int a2 = new Integer(o2.structureString);
			if (a1 < 0){
				a1 += Integer.MAX_VALUE / 2;
			}
			if (a2 < 0){
				a2 += Integer.MAX_VALUE / 2;
			}
			if (a1 < a2){
				return -1;
			}
			if (a1 > a2){
				return 1;
			}
			return 0;
		}
	}
	private static class ReactionPathway{
		public ArrayList<GraphEdge> reactionsInOrder = new ArrayList();
		public TreeSet<GraphNode> species = new TreeSet(new StructureComparator());
		public void updatePriority(double i, TreeMap<GraphNode, ReactionPathway> memo, PriorityQueue<GraphNode> unvisited) {
			for(GraphNode e : species){
				if (i < e.priority){
					e.priority = i;
					memo.put(e, this);
					if (!e.visited){
						unvisited.remove(e);
						unvisited.add(e);
					}
				}
			}
		}
	}
	
	public static void main(String[] args) throws Throwable{
		String inputFile = null;
		{
			Scanner in = new Scanner(System.in);
			System.out.println("Please enter a URL of the .path file");
			inputFile = in.nextLine();
		}
		Scanner in = new Scanner(new File(inputFile));
		CircDesigNAConfig config = new CircDesigNAConfig();
		TreeMap<String, String[]> pairings = new TreeMap();
		ReactionGraph3X.Graph g = new ReactionGraph3X.Graph(config, new Comparator<GraphNode>(){
			public int compare(GraphNode o1, GraphNode o2) {
				if (o1.priority < o2.priority){
					return -1;
				} else if (o1.priority == o2.priority){
					return 0;
				} else {
					return 1;
				}
			}
		});
		while(in.hasNextLine()){
			String lineS = in.nextLine();
			if (lineS.equals("END")){
				break;
			}
			String[] line = lineS.split("\\s+");
			String[] pair = new String[]{line[1],line[2]};
			Arrays.sort(pair);
			pairings.put(line[0], pair);
		}
		while(in.hasNextLine()){
			String lineS = in.nextLine();
			if (lineS.equals("END")){
				break;
			}
			String[] line = lineS.split("\\s+",3);
			GraphNode neu = new GraphNode(line[0]);
			neu.genData = new double[]{new Double(line[1])};
			g.allSingles.put(neu.structureString, neu);
		}
		for(Entry<String, String[]> pair : pairings.entrySet()){
			BimolecularNode pairing = new BimolecularNode(pair.getKey(),g.allSingles.get(pair.getValue()[0]),g.allSingles.get(pair.getValue()[1]));
			g.allSingles.put(pairing.structureString, pairing);
			g.allDockings.put(pair.getValue()[0]+" "+pair.getValue()[1], pairing);
		}
		pairings = null;
		while(in.hasNextLine()){
			String lineS = in.nextLine();
			if (lineS.equals("END")){
				break;
			}
			String[] line = lineS.split("\\s+",4);
	
			GraphEdge edge = new GraphEdge();
			edge.k = new Double(line[2]);
			edge.type = line[3];
			edge.towards = g.allSingles.get(line[1]);
			GraphNode from = g.allSingles.get(line[0]);
			edge.reverse = new GraphEdge();
			edge.reverse.towards = from;
			from.neighbors.add(edge);
		}
		in.close();

		for(GraphNode p : g.allSingles.values()){
			p.priority = Double.POSITIVE_INFINITY;
		}
		
		g.unvisited.clear();
		for(GraphNode p : g.allSingles.values()){
			g.unvisited.add(p);
		}
		
		TreeMap<GraphNode, ReactionPathway> memo = new TreeMap(new StructureComparator());
		{
			ReactionPathway initialConditions = new ReactionPathway();
			for(String n : g.allSingles.keySet()){
				int val = new Integer(n);
				if (val >= 0 && val <= 12){
					initialConditions.species.add(g.allSingles.get(n));
				}
			};
			initialConditions.updatePriority(0, memo, g.unvisited);
		};
		
		
		while(!g.unvisited.isEmpty()){
			System.out.println(g.unvisited.size());
			GraphNode visit = g.unvisited.poll();
			if (visit.visited){
				throw new RuntimeException();
			}
			visit.visited = true;
			ReactionPathway bestFamily = memo.get(visit);
			if (bestFamily == null){
				break;
			}
			if (visit.isMonoMolecular()){
				//Try to combine two monomolecule pathways
				for(GraphNode brother : g.allSingles.values()){
					if(!brother.visited){
						continue;
					}
					GraphNode dock = g.allDockings.get(visit.structureString+" "+brother.structureString); 
					if (dock==null){
						dock = g.allDockings.get(brother.structureString+" "+visit.structureString);
					}
					if(dock==null){
						continue;
					}
					ReactionPathway brotherPath = memo.get(brother);
					boolean obviousShortestPath = false;
					if (brotherPath.species.contains(visit)){
						if (brother.priority < dock.priority){
							brotherPath.species.add(dock);
							brotherPath.updatePriority(brother.priority, memo, g.unvisited);
							obviousShortestPath = true;
						}
					}
					if (bestFamily.species.contains(brother)){
						if (visit.priority < dock.priority){
							bestFamily.species.add(dock);
							bestFamily.updatePriority(visit.priority, memo, g.unvisited);
							obviousShortestPath = true;
						}
					}
					if (!obviousShortestPath){
						ReactionPathway unionPath = new ReactionPathway();
						unionPath.species.addAll(bestFamily.species);
						unionPath.species.addAll(brotherPath.species);
						double edgePriority = visit.priority;
						for(GraphEdge rxn : bestFamily.reactionsInOrder){
							unionPath.reactionsInOrder.add(rxn);
						}
						for(GraphEdge rxn : brotherPath.reactionsInOrder){
							boolean alreadyAdded = false;
							for(GraphEdge q : bestFamily.reactionsInOrder){
								if (q == rxn){
									alreadyAdded = true;
									break;
								}
							}
							if (!alreadyAdded){
								unionPath.reactionsInOrder.add(rxn);
								edgePriority += 1. / rxn.k;
							}
						}
						if (edgePriority > dock.priority){
							throw new RuntimeException();
						}
						unionPath.species.add(dock);
						if (edgePriority < dock.priority){
							unionPath.updatePriority(edgePriority, memo, g.unvisited);
						}
					}
				}
			}

			//Visit edges
			for(GraphEdge edge : visit.neighbors){
				GraphNode towards = edge.towards;
				if (edge.k <= 0){
					continue;
				}
				double edgePriority = visit.priority + 1. / edge.k;
				if (edgePriority < towards.priority){
					ReactionPathway newFamily = new ReactionPathway();
					newFamily.species.addAll(bestFamily.species);
					newFamily.species.add(towards);
					if (towards.isBiMolecular()){
						for(GraphNode component : ((BimolecularNode)towards).associate){
							newFamily.species.add(component);
						}
					}
					newFamily.reactionsInOrder.addAll(bestFamily.reactionsInOrder);
					newFamily.reactionsInOrder.add(edge);
					newFamily.updatePriority(edgePriority, memo, g.unvisited);
				}
			}
		}	
		
		in = new Scanner(System.in);
		while(true){
			System.out.println("All-Shortest-Paths solved. Please enter in the name of a target:");
			String targetS = in.nextLine();
			GraphNode target = g.allSingles.get(targetS);
			if (target==null){
				continue;
			}
			ReactionPathway reactionPathway = memo.get(target);
			if (reactionPathway==null){
				continue;
			}
			TreeSet<GraphNode> actuallyUsed = new TreeSet(new StructureComparator());
			//Output a .s file of the graph with optimum path labeled...
			for(GraphEdge p : reactionPathway.reactionsInOrder){
				actuallyUsed.add(p.reverse.towards);
				actuallyUsed.add(p.towards);
			}
			for(GraphNode p : actuallyUsed){
				System.out.print(p.structureString+"; ");
			}
			System.out.println();
			for(GraphNode p : actuallyUsed){
				if (p.isBiMolecular()){
					for(GraphNode a : ((BimolecularNode)p).associate){
						System.out.println(p.structureString+" [shape=point]");
						System.out.println(p.structureString+" -> "+a.structureString+" [dir=none]");
					}
				}
			}
			for(GraphEdge p : reactionPathway.reactionsInOrder){
				System.out.println(p.reverse.towards.structureString+" -> "+p.towards.structureString);
			}
			for(GraphEdge p : reactionPathway.reactionsInOrder){
				System.out.println(p.type);
			}
		}
	}
}
	