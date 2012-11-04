package de.sofd.swing;

import java.awt.Component;

/**
 * Interface for a component used in PopupMenuLayout.
 * @author oliver
 */
public interface IComponentInPopupMenuLayout {

    /**
     * Create corresponding component in popup menu.
     */
    public void createComponentInPopupMenu();

    /**
     * Returns corresponding component in popup menu and synchronizes 
     * its state.
     * @return Corresponding component in popup menu.
     */
    public Component getComponentInPopupMenu();

    /**
     * Synchronizes original component from component in popup menu.
     */
    public void syncOriginal();

    /**
     * Synchronizes component in popup menu from original component.
     */
    public void syncComponentInPopupMenu();
}