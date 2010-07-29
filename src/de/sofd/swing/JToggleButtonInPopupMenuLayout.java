package de.sofd.swing;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JToggleButton;

/**
 * Help class for toggle button in RoiToolPanel with PopupMenuLayout.
 * In popup menu each toggle button has a corresponding JCheckBoxMenuItem,
 * which should simulate this toggle button.
 * @author oliver
 */
public class JToggleButtonInPopupMenuLayout extends JToggleButton implements IComponentInPopupMenuLayout {
    protected JCheckBoxMenuItem checkBoxMenuItem;

    public JToggleButtonInPopupMenuLayout() {
        super();
    }

    public void createComponentInPopupMenu() {
        String text = this.getText();
        if (text == null || text.isEmpty())
            text = this.getToolTipText();

        checkBoxMenuItem = new JCheckBoxMenuItem(text, this.isSelected());
        checkBoxMenuItem.setIcon( this.getIcon() );
        checkBoxMenuItem.setActionCommand(this.getActionCommand());
        checkBoxMenuItem.setToolTipText(this.getToolTipText());

        this.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                syncComponentInPopupMenu();
            }
        });

        checkBoxMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                syncOriginal();
            }
        });
    }

    public JCheckBoxMenuItem getComponentInPopupMenu() {
        return checkBoxMenuItem;
    }

    public void syncOriginal() {
        if (isSelected() != checkBoxMenuItem.isSelected())
            doClick();
    }

    public void syncComponentInPopupMenu() {
        checkBoxMenuItem.setSelected(isSelected());
    }
   
}