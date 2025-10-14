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
import java.io.Serializable;
import java.nio.charset.Charset;

public interface XMLNode extends Serializable {

	public final short DOCUMENT_NODE = 0;
	public final short ELEMENT_NODE = 1;
	public final short ATTRIBUTE_NODE = 2;
	public final short CDATA_SECTION_NODE = 3;
	public final short COMMENT_NODE = 4;
	public final short PROCESSING_INSTRUCTION_NODE = 5;
	public final short TEXT_NODE = 6;
	public final short ATTRIBUTE_LIST_NODE = 7;
	public final short ATTRIBUTE_DECL_NODE = 8;
	public final short ELEMENT_DECL_NODE = 9;
	public final short ENTITY_DECL_NODE = 10;
	public final short NOTATION_DECL_NODE = 11;
	public final short XML_DECL_NODE = 12;
	public final short DOCUMENT_TYPE_NODE = 13;

	public short getNodeType();

	@Override
	public String toString();

	@Override
	public boolean equals(Object node);

	public void writeBytes(OutputStream output, Charset charset) throws IOException;

}
