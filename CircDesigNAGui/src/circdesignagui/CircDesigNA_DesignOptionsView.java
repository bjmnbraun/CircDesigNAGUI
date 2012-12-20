package circdesignagui;

import java.awt.BorderLayout;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JPanel;
import javax.swing.border.EtchedBorder;

import circdesigna.CircDesigNAOptions;
import circdesigna.SequenceDesigner;

public class CircDesigNA_DesignOptionsView extends JPanel{
	private CircDesigNA_Context context;
	public CircDesigNA_DesignOptionsView(final CircDesigNA_Context context) {
		this.context = context;
		
		SequenceDesigner<CircDesigNAOptions> cDesign = context.getDesigner();
		
		setLayout(new BorderLayout());
		
		String backText = "Design";
		String backDetailed = "Return to the design view.";
		String forwardText = "Run";
		String forwardDetailed = "Run the designer with these options.";
		if (cDesign.isRunning()){
			backDetailed = "Abort the designer and return to the design view.";
			//forwardText = "Interactive Designer";
			forwardText = "Results";
			forwardDetailed = "View sequence design progress.";
		}


		DnaDesignOptionsPanel inner = new DnaDesignOptionsPanel(cDesign);
		add(new CircDesigNA_BasicView(context, inner, "Run Options", backText, backDetailed, forwardText, forwardDetailed) {
			public void forward() {
				//context.openRunningDesignerView();
				context.openResultsView();
			}
			
			public void back() {
				context.openDesignView();
			}
		}, BorderLayout.NORTH);
		validate();
	}
	
	
}
