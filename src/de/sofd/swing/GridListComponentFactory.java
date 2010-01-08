package de.sofd.swing;

import javax.swing.JComponent;
import javax.swing.JPanel;

/**
 * Interface for "component factories" for {@link JGridList JGridLists}. A
 * JGridList is associated with exactly one GridListComponentFactory, which it
 * consults whenever a GUI component representing an item in the
 * {@link JGridList} needs to be created, have its "selected" state switched, or
 * removed.
 * 
 * @author Olaf Klischat
 */
// TODO: better name: ListCellComponentFactory
public interface GridListComponentFactory {
    /**
     * Called whenever a GUI component representing an item in the
     * {@link JGridList} needs to be created.
     * <p>
     * The component should be created such that it visualizes the item in the
     * unselected state. If the component is to show a "selected" state,
     * {@link #setSelectedStatus(JGridList, JPanel, Object, boolean, JComponent) setSelectedStatus}
     * will be called immediately afterwards.
     * 
     * @param source
     *            JGridList for which the component is to be created
     * @param parent
     *            JPanel inside which to create the component
     * @param modelItem
     *            item (of the JGridList's ListModel) that the component should
     *            represent
     * @return the created component
     */
    JComponent createComponent(JGridList source, JPanel parent, Object modelItem);

    /**
     * The "selected" state of a component previously created via
     * {@link #createComponent(JGridList, JPanel, Object) createComponent} is to
     * be set. The implementation should change the visual representation of the
     * component to reflect the new selected state (change background color,
     * draw a border or whatever).
     * 
     * @param source
     *            JGridList for which the component is to be created
     * @param parent
     *            JPanel inside which the component resides. The same panel was
     *            passed to
     *            {@link #createComponent(JGridList, JPanel, Object) createComponent}
     *            when the component was created
     * @param modelItem
     *            item (of the JGridList's ListModel) that the component
     *            represents. The same item was passed to
     *            {@link #createComponent(JGridList, JPanel, Object) createComponent}
     *            when the component was created
     * @param selected
     *            new selected state (true=selected, false=unselected)
     * @param component
     *            the component. It's the one that was returned by
     *            {@link #createComponent(JGridList, JPanel, Object) createComponent}
     *            when the component was created. This is meant as a convenience
     *            so the GridListComponentFactory implementation doesn't have to
     *            keep track of the association between model items and
     *            components itself
     */
    void setSelectedStatus(JGridList source, JPanel parent, Object modelItem, boolean selected, JComponent component);

    void parentUiStateChanged(JGridList source, JPanel parent, /*Object modelItem, */JComponent component);
    
    /**
     * A component previously created via
     * {@link #createComponent(JGridList, JPanel, Object) createComponent} is to
     * be deleted. You may do any necessary cleanup handling here and remove the
     * component. You may also do nothing, in which case the JGridList will
     * remove the component itself.
     * 
     * @param source
     *            JGridList for which the component is to be created
     * @param parent
     *            JPanel inside which the component resides. The same panel was
     *            passed to
     *            {@link #createComponent(JGridList, JPanel, Object) createComponent}
     *            when the component was created
     * @param modelItem
     *            item (of the JGridList's ListModel) that the component
     *            represents. The same item was passed to
     *            {@link #createComponent(JGridList, JPanel, Object) createComponent}
     *            when the component was created
     * @param component
     *            the component. It's the one that was returned by
     *            {@link #createComponent(JGridList, JPanel, Object) createComponent}
     *            when the component was created. This is meant as a convenience
     *            so the GridListComponentFactory implementation doesn't have to
     *            keep track of the association between model items and
     *            components itself
     */
    void deleteComponent(JGridList source, JPanel parent, Object modelItem, JComponent component);

    boolean canReuseComponents();
}
