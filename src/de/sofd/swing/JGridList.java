package de.sofd.swing;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.AbstractAction;
import javax.swing.AbstractListModel;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.DefaultListSelectionModel;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.ListCellRenderer;
import javax.swing.ListModel;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

/**
 * Component that displays a list of items in a rectangular grid with a fixed
 * number of rows and columns. Aims to behave like a {@link JList} from the
 * perspective of the programmer (not necessarily the end user look&feel),
 * provides additional methods for changing programmatically, at any time,
 * the number of rows/columns, the start of the interval of elements to display,
 * etc.
 * <p>
 * Just like {@link JList}, a JGridList is provided with the items to display
 * via a {@link ListModel}.
 * <p>
 * Contrary to {@link JList}, a JGridList does not use
 * {@link ListCellRenderer ListCellRenderers} to draw its cells. Instead, users
 * must implement a special interface, {@link GridListComponentFactory}, which
 * is consulted whenever a component representing an item in the JGridList's
 * ListModel needs to be created.
 * 
 * @author Olaf Klischat
 */
public class JGridList extends JPanel {

    private ListModel model;
    private GridListComponentFactory componentFactory;
    private int firstDisplayedIdx = 0;
    private int nRows = 4, nCols = 4;

    private ListSelectionModel selectionModel;

    private boolean followSelection = true;
    
    // invariants (conditions that hold whenever the outside code
    // can interact with this component):
    //
    // - the component's layout is a GridLayout with nRows rows and
    //   nCols columns
    //
    // - the component contains nRows*nCols direct child components called
    //   "containers", each of which is a JPanel that contains either 0 or 1
    //   child components: If the component corresponds to a valid index in
    //   model,
    
    
    public JGridList(ListModel model) {
        this();
        setModel(model);
    }
    
    public JGridList(ListModel model, GridListComponentFactory componentFactory) {
        this();
        setModel(model);
        setComponentFactory(componentFactory);
    }
    
    public JGridList() {
        reInitEmptyUI();
        copyUiStateToSubComponents();
        setupUiInteractions();
        setSelectionModel(createSelectionModel());
        setComponentFactory(new DefaultGridListComponentFactory());
        setModel(new AbstractListModel() {
                    public int getSize() { return 2; }
                    public Object getElementAt(int i) { return i==0?"no":"model"; }
                });
    }

    public JGridList(final Object[] listData)
    {
        this (
            new AbstractListModel() {
                public int getSize() { return listData.length; }
                public Object getElementAt(int i) { return listData[i]; }
            }
        );
    }
    
    protected void copyUiStateToSubComponents() {
        for (int i = 0; i < this.getComponentCount(); i++) {
            copyUiStateToSubComponent(i);
        }
    }
    
    protected void copyUiStateToSubComponent(int childIndex) {
        if (this.getComponentCount() > childIndex) {
            Component c = this.getComponent(childIndex);
            c.setBackground(getBackground());
            if (c instanceof Container && componentFactory != null) {
                Container cont = (Container)c;
                if (cont.getComponentCount() > 0) {
                    // cont is the container, cont.getComponent(0) is
                    // the component created by the componentFactory
                    // (see #addComponent)
                    componentFactory.parentUiStateChanged
                        (this, (JPanel)cont, (JComponent)cont.getComponent(0));
                }
            }
        }
    }

    @Override
    public void setBackground(Color bg) {
        super.setBackground(bg);
        copyUiStateToSubComponents();
    }
    
    /**
     * 
     * @return default selection model to be created during initialization if
     *         nothing else was specified
     */
    protected ListSelectionModel createSelectionModel() {
        return new DefaultListSelectionModel();
    }

    /**
     * @pre we're empty (no child components)
     * @post UI is initialized according to our current member variable values
     */
    private void reInitEmptyUI() {
        setLayout(new GridLayout(nRows, nCols));
        int displayedCount = nRows * nCols;
        for (int childIndex = 0; childIndex < displayedCount; childIndex++) {
            int modelIndex = firstDisplayedIdx + childIndex;
            addComponent(modelIndex, childIndex);
        }
        revalidate();
    }
    
    /**
     * @pre UI is initialized according to our current member variable values
     * @post we're empty (no child components)
     */
    private void deleteUI() {
        int displayedCount = nRows * nCols;
        for (int childIndex = displayedCount - 1; childIndex >= 0; childIndex--) {
            int modelIndex = firstDisplayedIdx + childIndex;
            removeComponent(modelIndex, childIndex);
        }
        revalidate();
    }

