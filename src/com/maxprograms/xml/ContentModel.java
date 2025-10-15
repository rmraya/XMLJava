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

import java.text.MessageFormat;
import java.util.List;
import java.util.Set;
import java.util.Stack;
import java.util.StringTokenizer;
import java.util.TreeSet;
import java.util.Vector;

public class ContentModel {

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
                MessageFormat mf = new MessageFormat(Messages.getString("ContentModel.0"));
                throw new IllegalArgumentException(mf.format(new String[] { modelString }));
            }
        }

        // Handle element content (sequence/choice/groups)
        StringTokenizer st = new StringTokenizer(string, "()|,?*+", true);
        Stack<List<Object>> stack = new Stack<>();
        List<Object> current = new Vector<>();

        while (st.hasMoreTokens()) {
            String token = st.nextToken().trim();

            if (token.equals("(")) {
                stack.push(current);
                current = new Vector<>();
            } else if (token.equals(")")) {
                ContentParticle groupParticle = processGroup(current);
                current = stack.pop();
                current.add(groupParticle);
            } else if ("*".equals(token) || "+".equals(token) || "?".equals(token)) {
                if (current.isEmpty()) {
                    MessageFormat mf = new MessageFormat(Messages.getString("ContentModel.1"));
                    throw new IllegalArgumentException(mf.format(new String[] { token }));
                }
                Object lastObject = current.get(current.size() - 1);
                if (!(lastObject instanceof ContentParticle)) {
                    MessageFormat mf = new MessageFormat(Messages.getString("ContentModel.1"));
                    throw new IllegalArgumentException(mf.format(new String[] { token }));
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
                MessageFormat mf = new MessageFormat(Messages.getString("ContentModel.2"));
                throw new IllegalArgumentException(mf.format(new String[] { modelString }));
            }
            particles.add((ContentParticle) obj);
        }

        return new ContentModel(particles, type);
    }

    private static ContentParticle processGroup(List<Object> group) {
        if (group.isEmpty()) {
            throw new IllegalArgumentException(Messages.getString("ContentModel.3"));
        }
        if (group.size() == 1) {
            Object obj = group.get(0);
            if (obj instanceof ContentParticle particle) {
                return particle;
            } else if (obj instanceof String name) {
                return new DTDName(name);
            }
        }
        String sep = null;
        for (Object obj : group) {
            if (obj instanceof String token) {
                if ("|".equals(token) || ",".equals(token)) {
                    sep = token;
                    break;
                }
            }
        }
        if (sep == null) {
            throw new IllegalArgumentException(Messages.getString("ContentModel.4"));
        }
        ContentParticle result = "|".equals(sep) ? new DTDChoice() : new DTDSecuence();
        for (Object obj : group) {
            if ("|".equals(obj) || ",".equals(obj)) {
                continue;
            }
            if (obj instanceof ContentParticle particle) {
                result.addParticle(particle);
            } else if (obj instanceof String name) {
                result.addParticle(new DTDName(name));
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
                MessageFormat mf = new MessageFormat(Messages.getString("ContentModel.5"));
                throw new IllegalArgumentException(mf.format(new String[] { string }));
            }
        }
        if (balance != 0) {
            MessageFormat mf = new MessageFormat(Messages.getString("ContentModel.5"));
            throw new IllegalArgumentException(mf.format(new String[] { string }));
        }
    }

    @Override
    public String toString() {
        if (type.equals(EMPTY)) {
            return EMPTY;
        }
        if (type.equals(ANY)) {
            return ANY;
        }
        if (content.isEmpty()) {
            return "";
        }
        
        StringBuilder sb = new StringBuilder();
        
        // For MIXED content, handle specially
        if (type.equals(MIXED)) {
            sb.append("(");
            for (int i = 0; i < content.size(); i++) {
                ContentParticle particle = content.get(i);
                sb.append(particle.toString());
                if (i < content.size() - 1) {
                    sb.append("|");
                }
            }
            sb.append(")*");
            return sb.toString();
        }
        
        // For CHILDREN content, the particles themselves determine the structure
        for (int i = 0; i < content.size(); i++) {
            ContentParticle particle = content.get(i);
            sb.append(particle.toString());
            if (i < content.size() - 1) {
                // This should not happen as CHILDREN content typically has a single root particle
                sb.append(",");
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

    public Set<String> getChildren() {
        if (type.equals(EMPTY)) {
            return new TreeSet<>();
        }
        Set<String> children = new TreeSet<>();
        for (ContentParticle particle : content) {
            if (particle instanceof DTDName name) {
                children.add(name.getName());
            }
            if (particle instanceof DTDChoice choice) {
                children.addAll(choice.getChildren());
            }
            if (particle instanceof DTDSecuence sequence) {
                children.addAll(sequence.getChildren());
            }
        }
        return children;
    }
}
