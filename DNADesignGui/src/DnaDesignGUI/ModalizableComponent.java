package DnaDesignGUI;

import javax.swing.JPanel;

public interface ModalizableComponent {
	public JPanel getModalPanel();
	public void addModalScale(Runnable runnable);
	public void removeAllModalScale();
}
