package de.sofd.swing;

import java.awt.*;

import javax.swing.*;
import javax.swing.event.*;

@SuppressWarnings("serial")
public class JSliderWithSpinnerPanel extends JPanel implements ChangeListener {
    protected JSlider slider;
    protected JSpinner spinner;
    protected ChangeListener observer;

    public JSliderWithSpinnerPanel(String name, int min, int max, int value, int stepSize, ChangeListener observer) {
        super(new BorderLayout());
        
        this.observer = observer;
                    
        slider = new JSlider(min, max, value);
        slider.setName(name);
        slider.addChangeListener(this);
        slider.setPaintLabels(true);
        
        JPanel panelN = new JPanel();
        panelN.setLayout(new BoxLayout(panelN, BoxLayout.X_AXIS));
        panelN.add(new JLabel(name));
        
        panelN.add(Box.createHorizontalGlue());
        
        spinner = new JSpinner(new SpinnerNumberModel(value, 0, max, stepSize));
        spinner.setName(name);
        spinner.addChangeListener(this);
        panelN.add(spinner);
                
        this.add(panelN, BorderLayout.NORTH);
        this.add(slider, BorderLayout.SOUTH);
    }

    public JSlider getSlider() {
        return slider;
    }
    
    public JSpinner getSpinner() {
        return spinner;
    }

    @Override
    public void stateChanged(ChangeEvent ev) {
        Component source = (Component)ev.getSource();
        
        if (source instanceof JSlider) {
            JSlider slider = (JSlider)source;
            updateSpinner(slider.getValue());
        } else {
            JSpinner spinner = (JSpinner)source;
            
            int value = ((SpinnerNumberModel)spinner.getModel()).getNumber().intValue();
            updateSlider(value);
        }
    }
    
    protected void updateSlider(final int value) {
        SwingUtilities.invokeLater(new Runnable()
        {
            public void run()
            {
                slider.removeChangeListener(JSliderWithSpinnerPanel.this);
                
                if (observer != null)
                    slider.removeChangeListener(observer);
                
                slider.setValue(value);
                
                if (observer != null)
                    slider.addChangeListener(observer);
                
                slider.addChangeListener(JSliderWithSpinnerPanel.this);
            }
        });
    }

    protected void updateSpinner(final int value) {
        SwingUtilities.invokeLater(new Runnable()
        {
            public void run()
            {
                spinner.removeChangeListener(JSliderWithSpinnerPanel.this);
                
                if (observer != null)
                    spinner.removeChangeListener(observer);
                
                spinner.setValue(value);
                
                if (observer != null)
                    spinner.addChangeListener(observer);
                
                
                spinner.addChangeListener(JSliderWithSpinnerPanel.this);
            }
        });
    }
}

