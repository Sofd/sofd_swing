package de.sofd.swing;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.nio.FloatBuffer;
import java.util.List;

import javax.swing.JComponent;

import org.jdesktop.swingx.JXMultiThumbSlider;
import org.jdesktop.swingx.multislider.Thumb;
import org.jdesktop.swingx.multislider.TrackRenderer;

/**
 * 
 * Renderer for drawing the track of the look up table
 * 
 * @author honglinh
 *
 */
public class LutTrackRenderer extends JComponent implements TrackRenderer {
    
    private static final long serialVersionUID = 5852805442531084052L;
    private JLutWindowingSlider slider;
    
    @Override
    public void paint(Graphics g) {
        super.paint(g);
        paintComponent(g);
    }
    
    @Override
    public void paintComponent(Graphics gfx) {
        FloatBuffer lut = slider.getLut();
        // is RGBA buffer of the lookup table assigned to the slider
        if (lut != null) {
            Graphics2D g2d = (Graphics2D) gfx;
            List<Thumb> thumbs = slider.getModel().getSortedThumbs();
            float[] thumbPositions = new float[thumbs.size()];
            for (int i = 0; i < thumbs.size(); i++) {
                thumbPositions[i] = thumbs.get(i).getPosition();
            }
            float max = slider.getMaximumValue();

            // calculate relative border
            float lowerLutBorder = thumbPositions[0] / max;
            float upperLutBorder = thumbPositions[2] / max;

            Dimension sz = slider.getSize();
            float sliderWidth = (float) sz.getWidth();

            // calculate border absolute border positions 
            int startPos = (int) (lowerLutBorder * sliderWidth);
            int endPos = (int) (upperLutBorder * sliderWidth);

            int nColors = lut.capacity() / 4;
            // draw lower part of lookup table
            for (int x = 0; x < startPos; x++) {
                // get the first color pixel
                Color c = new Color(lut.get(0), lut.get(1), lut.get(2));
                g2d.setColor(c);
                g2d.drawLine(x, 0, x, sz.height);
            }
            // draw lookup table
            g2d.translate(startPos, 0);
            for (int x = 0; x < endPos - startPos; x++) {
                int idx = 4 * (x * nColors / (endPos - startPos));
                Color c = new Color(lut.get(idx), lut.get(idx + 1), lut.get(idx + 2));
                g2d.setColor(c);
                g2d.drawLine(x, 0, x, sz.height);
            }
            g2d.translate(-startPos, 0);
            // draw upper part of lookup table
            for (int x = endPos; x < sz.width; x++) {
                // get the last color pixel
                Color c = new Color(lut.get(lut.capacity()- 4), lut.get(lut.capacity() - 3), lut.get(lut.capacity() - 2));
                g2d.setColor(c);
                g2d.drawLine(x, 0, x, sz.height);
            }
        }
    }

    public JComponent getRendererComponent(JXMultiThumbSlider slider) {
        this.slider = (JLutWindowingSlider)slider;
        return this;
    }
}