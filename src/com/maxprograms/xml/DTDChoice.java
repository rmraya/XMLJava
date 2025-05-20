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

import java.util.List;
import java.util.Vector;

public class DTDChoice implements ContentParticle {

    private int cardinality;
    private List<ContentParticle> content;

    public DTDChoice() {
        content = new Vector<>();
        cardinality = ContentModel.NONE;
    }

    public void addParticle(ContentParticle particle) {
        content.add(particle);
    }

    @Override
    public int getType() {
        return ContentParticle.CHOICE;
    }

    @Override
    public int getCardinality() {
        return cardinality;
    }

    @Override
    public void setCardinality(int cardinality) {
        this.cardinality = cardinality;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("(");
        for (int i = 0; i < content.size(); i++) {
            ContentParticle particle = content.get(i);
            sb.append(particle.toString());
            if (i < content.size() - 1) {
                sb.append(" | ");
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
}
