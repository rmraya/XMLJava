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
