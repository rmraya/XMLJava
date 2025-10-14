/*******************************************************************************
 * Copyright (c) 2022 - 2025 Maxprograms.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * Commercial licenses are available at https://maxprograms.com/
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