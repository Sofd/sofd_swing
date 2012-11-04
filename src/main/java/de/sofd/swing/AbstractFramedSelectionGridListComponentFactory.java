package de.sofd.swing;

import java.awt.Color;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

import de.sofd.swing.border.LineSegmentsBorder;
import de.sofd.swing.border.LineSegmentsBorder.SegmentLocation;

public abstract class AbstractFramedSelectionGridListComponentFactory implements GridListComponentFactory {

    private final int borderWidth;
    private final Color selectionColor;
    private final Color markerColor;
    
    /**
     * The border without drop location markers. These will be used most
     * frequently by far, so we cache them in member variables.
     */
    private Border unselectedBorder, selectedBorder;

    public AbstractFramedSelectionGridListComponentFactory() {
        this(2, Color.YELLOW, Color.BLUE);
    }

    public AbstractFramedSelectionGridListComponentFactory(int borderWidth, Color selectionColor, Color markerColor) {
        this.borderWidth = borderWidth;
        this.selectionColor = selectionColor;
        this.markerColor = markerColor;
        this.unselectedBorder = new EmptyBorder(borderWidth, borderWidth, borderWidth, borderWidth);
        this.selectedBorder = new LineBorder(selectionColor, borderWidth);
    }

    @Override
    public void setSelectedStatusAndDropLocationMarker(JGridList source,
            JPanel parent, Object modelItem, boolean selected,
            DropLocationMarker marker, JComponent component) {
        if (marker == DropLocationMarker.NONE) {
            component.setBorder(selected ? selectedBorder : unselectedBorder);
        } else {
            Color sc = selected ? selectionColor : null;
            LineSegmentsBorder.SegmentLocation segmentLoc = SegmentLocation.NONE;
            switch (marker) {
            case BEFORE:
                segmentLoc = SegmentLocation.WEST;
                break;
            case AFTER:
                segmentLoc = SegmentLocation.EAST;
                break;
            case ON:
                segmentLoc = SegmentLocation.ALL;
                break;
            }
            component.setBorder(new LineSegmentsBorder(sc, borderWidth, markerColor, segmentLoc));
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

    @Override
    public boolean canReuseComponents() {
        return false;
    }

}