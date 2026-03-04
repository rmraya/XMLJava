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

public class TextNode implements XMLNode {

	private static final long serialVersionUID = 2837146125080492272L;
	private String text;

	public TextNode(String value) {
		text = value;
	}

	@Override
	public short getNodeType() {
		return XMLNode.TEXT_NODE;
	}

	@Override
	public String toString() {
		return XMLUtils.cleanText(text);
	}

	@Override
	public void writeBytes(OutputStream output, Charset charset) throws IOException {
		output.write(toString().getBytes(charset));
	}

	public String getText() {
		return text;
	}

	public void setText(String value) {
		text = value;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof TextNode)) {
			return false;
		}
		return text.equals(((TextNode) obj).getText());
	}

	@Override
	public int hashCode() {
		return toString().hashCode();
	}

}
