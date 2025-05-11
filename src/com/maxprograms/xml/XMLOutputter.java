/*******************************************************************************
 * Copyright (c) 2022 - 2025 Maxprograms.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-v10.html
 *
 * Contributors:
 *     Maxprograms - initial API and implementation
 *******************************************************************************/
package com.maxprograms.xml;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class XMLOutputter {

	private Charset defaultEncoding = StandardCharsets.UTF_8;
	private boolean preserve = false;
	private Map<String, String> entities = null;
	private boolean escape = false;
	private boolean emptyDoctype = false;
	private boolean skipLinefeed;
	private boolean writeBOM;

	private static Logger logger = System.getLogger(XMLOutputter.class.getName());

	public void output(Document sdoc, OutputStream out) throws IOException {
		try (OutputStreamWriter outputWriter = new OutputStreamWriter(out, sdoc.getEncoding())) {
			try (Writer writer = new BufferedWriter(outputWriter)) {
				if (defaultEncoding.equals(StandardCharsets.UTF_16LE)) {
					writer.write(new String(new byte[] { (byte) 0xFF, (byte) 0xFE }, StandardCharsets.UTF_16LE));
				}
				if (defaultEncoding.equals(StandardCharsets.UTF_16BE)) {
					writer.write(new String(new byte[] { (byte) 0xFE, (byte) 0xFF }, StandardCharsets.UTF_16BE));
				}
				if (writeBOM && defaultEncoding.equals(StandardCharsets.UTF_8)) {
					writer.write(
							new String(new byte[] { (byte) 0xEF, (byte) 0xBB, (byte) 0xBF }, StandardCharsets.UTF_8));
				}
				if (!skipLinefeed) {
					writer.write("<?xml version=\"1.0\" encoding=\"" + defaultEncoding + "\" ?>\n");
				} else {
					writer.write("<?xml version=\"1.0\" encoding=\"" + defaultEncoding + "\"?>");
				}
				String doctype = sdoc.getRootElement().getName();
				String publicId = sdoc.getPublicId();
				String systemId = sdoc.getSystemId();
				String internalSubset = sdoc.getInternalSubset();

				List<AttlistDecl> attlistDeclarations = sdoc.getAttlistDeclarations();
				if (attlistDeclarations != null && !attlistDeclarations.isEmpty()) {
					if (internalSubset == null) {
						internalSubset = "";
					}
					for (AttlistDecl attlist : attlistDeclarations) {
						internalSubset += "\n" + attlist.toString();
					}
				}
				if (publicId != null || systemId != null || internalSubset != null) {
					writer.write("<!DOCTYPE " + doctype + " ");
					if (publicId != null) {
						writer.write("PUBLIC \"" + publicId + "\" \"" + systemId + "\"");
						if (internalSubset != null && !internalSubset.isEmpty()) {
							writer.write(" [" + internalSubset + "]>\n");
						} else {
							writer.write(">\n");
						}
					} else {
						if (systemId != null) {
							writer.write("SYSTEM \"" + systemId + "\" ");
						}
						if (internalSubset != null) {
							writer.write("[\n" + internalSubset + "]");
						}
						writer.write(">\n");
					}
				} else {
					if (emptyDoctype) {
						writer.write("<!DOCTYPE " + doctype + ">");
					}
				}
				entities = sdoc.getEntities();
				if (entities == null) {
					entities = new Hashtable<>();
					entities.put("lt", "&#38;#60;");
					entities.put("gt", "&#62;");
					entities.put("amp", "&#38;#38;");
				}
				processHeader(writer, sdoc.getContent());
			}
		}
	}

	private void processHeader(Writer output, List<XMLNode> list) throws IOException {
		int length = list.size();
		for (int i = 0; i < length; i++) {
			XMLNode n = list.get(i);
			switch (n.getNodeType()) {
				case XMLNode.PROCESSING_INSTRUCTION_NODE:
					PI pi = (PI) n;
					output.write("<?" + pi.getTarget() + " " + pi.getData() + "?>");
					if (!preserve) {
						output.write("\n");
					}
					break;
				case XMLNode.DOCUMENT_NODE:
					// DOCTYPE already written
					break;
				case XMLNode.ELEMENT_NODE:
					traverse(output, (Element) n);
					break;
				case XMLNode.COMMENT_NODE:
					Comment c = (Comment) n;
					if (!preserve) {
						output.write("\n<!-- " + c.getText() + " -->");
					} else {
						output.write("<!-- " + c.getText() + " -->");
					}
					break;
				case XMLNode.CDATA_SECTION_NODE:
					CData cd = (CData) n;
					output.write("<![CDATA[" + cd.getData() + "]]>");
					break;
				case XMLNode.TEXT_NODE:
					TextNode tn = (TextNode) n;
					String text = cleanString(tn.getText());
					if (text != null) {
						output.write(text);
					}
					break;
				default:
					// should never happen
					logger.log(Level.WARNING, Messages.getString("XMLOutputter.0"));
			}
		}
	}

	private void traverse(Writer output, Element el) throws IOException {
		String type = el.getName();
		String space = el.getAttributeValue("xml:space", "default");
		if (space.equals("preserve") && !preserve) {
			preserve = true;
		}
		output.write("<" + type);
		List<Attribute> attrs = el.getAttributes();
		for (int i = 0; i < attrs.size(); i++) {
			Attribute a = attrs.get(i);
			output.write(" " + a.toString());
		}
		List<XMLNode> list = el.getContent();
		if (!list.isEmpty()) {
			output.write(">");
			for (int i = 0; i < list.size(); i++) {
				XMLNode n = list.get(i);
				switch (n.getNodeType()) {
					case XMLNode.ELEMENT_NODE:
						traverse(output, (Element) n);
						break;
					case XMLNode.TEXT_NODE:
						TextNode tn = (TextNode) n;
						String text = cleanString(tn.getText());
						if (text == null) {
							text = "";
						}
						if (escape) {
							text = text.replace("\"", "&quot;");
							text = text.replace("'", "&apos;");
						}
						if (preserve) {
							output.write(text);
						} else {
							output.write(normalize(text));
						}
						break;
					case XMLNode.PROCESSING_INSTRUCTION_NODE:
						PI pi = (PI) n;
						output.write("<?" + pi.getTarget() + " " + pi.getData() + "?>");
						break;
					case XMLNode.COMMENT_NODE:
						Comment c = (Comment) n;
						if (!preserve) {
							output.write("\n<!-- " + c.getText() + " -->");
						} else {
							output.write("<!-- " + c.getText() + " -->");
						}
						break;
					case XMLNode.CDATA_SECTION_NODE:
						output.write(n.toString());
						break;
					default:
						// should never happen
						logger.log(Level.WARNING, Messages.getString("XMLOutputter.1"));
				}
			}
			if (!preserve) {
				output.write("</" + type + ">\n");
			} else {
				output.write("</" + type + ">");
			}
		} else {
			if (skipLinefeed) {
				output.write(" />");
			} else {
				output.write("/>");
			}
		}
	}

	private String cleanString(String string) {
		if (string == null) {
			return null;
		}
		String result = string.replace("&", "&amp;");
		result = result.replace("<", "&lt;");
		result = result.replace(">", "&gt;");

		// now replace common text with
		// the entities declared in the DTD

		Set<String> keys = entities.keySet();
		Iterator<String> it = keys.iterator();
		while (it.hasNext()) {
			String key = it.next();
			String value = entities.get(key);
			if (!value.isEmpty() && !key.equals("amp") && !key.equals("lt") && !key.equals("gt")
					&& !key.equals("quot")) {
				result = replaceEntities(result, value, "&" + key + ";");
			}
		}

		// now check for valid characters

		return XMLUtils.validChars(result);

	}

	private static String replaceEntities(String string, String token, String entity) {
		String result = string;
		int index = result.indexOf(token);
		while (index != -1) {
			String before = result.substring(0, index);
			String after = result.substring(index + token.length());
			// check if we are not inside an entity
			int amp = before.lastIndexOf('&');
			if (amp == -1) {
				// we are not in an entity
				result = before + entity + after;
			} else {
				boolean inEntity = true;
				for (int i = amp; i < before.length(); i++) {
					char c = before.charAt(i);
					if (Character.isWhitespace(c) || ";.@$*()[]{},/?\\\"\'+=-^".indexOf(c) != -1) {
						inEntity = false;
						break;
					}
				}
				if (inEntity) {
					// check for a colon in "after"
					int colon = after.indexOf(';');
					if (colon == -1) {
						// we are not in an entity
						result = before + entity + after;
					} else {
						// verify is there is something that breaks the entity before
						for (int i = 0; i < colon; i++) {
							char c = after.charAt(i);
							if (Character.isWhitespace(c) || "&.@$*()[]{},/?\\\"\'+=-^".indexOf(c) != -1) {
								break;
							}
						}
					}
				} else {
					// we are not in an entity
					result = before + entity + after;
				}
			}
			if (index < result.length()) {
				index = result.indexOf(token, index + 1);
			}
		}
		return result;
	}

	private static String normalize(String string) {
		StringBuilder rs = new StringBuilder("");
		int length = string.length();
		for (int i = 0; i < length; i++) {
			char ch = string.charAt(i);
			if (!Character.isSpaceChar(ch)) {
				if (ch != '\n') {
					rs.append(ch);
				} else {
					rs.append(" ");
				}
			} else {
				rs.append(" ");
				while (i < length - 1 && Character.isSpaceChar(string.charAt(i + 1))) {
					i++;
				}
			}
		}
		return rs.toString();
	}

	public void setEncoding(Charset charset) {
		defaultEncoding = charset;
	}

	public void preserveSpace(boolean value) {
		preserve = value;
	}

	public void escapeQuotes(boolean value) {
		escape = value;
	}

	protected static String replaceToken(String string, String token, String newText) {
		String result = string;
		int index = result.indexOf(token);
		while (index != -1) {
			result = result.substring(0, index) + newText + result.substring(index + token.length());
			index = result.indexOf(token, index + newText.length());
		}
		return result;
	}

	public void setEmptyDoctype(boolean b) {
		emptyDoctype = b;
	}

	public void setSkipLinefeed(boolean b) {
		skipLinefeed = b;
	}

	public void writeBOM(boolean b) {
		writeBOM = b;
	}

}
