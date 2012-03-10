package circdesignagui.TripleSim;

import java.io.PrintWriter;

import circdesigna.TripleSim.ReactionGraph3X.BimolecularNode;
import circdesigna.TripleSim.ReactionGraph3X.Graph;
import circdesigna.TripleSim.ReactionGraph3X.GraphEdge;
import circdesigna.TripleSim.ReactionGraph3X.GraphNode;

public class TripleSimGraphVis {
	public TripleSimGraphVis(){
		
	}
	public void write(Graph g, PrintWriter out){
		out.println("digraph G{");
		for(BimolecularNode pair : g.allDockings.values()){
			if (pair.neighbors.isEmpty()){
				continue;
			}
			for(GraphNode q : pair.associate){ 
				out.println(q.index+" -> "+pair.index+" [dir=none, len=1]");
			}
			out.println(pair.index+" [shape=square,label=\"\",height=.1,width=.1]");
		}
		for(GraphNode p : g.allSingles.values()){
			String color = "black";
			if (p.initialConc > 0){
				color = "purple";
			}
			out.println(p.index+" [shape=point, color="+color+", label=\""+p.structure.getStructureString()+"\"]");
		}
		for(GraphEdge p : g.edges){

			//System.out.println(p.reverse.towards+" "+p.type);
			out.println(p.reverse.towards.index+" -> "+p.towards.index);
			
			String k1 = String.format("%.3e",p.k);
			String k2 = String.format("%.3e",p.reverse.k);
			if (p.type.startsWith("Branch Migration")){
				String dir = "none";
				if (p.k > 0){
					dir = "forward";
					if (p.reverse.k > 0){
						dir = "both";
					}
				} else {
					dir = "back";
				}
				out.print(" [dir="+dir+", color=\"green\" label=\""+k1+"\"]");
			} else {
				String addStr = ", label=\""+k1+":"+k2+"\"";
				out.print(" [dir=both, color=\"red:blue\""+addStr+"]");
			}
			out.println();
		}
		out.println("}");
	}
}
