/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package sami.uilanguage.toui;

import java.util.ArrayList;

/**
 *
 * @author pscerri
 */
public class Importance implements java.io.Serializable {

    /**
     * 0 is most important, everything else is relative
     */
    public int base = Integer.MAX_VALUE;
    
    /**
     * Objects (e.g., proxies, missions) to which this relates
     */
    public ArrayList<Object> relevantTo = null;
    
    /**
     * (Roughly) a factor for how this will change over time, +ve means descreasing importance
     * 
     * I.e., importance 10s from time of receipt will be base + (10 * temporalPressure)
     * (assume appropriate bounds, e.g., never less than zero)
     * 
     * In the future we might want other than linear changes.
     */
    public double temporalPressure = 1.0;
    
    /**
     * Some number that tells the UI how much the operator will have to think about the thing this importance is attached to
     * 
     * 1.0 all their brain
     * 0.0 no brainer
     * > 1.0 too much for this operator
     * < 0.0 effort relieving?  (Probably makes no sense)
     * 
     * Perhaps not the right place for this?
     * 
     */
    public double cognitiveLoad = 1.0;

    public int getBase() {
        return base;
    }

    public void setBase(int base) {
        this.base = base;
    }

    public ArrayList<Object> getRelevantTo() {
        return relevantTo;
    }

    public void setRelevantTo(ArrayList<Object> relevantTo) {
        this.relevantTo = relevantTo;
    }

    public double getTemporalPressure() {
        return temporalPressure;
    }

    public void setTemporalPressure(double temporalPressure) {
        this.temporalPressure = temporalPressure;
    }

    public double getCognitiveLoad() {
        return cognitiveLoad;
    }

    public void setCognitiveLoad(double cognitiveLoad) {
        this.cognitiveLoad = cognitiveLoad;
    }
        
}
