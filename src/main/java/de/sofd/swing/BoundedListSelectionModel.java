package de.sofd.swing;

import javax.swing.ListSelectionModel;

/**
 * Interface for {@link ListSelectionModel}s that have optional upper and/or
 * lower bounds for their selection.
 * 
 * @author olaf
 * 
 */
public interface BoundedListSelectionModel extends ListSelectionModel {

    public abstract int getLowerBound();

    public abstract void setLowerBound(int lowerBound);

    public abstract void disableLowerBound();

    public abstract int getUpperBound();

    public abstract void setUpperBound(int upperBound);

    public abstract void disableUpperBound();

    public abstract int[] getBounds();

    public abstract void setBounds(int lower, int upper);

    public abstract void disableBounds();

}