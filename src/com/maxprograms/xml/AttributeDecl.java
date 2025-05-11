/*******************************************************************************
 * Copyright (c) 2022 - 2025 Maxprograms.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-v10.html
 *
 * Contributors:
 *     Maxprograms - initial API and implementation
 *******************************************************************************/

package com.maxprograms.xml;

public class AttributeDecl {
    private String name;
    private String type;
    private String defaultValue;
    private boolean isFixed;
    private boolean isParameterEntity = false;

    public AttributeDecl(String name, String type, String defaultValue, boolean isFixed) {
        this.name = name;
        this.type = type;
        this.defaultValue = defaultValue;
        this.isFixed = isFixed;
    }

    public void setParameterEntity(boolean isParameterEntity) {
        this.isParameterEntity = isParameterEntity;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public boolean isFixed() {
        return isFixed;
    }

    @Override
    public String toString() {
        if (isParameterEntity) {
            return name;
        }
        StringBuilder sb = new StringBuilder();
        sb.append(name).append(" ").append(type);
        if (isFixed) {
            sb.append(" #FIXED");
            sb.append(" \"").append(defaultValue).append("\"");
        } else if ("#REQUIRED".equals(defaultValue)) {
            sb.append(" #REQUIRED");
        } else if ("#IMPLIED".equals(defaultValue)) {
            sb.append(" #IMPLIED");
        } else if (defaultValue != null && !defaultValue.isEmpty()) {
            sb.append(" \"").append(defaultValue).append("\"");
        }
        return sb.toString();
    }
}