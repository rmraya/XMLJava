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
import java.util.List;

public class DocumentType implements XMLNode {

    private String name;
    private String publicId;
    private String systemId;
    private List<XMLNode> internalSubset;

    public DocumentType(String name, String publicId, String systemId) {
        this.name = name;
        this.publicId = publicId;
        this.systemId = systemId;
    }

    @Override
    public short getNodeType() {
        return XMLNode.DOCUMENT_TYPE_NODE;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("<!DOCTYPE ");
        sb.append(name);
        if (publicId != null && systemId != null) {
            sb.append(" PUBLIC \"");
            sb.append(publicId);
            sb.append("\" SYSTEM \"");
            sb.append(systemId);
            sb.append('\"');
        } else if (systemId != null) {
            sb.append(" SYSTEM \"");
            sb.append(systemId);
            sb.append('\"');
        }
        if (internalSubset != null) {
            sb.append('[');
            for (int i=0 ; i<internalSubset.size() ; i++) {
                sb.append(internalSubset.get(i).toString());
            }
            sb.append(']');
        }
        sb.append('>');
        return sb.toString();
    }

    @Override
    public void writeBytes(OutputStream output, Charset charset) throws IOException {
        output.write(toString().getBytes(charset));
    }

    public String getSystemId() {
        return systemId;
    }

    public void setSystemId(String systemId) {
        this.systemId = systemId;
    }

    public String getPublicId() {
        return publicId;
    }

    public void setPublicId(String publicId) {
        this.publicId = publicId;
    }

    public void setInternalSubset(List<XMLNode> internalSubset) {
        this.internalSubset = internalSubset;
    }
}
