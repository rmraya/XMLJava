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

public class CData implements XMLNode {

	private static final long serialVersionUID = 610927332260249086L;
	private String value;

	public CData(String data) {
		value = data;
	}

	public String getData() {
		return value;
	}

	@Override
	public String toString() {
		return "<![CDATA[" + value + "]]>";
	}

	@Override
	public short getNodeType() {
		return XMLNode.CDATA_SECTION_NODE;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof CData)) {
			return false;
		}
		CData cd = (CData) obj;
		return value.equals(cd.getData());
	}

	@Override
	public void writeBytes(OutputStream output, Charset charset) throws IOException {
		output.write(toString().getBytes(charset));
	}

	@Override
	public int hashCode() {
		return toString().hashCode();
	}

}
