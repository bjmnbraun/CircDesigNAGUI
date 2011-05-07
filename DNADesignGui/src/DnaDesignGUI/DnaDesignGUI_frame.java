package DnaDesignGUI;

import java.awt.BorderLayout;

import javax.swing.JFrame;

/**
 * For running the web applet in a local window (i.e., not in a web browser)
 * Pass in the width and height of the window you want to the applet to run in.
 * 
 * You may also want to specify the theme colors, as the webpage does, with
 * -Dthemecol0=FF0000 -Dthemecol1=0020D0 etc.
 * 
 * @see Color.decode
 */
public class DnaDesignGUI_frame extends JFrame{
	public static void main(String[] args){
		if (args.length < 2){
			System.err.println("Please provide width and height as arguments to this class.");
		}
		int w = new Integer(args[0]);
		int h = new Integer(args[1]);
		new DnaDesignGUI_frame(w,h);
	}
	public DnaDesignGUI_frame(int w, int h){
		setSize(w,h);
		addComponents();
	}
	private void addComponents() {
		setLayout(new BorderLayout());
		final DnaDesignGui comp = new DnaDesignGui(){
			public String getParameter(String key){
				return System.getProperty(key);
			}
		};
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		add(comp);
		comp.init();
		setVisible(true);
		comp.start();
	}
}
