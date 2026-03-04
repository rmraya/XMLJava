/*******************************************************************************
 * Copyright (c) 2022-2026 Maxprograms. All rights reserved.
 *
 * This software is the proprietary property of Maxprograms.
 * Use, modification, and distribution are subject to the terms of the 
 * Software License Agreement found in the root of this distribution 
 *
 * Unauthorized redistribution or commercial use is strictly prohibited.
 *******************************************************************************/
package com.maxprograms.xml;

import java.util.List;
import java.util.Vector;

public class DTDPCData implements ContentParticle {

    @Override
    public int getType() {
        return ContentParticle.PCDATA;
    }

    @Override
    public void setCardinality(int cardinality) {
        // do nothing
    }

    @Override
    public void addParticle(ContentParticle particle) {
        // do nothing
    }

    @Override
    public List<ContentParticle> getParticles() {
        return new Vector<>();
    }

    @Override
    public int getCardinality() {
        return ContentModel.NONE;
    }

    @Override
    public String toString() {
        return "#PCDATA";
    }
}
