package circdesignagui.TripleSim;

import java.io.PrintWriter;

import circdesigna.TripleSim.TripleSim;
import circdesigna.TripleSim.ReactionGraph3X.BimolecularNode;
import circdesigna.TripleSim.ReactionGraph3X.Graph;
import circdesigna.TripleSim.ReactionGraph3X.GraphEdge;
import circdesigna.TripleSim.ReactionGraph3X.GraphNode;
import circdesigna.config.CircDesigNAConfig;

public class TripleSimStability {
	public static void main(String[] args){
		CircDesigNAConfig config = new CircDesigNAConfig();
		Graph g = new Graph(config);
		final GraphNode aNode = new GraphNode("A");
		aNode.index = 0;
		g.allSingles.put("A",aNode);
		final BimolecularNode twoNodes = new BimolecularNode(aNode, aNode);
		g.allDockings.put("B", twoNodes);
		GraphEdge nReaction = g.addReaction(aNode,twoNodes);
		nReaction.k = 1;
		nReaction.type="Exponential Growth";
		
		TripleSim ts = new TripleSim();
		aNode.initialConc = 1;
		PrintWriter out = new PrintWriter(System.out);
		ts.updatePriorities(g, .001, 1, out, -1);
		out.flush();
	}
}
