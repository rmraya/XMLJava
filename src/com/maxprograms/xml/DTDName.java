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
import java.util.Vector;

public class DTDName implements ContentParticle {

    private String name;
    private int cardinality;

    public DTDName(String name) {
        this.name = name;
        this.cardinality = ContentModel.NONE;
    }

    public String getName() {
        return name;
    }
    
    @Override
    public int getType() {
        return ContentParticle.NAME;
    }

    @Override
    public int getCardinality() {
        return cardinality;
    }

    @Override
    public void addParticle(ContentParticle particle) {
        // do nothing
    }

    @Override
    public List<ContentParticle> getParticles() {
        List<ContentParticle> result = new Vector<>();
        result.add(this);
        return result;
    }

    @Override
    public String toString() {
        switch (cardinality) {
            case ContentModel.NONE:
                return name;
            case ContentModel.OPTIONAL:
                return name + "?";
            case ContentModel.ONEMANY:
                return name + "+";
            case ContentModel.ZEROMANY:
                return name + "*";
            default:
                // ignore
                return "";
        }
    }

    @Override
    public void setCardinality(int cardinality) {
        this.cardinality = cardinality;
    }
}
