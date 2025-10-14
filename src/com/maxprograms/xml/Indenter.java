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
