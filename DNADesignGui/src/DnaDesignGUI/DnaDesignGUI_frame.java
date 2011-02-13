package DnaDesignGUI;

import java.awt.BorderLayout;

import javax.swing.JFrame;

public class DnaDesignGUI_frame extends JFrame{
	public static void main(String[] args){
		new DnaDesignGUI_frame();
	}
	public DnaDesignGUI_frame(){
		setSize(640,480);
		addComponents();
	}
	private void addComponents() {
		setLayout(new BorderLayout());
		final DnaDesignGui comp = new DnaDesignGui();
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		add(comp);
		comp.init();
		setVisible(true);
		comp.start();
	}
}
