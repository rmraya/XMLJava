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

public class Comment implements XMLNode {

	private static final long serialVersionUID = 4121792990661005580L;
	private String value;

	protected Comment(String text) {
		value = text;
	}

	public String getText() {
		return value;
	}

	public void setText(String text) {
		value = text;
	}

	@Override
	public String toString() {
		return "<!-- " + value + " -->";
	}

	@Override
	public short getNodeType() {
		return XMLNode.COMMENT_NODE;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof Comment)) {
			return false;
		}
		Comment c = (Comment) obj;
		return value.equals(c.getText());
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
