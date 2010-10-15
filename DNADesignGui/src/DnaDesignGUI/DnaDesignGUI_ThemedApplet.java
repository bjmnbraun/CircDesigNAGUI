package DnaDesignGUI;

import java.applet.Applet;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;

public abstract class DnaDesignGUI_ThemedApplet extends Applet implements ModalizableComponent{
	public Color THEMECOL0,THEMECOL1,THEMECOL2,THEMECOL3,THEMECOL4;
	public void parseThemeColors(){
		THEMECOL0 = parseColorFromParam("themecol0");
		THEMECOL1 = parseColorFromParam("themecol1");
		THEMECOL2 = parseColorFromParam("themecol2");
		THEMECOL3 = parseColorFromParam("themecol3");
		THEMECOL4 = parseColorFromParam("themecol4");
	}

	public JPanel skinPanel(JPanel panel) {
		panel.setBackground(getBackground());
		return panel;
	}
	public JComponent skinGroup(final Component inner, String string) {
		JComponent inner2 = new JPanel(){
			{
				setOpaque(false);
				setLayout(new BorderLayout());
				add(inner, BorderLayout.CENTER);
			}
		    protected void paintComponent(Graphics g) {
		        int width = getWidth();
		        int height = getHeight();

				((Graphics2D)g).setRenderingHint(RenderingHints.KEY_ANTIALIASING, // Anti-alias!
				        RenderingHints.VALUE_ANTIALIAS_ON);
		        // Paint a rounded rectangle in the background.
		        g.setColor(Color.white);
		        int round = 15;
		        int topY = 1;
		        g.fillRoundRect(1, topY, width-3, height-topY-1, round,round);
		        g.setColor(THEMECOL4);
		        g.drawRoundRect(1, topY, width-3, height-topY-1, round,round);
		        super.paintComponent(g);
		    }
		};
		inner2.setBorder(BorderFactory.createTitledBorder(
				BorderFactory.createEmptyBorder(),
				string,
				TitledBorder.LEFT,
				TitledBorder.BELOW_TOP));
		return inner2;
	}

	private Color parseColorFromParam(String string) {
		try {
			string = getParameter(string);
		} catch (Throwable e){
			e.printStackTrace();
			return Color.black;
		}
		if (string==null){
			return Color.gray;
		}
		return Color.decode(string);
	}
}
