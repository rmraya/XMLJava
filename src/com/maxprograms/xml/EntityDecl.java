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

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;

public class EntityDecl implements XMLNode {

    public static final String PUBLIC = "PUBLIC";
    public static final String SYSTEM = "SYSTEM";
    public static final String NDATA = "NDATA";
    public static final String INTERNAL = "INTERNAL";

    private String name;
    private String type;
    private String value;
    private String publicId;
    private String systemId;
    private String ndataValue;
    boolean parameterEntity = false;

    public EntityDecl(String declaration) throws IndexOutOfBoundsException {
        int i = "<!ENTITY".length();
        char c = declaration.charAt(i);
        while (XMLUtils.isXmlSpace(c)) {
            i++;
            c = declaration.charAt(i);
        }
        if (c == '%') {
            // it is a % declaration
            parameterEntity = true;
            i++;
            c = declaration.charAt(i);
            while (XMLUtils.isXmlSpace(c)) {
                i++;
                c = declaration.charAt(i);
            }
        }
        StringBuilder sb = new StringBuilder();
        while (!XMLUtils.isXmlSpace(c)) {
            sb.append(c);
            i++;
            c = declaration.charAt(i);
        }
        name = sb.toString();

        String rest = declaration.substring(i, declaration.length() - ">".length()).strip();

        if (parameterEntity) {
            // it has value or externalId
            if (rest.indexOf(SYSTEM) != -1) {
                type = SYSTEM;
                value = rest.substring(rest.indexOf(SYSTEM) + SYSTEM.length()).strip();
                value = value.substring(1, value.length() - 1);
            } else if (rest.indexOf(PUBLIC) != -1) {
                type = PUBLIC;
                rest = rest.substring(rest.indexOf(PUBLIC) + PUBLIC.length()).strip();
                char delimiter = rest.charAt(0);
                StringBuilder publicBuilder = new StringBuilder();
                i = 1;
                c = rest.charAt(i);
                while (c != delimiter) {
                    publicBuilder.append(c);
                    i++;
                    c = rest.charAt(i);
                }
                publicId = publicBuilder.toString();
                rest = rest.substring(publicId.length() + 2).strip();
                value = rest.substring(1, rest.length() - 1);
            } else {
                type = INTERNAL;
                value = rest.strip();
                value = value.substring(1, value.length() - 1);
            }
        } else {
            // it has value or externalId with NData
            if (rest.indexOf(SYSTEM) != -1) {
                type = SYSTEM;
                rest = rest.substring(rest.indexOf(SYSTEM) + SYSTEM.length()).strip();
                if (rest.indexOf(NDATA) == -1) {
                    value = rest;
                    value = value.substring(1, value.length() - 1);
                } else {
                    value = rest.substring(0, rest.indexOf(NDATA)).strip();
                    value = value.substring(1, value.length() - 1);
                    ndataValue = rest.substring(rest.indexOf(NDATA) + NDATA.length()).strip();
                }
            } else if (rest.indexOf(PUBLIC) != -1) {
                type = PUBLIC;
                rest = rest.substring(PUBLIC.length()).strip();
                rest = rest.substring(PUBLIC.length()).strip();
                char delimiter = rest.charAt(0);
                StringBuilder publicBuilder = new StringBuilder();
                i = 1;
                c = rest.charAt(i);
                while (c != delimiter) {
                    publicBuilder.append(c);
                    i++;
                    c = rest.charAt(i);
                }
                publicId = publicBuilder.toString();
                if (rest.indexOf(NDATA) == -1) {
                    value = rest;
                } else {
                    value = rest.substring(publicId.length() + 2, rest.indexOf(NDATA)).strip();
                    value = value.substring(1, value.length() - 1);
                    ndataValue = rest.substring(rest.indexOf(NDATA) + NDATA.length()).strip();
                }
            } else {
                type = INTERNAL;
                value = rest.strip();
                value = value.substring(1, value.length() - 1);
            }
        }
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    protected void setValue(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public String getPublicId() {
        return publicId;
    }

    public void setSystemId(String systemId) {
        this.systemId = systemId;
    }
    
    public String getSystemId() {
        return systemId;
    }

    public String getNDATA() {
        return ndataValue;
    }

    @Override
    public short getNodeType() {
        return XMLNode.ENTITY_DECL_NODE;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("<!ENTITY ");
        if (parameterEntity) {
            sb.append("% ");
        }
        sb.append(name);
        sb.append(' ');
        switch (type) {
            case INTERNAL:
                char delimiter = value.indexOf("\"") == -1 ? '\"' : '\'';
                sb.append(delimiter);
                sb.append(value);
                sb.append(delimiter);
                break;
            case SYSTEM:
                sb.append(SYSTEM);
                sb.append(' ');
                delimiter = value.indexOf("\"") == -1 ? '\"' : '\'';
                sb.append(delimiter);
                sb.append(value);
                sb.append(delimiter);
                if (ndataValue != null) {
                    sb.append(' ');
                    sb.append(NDATA);
                    sb.append(' ');
                    sb.append(ndataValue);
                }
                break;
            case PUBLIC:
                sb.append(PUBLIC);
                sb.append(' ');
                delimiter = publicId.indexOf("\"") == -1 ? '\"' : '\'';
                sb.append(delimiter);
                sb.append(publicId);
                sb.append(delimiter);
                sb.append(' ');
                sb.append(delimiter);
                sb.append(value);
                sb.append(delimiter);
                if (ndataValue != null) {
                    sb.append(' ');
                    sb.append(NDATA);
                    sb.append(' ');
                    sb.append(ndataValue);
                }
                break;
            default:
                // throw an error?
        }
        sb.append('>');
        return sb.toString();
    }

    @Override
    public void writeBytes(OutputStream output, Charset charset) throws IOException {
        output.write(toString().getBytes(charset));
    }
}
