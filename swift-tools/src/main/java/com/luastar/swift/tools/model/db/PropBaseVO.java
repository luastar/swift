package com.luastar.swift.tools.model.db;

import com.luastar.swift.base.utils.ObjUtils;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PropBaseVO implements PropertyChangeListener {

    private static final Logger logger = LoggerFactory.getLogger(PropBaseVO.class);

    public static final Object[] NULL_ARGUMENTS = {};

    protected PropertyChangeSupport pcs;
    protected List<String> allPropertyList;
    protected Map<String, Object> tempPropMap;
    protected List<String> changedPropertyList;

    public PropBaseVO() {
        pcs = new PropertyChangeSupport(this);
        pcs.addPropertyChangeListener(this);
        changedPropertyList = new ArrayList<String>();
        tempPropMap = new HashMap<String, Object>();
    }

    public void firePropertyChange(String propertyName) {
        pcs.firePropertyChange(propertyName, null, null);
    }

    public void firePropertyChange(String propertyName, Object oldValue, Object newValue) {
        pcs.firePropertyChange(propertyName, oldValue, newValue);
    }

    public Object getPropertyValue(String propertyName) {
        try {
            if (StringUtils.isEmpty(propertyName)) {
                return null;
            }
            if (getAllPropertyList().contains(propertyName)) {
                return BeanUtils.getProperty(this, propertyName);
            } else {
                return tempPropMap.get(propertyName);
            }
        } catch (IllegalAccessException e) {
            logger.error(e.getMessage(), e);
        } catch (InvocationTargetException e) {
            logger.error(e.getMessage(), e);
        } catch (NoSuchMethodException e) {
            logger.error(e.getMessage(), e);
        }
        return null;
    }

    public void setPropertyValue(String propertyName, Object value) {
        try {
            if (StringUtils.isEmpty(propertyName)) {
                return;
            }
            if (getAllPropertyList().contains(propertyName)) {
                BeanUtils.setProperty(this, propertyName, value);
            } else {
                tempPropMap.put(propertyName, value);
            }
        } catch (IllegalAccessException e) {
            logger.error(e.getMessage(), e);
        } catch (InvocationTargetException e) {
            logger.error(e.getMessage(), e);
        }
    }

    public String getPropertyValueStr(String property) {
        return ObjUtils.toString(getPropertyValue(property), "");
    }

    public List<String> getAllPropertyList() {
        if (allPropertyList == null) {
            allPropertyList = new ArrayList<String>();
            PropertyDescriptor[] descriptors = PropertyUtils.getPropertyDescriptors(this);
            if (descriptors != null) {
                for (PropertyDescriptor pd : descriptors) {
                    allPropertyList.add(pd.getName());
                }
            }
        }
        return allPropertyList;
    }

    public List<String> getChangedPropertyList() {
        return changedPropertyList;
    }

    public void propertyChange(PropertyChangeEvent evt) {
        String property = evt.getPropertyName();
        if (!changedPropertyList.contains(property)) {
            changedPropertyList.add(property);
        }
    }

}
