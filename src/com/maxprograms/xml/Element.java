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
import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

public class Element implements XMLNode {

	private static final long serialVersionUID = 3004362075586010085L;

	private String name;
	private List<XMLNode> content;
	private Map<String, Attribute> attsTable;

	private static Logger logger = System.getLogger(Element.class.getName());

	public Element() {
		name = "";
		attsTable = new Hashtable<>();
		content = new Vector<>();
	}

	public Element(String name) {
		this.name = name;
		attsTable = new Hashtable<>();
		content = new Vector<>();
	}

	public void addContent(XMLNode n) {
		content.add(n);
	}

	public void addContent(Element e) {
		content.add(e);
	}

	public void addContent(Comment c) {
		content.add(c);
	}

	public void addContent(PI pi) {
		content.add(pi);
	}

	public void addContent(CData c) {
		content.add(c);
	}

	public void addContent(String text) {
		content.add(new TextNode(text));
	}

	public void addContent(TextNode text) {
		content.add(text);
	}

	public void clone(Element src) {
		name = src.getName();
		attsTable = new Hashtable<>();
		List<Attribute> atts = src.getAttributes();
		Iterator<Attribute> it = atts.iterator();
		while (it.hasNext()) {
			Attribute a = it.next();
			Attribute n = new Attribute(a.getName(), a.getValue());
			attsTable.put(n.getName(), n);
		}
		content = new Vector<>();
		List<XMLNode> cont = src.getContent();
		Iterator<XMLNode> ic = cont.iterator();
		while (ic.hasNext()) {
			XMLNode node = ic.next();
			switch (node.getNodeType()) {
				case XMLNode.TEXT_NODE:
					content.add(new TextNode(((TextNode) node).getText()));
					break;
				case XMLNode.ELEMENT_NODE:
					Element e = new Element();
					e.clone((Element) node);
					content.add(e);
					break;
				case XMLNode.PROCESSING_INSTRUCTION_NODE:
					content.add(new PI(((PI) node).getTarget(), ((PI) node).getData()));
					break;
				case XMLNode.COMMENT_NODE:
					content.add(new Comment(((Comment) node).getText()));
					break;
				case XMLNode.CDATA_SECTION_NODE:
					content.add(new CData(((CData) node).getData()));
					break;
				default:
					// should never happen
					logger.log(Level.WARNING, Messages.getString("Element.0"));
			}
		}
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof Element)) {
			return false;
		}
		Element s = (Element) obj;
		if (!name.equals(s.getName())) {
			return false;
		}
		if (attsTable.size() != s.attsTable.size()) {
			return false;
		}
		Set<String> keys = attsTable.keySet();
		Iterator<String> it = keys.iterator();
		while (it.hasNext()) {
			Attribute a = attsTable.get(it.next());
			Attribute b = s.getAttribute(a.getName());
			if (b == null) {
				return false;
			}
			if (!a.equals(b)) {
				return false;
			}
		}
		mergeText();
		s.mergeText();
		return content.equals(s.getContent());
	}

	private void mergeText() {
		if (content.size() < 2) {
			return;
		}
		List<XMLNode> newContent = new Vector<>();
		for (int i = 0; i < content.size(); i++) {
			XMLNode n = content.get(i);
			if (n == null) {
				// should not happen!
				continue;
			}
			if (n.getNodeType() == XMLNode.TEXT_NODE && !newContent.isEmpty()) {
				if (newContent.get(newContent.size() - 1).getNodeType() == XMLNode.TEXT_NODE) {
					TextNode t = (TextNode) newContent.get(newContent.size() - 1);
					StringBuilder buffer = new StringBuilder(t.getText());
					buffer.append(((TextNode) n).getText());
					while (i < content.size() - 1 && content.get(i + 1).getNodeType() == XMLNode.TEXT_NODE) {
						XMLNode next = content.get(i + 1);
						buffer.append(((TextNode) next).getText());
						i++;
					}
					t.setText(buffer.toString());
				} else {
					newContent.add(n);
				}
			} else {
				newContent.add(n);
			}
		}
		content = newContent;
	}

	public Attribute getAttribute(String attributeName) {
		return attsTable.get(attributeName);
	}

	public List<Attribute> getAttributes() {
		List<Attribute> result = new Vector<>();
		result.addAll(attsTable.values());
		return result;
	}

	public String getAttributeValue(String attributeName) {
		Attribute a = attsTable.get(attributeName);
		return a != null ? a.getValue() : "";
	}

	public boolean hasAttribute(String attributeName) {
		return attsTable.containsKey(attributeName);
	}

	public String getAttributeValue(String attributeName, String defaultValue) {
		Attribute a = attsTable.get(attributeName);
		return a != null ? a.getValue() : defaultValue;
	}

	public Element getChild(String tagName) {
		for (int i = 0; i < content.size(); i++) {
			XMLNode node = content.get(i);
			if (node.getNodeType() == XMLNode.ELEMENT_NODE && ((Element) node).getName().equals(tagName)) {
				return (Element) node;
			}
		}
		return null;
	}

	public List<Element> getChildren() {
		List<Element> result = new Vector<>();
		for (int i = 0; i < content.size(); i++) {
			XMLNode node = content.get(i);
			if (node.getNodeType() == XMLNode.ELEMENT_NODE) {
				result.add((Element) node);
			}
		}
		return result;
	}

	public List<Element> getChildren(String tagname) {
		List<Element> result = new Vector<>();
		for (int i = 0; i < content.size(); i++) {
			XMLNode node = content.get(i);
			if (node.getNodeType() == XMLNode.ELEMENT_NODE && ((Element) node).getName().equals(tagname)) {
				result.add((Element) node);
			}
		}
		return result;
	}

	public List<XMLNode> getContent() {
		mergeText();
		return content;
	}

	public String getName() {
		return name;
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

	@Override
	public short getNodeType() {
		return XMLNode.ELEMENT_NODE;
	}

	public String getText() {
		StringBuilder result = new StringBuilder("");
		for (int i = 0; i < content.size(); i++) {
			XMLNode node = content.get(i);
			if (node.getNodeType() == XMLNode.TEXT_NODE) {
				result.append(((TextNode) node).getText());
			}
			if (node.getNodeType() == XMLNode.ELEMENT_NODE) {
				result.append(((Element) node).getText());
			}
		}
		return result.toString();
	}

	public String getTextNormalize() {
		String result = getText();
		result = result.replaceAll("\\s\\s+", " ");
		return result.trim();
	}

	public void removeAttribute(String attributeName) {
		attsTable.remove(attributeName);
	}

	public void removeChild(String string) {
		List<XMLNode> newContent = new Vector<>();
		for (int i = 0; i < content.size(); i++) {
			XMLNode node = content.get(i);
			if (node.getNodeType() == XMLNode.ELEMENT_NODE) {
				Element e = (Element) node;
				if (string.equals(e.getName())) {
					continue;
				}
			}
			newContent.add(node);
		}
		content = newContent;
	}

	public void removeChild(Element child) {
		List<XMLNode> newContent = new Vector<>();
		for (int i = 0; i < content.size(); i++) {
			XMLNode node = content.get(i);
			if (node.getNodeType() == XMLNode.ELEMENT_NODE) {
				Element e = (Element) node;
				if (child.equals(e)) {
					continue;
				}
			}
			newContent.add(node);
		}
		content = newContent;
	}

	public void setAttribute(String attributeName, String value) {
		Attribute a = new Attribute(attributeName, value);
		attsTable.put(attributeName, a);
	}

	public void setAttribute(Attribute a) {
		attsTable.put(a.getName(), a);
	}

	public void setContent(List<XMLNode> c) {
		content = c;
	}

	public void setText(String text) {
		content.clear();
		content.add(new TextNode(text));
	}

	public String getHead() {
		StringBuilder result = new StringBuilder("<" + name);
		List<String> keys = new Vector<>();
		keys.addAll(attsTable.keySet());
		Collections.sort(keys);
		Iterator<String> it = keys.iterator();
		while (it.hasNext()) {
			Attribute a = attsTable.get(it.next());
			result.append(' ');
			result.append(a.toString());
		}
		result.append(">");
		return result.toString();
	}

	public String getTail() {
		return "</" + name + ">";
	}

	@Override
	public String toString() {
		StringBuilder result = new StringBuilder("<" + name);
		List<String> keys = new Vector<>();
		keys.addAll(attsTable.keySet());
		Collections.sort(keys);
		Iterator<String> it = keys.iterator();
		while (it.hasNext()) {
			Attribute a = attsTable.get(it.next());
			result.append(' ');
			result.append(a.toString());
		}
		if (content.isEmpty()) {
			result.append("/>");
			return result.toString();
		}
		result.append(">");
		for (int i = 0; i < content.size(); i++) {
			result.append(content.get(i).toString());
		}
		result.append("</");
		result.append(name);
		result.append(">");
		return result.toString();
	}

	public List<PI> getPI() {
		List<PI> result = new Vector<>();
		for (int i = 0; i < content.size(); i++) {
			XMLNode n = content.get(i);
			if (n.getNodeType() == XMLNode.PROCESSING_INSTRUCTION_NODE) {
				result.add((PI) n);
			}
		}
		return result;
	}

	public List<PI> getPI(String target) {
		List<PI> result = new Vector<>();
		for (int i = 0; i < content.size(); i++) {
			XMLNode n = content.get(i);
			if (n.getNodeType() == XMLNode.PROCESSING_INSTRUCTION_NODE && ((PI) n).getTarget().equals(target)) {
				result.add((PI) n);
			}
		}
		return result;
	}

	public void removePI(String string) {
		List<XMLNode> newContent = new Vector<>();
		for (int i = 0; i < content.size(); i++) {
			XMLNode node = content.get(i);
			if (node.getNodeType() == XMLNode.PROCESSING_INSTRUCTION_NODE) {
				PI pi = (PI) node;
				if (string.equals(pi.getTarget())) {
					continue;
				}
			}
			newContent.add(node);
		}
		content = newContent;
	}

	public String getPrefix() {
		String[] parts = name.split(":");
		if (parts.length == 2) {
			return parts[0];
		}
		return null;
	}

	public void setPrefix(String prfx) {
		String[] parts = name.split(":");
		if (parts.length == 2) {
			name = prfx + ':' + parts[1];
		} else {
			name = prfx + ":" + name;
		}
	}

	public void setAttributes(List<Attribute> list) {
		attsTable.clear();
		Iterator<Attribute> it = list.iterator();
		while (it.hasNext()) {
			Attribute a = it.next();
			setAttribute(a.getName(), a.getValue());
		}
	}

	public void setChildren(List<Element> c) {
		content.clear();
		content.addAll(c);
	}

	@Override
	public void writeBytes(OutputStream output, Charset charset) throws IOException {
		output.write(toString().getBytes(charset));
	}

	@Override
	public int hashCode() {
		return toString().hashCode();
	}

	public void addContent(List<XMLNode> list) {
		content.addAll(list);
	}

}
