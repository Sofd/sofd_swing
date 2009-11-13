package de.sofd.swing;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.UIManager;

public abstract class AbstractBgColorSelectionGridListComponentFactory implements GridListComponentFactory {

    public AbstractBgColorSelectionGridListComponentFactory() {
        super();
    }

    @Override
    public void setSelectedStatus(JGridList source, JPanel parent, Object modelItem, boolean selected, JComponent component) {
        if (selected) {
            component.setForeground(UIManager.getColor("List.selectionForeground"));
            component.setBackground(UIManager.getColor("List.selectionBackground"));
        } else {
            component.setForeground(source.getForeground());
            component.setBackground(source.getBackground());
        }
    }

    @Override
    public void parentUiStateChanged(JGridList source, JPanel parent, JComponent component) {
        component.setBackground(parent.getBackground());
        component.setForeground(parent.getForeground());
    }
    
    @Override
    public void deleteComponent(JGridList source, JPanel parent, Object modelItem, JComponent component) {
        // do nothing; leave it to the default behaviour
    }

}