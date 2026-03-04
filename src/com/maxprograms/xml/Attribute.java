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

public class Attribute implements XMLNode, Comparable<Attribute> {

	private static final long serialVersionUID = -859299907013846457L;
	private String name;
	private String value;

	protected Attribute() {
		name = "";
		value = "";
	}

	public Attribute(String name, String value) {
		this.name = name;
		this.value = value;
	}

	public String getValue() {
		return value;
	}

	public String getName() {
		return name;
	}

	@Override
	public String toString() {
		return name + "=\"" + XMLUtils.cleanText(value).replace("\"", "&quot;") + "\"";
	}

	@Override
	public short getNodeType() {
		return XMLNode.ATTRIBUTE_NODE;
	}

	public void setValue(String string) {
		value = string;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof Attribute)) {
			return false;
		}
		Attribute a = (Attribute) obj;
		return a.getName().equals(name) && a.getValue().equals(value);
	}

	@Override
	public void writeBytes(OutputStream output, Charset charset) throws IOException {
		output.write(toString().getBytes(charset));
	}

	@Override
	public int compareTo(Attribute o) {
		return name.compareTo(o.name);
	}

	@Override
	public int hashCode() {
		return toString().hashCode();
	}

	public String getLocalName() {
		if (name.indexOf(':') == -1) {
			return name;
		}
		return name.substring(name.indexOf(':') + 1);
	}

	public String getNamespace() {
		if (name.indexOf(':') == -1) {
			return "";
		}
		return name.substring(0, name.indexOf(':'));
	}

}