    private void addComponent(int modelIndex, int childIndex) {
        JPanel container = new JPanel(new GridLayout(1,1));
        container.setVisible(true);
        if (model != null && modelIndex < model.getSize() && componentFactory != null) {
            Object modelItem = model.getElementAt(modelIndex);
            JComponent comp = componentFactory.createComponent(this, container, modelItem);
            comp.setVisible(true);
            componentFactory.setSelectedStatus
                    (this,
                     container,
                     modelItem,
                     selectionModel != null && selectionModel.isSelectedIndex(modelIndex),
                     comp);
        }
        this.add(container, childIndex);
        copyUiStateToSubComponent(childIndex);
    }
    
    private void removeComponent(int modelIndex, int childIndex) {
        JPanel container = (JPanel)this.getComponent(childIndex);
        if (model != null && modelIndex < model.getSize() && componentFactory != null) {
            Object modelItem = model.getElementAt(modelIndex);
            if (container.getComponentCount() > 0) { // may be 0 if e.g. the model grew since last reInitEmptyUI()
                JComponent component = (JComponent)container.getComponent(0);
                componentFactory.deleteComponent(this, container, modelItem, component);
            }
        }
        this.remove(childIndex);
    }

    /**
     *
     * @param modelIndex modelIndex
     * @return the component (created by this list's component factory) that currently
     *         displays the model element at index modelIndex. null if that element
     *         isn't currently visible.
     */
    public JComponent getComponentFor(int modelIndex) {
        if (model != null && modelIndex < model.getSize()) {
            int displayedCount = nRows * nCols;
            int childIndex = modelIndex - getFirstDisplayedIdx();
            if (childIndex >= 0 && childIndex < displayedCount) {
                JPanel container = (JPanel)this.getComponent(childIndex);
                return (JComponent)container.getComponent(0);
            }
        }
        return null;
    }

    public void refresh() {
        deleteUI();
        reInitEmptyUI();
    }
    
    public ListModel getModel() {
        return model;
    }

    public void setModel(ListModel model) {
        if (model == this.model) { return; }
        deleteUI();
        if (this.model != null) {
            this.model.removeListDataListener(modelChangeListener);
        }
        this.model = model;
        if (this.model != null) {
            this.model.addListDataListener(modelChangeListener);
        }
        reInitEmptyUI();
    }

    private ListDataListener modelChangeListener = new ListDataListener() {
        
        // TODO: more efficient implementations

        @Override
        public void contentsChanged(ListDataEvent e) {
            refresh();
        }

        @Override
        public void intervalAdded(ListDataEvent e) {
            refresh();
        }

        @Override
        public void intervalRemoved(ListDataEvent e) {
            refresh();
        }
        
    };
    
    public GridListComponentFactory getComponentFactory() {
        return componentFactory;
    }

    /**
     * Sets or re-sets the JGridList's component factory, which is consulted
     * to create the JComponents that make up the cells of the list. Setting
     * the component factory while the list is already displayed will initiate
     * a complete refresh of the UI, including a re-creation of all the cell
     * components.
     *
     * @param componentFactory
     */
    public void setComponentFactory(GridListComponentFactory componentFactory) {
        if (componentFactory == this.componentFactory) { return; }
        deleteUI();
        this.componentFactory = componentFactory;
        reInitEmptyUI();
    }

    /**
     *
     * @return start of currently displayed interval of model elements
     */
    public int getFirstDisplayedIdx() {
        return firstDisplayedIdx;
    }

    /**
     * Programmatically "scrolls" the JGridList to a different position by
     * setting the index of the first model element to display.
     *
     * @param newValue the new index
     */
    public void setFirstDisplayedIdx(int newValue) {
        if (newValue == this.firstDisplayedIdx) { return; }
        
        //code has the same effect as these 3 lines (but is more efficient):
        //deleteUI();
        //this.firstDisplayedIdx = newValue;
        //reInitEmptyUI();
        
        if (null != model) {
            int displayedCount = getRowCount() * getColumnCount();
            if (newValue < firstDisplayedIdx) {
                int shift = Math.min(firstDisplayedIdx - newValue, displayedCount);
                for (int i = 0; i < shift; i++) {
                    removeComponent(firstDisplayedIdx + displayedCount - 1 - i, displayedCount - 1);
                    addComponent(newValue + shift - 1 - i, 0);
                }
            } else {
                assert(newValue > firstDisplayedIdx);
                int shift = Math.min(newValue - firstDisplayedIdx, displayedCount);
                for (int i = 0; i < shift; i++) {
                    removeComponent(firstDisplayedIdx + i, 0);
                    addComponent(newValue + displayedCount - shift + i, displayedCount - 1);
                }
            }
        }
        this.firstDisplayedIdx = newValue;
        revalidate();
    }

