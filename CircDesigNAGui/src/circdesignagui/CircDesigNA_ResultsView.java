package circdesignagui;

import java.awt.BorderLayout;

import javax.swing.JPanel;

import circdesigna.SequenceDesigner.AlternativeResult;

public class CircDesigNA_ResultsView extends JPanel{
	private CircDesigNA_Context context;
	
	private String newDomainDefs = null;
	
	public CircDesigNA_ResultsView(final CircDesigNA_Context context) {
		this.context = context;
	}
	public void init(){
		setLayout(new BorderLayout());

		final DnaDesignOutputPanel output = new DnaDesignOutputPanel(context.getDesigner());
		add(new CircDesigNA_BasicView(context, output, "Results", 
				"Return to options", "View design options",
				"Design", "Return to the design view keeping the current sequence.") {
			public void forward() {
				AlternativeResult ar = output.getBestChild();
				if (ar != null){
					String newDomainDefs = context.getDesigner().getResult(ar, new boolean[]{true});
					context.openDesignViewWithDomainDefs(newDomainDefs);
				} else {
					context.openDesignView();
				}
			}
			public void back() {
				//context.openRunningDesignerView();
				context.openDesignOptionsView();
			}
		});
		
		/*
		Auto update
		new Thread(){
			public void run(){
				while(context.getCurrentView() == CircDesigNA_ResultsView.this){
					output.fetchResults();
					try {
						Thread.sleep(100);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		}.start();
		*/
		
		validate();
	}
}
