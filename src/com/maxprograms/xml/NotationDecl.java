/*******************************************************************************
 * Copyright (c) 2022-2026 Maxprograms. All rights reserved.
 *
 * This software is the proprietary property of Maxprograms.
 * Use, modification, and distribution are subject to the terms of the 
 * Software License Agreement found in the root of this distribution 
 * and at http://www.maxprograms.com/
 *
 * Unauthorized redistribution or commercial use is strictly prohibited.
 *******************************************************************************/
package com.maxprograms.xml;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;

public class NotationDecl implements XMLNode {

    private String name;
    private String type;
    private String value;
    private String referenced;

    public NotationDecl(String declaration) {
        int i = "<!NOTATION".length();
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
        name = sb.toString();
        while (XMLUtils.isXmlSpace(c)) {
            i++;
            c = declaration.charAt(i);
        }
        sb = new StringBuilder();
        while (!XMLUtils.isXmlSpace(c)) {
            sb.append(c);
            i++;
            c = declaration.charAt(i);
        }
        type = sb.toString();
        while (XMLUtils.isXmlSpace(c)) {
            i++;
            c = declaration.charAt(i);
        }
        char delimiter = declaration.charAt(i);
        sb = new StringBuilder();
        i++;
        c = declaration.charAt(i);
        while (c != delimiter) {
            sb.append(c);
            i++;
            c = declaration.charAt(i);
        }
        value = sb.toString();
        referenced = declaration.substring(i + 1, declaration.indexOf('>')).trim();
    }

    public String getName() {
        return name;
    }

    @Override
    public short getNodeType() {
        return XMLNode.NOTATION_DECL_NODE;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("<!NOTATION ");
        sb.append(name);
        sb.append(' ');
        sb.append(type);
        sb.append(' ');
        char delimiter = value.indexOf("\"") == -1 ? '\"' : '\'';
        sb.append(delimiter);
        sb.append(value);
        sb.append(delimiter);
        if (!referenced.isEmpty()) {
            sb.append(' ');
            delimiter = referenced.indexOf("\"") == -1 ? '\"' : '\'';
            sb.append(delimiter);
            sb.append(referenced);
            sb.append(delimiter);
        }
        sb.append('>');
        return super.toString();
    }

    @Override
    public void writeBytes(OutputStream output, Charset charset) throws IOException {
        output.write(toString().getBytes(charset));
    }
}