    /**
     *
     * @return currently displayed number of grid rows
     */
    public int getRowCount() {
        return nRows;
    }

    /**
     * Sets the number of displayed grid rows to a new value and
     * refreshes the UI appropriately.
     *
     * @param rows
     */
    public void setRowCount(int rows) {
        setGridSizes(rows, nCols);
    }

    /**
     *
     * @return currently displayed number of grid columns
     */
    public int getColumnCount() {
        return nCols;
    }

    /**
     * Sets the number of displayed grid columns to a new value and
     * refreshes the UI appropriately.
     *
     * @param cols
     */
    public void setColumnCount(int cols) {
        setGridSizes(nRows, cols);
    }
    
    /**
     * Sets the displayed grid dimensions (row count & column count) to new
     * values and refreshes the UI appropriately.
     *
     * @param newNRows
     * @param newNCols
     */
    public void setGridSizes(int newNRows, int newNCols) {
        if (newNRows <= 0 || newNCols <= 0) {
            throw new IllegalArgumentException("rowCount, columnCount must be > 0");
        }
        //code has the same effect as these 4 lines (but is more efficient):
        //deleteUI();
        //this.nRows = newNRows;
        //this.nCols = newNCols;
        //reInitEmptyUI();

        int oldDisplayedCount = getRowCount() * getColumnCount();
        int newDisplayedCount = newNRows * newNCols;
        setLayout(new GridLayout(newNRows, newNCols));
        if (null != model) {
            if (newDisplayedCount > oldDisplayedCount) {
                for (int i = 0; i < (newDisplayedCount - oldDisplayedCount); i++) {
                    addComponent(firstDisplayedIdx + oldDisplayedCount + i,
                                 oldDisplayedCount + i);
                }
            } else if (newDisplayedCount < oldDisplayedCount) {
                for (int i = 0; i < (oldDisplayedCount - newDisplayedCount); i++) {
                    removeComponent(firstDisplayedIdx + oldDisplayedCount - 1 - i,
                                    oldDisplayedCount - 1 - i);
                }
            }
        }
        this.nRows = newNRows;
        this.nCols = newNCols;
        revalidate();
    }

    public ListSelectionModel getSelectionModel() {
        return selectionModel;
    }

    public void setSelectionModel(ListSelectionModel selectionModel) {
        if (selectionModel == this.selectionModel) { return; }
        if (this.selectionModel != null) {
            this.selectionModel.removeListSelectionListener(listSelectionListener);
        }
        deleteUI();
        this.selectionModel = selectionModel;
        if (this.selectionModel != null) {
            this.selectionModel.addListSelectionListener(listSelectionListener);
        }
        reInitEmptyUI();
    }

    private ListSelectionListener listSelectionListener = new ListSelectionListener() {

        @Override
        public void valueChanged(ListSelectionEvent e) {
            // refresh();  // more efficient:
            if (null != model) {
                int displayedCount = getRowCount() * getColumnCount();
                for (int childIdx = 0; childIdx < displayedCount; childIdx++) {
                    int modelIdx = firstDisplayedIdx + childIdx;
                    if (modelIdx >= 0 && modelIdx < model.getSize()) {
                        Object modelItem = model.getElementAt(modelIdx);
                        JPanel container = (JPanel)JGridList.this.getComponent(childIdx);
                        JComponent comp = (JComponent)container.getComponent(0);
                        componentFactory.setSelectedStatus
                            (JGridList.this,
                             container,
                             modelItem,
                             selectionModel != null && selectionModel.isSelectedIndex(modelIdx),
                             comp);
                    }
                }
            }
            if (isDisplayFollowsSelection()) {
                scrollToSelection();
            }
        }
        
    };

