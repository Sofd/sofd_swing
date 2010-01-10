package de.sofd.swing;

import java.awt.Color;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

public abstract class AbstractFramedSelectionGridListComponentFactory implements GridListComponentFactory {

    private Border unselectedBorder, selectedBorder;

    public AbstractFramedSelectionGridListComponentFactory() {
        this (new EmptyBorder(2,2,2,2), new LineBorder(Color.YELLOW, 2));
    }

    public AbstractFramedSelectionGridListComponentFactory(Border unselectedBorder, Border selectedBorder) {
        super();
        this.unselectedBorder = unselectedBorder;
        this.selectedBorder = selectedBorder;
    }

    @Override
    public void setSelectedStatus(JGridList source, JPanel parent, Object modelItem, boolean selected, JComponent component) {
        component.setBorder(selected ? selectedBorder : unselectedBorder);
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

    @Override
    public boolean canReuseComponents() {
        return false;
    }

}