package de.sofd.swing;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;


public class DefaultGridListComponentFactory extends AbstractFramedSelectionGridListComponentFactory {

    @Override
    public JComponent createComponent(JGridList source, JPanel parent, Object modelItem) {
        JLabel l = new JLabel(""+modelItem);
        parent.add(l);
        return l;
    }
}