    public void scrollToSelection() {
        ListSelectionModel sm = getSelectionModel();
        if (null != sm) {
            int li = sm.getLeadSelectionIndex();
            if (sm.isSelectedIndex(li)) {
                ensureIndexIsVisible(li);
            }
        }
    }
    
    public void ensureIndexIsVisible(int idx) {
        if (null == model) {
            return;
        }
        if (idx >= 0 && idx < model.getSize()) {
            int displayedCount = getRowCount() * getColumnCount();
            int lastDispIdx = getFirstDisplayedIdx() + displayedCount - 1;
            int newFirstDispIdx = -1;
            // TODO: the following always sets firstDispIdx to a multiple of
            //   getColumnCount(). Instead, it should take the previous firstDispIdx
            //   into account correctly
            if (idx < getFirstDisplayedIdx()) {
                newFirstDispIdx = idx / getColumnCount() * getColumnCount();
            } else if (idx > lastDispIdx) {
                int rowStartIdx = idx / getColumnCount() * getColumnCount();
                newFirstDispIdx = rowStartIdx - (getRowCount()-1) * getColumnCount();
            }
            if (newFirstDispIdx != -1) {
                setFirstDisplayedIdx(newFirstDispIdx);
            }
        }
    }
    
    public boolean isDisplayFollowsSelection() {
        return followSelection;
    }

    public void setDisplayFollowsSelection(boolean followSelection) {
        this.followSelection = followSelection;
    }
    

    public int findModelIndexAt(Point p) {
        Component child = findComponentAt(p);

        // child may be an arbitrarily deeply nested grand...child of us.
        // find the ancestor of child that's a direct child of us
        while (child != this && child.getParent() != this && child.getParent() != null) {
            child = child.getParent();
        }
        if (child == null || child == this) {
            return -1;
        }
        
        int childIndex = -1;
        Component[] children = this.getComponents();
        for (int i = 0; i < children.length; ++i) {
            if (child == children[i]) {
                childIndex = i;
                break;
            }
        }
        if (childIndex == -1) {
            return -1;
        }
        int modelIndex = childIndex + firstDisplayedIdx;
        return modelIndex < model.getSize() ? modelIndex : -1;
    }
    
    //// setting up default (built-in) interactive UI actions the user may
    //// user to change the list (e.g. clicking to select, cursor key).
    //// subclasses may override.

    protected void setupUiInteractions() {
        this.setFocusable(true);
        
        this.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (null == getSelectionModel() || null == getModel()) {
                    return;
                }
                requestFocus();
                if (e.getButton() != MouseEvent.BUTTON1) { return; }
                int clickedModelIndex = findModelIndexAt(e.getPoint());
                if (clickedModelIndex != -1) {
                    if ((e.getModifiersEx() & MouseEvent.CTRL_DOWN_MASK) == 0) {
                        getSelectionModel().clearSelection();
                    }
                    getSelectionModel().addSelectionInterval(clickedModelIndex, clickedModelIndex);
                }
            }
        });

        InputMap inputMap = this.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        ActionMap actionMap = this.getActionMap();
        if (inputMap != null && actionMap != null) {
            inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0), "up");
            actionMap.put("up", new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    shiftSelectionBy(-getColumnCount());
                }
            });
            inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0), "down");
            actionMap.put("down", new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    shiftSelectionBy(getColumnCount());
                }
            });
            inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0), "left");
            actionMap.put("left", new SelectionShiftAction(-1));
            inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0), "right");
            actionMap.put("right", new SelectionShiftAction(1));
        }
    }
    
    protected Action upAction = new AbstractAction() {
        @Override
        public void actionPerformed(ActionEvent e) {
        }
    };
    
    protected void shiftSelectionBy(int shift) {
        if (null == getSelectionModel() || null == getModel()) {
            return;
        }
        int idx = getSelectionModel().getLeadSelectionIndex();
        if (idx != -1) {
            idx += shift;
            if (idx >= 0 && idx < model.getSize()) {
                getSelectionModel().clearSelection();
                getSelectionModel().addSelectionInterval(idx, idx);
            }
        }
    }
    
    protected class SelectionShiftAction extends AbstractAction {
        private int shift;
        public SelectionShiftAction(int shift) {
            this.shift = shift;
        }
        @Override
        public void actionPerformed(ActionEvent e) {
            shiftSelectionBy(shift);
        }
    }


}
