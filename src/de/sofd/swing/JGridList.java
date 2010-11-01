package de.sofd.swing;

import java.awt.AWTEvent;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.AbstractAction;
import javax.swing.AbstractListModel;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.BoundedRangeModel;
import javax.swing.DefaultListSelectionModel;
import javax.swing.DropMode;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.KeyStroke;
import javax.swing.ListCellRenderer;
import javax.swing.ListModel;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.TransferHandler;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
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

    private final JPanel cellsContainer;
    private JScrollBar scrollBar = null;

    private ListSelectionModel selectionModel;

    private boolean followSelection = true;
    
    private boolean dragEnabled = false;
    private DropMode dropMode = DropMode.ON_OR_INSERT;
    
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
        setLayout(new BorderLayout());
        cellsContainer = new JPanel();
        this.add(cellsContainer, BorderLayout.CENTER);
        setTransferHandler(new DefaultTransferHandler(this));
        setShowScrollbar(true);
        reInitEmptyUI();
        copyUiStateToSubComponents();
        enableEvents(AWTEvent.MOUSE_EVENT_MASK|AWTEvent.MOUSE_MOTION_EVENT_MASK);
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
    
    public boolean isShowScrollbar() {
        return scrollBar != null;
    }
    
    public void setShowScrollbar(boolean show) {
        if (show == isShowScrollbar()) {
            return;
        }
        if (show) {
            scrollBar = new JScrollBar(JScrollBar.VERTICAL);
            this.add(scrollBar, BorderLayout.EAST);
            scrollBar.getModel().addChangeListener(scrollbarChangeListener);
        } else {
            this.remove(scrollBar);
            scrollBar.getModel().removeChangeListener(scrollbarChangeListener);
            scrollBar = null;
        }
        updateScrollbar();
        revalidate();
        validate();
    }
    
    protected void copyUiStateToSubComponents() {
        for (int i = 0; i < this.getComponentCount(); i++) {
            copyUiStateToSubComponent(i);
        }
    }
    
    protected void copyUiStateToSubComponent(int childIndex) {
        if (this.getComponentCount() > childIndex) {
            Component c = cellsContainer.getComponent(childIndex);
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
     * @pre we're empty (cellsContainer contains no child components)
     * @post UI is initialized according to our current member variable values
     */
    private void reInitEmptyUI() {
        cellsContainer.setLayout(new GridLayout(nRows, nCols));
        int displayedCount = nRows * nCols;
        for (int childIndex = 0; childIndex < displayedCount; childIndex++) {
            int modelIndex = firstDisplayedIdx + childIndex;
            addComponent(modelIndex, childIndex);
        }
        updateScrollbar();
        revalidate();
        validate();
    }
    
    /**
     * @pre UI is initialized according to our current member variable values
     * @post we're empty (no child components)
     */
    private void deleteUI() {
        int displayedCount = nRows * nCols;
        for (int childIndex = displayedCount - 1; childIndex >= 0; childIndex--) {
            int modelIndex = firstDisplayedIdx + childIndex;
            removeComponent(modelIndex, childIndex, true);
        }
        revalidate();
        validate();
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
        cellsContainer.add(container, childIndex);
        copyUiStateToSubComponent(childIndex);
    }
    
    private void setComponent(int modelIndex, int prevModelIndex, int childIndex) {
        if (componentFactory != null) {
            JPanel container = (JPanel) cellsContainer.getComponent(childIndex);
            if (modelIndex < 0) {   // modelIndex == -1 => set container at childIndex to "no model element"
                if (container.getComponentCount() > 0) {   // == (prevModelIndex >= 0)
                    JComponent component = (JComponent) container.getComponent(0);
                    if (prevModelIndex >= 0) {
                        if (container.getComponentCount() > 0) {
                            Object modelItem = model.getElementAt(prevModelIndex);
                            componentFactory.deleteComponent(this, container, modelItem, component);
                            if (container.getComponentCount() > 0) {
                                container.remove(0);
                            }
                            container.repaint();
                        }
                    } else {
                        componentFactory.createComponent(this, container, null);
                    }
                }
            } else {
                if (model != null && modelIndex < model.getSize()) {
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
            }
            copyUiStateToSubComponent(childIndex);
        }
    }

    private void removeComponent(int modelIndex, int childIndex, boolean removeContainer) {
        JPanel container = (JPanel) cellsContainer.getComponent(childIndex);
        if (model != null && modelIndex < model.getSize() && componentFactory != null) {
            Object modelItem = model.getElementAt(modelIndex);
            if (container.getComponentCount() > 0) { // may be 0 if e.g. the model grew since last reInitEmptyUI()
                JComponent component = (JComponent) container.getComponent(0);
                componentFactory.deleteComponent(this, container, modelItem, component);
            }
        }
        if (removeContainer) {
            cellsContainer.remove(childIndex);
        }
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
                JPanel container = (JPanel) cellsContainer.getComponent(childIndex);
                return (JComponent) container.getComponent(0);
            }
        }
        return null;
    }

    public void refresh() {
        deleteUI();
        reInitEmptyUI();
    }
    
    public void repaintCells() {
        if (model != null) {
            int displayedCount = nRows * nCols;
            for (int i = 0; i < displayedCount; i++) {
                JComponent c = (JComponent) cellsContainer.getComponent(i);
                if (c.getComponentCount() > 0) {
                    c.getComponent(0).repaint();
                }
            }
        }
    }
    
    public void repaintCell(int modelIndex) {
        JComponent c = getComponentFor(modelIndex);
        if (c != null) {
            c.repaint();
        }
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
            if (componentFactory.canReuseComponents()) {
                for (int i = 0; i < displayedCount; i++) {
                    int prevModelIndex = firstDisplayedIdx + i;
                    int modelIndex = newValue + i;
                    int modelSize = model.getSize();
                    setComponent(modelIndex < modelSize ? modelIndex : -1,
                                 prevModelIndex < modelSize ? prevModelIndex : -1,
                                 i);
                }
            } else {
                if (newValue < firstDisplayedIdx) {
                    int shift = Math.min(firstDisplayedIdx - newValue, displayedCount);
                    for (int i = 0; i < shift; i++) {
                        removeComponent(firstDisplayedIdx + displayedCount - 1 - i, displayedCount - 1, true);
                        addComponent(newValue + shift - 1 - i, 0);
                    }
                } else {
                    assert(newValue > firstDisplayedIdx);
                    int shift = Math.min(newValue - firstDisplayedIdx, displayedCount);
                    for (int i = 0; i < shift; i++) {
                        removeComponent(firstDisplayedIdx + i, 0, true);
                        addComponent(newValue + displayedCount - shift + i, displayedCount - 1);
                    }
                }
            }
        }
        this.firstDisplayedIdx = newValue;
        updateScrollbar();
        revalidate();
        validate();
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

        // TODO: use optimized implementation if componentFactory.canReuseComponents()

        int oldDisplayedCount = getRowCount() * getColumnCount();
        int newDisplayedCount = newNRows * newNCols;
        cellsContainer.setLayout(new GridLayout(newNRows, newNCols));
        if (null != model) {
            if (newDisplayedCount > oldDisplayedCount) {
                for (int i = 0; i < (newDisplayedCount - oldDisplayedCount); i++) {
                    addComponent(firstDisplayedIdx + oldDisplayedCount + i,
                                 oldDisplayedCount + i);
                }
            } else if (newDisplayedCount < oldDisplayedCount) {
                for (int i = 0; i < (oldDisplayedCount - newDisplayedCount); i++) {
                    removeComponent(firstDisplayedIdx + oldDisplayedCount - 1 - i,
                                    oldDisplayedCount - 1 - i,
                                    true);
                }
            }
        }
        this.nRows = newNRows;
        this.nCols = newNCols;
        updateScrollbar();
        revalidate();
        validate();
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

    public Object[] getSelectedValues() {
        ListSelectionModel sm = getSelectionModel();
        int minSI = sm.getMinSelectionIndex();
        int maxSI = sm.getMaxSelectionIndex();
        Object[] tmp = new Object[1 + (maxSI - minSI)];
        int n = 0;
        for (int i = minSI; i <= maxSI; i++) {
            if (sm.isSelectedIndex(i)) {
                tmp[n++] = getModel().getElementAt(i);
            }
        }
        Object[] result = new Object[n];
        System.arraycopy(tmp, 0, result, 0, n);
        return result;
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
                        JPanel container = (JPanel) cellsContainer.getComponent(childIdx);
                        JComponent comp = (JComponent) container.getComponent(0);
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

    /**
     * need our own valueIsAdjusting for the scrollbar instead of using
     * scrollBar.getModel().getValueIsAdjusting() because we want to be able to
     * tell the difference between the user dragging the thumb (we want to
     * update the display during that) and our own temporarily invalid
     * scrollModel value settings in updateScrollbar() (we do NOT want to update
     * the display during that)
     */
    private boolean internalScrollbarValueIsAdjusting = false;
    
    private void updateScrollbar() {
        if (null == scrollBar) {
            return;
        }
        if (null == model || model.getSize() == 0) {
            internalScrollbarValueIsAdjusting = true;
            scrollBar.getModel().setRangeProperties(0, 0, 0, 0, false);
            internalScrollbarValueIsAdjusting = false;
            scrollBar.setEnabled(false);
            return;
        }
        if (! scrollBar.isEnabled()) {
            scrollBar.setEnabled(true);
        }
        int size = model.getSize();
        int firstDispIdx = getFirstDisplayedIdx();
        int displayedCount = getRowCount() * getColumnCount();
        int lastDispIdx = firstDispIdx + displayedCount - 1;
        if (lastDispIdx >= size) {
            lastDispIdx = size - 1;
        }
        BoundedRangeModel scrollModel = scrollBar.getModel();
        internalScrollbarValueIsAdjusting = true;
        scrollModel.setMinimum(0);
        scrollModel.setMaximum(size - 1);
        scrollModel.setValue(firstDispIdx);
        scrollModel.setExtent(displayedCount - 1);
        internalScrollbarValueIsAdjusting = false;
        scrollBar.setUnitIncrement(getColumnCount());
        scrollBar.setBlockIncrement(displayedCount);
    }
    
    private ChangeListener scrollbarChangeListener = new ChangeListener() {
        private boolean inCall = false;
        @Override
        public void stateChanged(ChangeEvent e) {
            if (inCall) { return; }
            inCall = true;
            try {
                BoundedRangeModel scrollModel = scrollBar.getModel();
                if (internalScrollbarValueIsAdjusting) { return; }
                //System.out.println("scrollbar changed: " + scrollModel);
                setFirstDisplayedIdx(scrollModel.getValue());
            } finally {
                inCall = false;
            }
        }
    };
    
    // TODO: row-wise scrolling instead of cell-wise scrolling
    
    
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
        if (null == child) {
            return -1;
        }

        // child may be an arbitrarily deeply nested grand...child of us.
        // find the ancestor of child that's a direct child of cellsContainer, if any
        while (child != this && child.getParent() != this && child.getParent() != cellsContainer && child.getParent() != null) {
            child = child.getParent();
        }
        if (child == null || child == this || child == cellsContainer) {
            return -1;
        }
        
        int childIndex = -1;
        Component[] children = cellsContainer.getComponents();
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
    
    //// Drag&Drop support
    // Swing doesn't let us provide DnD support for JGridList that's API-compatible to that
    // of Swing's own components like JList: http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6448332
    // So we just provide drag support for now, and public APIs to let users more easily
    // implement drop support in their own TransferHandlers
    
    private DropLocation renderedDropLocation;
    
    public void setRenderedDropLocation(DropLocation renderedDropLocation) {
        DropLocation old = this.renderedDropLocation;
        this.renderedDropLocation = renderedDropLocation;
        if (old != null) {
            repaintCell(old.index);
        }
        if (renderedDropLocation != null) {
            repaintCell(renderedDropLocation.index);
        }
        System.out.println("renderedDropLocation: " + renderedDropLocation);
    }
    
    public DropLocation getRenderedDropLocation() {
        return renderedDropLocation;
    }

    private static final double DROPLOC_INSERT_RELATIVE_X = 0.2;
    
    public DropLocation getDropLocationFor(Point p) {
        if (getModel() == null) {
            return null;
        }
        p = SwingUtilities.convertPoint(this, p, cellsContainer);
        int boxWidth = cellsContainer.getWidth() / nCols;
        int boxHeight = cellsContainer.getHeight() / nRows;
        int col = Math.min(nCols-1, p.x / boxWidth);
        int row = Math.min(nRows-1, p.y / boxHeight);
        int idx = getFirstDisplayedIdx() + row * nCols + col;
        if (idx >= getModel().getSize()) {
            return new DropLocation(getModel().getSize(), true, p);
        }
        double boxRelativeX = ((double)p.x - col*boxWidth) / boxWidth;
        if (boxRelativeX < DROPLOC_INSERT_RELATIVE_X) {
            return new DropLocation(idx, true, p);
        } else if (boxRelativeX > 1 - DROPLOC_INSERT_RELATIVE_X) {
            return new DropLocation(idx + 1, true, p);
        } else {
            return new DropLocation(idx, false, p);
        }
    }
    
    public void setDragEnabled(boolean b) {
        dragEnabled = b;
    }

    public boolean getDragEnabled() {
        return dragEnabled;
    }

    public boolean isDragEnabled() {
        return dragEnabled;
    }

    public final void setDropMode(DropMode dropMode) {
        if (dropMode != null) {
            switch (dropMode) {
                case ON:
                case INSERT:
                case ON_OR_INSERT:
                    this.dropMode = dropMode;
                    return;
            }
        }

        throw new IllegalArgumentException(dropMode + ": Unsupported drop mode for JGridList");
    }
    
    public DropMode getDropMode() {
        return dropMode;
    }
    
    public static class DropLocation extends TransferHandler.DropLocation {
        private int index;
        private boolean isInsert;
        
        public DropLocation(int index, boolean isInsert, Point p) {
            super(p);
            this.index = index;
            this.isInsert = isInsert;
        }
        
        public int getIndex() {
            return index;
        }
        
        public boolean isInsert() {
            return isInsert;
        }
        
        @Override
        public String toString() {
            return "(" + getIndex() + "," + isInsert() + ")";
        }
        
    }

    public static class DefaultTransferHandler extends TransferHandler {
        protected JGridList list;
        public DefaultTransferHandler(JGridList list) {
            this.list = list;
        }
        @Override
        protected Transferable createTransferable(JComponent c) {
            StringBuffer txt = new StringBuffer(30);
            boolean start = true;
            for (Object elt : list.getSelectedValues()) {
                if (!start) {
                    txt.append("\n");
                }
                txt.append(elt.toString());
                start = false;
            }
            return new StringSelection(txt.toString());
        }
    }

    // drag gesture recognition

    //recognize drag gestures in the JGridList's processMouse*Event methods rather than
    //in the CellContainer's for now because doing the latter would prevent the events
    //from bubbling up to the JGridList (so outside parties would no longer receive
    //mouse events on the JGridList). We might manually re-dispatch the events on the
    //JGridList to prevent that, though
    
    private Point lastPressed = null;
    
    @Override
    protected void processMouseEvent(MouseEvent e) {
        super.processMouseEvent(e);
        if (e.isConsumed()) {
            return;
        }
        if (!isDragEnabled()) {
            return;
        }
        switch (e.getID()) {
        case MouseEvent.MOUSE_PRESSED:
            lastPressed = e.getPoint();
            break;

        default:
            lastPressed = null;
        }
    }
    
    @Override
    protected void processMouseMotionEvent(MouseEvent e) {
        super.processMouseMotionEvent(e);
        if (e.isConsumed()) {
            return;
        }
        if (!isDragEnabled()) {
            return;
        }
        if (getTransferHandler() == null) {
            return;
        }
        switch (e.getID()) {
        case MouseEvent.MOUSE_DRAGGED:
            if (lastPressed != null) {
                if (e.getPoint().distance(lastPressed) > 5) {
                    //int action = (0 != (e.getModifiers() & MouseEvent.CTRL_MASK) ? TransferHandler.COPY : TransferHandler.MOVE);
                    int action = TransferHandler.COPY;
                    getTransferHandler().exportAsDrag(this, SwingUtilities.convertMouseEvent(cellsContainer, e, this), action);
                    lastPressed = null;
                }
            }
            break;

        default:
            lastPressed = null;
        }
    }


    //TODO: How to account for the getDropMode()? Consult JList for inspiration

    //// default UI interactions

    /**
     * Called during initialization for setting up default interactive UI
     * actions the user may use to change the list (e.g. clicking to select,
     * cursor key). Subclasses may override.
     */
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
                        getSelectionModel().setSelectionInterval(clickedModelIndex, clickedModelIndex);
                    } else {
                        getSelectionModel().addSelectionInterval(clickedModelIndex, clickedModelIndex);
                    }
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
                getSelectionModel().setSelectionInterval(idx, idx);
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
