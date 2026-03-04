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
