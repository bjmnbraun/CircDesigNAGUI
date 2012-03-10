package circdesignagui.TripleSim;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import processing.core.PApplet;
import processing.xml.XMLElement;
import processing.xml.XMLWriter;
import circdesigna.TripleSim.ReactionGraph3X.BimolecularNode;
import circdesigna.TripleSim.ReactionGraph3X.Graph;
import circdesigna.TripleSim.ReactionGraph3X.GraphEdge;
import circdesigna.TripleSim.ReactionGraph3X.GraphNode;


public class TripleSimSBML {
	private XMLElement xml;
	public TripleSimSBML(String source, Graph add){
		PApplet got = new PApplet();
		got.init();
		xml = new XMLElement(got, source);
		
		//System.out.println(xml.toString(true));
		
		{
			XMLElement model = xml.getChild(0);
			//Leave units alone
			//Leave compartments alone
			Map<String, String> speciesLUT = new HashMap<String, String>();
			addSpecies(add, model.getChild("listOfSpecies"), speciesLUT);
			addReactions(add, model.getChild("listOfParameters"), model.getChild("listOfReactions"), speciesLUT);
		}
	}
	private void addSpecies(Graph g, XMLElement listOfSpecies, Map<String, String> speciesLUT) {
		String namespace = "http://www.sbml.org/sbml/level2/version4";
		int id = 1;
		for(GraphNode q : g.allSingles.values()){
			XMLElement species = new XMLElement();
			species.setName("species");
			String spid = "species_"+id++;
			speciesLUT.put(q.toString(),spid);
			species.setAttribute("id", spid);
			species.setAttribute("name", q.toString());
			species.setAttribute("compartment", "compartment_1");
			species.setAttribute("initialConcentration", String.format("%.3e",q.initialConc));
			listOfSpecies.addChild(species);
		}
	}
	private void addReactions(Graph g, XMLElement listOfParameters, XMLElement listOfReactions, Map<String, String> speciesLUT) {
		Map<String, String> ratesLUT = new HashMap();
		int pid = 1;
		int rid = 1;
		String baseKinetic = listOfReactions.getChild(0).getChild("kineticLaw").toString(false);
		while(listOfReactions.getChildCount()>0){
			listOfReactions.removeChildAtIndex(0);
		}
		while(listOfParameters.getChildCount()>0){
			listOfParameters.removeChildAtIndex(0);
		}
			
		for(GraphEdge edge : g.edges){
			addParameter(listOfParameters,edge.k, pid++, ratesLUT);
			addParameter(listOfParameters,edge.reverse.k, pid++, ratesLUT);
			
			XMLElement reaction = new XMLElement();
			String rname = "reaction_"+rid++;
			reaction.setName("reaction");
			reaction.setAttribute("id",rname);
			reaction.setAttribute("name",edge.type);
			XMLElement listOfReactants = new XMLElement();
			listOfReactants.setName("listOfReactants");
			addSpeciesReferences(edge.reverse.towards, listOfReactants, speciesLUT);
			XMLElement listOfProducts = new XMLElement();
			listOfProducts.setName("listOfProducts");
			addSpeciesReferences(edge.towards, listOfProducts, speciesLUT);
			XMLElement kineticLaw =  new XMLElement(baseKinetic);
			XMLElement[] reactProd = kineticLaw.getChild("math").getChild("apply").getChild("apply").getChildren("apply");
			addOneWayKinetic(reactProd[0],edge, ratesLUT, speciesLUT);
			addOneWayKinetic(reactProd[1],edge.reverse, ratesLUT, speciesLUT); 
			
			reaction.addChild(listOfReactants);
			reaction.addChild(listOfProducts);
			reaction.addChild(kineticLaw);
			listOfReactions.addChild(reaction);
		}
	}
	private void addOneWayKinetic(XMLElement element, GraphEdge edge, Map<String, String> ratesLUT, Map<String, String> speciesLUT) {
		//Clear the element
		while(element.getChildCount()>0){
			element.removeChildAtIndex(0);
		}
		element.addChild(new XMLElement("<times/>"));
		element.addChild(new XMLElement("<ci> "+ratesLUT.get(rf(edge.k))+" </ci>"));
		
		for(GraphNode s : getSpecies(edge.reverse.towards)){
			element.addChild(new XMLElement("<ci> "+speciesLUT.get(s.toString())+" </ci>"));
		}
	}
	private GraphNode[] getSpecies(GraphNode v){
		if (v instanceof BimolecularNode){
			return ((BimolecularNode) v).associate;
		}
		return new GraphNode[]{v};
	}
	private void addSpeciesReferences(GraphNode v,
			XMLElement listOfReactants, Map<String, String> speciesLUT) {
		for(GraphNode s : getSpecies(v)){
			XMLElement ref = new XMLElement();
			ref.setName("speciesReference");
			ref.setAttribute("species", speciesLUT.get(s.toString()));
			listOfReactants.addChild(ref);
		}
	}
	private void addParameter(XMLElement listOfParameters, double k, int id, Map<String, String> ratesLUT) {
		XMLElement par = new XMLElement();
		par.setName("parameter");
		String kon = rf(k);
		String kon_ = "parameter_"+id;
		par.setAttribute("id", kon_);
		par.setAttribute("name", kon);
		par.setAttribute("value", kon);
		ratesLUT.put(kon, kon_);
		
		listOfParameters.addChild(par);
	}
	private String rf(double k) {
		return String.format("%e",k);
	}
	public void write(String out2) {
		try {
			FileWriter out = new FileWriter(new File(out2));
			//System.out.println(xml.toString(true));
			out.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n");
			XMLWriter write = new XMLWriter(out);
			write.write(xml, true);
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
