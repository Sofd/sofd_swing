package de.sofd.swing.border;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;

import javax.swing.border.LineBorder;

/**
 * Border that draws an arbitrary-thickness line of some color first (like
 * {@link LineBorder}), and on top of that, one of the 4 segments of the border
 * (north, south, east, west), or all segments, in a different color.
 * 
 * @author olaf
 */
public class LineSegmentsBorder extends LineBorder {

    public static enum SegmentLocation {NORTH, SOUTH, EAST, WEST, ALL, NONE};

    private final SegmentLocation segmentLocation;
    private final Color segmentColor;
    private final boolean isBaseBorderVisible;
    
    public LineSegmentsBorder(Color color, int thickness, Color segmentColor, SegmentLocation segmentLocation) {
        super(color != null ? color : Color.black, thickness, false);
        isBaseBorderVisible = (color != null);
        this.segmentLocation = segmentLocation;
        this.segmentColor = segmentColor;
    }

    @Override
    public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
        if (isBaseBorderVisible) {
            super.paintBorder(c, g, x, y, width, height);
        }
        if (segmentColor != null) {
            Color oldColor = g.getColor();
            g.setColor(segmentColor);
            for(int i = 0; i < getThickness(); i++)  {
                if (segmentLocation == SegmentLocation.ALL) {
                    g.drawRect(x+i, y+i, width-i-i-1, height-i-i-1);
                } else {
                    switch (segmentLocation) {
                    case NORTH:
                        g.drawLine(x, y+i, x+width, y+i);
                        break;
                    case SOUTH:
                        g.drawLine(x, y+height-i, x+width, y+height-i);
                        break;
                    case WEST:
                        g.drawLine(x+i, y, x+i, y+height);
                        break;
                    case EAST:
                        g.drawLine(x+width-i, y, x+width-i, y+height);
                        break;
                    }
                    
                }
            }
            g.setColor(oldColor);
        }
    }

}
