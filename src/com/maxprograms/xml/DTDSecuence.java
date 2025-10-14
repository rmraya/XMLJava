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

import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.Vector;

public class DTDSecuence implements ContentParticle {

    private int cardinality;
    private List<ContentParticle> content;

    public DTDSecuence() {
        content = new Vector<>();
        cardinality = ContentModel.NONE;
    }

    public void addParticle(ContentParticle particle) {
        content.add(particle);
    }

    @Override
    public int getType() {
        return ContentParticle.SEQUENCE;
    }

    @Override
    public void setCardinality(int cardinality) {
        this.cardinality = cardinality;
    }

    @Override
    public int getCardinality() {
        return cardinality;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("(");
        for (int i = 0; i < content.size(); i++) {
            ContentParticle particle = content.get(i);
            sb.append(particle.toString());
            if (i < content.size() - 1) {
                sb.append(',');
            }
        }
        sb.append(')');
        switch (cardinality) {
            case ContentModel.NONE:
                return sb.toString();
            case ContentModel.OPTIONAL:
                return sb + "?";
            case ContentModel.ONEMANY:
                return sb + "+";
            case ContentModel.ZEROMANY:
                return sb + "*";
            default:
                // ignore
        }
        return sb.toString();
    }

    public List<ContentParticle> getParticles() {
        return content;
    }

    public Set<String> getChildren() {
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
