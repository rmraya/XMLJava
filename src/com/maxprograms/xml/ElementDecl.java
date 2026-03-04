/*******************************************************************************
 * Copyright (c) 2022-2026 Maxprograms. All rights reserved.
 *
 * This software is the proprietary property of Maxprograms.
 * Use, modification, and distribution are subject to the terms of the 
 * Software License Agreement found in the root of this distribution 
 *
 * Unauthorized redistribution or commercial use is strictly prohibited.
 *******************************************************************************/
package com.maxprograms.xml;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;

public class ElementDecl implements XMLNode {

    private String name;
    private ContentModel model;

    public ElementDecl(String declaration) {
        int i = "<!ELEMENT".length();
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
        model = ContentModel.parse(declaration.substring(i, declaration.lastIndexOf('>')).trim());
    }

    public String getName() {
        return name;
    }

    public ContentModel getModel() {
        return model;
    }
    
    @Override
    public short getNodeType() {
        return XMLNode.ELEMENT_DECL_NODE;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("<!ELEMENT ");
        sb.append(name);
        sb.append(' ');
        sb.append(model);
        sb.append('>');
        return sb.toString();
    }

    @Override
    public void writeBytes(OutputStream output, Charset charset) throws IOException {
        output.write(toString().getBytes(charset));
    }
}
