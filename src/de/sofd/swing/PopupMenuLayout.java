package de.sofd.swing;

import java.awt.*;
import javax.swing.*;
import java.awt.event.*;

/**
 * Für Toolbars mit Popupmenu.
 * Möglicherweise unfertig und noch nicht universell einsetzbar...
 * Bisher nur Support für JButton, JToggleButton und JCheckBox...
 * @author oliver
 */
public class PopupMenuLayout implements java.awt.LayoutManager {

    private JPopupMenu popupMenu = new JPopupMenu();
    private JButton popupButton = new JButton(new PopupAction());

    /** Creates a new instance of PopupMenuLayout */
    public PopupMenuLayout() {
    }

    /** If the layout manager uses a per-component string,
     * adds the component <code>comp</code> to the layout,
     * associating it
     * with the string specified by <code>name</code>.
     *
     * @param name the string to be associated with the component
     * @param comp the component to be added
     */
    public void addLayoutComponent(String name, Component comp) {
    }

    /**
     * Lays out the specified container.
     * @param parent the container to be laid out
     */
    public void layoutContainer(Container parent) {
        //remove popup button and menu
        parent.remove(popupButton);
        popupMenu.removeAll();

        //  Position all buttons in the container
        Insets insets = parent.getInsets();
        int x = insets.left;
        int y = insets.top;
        int spaceUsed = insets.right + insets.left;

        Dimension parentSize = parent.getSize();
        int parentHeight2 = parentSize.height - insets.top - insets.bottom;

        for (int i = 0; i < parent.getComponentCount(); i++) {
            Component aComponent = parent.getComponent(i);
            Dimension prefSize = aComponent.getPreferredSize();

            aComponent.setSize(prefSize);
            aComponent.setLocation(x, (int) (y + aComponent.getAlignmentY() * (parentHeight2 - prefSize.height)));
            aComponent.setVisible(true);

            x += prefSize.width;
            spaceUsed += prefSize.width;

            if (aComponent instanceof IComponentInPopupMenuLayout) {
                IComponentInPopupMenuLayout iComp = (IComponentInPopupMenuLayout) aComponent;
                iComp.syncOriginal();
            }
        }

        if (spaceUsed > parentSize.width) {
            parent.add(popupButton);
            popupButton.setSize(popupButton.getPreferredSize());
            int popupX = parentSize.width - insets.right - popupButton.getSize().width;
            popupButton.setLocation(popupX, y);
            spaceUsed += popupButton.getSize().width;
        }

        //  Remove buttons that don't fit and add to the popup menu

        int lastVisibleButtonIndex = 1;

        while (spaceUsed > parentSize.width) {
            lastVisibleButtonIndex++;
            int last = parent.getComponentCount() - lastVisibleButtonIndex;

            Component component = parent.getComponent(last);
            component.setVisible(false);
            spaceUsed -= component.getSize().width;

            addComponentToPopup(component);
        }

    }

    private void addComponentToPopup(Component component) {
        if (component instanceof IComponentInPopupMenuLayout) {
            IComponentInPopupMenuLayout iComp = (IComponentInPopupMenuLayout) component;
            iComp.syncComponentInPopupMenu();
            popupMenu.insert(iComp.getComponentInPopupMenu(), 0);
        } else if (component instanceof JButton) {
            JButton button = (JButton) component;
            String text = button.getText();
            if (text == null || text.isEmpty()) {
                text = button.getToolTipText();
            }

            JMenuItem menuItem = new JMenuItem(text);
            menuItem.setActionCommand(button.getActionCommand());
            menuItem.setIcon(button.getIcon());

            ActionListener[] listeners = button.getActionListeners();

            for (int i = 0; i < listeners.length; i++) {
                menuItem.addActionListener(listeners[i]);
            }

            popupMenu.insert(menuItem, 0);
        } else if (component instanceof JToolBar.Separator) {
            popupMenu.insert(new JSeparator(), 0);
        }
    }


    /**
     * Calculates the minimum size dimensions for the specified
     * container, given the components it contains.
     * @param parent the component to be laid out
     * @see #preferredLayoutSize
     */
    public Dimension minimumLayoutSize(Container parent) {
        return popupButton.getMinimumSize();
    }

    /** Calculates the preferred size dimensions for the specified
     * container, given the components it contains.
     * @param parent the container to be laid out
     *
     * @see #minimumLayoutSize
     */
    public Dimension preferredLayoutSize(Container parent) {
        System.out.println("preferredLayoutSize");
        //  Calculate the width of all components in the container
        Dimension d = new Dimension();
        d.width += parent.getInsets().right + parent.getInsets().left;

        for (int i = 0; i < parent.getComponents().length; i++) {
            Component component = parent.getComponent(i);

            if (component != popupButton) {
                component.setVisible(true);
                d.width += component.getPreferredSize().width;
                d.height = Math.max(d.height, component.getPreferredSize().height);
            }
        }

        d.height += parent.getInsets().top + parent.getInsets().bottom + 5;
        return d;
    }

    /** Removes the specified component from the layout.
     * @param comp the component to be removed
     */
    public void removeLayoutComponent(Component comp) {
    }

    protected class PopupAction extends AbstractAction {

        public PopupAction() {
            super(">>");
        }

        public void actionPerformed(ActionEvent e) {
            JComponent component = (JComponent) e.getSource();
            popupMenu.show(component, 0, component.getHeight());
        }
    }
}
