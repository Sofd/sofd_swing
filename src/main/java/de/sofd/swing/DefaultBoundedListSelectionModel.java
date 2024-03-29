package de.sofd.swing;

import javax.swing.DefaultListSelectionModel;
import javax.swing.event.ListSelectionListener;


/**
 * Default {@link BoundedListSelectionModel}.
 * 
 * @author olaf
 */
public class DefaultBoundedListSelectionModel implements BoundedListSelectionModel {

    protected DefaultListSelectionModel backend = new DefaultListSelectionModel();

    protected int[] bounds = new int[] {Integer.MIN_VALUE, Integer.MAX_VALUE};
    
    @Override
    public void addListSelectionListener(ListSelectionListener l) {
        backend.addListSelectionListener(l);
    }

    @Override
    public void addSelectionInterval(int index0, int index1) {
        int[] newInterval = intersect(new int[]{index0, index1}, bounds);
        if (newInterval != null) {
            backend.addSelectionInterval(newInterval[0], newInterval[1]);
        }
    }

    @Override
    public int getLowerBound() {
        return bounds[0];
    }
    
    @Override
    public void setLowerBound(int lowerBound) {
        setBounds(lowerBound, bounds[1]);
    }
    
    @Override
    public void disableLowerBound() {
        setLowerBound(Integer.MIN_VALUE);
    }
    
    @Override
    public int getUpperBound() {
        return bounds[1];
    }
    
    @Override
    public void setUpperBound(int upperBound) {
        setBounds(bounds[0], upperBound);
    }
    
    @Override
    public void disableUpperBound() {
        setUpperBound(Integer.MAX_VALUE);
    }
    
    @Override
    public int[] getBounds() {
        return new int[] {bounds[0], bounds[1]};
    }
    
    @Override
    public void setBounds(int lower, int upper) {
        if (lower > upper) {
            this.bounds[0] = Integer.MIN_VALUE;
            this.bounds[1] = Integer.MAX_VALUE;
        } else {
            this.bounds[0] = lower;
            this.bounds[1] = upper;
        }
        clipToBounds();
    }
    
    @Override
    public void disableBounds() {
        setBounds(Integer.MIN_VALUE, Integer.MAX_VALUE);
    }
    
    @Override
    public void clearSelection() {
        backend.clearSelection();
    }

    @Override
    public int getAnchorSelectionIndex() {
        return backend.getAnchorSelectionIndex();
    }

    @Override
    public int getLeadSelectionIndex() {
        return backend.getLeadSelectionIndex();
    }

    @Override
    public int getMaxSelectionIndex() {
        return backend.getMaxSelectionIndex();
    }

    @Override
    public int getMinSelectionIndex() {
        return backend.getMinSelectionIndex();
    }

    @Override
    public int getSelectionMode() {
        return backend.getSelectionMode();
    }

    @Override
    public boolean getValueIsAdjusting() {
        return backend.getValueIsAdjusting();
    }

    @Override
    public int hashCode() {
        return backend.hashCode();
    }

    @Override
    public void insertIndexInterval(int index, int length, boolean before) {
        backend.insertIndexInterval(index, length, before);
    }

    @Override
    public boolean isSelectedIndex(int index) {
        return backend.isSelectedIndex(index);
    }

    @Override
    public boolean isSelectionEmpty() {
        return backend.isSelectionEmpty();
    }

    @Override
    public void removeIndexInterval(int index0, int index1) {
        backend.removeIndexInterval(index0, index1);
    }

    @Override
    public void removeListSelectionListener(ListSelectionListener l) {
        backend.removeListSelectionListener(l);
    }

    @Override
    public void removeSelectionInterval(int index0, int index1) {
        backend.removeSelectionInterval(index0, index1);
    }

    @Override
    public void setAnchorSelectionIndex(int anchorIndex) {
        backend.setAnchorSelectionIndex(anchorIndex);
    }

    @Override
    public void setLeadSelectionIndex(int leadIndex) {
        backend.setLeadSelectionIndex(leadIndex);
    }

    @Override
    public void setSelectionInterval(int index0, int index1) {
        int[] newInterval = intersect(new int[]{index0, index1}, bounds);
        if (newInterval != null) {
            backend.setSelectionInterval(newInterval[0], newInterval[1]);
        }
    }

    @Override
    public void setSelectionMode(int selectionMode) {
        backend.setSelectionMode(selectionMode);
    }

    @Override
    public void setValueIsAdjusting(boolean isAdjusting) {
        backend.setValueIsAdjusting(isAdjusting);
    }
    
    protected void clipToBounds() {
        final int min = getMinSelectionIndex();
        if (min == -1) {
            return;
        }
        final int max = getMaxSelectionIndex();
        runWithValueAdjusting(new Runnable() {
            @Override
            public void run() {
                if (getLowerBound() > min) {
                    removeSelectionInterval(min, getLowerBound() - 1);
                }
                if (getUpperBound() < max) {
                    removeSelectionInterval(getUpperBound() + 1, max);
                }
            }
        });
    }
    
    protected void runWithValueAdjusting(Runnable r) {
        boolean oldVIA = getValueIsAdjusting();
        try {
            setValueIsAdjusting(true);
            r.run();
        } finally {
            setValueIsAdjusting(oldVIA);
        }
    }
    
    protected int[] intersect(int[] range1, int[] range2) {
        normalize(range1);
        normalize(range2);
        int[] result = {
                Math.max(range1[0], range2[0]),
                Math.min(range1[1], range2[1])
        };
        if (result[1] >= result[0]) {
            return result;
        } else {
            return null;
        }
    }

    protected void normalize(int[] range) {
        if (range[0] > range[1]) {
            int tmp = range[1];
            range[1] = range[0];
            range[0] = tmp;
        }
    }

}
