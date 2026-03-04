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

import java.util.List;
import java.util.Vector;

public class DTDName implements ContentParticle {

    private String dtdName;
    private int cardinality;

    public DTDName(String name) {
        dtdName = name;
        cardinality = ContentModel.NONE;
    }

    public String getName() {
        return dtdName;
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
                return dtdName;
            case ContentModel.OPTIONAL:
                return dtdName + "?";
            case ContentModel.ONEMANY:
                return dtdName + "+";
            case ContentModel.ZEROMANY:
                return dtdName + "*";
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
