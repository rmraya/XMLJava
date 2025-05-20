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

import java.io.Serializable;
import java.util.List;
import java.util.Stack;
import java.util.StringTokenizer;
import java.util.Vector;

public class ContentModel implements Serializable {

    public static final String EMPTY = "EMPTY";
    public static final String ANY = "ANY";
    public static final String MIXED = "Mixed";
    public static final String PCDATA = "#PCDATA";
    public static final String CHILDREN = "Children";

    // cardinality
    public static final int NONE = 0;
    public static final int OPTIONAL = 1; // ?
    public static final int ZEROMANY = 2; // *
    public static final int ONEMANY = 3; // +

    private List<ContentParticle> content;
    private String type = EMPTY;

    private ContentModel(List<ContentParticle> content, String type) {
        this.content = content;
        this.type = type;
    }

    public static ContentModel parse(String modelString) {
        String string = modelString.replaceAll("\\s+", "");
        validateParentheses(string);
        List<ContentParticle> particles = new Vector<>();
        String type = CHILDREN;

        // Handle EMPTY and ANY
        if (string.equals(EMPTY)) {
            type = EMPTY;
            return new ContentModel(particles, type);
        }
        if (string.equals(ANY)) {
            type = ANY;
            return new ContentModel(particles, type);
        }

        // Handle pure PCDATA
        if (string.equals("(#PCDATA)")) {
            particles.add(new DTDPCData());
            return new ContentModel(particles, MIXED);
        }

        // Handle mixed content
        if (string.startsWith("(#PCDATA")) {
            type = MIXED;
            if (!string.endsWith(")*")) {
                throw new IllegalArgumentException("Invalid mixed content model: " + string);
            }
        }

        // Handle element content (sequence/choice/groups)
        StringTokenizer st = new StringTokenizer(string, "()|,?*+", true);
        Stack<List<Object>> stack = new Stack<>();
        List<Object> current = new Vector<>();

        while (st.hasMoreTokens()) {
            String token = st.nextToken().trim();
            validateToken(token);

            if (token.equals("(")) {
                stack.push(current);
                current = new Vector<>();
            } else if (token.equals(")")) {
                ContentParticle groupParticle = processGroup(current);
                current = stack.pop();
                current.add(groupParticle);
            } else if ("*".equals(token) || "+".equals(token) || "?".equals(token)) {
                if (current.isEmpty()) {
                    throw new IllegalArgumentException(
                            "Cardinality operator '" + token + "' must follow a valid particle.");
                }
                Object lastObject = current.get(current.size() - 1);
                if (!(lastObject instanceof ContentParticle)) {
                    throw new IllegalArgumentException(
                            "Cardinality operator '" + token + "' must follow a valid particle.");
                }
                int cardinality = "?".equals(token) ? OPTIONAL : ("*".equals(token) ? ZEROMANY : ONEMANY);
                ((ContentParticle) lastObject).setCardinality(cardinality);
            } else if ("|".equals(token) || ",".equals(token)) {
                current.add(token);
            } else if (PCDATA.equals(token)) {
                current.add(new DTDPCData());
            } else {
                current.add(new DTDName(token));
            }
        }

        for (Object obj : current) {
            if (!(obj instanceof ContentParticle)) {
                throw new IllegalArgumentException("Invalid content model: " + string);
            }
            particles.add((ContentParticle) obj);
        }

        return new ContentModel(particles, type);
    }

    private static ContentParticle processGroup(List<Object> group) {
        if (group.isEmpty()) {
            throw new IllegalArgumentException("Empty group found in content model.");
        }
        if (group.size() == 1) {
            Object obj = group.get(0);
            if (obj instanceof ContentParticle) {
                return (ContentParticle) obj;
            } else if (obj instanceof String) {
                return new DTDName((String) obj);
            }
        }
        String sep = null;
        for (Object obj : group) {
            if (obj instanceof String) {
                String token = (String) obj;
                if ("|".equals(token) || ",".equals(token)) {
                    sep = token;
                    break;
                }
            }
        }
        if (sep == null) {
            throw new IllegalArgumentException("No separator found in group: " + group);
        }
        ContentParticle result = "|".equals(sep) ? new DTDChoice() : new DTDSecuence();
        for (Object obj : group) {
            if ("|".equals(obj) || ",".equals(obj)) {
                continue;
            }
            if (obj instanceof ContentParticle) {
                result.addParticle((ContentParticle) obj);
            } else if (obj instanceof String) {
                result.addParticle(new DTDName((String) obj));
            }
        }
        return result;
    }

    private static void validateParentheses(String string) {
        int balance = 0;
        for (char c : string.toCharArray()) {
            if (c == '(')
                balance++;
            else if (c == ')')
                balance--;
            if (balance < 0) {
                throw new IllegalArgumentException("Unbalanced parentheses in content model: " + string);
            }
        }
        if (balance != 0) {
            throw new IllegalArgumentException("Unbalanced parentheses in content model: " + string);
        }
    }

    private static void validateToken(String token) {
        if (!token.matches("[a-zA-Z0-9#|,?*+()]+")) {
            throw new IllegalArgumentException("Invalid token in content model: " + token);
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        String separator = type.equals("|") ? "|" : ",";
        for (int i = 0; i < content.size(); i++) {
            ContentParticle particle = content.get(i);
            sb.append(particle.toString());
            if (i < content.size() - 1) {
                sb.append(separator);
            }
        }
        return sb.toString();
    }

    public List<ContentParticle> getContent() {
        return content;
    }

    public String getType() {
        return type;
    }
}
