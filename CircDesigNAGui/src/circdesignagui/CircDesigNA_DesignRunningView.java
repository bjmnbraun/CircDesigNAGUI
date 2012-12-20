package circdesignagui;

import java.awt.BorderLayout;
import java.awt.Component;

import javax.swing.JPanel;

import processing.core.PApplet;

public class CircDesigNA_DesignRunningView extends JPanel{
	private CircDesigNA_Context context;
	private DesignDisplay display;
	public CircDesigNA_DesignRunningView(CircDesigNA_Context context){
		super();
		this.context = context;
	}
	public void init(){
		display = new DesignDisplay();
		setLayout(new BorderLayout());
		
		JPanel displayHolder = new JPanel();
		displayHolder.setLayout(new BorderLayout());
		displayHolder.add(display);
	
		Component header = new CircDesigNA_BasicView(context, displayHolder, "Interactive Design",
				"Options", "Review the selected design options.",
				"Results", "View sequence design results."
				){
					public void back() {						
						context.openDesignOptionsView();
					}
					public void forward() {
						context.openResultsView();
					}
		};
		add(header);
		
		validate();
		
		display.setPreferredSize(display.getSize());
		display.init();
		display.start();
	}
	public class DesignDisplay extends PApplet{
		public void setup(){
			int w = getPreferredSize().width, h = getPreferredSize().height;
			size(w,h,P3D);
			frameRate(60);
		}
		public void draw(){
			background(255);
			rect(0,0,width,height);
			lights();
			pushMatrix();
			translate(mouseX, mouseY);
			fill(200);
			specular(0.5f);
			rotateY(frameCount/20f);
			rotateX(.5f);
			box(100,100,100);
			popMatrix();
		}
	}
}
