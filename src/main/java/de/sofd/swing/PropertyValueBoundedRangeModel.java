package de.sofd.swing;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;

import javax.swing.BoundedRangeModel;
import javax.swing.JSlider;

/**
 * {@link BoundedRangeModel} that uses an integer-valued (JavaBeans-)property of
 * an object as its backend. I.e. setting the model's value ({@link BoundedRangeModel#setValue(int)}
 * sets the property's value, getting the model's value gets the property's
 * value, and vice versa. Thus, when connecting such a model with a GUI
 * component like {@link JSlider}, the end user can set the property's value by
 * sliding the slider.
 * 
 * @author Olaf Klischat
 */
public class PropertyValueBoundedRangeModel extends BaseBoundedRangeModel {

    private Object propOwner;
    private Method getterMethod, setterMethod;
    
    public PropertyValueBoundedRangeModel(PropertyDescriptor propDesc,
                                          Object propOwner,
                                          int minValue,
                                          int maxValue) {
        this.propOwner = propOwner;
        this.getterMethod = propDesc.getReadMethod();
        this.setterMethod = propDesc.getWriteMethod();
        init(getValue(), 0, minValue, maxValue);
          //TODO: account for read-only / write-only properties
    }
    
    public PropertyValueBoundedRangeModel(Object propOwner,
                                          String propName,
                                          int minValue,
                                          int maxValue) {
        this.propOwner = propOwner;
        try {
            BeanInfo bi = Introspector.getBeanInfo(propOwner.getClass());
            PropertyDescriptor propDesc = null;
            for (PropertyDescriptor pd: bi.getPropertyDescriptors()) {
                if (pd.getName().equals(propName)) {
                    propDesc = pd;
                    break;
                }
            }
            if (null == propDesc) {
                throw new IllegalArgumentException("object "+propOwner+" doesn't contain a property named "+propName);
            }
            this.getterMethod = propDesc.getReadMethod();
            this.setterMethod = propDesc.getWriteMethod();
            init(getValue(), 0, minValue, maxValue);
            //TODO: account for read-only / write-only properties
        } catch (IntrospectionException e) {
            throw new IllegalStateException("error during introspection", e);
        }
    }
    
    @Override
    public int getValue() {
        try {
            return (Integer)getterMethod.invoke(propOwner);
        } catch (Exception e) {
            // shouldn't include "this" in the string because
            // calling this.toString() might end up calling this method again,
            // leading to an endless recursion
            throw new RuntimeException("PropertyValueBoundedRangeModel: exception invoking property getter", e);
        }
    }
    
    @Override
    protected void doSetValue(int n) {
        try {
            setterMethod.invoke(propOwner, n);
        } catch (Exception e) {
            throw new RuntimeException("PropertyValueBoundedRangeModel: exception invoking property setter", e);
        }
    }
    
    //TODO: connect PropertyChangeEvent for the property
    
}
