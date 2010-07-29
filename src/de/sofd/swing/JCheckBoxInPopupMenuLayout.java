package de.sofd.swing;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JCheckBox;
import javax.swing.JCheckBoxMenuItem;

/**
 * Help class for check box in RoiToolPanel with PopupMenuLayout.
 * In popup menu each check box has a corresponding JCheckBoxMenuItem,
 * which should simulate this check box.
 * @author oliver
 */
public class JCheckBoxInPopupMenuLayout extends JCheckBox implements IComponentInPopupMenuLayout {
    protected JCheckBoxMenuItem checkBoxMenuItem;

    public JCheckBoxInPopupMenuLayout(String title) {
        super(title);
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