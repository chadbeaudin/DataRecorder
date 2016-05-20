/*
 * Created on Jan 22, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package com.datarecorder;

public class PropertyType {
    private String typeName;
    private String className;

    public PropertyType(final String type, final String className) {
        this.typeName = type;
        this.className = className;
    }

    /**
     * @return Returns the className.
     */
    public String getClassName() {
        return className;
    }

    /**
     * @param className
     *            The className to set.
     */
    public void setClassName(final String className) {
        this.className = className;
    }

    /**
     * @return Returns the type.
     */
    public String getTypeName() {
        return typeName;
    }

    /**
     * @param type
     *            The type to set.
     */
    public void setTypeName(final String type) {
        this.typeName = type;
    }

    @Override
    public String toString() {
        return this.typeName;
    }

}