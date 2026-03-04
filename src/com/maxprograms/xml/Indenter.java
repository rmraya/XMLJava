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

import java.util.Iterator;
import java.util.List;
import java.util.Vector;

public class Indenter {

	private static int level;
	private static int numSpaces;

	private Indenter() {
		// do not instantiate
	}

	public static void indent(Element e, int indent, int spaces) {
		level = indent;
		numSpaces = spaces;
		recurse(e);
	}

	public static void indent(Element e, int spaces) {
		level = 1;
		numSpaces = spaces;
		recurse(e);
	}

	private static void recurse(Element e) {
		if ("preserve".equals(e.getAttributeValue("xml:space"))) {
			return;
		}
		if (!hasText(e)) {
			indent(e);
		}
		level++;
		List<Element> children = e.getChildren();
		Iterator<Element> it = children.iterator();
		while (it.hasNext()) {
			recurse(it.next());
		}
		level--;
	}

	private static void indent(Element e) {
		StringBuilder start = new StringBuilder("\n");
		StringBuilder end = new StringBuilder("\n");
		for (int i = 0; i < (level * numSpaces); i++) {
			start.append(' ');
		}
		for (int i = 0; i < ((level - 1) * numSpaces); i++) {
			end.append(' ');
		}
		List<XMLNode> content = new Vector<>();
		List<XMLNode> nodes = e.getContent();
		Iterator<XMLNode> it = nodes.iterator();
		while (it.hasNext()) {
			XMLNode node = it.next();
			if (node.getNodeType() != XMLNode.TEXT_NODE) {
				content.add(new TextNode(start.toString()));
				content.add(node);
			}
		}
		if (!content.isEmpty()) {
			content.add(new TextNode(end.toString()));
		}
		e.setContent(content);
	}

	private static boolean hasText(Element e) {
		List<XMLNode> nodes = e.getContent();
		Iterator<XMLNode> it = nodes.iterator();
		while (it.hasNext()) {
			XMLNode node = it.next();
			if (node.getNodeType() == XMLNode.TEXT_NODE) {
				TextNode t = (TextNode) node;
				String text = t.getText();
				if (text != null) {
					for (int i = 0; i < text.length(); i++) {
						char c = text.charAt(i);
						if (c == '\u00A0') {
							return true;
						}
						if (!(Character.isSpaceChar(c) || c == '\n' || c == '\r' || c == '\t' || c == '\u00A0')) {
							return true;
						}
					}
				}
			}
		}
		return false;
	}
}
