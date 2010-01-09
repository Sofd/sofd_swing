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
     * {@link JGridList} needs to be created (or reused -- see below).
     * <p>
     * The component should be created such that it visualizes the item in the
     * unselected state. If the component is to show a "selected" state,
     * {@link #setSelectedStatus(JGridList, JPanel, Object, boolean, JComponent) setSelectedStatus}
     * will be called immediately afterwards.
     * <p>
     * If {@link #canReuseComponents() } is true, this method <i>may</i> be called
     * to reuse a previously created component for displaying modelItem, rather
     * that creating a new component. These two cases can be told apart by checking
     * whether parent contains a child component. If it does, that's the component
     * to be reused -- i.e., #createComponent() may modify that child component
     * to represent modelItem. If there's no child component in parent, no reusing
     * can take place, and createComponent must create a new child component in
     * parent for representing modelItem.
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

    /**
     * Tell whether or not this GridListComponentFactory supports reusing the JComponent
     * it creates (via {@link #createComponent(de.sofd.swing.JGridList, javax.swing.JPanel, java.lang.Object) })
     * for other model elements later. If this method returns false, the JGridList will always call
     * #createComponent with an empty "parent" JPanel, and #createComponent must create and new
     * component in the parent for representing the model element. If canReuseComponents()
     * returns true, the JGridList <i>may</i> call #createComponent with a "parent" JPanel
     * that contains a previously created component representing a different model element
     * (the one that was previously being represented by that component).
     * This gives the component factory the opportunity to <i>modify</i> this existing
     * component to represent the newly passed modelItem. This can be beneficial for performance
     * if creating new components is expensive and reusing existing ones is cheap.
     * <p>
     *
     * <p>
     * The return value of this method should not change through the lifetime of <i>this</i>.
     *
     * @return
     */
    boolean canReuseComponents();
}
