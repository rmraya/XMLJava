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

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Vector;

public class AttlistDecl implements XMLNode {

    private String listName;
    private List<AttributeDecl> attributes;

    public AttlistDecl(String declaration) {
        attributes = new Vector<>();
        int i = "<!ATTLIST".length();
        declaration = declaration.trim();
        char c = declaration.charAt(i);
        while (XMLUtils.isXmlSpace(c)) {
            i++;
            c = declaration.charAt(i);
        }
        StringBuilder sb = new StringBuilder();
        while (!XMLUtils.isXmlSpace(c)) {
            sb.append(c);
            i++;
            c = declaration.charAt(i);
        }
        listName = sb.toString();
        parseAttributes(declaration.substring(i, declaration.lastIndexOf(">")).trim());
    }

    private void parseAttributes(String declaration) {
        int i = 0;
        while (i < declaration.length()) {
            char c = declaration.charAt(i);

            // Skip whitespace
            while (i < declaration.length() && XMLUtils.isXmlSpace(c)) {
                i++;
                if (i < declaration.length()) {
                    c = declaration.charAt(i);
                }
            }

            // Parse attribute name
            StringBuilder nameBuilder = new StringBuilder();
            while (i < declaration.length() && !XMLUtils.isXmlSpace(c)) {
                nameBuilder.append(c);
                i++;
                if (i < declaration.length()) {
                    c = declaration.charAt(i);
                }
            }
            String name = nameBuilder.toString();

            if (name.startsWith("%") && name.endsWith(";")) {
                // It is a parameter entity reference
                AttributeDecl parameterEntity = new AttributeDecl(name, null, null, false);
                parameterEntity.setParameterEntity(true);
                attributes.add(parameterEntity);
                continue;
            }
            // Skip whitespace
            while (i < declaration.length() && XMLUtils.isXmlSpace(c)) {
                i++;
                if (i < declaration.length()) {
                    c = declaration.charAt(i);
                }
            }

            // Parse attribute type, it can be:
            // StringType: 'CDATA'
            // TokenizedType: 'ID','IDREF','IDREFS','ENTITY','ENTITIES','NMTOKEN' or
            // 'NMTOKENS'
            // or an enumeration od string values

            StringBuilder typeBuilder = new StringBuilder();
            if (c == '(') {
                // It is an enumeration
                while (i < declaration.length() && c != ')') {
                    typeBuilder.append(c);
                    i++;
                    if (i < declaration.length()) {
                        c = declaration.charAt(i);
                    }
                }
                if (c == ')') {
                    typeBuilder.append(c);
                    i++;
                    c = declaration.charAt(i);
                }
            } else {
                while (i < declaration.length() && !XMLUtils.isXmlSpace(c)) {
                    typeBuilder.append(c);
                    i++;
                    if (i < declaration.length()) {
                        c = declaration.charAt(i);
                    }
                }
            }
            String type = typeBuilder.toString();

            // Skip whitespace
            while (i < declaration.length() && XMLUtils.isXmlSpace(c)) {
                i++;
                if (i < declaration.length()) {
                    c = declaration.charAt(i);
                }
            }

            // Parse defaultDecl section
            // DefaultDecl can be:
            // #IMPLIED, #REQUIRED, #FIXED or a quoted string

            if (c == '#') {
                // Parse #IMPLIED, #REQUIRED, or #FIXED
                StringBuilder keywordBuilder = new StringBuilder();
                while (i < declaration.length() && !XMLUtils.isXmlSpace(c)) {
                    keywordBuilder.append(c);
                    i++;
                    if (i < declaration.length()) {
                        c = declaration.charAt(i);
                    }
                }
                String keyword = keywordBuilder.toString();
                if ("#FIXED".equals(keyword)) {
                    StringBuilder defaultValueBuilder = new StringBuilder();

                    // Skip whitespace after #FIXED
                    while (i < declaration.length() && XMLUtils.isXmlSpace(c)) {
                        i++;
                        if (i < declaration.length()) {
                            c = declaration.charAt(i);
                        }
                    }

                    // Parse the fixed default value
                    if (c == '"' || c == '\'') {
                        char quote = c;
                        i++;
                        if (i < declaration.length()) {
                            c = declaration.charAt(i);
                        }
                        while (i < declaration.length() && c != quote) {
                            defaultValueBuilder.append(c);
                            i++;
                            if (i < declaration.length()) {
                                c = declaration.charAt(i);
                            }
                        }
                        if (c == quote) {
                            i++;
                        }
                    } else {
                        throw new IllegalArgumentException("Expected quoted default value after #FIXED.");
                    }
                    attributes.add(new AttributeDecl(name, type, defaultValueBuilder.toString(), true));
                } else {
                    attributes.add(new AttributeDecl(name, type, keyword, false));
                }
            } else if (c == '"' || c == '\'') {
                StringBuilder defaultValueBuilder = new StringBuilder();
                // Default value is quoted
                char quote = c;
                i++;
                if (i < declaration.length()) {
                    c = declaration.charAt(i);
                }
                while (i < declaration.length() && c != quote) {
                    defaultValueBuilder.append(c);
                    i++;
                    if (i < declaration.length()) {
                        c = declaration.charAt(i);
                    }
                }
                if (c == quote) {
                    i++;
                }
                attributes.add(new AttributeDecl(name, type, defaultValueBuilder.toString(), false));
            }
        }
    }

    public String getListName() {
        return listName;
    }

    public List<AttributeDecl> getAttributes() {
        return attributes;
    }

    @Override
    public short getNodeType() {
        return XMLNode.ATTRIBUTE_DECL_NODE;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("<!ATTLIST ");
        sb.append(listName);
        for (AttributeDecl attr : attributes) {
            sb.append("\n  ").append(attr.toString());
        }
        sb.append("\n>");
        return sb.toString();
    }

    @Override
    public void writeBytes(OutputStream output, Charset charset) throws IOException {
        output.write(toString().getBytes(charset));
    }
}