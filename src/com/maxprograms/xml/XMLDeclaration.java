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

public class XMLDeclaration implements XMLNode {

    private String version;
    private String encoding;
    private String standalone;

    public XMLDeclaration(String version, String encoding, String standalone) {
        this.version = version;
        this.encoding = encoding;
        this.standalone = standalone;
    }

    @Override
    public short getNodeType() {
        return XMLNode.XML_DECL_NODE;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("<?xml");
        if (version != null) {
            sb.append(" version=\"");
            sb.append(version);
            sb.append("\"");
        }
        if (encoding != null) {
            sb.append(" encoding=\"");
            sb.append(encoding);
            sb.append("\"");
        }
        if (standalone != null) {
            sb.append(" standalone=\"");
            sb.append(standalone);
            sb.append("\"");
        }
        sb.append("?>");
        return sb.toString();
    }

    @Override
    public void writeBytes(OutputStream output, Charset charset) throws IOException {
        output.write(toString().getBytes(charset));
    }

    public String getVersion() {
        return version;
    }

    public String getEncoding() {
        return encoding;
    }

    public String getStandalone() {
        return standalone;
    }
}
