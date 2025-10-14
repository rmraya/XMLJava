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
