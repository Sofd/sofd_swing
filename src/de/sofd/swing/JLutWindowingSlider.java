package de.sofd.swing;

import java.nio.FloatBuffer;

import org.jdesktop.swingx.JXMultiThumbSlider;
import org.jdesktop.swingx.multislider.MultiThumbModel;
import org.jdesktop.swingx.multislider.Thumb;
import org.jdesktop.swingx.multislider.ThumbDataEvent;
import org.jdesktop.swingx.multislider.ThumbDataListener;

@SuppressWarnings("unchecked")
public class JLutWindowingSlider extends JXMultiThumbSlider implements ThumbDataListener {
    
    private static final long serialVersionUID = 3825020006866482666L;
    private final Thumb lowerThumb;
    private final Thumb midThumb;
    private final Thumb upperThumb;
    private FloatBuffer lut;
    
    public JLutWindowingSlider() {
        this(0.0f,1000.0f);
    }
    
    public JLutWindowingSlider(float min, float max) {
        super();
        setThumbRenderer(new LutThumbRenderer());
        setTrackRenderer(new LutTrackRenderer());
        
        setMaximumValue(max);
        setMinimumValue(min);
        
        MultiThumbModel model = this.getModel();        
        
        lowerThumb = model.getThumbAt(model.addThumb(min, null));
        midThumb = model.getThumbAt(model.addThumb((max-min)/2.0f, null));
        upperThumb = model.getThumbAt(model.addThumb(max, null));        
        
        model.addThumbDataListener(this);
    }
    
    public void setUpperValue(float uValue) {
        upperThumb.setPosition(uValue);
    }
    
    public void setLowerValue(float lValue) {
        lowerThumb.setPosition(lValue);    
    }
    
    public void setMidValue(float mValue) {
        midThumb.setPosition(mValue);
    }
    
    @Override
    public void setMaximumValue(float max) {
        this.getModel().setMaximumValue(max);
    }
    
    @Override
    public void setMinimumValue(float min) {
        this.getModel().setMinimumValue(min);
    }

    public void setLut(FloatBuffer lut) {
        this.lut = lut;
    }

    public FloatBuffer getLut() {
        return lut;
    }
    
    public float getWindowWidth() {
        return upperThumb.getPosition()-lowerThumb.getPosition();
    }
    
    public float getWindowLocation() {
        return midThumb.getPosition();    
    }

    @Override
    public void positionChanged(ThumbDataEvent e) {
        Thumb movedThumb = e.getThumb();
        int selectedThumbIdx = this.getSelectedIndex();
        
        //TODO position of upperThumb must be lower than position of lowerThumb
        
//        if(movedThumb.equals(upperThumb)) {
//            if(movedThumb.getPosition() <= lowerThumb.getPosition()) {
//                movedThumb.setPosition(lowerThumb.getPosition());
//            }
//        }
//        else if(movedThumb.equals(lowerThumb)) {
//            if(movedThumb.getPosition() >= upperThumb.getPosition()) {
//                movedThumb.setPosition(upperThumb.getPosition());
//            }
//        }
        
        // mid thumb moved, so recalculate position of lower and upper thumb
        if (movedThumb.equals(midThumb) && selectedThumbIdx == 1) {
            float midPosition = midThumb.getPosition();
            float width = (upperThumb.getPosition() - lowerThumb.getPosition()) / 2.0f;
            upperThumb.setPosition(midPosition + width);
            lowerThumb.setPosition(midPosition - width);
        } 
        // upper or lower thumb moved, so recalculate position of mid thumb
        else if ((movedThumb.equals(upperThumb) || movedThumb.equals(lowerThumb)) && (selectedThumbIdx == 0 || selectedThumbIdx == 2)) {
            midThumb.setPosition((upperThumb.getPosition() + lowerThumb.getPosition()) / 2.0f);
        }
    }

    @Override
    public void thumbAdded(ThumbDataEvent e) {
    }

    @Override
    public void thumbRemoved(ThumbDataEvent e) {    
    }

    @Override
    public void valueChanged(ThumbDataEvent e) {    
    }
}