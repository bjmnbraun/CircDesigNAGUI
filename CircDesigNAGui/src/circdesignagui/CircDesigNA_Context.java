package circdesignagui;

import java.applet.Applet;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;

import javax.swing.JButton;
import javax.swing.JPanel;

import circdesigna.CircDesigNA;
import circdesigna.CircDesigNAOptions;
import circdesigna.SequenceDesigner;

/**
 * The top-level object for CircDesigNA. This holds information that gets persisted between views.
 */
public class CircDesigNA_Context extends Applet{
	public CircDesigNA_Context(){
	}
	
	//CircDesigNA_DesignView:
	private boolean started = false;
	public void start(){
		if(started) return;
		started = true;

		setLayout(new BorderLayout());
		openDesignView();
	}
	private void theme(Container view) {
		for(Component c : view.getComponents()){
			if (c instanceof Container){
				theme((Container)c);
			}
			if (c instanceof JButton){
				ButtonSkin.process(designView, (JButton)c);
			}
		}
		if (view instanceof JPanel){
			designView.skinPanel((JPanel)view);
		}
	}
	/**
	 * Adds, initializes, resizes, and validates a component to be added.
	 */
	private void aIVR(Component view) {
		add(view);
		view.setPreferredSize(getSize());
		validate();
	}
	
	private Component currentView;
	private void closeView() {
		if (currentView != null){
			remove(currentView);
		}
		currentView = null;
	}
	private CircDesigNA_DesignView.Output designView_data;
	private SequenceDesigner<CircDesigNAOptions> cDesign;
	private CircDesigNA_DesignView designView;
	public void setDesignViewOutput(CircDesigNA_DesignView.Output output) {
		this.designView_data = output;
		
		if (cDesign != null){
			System.err.println("Warning: old designer was not cleaned up. Overwriting.");
		}
		//Start a new designer
		cDesign = CircDesigNA.getDefaultDesigner(designView_data.molDefs,
				designView_data.domainDefs, designView_data.cfg);
	}
	/**
	*  Return a reference to the current design.
	*/
	public SequenceDesigner<CircDesigNAOptions> getDesigner() {
		return cDesign;	
	}
	/**
	* Returns whatever top-level component is currently being displayed. These are called "views."
	*/
	public Component getCurrentView() {
		return currentView;
	}
	public ThemedApplet getThemedApplet() {
		return designView;
	}
	public void openDesignViewWithDomainDefs(String modDomainDefs){
		designView_data.domainDefs = modDomainDefs;
		openDesignView();
	}
	public void openDesignView() {
		closeView();
		if (cDesign != null){
			cDesign.abort();
			cDesign = null;
		}	
		
		//Sort of wasteful to create a new one, but its safe at least.
		designView = new CircDesigNA_DesignView(this){
			public String getParameter(String key){
				return CircDesigNA_Context.this.getParameter(key);
			}
		};
		currentView = designView;
		aIVR(designView);
		designView.init();
		if (designView_data==null){
			designView.start();
		} else {
			designView.start(designView_data);
		}
	}
	
	
	public void openDesignOptionsView() {
		closeView();
		//Make sure you can't change options when the designer's started.
		CircDesigNA_DesignOptionsView designOptionsView = new CircDesigNA_DesignOptionsView(this);
		currentView = designOptionsView;
		aIVR(designOptionsView);
		//resultsView.init();
		theme(designOptionsView);
	}
	
	public void openRunningDesignerView() {
		closeView();
		
		cDesign.resume(); //Does nothing if already running
		
		CircDesigNA_DesignRunningView designRunningView = new CircDesigNA_DesignRunningView(this);
		currentView = designRunningView;
		aIVR(designRunningView);
		designRunningView.init();
		theme(designRunningView);
	}

	public void openResultsView() {
		closeView();
		
		cDesign.resume(); //Does nothing if already running
		
		CircDesigNA_ResultsView resultsView = new CircDesigNA_ResultsView(this);
		currentView = resultsView;
		aIVR(resultsView);
		resultsView.init();
		theme(resultsView);
	}
}
