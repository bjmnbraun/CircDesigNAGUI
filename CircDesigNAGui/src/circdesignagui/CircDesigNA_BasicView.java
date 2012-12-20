package circdesignagui;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.EtchedBorder;

public abstract class CircDesigNA_BasicView extends JPanel{
	
	public CircDesigNA_BasicView(CircDesigNA_Context context, JComponent inner, String title, String backText, final String backDetailed, 
			String forwardText, final String forwardDetailed) {
		Box layout = Box.createVerticalBox();
		
		Box buttonBox = Box.createHorizontalBox();

		//buttonBox.setBackground(context.getThemedApplet().THEMECOL0);
		//buttonBox.setOpaque(true);
		
		final JButton pageDescription = new JButton("<html></html>"){{
				setEnabled(false);
			}};
		
		buttonBox.add(new JButton("\u2190 "+backText){{
		addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				back();
			}
		});
		addMouseListener(new MouseAdapter() {
			public void mouseExited(MouseEvent e) {
				pageDescription.setText("<html></html>");
			}
			public void mouseEntered(MouseEvent e) {
				pageDescription.setText("<html>"+backDetailed+"</html>");
			}
		});
		}});

		buttonBox.add(pageDescription);
		pageDescription.setHorizontalAlignment(SwingConstants.CENTER);
		
		buttonBox.add(new JButton(forwardText+" \u2192"){{addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				forward();
			}
		});
		addMouseListener(new MouseAdapter() {
			public void mouseExited(MouseEvent e) {
				pageDescription.setText("");
			}
			public void mouseEntered(MouseEvent e) {
				pageDescription.setText("<html>"+forwardDetailed+"</html>");
			}
		});
		}});
		
		layout.add(buttonBox);

		inner.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
		layout.add(context.getThemedApplet().skinGroup(inner, title));
		
		setLayout(new BorderLayout());
		add(layout);
		
		validate();
	}
	public abstract void back();
	public abstract void forward();
}
